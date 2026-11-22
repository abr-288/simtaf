package com.menewa.giving;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.menewa.identity.IdentityDirectory;
import com.menewa.identity.IdentityPermissions;
import com.menewa.identity.IdentityUser;
import com.menewa.platform.Audit;
import com.menewa.platform.Notifications;
import com.menewa.platform.Problems;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Paiement en ligne Mobile Money : encaissement auprès du client, reversement au prestataire.
 *
 * <p>Règles : le montant vient toujours du serveur (reste dû) ; une seule transaction en attente par paiement ;
 * le paiement n'est marqué payé qu'après confirmation du fournisseur (webhook signé, puis statut relu chez lui) ;
 * une confirmation reçue deux fois n'est appliquée qu'une fois.
 */
@Service
@Transactional
class OnlinePayments {
    static final Set<String> OPERATORS = Set.of("tmoney", "flooz");
    private static final String TX_COLS = """
            id, payment_id, direction, provider, provider_ref, amount, phone, operator, status, failure_reason, created_at, settled_at""";

    private final GivingDb db;
    private final PaymentGateway gateway;
    private final IdentityDirectory directory;
    private final Notifications notifications;
    private final Audit audit;
    private final ObjectMapper json;

    OnlinePayments(GivingDb db, PaymentGateway gateway, IdentityDirectory directory, Notifications notifications, Audit audit, ObjectMapper json) {
        this.db = db;
        this.gateway = gateway;
        this.directory = directory;
        this.notifications = notifications;
        this.audit = audit;
        this.json = json;
    }

    record PaymentTransaction(Long id, Long paymentId, String direction, String provider, String providerRef, int amount, String phone,
            String operator, String status, String failureReason, OffsetDateTime createdAt, OffsetDateTime settledAt) {}

    record PaymentTransactionView(PaymentTransaction transaction, boolean simulated, String message) {}

    record OnlinePaymentConfig(String provider, boolean simulated, List<String> operators) {}

    OnlinePaymentConfig config() {
        return new OnlinePaymentConfig(gateway.name(), gateway.simulated(), List.of("tmoney", "flooz"));
    }

    private PaymentTransaction tx(long id) {
        return db.jdbc.sql("SELECT " + TX_COLS + " FROM giving_payment_transactions WHERE id = :id").param("id", id)
                .query(PaymentTransaction.class).optional().orElseThrow(() -> Problems.notFound("PaymentTransaction introuvable."));
    }

    private static String phone(String raw) {
        String p = raw == null ? "" : raw.replaceAll("[\\s().-]", "");
        if (p.startsWith("00")) p = "+" + p.substring(2);
        if (p.matches("\\d{8}")) p = "+228" + p;
        if (!p.matches("\\+228\\d{8}")) throw Problems.bad("Numéro Mobile Money invalide (ex. +228 90 00 00 00).");
        return p;
    }

    private static String operator(String raw) {
        if (raw == null || !OPERATORS.contains(raw)) throw Problems.bad("Choisissez l'opérateur Mobile Money.");
        return raw;
    }

    private PaymentTransaction open(IdentityUser u, GivingRows.PaymentRow p, String direction, String phone, String operator) {
        if (!"money".equals(p.mode())) throw Problems.bad("Un échange de services ne se paie pas en ligne.");
        int amount = p.remaining();
        if (amount < 1) throw Problems.bad("Rien à payer : ce paiement est déjà soldé.");
        Long pending = db.jdbc.sql("SELECT id FROM giving_payment_transactions WHERE payment_id = :p AND status = 'pending'")
                .param("p", p.id()).query(Long.class).optional().orElse(null);
        if (pending != null) throw Problems.conflict("Une transaction est déjà en attente de confirmation pour ce paiement.");
        Map<String, Object> params = new HashMap<>();
        params.put("p", p.id()); params.put("d", direction); params.put("prov", gateway.name()); params.put("a", amount);
        params.put("ph", phone); params.put("op", operator); params.put("u", u.getId());
        long id = db.insert("""
                INSERT INTO giving_payment_transactions (payment_id, direction, provider, amount, phone, operator, initiated_by)
                VALUES (:p, :d, :prov, :a, :ph, :op, :u)""", params);
        return tx(id);
    }

    /** Une panne du fournisseur annule la transaction (rien n'est enregistré) et l'utilisateur peut réessayer. */
    private static PaymentGateway.Initiation call(java.util.function.Supplier<PaymentGateway.Initiation> request) {
        try {
            return request.get();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Le service de paiement est indisponible. Réessayez dans quelques minutes.", e);
        }
    }

    private PaymentTransactionView started(PaymentTransaction t, PaymentGateway.Initiation init) {
        if (init.providerRef() == null || init.providerRef().isBlank()) throw new IllegalStateException("Référence fournisseur manquante");
        db.jdbc.sql("UPDATE giving_payment_transactions SET provider_ref = :r WHERE id = :id").param("r", init.providerRef()).param("id", t.id()).update();
        return new PaymentTransactionView(tx(t.id()), gateway.simulated(), init.message());
    }

    /** Le client paie un de ses paiements (don, échéance, service) depuis son téléphone. */
    PaymentTransactionView collect(IdentityUser u, long paymentId, String rawPhone, String rawOperator) {
        var p = db.payment(paymentId);
        if (!p.userId().equals(u.getId()) || !"in".equals(p.direction())) throw Problems.notFound("Paiement introuvable.");
        if (!Set.of("planned", "declared", "validated", "partial").contains(p.status())) throw Problems.bad("Ce paiement ne peut plus être réglé.");
        var t = open(u, p, "in", phone(rawPhone), operator(rawOperator));
        var init = call(() -> gateway.collect("MN-TX-" + t.id(), t.amount(), t.phone(), t.operator(), p.note()));
        audit.record(u, "payment.online_start", "payment", paymentId, Map.of("transaction", t.id(), "amount", t.amount()));
        return started(t, init);
    }

    /** L'administration verse la rémunération validée au prestataire. */
    PaymentTransactionView payout(IdentityUser u, long paymentId, String rawPhone, String rawOperator) {
        var p = db.payment(paymentId);
        if (!"out".equals(p.direction())) throw Problems.bad("Seule une rémunération peut être versée.");
        if (!Set.of("validated", "partial").contains(p.status()))
            throw Problems.bad("La rémunération doit d'abord être validée (mission validée).");
        String phone = rawPhone == null || rawPhone.isBlank() ? directory.find(p.userId()).map(r -> r.phone()).orElse(null) : rawPhone;
        var t = open(u, p, "out", phone(phone), operator(rawOperator));
        var init = call(() -> gateway.payout("MN-TX-" + t.id(), t.amount(), t.phone(), t.operator(), p.note()));
        audit.record(u, "payment.payout_start", "payment", paymentId, Map.of("transaction", t.id(), "amount", t.amount(), "phone", t.phone()));
        return started(t, init);
    }

    List<PaymentTransaction> forPayment(IdentityUser u, long paymentId) {
        var p = db.payment(paymentId);
        if (!p.userId().equals(u.getId()) && !IdentityPermissions.has(u, "payments.manage")) throw Problems.notFound("Paiement introuvable.");
        refreshPending(paymentId);
        return db.jdbc.sql("SELECT " + TX_COLS + " FROM giving_payment_transactions WHERE payment_id = :p ORDER BY id DESC")
                .param("p", paymentId).query(PaymentTransaction.class).list();
    }

    List<PaymentTransaction> recent() {
        return db.jdbc.sql("SELECT " + TX_COLS + " FROM giving_payment_transactions ORDER BY id DESC LIMIT 200").query(PaymentTransaction.class).list();
    }

    /** Filet de sécurité si un webhook se perd : on relit le statut chez le fournisseur. */
    private void refreshPending(long paymentId) {
        var pending = db.jdbc.sql("SELECT " + TX_COLS + " FROM giving_payment_transactions WHERE payment_id = :p AND status = 'pending' AND provider_ref IS NOT NULL")
                .param("p", paymentId).query(PaymentTransaction.class).list();
        for (var t : pending) settle(t, gateway.fetchStatus(t.providerRef()));
    }

    /** Webhook du fournisseur. Le corps n'est jamais cru tel quel : seule la relecture du statut fait foi. */
    void webhook(String body, String signature) {
        if (!gateway.verifyWebhook(body, signature)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Signature invalide.");
        String ref;
        try {
            JsonNode node = json.readTree(body);
            ref = node.path("reference").asText("");
        } catch (Exception e) {
            throw Problems.bad("Corps de webhook illisible.");
        }
        var t = db.jdbc.sql("SELECT " + TX_COLS + " FROM giving_payment_transactions WHERE provider = :prov AND provider_ref = :r")
                .param("prov", gateway.name()).param("r", ref).query(PaymentTransaction.class).optional().orElse(null);
        if (t == null) return; // référence inconnue : ignorée (le fournisseur ne doit pas réessayer indéfiniment)
        settle(t, gateway.fetchStatus(ref));
    }

    /** Applique l'issue confirmée par le fournisseur, une seule fois. */
    private void settle(PaymentTransaction t, String status) {
        if (!"pending".equals(t.status()) || !Set.of("succeeded", "failed").contains(status)) return;
        int changed = db.jdbc.sql("UPDATE giving_payment_transactions SET status = :s, settled_at = :now, failure_reason = :f WHERE id = :id AND status = 'pending'")
                .param("s", status).param("now", GivingDb.now()).param("f", "failed".equals(status) ? "Refusé ou annulé chez l'opérateur" : null)
                .param("id", t.id()).update();
        if (changed == 0) return; // déjà traité par un autre appel
        var p = db.payment(t.paymentId());
        boolean in = "in".equals(t.direction());
        if ("failed".equals(status)) {
            notifications.notify(p.userId(), "payment", in ? "Paiement non abouti" : "Reversement non abouti",
                    fcfa(t.amount()) + " — vous pouvez réessayer.", in ? link(p) : "/espace/paiements");
            if (!in) notifications.notifyAdmins("payments.manage", "payment", "Reversement échoué", fcfa(t.amount()), "/admin/paiements");
            audit.record(null, "system", in ? "payment.online_failed" : "payment.payout_failed", "payment", p.id(), Map.of("transaction", t.id()));
            return;
        }
        int paid = p.amountPaid() + t.amount();
        int ceiling = p.amountValidated() != null ? p.amountValidated() : p.amountPlanned();
        String newStatus = paid >= ceiling ? "paid" : "partial";
        db.jdbc.sql("""
                UPDATE giving_payments SET status = :s, amount_paid = :a, method = 'mobile_money', reference = :r,
                    paid_at = CASE WHEN :s = 'paid' THEN :now ELSE paid_at END WHERE id = :id""")
                .param("s", newStatus).param("a", paid).param("r", t.providerRef()).param("now", GivingDb.now()).param("id", p.id()).update();
        audit.record(null, "system", in ? "payment.online_paid" : "payment.payout_paid", "payment", p.id(),
                Map.of("transaction", t.id(), "amount", t.amount(), "reference", t.providerRef()));
        if (in) {
            notifications.notify(p.userId(), "payment", "Paiement confirmé", fcfa(t.amount()) + " reçus. Merci !", link(p));
            notifications.notifyAdmins("payments.manage", "payment", "Paiement Mobile Money reçu", fcfa(t.amount()) + " · réf. " + t.providerRef(), "/admin/paiements");
        } else {
            notifications.notify(p.userId(), "payment", "Votre rémunération a été versée", fcfa(t.amount()) + " sur " + t.phone(), "/espace/paiements");
        }
    }

    private static String link(GivingRows.PaymentRow p) {
        if (p.requestId() != null) return "/espace/demandes/" + p.requestId();
        if (p.serviceOrderId() != null) return "/espace/services/" + p.serviceOrderId();
        return "/espace";
    }

    private static String fcfa(int amount) {
        return String.format("%,d FCFA", amount).replace(',', ' ');
    }

    /* ───────── Fournisseur simulé ───────── */

    /** Le testeur choisit l'issue d'une transaction simulée ; elle repasse par le webhook signé, comme en réel. */
    PaymentTransactionView simulate(IdentityUser u, long txId, String outcome) {
        if (!(gateway instanceof SimulatedPaymentGateway sim)) throw Problems.notFound("Simulation indisponible.");
        var t = tx(txId);
        var p = db.payment(t.paymentId());
        boolean owner = "in".equals(t.direction()) && p.userId().equals(u.getId());
        if (!owner && !IdentityPermissions.has(u, "payments.manage")) throw Problems.notFound("PaymentTransaction introuvable.");
        if (!"pending".equals(t.status())) throw Problems.bad("Cette transaction est déjà terminée.");
        if (!Set.of("success", "failure").contains(outcome)) throw Problems.bad("Issue invalide.");
        String body = sim.settle(t.providerRef(), "success".equals(outcome));
        webhook(body, sim.sign(body));
        var after = tx(txId);
        return new PaymentTransactionView(after, true, "succeeded".equals(after.status()) ? "Paiement simulé confirmé." : "Paiement simulé refusé.");
    }
}
