package com.menewa.platform;

import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * File d'envoi des SMS de notification (« outbox ») :
 * {@link #enqueue} écrit dans la transaction de l'action métier ; {@link #deliverDue} envoie en arrière-plan.
 * Échec : nouvel essai après 1 min, 5 min, 30 min puis 2 h ; abandon (« failed ») au 5e échec.
 * Un envoi interrompu (arrêt du serveur) est repris après expiration de son bail de 5 minutes.
 */
@Component
class SmsOutbox {
    private static final Logger log = LoggerFactory.getLogger(SmsOutbox.class);
    static final int MAX_ATTEMPTS = 5;
    private static final List<Duration> BACKOFF = List.of(Duration.ofMinutes(1), Duration.ofMinutes(5), Duration.ofMinutes(30), Duration.ofHours(2));
    private static final Duration LEASE = Duration.ofMinutes(5);
    private static final int BATCH = 20;

    private final JdbcClient jdbc;
    private final SmsGateway gateway;

    SmsOutbox(JdbcClient jdbc, SmsGateway gateway) {
        this.jdbc = jdbc;
        this.gateway = gateway;
    }

    void enqueue(Long userId, String phone, String message, String type) {
        jdbc.sql("INSERT INTO platform_sms_outbox (user_id, phone, message, notification_type) VALUES (:u, :p, :m, :t)")
                .param("u", userId).param("p", phone).param("m", Problems.trim(message, 480)).param("t", type).update();
    }

    record Claimed(Long id, String phone, String message, int attempts) {}

    /** Envoie les SMS arrivés à échéance ; renvoie le nombre de messages traités. */
    @Scheduled(fixedDelayString = "${menewa.sms.outbox-delay:PT20S}", initialDelayString = "${menewa.sms.outbox-initial-delay:PT30S}")
    int deliverDue() {
        // Réservation atomique (SKIP LOCKED) : deux exécutions simultanées ne prennent jamais le même message.
        // Horloge de la base pour toutes les comparaisons (même référence que les valeurs par défaut des colonnes).
        List<Claimed> batch = jdbc.sql("""
                UPDATE platform_sms_outbox SET status = 'sending', attempts = attempts + 1,
                       next_attempt_at = NOW() + make_interval(secs => :leaseSeconds)
                WHERE id IN (SELECT id FROM platform_sms_outbox WHERE status IN ('pending', 'sending') AND next_attempt_at <= NOW()
                             ORDER BY next_attempt_at LIMIT :n FOR UPDATE SKIP LOCKED)
                RETURNING id, phone, message, attempts""")
                .param("leaseSeconds", LEASE.toSeconds()).param("n", BATCH).query(Claimed.class).list();
        for (Claimed sms : batch) {
            String error = gateway.send(sms.phone(), sms.message());
            if (error == null) {
                jdbc.sql("UPDATE platform_sms_outbox SET status = 'sent', sent_at = NOW(), last_error = NULL WHERE id = :id")
                        .param("id", sms.id()).update();
            } else if (sms.attempts() >= MAX_ATTEMPTS) {
                jdbc.sql("UPDATE platform_sms_outbox SET status = 'failed', last_error = :e WHERE id = :id")
                        .param("e", Problems.trim(error, 500)).param("id", sms.id()).update();
                log.warn("SMS {} abandonné après {} essais : {}", sms.id(), sms.attempts(), error);
            } else {
                Duration wait = BACKOFF.get(Math.min(sms.attempts(), BACKOFF.size()) - 1);
                jdbc.sql("UPDATE platform_sms_outbox SET status = 'pending', next_attempt_at = NOW() + make_interval(secs => :wait), last_error = :e WHERE id = :id")
                        .param("wait", wait.toSeconds()).param("e", Problems.trim(error, 500)).param("id", sms.id()).update();
            }
        }
        return batch.size();
    }

    record Summary(long pending, long sent, long failed) {}

    Summary summary() {
        return jdbc.sql("""
                SELECT COUNT(*) FILTER (WHERE status IN ('pending', 'sending')) AS pending,
                       COUNT(*) FILTER (WHERE status = 'sent') AS sent, COUNT(*) FILTER (WHERE status = 'failed') AS failed
                FROM platform_sms_outbox""").query(Summary.class).single();
    }
}
