package com.menewa.giving;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.menewa.ApiTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;

/** Photos de l'accueil : téléversées par l'administration, servies publiquement. */
class HomePhotosTests extends ApiTestBase {
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    private org.springframework.test.web.servlet.ResultActions upload(String slot, String token, byte[] bytes) throws Exception {
        return mvc.perform(multipart(HttpMethod.PUT, "/api/v1/admin/home-photos/" + slot)
                .file(new MockMultipartFile("file", "photo.png", "image/png", bytes)).param("alt", "Remise de repas à Lomé")
                .header("Authorization", "Bearer " + token));
    }

    @Test
    void adminUploadsAPhotoThatEveryoneCanSee() throws Exception {
        String admin = admin();
        upload("hero", register("donor").token(), PNG).andExpect(status().isForbidden());
        upload("inconnu", admin, PNG).andExpect(status().isNotFound());
        upload("hero", admin, "%PDF-1.4 faux".getBytes()).andExpect(status().isBadRequest());

        upload("hero", admin, PNG).andExpect(status().isOk());
        var list = body(getAs("/api/v1/public/home-photos", null));
        assertThat(list).hasSize(5);
        assertThat(list.get(0).get("slot").asText()).isEqualTo("hero");
        assertThat(list.get(0).get("available").asBoolean()).isTrue();
        assertThat(list.get(0).get("alt").asText()).isEqualTo("Remise de repas à Lomé");
        getAs("/api/v1/public/home-photos/hero", null).andExpect(status().isOk())
                .andExpect(content().contentType("image/png")).andExpect(content().bytes(PNG));

        call(HttpMethod.DELETE, "/api/v1/admin/home-photos/hero", admin, null).andExpect(status().isOk());
        getAs("/api/v1/public/home-photos/hero", null).andExpect(status().isNotFound());
        assertThat(body(getAs("/api/v1/public/home-photos", null)).get(0).get("available").asBoolean()).isFalse();
    }
}
