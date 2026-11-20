package com.menewa.catalog;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.menewa.ApiTestBase;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class CatalogApiTests extends ApiTestBase {

    @Test
    void bootstrappedAdminReceivesAllPermissions() throws Exception {
        getAs("/api/auth/permissions", admin()).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(17));
    }

    @Test
    void catalogIsPublicAndManagementRequiresCatalogPermission() throws Exception {
        String title = "Kit " + newPhone();
        postJson("/api/admin/catalog", admin(), Map.of("title", title, "category", "fournitures", "description", "Un kit scolaire",
                "unitPrice", 7500, "costEstimate", 6000, "unit", "kit", "emoji", "🎒", "active", true))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.kind").value("don"));

        getAs("/api/catalog", null).andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("XOF"))
                .andExpect(jsonPath("$.items[*].title", hasItem(title)));

        getAs("/api/admin/catalog", register("donor").token()).andExpect(status().isForbidden());
    }

    @Test
    void targetsAreManagedByCatalogManagersAndListedPublicly() throws Exception {
        String admin = admin();
        String id = body(postJson("/api/v1/admin/catalog/targets", admin, Map.of("label", "Cible " + newPhone(), "description", "", "active", true))
                .andExpect(status().isOk())).get("id").asText();
        getAs("/api/v1/catalog/targets", null).andExpect(jsonPath("$[*].id", hasItem(id)));
        postJson("/api/v1/admin/catalog/targets", admin, Map.of("label", "Famille", "description", "", "active", true))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("Une cible porte déjà ce nom."));
        call(HttpMethod.DELETE, "/api/v1/admin/catalog/targets/" + id, subadmin("requests.manage").token(), null).andExpect(status().isForbidden());
        call(HttpMethod.DELETE, "/api/v1/admin/catalog/targets/" + id, admin, null).andExpect(status().isOk());
    }
}
