package com.menewa.platform;

import com.menewa.identity.IdentityDirectory;
import com.menewa.identity.UserRef;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/** Notifications multicanales selon les préférences de l'utilisateur : in-app immédiate, SMS en file d'envoi ({@link SmsOutbox}) ; e-mail à brancher. */
@Component
public class Notifications {
    private static final Set<String> SMS_TYPES = Set.of(
            "request_new", "request_decision", "info_requested", "mission_proposed", "due_soon", "due_late", "proof_review", "request_closed");

    private final JdbcClient jdbc;
    private final IdentityDirectory directory;
    private final SmsOutbox sms;

    Notifications(JdbcClient jdbc, IdentityDirectory directory, SmsOutbox sms) {
        this.jdbc = jdbc;
        this.directory = directory;
        this.sms = sms;
    }

    public void notify(Long userId, String type, String title, String body, String link) {
        if (userId == null) return;
        directory.find(userId).ifPresent(user -> deliver(user, type, title, body, link));
    }

    public void notifyAdmins(String permission, String type, String title, String body, String link) {
        for (UserRef admin : directory.administratorsWith(permission)) deliver(admin, type, title, body, link);
    }

    private void deliver(UserRef user, String type, String title, String body, String link) {
        if (user.notifInapp()) {
            jdbc.sql("INSERT INTO platform_notifications (user_id, type, title, body, link) VALUES (:u, :t, :ti, :b, :l)")
                    .param("u", user.id()).param("t", type).param("ti", Problems.trim(title, 200))
                    .param("b", body == null ? null : Problems.trim(body, 1000)).param("l", link).update();
        }
        // SMS mis en file (même transaction) puis envoyé en arrière-plan : l'action n'attend jamais la passerelle.
        if (user.notifSms() && SMS_TYPES.contains(type)) sms.enqueue(user.id(), user.phone(), "Menewa Naya : " + title, type);
    }

    record NotificationView(Long id, String type, String title, String body, String link, OffsetDateTime readAt, OffsetDateTime createdAt) {}

    List<NotificationView> list(Long userId, int limit) {
        return jdbc.sql("SELECT id, type, title, body, link, read_at, created_at FROM platform_notifications WHERE user_id = :u ORDER BY id DESC LIMIT :n")
                .param("u", userId).param("n", Math.max(1, Math.min(limit, 200))).query(NotificationView.class).list();
    }

    long unread(Long userId) {
        return jdbc.sql("SELECT COUNT(*) FROM platform_notifications WHERE user_id = :u AND read_at IS NULL")
                .param("u", userId).query(Long.class).single();
    }

    void markAllRead(Long userId) {
        jdbc.sql("UPDATE platform_notifications SET read_at = :now WHERE user_id = :u AND read_at IS NULL")
                .param("now", OffsetDateTime.now()).param("u", userId).update();
    }
}
