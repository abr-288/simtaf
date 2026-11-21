package com.menewa.giving;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.menewa.catalog.CatalogLookup;
import com.menewa.identity.IdentityDirectory;
import com.menewa.identity.IdentityPermissions;
import com.menewa.identity.IdentityUser;
import com.menewa.platform.Audit;
import com.menewa.platform.Notifications;
import com.menewa.platform.Problems;
import com.menewa.platform.Reference;
import com.menewa.platform.Settings;
import com.menewa.rewards.Reputation;
import com.menewa.rewards.Rewards;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Panier, création et cycle de vie des demandes de dons (règles reprises de Menewa Naya). */
@Service
@Transactional
class Requests {
    static final Map<String, String> ANNEX_LABELS = new LinkedHashMap<>();
    static {
        ANNEX_LABELS.put("packaging", "Personnalisation du packaging");
        ANNEX_LABELS.put("officiant", "Intervention d'un officiant religieux");
        ANNEX_LABELS.put("photos", "Prises de photos");
        ANNEX_LABELS.put("video", "Reportage photo / vidéo");
    }

    record Annex(boolean packaging, boolean officiant, boolean photos, boolean video, String packagingNotes, String other) {}
    record Schedule(String recurrence, Integer occurrences, String startDate, String paymentMode) {}
    record NewItem(Long catalogItemId, String label, int quantity, int unitPrice, int costEstimate, String target) {}
    record Created(Long id, String ref) {}

    private final GivingDb db;
    private final CatalogLookup catalog;
    private final Settings settings;
    private final Notifications notifications;
    private final Audit audit;
    private final Rewards rewards;
    private final Reputation reputation;
    private final ObjectMapper json;
    private final IdentityDirectory directory;

    Requests(GivingDb db, CatalogLookup catalog, Settings settings, Notifications notifications, Audit audit, Rewards rewards,
            Reputation reputation, ObjectMapper json, IdentityDirectory directory) {
        this.directory = directory;
        this.db = db;
        this.catalog = catalog;
        this.settings = settings;
        this.notifications = notifications;
        this.audit = audit;
        this.rewards = rewards;
        this.reputation = reputation;
        this.json = json;
    }

    private static void requireRequester(IdentityUser u) {
        if (!IdentityPermissions.isRequester(u.getRole())) throw Problems.forbidden("Action réservée aux donateurs et clients.");
    }

    /* ───────── Panier ───────── */
    String addToCart(IdentityUser u, long catalogItemId, int quantity, String target) {
        requireRequester(u);
        var item = catalog.findActive(catalogItemId).orElseThrow(() -> Problems.notFound("Article introuvable."));
        int qty = Math.min(Math.max(quantity, 1), 500);
        db.jdbc.sql("INSERT INTO giving_cart_items (user_id, catalog_item_id, quantity, target) VALUES (:u, :c, :q, :t)")
                .param("u", u.getId()).param("c", catalogItemId).param("q", qty).param("t", Problems.trimToNull(target, 150)).update();
        return item.title() + " ajouté au panier.";
    }

    void removeCartItem(IdentityUser u, long id) {
        db.jdbc.sql("DELETE FROM giving_cart_items WHERE id = :id AND user_id = :u").param("id", id).param("u", u.getId()).update();
    }

    record CheckoutRequest(String targetName, String targetZone, String description, Schedule schedule, Annex annex, String idempotencyKey) {}

    Created checkout(IdentityUser u, CheckoutRequest r) {
        requireRequester(u);
        Created existing = byIdempotencyKey(u, r.idempotencyKey());
        if (existing != null) return existing;
        var cart = db.jdbc.sql("SELECT id, user_id, catalog_item_id, quantity, target, created_at FROM giving_cart_items WHERE user_id = :u ORDER BY id")
                .param("u", u.getId()).query(GivingRows.CartRow.class).list();
        if (cart.isEmpty()) throw Problems.bad("Votre panier est vide.");
        String targetName = Problems.trim(r.targetName(), 150);
        if (targetName.isEmpty()) throw Problems.bad("Veuillez préciser la cible du don (personne, famille, école, orphelinat…).");
        var entries = catalog.findAll(cart.stream().map(GivingRows.CartRow::catalogItemId).toList());
        List<NewItem> items = new ArrayList<>();
        boolean allAction = true;
        String category = null;
        for (var c : cart) {
            var entry = entries.get(c.catalogItemId());
            if (entry == null || !entry.active()) throw Problems.bad("Un article de votre panier n'est plus disponible. Retirez-le puis réessayez.");
            // Prix et coûts relus côté serveur : le total n'est jamais fourni par le client.
            items.add(new NewItem(entry.id(), entry.title(), c.quantity(), entry.unitPrice(), entry.costEstimate(), c.target()));
            allAction &= "action".equals(entry.kind());
            if (category == null) category = entry.category();
        }
        Schedule sch = normalize(r.schedule());
        String title = items.size() == 1 ? items.getFirst().label() + " ×" + items.getFirst().quantity()
                : items.getFirst().label() + " et " + (items.size() - 1) + " autre(s) don(s)";
        String kind = !"none".equals(sch.recurrence()) ? "recurrent" : allAction ? "action" : "catalogue";
        Created created = create(u.getId(), kind, title, Problems.trimToNull(r.description(), 1500), category, targetName,
                Problems.trimToNull(r.targetZone(), 100), items, sch, r.annex(), r.idempotencyKey());
        db.jdbc.sql("DELETE FROM giving_cart_items WHERE user_id = :u").param("u", u.getId()).update();
        audit.record(u, "request.create", "request", created.id(), Map.of("via", "cart"));
        return created;
    }

    record CustomRequest(String kind, String title, String description, String targetName, String targetZone, int amount,
            Schedule schedule, Annex annex, String idempotencyKey) {}

    Created createCustom(IdentityUser u, CustomRequest r) {
        requireRequester(u);
        Created existing = byIdempotencyKey(u, r.idempotencyKey());
        if (existing != null) return existing;
        String title = Problems.trim(r.title(), 150), targetName = Problems.trim(r.targetName(), 150);
        if (title.length() < 3) throw Problems.bad("Indiquez un intitulé.");
        if (targetName.isEmpty()) throw Problems.bad("Indiquez la cible (personne, famille, structure…).");
        if (r.amount() < 500) throw Problems.bad("Le budget minimal est de 500 FCFA.");
        String kind = "action".equals(r.kind()) ? "action" : "personnalise";
        Schedule sch = normalize(r.schedule());
        Created created = create(u.getId(), !"none".equals(sch.recurrence()) ? "recurrent" : kind, title,
                Problems.trimToNull(r.description(), 2000), "action".equals(kind) ? "action" : "autre", targetName,
                Problems.trimToNull(r.targetZone(), 100), List.of(new NewItem(null, title, 1, r.amount(), 0, targetName)), sch, r.annex(),
                r.idempotencyKey());
        audit.record(u, "request.create", "request", created.id(), Map.of("via", "custom"));
        return created;
    }

    private Created byIdempotencyKey(IdentityUser u, String key) {
        if (key == null || key.isBlank()) return null;
        return db.jdbc.sql("SELECT id, ref FROM giving_requests WHERE idempotency_key = :k AND user_id = :u")
                .param("k", Problems.trim(key, 80)).param("u", u.getId()).query(Created.class).optional().orElse(null);
    }

    private static Schedule normalize(Schedule s) {
        String rec = s != null && Set.of("none", "weekly", "monthly", "quarterly").contains(s.recurrence()) ? s.recurrence() : "none";
        String pm = s != null && Set.of("immediate", "prefinance", "echeance").contains(s.paymentMode()) ? s.paymentMode() : "immediate";
        return new Schedule(rec, s == null || s.occurrences() == null ? 2 : s.occurrences(), s == null ? null : s.startDate(), pm);
    }

    private OffsetDateTime parseStart(String raw) {
        if (raw == null || raw.isBlank()) return OffsetDateTime.now();
        try { return LocalDate.parse(raw.trim()).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(); }
        catch (RuntimeException ex) { return OffsetDateTime.now(); }
    }

    private static OffsetDateTime addPeriod(OffsetDateTime d, String rec, int i) {
        return switch (rec) {
            case "weekly" -> d.plusWeeks(i);
            case "monthly" -> d.plusMonths(i);
            case "quarterly" -> d.plusMonths(3L * i);
            default -> d;
        };
    }

    /** Création d'une demande avec ses lignes, son échéancier et ses paiements prévus. */
    Created create(Long userId, String kind, String title, String description, String category, String targetName, String targetZone,
            List<NewItem> items, Schedule sch, Annex annexInput, String idempotencyKey) {
        String recurrence = sch.recurrence();
        int occurrences = "none".equals(recurrence) ? 1 : Math.min(Math.max(sch.occurrences() == null ? 2 : sch.occurrences(), 2), 60);
        Map<String, Object> annex = new LinkedHashMap<>();
        int annexCost = 0;
        Map<String, Integer> prices = settings.intMap("annex.prices");
        if (annexInput != null) {
            if (annexInput.packaging()) { annex.put("packaging", true); annexCost += prices.getOrDefault("packaging", 0); }
            if (annexInput.officiant()) { annex.put("officiant", true); annexCost += prices.getOrDefault("officiant", 0); }
            if (annexInput.photos()) { annex.put("photos", true); annexCost += prices.getOrDefault("photos", 0); }
            if (annexInput.video()) { annex.put("video", true); annexCost += prices.getOrDefault("video", 0); }
            String notes = Problems.trim(annexInput.packagingNotes(), 500);
            if (!notes.isEmpty()) annex.put("packaging_notes", notes);
            String other = Problems.trim(annexInput.other(), 500);
            if (!other.isEmpty()) annex.put("other", other);
        }
        int itemsTotal = items.stream().mapToInt(i -> i.quantity() * i.unitPrice()).sum();
        int perOcc = itemsTotal + annexCost;
        int total = perOcc * occurrences;
        OffsetDateTime start = parseStart(sch.startDate());
        String paymentMode = "none".equals(recurrence) ? "immediate" : "immediate".equals(sch.paymentMode()) ? "prefinance" : sch.paymentMode();
        int costEstimate = items.stream().mapToInt(i -> i.quantity() * i.costEstimate()).sum() * occurrences;
        String annexJson;
        try { annexJson = annex.isEmpty() ? null : json.writeValueAsString(annex); } catch (Exception ex) { annexJson = null; }

        Map<String, Object> p = new HashMap<>();
        p.put("ref", "tmp-" + java.util.UUID.randomUUID());
        p.put("u", userId); p.put("kind", kind); p.put("title", Problems.trim(title, 200)); p.put("d", description);
        p.put("cat", category == null ? "autre" : category); p.put("tn", targetName); p.put("tz", targetZone);
        p.put("apo", perOcc); p.put("occ", occurrences); p.put("tot", total); p.put("pm", paymentMode); p.put("rec", recurrence);
        p.put("sd", start); p.put("annex", annexJson); p.put("ac", annexCost * occurrences); p.put("eb", costEstimate);
        p.put("ik", idempotencyKey == null || idempotencyKey.isBlank() ? null : Problems.trim(idempotencyKey, 80));
        long id = db.insert("""
                INSERT INTO giving_requests (ref, user_id, kind, title, description, category, target_name, target_zone,
                    amount_per_occurrence, occurrences, total_amount, payment_mode, recurrence, start_date, annex_services, annex_cost,
                    execution_budget, idempotency_key)
                VALUES (:ref, :u, :kind, :title, :d, :cat, :tn, :tz, :apo, :occ, :tot, :pm, :rec, :sd, :annex, :ac, :eb, :ik)""", p);
        String ref = "MN-" + Year.now().getValue() + "-" + String.format("%05d", id);
        db.jdbc.sql("UPDATE giving_requests SET ref = :ref WHERE id = :id").param("ref", ref).param("id", id).update();

        for (NewItem i : items) {
            db.jdbc.sql("""
                    INSERT INTO giving_request_items (request_id, catalog_item_id, label, quantity, unit_price, cost_estimate, target)
                    VALUES (:r, :c, :l, :q, :p, :ce, :t)""")
                    .param("r", id).param("c", i.catalogItemId()).param("l", Problems.trim(i.label(), 200)).param("q", i.quantity())
                    .param("p", i.unitPrice()).param("ce", i.costEstimate()).param("t", i.target()).update();
        }
        List<long[]> installments = new ArrayList<>();
        List<OffsetDateTime> dues = new ArrayList<>();
        for (int i = 0; i < occurrences; i++) {
            OffsetDateTime due = addPeriod(start, recurrence, i);
            long instId = db.insert("INSERT INTO giving_installments (request_id, seq, due_date, amount) VALUES (:r, :s, :d, :a)",
                    Map.of("r", id, "s", i + 1, "d", due, "a", perOcc));
            installments.add(new long[]{instId, i + 1});
            dues.add(due);
        }
        if ("echeance".equals(paymentMode)) {
            for (int i = 0; i < installments.size(); i++) {
                db.jdbc.sql("""
                        INSERT INTO giving_payments (request_id, installment_id, user_id, direction, kind, amount_planned, due_date, note)
                        VALUES (:r, :i, :u, 'in', 'don', :a, :d, :n)""")
                        .param("r", id).param("i", installments.get(i)[0]).param("u", userId).param("a", perOcc).param("d", dues.get(i))
                        .param("n", "Échéance " + installments.get(i)[1] + "/" + occurrences).update();
            }
        } else {
            db.jdbc.sql("""
                    INSERT INTO giving_payments (request_id, user_id, direction, kind, amount_planned, due_date, note)
                    VALUES (:r, :u, 'in', 'don', :a, :d, :n)""")
                    .param("r", id).param("u", userId).param("a", total).param("d", OffsetDateTime.now())
                    .param("n", "prefinance".equals(paymentMode) ? "Préfinancement de " + occurrences + " échéance(s)" : "Paiement immédiat").update();
        }
        db.history(id, "submitted", "Demande créée par le donateur", userId);
        notifications.notifyAdmins("requests.manage", "request_new", "Nouvelle demande " + ref, title, "/admin/demandes/" + id);
        notifications.notify(userId, "request_new", "Demande " + ref + " enregistrée", "Elle sera examinée par notre équipe.", "/espace/demandes/" + id);
        return new Created(id, ref);
    }

    /* ───────── Donateur : suivi ───────── */
    private GivingRows.RequestRow ownRequest(IdentityUser u, long id) {
        var r = db.request(id);
        if (!r.userId().equals(u.getId())) throw Problems.notFound("Demande introuvable.");
        return r;
    }

    String cancel(IdentityUser u, long id) {
        var r = ownRequest(u, id);
        if (!Set.of("submitted", "info_requested").contains(r.status())) throw Problems.bad("Cette demande ne peut plus être annulée (déjà traitée).");
        db.setRequestStatus(id, "cancelled");
        db.jdbc.sql("UPDATE giving_payments SET status = 'cancelled' WHERE request_id = :r AND status IN ('planned', 'declared')").param("r", id).update();
        db.history(id, "cancelled", "Annulée par le donateur", u.getId());
        audit.record(u, "request.cancel", "request", id);
        return "Demande annulée.";
    }

    String answerInfo(IdentityUser u, long id, String answerRaw) {
        var r = ownRequest(u, id);
        if (!"info_requested".equals(r.status())) throw Problems.bad("Aucune information n'est attendue.");
        String answer = Problems.trim(answerRaw, 1500);
        if (answer.length() < 3) throw Problems.bad("Saisissez votre réponse.");
        db.setRequestStatus(id, "submitted");
        db.history(id, "submitted", "Réponse du donateur : " + answer, u.getId());
        notifications.notifyAdmins("requests.manage", "request_new", "Réponse reçue pour " + r.ref(), Problems.trim(answer, 100), "/admin/demandes/" + id);
        return "Réponse envoyée.";
    }

    String rateMission(IdentityUser u, long requestId, int rating, String comment) {
        var r = ownRequest(u, requestId);
        if (!Set.of("completed", "closed").contains(r.status())) throw Problems.bad("Vous pourrez noter une fois le don réalisé.");
        var m = db.jdbc.sql("SELECT " + GivingRows.MISSION_COLS + " FROM giving_missions WHERE request_id = :r AND validated_at IS NOT NULL ORDER BY id")
                .param("r", requestId).query(GivingRows.MissionRow.class).list().stream().findFirst().orElse(null);
        if (m == null || m.providerId() == null) throw Problems.bad("Aucune mission validée à noter.");
        if (rating < 1 || rating > 5) throw Problems.bad("Note de 1 à 5 requise.");
        String text = Problems.trimToNull(comment, 800);
        if (!reputation.add("mission", m.id(), u.getId(), m.providerId(), rating, text)) throw Problems.bad("Vous avez déjà noté cette mission.");
        rewards.award(u.getId(), "review_given", "mission", m.id(), null);
        notifications.notify(m.providerId(), "review", "Nouvel avis : " + rating + "/5", text == null ? null : Problems.trim(text, 100), "/espace/missions");
        return "Merci pour votre avis !";
    }

    /* ───────── Administration ───────── */
    String decide(IdentityUser u, long id, String decision, String messageRaw, Integer newAmount) {
        var r = db.request(id);
        if (!"submitted".equals(r.status())) throw Problems.bad("Seules les demandes « Soumises » peuvent faire l'objet d'une décision.");
        String msg = Problems.trim(messageRaw, 1000);
        switch (decision == null ? "" : decision) {
            case "reject" -> {
                if (msg.length() < 5) throw Problems.bad("Le motif de refus est obligatoire.");
                db.jdbc.sql("UPDATE giving_requests SET status = 'rejected', rejection_reason = :m, updated_at = :now WHERE id = :id")
                        .param("m", msg).param("now", GivingDb.now()).param("id", id).update();
                db.jdbc.sql("UPDATE giving_payments SET status = 'cancelled' WHERE request_id = :r AND status IN ('planned', 'declared')").param("r", id).update();
                db.history(id, "rejected", "Refusée : " + msg, u.getId());
                notifications.notify(r.userId(), "request_decision", "Votre demande " + r.ref() + " a été refusée", msg, "/espace/demandes/" + id);
            }
            case "info" -> {
                if (msg.length() < 5) throw Problems.bad("Précisez l'information demandée.");
                db.jdbc.sql("UPDATE giving_requests SET status = 'info_requested', info_request = :m, updated_at = :now WHERE id = :id")
                        .param("m", msg).param("now", GivingDb.now()).param("id", id).update();
                db.history(id, "info_requested", "Information demandée : " + msg, u.getId());
                notifications.notify(r.userId(), "info_requested", "Informations demandées pour " + r.ref(), msg, "/espace/demandes/" + id);
            }
            case "accept" -> {
                if (newAmount != null && newAmount > 0 && newAmount != r.amountPerOccurrence()) {
                    int total = newAmount * r.occurrences();
                    db.jdbc.sql("UPDATE giving_requests SET amount_per_occurrence = :a, total_amount = :t WHERE id = :id")
                            .param("a", newAmount).param("t", total).param("id", id).update();
                    db.jdbc.sql("UPDATE giving_installments SET amount = :a WHERE request_id = :id").param("a", newAmount).param("id", id).update();
                    db.jdbc.sql("UPDATE giving_payments SET amount_planned = :a WHERE request_id = :id AND direction = 'in' AND status = 'planned'")
                            .param("a", "echeance".equals(r.paymentMode()) ? newAmount : total).param("id", id).update();
                }
                db.jdbc.sql("UPDATE giving_requests SET status = 'accepted', rejection_reason = NULL, updated_at = :now WHERE id = :id")
                        .param("now", GivingDb.now()).param("id", id).update();
                db.history(id, "accepted", msg.isEmpty() ? "Acceptée par l'administration" : "Acceptée : " + msg, u.getId());
                notifications.notify(r.userId(), "request_decision", "Votre demande " + r.ref() + " a été acceptée", msg.isEmpty() ? null : msg, "/espace/demandes/" + id);
            }
            default -> throw Problems.bad("Décision invalide.");
        }
        audit.record(u, "request." + decision, "request", id, Map.of("message", msg));
        return "Décision enregistrée et notifiée au donateur.";
    }

    String linkBeneficiary(IdentityUser u, long id, Long beneficiaryId) {
        db.request(id);
        db.jdbc.sql("UPDATE giving_requests SET beneficiary_id = :b, updated_at = :now WHERE id = :id")
                .param("b", beneficiaryId == null || beneficiaryId == 0 ? null : beneficiaryId).param("now", GivingDb.now()).param("id", id).update();
        audit.record(u, "request.link_beneficiary", "request", id, Map.of("beneficiaryId", beneficiaryId == null ? 0 : beneficiaryId));
        return "Bénéficiaire associé.";
    }

    String updateFinance(IdentityUser u, long id, int executionBudget, int otherFees) {
        db.request(id);
        int exec = Math.max(executionBudget, 0), other = Math.max(otherFees, 0);
        db.jdbc.sql("UPDATE giving_requests SET execution_budget = :e, other_fees = :o, updated_at = :now WHERE id = :id")
                .param("e", exec).param("o", other).param("now", GivingDb.now()).param("id", id).update();
        audit.record(u, "request.finance_update", "request", id, Map.of("executionBudget", exec, "otherFees", other));
        return "Budget d'exécution et frais mis à jour.";
    }

    String installmentDone(IdentityUser u, long installmentId) {
        var inst = db.jdbc.sql("SELECT id, request_id, seq, due_date, amount, status, notified_at FROM giving_installments WHERE id = :id")
                .param("id", installmentId).query(GivingRows.InstallmentRow.class).optional().orElseThrow(() -> Problems.notFound("Échéance introuvable."));
        db.jdbc.sql("UPDATE giving_installments SET status = 'done' WHERE id = :id").param("id", installmentId).update();
        db.history(inst.requestId(), "in_progress", "Échéance " + inst.seq() + " réalisée", u.getId());
        audit.record(u, "installment.done", "installment", installmentId);
        return "Échéance réalisée.";
    }

    /** Conditions pour déclarer un don réalisé : preuves validées, missions terminées, échéances faites. */
    record Check(boolean ok, String label, String help) {}

    List<Check> completionChecks(long id) {
        int min = settings.intValue("proofs.min_validated", 1);
        List<String> types = settings.stringList("proofs.accepted_types");
        if (types.isEmpty()) types = List.of("photo", "attestation", "receipt");
        long okProofs = db.jdbc.sql("SELECT COUNT(*) FROM giving_proofs WHERE request_id = :r AND status = 'validated' AND type IN (:t)")
                .param("r", id).param("t", types).query(Long.class).single();
        var missions = db.jdbc.sql("SELECT " + GivingRows.MISSION_COLS + " FROM giving_missions WHERE request_id = :r AND status IN ('proposed', 'accepted', 'in_progress')")
                .param("r", id).query(GivingRows.MissionRow.class).list();
        long totalInst = db.jdbc.sql("SELECT COUNT(*) FROM giving_installments WHERE request_id = :r").param("r", id).query(Long.class).single();
        long pendingInst = db.jdbc.sql("SELECT COUNT(*) FROM giving_installments WHERE request_id = :r AND status = 'planned'").param("r", id).query(Long.class).single();
        Map<String, String> waiting = Map.of(
                "proposed", "attend que le prestataire accepte la mission depuis son espace",
                "accepted", "acceptée : le prestataire doit la démarrer, déposer une preuve puis la terminer",
                "in_progress", "en cours : le prestataire doit déposer une preuve puis la terminer");
        var names = directory.findAll(missions.stream().map(GivingRows.MissionRow::providerId).toList());
        String missionHelp = missions.stream().map(m -> "« " + m.title() + " » ("
                + (m.providerId() != null && names.containsKey(m.providerId()) ? names.get(m.providerId()).displayName() : "sans prestataire") + ") "
                + waiting.get(m.status()) + ". Sinon, réattribuez-la ou annulez-la.").collect(java.util.stream.Collectors.joining(" "));
        String typeLabels = types.stream().map(t -> Reference.label("proofType", t)).collect(java.util.stream.Collectors.joining(", "));
        List<Check> out = new ArrayList<>();
        out.add(new Check(okProofs >= min, "Preuve de remise validée (" + okProofs + "/" + min + ")",
                okProofs >= min ? null : "Types acceptés : " + typeLabels + ". Faites contrôler les preuves déposées ou déposez-en une ci-contre."));
        out.add(new Check(missions.isEmpty(), missions.isEmpty() ? "Missions terminées" : missions.size() + " mission(s) non terminée(s)",
                missions.isEmpty() ? null : missionHelp));
        out.add(new Check(pendingInst == 0, "Échéances réalisées (" + (totalInst - pendingInst) + "/" + totalInst + ")",
                pendingInst == 0 ? null : "Marquez chaque échéance comme « Réalisée » dans l’échéancier ci-dessous."));
        return out;
    }

    String markCompleted(IdentityUser u, long id) {
        var r = db.request(id);
        if (!Set.of("accepted", "in_progress").contains(r.status())) throw Problems.bad("La demande doit être acceptée ou en cours.");
        var checks = completionChecks(id);
        if (!checks.get(0).ok()) throw Problems.bad("Un don ne peut être déclaré réalisé sans preuve de remise validée (" + checks.get(0).label() + ").");
        if (!checks.get(1).ok()) throw Problems.bad("Des missions ne sont pas encore terminées.");
        if (!checks.get(2).ok()) throw Problems.bad("Des échéances restent à réaliser.");
        db.jdbc.sql("UPDATE giving_requests SET status = 'completed', completed_at = :now, updated_at = :now WHERE id = :id")
                .param("now", GivingDb.now()).param("id", id).update();
        db.history(id, "completed", "Don déclaré réalisé après contrôle des preuves", u.getId());
        audit.record(u, "request.completed", "request", id);
        notifications.notify(r.userId(), "request_decision", "Votre don " + r.ref() + " est réalisé", "Consultez les preuves de remise.", "/espace/demandes/" + id);
        return "Don déclaré réalisé.";
    }

    String close(IdentityUser u, long id) {
        var r = db.request(id);
        if (!"completed".equals(r.status())) throw Problems.bad("Seule une demande réalisée peut être clôturée.");
        db.jdbc.sql("UPDATE giving_requests SET status = 'closed', closed_at = :now, updated_at = :now WHERE id = :id")
                .param("now", GivingDb.now()).param("id", id).update();
        db.history(id, "closed", "Don clôturé", u.getId());
        audit.record(u, "request.closed", "request", id);
        notifications.notify(r.userId(), "request_closed", "Clôture du don " + r.ref(), "Merci pour votre générosité ! Vous pouvez noter le prestataire.", "/espace/demandes/" + id);
        rewards.award(r.userId(), "request_closed", "request", id, u.getId());
        return "Don clôturé. Tokens attribués au donateur selon les règles.";
    }

    /** Rappel avant échéance (idempotent : un seul rappel par échéance). Lancé chaque jour par {@link GivingReminders}. */
    int runDueScan() {
        int days = settings.intValue("requests.late_alert_days", 3);
        record Due(Long id, Long requestId, int seq, OffsetDateTime dueDate) {}
        var due = db.jdbc.sql("SELECT id, request_id, seq, due_date FROM giving_installments WHERE status = 'planned' AND notified_at IS NULL AND due_date <= :lim AND due_date >= :now")
                .param("lim", OffsetDateTime.now().plusDays(days)).param("now", OffsetDateTime.now()).query(Due.class).list();
        int sent = 0;
        for (Due d : due) {
            var r = db.request(d.requestId());
            db.jdbc.sql("UPDATE giving_installments SET notified_at = :now WHERE id = :id").param("now", GivingDb.now()).param("id", d.id()).update();
            if (!OPEN_FOR_INSTALLMENTS.contains(r.status())) continue;
            String body = "Échéance " + d.seq() + " prévue le " + d.dueDate().format(DAY);
            notifications.notify(r.userId(), "due_soon", "Échéance proche — " + r.ref(), body, "/espace/demandes/" + r.id());
            notifications.notifyAdmins("requests.manage", "due_soon", "Échéance proche — " + r.ref(), body, "/admin/demandes/" + r.id());
            sent++;
        }
        return sent;
    }

    /** Relance unique quand une échéance planifiée est dépassée sans avoir été réalisée. */
    int runOverdueScan() {
        record Due(Long id, Long requestId, int seq, OffsetDateTime dueDate) {}
        var late = db.jdbc.sql("SELECT id, request_id, seq, due_date FROM giving_installments WHERE status = 'planned' AND overdue_notified_at IS NULL AND due_date < :now")
                .param("now", OffsetDateTime.now()).query(Due.class).list();
        int sent = 0;
        for (Due d : late) {
            var r = db.request(d.requestId());
            db.jdbc.sql("UPDATE giving_installments SET overdue_notified_at = :now WHERE id = :id").param("now", GivingDb.now()).param("id", d.id()).update();
            if (!OPEN_FOR_INSTALLMENTS.contains(r.status())) continue;
            String body = "L’échéance " + d.seq() + " du " + d.dueDate().format(DAY) + " n’est pas encore réalisée.";
            notifications.notify(r.userId(), "due_late", "Échéance en retard — " + r.ref(), body, "/espace/demandes/" + r.id());
            notifications.notifyAdmins("requests.manage", "due_late", "Échéance en retard — " + r.ref(), body, "/admin/demandes/" + r.id());
            sent++;
        }
        return sent;
    }

    private static final Set<String> OPEN_FOR_INSTALLMENTS = Set.of("accepted", "in_progress", "submitted");
    private static final java.time.format.DateTimeFormatter DAY = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
}
