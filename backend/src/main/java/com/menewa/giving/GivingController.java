package com.menewa.giving;

import com.menewa.catalog.CatalogLookup;
import com.menewa.identity.IdentityAccess;
import com.menewa.identity.IdentityPermissions;
import com.menewa.identity.IdentityUser;
import com.menewa.platform.Audit;
import com.menewa.platform.Problems;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Espace membre : donateurs/clients, prestataires, bénéficiaires ; fichiers protégés. */
@RestController
@RequestMapping("/api/v1")
class GivingController {
    private static final String AUTH = "Authorization";

    private final IdentityAccess access;
    private final Requests requests;
    private final Missions missions;
    private final Evidence evidence;
    private final GivingQueries queries;
    private final CatalogLookup catalog;
    private final GivingDb db;
    private final Storage storage;
    private final Audit audit;

    GivingController(IdentityAccess access, Requests requests, Missions missions, Evidence evidence, GivingQueries queries,
            CatalogLookup catalog, GivingDb db, Storage storage, Audit audit) {
        this.access = access;
        this.requests = requests;
        this.missions = missions;
        this.evidence = evidence;
        this.queries = queries;
        this.catalog = catalog;
        this.db = db;
        this.storage = storage;
        this.audit = audit;
    }

    private static Map<String, String> ok(String message) { return Map.of("ok", message); }

    /* ───────── Panier ───────── */
    @GetMapping("/cart")
    GivingQueries.Cart cart(@RequestHeader(value = AUTH, required = false) String auth) {
        return queries.cart(access.requireRole(auth, "donor", "client"), catalog);
    }

    record AddToCart(long catalogItemId, int quantity, String target) {}

    @PostMapping("/cart")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, String> addToCart(@RequestHeader(value = AUTH, required = false) String auth, @RequestBody AddToCart r) {
        return ok(requests.addToCart(access.requireUser(auth), r.catalogItemId(), r.quantity(), r.target()));
    }

    @DeleteMapping("/cart/{id}")
    Map<String, String> removeCartItem(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id) {
        requests.removeCartItem(access.requireUser(auth), id);
        return ok("Article retiré du panier.");
    }

    @PostMapping("/cart/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    Requests.Created checkout(@RequestHeader(value = AUTH, required = false) String auth, @RequestBody Requests.CheckoutRequest r) {
        return requests.checkout(access.requireUser(auth), r);
    }

    /* ───────── Demandes (donateur / client) ───────── */
    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    Requests.Created createCustom(@RequestHeader(value = AUTH, required = false) String auth, @RequestBody Requests.CustomRequest r) {
        return requests.createCustom(access.requireUser(auth), r);
    }

    @GetMapping("/requests")
    List<GivingRows.RequestRow> myRequests(@RequestHeader(value = AUTH, required = false) String auth, @RequestParam(required = false) String status) {
        return queries.myRequests(access.requireRole(auth, "donor", "client"), status);
    }

    @GetMapping("/requests/dashboard")
    GivingQueries.DonorDashboard donorDashboard(@RequestHeader(value = AUTH, required = false) String auth) {
        return queries.donorDashboard(access.requireRole(auth, "donor", "client"));
    }

    @GetMapping("/requests/{id}")
    GivingQueries.DonorRequestDetail request(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id) {
        return queries.donorRequest(access.requireRole(auth, "donor", "client"), id);
    }

    @PostMapping("/requests/{id}/cancel")
    Map<String, String> cancel(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id) {
        return ok(requests.cancel(access.requireUser(auth), id));
    }

    record Answer(String answer) {}

    @PostMapping("/requests/{id}/answer")
    Map<String, String> answer(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id, @RequestBody Answer r) {
        return ok(requests.answerInfo(access.requireUser(auth), id, r.answer()));
    }

    record ReviewRequest(int rating, String comment) {}

    @PostMapping("/requests/{id}/review")
    Map<String, String> review(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id, @RequestBody ReviewRequest r) {
        return ok(requests.rateMission(access.requireUser(auth), id, r.rating(), r.comment()));
    }

    record DisputeRequest(String subject, String description) {}

    @PostMapping("/requests/{id}/disputes")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, String> openDispute(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id, @RequestBody DisputeRequest r) {
        return ok(evidence.openDispute(access.requireUser(auth), id, r.subject(), r.description()));
    }

    /* ───────── Paiements ───────── */
    @GetMapping("/payments/mine")
    List<GivingRows.PaymentRow> myPayments(@RequestHeader(value = AUTH, required = false) String auth) {
        return queries.myPayments(access.requireUser(auth));
    }

    @PostMapping(value = "/payments/{id}/declare", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Map<String, String> declare(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id,
            @RequestParam String method, @RequestParam(required = false) String reference,
            @RequestPart(value = "receipt", required = false) MultipartFile receipt) {
        return ok(evidence.declarePayment(access.requireUser(auth), id, method, reference, receipt));
    }

    /* ───────── Litiges ───────── */
    @GetMapping("/disputes/{id}")
    GivingQueries.DisputeDetail dispute(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id) {
        IdentityUser u = access.requireUser(auth);
        if (!evidence.canSeeDispute(u, db.dispute(id))) throw Problems.notFound("Litige introuvable.");
        return queries.dispute(id);
    }

    @PostMapping(value = "/disputes/{id}/messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, String> disputeMessage(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id,
            @RequestParam String body, @RequestPart(value = "attachment", required = false) MultipartFile attachment) {
        return ok(evidence.postDisputeMessage(access.requireUser(auth), id, body, attachment));
    }

    /* ───────── Missions (prestataire) ───────── */
    @GetMapping("/missions/mine")
    List<GivingRows.MissionRow> myMissions(@RequestHeader(value = AUTH, required = false) String auth) {
        return queries.myMissions(access.requireRole(auth, "provider"));
    }

    @GetMapping("/missions/dashboard")
    GivingQueries.ProviderDashboard providerDashboard(@RequestHeader(value = AUTH, required = false) String auth) {
        return queries.providerDashboard(access.requireRole(auth, "provider"));
    }

    @GetMapping("/missions/{id}")
    GivingQueries.ProviderMissionDetail mission(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id) {
        return queries.providerMission(access.requireRole(auth, "provider"), id);
    }

    record Respond(String decision, String reason) {}

    @PostMapping("/missions/{id}/respond")
    Map<String, String> respond(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id, @RequestBody Respond r) {
        return ok(missions.respond(access.requireUser(auth), id, r.decision(), r.reason()));
    }

    @PostMapping("/missions/{id}/start")
    Map<String, String> start(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id) {
        return ok(missions.start(access.requireUser(auth), id));
    }

    @PostMapping("/missions/{id}/complete")
    Map<String, String> complete(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id) {
        return ok(missions.complete(access.requireUser(auth), id));
    }

    @PostMapping(value = "/proofs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, String> uploadProof(@RequestHeader(value = AUTH, required = false) String auth,
            @RequestParam(required = false) Long missionId, @RequestParam(required = false) Long requestId, @RequestParam String type,
            @RequestParam(required = false) String caption, @RequestParam(defaultValue = "false") boolean hidden,
            @RequestPart("file") MultipartFile file) {
        return ok(evidence.uploadProof(access.requireUser(auth), missionId, requestId, type, caption, hidden, file));
    }

    /* ───────── Bénéficiaire ───────── */
    @GetMapping("/beneficiary/requests")
    List<GivingQueries.ReceivedGift> received(@RequestHeader(value = AUTH, required = false) String auth) {
        return queries.beneficiaryRequests(access.requireRole(auth, "beneficiary"));
    }

    /* ───────── Fichiers protégés ───────── */
    @GetMapping("/files/{kind}/{id}")
    ResponseEntity<byte[]> file(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable String kind, @PathVariable long id) {
        IdentityUser u = access.requireUser(auth);
        String key, name, mime;
        switch (kind) {
            case "proof" -> {
                var p = db.proof(id);
                boolean allowed = IdentityPermissions.has(u, "proofs.review") || u.getId().equals(p.uploadedBy());
                if (!allowed && p.missionId() != null) allowed = u.getId().equals(db.mission(p.missionId()).providerId());
                if (!allowed) allowed = db.request(p.requestId()).userId().equals(u.getId()) && "validated".equals(p.status()) && p.visibleToDonor();
                if (!allowed) throw Problems.forbidden();
                key = p.fileKey(); name = p.fileName(); mime = p.mime();
            }
            case "receipt" -> {
                var p = db.payment(id);
                if (p.receiptKey() == null) throw Problems.notFound("Introuvable.");
                boolean allowed = IdentityPermissions.has(u, "payments.manage") || u.getId().equals(p.userId());
                if (!allowed && p.requestId() != null) allowed = db.request(p.requestId()).userId().equals(u.getId()) && "in".equals(p.direction());
                if (!allowed) throw Problems.forbidden();
                key = p.receiptKey(); name = p.receiptName() == null ? "recu" : p.receiptName(); mime = p.receiptMime();
            }
            case "dispute" -> {
                var m = db.jdbc.sql("SELECT id, dispute_id, author_id, body, attachment_key, attachment_name, attachment_mime, created_at FROM giving_dispute_messages WHERE id = :id")
                        .param("id", id).query(GivingRows.DisputeMessageRow.class).optional().orElseThrow(() -> Problems.notFound("Introuvable."));
                if (m.attachmentKey() == null) throw Problems.notFound("Introuvable.");
                if (!evidence.canSeeDispute(u, db.dispute(m.disputeId()))) throw Problems.forbidden();
                key = m.attachmentKey(); name = m.attachmentName() == null ? "piece" : m.attachmentName(); mime = m.attachmentMime();
            }
            default -> throw Problems.notFound("Type inconnu.");
        }
        if (IdentityPermissions.has(u, "proofs.review") || IdentityPermissions.has(u, "payments.manage")) audit.record(u, "file.download", kind, id);
        byte[] bytes;
        try { bytes = storage.read(key); } catch (Exception ex) { throw Problems.notFound("Fichier indisponible."); }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, mime == null ? "application/octet-stream" : mime)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + java.net.URLEncoder.encode(name, java.nio.charset.StandardCharsets.UTF_8) + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .header("X-Content-Type-Options", "nosniff")
                .body(bytes);
    }
}
