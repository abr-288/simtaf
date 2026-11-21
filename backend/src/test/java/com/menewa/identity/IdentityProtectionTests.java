package com.menewa.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.menewa.ApiTestBase;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;

/** Limite des demandes de code par adresse IP et ménage planifié du module identity (limites basses, contexte dédié). */
@TestPropertySource(properties = {"menewa.identity.otp.max-requests-per-ip=3", "menewa.identity.otp.max-failures-per-ip=3"})
class IdentityProtectionTests extends ApiTestBase {
    private static final AtomicInteger IP = new AtomicInteger(10);

    @Autowired IdentityMaintenance maintenance;
    @Autowired JdbcClient jdbc;

    private static String newIp() { return "203.0.113." + IP.incrementAndGet(); }

    private ResultActions otp(String path, String ip, Object payload) throws Exception {
        return mvc.perform(post("/api/auth/otp/" + path).header("X-Forwarded-For", ip + ", 10.0.0.2")
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(payload)));
    }

    @Test
    void oneAddressCannotRequestCodesForManyNumbers() throws Exception {
        String ip = newIp();
        // Trois numéros différents (même inexistants) : la limite par numéro ne joue pas, celle par IP si.
        for (int i = 0; i < 3; i++) otp("request", ip, Map.of("phone", newPhone(), "mode", "login")).andExpect(status().isNotFound());
        otp("request", ip, Map.of("phone", newPhone(), "mode", "register", "name", "Test", "role", "donor"))
                .andExpect(status().isTooManyRequests()).andExpect(jsonPath("$.detail").value("otp_ip_rate_limited"));
        // Une autre adresse n'est pas concernée.
        otp("request", newIp(), Map.of("phone", newPhone(), "mode", "register", "name", "Test", "role", "donor")).andExpect(status().isOk());
    }

    @Test
    void repeatedWrongCodesBlockTheAddress() throws Exception {
        String ip = newIp(), phone = newPhone();
        String code = body(otp("request", ip, Map.of("phone", phone, "mode", "register", "name", "Test", "role", "donor"))).get("devCode").asText();
        for (int i = 0; i < 3; i++) otp("verify", ip, Map.of("phone", phone, "code", "000000")).andExpect(status().isUnauthorized());
        // Même le bon code est refusé depuis cette adresse pendant la fenêtre.
        otp("verify", ip, Map.of("phone", phone, "code", code)).andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.detail").value("otp_ip_rate_limited"));
        otp("verify", newIp(), Map.of("phone", phone, "code", code)).andExpect(status().isOk());
    }

    @Test
    void maintenancePurgesExpiredSessionsAndOldCodes() throws Exception {
        String ip = newIp(), phone = newPhone();
        String code = body(otp("request", ip, Map.of("phone", phone, "mode", "register", "name", "Test", "role", "donor"))).get("devCode").asText();
        String token = body(otp("verify", ip, Map.of("phone", phone, "code", code))).get("accessToken").asText();
        long userId = jdbc.sql("SELECT id FROM identity_users WHERE phone = :p").param("p", phone).query(Long.class).single();
        jdbc.sql("INSERT INTO identity_sessions (token_hash, user_id, expires_at) VALUES ('expired-test-session', :u, :e)")
                .param("u", userId).param("e", java.sql.Timestamp.from(Instant.now().minusSeconds(60))).update();
        jdbc.sql("UPDATE identity_otp_challenges SET created_at = :old WHERE phone = :p")
                .param("old", java.sql.Timestamp.from(Instant.now().minusSeconds(3 * 86400))).param("p", phone).update();

        var result = maintenance.purge();

        assertThat(result.expiredSessions()).isGreaterThanOrEqualTo(1);
        assertThat(result.oldChallenges()).isGreaterThanOrEqualTo(1);
        assertThat(jdbc.sql("SELECT COUNT(*) FROM identity_sessions WHERE token_hash = 'expired-test-session'").query(Long.class).single()).isZero();
        assertThat(jdbc.sql("SELECT COUNT(*) FROM identity_otp_challenges WHERE phone = :p").param("p", phone).query(Long.class).single()).isZero();
        // La session encore valide n'est pas touchée.
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }
}
