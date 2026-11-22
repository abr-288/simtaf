package com.menewa.giving;

import com.menewa.identity.IdentityDirectory;
import com.menewa.identity.IdentityUser;
import com.menewa.platform.Audit;
import com.menewa.platform.Notifications;
import com.menewa.platform.Problems;
import com.menewa.rewards.Rewards;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Missions : création par l'administration, réponse/exécution par le prestataire, validation. */
@Service
@Transactional
class Missions {
    private final GivingDb db;
    private final IdentityDirectory directory;
    private final Notifications notifications;
    private final Audit audit;
    private final Rewards rewards;

    Missions(GivingDb db, IdentityDirectory directory, Notifications notifications, Audit audit, Rewards rewards) {
        this.db = db;
        this.directory = directory;
        this.notifications = notifications;
        this.audit = audit;
        this.rewards = rewards;
    }

    record CreateMission(String title, String instructions, Long providerId, String exceptionReason, String dueDate,
            String compensationMode, int providerFee, String exchangeDescription) {}

    private void requireProvider(Long providerId) {
        var p = directory.find(providerId).orElse(null);
        if (p == null || !"provider".equals(p.role()) || !p.active()) throw Problems.bad("Prestataire invalide.");
    }

    String create(IdentityUser u, long requestId, CreateMission c) {
        var r = db.request(requestId);
        if (!Set.of("accepted", "in_progress").contains(r.status())) throw Problems.bad("Une mission doit être rattachée à une demande acceptée.");
        Long providerId = c.providerId() == null || c.providerId() == 0 ? null : c.providerId();
        String exception = Problems.trim(c.exceptionReason(), 500);
        if (providerId == null && exception.length() < 10)
            throw Problems.bad("Désignez un prestataire, ou documentez l'exception administrative (10 caractères min.).");
        if (providerId != null) requireProvider(providerId);
        String title = Problems.trim(c.title(), 200);
        if (title.length() < 4) throw Problems.bad("Intitulé de mission requis.");
        String mode = "exchange".equals(c.compensationMode()) ? "exchange" : "money";
        int fee = Math.max(c.providerFee(), 0);
        String ex = Problems.trim(c.exchangeDescription(), 600);
        if ("money".equals(mode) && fee < 1) throw Problems.bad("Coût de la prestation requis (montant en FCFA).");
        if ("exchange".equals(mode) && ex.length() < 5) throw Problems.bad("Décrivez les engagements réciproques de l'échange de services.");
        if (c.dueDate() == null || c.dueDate().isBlank()) throw Problems.bad("Date d'échéance requise.");
        LocalDate dueDay;
        try { dueDay = LocalDate.parse(c.dueDate().trim()); }
        catch (RuntimeException e) { throw Problems.bad("Date d'échéance invalide."); }
        if (dueDay.isBefore(LocalDate.now(ZoneId.systemDefault()))) throw Problems.bad("La date d'échéance ne peut pas être dans le passé.");
        OffsetDateTime due = dueDay.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
        Map<String, Object> p = new HashMap<>();
        p.put("r", requestId); p.put("p", providerId); p.put("t", title); p.put("i", Problems.trimToNull(c.instructions(), 1500));
        p.put("d", due); p.put("fee", "money".equals(mode) ? fee : 0); p.put("mode", mode); p.put("ex", "exchange".equals(mode) ? ex : null);
        p.put("exc", providerId == null ? exception : null); p.put("s", providerId != null ? "proposed" : "accepted"); p.put("by", u.getId());
        long id = db.insert("""
                INSERT INTO giving_missions (request_id, provider_id, title, instructions, due_date, provider_fee, compensation_mode,
                    exchange_description, exception_reason, status, created_by)
                VALUES (:r, :p, :t, :i, :d, :fee, :mode, :ex, :exc, :s, :by)""", p);
        if (providerId != null) {
            Map<String, Object> pay = new HashMap<>();
            pay.put("r", requestId); pay.put("m", id); pay.put("u", providerId); pay.put("mode", mode);
            pay.put("method", "exchange".equals(mode) ? "exchange" : null); pay.put("a", "money".equals(mode) ? fee : 0);
            pay.put("ex", "exchange".equals(mode) ? ex : null); pay.put("es", "exchange".equals(mode) ? "pending" : null);
            pay.put("n", "Rémunération mission #" + id);
            db.insert("""
                    INSERT INTO giving_payments (request_id, mission_id, user_id, direction, kind, mode, method, amount_planned,
                        exchange_description, exchange_status, note)
                    VALUES (:r, :m, :u, 'out', 'prestation', :mode, :method, :a, :ex, :es, :n)""", pay);
            notifications.notify(providerId, "mission_proposed", "Nouvelle mission proposée : " + title, null, "/espace/missions/" + id);
        }
        db.history(requestId, r.status(), "Mission créée : " + title + (providerId == null ? " (exception administrative)" : ""), u.getId());
        Map<String, Object> details = new HashMap<>();
        details.put("requestId", requestId);
        details.put("providerId", providerId);
        if (!exception.isEmpty()) details.put("exception", exception);
        audit.record(u, "mission.create", "mission", id, details);
        return "Mission créée.";
    }

    String cancel(IdentityUser u, long id, String reasonRaw) {
        String reason = Problems.trim(reasonRaw, 500);
        if (reason.length() < 5) throw Problems.bad("Indiquez le motif de l’annulation (5 caractères min.).");
        var m = db.mission(id);
        if (!m.open()) throw Problems.bad("Seule une mission proposée, acceptée ou en cours peut être annulée.");
        db.jdbc.sql("UPDATE giving_missions SET status = 'cancelled', refusal_reason = :r, updated_at = :now WHERE id = :id")
                .param("r", "Annulée par l’administration : " + reason).param("now", GivingDb.now()).param("id", id).update();
        db.jdbc.sql("UPDATE giving_payments SET status = 'cancelled' WHERE mission_id = :m AND direction = 'out' AND status = 'planned'").param("m", id).update();
        db.history(m.requestId(), db.request(m.requestId()).status(), "Mission « " + m.title() + " » annulée : " + reason, u.getId());
        audit.record(u, "mission.cancel", "mission", id, Map.of("reason", reason));
        if (m.providerId() != null) notifications.notify(m.providerId(), "mission", "Mission annulée : " + m.title(), reason, "/espace/missions/" + id);
        return "Mission annulée.";
    }

    String reassign(IdentityUser u, long id, Long providerId) {
        var m = db.mission(id);
        if (!Set.of("refused", "proposed", "accepted").contains(m.status())) throw Problems.bad("Cette mission ne peut pas être réattribuée.");
        if (providerId == null) throw Problems.bad("Prestataire invalide.");
        requireProvider(providerId);
        db.jdbc.sql("""
                UPDATE giving_missions SET provider_id = :p, status = 'proposed', reassigned_from = :from, refusal_reason = NULL,
                    exception_reason = NULL, updated_at = :now WHERE id = :id""")
                .param("p", providerId).param("from", m.providerId()).param("now", GivingDb.now()).param("id", id).update();
        db.jdbc.sql("UPDATE giving_payments SET user_id = :p WHERE mission_id = :m AND direction = 'out'").param("p", providerId).param("m", id).update();
        db.history(m.requestId(), "accepted", "Mission « " + m.title() + " » réattribuée à " + directory.nameOf(providerId), u.getId());
        Map<String, Object> details = new HashMap<>();
        details.put("from", m.providerId());
        details.put("to", providerId);
        details.put("reason", m.refusalReason());
        audit.record(u, "mission.reassign", "mission", id, details);
        notifications.notify(providerId, "mission_proposed", "Nouvelle mission proposée : " + m.title(), null, "/espace/missions/" + id);
        return "Mission réattribuée.";
    }

    String validate(IdentityUser u, long id) {
        var m = db.mission(id);
        if (!"completed".equals(m.status())) throw Problems.bad("Mission non terminée.");
        if (m.validatedAt() != null) throw Problems.bad("Déjà validée.");
        long validated = db.jdbc.sql("SELECT COUNT(*) FROM giving_proofs WHERE mission_id = :m AND status = 'validated'").param("m", id).query(Long.class).single();
        if (validated < 1) throw Problems.bad("Au moins une preuve validée est nécessaire pour valider la mission.");
        db.jdbc.sql("UPDATE giving_missions SET validated_at = :now, updated_at = :now WHERE id = :id").param("now", GivingDb.now()).param("id", id).update();
        db.jdbc.sql("UPDATE giving_payments SET status = 'validated', amount_validated = :a WHERE mission_id = :m AND direction = 'out' AND status = 'planned'")
                .param("a", m.providerFee()).param("m", id).update();
        db.history(m.requestId(), "in_progress", "Mission « " + m.title() + " » validée", u.getId());
        audit.record(u, "mission.validate", "mission", id);
        if (m.providerId() != null) {
            notifications.notify(m.providerId(), "mission", "Mission validée : " + m.title(), "Votre rémunération est validée.", "/espace/missions/" + id);
            rewards.award(m.providerId(), "mission_validated", "mission", id, u.getId());
        }
        return "Mission validée, rémunération validée et tokens attribués.";
    }

    /* ───────── Prestataire ───────── */
    private GivingRows.MissionRow own(IdentityUser u, long id) {
        var m = db.mission(id);
        if (m.providerId() == null || !m.providerId().equals(u.getId())) throw Problems.notFound("Mission introuvable.");
        return m;
    }

    String respond(IdentityUser u, long id, String decision, String reasonRaw) {
        var m = own(u, id);
        if (!"proposed".equals(m.status())) throw Problems.bad("Cette mission a déjà reçu une réponse.");
        var r = db.request(m.requestId());
        if ("accept".equals(decision)) {
            db.jdbc.sql("UPDATE giving_missions SET status = 'accepted', updated_at = :now WHERE id = :id").param("now", GivingDb.now()).param("id", id).update();
            if ("accepted".equals(r.status())) db.setRequestStatus(r.id(), "in_progress");
            db.history(m.requestId(), "in_progress", "Mission « " + m.title() + " » acceptée par le prestataire", u.getId());
            notifications.notifyAdmins("missions.manage", "mission", "Mission acceptée : " + m.title(), u.getName(), "/admin/missions");
            audit.record(u, "mission.accept", "mission", id);
            return "Mission acceptée. Bonne réalisation !";
        }
        if ("refuse".equals(decision)) {
            String reason = Problems.trim(reasonRaw, 600);
            if (reason.length() < 5) throw Problems.bad("Un motif de refus est obligatoire.");
            db.jdbc.sql("UPDATE giving_missions SET status = 'refused', refusal_reason = :r, updated_at = :now WHERE id = :id")
                    .param("r", reason).param("now", GivingDb.now()).param("id", id).update();
            db.history(m.requestId(), r.status(), "Mission « " + m.title() + " » refusée : " + reason, u.getId());
            notifications.notifyAdmins("missions.manage", "mission", "Mission refusée : " + m.title(), reason, "/admin/missions");
            audit.record(u, "mission.refuse", "mission", id, Map.of("reason", reason));
            return "Refus enregistré. L'administration pourra réattribuer la mission.";
        }
        throw Problems.bad("Décision invalide.");
    }

    String start(IdentityUser u, long id) {
        var m = own(u, id);
        if (!"accepted".equals(m.status())) throw Problems.bad("Mission non démarrable.");
        db.jdbc.sql("UPDATE giving_missions SET status = 'in_progress', exchange_status = :es, updated_at = :now WHERE id = :id")
                .param("es", "exchange".equals(m.compensationMode()) ? "in_progress" : m.exchangeStatus()).param("now", GivingDb.now()).param("id", id).update();
        db.history(m.requestId(), "in_progress", "Mission « " + m.title() + " » démarrée", u.getId());
        return "Mission démarrée.";
    }

    String complete(IdentityUser u, long id) {
        var m = own(u, id);
        if ("accepted".equals(m.status())) throw Problems.bad("Démarrez la mission avant de la terminer.");
        if (!"in_progress".equals(m.status())) throw Problems.bad("Mission introuvable ou déjà terminée.");
        long proofs = db.jdbc.sql("SELECT COUNT(*) FROM giving_proofs WHERE mission_id = :m").param("m", id).query(Long.class).single();
        if (proofs == 0) throw Problems.bad("Déposez au moins une preuve (photo, reçu, facture ou attestation) avant de terminer.");
        db.jdbc.sql("UPDATE giving_missions SET status = 'completed', completed_at = :now, updated_at = :now, exchange_status = :es WHERE id = :id")
                .param("now", GivingDb.now()).param("es", "exchange".equals(m.compensationMode()) ? "done" : m.exchangeStatus()).param("id", id).update();
        db.history(m.requestId(), "in_progress", "Mission « " + m.title() + " » terminée — en attente de contrôle des preuves", u.getId());
        notifications.notifyAdmins("proofs.review", "proof_review", "Mission terminée : " + m.title(), "Preuves à contrôler", "/admin/preuves");
        audit.record(u, "mission.complete", "mission", id);
        return "Mission terminée. En attente du contrôle administratif.";
    }
}
