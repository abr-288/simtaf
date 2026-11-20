package com.menewa.rewards;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/** Avis (missions et services communautaires) et notes moyennes des prestataires. */
@Component
public class Reputation {
    public record Rating(double average, long count) {
        public static final Rating NONE = new Rating(0, 0);
    }

    public record Review(Long id, String kind, Long refId, Long fromUserId, Long toUserId, int rating, String comment, OffsetDateTime createdAt) {}

    private final JdbcClient jdbc;
    private final Rewards rewards;

    Reputation(JdbcClient jdbc, Rewards rewards) {
        this.jdbc = jdbc;
        this.rewards = rewards;
    }

    /** @return false si l'auteur a déjà noté cet objet. */
    public boolean add(String kind, Long refId, Long fromUserId, Long toUserId, int rating, String comment) {
        if (hasReviewed(kind, refId, fromUserId)) return false;
        try {
            jdbc.sql("INSERT INTO rewards_reviews (kind, ref_id, from_user_id, to_user_id, rating, comment) VALUES (:k, :r, :f, :t, :n, :c)")
                    .param("k", kind).param("r", refId).param("f", fromUserId).param("t", toUserId).param("n", rating)
                    .param("c", comment == null || comment.isBlank() ? null : comment.trim()).update();
        } catch (DuplicateKeyException ex) {
            return false;
        }
        rewards.refreshBadges(toUserId);
        return true;
    }

    public boolean hasReviewed(String kind, Long refId, Long fromUserId) {
        return jdbc.sql("SELECT COUNT(*) FROM rewards_reviews WHERE kind = :k AND ref_id = :r AND from_user_id = :f")
                .param("k", kind).param("r", refId).param("f", fromUserId).query(Long.class).single() > 0;
    }

    public boolean hasReviewedAny(String kind, Collection<Long> refIds, Long fromUserId) {
        if (refIds.isEmpty()) return false;
        return jdbc.sql("SELECT COUNT(*) FROM rewards_reviews WHERE kind = :k AND ref_id IN (:r) AND from_user_id = :f")
                .param("k", kind).param("r", refIds).param("f", fromUserId).query(Long.class).single() > 0;
    }

    public Map<Long, Rating> ratings(Collection<Long> userIds) {
        Map<Long, Rating> out = new HashMap<>();
        if (userIds.isEmpty()) return out;
        jdbc.sql("SELECT to_user_id, AVG(CAST(rating AS DOUBLE PRECISION)) AS a, COUNT(*) AS n FROM rewards_reviews WHERE to_user_id IN (:ids) GROUP BY to_user_id")
                .param("ids", userIds)
                .query((rs, i) -> Map.entry(rs.getLong(1), new Rating(rs.getDouble(2), rs.getLong(3))))
                .list().forEach(e -> out.put(e.getKey(), e.getValue()));
        return out;
    }

    public List<Review> receivedBy(Long userId, int limit) {
        return jdbc.sql("SELECT id, kind, ref_id, from_user_id, to_user_id, rating, comment, created_at FROM rewards_reviews WHERE to_user_id = :u ORDER BY id DESC LIMIT :n")
                .param("u", userId).param("n", limit).query(Review.class).list();
    }

    public long countReceivedBy(Long userId) {
        return jdbc.sql("SELECT COUNT(*) FROM rewards_reviews WHERE to_user_id = :u").param("u", userId).query(Long.class).single();
    }
}
