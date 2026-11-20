package com.menewa.giving;

import com.menewa.platform.Problems;
import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

/** Accès SQL commun du module : insertions avec clé générée, lectures des agrégats principaux, historique. */
@Component
class GivingDb {
    final JdbcClient jdbc;

    GivingDb(JdbcClient jdbc) { this.jdbc = jdbc; }

    long insert(String sql, Map<String, ?> params) {
        var keys = new GeneratedKeyHolder();
        jdbc.sql(sql).params(params).update(keys, "id");
        return keys.getKey().longValue();
    }

    GivingRows.RequestRow request(long id) {
        return jdbc.sql("SELECT " + GivingRows.REQUEST_COLS + " FROM giving_requests WHERE id = :id").param("id", id)
                .query(GivingRows.RequestRow.class).optional().orElseThrow(() -> Problems.notFound("Demande introuvable."));
    }

    GivingRows.MissionRow mission(long id) {
        return jdbc.sql("SELECT " + GivingRows.MISSION_COLS + " FROM giving_missions WHERE id = :id").param("id", id)
                .query(GivingRows.MissionRow.class).optional().orElseThrow(() -> Problems.notFound("Mission introuvable."));
    }

    GivingRows.PaymentRow payment(long id) {
        return jdbc.sql("SELECT " + GivingRows.PAYMENT_COLS + " FROM giving_payments WHERE id = :id").param("id", id)
                .query(GivingRows.PaymentRow.class).optional().orElseThrow(() -> Problems.notFound("Paiement introuvable."));
    }

    GivingRows.ProofRow proof(long id) {
        return jdbc.sql("SELECT " + GivingRows.PROOF_COLS + " FROM giving_proofs WHERE id = :id").param("id", id)
                .query(GivingRows.ProofRow.class).optional().orElseThrow(() -> Problems.notFound("Preuve introuvable."));
    }

    GivingRows.DisputeRow dispute(long id) {
        return jdbc.sql("SELECT " + GivingRows.DISPUTE_COLS + " FROM giving_disputes WHERE id = :id").param("id", id)
                .query(GivingRows.DisputeRow.class).optional().orElseThrow(() -> Problems.notFound("Litige introuvable."));
    }

    void history(long requestId, String status, String note, Long actorId) {
        jdbc.sql("INSERT INTO giving_request_history (request_id, status, note, actor_id) VALUES (:r, :s, :n, :a)")
                .param("r", requestId).param("s", status).param("n", note == null ? null : Problems.trim(note, 2000)).param("a", actorId).update();
    }

    void setRequestStatus(long requestId, String status) {
        jdbc.sql("UPDATE giving_requests SET status = :s, updated_at = :now WHERE id = :id")
                .param("s", status).param("now", OffsetDateTime.now()).param("id", requestId).update();
    }

    static OffsetDateTime now() { return OffsetDateTime.now(); }
}
