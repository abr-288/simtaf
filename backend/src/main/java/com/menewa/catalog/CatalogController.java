package com.menewa.catalog;

import com.menewa.identity.IdentityAccess;
import com.menewa.identity.IdentityUser;
import com.menewa.platform.Audit;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CatalogController {
    private final CatalogManagement catalog;
    private final IdentityAccess identity;
    private final Audit audit;

    CatalogController(CatalogManagement catalog, IdentityAccess identity, Audit audit) {
        this.catalog = catalog;
        this.identity = identity;
        this.audit = audit;
    }

    @GetMapping({"/catalog", "/v1/catalog"})
    CatalogResponse publicCatalog() { return new CatalogResponse("XOF", catalog.publicCatalog()); }

    @GetMapping({"/admin/catalog", "/v1/admin/catalog"})
    List<ManagedCatalogItemResponse> managedCatalog(@RequestHeader(value = "Authorization", required = false) String authorization) {
        identity.requirePermission(authorization, "catalog.manage");
        return catalog.managedCatalog();
    }

    @PostMapping({"/admin/catalog", "/v1/admin/catalog"})
    @ResponseStatus(HttpStatus.CREATED)
    ManagedCatalogItemResponse create(@RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody SaveCatalogItemRequest request) {
        IdentityUser user = identity.requirePermission(authorization, "catalog.manage");
        ManagedCatalogItemResponse created = catalog.create(request);
        audit.record(user, "catalog.create", "catalog_item", created.id());
        return created;
    }

    @PutMapping({"/admin/catalog/{id}", "/v1/admin/catalog/{id}"})
    ManagedCatalogItemResponse update(@RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable long id, @Valid @RequestBody SaveCatalogItemRequest request) {
        IdentityUser user = identity.requirePermission(authorization, "catalog.manage");
        ManagedCatalogItemResponse updated = catalog.update(id, request);
        audit.record(user, "catalog.update", "catalog_item", id, Map.of("price", request.unitPrice(), "cost", request.costEstimate()));
        return updated;
    }

    @PatchMapping({"/admin/catalog/{id}/active", "/v1/admin/catalog/{id}/active"})
    ManagedCatalogItemResponse setActive(@RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable long id, @RequestBody SetCatalogItemActiveRequest request) {
        IdentityUser user = identity.requirePermission(authorization, "catalog.manage");
        ManagedCatalogItemResponse updated = catalog.setActive(id, request.active());
        audit.record(user, "catalog.toggle", "catalog_item", id, Map.of("active", request.active()));
        return updated;
    }
}
