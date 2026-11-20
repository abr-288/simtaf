package com.menewa;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import com.fasterxml.jackson.annotation.JsonRawValue;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Contrat OpenAPI de référence (`/v3/api-docs`, exporté dans `docs/api/openapi.json`).
 * L'en-tête `Authorization` des contrôleurs est décrit une seule fois comme schéma Bearer.
 */
@Configuration
class OpenApiConfig {
    private static final String BEARER = "bearer";

    @Bean
    OpenAPI menewaOpenApi() {
        return new OpenAPI()
                .info(new Info().title("API Menewa").version("v1")
                        .description("Contrat commun de l'admin, de l'espace membre (SaaS) et de l'application mobile."))
                .components(new Components().addSecuritySchemes(BEARER,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer")));
    }

    /**
     * Jackson écrit toujours tous les champs des réponses (`null` compris) : tous sont présents. Un champ de type Java
     * non primitif (hors collections) peut valoir `null` ; un champ `@JsonRawValue` contient un objet JSON.
     * Calculé par réflexion sur les records du même nom que les schémas, pour des types clients exacts.
     */
    @Bean
    OpenApiCustomizer recordFieldsContract() {
        Map<String, Class<?>> records = new HashMap<>();
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter((reader, factory) -> "java.lang.Record".equals(reader.getClassMetadata().getSuperClassName()));
        for (var candidate : scanner.findCandidateComponents("com.menewa")) {
            try {
                Class<?> c = Class.forName(candidate.getBeanClassName());
                records.putIfAbsent(c.getSimpleName(), c);
            } catch (ClassNotFoundException ignored) { /* classe non chargeable : ignorée */ }
        }
        return openApi -> {
            if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) return;
            openApi.getComponents().getSchemas().forEach((name, schema) -> {
                if (schema.getProperties() == null || schema.getProperties().isEmpty()) return;
                schema.setRequired(new ArrayList<>(schema.getProperties().keySet()));
                Class<?> record = records.get(name);
                if (record == null || record.getRecordComponents() == null) return;
                for (RecordComponent rc : record.getRecordComponents()) {
                    @SuppressWarnings("unchecked") Map<String, Schema> props = (Map<String, Schema>) (Map<?, ?>) schema.getProperties();
                    Schema<?> prop = props.get(rc.getName());
                    if (prop == null) continue;
                    if (rc.getAccessor().isAnnotationPresent(JsonRawValue.class)) {
                        props.put(rc.getName(), new ObjectSchema().nullable(true));
                        continue;
                    }
                    Class<?> t = rc.getType();
                    if (t.isPrimitive() || Collection.class.isAssignableFrom(t) || Map.class.isAssignableFrom(t)) continue;
                    if (prop.get$ref() != null) props.put(rc.getName(), new ComposedSchema().allOf(List.of(new Schema<>().$ref(prop.get$ref()))).nullable(true));
                    else prop.setNullable(true);
                }
            });
        };
    }

    @Bean
    OperationCustomizer bearerHeaderAsSecurity() {
        return (operation, handlerMethod) -> {
            if (operation.getParameters() != null
                    && operation.getParameters().removeIf(p -> "header".equals(p.getIn()) && "Authorization".equalsIgnoreCase(p.getName()))) {
                operation.addSecurityItem(new SecurityRequirement().addList(BEARER));
            }
            return operation;
        };
    }
}
