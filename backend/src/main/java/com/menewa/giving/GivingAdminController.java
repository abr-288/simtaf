package com.menewa.giving;

import com.menewa.identity.IdentityAccess;
import com.menewa.identity.IdentityUser;
import com.menewa.platform.Audit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Opérations d'administration : chaque route revérifie la permission correspondante. */
@RestController
@RequestMapping("/api/v1/admin")
class GivingAdminController {
    private static final String AUTH = "Authorization";

    private final IdentityAccess access;
    private final Requests requests;
    private final Missions missions;
    private final Evidence evidence;
    private final GivingQueries queries;
    private final Audit audit;

    GivingAdminController(IdentityAccess access, Requests requests, Missions missions, Evidence evidence, GivingQueries queries, Audit audit) {
        this.access = access;
        this.requests = requests;
        this.missions = missions;
        this.evidence = evidence;
        this.queries = queries;
        this.audit = audit;
    }

    private static Map<String, String> ok(String message) { return Map.of("ok", message); }

    @GetMapping("/dashboard")
    GivingQueries.AdminDashboard dashboard(@RequestHeader(value = AUTH, required = false) String auth) {
        access.requireAdmin(auth); // relances d'échéances : tâche planifiée GivingReminders
        return queries.adminDashboard();
    }

    /* ───────── Demandes ───────── */
    @GetMapping("/requests")
    List<GivingQueries.AdminRequestRow> requests(@RequestHeader(value = AUTH, required = false) String auth,
            @RequestParam(required = false) String status, @RequestParam(required = false) String q) {
        access.requirePermission(auth, "requests.manage");
        return queries.adminRequests(status, q);
    }

    @GetMapping("/requests/target-usage")
    List<GivingQueries.TargetUsage> targetUsage(@RequestHeader(value = AUTH, required = false) String auth) {
        access.requirePermission(auth, "catalog.manage");
        return queries.targetUsage();
    }

    @GetMapping("/requests/{id}")
    GivingQueries.AdminRequestDetail request(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id) {
        return queries.adminRequest(access.requirePermission(auth, "requests.manage"), id);
    }

    record Decision(String decision, String message, Integer newAmount) {}

    @PostMapping("/requests/{id}/decision")
    Map<String, String> decide(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id, @RequestBody Decision r) {
        return ok(requests.decide(access.requirePermission(auth, "requests.manage"), id, r.decision(), r.message(), r.newAmount()));
    }

    record LinkBeneficiary(Long beneficiaryId) {}

    @PutMapping("/requests/{id}/beneficiary")
    Map<String, String> linkBeneficiary(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id, @RequestBody LinkBeneficiary r) {
        return ok(requests.linkBeneficiary(access.requirePermission(auth, "requests.manage"), id, r.beneficiaryId()));
    }

    record Finance(int executionBudget, int otherFees) {}

    @PutMapping("/requests/{id}/finance")
    Map<String, String> finance(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id, @RequestBody Finance r) {
        return ok(requests.updateFinance(access.requirePermission(auth, "payments.manage"), id, r.executionBudget(), r.otherFees()));
    }

    @PostMapping("/installments/{id}/done")
    Map<String, String> installmentDone(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id) {
        return ok(requests.installmentDone(access.requirePermission(auth, "requests.manage"), id));
    }

    @PostMapping("/requests/{id}/complete")
    Map<String, String> complete(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id) {
        return ok(requests.markCompleted(access.requirePermission(auth, "requests.manage"), id));
    }

    @PostMapping("/requests/{id}/close")
    Map<String, String> close(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id) {
        return ok(requests.close(access.requirePermission(auth, "requests.manage"), id));
    }

    /* ───────── Missions ───────── */
    @GetMapping("/missions")
    GivingQueries.AdminMissions missions(@RequestHeader(value = AUTH, required = false) String auth, @RequestParam(required = false) String status) {
        access.requirePermission(auth, "missions.manage");
        return queries.adminMissions(status);
    }

    @PostMapping("/requests/{id}/missions")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, String> createMission(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id,
            @RequestBody Missions.CreateMission r) {
        return ok(missions.create(access.requirePermission(auth, "missions.manage"), id, r));
    }

    record Reason(String reason) {}

    @PostMapping("/missions/{id}/cancel")
    Map<String, String> cancelMission(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id, @RequestBody Reason r) {
        return ok(missions.cancel(access.requirePermission(auth, "missions.manage"), id, r.reason()));
    }

    record Reassign(Long providerId) {}

    @PostMapping("/missions/{id}/reassign")
    Map<String, String> reassign(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id, @RequestBody Reassign r) {
        return ok(missions.reassign(access.requirePermission(auth, "missions.manage"), id, r.providerId()));
    }

    @PostMapping("/missions/{id}/validate")
    Map<String, String> validate(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id) {
        return ok(missions.validate(access.requirePermission(auth, "proofs.review"), id));
    }

    /* ───────── Preuves ───────── */
    @GetMapping("/proofs")
    List<GivingQueries.AdminProof> proofs(@RequestHeader(value = AUTH, required = false) String auth) {
        access.requirePermission(auth, "proofs.review");
        return queries.adminProofs();
    }

    record ProofReview(String decision, String reason, boolean hide) {}

    @PostMapping("/proofs/{id}/review")
    Map<String, String> reviewProof(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id, @RequestBody ProofReview r) {
        return ok(evidence.reviewProof(access.requirePermission(auth, "proofs.review"), id, r.decision(), r.reason(), r.hide()));
    }

    /* ───────── Paiements & finances ───────── */
    @GetMapping("/payments")
    GivingQueries.AdminPayments payments(@RequestHeader(value = AUTH, required = false) String auth,
            @RequestParam(required = false) String status, @RequestParam(required = false) String direction) {
        access.requirePermission(auth, "payments.manage");
        return queries.adminPayments(status, direction);
    }

    @PutMapping(value = "/payments/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Map<String, String> updatePayment(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id,
            @RequestParam String status, @RequestParam(required = false) Integer amountValidated, @RequestParam(required = false) Integer amountPaid,
            @RequestParam(required = false) String reference, @RequestParam(required = false) String note,
            @RequestParam(required = false) String exchangeStatus, @RequestPart(value = "receipt", required = false) MultipartFile receipt) {
        return ok(evidence.updatePayment(access.requirePermission(auth, "payments.manage"), id,
                new Evidence.PaymentUpdate(status, amountValidated, amountPaid, reference, note, exchangeStatus), receipt));
    }

    @GetMapping("/finances")
    GivingQueries.Finances finances(@RequestHeader(value = AUTH, required = false) String auth) {
        access.requirePermission(auth, "finances.view");
        return queries.finances();
    }

    /* ───────── Litiges ───────── */
    @GetMapping("/disputes")
    List<GivingQueries.AdminDispute> disputes(@RequestHeader(value = AUTH, required = false) String auth) {
        access.requirePermission(auth, "disputes.manage");
        return queries.adminDisputes();
    }

    record DisputeUpdate(String status, String decision, Long responsibleId) {}

    @PutMapping("/disputes/{id}")
    Map<String, String> updateDispute(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id, @RequestBody DisputeUpdate r) {
        return ok(evidence.updateDispute(access.requirePermission(auth, "disputes.manage"), id, r.status(), r.decision(), r.responsibleId()));
    }

    /* ───────── Bénéficiaires (données sensibles, accès tracé) ───────── */
    @GetMapping("/beneficiaries")
    List<GivingRows.BeneficiaryRow> beneficiaries(@RequestHeader(value = AUTH, required = false) String auth) {
        IdentityUser u = access.requirePermission(auth, "beneficiaries.view");
        var rows = queries.beneficiaries(true);
        audit.record(u, "beneficiary.list_view", "beneficiary", null, Map.of("count", rows.size()));
        return rows;
    }

    @PostMapping("/beneficiaries")
    @ResponseStatus(HttpStatus.CREATED)
    Map<String, String> createBeneficiary(@RequestHeader(value = AUTH, required = false) String auth, @RequestBody Evidence.BeneficiaryRequest r) {
        return ok(evidence.createBeneficiary(access.requirePermission(auth, "beneficiaries.view"), r));
    }

    record Archive(boolean restore) {}

    @PostMapping("/beneficiaries/{id}/archive")
    Map<String, String> archive(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id, @RequestBody Archive r) {
        return ok(evidence.archiveBeneficiary(access.requirePermission(auth, "beneficiaries.view"), id, r.restore()));
    }

    /* ───────── Statistiques ───────── */
    @GetMapping("/stats")
    GivingQueries.Stats stats(@RequestHeader(value = AUTH, required = false) String auth, @RequestParam(required = false) String from,
            @RequestParam(required = false) String to, @RequestParam(required = false) String status, @RequestParam(required = false) String category,
            @RequestParam(required = false) Long provider, @RequestParam(required = false) String zone) {
        access.requirePermission(auth, "stats.view");
        return queries.stats(new GivingQueries.StatFilters(from, to, status, category, provider, zone));
    }

    @GetMapping("/stats/export")
    ResponseEntity<String> export(@RequestHeader(value = AUTH, required = false) String auth, @RequestParam(required = false) String from,
            @RequestParam(required = false) String to, @RequestParam(required = false) String status, @RequestParam(required = false) String category,
            @RequestParam(required = false) Long provider, @RequestParam(required = false) String zone) {
        IdentityUser u = access.requirePermission(auth, "stats.export");
        var filters = new GivingQueries.StatFilters(from, to, status, category, provider, zone);
        String csv = queries.statsCsv(filters);
        Map<String, Object> details = new HashMap<>();
        details.put("filters", filters);
        audit.record(u, "report.export", "report", "requests", details);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=utf-8")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"menewa-rapport-demandes.csv\"")
                .body(csv);
    }
}
