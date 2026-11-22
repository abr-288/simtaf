package com.menewa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.menewa.rewards.Rewards;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;

/** Règles métier reprises de Menewa Naya, vérifiées de bout en bout sur PostgreSQL. */
class BusinessRulesTests extends ApiTestBase {

    @Nested
    class Identity {
        @Test
        void otpSendingIsRateLimited() throws Exception {
            String phone = newPhone();
            for (int i = 0; i < 3; i++)
                postJson("/api/auth/otp/request", null, Map.of("phone", phone, "mode", "register", "name", "Limite", "role", "donor")).andExpect(status().isOk());
            postJson("/api/auth/otp/request", null, Map.of("phone", phone, "mode", "register", "name", "Limite", "role", "donor"))
                    .andExpect(status().isTooManyRequests()).andExpect(jsonPath("$.detail").value("otp_rate_limited"));
        }

        @Test
        void otpAttemptsAreLimitedAndWrongCodesRejected() throws Exception {
            String phone = newPhone();
            postJson("/api/auth/otp/request", null, Map.of("phone", phone, "mode", "register", "name", "Essais", "role", "donor")).andExpect(status().isOk());
            for (int i = 0; i < 5; i++)
                postJson("/api/auth/otp/verify", null, Map.of("phone", phone, "code", "000000")).andExpect(status().isUnauthorized());
            postJson("/api/auth/otp/verify", null, Map.of("phone", phone, "code", "000000"))
                    .andExpect(status().isTooManyRequests()).andExpect(jsonPath("$.detail").value("otp_attempts_exceeded"));
        }

        @Test
        void registrationAndLoginRespectAccountState() throws Exception {
            Account member = register("donor");
            postJson("/api/auth/otp/request", null, Map.of("phone", member.phone(), "mode", "register", "name", "Doublon", "role", "donor"))
                    .andExpect(status().isConflict());
            postJson("/api/auth/otp/request", null, Map.of("phone", newPhone(), "mode", "login")).andExpect(status().isNotFound());
            // Un compte suspendu ne peut plus se connecter et sa session tombe.
            call(HttpMethod.PATCH, "/api/v1/admin/users/" + member.id() + "/status", admin(), Map.of("status", "suspended")).andExpect(status().isOk());
            getAs("/api/auth/me", member.token()).andExpect(status().isUnauthorized());
            postJson("/api/auth/otp/request", null, Map.of("phone", member.phone(), "mode", "login")).andExpect(status().isForbidden());
        }

        @Test
        void administratorsCannotDemoteThemselvesAndProfilesAreValidated() throws Exception {
            long adminId = body(getAs("/api/auth/me", admin())).get("id").asLong();
            call(HttpMethod.PUT, "/api/v1/admin/users/" + adminId + "/role", admin(), Map.of("role", "donor", "permissions", java.util.List.of()))
                    .andExpect(status().isBadRequest());
            call(HttpMethod.PUT, "/api/auth/me", register("client").token(), Map.of("name", "A", "notifInapp", true, "notifSms", false, "notifEmail", false))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("Nom trop court."));
        }
    }

    @Nested
    class Giving {
        @Test
        void requestTransitionsAreGuarded() throws Exception {
            Account donor = register("donor");
            String admin = admin();
            long id = submittedRequest(donor);
            postJson("/api/v1/admin/requests/" + id + "/decision", admin, Map.of("decision", "reject", "message", "non")).andExpect(status().isBadRequest());
            postJson("/api/v1/admin/requests/" + id + "/decision", admin, Map.of("decision", "info", "message", "Précisez l'adresse")).andExpect(status().isOk());
            postJson("/api/v1/requests/" + id + "/answer", donor.token(), Map.of("answer", "Quartier Bè, Lomé")).andExpect(status().isOk());
            getAs("/api/v1/requests/" + id, donor.token()).andExpect(jsonPath("$.request.status").value("submitted"));
            postJson("/api/v1/admin/requests/" + id + "/decision", admin, Map.of("decision", "accept")).andExpect(status().isOk());
            postJson("/api/v1/admin/requests/" + id + "/decision", admin, Map.of("decision", "accept")).andExpect(status().isBadRequest());
            postJson("/api/v1/requests/" + id + "/cancel", donor.token(), null)
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail", containsString("ne peut plus être annulée")));
            postJson("/api/v1/admin/requests/" + id + "/complete", admin, null)
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail", containsString("preuve de remise validée")));
            postJson("/api/v1/admin/requests/" + id + "/close", admin, null).andExpect(status().isBadRequest());
        }

        @Test
        void requestCreationIsIdempotentAndAmountsComputedServerSide() throws Exception {
            Account donor = register("donor");
            Map<String, Object> custom = Map.of("kind", "personnalise", "title", "Repas de fête", "targetName", "Orphelinat", "amount", 20000,
                    "schedule", Map.of("recurrence", "monthly", "occurrences", 3, "paymentMode", "echeance"),
                    "annex", Map.of("photos", true), "idempotencyKey", "cle-" + newPhone());
            JsonNode first = body(postJson("/api/v1/requests", donor.token(), custom).andExpect(status().isCreated()));
            JsonNode again = body(postJson("/api/v1/requests", donor.token(), custom).andExpect(status().isCreated()));
            assertThat(again.get("id").asLong()).isEqualTo(first.get("id").asLong());

            JsonNode detail = body(getAs("/api/v1/requests/" + first.get("id").asLong(), donor.token()));
            assertThat(detail.at("/request/amountPerOccurrence").asInt()).isEqualTo(23000);   // 20 000 + photos (3 000)
            assertThat(detail.at("/request/totalAmount").asInt()).isEqualTo(69000);
            assertThat(detail.get("installments")).hasSize(3);
            assertThat(detail.get("payments")).hasSize(3);                                   // un paiement par échéance
            postJson("/api/v1/requests", donor.token(), Map.of("kind", "personnalise", "title", "Trop petit", "targetName", "X", "amount", 100,
                    "schedule", Map.of("recurrence", "none", "paymentMode", "immediate"), "annex", Map.of()))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("Le budget minimal est de 500 FCFA."));
            postJson("/api/v1/requests", register("provider").token(), custom).andExpect(status().isForbidden());
        }

        @Test
        void missionsAndProofsFollowTheRules() throws Exception {
            Account donor = register("donor"), provider = register("provider");
            String admin = admin();
            long id = submittedRequest(donor);
            String soon = java.time.LocalDate.now().plusDays(7).toString();
            postJson("/api/v1/admin/requests/" + id + "/missions", admin, Map.of("title", "Trop tôt", "providerId", provider.id(), "compensationMode", "money", "providerFee", 0))
                    .andExpect(status().isBadRequest());
            postJson("/api/v1/admin/requests/" + id + "/decision", admin, Map.of("decision", "accept")).andExpect(status().isOk());
            postJson("/api/v1/admin/requests/" + id + "/missions", admin, Map.of("title", "Sans prestataire", "providerId", 0, "exceptionReason", "court",
                    "compensationMode", "money", "providerFee", 0)).andExpect(status().isBadRequest());
            postJson("/api/v1/admin/requests/" + id + "/missions", admin, Map.of("title", "Remise par l'équipe", "providerId", 0,
                    "exceptionReason", "Remise effectuée directement par l'équipe Menewa", "compensationMode", "money", "providerFee", 1000,
                    "dueDate", soon)).andExpect(status().isCreated());
            // Intitulé, échéance et coût de la prestation sont obligatoires.
            postJson("/api/v1/admin/requests/" + id + "/missions", admin, Map.of("title", "Livraison", "providerId", provider.id(), "compensationMode", "money", "providerFee", 2000))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("Date d'échéance requise."));
            postJson("/api/v1/admin/requests/" + id + "/missions", admin, Map.of("title", "Livraison", "providerId", provider.id(), "compensationMode", "money", "providerFee", 2000,
                    "dueDate", java.time.LocalDate.now().minusDays(1).toString())).andExpect(status().isBadRequest());
            postJson("/api/v1/admin/requests/" + id + "/missions", admin, Map.of("title", "Livraison", "providerId", provider.id(), "compensationMode", "money", "providerFee", 0,
                    "dueDate", soon)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail", containsString("Coût de la prestation")));
            postJson("/api/v1/admin/requests/" + id + "/missions", admin, Map.of("title", "", "providerId", provider.id(), "compensationMode", "money", "providerFee", 2000,
                    "dueDate", soon)).andExpect(status().isBadRequest());
            postJson("/api/v1/admin/requests/" + id + "/missions", admin, Map.of("title", "Livraison", "providerId", provider.id(), "compensationMode", "money", "providerFee", 2000,
                    "dueDate", soon)).andExpect(status().isCreated());
            long missionId = body(getAs("/api/v1/missions/mine", provider.token())).get(0).get("id").asLong();
            postJson("/api/v1/missions/" + missionId + "/respond", provider.token(), Map.of("decision", "refuse", "reason", "non"))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("Un motif de refus est obligatoire."));
            postJson("/api/v1/missions/" + missionId + "/respond", provider.token(), Map.of("decision", "accept")).andExpect(status().isOk());
            getAs("/api/v1/requests/" + id, donor.token()).andExpect(jsonPath("$.request.status").value("in_progress"));
            // Pas de preuve ni de clôture sans démarrage.
            postJson("/api/v1/missions/" + missionId + "/complete", provider.token(), null)
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("Démarrez la mission avant de la terminer."));
            mvc.perform(multipart("/api/v1/proofs").file(new MockMultipartFile("file", "remise.png", "image/png", new byte[] {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10}))
                            .param("missionId", String.valueOf(missionId)).param("type", "photo").header("Authorization", "Bearer " + provider.token()))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail", containsString("Démarrez la mission")));
            postJson("/api/v1/missions/" + missionId + "/start", provider.token(), null).andExpect(status().isOk());
            // Le type réel du fichier est contrôlé (signature), pas son extension.
            mvc.perform(multipart("/api/v1/proofs").file(new MockMultipartFile("file", "faux.png", "image/png", "pas une image".getBytes()))
                            .param("missionId", String.valueOf(missionId)).param("type", "photo").header("Authorization", "Bearer " + provider.token()))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail", containsString("Format non autorisé")));
            postJson("/api/v1/missions/" + missionId + "/complete", provider.token(), null).andExpect(status().isBadRequest());
            postJson("/api/v1/admin/missions/" + missionId + "/validate", admin, null).andExpect(status().isBadRequest());
        }
    }

    @Nested
    class TokenMena {
        @Autowired Rewards rewards;

        @Test
        void automaticAwardsAreCappedDedupedAndNeverSelfGranted() throws Exception {
            Account member = register("client");     // +20 à l'inscription
            // Règle « review_given » : 5 Mena, plafond 15 par jour.
            assertThat(rewards.award(member.id(), "review_given", "test", 1L, null)).isEqualTo(5);
            assertThat(rewards.award(member.id(), "review_given", "test", 1L, null)).isNull();   // même objet : anti-doublon
            assertThat(rewards.award(member.id(), "review_given", "test", 2L, null)).isEqualTo(5);
            assertThat(rewards.award(member.id(), "review_given", "test", 3L, null)).isEqualTo(5);
            assertThat(rewards.award(member.id(), "review_given", "test", 4L, null)).isNull();   // plafond quotidien atteint
            assertThat(rewards.award(member.id(), "request_closed", "test", 9L, member.id())).isNull(); // auto-attribution
            assertThat(rewards.award(member.id(), "evenement_inconnu", "test", 1L, null)).isNull();
            getAs("/api/v1/rewards/me", member.token()).andExpect(jsonPath("$.balance").value(35));
        }

        @Test
        void largeManualAdjustmentsNeedAnotherMainAdministrator() throws Exception {
            Account member = register("client");
            Account manager = subadmin("tokens.manage");
            postJson("/api/v1/admin/rewards/adjust", manager.token(), Map.of("userId", member.id(), "amount", 600, "reason", "Geste exceptionnel"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.ok", containsString("en attente")));
            getAs("/api/v1/rewards/me", member.token()).andExpect(jsonPath("$.balance").value(20));
            long txId = 0;
            for (JsonNode t : body(getAs("/api/v1/admin/rewards", admin())).get("transactions"))
                if (t.get("userId").asLong() == member.id() && "pending".equals(t.get("status").asText())) txId = t.get("id").asLong();
            postJson("/api/v1/admin/rewards/transactions/" + txId + "/decision", manager.token(), Map.of("approve", true)).andExpect(status().isForbidden());
            postJson("/api/v1/admin/rewards/transactions/" + txId + "/decision", admin(), Map.of("approve", true)).andExpect(status().isOk());
            getAs("/api/v1/rewards/me", member.token()).andExpect(jsonPath("$.balance").value(620));
            postJson("/api/v1/admin/rewards/adjust", admin(), Map.of("userId", member.id(), "amount", -1000, "reason", "Correction erronée"))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("Le solde ne peut pas devenir négatif."));
        }

        @Test
        void transfersStayDisabledUntilTheMainAdministratorEnablesThem() throws Exception {
            Account member = register("client");
            postJson("/api/v1/rewards/transfer", member.token(), Map.of("phone", ADMIN_PHONE, "amount", 5))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail", containsString("désactivés")));
            call(HttpMethod.PUT, "/api/v1/admin/settings/feature.token_transfer", subadmin("settings.manage").token(), Map.of("value", true))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail", containsString("administrateur principal")));
            call(HttpMethod.PUT, "/api/v1/admin/settings/feature.token_transfer", admin(), Map.of("value", "oui")).andExpect(status().isBadRequest());
        }
    }

    @Nested
    class Community {
        @Test
        void priceNegotiationNeedsTheOtherPartyToAgree() throws Exception {
            Account client = register("client"), provider = register("provider");
            long orderId = body(postJson("/api/v1/services/orders", client.token(), Map.of("providerId", provider.id(), "title", "Repas pour 20 personnes",
                    "compensationMode", "money", "price", 5000)).andExpect(status().isCreated())).get("id").asLong();
            postJson("/api/v1/services/orders/" + orderId + "/transition", client.token(), Map.of("action", "accept"))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail", containsString("propre proposition")));
            postJson("/api/v1/services/orders/" + orderId + "/messages", provider.token(), Map.of("body", "Plutôt 6 000", "price", 6000)).andExpect(status().isOk());
            postJson("/api/v1/services/orders/" + orderId + "/transition", client.token(), Map.of("action", "accept")).andExpect(status().isOk());
            JsonNode order = body(getAs("/api/v1/services/orders/" + orderId, client.token()));
            assertThat(order.at("/order/agreedPrice").asInt()).isEqualTo(6000);
            assertThat(order.get("payments")).hasSize(1);
            getAs("/api/v1/services/orders/" + orderId, register("client").token()).andExpect(status().isNotFound());
            postJson("/api/v1/services/orders/" + orderId + "/transition", provider.token(), Map.of("action", "start")).andExpect(status().isOk());
            postJson("/api/v1/services/orders/" + orderId + "/transition", provider.token(), Map.of("action", "done")).andExpect(status().isOk());
            postJson("/api/v1/services/orders/" + orderId + "/transition", client.token(), Map.of("action", "confirm")).andExpect(status().isBadRequest());
            postJson("/api/v1/services/orders/" + orderId + "/transition", client.token(), Map.of("action", "confirm", "rating", 5)).andExpect(status().isOk());
        }

        @Test
        void campaignsAreModeratedAndContributionsConfirmed() throws Exception {
            Account partner = register("partner"), donor = register("donor");
            String admin = admin();
            postJson("/api/v1/listings", partner.token(), Map.of("type", "crowdfunding", "title", "Puits du village", "goalAmount", 5000))
                    .andExpect(status().isBadRequest());
            postJson("/api/v1/listings", donor.token(), Map.of("type", "action", "title", "Pas partenaire", "goalAmount", 0)).andExpect(status().isForbidden());
            String title = "Puits " + newPhone();
            postJson("/api/v1/listings", partner.token(), Map.of("type", "crowdfunding", "title", title, "goalAmount", 100000)).andExpect(status().isCreated());
            long listingId = body(getAs("/api/v1/listings/mine", partner.token())).get(0).at("/listing/id").asLong();
            postJson("/api/v1/listings/" + listingId + "/contributions", donor.token(), Map.of("amount", 2000, "method", "cash")).andExpect(status().isNotFound());
            postJson("/api/v1/admin/listings/" + listingId + "/moderate", admin, Map.of("decision", "publish")).andExpect(status().isOk());
            postJson("/api/v1/listings/" + listingId + "/contributions", donor.token(), Map.of("amount", 2000, "method", "cash")).andExpect(status().isCreated());
            long contributionId = 0;
            for (JsonNode c : body(getAs("/api/v1/admin/listings", admin)).get("pendingContributions"))
                if (c.at("/contribution/listingId").asLong() == listingId) contributionId = c.at("/contribution/id").asLong();
            postJson("/api/v1/admin/contributions/" + contributionId + "/decision", admin, Map.of("decision", "confirm")).andExpect(status().isOk());
            postJson("/api/v1/admin/contributions/" + contributionId + "/decision", admin, Map.of("decision", "confirm")).andExpect(status().isNotFound());
            assertThat(body(getAs("/api/v1/listings/mine", partner.token())).get(0).at("/listing/raisedAmount").asInt()).isEqualTo(2000);
        }
    }
}
