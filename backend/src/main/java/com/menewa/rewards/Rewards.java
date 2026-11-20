package com.menewa.rewards;

import com.menewa.identity.IdentityDirectory;
import com.menewa.identity.IdentityEvents;
import com.menewa.platform.Notifications;
import com.menewa.platform.Problems;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Attribution automatique de Mena selon les règles configurées. Contrôles anti-fraude : règle active,
 * compte actif, anti-doublon (ref_key unique), plafond quotidien par événement, pas d'auto-attribution.
 */
@Component
public class Rewards {
    /** Événements pouvant déclencher une attribution ; `base` = règle d'installation non supprimable. */
    public record TokenEvent(String label, String who, boolean base) {}

    public static final Map<String, TokenEvent> EVENTS = new LinkedHashMap<>();
    static {
        EVENTS.put("signup", new TokenEvent("Inscription sur la plateforme", "Nouveau membre", true));
        EVENTS.put("mission_validated", new TokenEvent("Mission validée par l’administration", "Prestataire", true));
        EVENTS.put("service_confirmed", new TokenEvent("Service communautaire confirmé par le client", "Prestataire", true));
        EVENTS.put("request_closed", new TokenEvent("Don clôturé", "Donateur", true));
        EVENTS.put("review_given", new TokenEvent("Avis publié après une mission ou un service", "Donateur ou client", true));
        EVENTS.put("proof_validated", new TokenEvent("Preuve de remise validée par l’administration", "Auteur de la preuve (prestataire)", false));
        EVENTS.put("contribution_confirmed", new TokenEvent("Contribution à une campagne confirmée", "Donateur", false));
        EVENTS.put("listing_published", new TokenEvent("Publication partenaire publiée après modération", "Partenaire", false));
    }

    /** Réalisations comptées pour les badges, indépendamment de l'attribution de Mena. */
    private static final Map<String, String> ACHIEVEMENTS = Map.of("request_closed", "gift_closed", "mission_validated", "mission_validated");

    record Badge(String label, String emoji, String desc) {}
    static final Map<String, Badge> BADGES = new LinkedHashMap<>();
    static {
        BADGES.put("first_gift", new Badge("Premier don", "💚", "Un premier don clôturé"));
        BADGES.put("generous", new Badge("Généreux", "🌟", "5 dons clôturés"));
        BADGES.put("patron", new Badge("Mécène", "🏆", "20 dons clôturés"));
        BADGES.put("first_mission", new Badge("Première mission", "🤝", "Une mission validée"));
        BADGES.put("elite_provider", new Badge("Prestataire d'élite", "🥇", "10 missions validées"));
        BADGES.put("five_stars", new Badge("5 étoiles", "⭐", "Note moyenne ≥ 4,5 avec 3 avis"));
        BADGES.put("ambassador", new Badge("Ambassadeur", "🌍", "1 000 Mena gagnés"));
    }

    record Rule(Long id, String eventKey, String label, int amount, int dailyCap, boolean active) {}
    record Account(int balance, int earned) {}

    private final JdbcClient jdbc;
    private final IdentityDirectory directory;
    private final Notifications notifications;

    Rewards(JdbcClient jdbc, IdentityDirectory directory, Notifications notifications) {
        this.jdbc = jdbc;
        this.directory = directory;
        this.notifications = notifications;
    }

    @EventListener
    void onRegistered(IdentityEvents.UserRegistered e) {
        award(e.userId(), "signup", "user", e.userId(), null);
    }

    /** Enregistre la réalisation (badges) puis attribue les Mena de la règle si les contrôles passent. */
    @Transactional
    public Integer award(Long userId, String eventKey, String refType, Long refId, Long actorId) {
        if (userId == null) return null;
        String achievement = ACHIEVEMENTS.get(eventKey);
        if (achievement != null && refType != null) recordAchievement(userId, achievement, refType + ":" + refId);
        Integer granted = grant(userId, eventKey, refType, refId, actorId);
        refreshBadges(userId);
        return granted;
    }

    private Integer grant(Long userId, String eventKey, String refType, Long refId, Long actorId) {
        Rule rule = jdbc.sql("SELECT id, event_key, label, amount, daily_cap, active FROM rewards_rules WHERE event_key = :k")
                .param("k", eventKey).query(Rule.class).optional().orElse(null);
        if (rule == null || !rule.active() || rule.amount() <= 0) return null;
        if (actorId != null && actorId.equals(userId)) return null;
        var user = directory.find(userId).orElse(null);
        if (user == null || !user.active()) return null;
        String refKey = refType == null ? null : eventKey + ":" + refType + ":" + refId + ":" + userId;
        if (refKey != null && jdbc.sql("SELECT COUNT(*) FROM rewards_transactions WHERE ref_key = :k").param("k", refKey).query(Long.class).single() > 0)
            return null;
        if (rule.dailyCap() > 0) {
            OffsetDateTime startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
            int today = jdbc.sql("""
                    SELECT COALESCE(SUM(amount), 0) FROM rewards_transactions
                    WHERE user_id = :u AND event_key = :k AND status = 'approved' AND created_at >= :since""")
                    .param("u", userId).param("k", eventKey).param("since", startOfDay).query(Integer.class).single();
            if (today + rule.amount() > rule.dailyCap()) return null;
        }
        try {
            insertTransaction(userId, rule.amount(), "earn", eventKey, rule.label(), refType, refId, refKey, "approved", actorId);
        } catch (DuplicateKeyException ex) {
            return null;
        }
        applyDelta(userId, rule.amount(), true);
        notifications.notify(userId, "tokens", "+" + rule.amount() + " Mena : " + rule.label(), null, "/espace/tokens");
        return rule.amount();
    }

    /** Dépense de Mena (soutien symbolique à une campagne). */
    @Transactional
    public void spend(Long userId, int amount, String reason, String refType, Long refId) {
        if (amount < 1) throw Problems.bad("Montant invalide.");
        if (account(userId).balance() < amount) throw Problems.bad("Solde de Mena insuffisant.");
        insertTransaction(userId, -amount, "spend", null, reason, refType, refId, null, "approved", userId);
        applyDelta(userId, -amount, false);
    }

    public int balanceOf(Long userId) { return account(userId).balance(); }

    Account account(Long userId) {
        ensureAccount(userId);
        return jdbc.sql("SELECT balance, earned FROM rewards_accounts WHERE user_id = :u").param("u", userId).query(Account.class).single();
    }

    void applyDelta(Long userId, int delta, boolean earned) {
        ensureAccount(userId);
        jdbc.sql("UPDATE rewards_accounts SET balance = balance + :d, earned = earned + :e WHERE user_id = :u")
                .param("d", delta).param("e", earned && delta > 0 ? delta : 0).param("u", userId).update();
    }

    long insertTransaction(Long userId, int amount, String type, String eventKey, String reason, String refType, Long refId,
            String refKey, String status, Long createdBy) {
        var keys = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbc.sql("""
                INSERT INTO rewards_transactions (user_id, amount, type, event_key, reason, ref_type, ref_id, ref_key, status, created_by)
                VALUES (:u, :a, :t, :e, :r, :rt, :ri, :rk, :s, :c)""")
                .param("u", userId).param("a", amount).param("t", type).param("e", eventKey)
                .param("r", reason == null ? null : Problems.trim(reason, 300)).param("rt", refType).param("ri", refId)
                .param("rk", refKey).param("s", status).param("c", createdBy)
                .update(keys, "id");
        return keys.getKey().longValue();
    }

    private void ensureAccount(Long userId) {
        long n = jdbc.sql("SELECT COUNT(*) FROM rewards_accounts WHERE user_id = :u").param("u", userId).query(Long.class).single();
        if (n == 0) {
            try { jdbc.sql("INSERT INTO rewards_accounts (user_id) VALUES (:u)").param("u", userId).update(); }
            catch (DuplicateKeyException ignored) { /* créé en parallèle */ }
        }
    }

    private void recordAchievement(Long userId, String kind, String ref) {
        String key = kind + ":" + ref + ":" + userId;
        if (jdbc.sql("SELECT COUNT(*) FROM rewards_achievements WHERE ref_key = :k").param("k", key).query(Long.class).single() > 0) return;
        jdbc.sql("INSERT INTO rewards_achievements (ref_key, user_id, kind) VALUES (:k, :u, :kind)")
                .param("k", key).param("u", userId).param("kind", kind).update();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void refreshBadges(Long userId) {
        long gifts = countAchievements(userId, "gift_closed");
        long missions = countAchievements(userId, "mission_validated");
        long reviews = jdbc.sql("SELECT COUNT(*) FROM rewards_reviews WHERE to_user_id = :u").param("u", userId).query(Long.class).single();
        double avg = reviews == 0 ? 0 : jdbc.sql("SELECT AVG(CAST(rating AS DOUBLE PRECISION)) FROM rewards_reviews WHERE to_user_id = :u")
                .param("u", userId).query(Double.class).single();
        int earned = account(userId).earned();
        List<String> codes = new ArrayList<>();
        if (gifts >= 1) codes.add("first_gift");
        if (gifts >= 5) codes.add("generous");
        if (gifts >= 20) codes.add("patron");
        if (missions >= 1) codes.add("first_mission");
        if (missions >= 10) codes.add("elite_provider");
        if (reviews >= 3 && avg >= 4.5) codes.add("five_stars");
        if (earned >= 1000) codes.add("ambassador");
        for (String code : codes) {
            long has = jdbc.sql("SELECT COUNT(*) FROM rewards_badges WHERE user_id = :u AND code = :c").param("u", userId).param("c", code).query(Long.class).single();
            if (has > 0) continue;
            jdbc.sql("INSERT INTO rewards_badges (user_id, code) VALUES (:u, :c)").param("u", userId).param("c", code).update();
            Badge b = BADGES.get(code);
            notifications.notify(userId, "badge", "Nouveau badge : " + b.label(), b.desc(), "/espace/tokens");
        }
    }

    private long countAchievements(Long userId, String kind) {
        return jdbc.sql("SELECT COUNT(*) FROM rewards_achievements WHERE user_id = :u AND kind = :k").param("u", userId).param("k", kind).query(Long.class).single();
    }
}
