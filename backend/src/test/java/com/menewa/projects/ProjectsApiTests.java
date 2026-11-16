package com.menewa.projects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(properties = "menewa.identity.bootstrap-admin-phone=+22890001002")
@AutoConfigureMockMvc
class ProjectsApiTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createsAndListsProjects() throws Exception {
        String token = registerAndAuthenticate();
        mvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Projet de démonstration","description":"Créé par le SaaS"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Projet de démonstration"));

        mvc.perform(get("/api/projects").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Projet de démonstration"))
                .andExpect(jsonPath("$[0].description").value("Créé par le SaaS"));
    }

    @Test
    void bootstrappedAdminReceivesAdministrativePermissions() throws Exception {
        String token = loginAdmin();
        mvc.perform(get("/api/auth/permissions").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(17));
    }

    @Test
    void catalogIsPublicAndManagementRequiresCatalogPermission() throws Exception {
        String adminToken = loginAdmin();
        mvc.perform(post("/api/admin/catalog")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Kit de démonstration","category":"fournitures","description":"Un kit scolaire","unitPrice":7500,"costEstimate":6000,"unit":"kit","emoji":"🎒","active":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.kind").value("don"));

        mvc.perform(get("/api/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("XOF"))
                .andExpect(jsonPath("$.items[0].title").value("Kit de démonstration"));

        String donorToken = registerAndAuthenticate();
        mvc.perform(get("/api/admin/catalog").header("Authorization", "Bearer " + donorToken))
                .andExpect(status().isForbidden());
    }

    private String loginAdmin() throws Exception {
        MvcResult request = mvc.perform(post("/api/auth/otp/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phone":"+22890001002","mode":"login"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String code = objectMapper.readTree(request.getResponse().getContentAsString()).get("devCode").asText();
        MvcResult verified = mvc.perform(post("/api/auth/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("phone", "+22890001002", "code", code))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("admin"))
                .andReturn();
        return objectMapper.readTree(verified.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String registerAndAuthenticate() throws Exception {
        String phone = "+228" + String.format("%08d", Math.floorMod(System.nanoTime(), 100_000_000));
        MvcResult request = mvc.perform(post("/api/auth/otp/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "phone", phone, "name", "Test Donor", "role", "donor", "mode", "register"))))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode requestBody = objectMapper.readTree(request.getResponse().getContentAsString());
        String code = requestBody.get("devCode").asText();
        MvcResult verified = mvc.perform(post("/api/auth/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("phone", phone, "code", code))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("donor"))
                .andReturn();
        return objectMapper.readTree(verified.getResponse().getContentAsString()).get("accessToken").asText();
    }
}
