package com.menewa.rewards;

import com.menewa.identity.IdentityAccess;
import com.menewa.identity.IdentityDirectory;
import com.menewa.identity.IdentityUser;
import com.menewa.identity.UserRef;
import com.menewa.platform.Audit;
import com.menewa.platform.Notifications;
import com.menewa.platform.Problems;
import com.menewa.platform.Settings;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
class RewardsController {
    record Level(String key, String label, int min) {}
    record TransactionView(Long id, Long userId, int amount, String type, String eventKey, String reason, String status,
            Long createdBy, OffsetDateTime createdAt) {}
    record BadgeView(String code, String label, String emoji, String desc, boolean earned) {}
    record MyRewards(int balance, int earned, Level level, Level next, List<Level> levels, List<TransactionView> transactions,
            List<BadgeView> badges, boolean transferEnabled, boolean conversionEnabled) {}

    private final Rewards rewards;
    private final IdentityAccess access;
    private final IdentityDirectory directory;
    private final JdbcClient jdbc;
    private final Settings settings;
    private final Audit audit;
    private final Notifications notifications;

    RewardsController(Rewards rewards, IdentityAccess access, IdentityDirectory directory, JdbcClient jdbc, Settings settings,
            Audit audit, Notifications notifications) {
        this.rewards = rewards;
        this.access = access;
        this.directory = directory;
        this.jdbc = jdbc;
        this.settings = settings;
        this.audit = audit;
        this.notifications = notifications;
    }

    List<Level> levels() {
        var t = settings.intMap("tokens.level_thresholds");
        return List.of(new Level("bronze", "Bronze", 0), new Level("silver", "Argent", t.getOrDefault("silver", 200)),
                new Level("gold", "Or", t.getOrDefault("gold", 1000)), new Level("platinum", "Platine", t.getOrDefault("platinum", 3000)));
    }

    /* ───────── Espace membre ───────── */
    @GetMapping("/rewards/me")
    MyRewards me(@RequestHeader(value = "Authorization", required = false) String auth) {
        IdentityUser u = access.requireUser(auth);
        var account = rewards.account(u.getId());
        List<Level> levels = levels();
        Level cur = levels.getFirst(), next = null;
        for (int i = 0; i < levels.size(); i++) {
            if (account.earned() >= levels.get(i).min()) { cur = levels.get(i); next = i + 1 < levels.size() ? levels.get(i + 1) : null; }
        }
        var txs = jdbc.sql("SELECT id, user_id, amount, type, event_key, reason, status, created_by, created_at FROM rewards_transactions WHERE user_id = :u ORDER BY id DESC LIMIT 50")
                .param("u", u.getId()).query(TransactionView.class).list();
        var owned = jdbc.sql("SELECT code FROM rewards_badges WHERE user_id = :u").param("u", u.getId()).query(String.class).list();
        List<BadgeView> badges = new ArrayList<>();
        Rewards.BADGES.forEach((code, b) -> badges.add(new BadgeView(code, b.label(), b.emoji(), b.desc(), owned.contains(code))));
        return new MyRewards(account.balance(), account.earned(), cur, next, levels, txs, badges,
                settings.boolValue("feature.token_transfer"), settings.boolValue("feature.token_conversion"));
    }

    record TransferRequest(String phone, int amount) {}

    @PostMapping("/rewards/transfer")
    @Transactional
    Map<String, String> transfer(@RequestHeader(value = "Authorization", required = false) String auth, @RequestBody TransferRequest r) {
        IdentityUser u = access.requireUser(auth);
        if (!settings.boolValue("feature.token_transfer")) throw Problems.bad("Les transferts de Mena sont désactivés (validation juridique en attente).");
        UserRef to = directory.findActiveByPhone(r.phone() == null ? "" : r.phone()).orElse(null);
        if (to == null || to.id().equals(u.getId())) throw Problems.bad("Destinataire invalide.");
        if (r.amount() < 1 || r.amount() > 1000) throw Problems.bad("Montant invalide (1 à 1000).");
        if (rewards.balanceOf(u.getId()) < r.amount()) throw Problems.bad("Solde insuffisant.");
        rewards.insertTransaction(u.getId(), -r.amount(), "transfer_out", null, "Transfert vers " + to.displayName(), null, null, null, "approved", u.getId());
        rewards.insertTransaction(to.id(), r.amount(), "transfer_in", null, "Transfert de " + u.displayName(), null, null, null, "approved", u.getId());
        rewards.applyDelta(u.getId(), -r.amount(), false);
        rewards.applyDelta(to.id(), r.amount(), false);
        audit.record(u, "tokens.transfer", "user", to.id(), Map.of("amount", r.amount()));
        notifications.notify(to.id(), "tokens", "+" + r.amount() + " Mena reçus", null, "/espace/tokens");
        return Map.of("ok", "Transfert effectué.");
    }

    record ConversionRequest(int amount) {}

    @PostMapping("/rewards/conversion")
    @Transactional
    Map<String, String> conversion(@RequestHeader(value = "Authorization", required = false) String auth, @RequestBody ConversionRequest r) {
        IdentityUser u = access.requireUser(auth);
        if (!settings.boolValue("feature.token_conversion")) throw Problems.bad("La conversion des Mena est désactivée (validation juridique et réglementaire en attente).");
        int rate = settings.intValue("tokens.conversion_rate_xof", 0);
        if (rate <= 0) throw Problems.bad("Le taux de conversion n'est pas défini.");
        if (r.amount() < 1 || rewards.balanceOf(u.getId()) < r.amount()) throw Problems.bad("Montant invalide ou solde insuffisant.");
        rewards.insertTransaction(u.getId(), -r.amount(), "conversion", null, "Demande de conversion (" + (r.amount() * rate) + " FCFA)", null, null, null, "pending", u.getId());
        rewards.applyDelta(u.getId(), -r.amount(), false);
        notifications.notifyAdmins("tokens.manage", "tokens", "Demande de conversion de Mena à valider", null, "/admin/tokens");
        return Map.of("ok", "Demande de conversion enregistrée, en attente de validation.");
    }

    /* ───────── Administration ───────── */
    record AdminTransaction(Long id, Long userId, String userName, int amount, String type, String reason, String status,
            Long createdBy, OffsetDateTime createdAt) {}
    record EventOption(String key, String label, String who, boolean base) {}
    record AdminOverview(List<Rewards.Rule> rules, List<AdminTransaction> transactions, long circulating, int manualAdjustMax,
            List<EventOption> events) {}

    @GetMapping("/admin/rewards")
    AdminOverview overview(@RequestHeader(value = "Authorization", required = false) String auth) {
        access.requirePermission(auth, "tokens.manage");
        var rules = jdbc.sql("SELECT id, event_key, label, amount, daily_cap, active FROM rewards_rules ORDER BY id").query(Rewards.Rule.class).list();
        var txs = jdbc.sql("SELECT id, user_id, amount, type, event_key, reason, status, created_by, created_at FROM rewards_transactions ORDER BY id DESC LIMIT 80")
                .query(TransactionView.class).list();
        var names = directory.findAll(txs.stream().map(TransactionView::userId).toList());
        var adminTxs = txs.stream().map(t -> new AdminTransaction(t.id(), t.userId(),
                names.containsKey(t.userId()) ? names.get(t.userId()).displayName() : "—", t.amount(), t.type(), t.reason(), t.status(),
                t.createdBy(), t.createdAt())).toList();
        long circulating = jdbc.sql("SELECT COALESCE(SUM(balance), 0) FROM rewards_accounts").query(Long.class).single();
        List<EventOption> events = new ArrayList<>();
        Rewards.EVENTS.forEach((k, v) -> events.add(new EventOption(k, v.label(), v.who(), v.base())));
        return new AdminOverview(rules, adminTxs, circulating, settings.intValue("tokens.manual_adjust_max", 500), events);
    }

    record AccountView(Long userId, int balance, int earned) {}

    @GetMapping("/admin/rewards/accounts")
    List<AccountView> accounts(@RequestHeader(value = "Authorization", required = false) String auth) {
        access.requireAdmin(auth);
        return jdbc.sql("SELECT user_id, balance, earned FROM rewards_accounts").query(AccountView.class).list();
    }

    record RuleRequest(String eventKey, String label, int amount, int dailyCap, boolean active) {}

    @PutMapping("/admin/rewards/rules/{id}")
    @Transactional
    Map<String, String> saveRule(@RequestHeader(value = "Authorization", required = false) String auth, @PathVariable long id, @RequestBody RuleRequest r) {
        IdentityUser u = access.requirePermission(auth, "tokens.manage");
        int amount = Math.max(r.amount(), 0);
        String label = Problems.trim(r.label(), 120);
        if (amount > 1000) throw Problems.bad("Montant par événement limité à 1000 Mena (anti-fraude).");
        if (label.length() < 3) throw Problems.bad("L’intitulé de la règle doit contenir au moins 3 caractères.");
        var before = jdbc.sql("SELECT id, event_key, label, amount, daily_cap, active FROM rewards_rules WHERE id = :id").param("id", id)
                .query(Rewards.Rule.class).optional().orElseThrow(() -> Problems.notFound("Règle introuvable."));
        int cap = Math.max(r.dailyCap(), 0);
        jdbc.sql("UPDATE rewards_rules SET label = :l, amount = :a, daily_cap = :c, active = :act WHERE id = :id")
                .param("l", label).param("a", amount).param("c", cap).param("act", r.active()).param("id", id).update();
        Map<String, Object> from = new LinkedHashMap<>(Map.of("label", before.label(), "amount", before.amount(), "dailyCap", before.dailyCap(), "active", before.active()));
        Map<String, Object> to = new LinkedHashMap<>(Map.of("label", label, "amount", amount, "dailyCap", cap, "active", r.active()));
        audit.record(u, "tokens.rule_update", "token_rule", id, Map.of("from", from, "to", to));
        return Map.of("ok", "Règle enregistrée.");
    }

    @PostMapping("/admin/rewards/rules")
    @Transactional
    Map<String, String> createRule(@RequestHeader(value = "Authorization", required = false) String auth, @RequestBody RuleRequest r) {
        IdentityUser u = access.requirePermission(auth, "tokens.manage");
        String label = Problems.trim(r.label(), 120);
        if (r.eventKey() == null || !Rewards.EVENTS.containsKey(r.eventKey())) throw Problems.bad("Choisissez un événement de la liste.");
        if (label.length() < 3) throw Problems.bad("L’intitulé de la règle doit contenir au moins 3 caractères.");
        if (r.amount() < 1 || r.amount() > 1000) throw Problems.bad("Le montant doit être compris entre 1 et 1000 Mena (anti-fraude).");
        if (jdbc.sql("SELECT COUNT(*) FROM rewards_rules WHERE event_key = :k").param("k", r.eventKey()).query(Long.class).single() > 0)
            throw Problems.bad("Une règle existe déjà pour cet événement : modifiez-la plutôt.");
        var keys = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbc.sql("INSERT INTO rewards_rules (event_key, label, amount, daily_cap, active) VALUES (:k, :l, :a, :c, :act)")
                .param("k", r.eventKey()).param("l", label).param("a", r.amount()).param("c", Math.max(r.dailyCap(), 0)).param("act", r.active())
                .update(keys, "id");
        audit.record(u, "tokens.rule_create", "token_rule", keys.getKey().longValue(), Map.of("eventKey", r.eventKey(), "amount", r.amount(), "dailyCap", Math.max(r.dailyCap(), 0)));
        return Map.of("ok", "Règle créée : elle s’appliquera aux prochains événements.");
    }

    @DeleteMapping("/admin/rewards/rules/{id}")
    @Transactional
    Map<String, String> deleteRule(@RequestHeader(value = "Authorization", required = false) String auth, @PathVariable long id) {
        IdentityUser u = access.requirePermission(auth, "tokens.manage");
        var rule = jdbc.sql("SELECT id, event_key, label, amount, daily_cap, active FROM rewards_rules WHERE id = :id").param("id", id)
                .query(Rewards.Rule.class).optional().orElseThrow(() -> Problems.notFound("Règle introuvable."));
        var ev = Rewards.EVENTS.get(rule.eventKey());
        if (ev != null && ev.base()) throw Problems.bad("Une règle de base ne peut pas être supprimée : désactivez-la.");
        jdbc.sql("DELETE FROM rewards_rules WHERE id = :id").param("id", id).update();
        audit.record(u, "tokens.rule_delete", "token_rule", id, Map.of("eventKey", rule.eventKey(), "amount", rule.amount()));
        return Map.of("ok", "Règle supprimée.");
    }

    record AdjustRequest(Long userId, int amount, String reason) {}

    @PostMapping("/admin/rewards/adjust")
    @Transactional
    Map<String, String> adjust(@RequestHeader(value = "Authorization", required = false) String auth, @RequestBody AdjustRequest r) {
        IdentityUser u = access.requirePermission(auth, "tokens.manage");
        String reason = Problems.trim(r.reason(), 300);
        if (r.amount() == 0 || reason.length() < 5) throw Problems.bad("Montant et motif (5 caractères min.) requis.");
        if (r.userId() == null || r.userId().equals(u.getId())) throw Problems.bad("Vous ne pouvez pas ajuster votre propre solde.");
        if (directory.find(r.userId()).isEmpty()) throw Problems.notFound("Utilisateur introuvable.");
        if (r.amount() < 0 && rewards.balanceOf(r.userId()) + r.amount() < 0) throw Problems.bad("Le solde ne peut pas devenir négatif.");
        int max = settings.intValue("tokens.manual_adjust_max", 500);
        boolean needsApproval = Math.abs(r.amount()) > max && !"admin".equals(u.getRole());
        rewards.insertTransaction(r.userId(), r.amount(), "adjust", null, reason, null, null, null, needsApproval ? "pending" : "approved", u.getId());
        if (!needsApproval) rewards.applyDelta(r.userId(), r.amount(), true);
        audit.record(u, "tokens.adjust", "user", r.userId(), Map.of("amount", r.amount(), "reason", reason, "pending", needsApproval));
        if (!needsApproval) {
            notifications.notify(r.userId(), "tokens", (r.amount() > 0 ? "+" : "") + r.amount() + " Mena : " + reason, null, "/espace/tokens");
            rewards.refreshBadges(r.userId());
        }
        return Map.of("ok", needsApproval
                ? "Ajustement au-dessus du plafond : en attente de validation de l'administrateur principal."
                : "Ajustement appliqué.");
    }

    record DecisionRequest(boolean approve) {}

    /** Validation réservée à l'administrateur principal, jamais par l'auteur de l'opération. */
    @PostMapping("/admin/rewards/transactions/{id}/decision")
    @Transactional
    Map<String, String> decide(@RequestHeader(value = "Authorization", required = false) String auth, @PathVariable long id, @RequestBody DecisionRequest r) {
        IdentityUser u = access.requireUser(auth);
        if (!"admin".equals(u.getRole())) throw Problems.forbidden("Validation réservée à l'administrateur principal.");
        var t = jdbc.sql("SELECT id, user_id, amount, type, event_key, reason, status, created_by, created_at FROM rewards_transactions WHERE id = :id AND status = 'pending'")
                .param("id", id).query(TransactionView.class).optional().orElseThrow(() -> Problems.notFound("Opération introuvable ou déjà traitée."));
        if (u.getId().equals(t.createdBy())) throw Problems.forbidden("Validation réservée à un autre administrateur principal.");
        jdbc.sql("UPDATE rewards_transactions SET status = :s WHERE id = :id").param("s", r.approve() ? "approved" : "rejected").param("id", id).update();
        if ("adjust".equals(t.type()) && r.approve()) rewards.applyDelta(t.userId(), t.amount(), true);
        if ("conversion".equals(t.type()) && !r.approve()) rewards.applyDelta(t.userId(), -t.amount(), false);
        audit.record(u, r.approve() ? "tokens.approve" : "tokens.reject", "token_tx", id, Map.of("amount", t.amount()));
        notifications.notify(t.userId(), "tokens", "Opération Mena " + (r.approve() ? "validée" : "rejetée"), t.reason(), "/espace/tokens");
        return Map.of("ok", r.approve() ? "Opération validée." : "Opération rejetée.");
    }
}
