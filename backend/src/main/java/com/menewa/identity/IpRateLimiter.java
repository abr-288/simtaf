package com.menewa.identity;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Limitation par adresse IP (fenêtre glissante, en mémoire) des demandes de code et des codes erronés.
 * Complète la limite par numéro : empêche une même machine de viser beaucoup de numéros différents.
 * En mémoire car l'API tourne en une seule instance ; les compteurs repartent de zéro à chaque redémarrage.
 * Les entrées périmées sont purgées par {@link IdentityMaintenance}.
 */
@Component
class IpRateLimiter {
    private final Map<String, Deque<Instant>> hits = new ConcurrentHashMap<>();

    /** Adresse du client : premier maillon de X-Forwarded-For (posé par Caddy, puis relayé par les apps web), sinon l'adresse TCP. */
    static String currentClientIp() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) return null;
        HttpServletRequest request = attrs.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }

    /** Nombre d'événements de ce type pour cette IP dans la fenêtre. */
    int count(String kind, String ip, Duration window) {
        if (ip == null) return 0;
        Deque<Instant> deque = hits.get(kind + "|" + ip);
        if (deque == null) return 0;
        synchronized (deque) {
            prune(deque, Instant.now().minus(window));
            return deque.size();
        }
    }

    void record(String kind, String ip) {
        if (ip == null) return;
        Deque<Instant> deque = hits.computeIfAbsent(kind + "|" + ip, k -> new ArrayDeque<>());
        synchronized (deque) { deque.addLast(Instant.now()); }
    }

    /** Retire les événements plus anciens que la fenêtre et les IP devenues inactives. */
    void purge(Duration window) {
        Instant limit = Instant.now().minus(window);
        hits.entrySet().removeIf(entry -> {
            synchronized (entry.getValue()) {
                prune(entry.getValue(), limit);
                return entry.getValue().isEmpty();
            }
        });
    }

    int trackedKeys() { return hits.size(); }

    private static void prune(Deque<Instant> deque, Instant limit) {
        while (!deque.isEmpty() && deque.peekFirst().isBefore(limit)) deque.removeFirst();
    }
}
