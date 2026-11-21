package com.menewa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;

/** Contrôle d'accès vérifié côté API : rôles, permissions granulaires et propriété des objets. */
class AuthorizationTests extends ApiTestBase {
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    static Stream<Arguments> adminEndpoints() {
        return Stream.of(
                Arguments.of("/api/v1/admin/requests", "requests.manage"),
                Arguments.of("/api/v1/admin/missions", "missions.manage"),
                Arguments.of("/api/v1/admin/proofs", "proofs.review"),
                Arguments.of("/api/v1/admin/payments", "payments.manage"),
                Arguments.of("/api/v1/admin/finances", "finances.view"),
                Arguments.of("/api/v1/admin/disputes", "disputes.manage"),
                Arguments.of("/api/v1/admin/users", "users.manage"),
                Arguments.of("/api/v1/admin/providers", "providers.manage"),
                Arguments.of("/api/v1/admin/beneficiaries", "beneficiaries.view"),
                Arguments.of("/api/v1/admin/listings", "listings.moderate"),
                Arguments.of("/api/v1/admin/catalog", "catalog.manage"),
                Arguments.of("/api/v1/admin/rewards", "tokens.manage"),
                Arguments.of("/api/v1/admin/stats", "stats.view"),
                Arguments.of("/api/v1/admin/stats/export", "stats.export"),
                Arguments.of("/api/v1/admin/audit", "audit.view"),
                Arguments.of("/api/v1/admin/settings", "settings.manage"));
    }

    @ParameterizedTest(name = "{0} exige {1}")
    @MethodSource("adminEndpoints")
    void adminEndpointsRequireTheirPermission(String url, String permission) throws Exception {
        getAs(url, null).andExpect(status().isUnauthorized());
        getAs(url, register("donor").token()).andExpect(status().isForbidden());
        String other = "audit.view".equals(permission) ? "stats.view" : "audit.view";
        getAs(url, subadmin(other).token()).andExpect(status().isForbidden());
        getAs(url, subadmin(permission).token()).andExpect(status().isOk());
        getAs(url, admin()).andExpect(status().isOk());
    }

    @Test
    void rolesAreReservedToTheMainAdministrator() throws Exception {
        Account sub = subadmin("users.manage");
        Account target = register("donor");
        call(HttpMethod.PUT, "/api/v1/admin/users/" + target.id() + "/role", sub.token(), Map.of("role", "subadmin", "permissions", List.of("roles.manage")))
                .andExpect(status().isForbidden());
        // Même accordée par l'administrateur principal, `roles.manage` est retirée d'un administrateur secondaire.
        Account promoted = subadmin("roles.manage", "audit.view");
        assertThat(body(getAs("/api/auth/permissions", promoted.token())).toString()).contains("audit.view").doesNotContain("roles.manage");
        // Un administrateur secondaire ne peut pas suspendre un administrateur principal.
        long adminId = body(getAs("/api/auth/me", admin())).get("id").asLong();
        call(HttpMethod.PATCH, "/api/v1/admin/users/" + adminId + "/status", sub.token(), Map.of("status", "suspended"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void membersOnlySeeTheirOwnObjects() throws Exception {
        Account owner = register("donor"), intruder = register("donor");
        long requestId = submittedRequest(owner);
        getAs("/api/v1/requests/" + requestId, owner.token()).andExpect(status().isOk());
        getAs("/api/v1/requests/" + requestId, intruder.token()).andExpect(status().isNotFound());
        postJson("/api/v1/requests/" + requestId + "/cancel", intruder.token(), null).andExpect(status().isNotFound());
        long paymentId = body(getAs("/api/v1/requests/" + requestId, owner.token())).get("payments").get(0).get("id").asLong();
        mvc.perform(multipart("/api/v1/payments/" + paymentId + "/declare").param("method", "cash")
                        .header("Authorization", "Bearer " + intruder.token()))
                .andExpect(status().isNotFound());

        // Litige : visible des parties et des gestionnaires de litiges seulement.
        postJson("/api/v1/requests/" + requestId + "/disputes", owner.token(), Map.of("subject", "Retard de livraison", "description", "La remise n'a pas eu lieu à la date prévue."))
                .andExpect(status().isCreated());
        long disputeId = body(getAs("/api/v1/requests/" + requestId, owner.token())).get("disputes").get(0).get("id").asLong();
        getAs("/api/v1/disputes/" + disputeId, owner.token()).andExpect(status().isOk());
        getAs("/api/v1/disputes/" + disputeId, intruder.token()).andExpect(status().isNotFound());
        getAs("/api/v1/disputes/" + disputeId, subadmin("disputes.manage").token()).andExpect(status().isOk());
    }

    @Test
    void providersOnlySeeTheirMissionsAndProofsStayPrivateUntilValidated() throws Exception {
        Account donor = register("donor"), provider = register("provider"), otherProvider = register("provider"), otherDonor = register("donor");
        long requestId = submittedRequest(donor);
        String admin = admin();
        postJson("/api/v1/admin/requests/" + requestId + "/decision", admin, Map.of("decision", "accept")).andExpect(status().isOk());
        String fullName = "Bénéficiaire " + newPhone();
        postJson("/api/v1/admin/beneficiaries", admin, Map.of("fullName", fullName, "phone", "+22891112233", "consent", true)).andExpect(status().isCreated());
        long beneficiaryId = 0;
        for (var b : body(getAs("/api/v1/admin/beneficiaries", admin))) if (fullName.equals(b.get("fullName").asText())) beneficiaryId = b.get("id").asLong();
        call(HttpMethod.PUT, "/api/v1/admin/requests/" + requestId + "/beneficiary", admin, Map.of("beneficiaryId", beneficiaryId)).andExpect(status().isOk());
        postJson("/api/v1/admin/requests/" + requestId + "/missions", admin, Map.of("title", "Livraison test", "providerId", provider.id(),
                "compensationMode", "money", "providerFee", 1000)).andExpect(status().isCreated());
        long missionId = body(getAs("/api/v1/missions/mine", provider.token())).get(0).get("id").asLong();

        getAs("/api/v1/missions/" + missionId, otherProvider.token()).andExpect(status().isNotFound());
        postJson("/api/v1/missions/" + missionId + "/respond", otherProvider.token(), Map.of("decision", "accept")).andExpect(status().isNotFound());
        getAs("/api/v1/missions/mine", donor.token()).andExpect(status().isForbidden());

        // Coordonnées du bénéficiaire masquées tant que la mission n'est pas acceptée.
        getAs("/api/v1/missions/" + missionId, provider.token()).andExpect(jsonPath("$.beneficiary").doesNotExist());
        postJson("/api/v1/missions/" + missionId + "/respond", provider.token(), Map.of("decision", "accept")).andExpect(status().isOk());
        getAs("/api/v1/missions/" + missionId, provider.token()).andExpect(jsonPath("$.beneficiary.fullName").value(fullName));
        mvc.perform(multipart("/api/v1/proofs").file(new MockMultipartFile("file", "p.png", "image/png", PNG))
                .param("missionId", String.valueOf(missionId)).param("type", "photo").header("Authorization", "Bearer " + provider.token()))
                .andExpect(status().isCreated());
        long proofId = body(getAs("/api/v1/missions/" + missionId, provider.token())).get("proofs").get(0).get("id").asLong();

        getAs("/api/v1/files/proof/" + proofId, provider.token()).andExpect(status().isOk());
        getAs("/api/v1/files/proof/" + proofId, donor.token()).andExpect(status().isForbidden());   // pas encore validée
        getAs("/api/v1/files/proof/" + proofId, otherDonor.token()).andExpect(status().isForbidden());
        getAs("/api/v1/files/proof/" + proofId, null).andExpect(status().isUnauthorized());
        postJson("/api/v1/admin/proofs/" + proofId + "/review", admin, Map.of("decision", "validate")).andExpect(status().isOk());
        getAs("/api/v1/files/proof/" + proofId, donor.token()).andExpect(status().isOk());
        getAs("/api/v1/files/proof/" + proofId, otherDonor.token()).andExpect(status().isForbidden());
    }

    @Test
    void sessionsAreRevokedOnLogout() throws Exception {
        Account member = register("client");
        getAs("/api/auth/me", member.token()).andExpect(status().isOk());
        postJson("/api/auth/logout", member.token(), null).andExpect(status().isNoContent());
        getAs("/api/auth/me", member.token()).andExpect(status().isUnauthorized());
        getAs("/api/auth/me", "jeton-invente").andExpect(status().isUnauthorized());
        // Inscription, connexion et déconnexion sont tracées au journal d'audit.
        assertThat(body(getAs("/api/v1/admin/audit?actor=" + member.id(), admin())).findValuesAsText("action"))
                .contains("user.register", "user.login", "user.logout");
    }
}
