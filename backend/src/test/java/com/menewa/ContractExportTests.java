package com.menewa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Exporte le contrat de référence dans `docs/api/` (OpenAPI + référentiel des libellés).
 * `pnpm contract` en génère ensuite les types et constantes des clients web et mobile, et vérifie leurs appels.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ContractExportTests {
    private static final Path OUT = Path.of("..", "docs", "api");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    /** Deux records de même nom simple seraient fusionnés en un seul schéma OpenAPI : on l'interdit. */
    @Test
    void recordNamesAreUniqueForTheContract() {
        var scanner = new org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter((reader, factory) -> "java.lang.Record".equals(reader.getClassMetadata().getSuperClassName()));
        var names = scanner.findCandidateComponents("com.menewa").stream()
                .map(c -> c.getBeanClassName().substring(c.getBeanClassName().lastIndexOf('$') + 1).replaceAll(".*\\.", "")).toList();
        assertThat(names).doesNotHaveDuplicates();
    }

    @Test
    void exportsOpenApiAndReference() throws Exception {
        Files.createDirectories(OUT);
        export("/v3/api-docs", "openapi.json", true);
        export("/api/v1/reference", "reference.json", false); // ordre métier conservé (menus, étapes)
        assertThat(Files.readString(OUT.resolve("openapi.json"))).contains("/api/v1/requests/{id}", "/api/v1/reference");
    }

    private void export(String url, String file, boolean sortKeys) throws Exception {
        String body = mvc.perform(get(url)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        Object tree = json.readValue(body, Object.class);
        var writer = json.copy().enable(SerializationFeature.INDENT_OUTPUT);
        if (sortKeys) writer.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        String pretty = writer.writeValueAsString(tree);
        Files.writeString(OUT.resolve(file), pretty + "\n", StandardCharsets.UTF_8);
    }
}
