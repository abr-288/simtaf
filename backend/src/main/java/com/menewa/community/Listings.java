package com.menewa.community;

import com.menewa.identity.IdentityAccess;
import com.menewa.identity.IdentityDirectory;
import com.menewa.identity.IdentityPermissions;
import com.menewa.identity.IdentityUser;
import com.menewa.identity.UserRef;
import com.menewa.platform.Audit;
import com.menewa.platform.Notifications;
import com.menewa.platform.Problems;
import com.menewa.rewards.Rewards;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Publications partenaires et campagnes de financement participatif, modérées avant mise en ligne. */
@RestController
@RequestMapping("/api/v1")
class Listings {
    private static final String AUTH = "Authorization";
    private static final String COLS = "id, owner_id, type, title, description, zone, goal_amount, raised_amount, mena_support, ends_at, status, rejection_reason, created_at";
    private static final Set<String> METHODS = Set.of("mobile_money", "card", "bank", "cash");

    record Listing(Long id, Long ownerId, String type, String title, String description, String zone, int goalAmount, int raisedAmount,
            int menaSupport, OffsetDateTime endsAt, String status, String rejectionReason, OffsetDateTime createdAt) {}
    record ListingView(Listing listing, String ownerName, String organization, long confirmedContributions) {}
    record Contribution(Long id, Long listingId, Long userId, int amount, String method, String reference, String status, OffsetDateTime createdAt) {}
    record ContributionView(Contribution contribution, String userName, String listingTitle) {}
    record PartnerDashboard(long total, long published, long pending, long raised) {}

    private final JdbcClient jdbc;
    private final IdentityAccess access;
    private final IdentityDirectory directory;
    private final Notifications notifications;
    private final Audit audit;
    private final Rewards rewards;

    Listings(JdbcClient jdbc, IdentityAccess access, IdentityDirectory directory, Notifications notifications, Audit audit, Rewards rewards) {
        this.jdbc = jdbc;
        this.access = access;
        this.directory = directory;
        this.notifications = notifications;
        this.audit = audit;
        this.rewards = rewards;
    }

    private List<ListingView> views(List<Listing> ls) {
        var owners = directory.findAll(ls.stream().map(Listing::ownerId).toList());
        Map<Long, Long> confirmed = new HashMap<>();
        if (!ls.isEmpty()) {
            jdbc.sql("SELECT listing_id, COUNT(*) FROM community_listing_contributions WHERE status = 'confirmed' AND listing_id IN (:ids) GROUP BY listing_id")
                    .param("ids", ls.stream().map(Listing::id).toList())
                    .query((rs, i) -> Map.entry(rs.getLong(1), rs.getLong(2))).list().forEach(e -> confirmed.put(e.getKey(), e.getValue()));
        }
        return ls.stream().map(l -> {
            UserRef o = owners.get(l.ownerId());
            return new ListingView(l, o == null ? "—" : o.displayName(), o == null ? null : o.organization(), confirmed.getOrDefault(l.id(), 0L));
        }).toList();
    }

    private Listing listing(long id) {
        return jdbc.sql("SELECT " + COLS + " FROM community_listings WHERE id = :id").param("id", id).query(Listing.class)
                .optional().orElseThrow(() -> Problems.notFound("Publication introuvable."));
    }

    @GetMapping("/listings/published")
    List<ListingView> published(@RequestHeader(value = AUTH, required = false) String auth) {
        access.requireUser(auth);
        return views(jdbc.sql("SELECT " + COLS + " FROM community_listings WHERE status = 'published' ORDER BY id DESC").query(Listing.class).list());
    }

    record PublicListing(Long id, String type, String title, String description, String zone, int goalAmount, int raisedAmount,
            int menaSupport, OffsetDateTime endsAt, String organization) {}

    /** Campagnes publiées pour le site public (sans authentification). */
    @GetMapping("/public/listings")
    List<PublicListing> publicListings() {
        return views(jdbc.sql("SELECT " + COLS + " FROM community_listings WHERE status = 'published' ORDER BY id DESC").query(Listing.class).list())
                .stream().map(v -> new PublicListing(v.listing().id(), v.listing().type(), v.listing().title(), v.listing().description(),
                        v.listing().zone(), v.listing().goalAmount(), v.listing().raisedAmount(), v.listing().menaSupport(), v.listing().endsAt(),
                        v.organization() == null ? v.ownerName() : v.organization()))
                .toList();
    }

    @GetMapping("/listings/mine")
    List<ListingView> mine(@RequestHeader(value = AUTH, required = false) String auth) {
        IdentityUser u = access.requireRole(auth, "partner");
        return views(jdbc.sql("SELECT " + COLS + " FROM community_listings WHERE owner_id = :u ORDER BY id DESC").param("u", u.getId()).query(Listing.class).list());
    }

    @GetMapping("/listings/dashboard")
    PartnerDashboard dashboard(@RequestHeader(value = AUTH, required = false) String auth) {
        IdentityUser u = access.requireRole(auth, "partner");
        var ls = jdbc.sql("SELECT " + COLS + " FROM community_listings WHERE owner_id = :u").param("u", u.getId()).query(Listing.class).list();
        return new PartnerDashboard(ls.size(), ls.stream().filter(l -> "published".equals(l.status())).count(),
                ls.stream().filter(l -> "pending".equals(l.status())).count(), ls.stream().mapToLong(Listing::raisedAmount).sum());
    }

    record CreateListing(String type, String title, String description, String zone, int goalAmount, String endsAt) {}

    @PostMapping("/listings")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    Map<String, String> create(@RequestHeader(value = AUTH, required = false) String auth, @RequestBody CreateListing r) {
        IdentityUser u = access.requireRole(auth, "partner");
        if (!Set.of("don", "action", "crowdfunding").contains(r.type())) throw Problems.bad("Type invalide.");
        String title = Problems.trim(r.title(), 150);
        if (title.length() < 4) throw Problems.bad("Intitulé trop court.");
        if ("crowdfunding".equals(r.type()) && r.goalAmount() < 10000) throw Problems.bad("Un objectif de financement (≥ 10 000 FCFA) est requis.");
        OffsetDateTime ends = null;
        if (r.endsAt() != null && !r.endsAt().isBlank()) {
            try { ends = LocalDate.parse(r.endsAt().trim()).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime(); }
            catch (RuntimeException ex) { throw Problems.bad("Date de fin invalide."); }
        }
        Map<String, Object> p = new HashMap<>();
        p.put("o", u.getId()); p.put("t", r.type()); p.put("title", title); p.put("d", Problems.trimToNull(r.description(), 2000));
        p.put("z", Problems.trimToNull(r.zone(), 100)); p.put("g", "crowdfunding".equals(r.type()) ? r.goalAmount() : 0); p.put("e", ends);
        var keys = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO community_listings (owner_id, type, title, description, zone, goal_amount, ends_at) VALUES (:o, :t, :title, :d, :z, :g, :e)")
                .params(p).update(keys, "id");
        audit.record(u, "listing.create", "listing", keys.getKey().longValue());
        notifications.notifyAdmins("listings.moderate", "listing", "Publication à modérer : " + title, null, "/admin/publications");
        return Map.of("ok", "Publication envoyée en modération.");
    }

    record Contribute(int amount, String method, String reference) {}

    @PostMapping("/listings/{id}/contributions")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    Map<String, String> contribute(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id, @RequestBody Contribute r) {
        IdentityUser u = access.requireUser(auth);
        if (IdentityPermissions.isAdmin(u)) throw Problems.bad("Les administrateurs ne peuvent pas contribuer.");
        Listing l = listing(id);
        if (!"published".equals(l.status())) throw Problems.notFound("Campagne introuvable.");
        if (r.amount() < 500) throw Problems.bad("Contribution minimale : 500 FCFA.");
        if (!METHODS.contains(r.method())) throw Problems.bad("Choisissez un moyen de paiement.");
        jdbc.sql("INSERT INTO community_listing_contributions (listing_id, user_id, amount, method, reference) VALUES (:l, :u, :a, :m, :r)")
                .param("l", id).param("u", u.getId()).param("a", r.amount()).param("m", r.method()).param("r", Problems.trimToNull(r.reference(), 100)).update();
        notifications.notifyAdmins("listings.moderate", "payment", "Contribution à confirmer (" + l.title() + ")", r.amount() + " FCFA", "/admin/publications");
        return Map.of("ok", "Contribution enregistrée. Elle sera comptabilisée après confirmation du paiement.");
    }

    record Support(int amount) {}

    @PostMapping("/listings/{id}/mena")
    @Transactional
    Map<String, String> supportMena(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id, @RequestBody Support r) {
        IdentityUser u = access.requireUser(auth);
        Listing l = listing(id);
        if (!"published".equals(l.status())) throw Problems.notFound("Campagne introuvable.");
        rewards.spend(u.getId(), r.amount(), "Soutien symbolique : " + l.title(), "listing", id);
        jdbc.sql("UPDATE community_listings SET mena_support = mena_support + :a WHERE id = :id").param("a", r.amount()).param("id", id).update();
        return Map.of("ok", "Merci ! " + r.amount() + " Mena offerts en soutien symbolique à la campagne.");
    }

    /* ───────── Modération ───────── */
    record AdminListings(List<ListingView> listings, List<ContributionView> pendingContributions) {}

    @GetMapping("/admin/listings")
    AdminListings adminList(@RequestHeader(value = AUTH, required = false) String auth) {
        access.requirePermission(auth, "listings.moderate");
        var ls = jdbc.sql("SELECT " + COLS + " FROM community_listings ORDER BY id DESC").query(Listing.class).list();
        var cs = jdbc.sql("SELECT id, listing_id, user_id, amount, method, reference, status, created_at FROM community_listing_contributions WHERE status = 'declared' ORDER BY id")
                .query(Contribution.class).list();
        var names = directory.findAll(cs.stream().map(Contribution::userId).toList());
        Map<Long, String> titles = new HashMap<>();
        ls.forEach(l -> titles.put(l.id(), l.title()));
        return new AdminListings(views(ls), cs.stream().map(c -> new ContributionView(c,
                names.containsKey(c.userId()) ? names.get(c.userId()).displayName() : "—", titles.get(c.listingId()))).toList());
    }

    record Moderate(String decision, String reason) {}

    @PostMapping("/admin/listings/{id}/moderate")
    @Transactional
    Map<String, String> moderate(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id, @RequestBody Moderate r) {
        IdentityUser u = access.requirePermission(auth, "listings.moderate");
        Listing l = listing(id);
        String reason = Problems.trim(r.reason(), 400);
        String decision = r.decision() == null ? "" : r.decision();
        if (!Set.of("publish", "close", "reject").contains(decision)) throw Problems.bad("Décision invalide.");
        if ("reject".equals(decision) && reason.length() < 5) throw Problems.bad("Motif de refus requis.");
        String status = "publish".equals(decision) ? "published" : "close".equals(decision) ? "closed" : "rejected";
        jdbc.sql("UPDATE community_listings SET status = :s, rejection_reason = :r WHERE id = :id")
                .param("s", status).param("r", "rejected".equals(status) ? reason : null).param("id", id).update();
        audit.record(u, "listing." + status, "listing", id, reason.isEmpty() ? null : Map.of("reason", reason));
        notifications.notify(l.ownerId(), "listing", "Publication « " + l.title() + " » : "
                + ("published".equals(status) ? "publiée" : "closed".equals(status) ? "close" : "refusée"), reason.isEmpty() ? null : reason, "/espace/publications");
        if ("published".equals(status)) rewards.award(l.ownerId(), "listing_published", "listing", id, u.getId());
        return Map.of("ok", "Décision enregistrée.");
    }

    record ContributionDecision(String decision) {}

    @PostMapping("/admin/contributions/{id}/decision")
    @Transactional
    Map<String, String> decideContribution(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id, @RequestBody ContributionDecision r) {
        IdentityUser u = access.requirePermission(auth, "listings.moderate");
        boolean ok = "confirm".equals(r.decision());
        var c = jdbc.sql("SELECT id, listing_id, user_id, amount, method, reference, status, created_at FROM community_listing_contributions WHERE id = :id AND status = 'declared'")
                .param("id", id).query(Contribution.class).optional().orElseThrow(() -> Problems.notFound("Contribution introuvable ou déjà traitée."));
        jdbc.sql("UPDATE community_listing_contributions SET status = :s WHERE id = :id").param("s", ok ? "confirmed" : "rejected").param("id", id).update();
        if (ok) jdbc.sql("UPDATE community_listings SET raised_amount = raised_amount + :a WHERE id = :l").param("a", c.amount()).param("l", c.listingId()).update();
        audit.record(u, ok ? "contribution.confirm" : "contribution.reject", "contribution", id, Map.of("amount", c.amount()));
        notifications.notify(c.userId(), "payment", ok ? "Contribution confirmée — merci !" : "Contribution non validée", c.amount() + " FCFA", "/espace/campagnes");
        if (ok) rewards.award(c.userId(), "contribution_confirmed", "contribution", id, u.getId());
        return Map.of("ok", ok ? "Contribution confirmée." : "Contribution rejetée.");
    }
}
