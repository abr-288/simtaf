package com.menewa.giving;

import static org.assertj.core.api.Assertions.assertThat;

import com.menewa.ApiTestBase;
import java.sql.Timestamp;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Relances planifiées : un rappel avant l'échéance, une relance en cas de retard, jamais deux fois. */
class GivingRemindersTests extends ApiTestBase {
    @Autowired GivingReminders reminders;
    @Autowired JdbcClient jdbc;

    private void dueIn(long requestId, long seconds) {
        jdbc.sql("UPDATE giving_installments SET due_date = :d WHERE request_id = :r")
                .param("d", Timestamp.from(Instant.now().plusSeconds(seconds))).param("r", requestId).update();
    }

    private String titles(Account a) throws Exception {
        return body(getAs("/api/v1/notifications", a.token())).findValuesAsText("title").toString();
    }

    @Test
    void remindsBeforeDueDateThenWhenLateOnlyOnce() throws Exception {
        Account donor = register("donor");
        long requestId = submittedRequest(donor);
        String ref = body(getAs("/api/v1/requests/" + requestId, donor.token())).at("/request/ref").asText();

        dueIn(requestId, 86_400); // demain : dans le délai de rappel (3 jours par défaut)
        reminders.run();
        reminders.run(); // idempotent
        assertThat(titles(donor)).containsOnlyOnce("Échéance proche — " + ref);

        dueIn(requestId, -86_400); // hier, toujours pas réalisée
        reminders.run();
        reminders.run();
        assertThat(titles(donor)).containsOnlyOnce("Échéance en retard — " + ref);
    }

    @Test
    void doneInstallmentsAreNotChased() throws Exception {
        Account donor = register("donor");
        long requestId = submittedRequest(donor);
        String ref = body(getAs("/api/v1/requests/" + requestId, donor.token())).at("/request/ref").asText();
        jdbc.sql("UPDATE giving_installments SET status = 'done' WHERE request_id = :r").param("r", requestId).update();
        dueIn(requestId, -86_400);
        reminders.run();
        assertThat(titles(donor)).doesNotContain("Échéance en retard — " + ref);
    }
}
