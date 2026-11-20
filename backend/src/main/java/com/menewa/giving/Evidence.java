package com.menewa.giving;

import com.menewa.identity.IdentityPermissions;
import com.menewa.identity.IdentityUser;
import com.menewa.platform.Audit;
import com.menewa.platform.Notifications;
import com.menewa.platform.Problems;
import com.menewa.rewards.Rewards;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/** Preuves de remise, paiements (déclaration donateur, suivi administratif), litiges et bénéficiaires. */
@Service
@Transactional
class Evidence {
    static final Set<String> PROOF_TYPES = Set.of("photo", "video", "receipt", "invoice", "attestation");
    static final Set<String> PAY_METHODS = Set.of("mobile_money", "card", "bank", "cash");

    private final GivingDb db;
    private final Storage storage;
    private final Notifications notifications;
    private final Audit audit;
    private final Rewards rewards;

    Evidence(GivingDb db, Storage storage, Notifications notifications, Audit audit, Rewards rewards) {
        this.db = db;
        this.storage = storage;
        this.notifications = notifications;
        this.audit = audit;
        this.rewards = rewards;
    }

    /* ───────── Preuves ───────── */
    String uploadProof(IdentityUser u, Long missionId, Long requestId, String type, String caption, boolean hidden, MultipartFile file) {
        if (!PROOF_TYPES.contains(type)) throw Problems.bad("Type de preuve invalide.");
        boolean adminUploader = IdentityPermissions.has(u, "proofs.review");
        Long reqId = requestId;
        if (missionId != null && missionId > 0) {
            var m = db.mission(missionId);
            boolean mine = u.getId().equals(m.providerId());
            if (!mine && !adminUploader) throw Problems.forbidden();
            if (mine && !Set.of("accepted", "in_progress", "completed").contains(m.status())) throw Problems.bad("Mission non active.");
            reqId = m.requestId();
        } else if (!adminUploader) {
            throw Problems.forbidden();
        }
        if (reqId == null || reqId == 0) throw Problems.bad("Demande introuvable.");
        db.request(reqId);
        var saved = storage.save(file, "proofs");
        if (saved == null) throw Problems.bad("Sélectionnez un fichier.");
        Map<String, Object> p = new HashMap<>();
        p.put("r", reqId); p.put("m", missionId == null || missionId == 0 ? null : missionId); p.put("t", type); p.put("k", saved.key());
        p.put("n", saved.name()); p.put("mime", saved.mime()); p.put("size", saved.size()); p.put("sha", saved.sha256());
        p.put("c", Problems.trimToNull(caption, 300)); p.put("u", u.getId()); p.put("v", !hidden);
        long id = db.insert("""
                INSERT INTO giving_proofs (request_id, mission_id, type, file_key, file_name, mime, size, sha256, caption, uploaded_by, visible_to_donor)
                VALUES (:r, :m, :t, :k, :n, :mime, :size, :sha, :c, :u, :v)""", p);
        Map<String, Object> details = new HashMap<>();
        details.put("requestId", reqId); details.put("missionId", missionId); details.put("type", type);
        audit.record(u, "proof.upload", "proof", id, details);
        notifications.notifyAdmins("proofs.review", "proof_review", "Nouvelle preuve à contrôler", saved.name(), "/admin/preuves");
        return "Preuve déposée. Elle sera contrôlée par l'administration.";
    }

    String reviewProof(IdentityUser u, long id, String decision, String reasonRaw, boolean hide) {
        var p = db.proof(id);
        String reason = Problems.trim(reasonRaw, 500);
        if (!Set.of("validate", "reject").contains(decision)) throw Problems.bad("Décision invalide.");
        if ("reject".equals(decision) && reason.length() < 5) throw Problems.bad("Motif de rejet obligatoire.");
        boolean validate = "validate".equals(decision);
        boolean visible = !hide && p.visibleToDonor();
        db.jdbc.sql("""
                UPDATE giving_proofs SET status = :s, rejection_reason = :r, reviewed_by = :u, reviewed_at = :now, visible_to_donor = :v WHERE id = :id""")
                .param("s", validate ? "validated" : "rejected").param("r", validate ? null : reason).param("u", u.getId())
                .param("now", GivingDb.now()).param("v", visible).param("id", id).update();
        audit.record(u, "proof." + decision, "proof", id, reason.isEmpty() ? null : Map.of("reason", reason));
        notifications.notify(p.uploadedBy(), "proof_review", validate ? "Preuve validée" : "Preuve rejetée", reason.isEmpty() ? p.fileName() : reason,
                p.missionId() != null ? "/espace/missions/" + p.missionId() : null);
        if (validate && visible) {
            var r = db.request(p.requestId());
            if (!r.userId().equals(p.uploadedBy()))
                notifications.notify(r.userId(), "proof_review", "Nouvelle preuve disponible — " + r.ref(), null, "/espace/demandes/" + r.id());
        }
        if (validate) rewards.award(p.uploadedBy(), "proof_validated", "proof", id, u.getId());
        return validate ? "Preuve validée." : "Preuve rejetée.";
    }

    /* ───────── Paiements ───────── */
    String declarePayment(IdentityUser u, long id, String method, String referenceRaw, MultipartFile receipt) {
        var p = db.payment(id);
        if (!p.userId().equals(u.getId()) || !"in".equals(p.direction())) throw Problems.notFound("Paiement introuvable.");
        if (!Set.of("planned", "declared").contains(p.status())) throw Problems.bad("Ce paiement ne peut plus être modifié.");
        String reference = Problems.trim(referenceRaw, 100);
        if (!PAY_METHODS.contains(method)) throw Problems.bad("Choisissez un moyen de paiement.");
        if (!"cash".equals(method) && reference.length() < 4) throw Problems.bad("Indiquez la référence de transaction.");
        var saved = storage.save(receipt, "receipts");
        db.jdbc.sql("""
                UPDATE giving_payments SET status = 'declared', method = :m, reference = :r,
                    receipt_key = COALESCE(:k, receipt_key), receipt_name = COALESCE(:n, receipt_name), receipt_mime = COALESCE(:mime, receipt_mime)
                WHERE id = :id""")
                .param("m", method).param("r", reference).param("k", saved == null ? null : saved.key())
                .param("n", saved == null ? null : saved.name()).param("mime", saved == null ? null : saved.mime()).param("id", id).update();
        audit.record(u, "payment.declare", "payment", id, Map.of("method", method, "reference", reference));
        notifications.notifyAdmins("payments.manage", "payment", "Paiement déclaré à valider", "Référence " + (reference.isEmpty() ? "—" : reference), "/admin/paiements");
        return "Paiement déclaré. Il sera validé par l'administration après vérification.";
    }

    record PaymentUpdate(String status, Integer amountValidated, Integer amountPaid, String reference, String note, String exchangeStatus) {}

    String updatePayment(IdentityUser u, long id, PaymentUpdate r, MultipartFile receipt) {
        var p = db.payment(id);
        if (!Set.of("planned", "declared", "validated", "partial", "paid", "cancelled").contains(r.status())) throw Problems.bad("Statut invalide.");
        Integer validated = r.amountValidated() != null ? Math.max(r.amountValidated(), 0) : p.amountValidated();
        int paid = Math.max(r.amountPaid() == null ? p.amountPaid() : r.amountPaid(), 0);
        int ceiling = validated != null ? validated : p.amountPlanned();
        if ("money".equals(p.mode()) && paid > ceiling) throw Problems.bad("Le montant payé ne peut excéder le montant validé / prévu.");
        var saved = storage.save(receipt, "receipts");
        String exchangeStatus = r.exchangeStatus() == null || r.exchangeStatus().isBlank() ? p.exchangeStatus() : r.exchangeStatus();
        if (exchangeStatus != null && !Set.of("pending", "in_progress", "done").contains(exchangeStatus)) throw Problems.bad("Statut d'échange invalide.");
        Map<String, Object> params = new HashMap<>();
        params.put("s", r.status()); params.put("av", validated); params.put("ap", paid);
        params.put("ref", r.reference() == null || r.reference().isBlank() ? p.reference() : Problems.trim(r.reference(), 100));
        params.put("note", r.note() == null || r.note().isBlank() ? p.note() : Problems.trim(r.note(), 400));
        params.put("es", exchangeStatus);
        params.put("paidAt", "paid".equals(r.status()) ? OffsetDateTime.now() : p.paidAt());
        params.put("k", saved == null ? p.receiptKey() : saved.key());
        params.put("n", saved == null ? p.receiptName() : saved.name());
        params.put("mime", saved == null ? p.receiptMime() : saved.mime());
        params.put("id", id);
        db.jdbc.sql("""
                UPDATE giving_payments SET status = :s, amount_validated = :av, amount_paid = :ap, reference = :ref, note = :note,
                    exchange_status = :es, paid_at = :paidAt, receipt_key = :k, receipt_name = :n, receipt_mime = :mime WHERE id = :id""")
                .params(params).update();
        Map<String, Object> details = new HashMap<>();
        details.put("status", r.status()); details.put("validated", validated); details.put("paid", paid);
        audit.record(u, "payment.update", "payment", id, details);
        if ("in".equals(p.direction()) && p.requestId() != null && Set.of("paid", "validated").contains(r.status()))
            notifications.notify(p.userId(), "payment", "paid".equals(r.status()) ? "Paiement confirmé" : "Paiement validé",
                    (paid > 0 ? paid : ceiling) + " FCFA", "/espace/demandes/" + p.requestId());
        if ("out".equals(p.direction()) && "paid".equals(r.status()))
            notifications.notify(p.userId(), "payment", "Votre rémunération a été réglée", null, "/espace/paiements");
        return "Paiement mis à jour.";
    }

    /* ───────── Litiges ───────── */
    String openDispute(IdentityUser u, long requestId, String subjectRaw, String descriptionRaw) {
        var r = db.request(requestId);
        var myMission = db.jdbc.sql("SELECT " + GivingRows.MISSION_COLS + " FROM giving_missions WHERE request_id = :r AND provider_id = :u ORDER BY id")
                .param("r", requestId).param("u", u.getId()).query(GivingRows.MissionRow.class).list().stream().findFirst().orElse(null);
        boolean owner = r.userId().equals(u.getId());
        if (!owner && myMission == null) throw Problems.forbidden();
        String subject = Problems.trim(subjectRaw, 150), description = Problems.trim(descriptionRaw, 2000);
        if (subject.length() < 4 || description.length() < 10) throw Problems.bad("Précisez l'objet et la description du litige.");
        Long against = owner
                ? db.jdbc.sql("SELECT provider_id FROM giving_missions WHERE request_id = :r ORDER BY id").param("r", requestId)
                        .query(Long.class).list().stream().filter(java.util.Objects::nonNull).findFirst().orElse(null)
                : r.userId();
        Map<String, Object> p = new HashMap<>();
        p.put("ref", "tmp-" + java.util.UUID.randomUUID()); p.put("r", requestId); p.put("m", myMission == null ? null : myMission.id());
        p.put("o", u.getId()); p.put("a", against); p.put("s", subject); p.put("d", description);
        long id = db.insert("""
                INSERT INTO giving_disputes (ref, request_id, mission_id, opened_by, against_user_id, subject, description)
                VALUES (:ref, :r, :m, :o, :a, :s, :d)""", p);
        db.jdbc.sql("UPDATE giving_disputes SET ref = :ref WHERE id = :id")
                .param("ref", "LT-" + java.time.Year.now().getValue() + "-" + String.format("%04d", id)).param("id", id).update();
        db.history(requestId, r.status(), "Litige ouvert : " + subject, u.getId());
        audit.record(u, "dispute.open", "dispute", id, Map.of("requestId", requestId));
        notifications.notifyAdmins("disputes.manage", "dispute", "Nouveau litige : " + subject, null, "/admin/litiges/" + id);
        return "Litige enregistré. L'administration va l'examiner.";
    }

    boolean canSeeDispute(IdentityUser u, GivingRows.DisputeRow d) {
        return u.getId().equals(d.openedBy()) || u.getId().equals(d.againstUserId()) || IdentityPermissions.has(u, "disputes.manage");
    }

    String postDisputeMessage(IdentityUser u, long id, String bodyRaw, MultipartFile attachment) {
        var d = db.dispute(id);
        if (!canSeeDispute(u, d)) throw Problems.forbidden();
        if ("closed".equals(d.status())) throw Problems.bad("Ce litige est clos.");
        String body = Problems.trim(bodyRaw, 2000);
        if (body.length() < 2) throw Problems.bad("Message vide.");
        var saved = storage.save(attachment, "disputes");
        Map<String, Object> p = new HashMap<>();
        p.put("d", id); p.put("a", u.getId()); p.put("b", body); p.put("k", saved == null ? null : saved.key());
        p.put("n", saved == null ? null : saved.name()); p.put("m", saved == null ? null : saved.mime());
        db.insert("""
                INSERT INTO giving_dispute_messages (dispute_id, author_id, body, attachment_key, attachment_name, attachment_mime)
                VALUES (:d, :a, :b, :k, :n, :m)""", p);
        for (Long t : new Long[]{d.openedBy(), d.againstUserId(), d.responsibleId()})
            if (t != null && !t.equals(u.getId()))
                notifications.notify(t, "dispute", "Nouveau message — litige " + d.ref(), Problems.trim(body, 100), "/espace/litiges/" + id);
        return "Message envoyé.";
    }

    String updateDispute(IdentityUser u, long id, String status, String decisionRaw, Long responsibleId) {
        var d = db.dispute(id);
        if (!Set.of("open", "investigating", "resolved", "closed").contains(status)) throw Problems.bad("Statut invalide.");
        String decision = Problems.trim(decisionRaw, 2000);
        if (Set.of("resolved", "closed").contains(status) && decision.length() < 10)
            throw Problems.bad("Une décision motivée est requise pour résoudre ou clore un litige.");
        Long responsible = responsibleId == null || responsibleId == 0 ? null : responsibleId;
        db.jdbc.sql("UPDATE giving_disputes SET status = :s, decision = :d, responsible_id = :r, closed_at = :c WHERE id = :id")
                .param("s", status).param("d", decision.isEmpty() ? d.decision() : decision).param("r", responsible)
                .param("c", "closed".equals(status) ? OffsetDateTime.now() : null).param("id", id).update();
        Map<String, Object> details = new HashMap<>();
        details.put("status", status); details.put("responsible", responsible);
        audit.record(u, "dispute.update", "dispute", id, details);
        for (Long t : new Long[]{d.openedBy(), d.againstUserId()})
            if (t != null) notifications.notify(t, "dispute", "Litige " + d.ref() + " : " + ("closed".equals(status) ? "clos" : "mis à jour"),
                    decision.isEmpty() ? null : Problems.trim(decision, 100), "/espace/litiges/" + id);
        return "Litige mis à jour.";
    }

    /* ───────── Bénéficiaires ───────── */
    record BeneficiaryRequest(String fullName, String phone, String zone, String address, String needs, boolean consent, Long userId) {}

    String createBeneficiary(IdentityUser u, BeneficiaryRequest r) {
        String name = Problems.trim(r.fullName(), 120);
        if (name.length() < 2) throw Problems.bad("Nom requis.");
        if (!r.consent()) throw Problems.bad("Le consentement du bénéficiaire à la collecte de ses données est requis.");
        Map<String, Object> p = new HashMap<>();
        p.put("n", name); p.put("p", Problems.trimToNull(r.phone(), 30)); p.put("z", Problems.trimToNull(r.zone(), 100));
        p.put("a", Problems.trimToNull(r.address(), 200)); p.put("needs", Problems.trimToNull(r.needs(), 500)); p.put("uid", r.userId()); p.put("by", u.getId());
        long id = db.insert("""
                INSERT INTO giving_beneficiaries (full_name, phone, zone, address, needs, consent, user_id, created_by)
                VALUES (:n, :p, :z, :a, :needs, TRUE, :uid, :by)""", p);
        audit.record(u, "beneficiary.create", "beneficiary", id);
        return "Bénéficiaire enregistré.";
    }

    String archiveBeneficiary(IdentityUser u, long id, boolean restore) {
        int n = db.jdbc.sql("UPDATE giving_beneficiaries SET archived_at = :a WHERE id = :id")
                .param("a", restore ? null : OffsetDateTime.now()).param("id", id).update();
        if (n == 0) throw Problems.notFound("Bénéficiaire introuvable.");
        audit.record(u, restore ? "beneficiary.restore" : "beneficiary.archive", "beneficiary", id);
        return restore ? "Bénéficiaire restauré." : "Bénéficiaire archivé.";
    }
}
