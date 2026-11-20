package com.menewa.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.menewa.identity.IdentityEvents;
import com.menewa.identity.IdentityUser;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Journal d'audit des actions critiques et décisions administratives (append-only). */
@Component
public class Audit {
    private final JdbcClient jdbc;
    private final ObjectMapper json;

    Audit(JdbcClient jdbc, ObjectMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    public void record(IdentityUser actor, String action, String entity, Object entityId, Map<String, ?> details) {
        record(actor == null ? null : actor.getId(), actor == null ? null : actor.getRole(), action, entity, entityId, details);
    }

    public void record(IdentityUser actor, String action, String entity, Object entityId) {
        record(actor, action, entity, entityId, null);
    }

    public void record(Long actorId, String actorRole, String action, String entity, Object entityId, Map<String, ?> details) {
        String serialized = null;
        if (details != null && !details.isEmpty()) {
            try { serialized = json.writeValueAsString(details); } catch (Exception ex) { serialized = String.valueOf(details); }
            if (serialized.length() > 4000) serialized = serialized.substring(0, 4000);
        }
        jdbc.sql("""
                INSERT INTO platform_audit_logs (actor_id, actor_role, action, entity, entity_id, details, ip)
                VALUES (:actorId, :actorRole, :action, :entity, :entityId, :details, :ip)""")
                .param("actorId", actorId).param("actorRole", actorRole).param("action", action)
                .param("entity", entity).param("entityId", entityId == null ? null : String.valueOf(entityId))
                .param("details", serialized).param("ip", clientIp())
                .update();
    }

    @EventListener
    void onRegistered(IdentityEvents.UserRegistered e) {
        record(e.userId(), e.role(), "user.register", "user", e.userId(), null);
    }

    @EventListener
    void onLogin(IdentityEvents.UserLoggedIn e) {
        record(e.userId(), e.role(), "user.login", "user", e.userId(), null);
    }

    @EventListener
    void onIdentityAudited(IdentityEvents.IdentityAudited e) {
        record(e.actorId(), e.actorRole(), e.action(), e.entity(), e.entityId(), e.details());
    }

    private static String clientIp() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) return null;
        HttpServletRequest request = attrs.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
