package com.menewa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Exporte le contrat de référence dans `docs/api/` (OpenAPI + référentiel des libellés).
 * `pnpm contract` en génère ensuite les types et constantes des clients web et mobile, et vérifie leurs appels.
 */
class ContractExportTests extends ApiTestBase {
    private static final Path OUT = Path.of("..", "docs", "api");

    /** Deux records de même nom simple seraient fusionnés en un seul schéma OpenAPI : on l'interdit. */
    @Test
    void recordNamesAreUniqueForTheContract() {
        var scanner = new org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter((reader, factory) -> "java.lang.Record".equals(reader.getClassMetadata().getSuperClassName()));
        var names = scanner.findCandidateComponents("com.menewa").stream()
                .filter(c -> !c.getBeanClassName().split("\\$")[0].matches(".*(Test|Tests|TestBase)$")) // code de production seulement
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
        var mapper = json.copy();
        if (sortKeys) mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        // Fins de ligne LF quel que soit le système : le fichier exporté est identique sous Windows et en CI.
        var indenter = new DefaultIndenter("  ", "\n");
        String pretty = mapper.writer(new DefaultPrettyPrinter().withObjectIndenter(indenter)).writeValueAsString(tree);
        Files.writeString(OUT.resolve(file), pretty + "\n", StandardCharsets.UTF_8);
    }
}
