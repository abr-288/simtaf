package com.menewa.catalog;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
class CatalogManagement {
    private final CatalogRepository catalog;

    CatalogManagement(CatalogRepository catalog) { this.catalog = catalog; }

    List<CatalogItemResponse> publicCatalog() {
        return catalog.findAllByActiveTrueOrderByCategoryAscTitleAsc().stream().map(CatalogItemResponse::from).toList();
    }

    List<ManagedCatalogItemResponse> managedCatalog() {
        return catalog.findAllByOrderByCategoryAscTitleAsc().stream().map(ManagedCatalogItemResponse::from).toList();
    }

    @Transactional
    ManagedCatalogItemResponse create(SaveCatalogItemRequest request) {
        validate(request);
        String slug = slug(request.title());
        if (catalog.existsBySlug(slug)) slug = slug + "-" + UUID.randomUUID().toString().substring(0, 8);
        return ManagedCatalogItemResponse.from(catalog.save(new CatalogItem(slug, request.title().trim(),
                request.category().trim(), kind(request.category()), normalize(request.description()), request.unitPrice(),
                request.costEstimate(), normalizedUnit(request.unit()), normalizedEmoji(request.emoji()), request.active())));
    }

    @Transactional
    ManagedCatalogItemResponse update(long id, SaveCatalogItemRequest request) {
        validate(request);
        CatalogItem item = catalog.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "catalog_item_not_found"));
        item.update(request.title().trim(), request.category().trim(), kind(request.category()), normalize(request.description()),
                request.unitPrice(), request.costEstimate(), normalizedUnit(request.unit()), normalizedEmoji(request.emoji()), request.active());
        return ManagedCatalogItemResponse.from(item);
    }

    @Transactional
    ManagedCatalogItemResponse setActive(long id, boolean active) {
        CatalogItem item = catalog.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "catalog_item_not_found"));
        item.setActive(active);
        return ManagedCatalogItemResponse.from(item);
    }

    private static void validate(SaveCatalogItemRequest r) {
        if (r.title().trim().length() < 3 || r.category().isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid_catalog_item");
    }

    private static String kind(String category) { return "action".equals(category.trim()) ? "action" : "don"; }
    private static String normalize(String text) { return text == null || text.isBlank() ? null : text.trim(); }
    private static String normalizedUnit(String unit) { return unit == null || unit.isBlank() ? "unité" : unit.trim(); }
    private static String normalizedEmoji(String emoji) { return emoji == null || emoji.isBlank() ? "🎁" : emoji.trim(); }
    private static String slug(String title) {
        return Normalizer.normalize(title.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
