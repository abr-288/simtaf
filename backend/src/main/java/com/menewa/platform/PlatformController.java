package com.menewa.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.menewa.identity.IdentityAccess;
import com.menewa.identity.IdentityDirectory;
import com.menewa.identity.IdentityUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
class PlatformController {
    private final IdentityAccess access;
    private final Notifications notifications;
    private final Settings settings;
    private final Audit audit;
    private final JdbcClient jdbc;
    private final IdentityDirectory directory;

    PlatformController(IdentityAccess access, Notifications notifications, Settings settings, Audit audit, JdbcClient jdbc,
            IdentityDirectory directory) {
        this.directory = directory;
        this.access = access;
        this.notifications = notifications;
        this.settings = settings;
        this.audit = audit;
        this.jdbc = jdbc;
    }

    /* ───────── Notifications ───────── */
    @GetMapping("/notifications")
    List<Notifications.NotificationView> notifications(@RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam(defaultValue = "100") int limit) {
        return notifications.list(access.requireUser(auth).getId(), limit);
    }

    @GetMapping("/notifications/unread-count")
    Map<String, Long> unread(@RequestHeader(value = "Authorization", required = false) String auth) {
        return Map.of("count", notifications.unread(access.requireUser(auth).getId()));
    }

    @PostMapping("/notifications/read-all")
    @Transactional
    Map<String, Boolean> readAll(@RequestHeader(value = "Authorization", required = false) String auth) {
        notifications.markAllRead(access.requireUser(auth).getId());
        return Map.of("ok", true);
    }

    /* ───────── Paramètres ───────── */
    @GetMapping("/settings/public")
    Map<String, JsonNode> publicSettings() { return settings.publicValues(); }

    @GetMapping("/admin/settings")
    List<Settings.SettingView> allSettings(@RequestHeader(value = "Authorization", required = false) String auth) {
        access.requirePermission(auth, "settings.manage");
        return settings.all();
    }

    record SaveSettingRequest(@NotNull JsonNode value) {}

    @PutMapping("/admin/settings/{key}")
    @Transactional
    Map<String, String> saveSetting(@RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable String key, @Valid @RequestBody SaveSettingRequest request) {
        IdentityUser u = access.requirePermission(auth, "settings.manage");
        if (!settings.exists(key)) throw Problems.bad("Paramètre inconnu.");
        JsonNode value = request.value();
        if (key.startsWith("feature.") && !value.isBoolean()) throw Problems.bad("Un interrupteur de fonctionnalité doit valoir true ou false.");
        if (key.startsWith("feature.") && value.asBoolean() && !"admin".equals(u.getRole()))
            throw Problems.bad("Seul l'administrateur principal peut activer une fonctionnalité soumise à validation juridique.");
        JsonNode before = settings.get(key);
        settings.write(key, value, u.getId());
        audit.record(u, "settings.update", "setting", key, Map.of("from", before, "to", value));
        return Map.of("ok", "Paramètre enregistré.");
    }

    /* ───────── Audit ───────── */
    record AuditRow(Long id, Long actorId, String actorRole, String action, String entity, String entityId,
            String details, String ip, OffsetDateTime createdAt) {}

    record AuditView(Long id, Long actorId, String actorRole, String actorName, String action, String entity, String entityId,
            String details, String ip, OffsetDateTime createdAt) {}

    @GetMapping("/admin/audit")
    List<AuditView> audit(@RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam(required = false) String q, @RequestParam(required = false) Long actor) {
        access.requirePermission(auth, "audit.view");
        StringBuilder sql = new StringBuilder("SELECT id, actor_id, actor_role, action, entity, entity_id, details, ip, created_at FROM platform_audit_logs WHERE 1 = 1");
        var params = new java.util.HashMap<String, Object>();
        if (q != null && !q.isBlank()) { sql.append(" AND LOWER(action) LIKE :q"); params.put("q", "%" + q.trim().toLowerCase() + "%"); }
        if (actor != null) { sql.append(" AND actor_id = :actor"); params.put("actor", actor); }
        sql.append(" ORDER BY id DESC LIMIT 300");
        List<AuditRow> rows = jdbc.sql(sql.toString()).params(params).query(AuditRow.class).list();
        var names = directory.findAll(rows.stream().map(AuditRow::actorId).toList());
        return rows.stream().map(a -> new AuditView(a.id(), a.actorId(), a.actorRole(),
                a.actorId() == null ? null : (names.containsKey(a.actorId()) ? names.get(a.actorId()).displayName() : null),
                a.action(), a.entity(), a.entityId(), a.details(), a.ip(), a.createdAt())).toList();
    }

    /* ───────── Contact ───────── */
    record ContactRequest(String name, String email, String phone, String subject, String message) {}

    @PostMapping("/contact")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    Map<String, String> contact(@RequestBody ContactRequest r) {
        String name = Problems.trim(r.name(), 100), subject = Problems.trim(r.subject(), 150), message = Problems.trim(r.message(), 3000);
        if (name.length() < 2 || message.length() < 10 || subject.length() < 3)
            throw Problems.bad("Merci de renseigner nom, sujet et message (10 caractères min.).");
        jdbc.sql("INSERT INTO platform_contact_messages (name, email, phone, subject, message) VALUES (:n, :e, :p, :s, :m)")
                .param("n", name).param("e", Problems.trimToNull(r.email(), 120)).param("p", Problems.trimToNull(r.phone(), 30))
                .param("s", subject).param("m", message).update();
        notifications.notifyAdmins("settings.manage", "contact", "Nouveau message de contact : " + subject,
                message.length() > 120 ? message.substring(0, 120) : message, "/admin/parametres");
        return Map.of("ok", "Merci ! Votre message a bien été envoyé. Nous vous répondrons rapidement.");
    }

    record ContactView(Long id, String name, String email, String phone, String subject, String message, boolean handled, OffsetDateTime createdAt) {}

    @GetMapping("/admin/contact-messages")
    List<ContactView> contactMessages(@RequestHeader(value = "Authorization", required = false) String auth) {
        access.requirePermission(auth, "settings.manage");
        return jdbc.sql("SELECT id, name, email, phone, subject, message, handled, created_at FROM platform_contact_messages ORDER BY id DESC LIMIT 20")
                .query(ContactView.class).list();
    }
}
