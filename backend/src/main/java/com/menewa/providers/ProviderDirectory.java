package com.menewa.providers;

import com.menewa.identity.IdentityDirectory;
import com.menewa.identity.IdentityEvents;
import com.menewa.identity.UserRef;
import com.menewa.rewards.Reputation;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/** API publique : annuaire des prestataires (sélecteurs d'administration, recherche, répertoire des coûts). */
@Component
public class ProviderDirectory {
    public record Service(Long id, Long providerId, String label, String category, int unitCost, String unit, String description, boolean active) {}
    record Profile(Long userId, String category, String description, String zone, boolean available, boolean verified) {}
    public record ProviderCard(Long id, String name, String phone, String zone, String category, String description,
            boolean available, boolean verified, double rating, long reviews, List<Service> services) {}
    public record ProviderOption(Long id, String name, String category) {}

    private final JdbcClient jdbc;
    private final IdentityDirectory directory;
    private final Reputation reputation;

    ProviderDirectory(JdbcClient jdbc, IdentityDirectory directory, Reputation reputation) {
        this.jdbc = jdbc;
        this.directory = directory;
        this.reputation = reputation;
    }

    @EventListener
    void onRegistered(IdentityEvents.UserRegistered e) {
        if ("provider".equals(e.role())) ensureProfile(e.userId());
    }

    @EventListener
    void onRoleChanged(IdentityEvents.UserRoleChanged e) {
        if ("provider".equals(e.toRole())) ensureProfile(e.userId());
    }

    void ensureProfile(Long userId) {
        long n = jdbc.sql("SELECT COUNT(*) FROM providers_profiles WHERE user_id = :u").param("u", userId).query(Long.class).single();
        if (n == 0) jdbc.sql("INSERT INTO providers_profiles (user_id) VALUES (:u)").param("u", userId).update();
    }

    /** Prestataires actifs pour les listes de choix (missions, réattribution). */
    public List<ProviderOption> activeOptions() {
        var users = directory.activeWithRole("provider");
        Map<Long, String> categories = jdbc.sql("SELECT user_id, category FROM providers_profiles")
                .query((rs, i) -> Map.entry(rs.getLong(1), rs.getString(2))).list().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a));
        return users.stream().map(u -> new ProviderOption(u.id(), u.displayName(), categories.getOrDefault(u.id(), "autre"))).toList();
    }

    /** Répertoire complet (profil, services actifs, note) ; `onlyAvailable` pour la recherche publique. */
    public List<ProviderCard> cards(boolean onlyAvailable, String q, String category, String zone) {
        List<Profile> profiles = jdbc.sql("SELECT user_id, category, description, zone, available, verified FROM providers_profiles")
                .query(Profile.class).list();
        var users = directory.findAll(profiles.stream().map(Profile::userId).toList());
        var ratings = reputation.ratings(profiles.stream().map(Profile::userId).toList());
        var services = jdbc.sql("SELECT id, provider_id, label, category, unit_cost, unit, description, active FROM providers_services WHERE active = TRUE ORDER BY id")
                .query(Service.class).list().stream().collect(Collectors.groupingBy(Service::providerId));
        String needle = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        String zoneNeedle = zone == null ? "" : zone.trim().toLowerCase(Locale.ROOT);
        List<ProviderCard> out = new ArrayList<>();
        for (Profile p : profiles) {
            UserRef u = users.get(p.userId());
            if (u == null) continue;
            if (onlyAvailable && (!u.active() || !"provider".equals(u.role()) || !p.available())) continue;
            if (category != null && !category.isBlank() && !category.equals(p.category())) continue;
            String userZone = u.zone() == null ? "" : u.zone().toLowerCase(Locale.ROOT);
            if (!zoneNeedle.isEmpty() && !userZone.contains(zoneNeedle)) continue;
            if (!needle.isEmpty() && !u.displayName().toLowerCase(Locale.ROOT).contains(needle)
                    && (p.description() == null || !p.description().toLowerCase(Locale.ROOT).contains(needle))) continue;
            var rating = ratings.getOrDefault(p.userId(), Reputation.Rating.NONE);
            out.add(new ProviderCard(u.id(), u.displayName(), u.phone(), u.zone(), p.category(), p.description(), p.available(), p.verified(),
                    rating.average(), rating.count(), services.getOrDefault(p.userId(), List.of())));
        }
        return out;
    }
}
