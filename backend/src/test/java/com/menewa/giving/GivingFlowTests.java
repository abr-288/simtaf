package com.menewa.giving;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import com.menewa.ApiTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class GivingFlowTests extends ApiTestBase {
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    @Test
    void donationLifecycleFromCartToClosureAwardsTokens() throws Exception {
        String admin = admin();
        long itemId = body(postJson("/api/v1/admin/catalog", admin, Map.of("title", "Pack test", "category", "nourriture", "description", "Pack",
                "unitPrice", 15000, "costEstimate", 12000, "unit", "pack", "emoji", "🍚", "active", true)).andExpect(status().isCreated())).get("id").asLong();

        String donor = register("donor").token();
        Account providerAccount = register("provider");
        String provider = providerAccount.token();
        long providerId = providerAccount.id();

        // Le donateur ne peut pas accéder à l'administration.
        mvc.perform(get("/api/v1/admin/requests").header("Authorization", "Bearer " + donor)).andExpect(status().isForbidden());

        postJson("/api/v1/cart", donor, Map.of("catalogItemId", itemId, "quantity", 2, "target", "Famille")).andExpect(status().isCreated());
        JsonNode created = body(postJson("/api/v1/cart/checkout", donor, Map.of("targetName", "Famille Test", "targetZone", "Lomé",
                "schedule", Map.of("recurrence", "none", "paymentMode", "immediate"), "annex", Map.of("photos", true)))
                .andExpect(status().isCreated()));
        long requestId = created.get("id").asLong();
        assertThat(created.get("ref").asText()).startsWith("MN-");

        JsonNode detail = body(mvc.perform(get("/api/v1/requests/" + requestId).header("Authorization", "Bearer " + donor)));
        // 2 × 15 000 + 3 000 (photos, tarif par défaut) recalculés côté serveur.
        assertThat(detail.at("/request/totalAmount").asInt()).isEqualTo(33000);
        assertThat(detail.at("/request/annexServices/photos").asBoolean()).isTrue();
        assertThat(detail.get("payments")).hasSize(1);

        postJson("/api/v1/admin/requests/" + requestId + "/decision", admin, Map.of("decision", "accept", "message", "OK")).andExpect(status().isOk());
        postJson("/api/v1/admin/requests/" + requestId + "/missions", admin, Map.of("title", "Livrer les packs", "providerId", providerId,
                "compensationMode", "money", "providerFee", 8000)).andExpect(status().isCreated());

        long missionId = body(mvc.perform(get("/api/v1/missions/mine").header("Authorization", "Bearer " + provider))).get(0).get("id").asLong();
        postJson("/api/v1/missions/" + missionId + "/respond", provider, Map.of("decision", "accept")).andExpect(status().isOk());
        postJson("/api/v1/missions/" + missionId + "/start", provider, Map.of()).andExpect(status().isOk());
        // Pas de fin de mission sans preuve.
        postJson("/api/v1/missions/" + missionId + "/complete", provider, Map.of()).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("preuve")));
        mvc.perform(multipart("/api/v1/proofs").file(new MockMultipartFile("file", "remise.png", "image/png", PNG))
                        .param("missionId", String.valueOf(missionId)).param("type", "photo").param("caption", "Remise")
                        .header("Authorization", "Bearer " + provider))
                .andExpect(status().isCreated());
        postJson("/api/v1/missions/" + missionId + "/complete", provider, Map.of()).andExpect(status().isOk());

        long proofId = body(mvc.perform(get("/api/v1/admin/proofs").header("Authorization", "Bearer " + admin))).get(0).at("/proof/id").asLong();
        postJson("/api/v1/admin/proofs/" + proofId + "/review", admin, Map.of("decision", "validate")).andExpect(status().isOk());
        postJson("/api/v1/admin/missions/" + missionId + "/validate", admin, Map.of()).andExpect(status().isOk());

        JsonNode adminDetail = body(mvc.perform(get("/api/v1/admin/requests/" + requestId).header("Authorization", "Bearer " + admin)));
        long installmentId = adminDetail.get("installments").get(0).get("id").asLong();
        assertThat(adminDetail.at("/margin/fees").asInt()).isEqualTo(8000);
        postJson("/api/v1/admin/installments/" + installmentId + "/done", admin, Map.of()).andExpect(status().isOk());
        postJson("/api/v1/admin/requests/" + requestId + "/complete", admin, Map.of()).andExpect(status().isOk());
        postJson("/api/v1/admin/requests/" + requestId + "/close", admin, Map.of()).andExpect(status().isOk());

        // Preuve validée visible du donateur, fichier téléchargeable, avis possible.
        detail = body(mvc.perform(get("/api/v1/requests/" + requestId).header("Authorization", "Bearer " + donor)));
        assertThat(detail.at("/request/status").asText()).isEqualTo("closed");
        assertThat(detail.get("proofs")).hasSize(1);
        assertThat(detail.get("canReview").asBoolean()).isTrue();
        mvc.perform(get("/api/v1/files/proof/" + proofId).header("Authorization", "Bearer " + donor)).andExpect(status().isOk());
        postJson("/api/v1/requests/" + requestId + "/review", donor, Map.of("rating", 5, "comment", "Parfait")).andExpect(status().isOk());

        // Mena : inscription (20) + don clôturé (20) + avis (5) pour le donateur ; inscription (20) + mission validée (50) pour le prestataire.
        assertThat(body(mvc.perform(get("/api/v1/rewards/me").header("Authorization", "Bearer " + donor))).get("balance").asInt()).isEqualTo(45);
        JsonNode providerRewards = body(mvc.perform(get("/api/v1/rewards/me").header("Authorization", "Bearer " + provider)));
        assertThat(providerRewards.get("balance").asInt()).isEqualTo(70);

        JsonNode notifications = body(mvc.perform(get("/api/v1/notifications").header("Authorization", "Bearer " + donor)));
        assertThat(notifications.size()).isGreaterThan(3);
        mvc.perform(get("/api/v1/admin/stats/export").header("Authorization", "Bearer " + admin)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/admin/audit").header("Authorization", "Bearer " + admin)).andExpect(status().isOk());
    }

}
