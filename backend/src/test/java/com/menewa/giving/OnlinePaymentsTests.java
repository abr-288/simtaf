package com.menewa.giving;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.menewa.ApiTestBase;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;

/** Paiement Mobile Money avec le fournisseur simulé : aucun argent réel, mêmes règles qu'avec un agrégateur. */
class OnlinePaymentsTests extends ApiTestBase {
    @Autowired JdbcClient jdbc;

    private JsonNode firstPayment(Account donor) throws Exception {
        submittedRequest(donor);
        return body(getAs("/api/v1/payments/mine", donor.token())).get(0);
    }

    @Test
    void clientPaysOnlineAndThePaymentIsMarkedPaidOnlyAfterConfirmation() throws Exception {
        Account donor = register("donor"), other = register("donor");
        JsonNode payment = firstPayment(donor);
        long id = payment.get("id").asLong();
        int amount = payment.get("remaining").asInt();

        getAs("/api/v1/payments/online/config", null).andExpect(jsonPath("$.simulated").value(true));
        postJson("/api/v1/payments/" + id + "/pay", other.token(), Map.of("phone", "90000001", "operator", "tmoney")).andExpect(status().isNotFound());
        postJson("/api/v1/payments/" + id + "/pay", donor.token(), Map.of("phone", "123", "operator", "tmoney")).andExpect(status().isBadRequest());
        postJson("/api/v1/payments/" + id + "/pay", donor.token(), Map.of("phone", "90000001", "operator", "orange")).andExpect(status().isBadRequest());

        JsonNode started = body(postJson("/api/v1/payments/" + id + "/pay", donor.token(), Map.of("phone", "90 00 00 01", "operator", "tmoney"))
                .andExpect(status().isCreated()));
        assertThat(started.at("/transaction/status").asText()).isEqualTo("pending");
        assertThat(started.at("/transaction/amount").asInt()).isEqualTo(amount);   // montant fixé par le serveur
        assertThat(started.at("/transaction/phone").asText()).isEqualTo("+22890000001");
        long txId = started.at("/transaction/id").asLong();

        // Pas de double débit : une seule transaction en attente par paiement ; rien n'est payé avant confirmation.
        postJson("/api/v1/payments/" + id + "/pay", donor.token(), Map.of("phone", "90000001", "operator", "tmoney")).andExpect(status().isConflict());
        assertThat(body(getAs("/api/v1/payments/mine", donor.token())).get(0).get("status").asText()).isNotEqualTo("paid");

        // Un webhook non signé ou mal signé est refusé.
        String forged = "{\"reference\":\"" + started.at("/transaction/providerRef").asText() + "\",\"status\":\"succeeded\"}";
        mvc.perform(post("/api/v1/payments/webhook").contentType(MediaType.APPLICATION_JSON).content(forged)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/payments/webhook").contentType(MediaType.APPLICATION_JSON).content(forged).header("X-Menewa-Signature", "00ff"))
                .andExpect(status().isUnauthorized());

        postJson("/api/v1/payments/transactions/" + txId + "/simulate", other.token(), Map.of("outcome", "success")).andExpect(status().isNotFound());
        postJson("/api/v1/payments/transactions/" + txId + "/simulate", donor.token(), Map.of("outcome", "success"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.transaction.status").value("succeeded"));
        JsonNode paid = body(getAs("/api/v1/payments/mine", donor.token())).get(0);
        assertThat(paid.get("status").asText()).isEqualTo("paid");
        assertThat(paid.get("amountPaid").asInt()).isEqualTo(amount);
        assertThat(paid.get("method").asText()).isEqualTo("mobile_money");
        assertThat(paid.get("reference").asText()).startsWith("SIM-IN-");

        // Confirmation reçue deux fois : appliquée une seule fois ; plus rien à payer ensuite.
        postJson("/api/v1/payments/transactions/" + txId + "/simulate", donor.token(), Map.of("outcome", "success")).andExpect(status().isBadRequest());
        assertThat(body(getAs("/api/v1/payments/mine", donor.token())).get(0).get("amountPaid").asInt()).isEqualTo(amount);
        postJson("/api/v1/payments/" + id + "/pay", donor.token(), Map.of("phone", "90000001", "operator", "tmoney")).andExpect(status().isBadRequest());
        assertThat(body(getAs("/api/v1/payments/" + id + "/transactions", donor.token()))).hasSize(1);
        assertThat(body(getAs("/api/v1/notifications", donor.token())).toString()).contains("Paiement confirmé");
    }

    @Test
    void failedPaymentCanBeRetried() throws Exception {
        Account donor = register("donor");
        long id = firstPayment(donor).get("id").asLong();
        long tx = body(postJson("/api/v1/payments/" + id + "/pay", donor.token(), Map.of("phone", "99000002", "operator", "flooz"))).at("/transaction/id").asLong();
        postJson("/api/v1/payments/transactions/" + tx + "/simulate", donor.token(), Map.of("outcome", "failure"))
                .andExpect(jsonPath("$.transaction.status").value("failed"));
        assertThat(body(getAs("/api/v1/payments/mine", donor.token())).get(0).get("amountPaid").asInt()).isZero();
        postJson("/api/v1/payments/" + id + "/pay", donor.token(), Map.of("phone", "99000002", "operator", "flooz")).andExpect(status().isCreated());
    }

    @Test
    void adminPaysOutAValidatedProviderFeeToTheProviderPhone() throws Exception {
        Account provider = register("provider");
        String admin = admin();
        long planned = jdbc.sql("""
                INSERT INTO giving_payments (user_id, direction, kind, mode, amount_planned, status, note)
                VALUES (:u, 'out', 'prestation', 'money', 5000, 'planned', 'Rémunération test') RETURNING id""")
                .param("u", provider.id()).query(Long.class).single();
        postJson("/api/v1/admin/payments/" + planned + "/payout", admin, Map.of("operator", "tmoney"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("validée")));
        jdbc.sql("UPDATE giving_payments SET status = 'validated', amount_validated = 4000 WHERE id = :id").param("id", planned).update();
        postJson("/api/v1/admin/payments/" + planned + "/payout", provider.token(), Map.of("operator", "tmoney")).andExpect(status().isForbidden());

        JsonNode started = body(postJson("/api/v1/admin/payments/" + planned + "/payout", admin, Map.of("operator", "tmoney")).andExpect(status().isCreated()));
        assertThat(started.at("/transaction/phone").asText()).isEqualTo(provider.phone());   // numéro du compte prestataire par défaut
        assertThat(started.at("/transaction/amount").asInt()).isEqualTo(4000);               // montant validé, pas prévu
        // Le prestataire ne peut pas confirmer lui-même son reversement.
        long tx = started.at("/transaction/id").asLong();
        postJson("/api/v1/payments/transactions/" + tx + "/simulate", provider.token(), Map.of("outcome", "success")).andExpect(status().isNotFound());
        postJson("/api/v1/payments/transactions/" + tx + "/simulate", admin, Map.of("outcome", "success")).andExpect(status().isOk());

        JsonNode row = body(getAs("/api/v1/payments/mine", provider.token())).get(0);
        assertThat(row.get("status").asText()).isEqualTo("paid");
        assertThat(row.get("amountPaid").asInt()).isEqualTo(4000);
        assertThat(body(getAs("/api/v1/admin/payment-transactions", admin)).findValuesAsText("status")).contains("succeeded");
        assertThat(body(getAs("/api/v1/notifications", provider.token())).toString()).contains("rémunération a été versée");
    }
}
