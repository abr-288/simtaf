package com.menewa;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Base des tests d'intégration : vrai PostgreSQL 16 embarqué (démarré une fois pour toute la suite, sans Docker),
 * migrations Flyway réelles, contexte Spring partagé entre les classes de test.
 */
@SpringBootTest(properties = "menewa.identity.bootstrap-admin-phone=" + ApiTestBase.ADMIN_PHONE)
@AutoConfigureMockMvc
public abstract class ApiTestBase {
    protected static final String ADMIN_PHONE = "+22890009001";
    private static final AtomicLong PHONES = new AtomicLong(System.nanoTime() % 10_000_000L);
    private static EmbeddedPostgres postgres;

    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper json;

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) throws IOException {
        synchronized (ApiTestBase.class) {
            if (postgres == null) postgres = EmbeddedPostgres.builder().start();
        }
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
    }

    /* ───────── Comptes ───────── */
    protected static String newPhone() {
        return "+228" + String.format("%08d", 10_000_000L + PHONES.incrementAndGet() % 89_999_999L);
    }

    protected String login(String phone) throws Exception {
        JsonNode otp = body(postJson("/api/auth/otp/request", null, Map.of("phone", phone, "mode", "login")).andExpect(status().isOk()));
        return verify(phone, otp.get("devCode").asText());
    }

    private static String adminToken;

    /** Session de l'administrateur principal, ouverte une fois (l'envoi d'OTP est limité à 3 par 10 minutes). */
    protected String admin() throws Exception {
        synchronized (ApiTestBase.class) {
            if (adminToken == null) adminToken = login(ADMIN_PHONE);
            return adminToken;
        }
    }

    /** Inscrit un nouveau compte du rôle demandé et renvoie son jeton. */
    protected Account register(String role) throws Exception {
        String phone = newPhone();
        JsonNode otp = body(postJson("/api/auth/otp/request", null,
                Map.of("phone", phone, "mode", "register", "name", "Test " + role, "role", role)).andExpect(status().isOk()));
        String token = verify(phone, otp.get("devCode").asText());
        long id = body(call(HttpMethod.GET, "/api/auth/me", token, null)).get("id").asLong();
        return new Account(id, phone, token);
    }

    /** Administrateur secondaire avec exactement les permissions données. */
    protected Account subadmin(String... permissions) throws Exception {
        Account a = register("donor");
        call(HttpMethod.PUT, "/api/v1/admin/users/" + a.id() + "/role", admin(), Map.of("role", "subadmin", "permissions", List.of(permissions)))
                .andExpect(status().isOk());
        return a;
    }

    private String verify(String phone, String code) throws Exception {
        return body(postJson("/api/auth/otp/verify", null, Map.of("phone", phone, "code", code)).andExpect(status().isOk()))
                .get("accessToken").asText();
    }

    public record Account(long id, String phone, String token) {}

    /* ───────── Requêtes ───────── */
    protected ResultActions postJson(String url, String token, Object payload) throws Exception {
        return call(HttpMethod.POST, url, token, payload == null ? Map.of() : payload);
    }

    protected ResultActions getAs(String url, String token) throws Exception {
        var request = get(url);
        if (token != null) request.header("Authorization", "Bearer " + token);
        return mvc.perform(request);
    }

    protected ResultActions call(HttpMethod method, String url, String token, Object payload) throws Exception {
        var request = MockMvcRequestBuilders.request(method, url);
        if (token != null) request.header("Authorization", "Bearer " + token);
        if (payload != null) request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(payload));
        return mvc.perform(request);
    }

    protected JsonNode body(ResultActions result) throws Exception {
        String content = result.andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        return content.isEmpty() ? json.nullNode() : json.readTree(content);
    }

    /** Article actif du catalogue (créé au besoin), pour les parcours de don. */
    protected long catalogItem() throws Exception {
        JsonNode created = body(postJson("/api/v1/admin/catalog", admin(), Map.of("title", "Article " + newPhone(), "category", "nourriture",
                "description", "Test", "unitPrice", 10000, "costEstimate", 8000, "unit", "pack", "emoji", "🍚", "active", true))
                .andExpect(status().isCreated()));
        return created.get("id").asLong();
    }

    /** Demande soumise par un donateur (panier → validation) ; renvoie son identifiant. */
    protected long submittedRequest(Account donor) throws Exception {
        postJson("/api/v1/cart", donor.token(), Map.of("catalogItemId", catalogItem(), "quantity", 1)).andExpect(status().isCreated());
        return body(postJson("/api/v1/cart/checkout", donor.token(), Map.of("targetName", "Famille test",
                "schedule", Map.of("recurrence", "none", "paymentMode", "immediate"), "annex", Map.of())).andExpect(status().isCreated()))
                .get("id").asLong();
    }
}
