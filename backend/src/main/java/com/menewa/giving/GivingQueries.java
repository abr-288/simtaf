package com.menewa.giving;

import com.menewa.giving.GivingRows.BeneficiaryRow;
import com.menewa.giving.GivingRows.DisputeMessageRow;
import com.menewa.giving.GivingRows.DisputeRow;
import com.menewa.giving.GivingRows.HistoryRow;
import com.menewa.giving.GivingRows.InstallmentRow;
import com.menewa.giving.GivingRows.ItemRow;
import com.menewa.giving.GivingRows.MissionRow;
import com.menewa.giving.GivingRows.PaymentRow;
import com.menewa.giving.GivingRows.ProofRow;
import com.menewa.giving.GivingRows.RequestRow;
import com.menewa.identity.IdentityDirectory;
import com.menewa.identity.IdentityPermissions;
import com.menewa.identity.IdentityUser;
import com.menewa.identity.UserRef;
import com.menewa.platform.Audit;
import com.menewa.platform.Settings;
import com.menewa.providers.ProviderDirectory;
import com.menewa.rewards.Reputation;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Modèles de lecture du module (aucune écriture, hors traces d'accès aux données sensibles). */
@Service
@Transactional(readOnly = true)
class GivingQueries {
    private final GivingDb db;
    private final IdentityDirectory directory;
    private final Settings settings;
    private final ProviderDirectory providers;
    private final Reputation reputation;
    private final Audit audit;
    private final Requests requests;

    GivingQueries(GivingDb db, IdentityDirectory directory, Settings settings, ProviderDirectory providers, Reputation reputation,
            Audit audit, Requests requests) {
        this.db = db;
        this.directory = directory;
        this.settings = settings;
        this.providers = providers;
        this.reputation = reputation;
        this.audit = audit;
        this.requests = requests;
    }

    private List<RequestRow> requestsWhere(String where, Map<String, ?> params) {
        return db.jdbc.sql("SELECT " + GivingRows.REQUEST_COLS + " FROM giving_requests " + where).params(params).query(RequestRow.class).list();
    }

    private List<MissionRow> missionsWhere(String where, Map<String, ?> params) {
        return db.jdbc.sql("SELECT " + GivingRows.MISSION_COLS + " FROM giving_missions " + where).params(params).query(MissionRow.class).list();
    }

    private List<PaymentRow> paymentsWhere(String where, Map<String, ?> params) {
        return db.jdbc.sql("SELECT " + GivingRows.PAYMENT_COLS + " FROM giving_payments " + where).params(params).query(PaymentRow.class).list();
    }

    private List<ProofRow> proofsWhere(String where, Map<String, ?> params) {
        return db.jdbc.sql("SELECT " + GivingRows.PROOF_COLS + " FROM giving_proofs " + where).params(params).query(ProofRow.class).list();
    }

    private String name(Map<Long, UserRef> names, Long id) {
        if (id == null) return null;
        UserRef u = names.get(id);
        return u == null ? null : u.displayName();
    }

    /* ───────── Donateur ───────── */
    record DonorDashboard(long pending, long ongoing, long done, long proofsAvailable, List<RequestRow> recent) {}

    DonorDashboard donorDashboard(IdentityUser u) {
        var rs = requestsWhere("WHERE user_id = :u ORDER BY id DESC", Map.of("u", u.getId()));
        long proofs = db.jdbc.sql("""
                SELECT COUNT(*) FROM giving_proofs p JOIN giving_requests r ON r.id = p.request_id
                WHERE r.user_id = :u AND p.status = 'validated' AND p.visible_to_donor = TRUE""").param("u", u.getId()).query(Long.class).single();
        return new DonorDashboard(count(rs, "submitted", "info_requested"), count(rs, "accepted", "in_progress"), count(rs, "completed", "closed"),
                proofs, rs.stream().limit(5).toList());
    }

    private static long count(List<RequestRow> rs, String... statuses) {
        Set<String> s = Set.of(statuses);
        return rs.stream().filter(r -> s.contains(r.status())).count();
    }

    List<RequestRow> myRequests(IdentityUser u, String status) {
        if (status == null || status.isBlank()) return requestsWhere("WHERE user_id = :u ORDER BY id DESC", Map.of("u", u.getId()));
        return requestsWhere("WHERE user_id = :u AND status = :s ORDER BY id DESC", Map.of("u", u.getId(), "s", status));
    }

    record MissionSummary(Long id, String title, String status, OffsetDateTime dueDate, String providerName, OffsetDateTime validatedAt) {}
    record DonorRequestDetail(RequestRow request, List<ItemRow> items, List<HistoryRow> history, List<InstallmentRow> installments,
            List<PaymentRow> payments, List<ProofRow> proofs, List<MissionSummary> missions, List<DisputeRow> disputes, boolean canReview) {}

    DonorRequestDetail donorRequest(IdentityUser u, long id) {
        RequestRow r = db.request(id);
        if (!r.userId().equals(u.getId())) throw com.menewa.platform.Problems.notFound("Demande introuvable.");
        var ms = missionsWhere("WHERE request_id = :r ORDER BY id", Map.of("r", id));
        var names = directory.findAll(ms.stream().map(MissionRow::providerId).toList());
        var missions = ms.stream().map(m -> new MissionSummary(m.id(), m.title(), m.status(), m.dueDate(),
                m.providerId() == null ? "Équipe Menewa" : name(names, m.providerId()), m.validatedAt())).toList();
        boolean reviewed = reputation.hasReviewedAny("mission", ms.stream().map(MissionRow::id).toList(), u.getId());
        boolean canReview = Set.of("completed", "closed").contains(r.status()) && !reviewed && ms.stream().anyMatch(m -> m.validatedAt() != null);
        return new DonorRequestDetail(r, items(id), history(id), installments(id),
                paymentsWhere("WHERE request_id = :r AND direction = 'in' AND user_id = :u ORDER BY id", Map.of("r", id, "u", u.getId())),
                proofsWhere("WHERE request_id = :r AND status = 'validated' AND visible_to_donor = TRUE ORDER BY id DESC", Map.of("r", id)),
                missions,
                db.jdbc.sql("SELECT " + GivingRows.DISPUTE_COLS + " FROM giving_disputes WHERE request_id = :r AND opened_by = :u ORDER BY id")
                        .param("r", id).param("u", u.getId()).query(DisputeRow.class).list(),
                canReview);
    }

    List<ItemRow> items(long requestId) {
        return db.jdbc.sql("SELECT id, request_id, catalog_item_id, label, quantity, unit_price, cost_estimate, target FROM giving_request_items WHERE request_id = :r ORDER BY id")
                .param("r", requestId).query(ItemRow.class).list();
    }

    List<HistoryRow> history(long requestId) {
        return db.jdbc.sql("SELECT id, request_id, status, note, actor_id, created_at FROM giving_request_history WHERE request_id = :r ORDER BY id DESC")
                .param("r", requestId).query(HistoryRow.class).list();
    }

    List<InstallmentRow> installments(long requestId) {
        return db.jdbc.sql("SELECT id, request_id, seq, due_date, amount, status, notified_at FROM giving_installments WHERE request_id = :r ORDER BY seq")
                .param("r", requestId).query(InstallmentRow.class).list();
    }

    List<PaymentRow> myPayments(IdentityUser u) {
        return paymentsWhere("WHERE user_id = :u ORDER BY id DESC", Map.of("u", u.getId()));
    }

    record CartLine(Long id, Long catalogItemId, String title, String emoji, String category, int quantity, int unitPrice, String target) {}
    record Cart(List<CartLine> lines, int total, Map<String, Integer> annexPrices) {}

    Cart cart(IdentityUser u, com.menewa.catalog.CatalogLookup catalog) {
        var rows = db.jdbc.sql("SELECT id, user_id, catalog_item_id, quantity, target, created_at FROM giving_cart_items WHERE user_id = :u ORDER BY id")
                .param("u", u.getId()).query(GivingRows.CartRow.class).list();
        var entries = catalog.findAll(rows.stream().map(GivingRows.CartRow::catalogItemId).toList());
        List<CartLine> lines = new ArrayList<>();
        for (var c : rows) {
            var e = entries.get(c.catalogItemId());
            if (e == null) continue;
            lines.add(new CartLine(c.id(), e.id(), e.title(), e.emoji(), e.category(), c.quantity(), e.unitPrice(), c.target()));
        }
        return new Cart(lines, lines.stream().mapToInt(l -> l.quantity() * l.unitPrice()).sum(), settings.intMap("annex.prices"));
    }

    /* ───────── Prestataire ───────── */
    List<MissionRow> myMissions(IdentityUser u) {
        return missionsWhere("WHERE provider_id = :u ORDER BY id DESC", Map.of("u", u.getId()));
    }

    record ProviderDashboard(long proposed, long accepted, long inProgress, long completed, long validated, List<MissionRow> proposals) {}

    ProviderDashboard providerDashboard(IdentityUser u) {
        var ms = myMissions(u);
        return new ProviderDashboard(ms.stream().filter(m -> "proposed".equals(m.status())).count(),
                ms.stream().filter(m -> "accepted".equals(m.status())).count(),
                ms.stream().filter(m -> "in_progress".equals(m.status())).count(),
                ms.stream().filter(m -> "completed".equals(m.status())).count(),
                ms.stream().filter(m -> m.validatedAt() != null).count(),
                ms.stream().filter(m -> "proposed".equals(m.status())).toList());
    }

    record MissionRequestInfo(Long id, String ref, String title, String targetName, String targetZone,
            @com.fasterxml.jackson.annotation.JsonRawValue String annexServices) {}
    record BeneficiaryContact(String fullName, String phone, String address, String zone) {}
    record ProviderMissionDetail(MissionRow mission, MissionRequestInfo request, List<ItemRow> items, List<ProofRow> proofs, BeneficiaryContact beneficiary) {}

    ProviderMissionDetail providerMission(IdentityUser u, long id) {
        MissionRow m = db.mission(id);
        if (!u.getId().equals(m.providerId())) throw com.menewa.platform.Problems.notFound("Mission introuvable.");
        RequestRow r = db.request(m.requestId());
        BeneficiaryContact ben = null;
        // Coordonnées du bénéficiaire seulement après acceptation (besoin d'en connaître).
        if (r.beneficiaryId() != null && Set.of("accepted", "in_progress").contains(m.status())) {
            ben = db.jdbc.sql("SELECT full_name, phone, address, zone FROM giving_beneficiaries WHERE id = :id").param("id", r.beneficiaryId())
                    .query(BeneficiaryContact.class).optional().orElse(null);
        }
        return new ProviderMissionDetail(m, new MissionRequestInfo(r.id(), r.ref(), r.title(), r.targetName(), r.targetZone(), r.annexServices()),
                items(r.id()), proofsWhere("WHERE mission_id = :m ORDER BY id DESC", Map.of("m", id)), ben);
    }

    /* ───────── Bénéficiaire ───────── */
    record ReceivedGift(Long id, String title, String status, OffsetDateTime createdAt) {}

    List<ReceivedGift> beneficiaryRequests(IdentityUser u) {
        return db.jdbc.sql("""
                SELECT r.id, r.title, r.status, r.created_at FROM giving_requests r JOIN giving_beneficiaries b ON b.id = r.beneficiary_id
                WHERE b.user_id = :u AND r.status IN ('in_progress', 'completed', 'closed') ORDER BY r.id DESC""")
                .param("u", u.getId()).query(ReceivedGift.class).list();
    }

    /* ───────── Administration ───────── */
    record AdminDashboard(long toTreat, long infoRequested, long lateMissions, long proofsToReview, long paymentsToFollow, long openDisputes,
            long users, long completed, long collected, List<RequestRow> recent) {}

    AdminDashboard adminDashboard() {
        return new AdminDashboard(
                countSql("SELECT COUNT(*) FROM giving_requests WHERE status = 'submitted'", Map.of()),
                countSql("SELECT COUNT(*) FROM giving_requests WHERE status = 'info_requested'", Map.of()),
                countSql("SELECT COUNT(*) FROM giving_missions WHERE status IN ('proposed', 'accepted', 'in_progress') AND due_date < :now", Map.of("now", OffsetDateTime.now())),
                countSql("SELECT COUNT(*) FROM giving_proofs WHERE status = 'pending'", Map.of()),
                countSql("SELECT COUNT(*) FROM giving_payments WHERE status IN ('declared', 'validated', 'partial')", Map.of()),
                countSql("SELECT COUNT(*) FROM giving_disputes WHERE status IN ('open', 'investigating')", Map.of()),
                directory.count(),
                countSql("SELECT COUNT(*) FROM giving_requests WHERE status IN ('completed', 'closed')", Map.of()),
                countSql("SELECT COALESCE(SUM(amount_paid), 0) FROM giving_payments WHERE direction = 'in'", Map.of()),
                requestsWhere("WHERE status IN ('submitted', 'info_requested') ORDER BY id DESC LIMIT 6", Map.of()));
    }

    private long countSql(String sql, Map<String, ?> params) {
        Number n = db.jdbc.sql(sql).params(params).query(Long.class).single();
        return n == null ? 0 : n.longValue();
    }

    record AdminRequestRow(RequestRow request, String donorName, String donorPhone) {}

    List<AdminRequestRow> adminRequests(String status, String q) {
        StringBuilder where = new StringBuilder("WHERE 1 = 1");
        Map<String, Object> params = new HashMap<>();
        if (status != null && !status.isBlank()) { where.append(" AND status = :s"); params.put("s", status); }
        if (q != null && !q.isBlank()) {
            where.append(" AND (LOWER(title) LIKE :q OR LOWER(ref) LIKE :q OR LOWER(COALESCE(target_name, '')) LIKE :q)");
            params.put("q", "%" + q.trim().toLowerCase(Locale.ROOT) + "%");
        }
        var rows = requestsWhere(where + " ORDER BY id DESC LIMIT 200", params);
        var names = directory.findAll(rows.stream().map(RequestRow::userId).toList());
        return rows.stream().map(r -> new AdminRequestRow(r, name(names, r.userId()), names.containsKey(r.userId()) ? names.get(r.userId()).phone() : null)).toList();
    }

    record AdminMission(MissionRow mission, String providerName, String requestRef) {}
    record Margin(int fees, int cost, int gross, double pct) {}
    record AdminRequestDetail(RequestRow request, String donorName, String donorPhone, List<ItemRow> items, List<HistoryRow> history,
            List<InstallmentRow> installments, List<PaymentRow> payments, List<ProofRow> proofs, List<AdminMission> missions,
            List<DisputeRow> disputes, BeneficiaryRow beneficiary, List<BeneficiaryRow> beneficiaries, Margin margin, int targetMarginPct,
            List<Requests.Check> checks, List<ProviderDirectory.ProviderOption> providers) {}

    static Margin margin(RequestRow r, int fees) {
        int cost = r.executionBudget() + fees + r.otherFees();
        int gross = r.totalAmount() - cost;
        return new Margin(fees, cost, gross, r.totalAmount() > 0 ? gross * 100.0 / r.totalAmount() : 0);
    }

    @Transactional
    AdminRequestDetail adminRequest(IdentityUser u, long id) {
        RequestRow r = db.request(id);
        UserRef donor = directory.find(r.userId()).orElse(null);
        var ms = missionsWhere("WHERE request_id = :r ORDER BY id", Map.of("r", id));
        var names = directory.findAll(ms.stream().map(MissionRow::providerId).toList());
        boolean canBen = IdentityPermissions.has(u, "beneficiaries.view");
        BeneficiaryRow target = null;
        List<BeneficiaryRow> bens = List.of();
        if (canBen) {
            bens = beneficiaries(false);
            if (r.beneficiaryId() != null) {
                target = beneficiary(r.beneficiaryId());
                if (target != null) audit.record(u, "beneficiary.view", "beneficiary", target.id(), Map.of("via", "request " + r.ref()));
            }
        }
        int fees = ms.stream().filter(m -> !Set.of("refused", "cancelled").contains(m.status())).mapToInt(MissionRow::providerFee).sum();
        return new AdminRequestDetail(r, donor == null ? null : donor.displayName(), donor == null ? null : donor.phone(), items(id), history(id),
                installments(id),
                IdentityPermissions.has(u, "payments.manage") ? paymentsWhere("WHERE request_id = :r ORDER BY id", Map.of("r", id)) : List.of(),
                proofsWhere("WHERE request_id = :r ORDER BY id DESC", Map.of("r", id)),
                ms.stream().map(m -> new AdminMission(m, name(names, m.providerId()), r.ref())).toList(),
                db.jdbc.sql("SELECT " + GivingRows.DISPUTE_COLS + " FROM giving_disputes WHERE request_id = :r ORDER BY id").param("r", id).query(DisputeRow.class).list(),
                target, bens,
                IdentityPermissions.has(u, "finances.view") ? margin(r, fees) : null,
                settings.intValue("finance.platform_margin_pct", 0),
                requests.completionChecks(id), providers.activeOptions());
    }

    record AdminMissions(List<AdminMission> missions, List<ProviderDirectory.ProviderOption> providers) {}

    AdminMissions adminMissions(String status) {
        var ms = status == null || status.isBlank() ? missionsWhere("ORDER BY id DESC LIMIT 200", Map.of())
                : missionsWhere("WHERE status = :s ORDER BY id DESC LIMIT 200", Map.of("s", status));
        var names = directory.findAll(ms.stream().map(MissionRow::providerId).toList());
        Map<Long, String> refs = refs(ms.stream().map(MissionRow::requestId).toList());
        return new AdminMissions(ms.stream().map(m -> new AdminMission(m, name(names, m.providerId()), refs.get(m.requestId()))).toList(),
                providers.activeOptions());
    }

    private Map<Long, String> refs(List<Long> requestIds) {
        var ids = requestIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) return Map.of();
        return db.jdbc.sql("SELECT id, ref FROM giving_requests WHERE id IN (:ids)").param("ids", ids)
                .query((rs, i) -> Map.entry(rs.getLong(1), rs.getString(2))).list().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    record AdminProof(ProofRow proof, String requestRef, String uploaderName) {}

    List<AdminProof> adminProofs() {
        var ps = proofsWhere("ORDER BY id DESC LIMIT 100", Map.of());
        var names = directory.findAll(ps.stream().map(ProofRow::uploadedBy).toList());
        var refs = refs(ps.stream().map(ProofRow::requestId).toList());
        return ps.stream().map(p -> new AdminProof(p, refs.get(p.requestId()), name(names, p.uploadedBy()))).toList();
    }

    record AdminPayment(PaymentRow payment, String userName, String userPhone) {}
    record AdminPayments(List<AdminPayment> payments, long planned, long paid) {}

    AdminPayments adminPayments(String status, String direction) {
        StringBuilder where = new StringBuilder("WHERE 1 = 1");
        Map<String, Object> params = new HashMap<>();
        if (status != null && !status.isBlank()) { where.append(" AND status = :s"); params.put("s", status); }
        if (direction != null && !direction.isBlank()) { where.append(" AND direction = :d"); params.put("d", direction); }
        var ps = paymentsWhere(where + " ORDER BY id DESC LIMIT 200", params);
        var names = directory.findAll(ps.stream().map(PaymentRow::userId).toList());
        return new AdminPayments(ps.stream().map(p -> new AdminPayment(p, name(names, p.userId()),
                names.containsKey(p.userId()) ? names.get(p.userId()).phone() : null)).toList(),
                ps.stream().mapToLong(PaymentRow::amountPlanned).sum(), ps.stream().mapToLong(PaymentRow::amountPaid).sum());
    }

    record FinanceRow(Long id, String ref, String title, int totalAmount, int executionBudget, int otherFees, Long partnerId, int actors, Margin margin) {}
    record Finances(List<FinanceRow> rows, long revenue, long cost, long gross, int targetMarginPct) {}

    Finances finances() {
        var rs = requestsWhere("WHERE status NOT IN ('rejected', 'cancelled') ORDER BY id DESC LIMIT 100", Map.of());
        var ids = rs.stream().map(RequestRow::id).toList();
        var ms = ids.isEmpty() ? List.<MissionRow>of() : missionsWhere("WHERE request_id IN (:ids)", Map.of("ids", ids));
        List<FinanceRow> rows = new ArrayList<>();
        for (RequestRow r : rs) {
            var active = ms.stream().filter(m -> m.requestId().equals(r.id()) && !Set.of("refused", "cancelled").contains(m.status())).toList();
            int fees = active.stream().mapToInt(MissionRow::providerFee).sum();
            rows.add(new FinanceRow(r.id(), r.ref(), r.title(), r.totalAmount(), r.executionBudget(), r.otherFees(), r.partnerId(), active.size(), margin(r, fees)));
        }
        long rev = rows.stream().mapToLong(FinanceRow::totalAmount).sum();
        long cost = rows.stream().mapToLong(f -> f.margin().cost()).sum();
        return new Finances(rows, rev, cost, rev - cost, settings.intValue("finance.platform_margin_pct", 0));
    }

    record AdminDispute(DisputeRow dispute, String openedByName) {}

    List<AdminDispute> adminDisputes() {
        var ds = db.jdbc.sql("SELECT " + GivingRows.DISPUTE_COLS + " FROM giving_disputes ORDER BY id DESC").query(DisputeRow.class).list();
        var names = directory.findAll(ds.stream().map(DisputeRow::openedBy).toList());
        return ds.stream().map(d -> new AdminDispute(d, name(names, d.openedBy()))).toList();
    }

    record DisputeMessageView(DisputeMessageRow message, String authorName) {}
    record DisputeDetail(DisputeRow dispute, List<DisputeMessageView> messages, String openedByName, String againstName, String responsibleName) {}

    DisputeDetail dispute(long id) {
        DisputeRow d = db.dispute(id);
        var msgs = db.jdbc.sql("SELECT id, dispute_id, author_id, body, attachment_key, attachment_name, attachment_mime, created_at FROM giving_dispute_messages WHERE dispute_id = :d ORDER BY id")
                .param("d", id).query(DisputeMessageRow.class).list();
        List<Long> ids = new ArrayList<>(msgs.stream().map(DisputeMessageRow::authorId).toList());
        ids.add(d.openedBy()); ids.add(d.againstUserId()); ids.add(d.responsibleId());
        var names = directory.findAll(ids);
        return new DisputeDetail(d, msgs.stream().map(m -> new DisputeMessageView(m, name(names, m.authorId()))).toList(),
                name(names, d.openedBy()), name(names, d.againstUserId()), name(names, d.responsibleId()));
    }

    List<BeneficiaryRow> beneficiaries(boolean includeArchived) {
        String where = includeArchived ? "" : "WHERE archived_at IS NULL ";
        return db.jdbc.sql("SELECT id, full_name, phone, zone, address, needs, notes, consent, user_id, created_by, archived_at, created_at FROM giving_beneficiaries "
                + where + "ORDER BY id DESC").query(BeneficiaryRow.class).list();
    }

    BeneficiaryRow beneficiary(long id) {
        return db.jdbc.sql("SELECT id, full_name, phone, zone, address, needs, notes, consent, user_id, created_by, archived_at, created_at FROM giving_beneficiaries WHERE id = :id")
                .param("id", id).query(BeneficiaryRow.class).optional().orElse(null);
    }

    record TargetUsage(String target, long count) {}

    List<TargetUsage> targetUsage() {
        return db.jdbc.sql("SELECT target, COUNT(*) AS count FROM giving_request_items WHERE target IS NOT NULL GROUP BY target").query(TargetUsage.class).list();
    }

    /* ───────── Statistiques ───────── */
    record StatFilters(String from, String to, String status, String category, Long provider, String zone) {}
    record StatRow(RequestRow request, String providers, boolean late) {}
    record Kpi(long received, long accepted, long rejected, long realized, double realizationRate, double acceptanceRate, double avgDays,
            long documented, long late, long amount) {}
    record Stats(List<StatRow> rows, Kpi kpi) {}

    Stats stats(StatFilters f) {
        StringBuilder where = new StringBuilder("WHERE 1 = 1");
        Map<String, Object> params = new HashMap<>();
        if (f.from() != null && !f.from().isBlank()) {
            where.append(" AND created_at >= :from");
            params.put("from", LocalDate.parse(f.from()).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime());
        }
        if (f.to() != null && !f.to().isBlank()) {
            where.append(" AND created_at < :to");
            params.put("to", LocalDate.parse(f.to()).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime());
        }
        if (f.status() != null && !f.status().isBlank()) { where.append(" AND status = :s"); params.put("s", f.status()); }
        if (f.category() != null && !f.category().isBlank()) { where.append(" AND category = :c"); params.put("c", f.category()); }
        var rs = requestsWhere(where + " ORDER BY id DESC", params);
        var ids = rs.stream().map(RequestRow::id).toList();
        var ms = ids.isEmpty() ? List.<MissionRow>of() : missionsWhere("WHERE request_id IN (:ids)", Map.of("ids", ids));
        var names = directory.findAll(ms.stream().map(MissionRow::providerId).toList());
        List<StatRow> rows = new ArrayList<>();
        for (RequestRow r : rs) {
            var rm = ms.stream().filter(m -> m.requestId().equals(r.id())).toList();
            if (f.provider() != null && rm.stream().noneMatch(m -> f.provider().equals(m.providerId()))) continue;
            if (f.zone() != null && !f.zone().isBlank()
                    && !(r.targetZone() == null ? "" : r.targetZone().toLowerCase(Locale.ROOT)).contains(f.zone().toLowerCase(Locale.ROOT))) continue;
            String provs = rm.stream().map(m -> name(names, m.providerId())).filter(Objects::nonNull).collect(Collectors.joining(", "));
            rows.add(new StatRow(r, provs, rm.stream().anyMatch(MissionRow::late)));
        }
        Set<String> decided = Set.of("accepted", "in_progress", "completed", "closed", "rejected");
        Set<String> accepted = Set.of("accepted", "in_progress", "completed", "closed");
        long nDecided = rows.stream().filter(x -> decided.contains(x.request().status())).count();
        long nAccepted = rows.stream().filter(x -> accepted.contains(x.request().status())).count();
        long realized = rows.stream().filter(x -> Set.of("completed", "closed").contains(x.request().status())).count();
        var done = rows.stream().filter(x -> x.request().completedAt() != null).toList();
        double avgDays = done.isEmpty() ? 0 : done.stream()
                .mapToDouble(x -> ChronoUnit.MINUTES.between(x.request().createdAt(), x.request().completedAt()) / 1440.0).average().orElse(0);
        return new Stats(rows, new Kpi(rows.size(), nAccepted, rows.stream().filter(x -> "rejected".equals(x.request().status())).count(), realized,
                nAccepted == 0 ? 0 : realized * 100.0 / nAccepted, nDecided == 0 ? 0 : nAccepted * 100.0 / nDecided, avgDays,
                rows.stream().mapToLong(x -> x.request().executionBudget() + x.request().otherFees()).sum(),
                rows.stream().filter(StatRow::late).count(), rows.stream().mapToLong(x -> x.request().totalAmount()).sum()));
    }

    String statsCsv(StatFilters f) {
        var rows = stats(f).rows();
        StringBuilder out = new StringBuilder("﻿");
        out.append(csv("Référence", "Intitulé", "Type", "Catégorie", "Zone", "Prestataires", "Montant FCFA", "Budget exécution", "Autres frais", "Statut", "Créée le")).append('\n');
        for (StatRow x : rows) {
            RequestRow r = x.request();
            out.append(csv(r.ref(), r.title(), r.kind(), r.category(), r.targetZone(), x.providers(), r.totalAmount(), r.executionBudget(), r.otherFees(),
                    r.status(), r.createdAt().toLocalDate())).append('\n');
        }
        return out.toString();
    }

    private static String csv(Object... values) {
        List<String> cells = new ArrayList<>();
        for (Object v : values) cells.add("\"" + String.valueOf(v == null ? "" : v).replace("\"", "\"\"") + "\"");
        return String.join(";", cells);
    }
}
