package com.menewa.platform;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.menewa.ApiTestBase;
import com.menewa.sms.SmsSender;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

/** SMS de notification : mis en file pendant l'action, envoyés en arrière-plan, réessayés puis abandonnés. */
class SmsOutboxTests extends ApiTestBase {
    @Autowired Notifications notifications;
    @Autowired SmsOutbox outbox;
    @Autowired JdbcClient jdbc;

    private Map<String, Object> row(long userId) {
        return jdbc.sql("SELECT status, attempts, last_error, next_attempt_at > NOW() AS later FROM platform_sms_outbox WHERE user_id = :u ORDER BY id DESC LIMIT 1")
                .param("u", userId).query().singleRow();
    }

    /** Le travailleur traite 20 messages par passage : on vide la file (d'autres tests y ont laissé des SMS). */
    private static void drain(SmsOutbox o) {
        for (int i = 0; i < 1000 && o.deliverDue() > 0; i++) { /* passage suivant */ }
    }

    @Test
    void notificationIsQueuedThenSentInBackground() throws Exception {
        Account member = register("donor");
        notifications.notify(member.id(), "request_decision", "Votre demande est acceptée", null, "/espace");
        // L'action n'a rien envoyé : le SMS attend dans la file, la notification in-app est déjà là.
        assertThat(row(member.id())).containsEntry("status", "pending").containsEntry("attempts", 0);
        assertThat(body(getAs("/api/v1/notifications", member.token())).toString()).contains("Votre demande est acceptée");

        drain(outbox); // passerelle de démonstration (aucune URL) : envoi simulé réussi
        assertThat(row(member.id())).containsEntry("status", "sent").containsEntry("attempts", 1);
    }

    @Test
    void failedSendsAreRetriedThenAbandoned() throws Exception {
        Account member = register("donor");
        notifications.notify(member.id(), "request_decision", "Décision", null, null);
        var down = new SmsSender(new ObjectMapper(), "", "", "", "", "", "", "", "https://api.twilio.com") {
            @Override public String send(String to, String message) { return "HTTP 503"; }
        };
        var failingOutbox = new SmsOutbox(jdbc, down);

        drain(failingOutbox);
        assertThat(row(member.id())).containsEntry("status", "pending").containsEntry("attempts", 1)
                .containsEntry("last_error", "HTTP 503").containsEntry("later", true); // nouvel essai plus tard
        for (int i = 2; i <= SmsOutbox.MAX_ATTEMPTS; i++) {
            jdbc.sql("UPDATE platform_sms_outbox SET next_attempt_at = :t WHERE user_id = :u")
                    .param("t", Timestamp.from(Instant.now().minusSeconds(1))).param("u", member.id()).update();
            drain(failingOutbox);
        }
        assertThat(row(member.id())).containsEntry("status", "failed").containsEntry("attempts", SmsOutbox.MAX_ATTEMPTS);
        assertThat(body(getAs("/api/v1/admin/sms", admin())).at("/summary/failed").asLong()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void membersWithoutSmsPreferenceReceiveNoSms() throws Exception {
        Account member = register("donor");
        jdbc.sql("UPDATE identity_users SET notif_sms = FALSE WHERE id = :u").param("u", member.id()).update();
        notifications.notify(member.id(), "request_decision", "Décision", null, null);
        assertThat(jdbc.sql("SELECT COUNT(*) FROM platform_sms_outbox WHERE user_id = :u").param("u", member.id()).query(Long.class).single()).isZero();
    }
}
