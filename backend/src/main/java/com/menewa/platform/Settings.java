package com.menewa.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Paramètres de la plateforme (valeurs JSON). Valeurs PROVISOIRES : décisions métier et juridiques
 * à valider, modifiables en administration et auditées.
 */
@Component
public class Settings implements ApplicationRunner {
    record Default(String json, String description) {}

    static final Map<String, Default> DEFAULTS = new LinkedHashMap<>();
    static {
        DEFAULTS.put("feature.token_transfer", new Default("false", "Transferts de Mena entre membres — désactivé jusqu'à validation juridique et réglementaire."));
        DEFAULTS.put("feature.token_conversion", new Default("false", "Conversion des Mena en monnaie — désactivée jusqu'à validation juridique et réglementaire."));
        DEFAULTS.put("finance.platform_margin_pct", new Default("15", "Marge brute cible de la plateforme (%) — valeur provisoire à valider."));
        DEFAULTS.put("finance.currency", new Default("\"FCFA\"", "Devise d'affichage."));
        DEFAULTS.put("proofs.min_validated", new Default("1", "Nombre minimal de preuves validées avant de déclarer un don réalisé (règle à valider)."));
        DEFAULTS.put("proofs.accepted_types", new Default("[\"photo\",\"video\",\"receipt\",\"attestation\"]", "Types de preuves acceptés comme preuve de remise (hors factures)."));
        DEFAULTS.put("tokens.manual_adjust_max", new Default("500", "Plafond d'un ajustement manuel de Mena ; au-delà, validation de l'administrateur principal."));
        DEFAULTS.put("tokens.conversion_rate_xof", new Default("0", "Valeur d'un Mena en FCFA (0 = non défini, conversion impossible)."));
        DEFAULTS.put("tokens.level_thresholds", new Default("{\"silver\":200,\"gold\":1000,\"platinum\":3000}", "Seuils de niveaux d'engagement (Mena gagnés cumulés)."));
        DEFAULTS.put("payments.external_provider", new Default("\"manual\"", "Prestataire de paiement externe (manual | futur : mobile money / agrégateur). Intégration à prévoir."));
        DEFAULTS.put("annex.prices", new Default("{\"packaging\":2000,\"officiant\":5000,\"photos\":3000,\"video\":10000}", "Tarifs provisoires des services annexes (FCFA) : packaging, officiant religieux, photos, reportage vidéo."));
        DEFAULTS.put("requests.late_alert_days", new Default("3", "Nombre de jours avant échéance pour notifier."));
        DEFAULTS.put("public.stats", new Default("{\"beneficiaries\":\"12 500+\",\"partners\":\"420+\",\"gifts\":\"1 850+\",\"members\":\"2 300+\"}", "Chiffres d'impact affichés sur la page d'accueil (à remplacer par des données vérifiées)."));
    }

    /** Paramètres lisibles sans permission (formulaires de don, page d'accueil, Token Mena). */
    static final Set<String> PUBLIC_KEYS = Set.of("annex.prices", "feature.token_transfer", "feature.token_conversion",
            "tokens.level_thresholds", "public.stats", "finance.currency");

    private final JdbcClient jdbc;
    private final ObjectMapper json;

    Settings(JdbcClient jdbc, ObjectMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (var entry : DEFAULTS.entrySet()) {
            long n = jdbc.sql("SELECT COUNT(*) FROM platform_settings WHERE setting_key = :k").param("k", entry.getKey()).query(Long.class).single();
            if (n == 0) jdbc.sql("INSERT INTO platform_settings (setting_key, setting_value, description) VALUES (:k, :v, :d)")
                    .param("k", entry.getKey()).param("v", entry.getValue().json()).param("d", entry.getValue().description()).update();
        }
    }

    public JsonNode get(String key) {
        String raw = jdbc.sql("SELECT setting_value FROM platform_settings WHERE setting_key = :k").param("k", key)
                .query(String.class).optional().orElseGet(() -> DEFAULTS.containsKey(key) ? DEFAULTS.get(key).json() : "null");
        try { return json.readTree(raw); } catch (Exception ex) { return json.nullNode(); }
    }

    public int intValue(String key, int fallback) {
        JsonNode node = get(key);
        return node.isNumber() ? node.asInt() : fallback;
    }

    public boolean boolValue(String key) {
        JsonNode node = get(key);
        return node.isBoolean() && node.asBoolean();
    }

    public List<String> stringList(String key) {
        List<String> out = new ArrayList<>();
        get(key).forEach(n -> out.add(n.asText()));
        return out;
    }

    public Map<String, Integer> intMap(String key) {
        Map<String, Integer> out = new LinkedHashMap<>();
        get(key).fields().forEachRemaining(e -> out.put(e.getKey(), e.getValue().asInt()));
        return out;
    }

    record SettingView(String key, JsonNode value, String description, Long updatedBy, OffsetDateTime updatedAt) {}

    List<SettingView> all() {
        return jdbc.sql("SELECT setting_key, setting_value, description, updated_by, updated_at FROM platform_settings ORDER BY setting_key")
                .query((rs, i) -> new SettingView(rs.getString(1), parse(rs.getString(2)), rs.getString(3),
                        (Long) rs.getObject(4, Long.class), rs.getObject(5, OffsetDateTime.class)))
                .list();
    }

    Map<String, JsonNode> publicValues() {
        Map<String, JsonNode> out = new LinkedHashMap<>();
        PUBLIC_KEYS.stream().sorted().forEach(k -> out.put(k, get(k)));
        return out;
    }

    boolean exists(String key) {
        return jdbc.sql("SELECT COUNT(*) FROM platform_settings WHERE setting_key = :k").param("k", key).query(Long.class).single() > 0;
    }

    void write(String key, JsonNode value, Long actorId) {
        jdbc.sql("UPDATE platform_settings SET setting_value = :v, updated_by = :u, updated_at = :now WHERE setting_key = :k")
                .param("v", value.toString()).param("u", actorId).param("now", OffsetDateTime.now()).param("k", key).update();
    }

    private JsonNode parse(String raw) {
        try { return json.readTree(raw); } catch (Exception ex) { return json.nullNode(); }
    }
}
