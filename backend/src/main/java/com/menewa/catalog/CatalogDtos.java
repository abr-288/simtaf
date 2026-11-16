package com.menewa.catalog;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

record SaveCatalogItemRequest(
        @NotBlank @Size(max = 120) String title,
        @NotBlank @Size(max = 40) String category,
        @Size(max = 1000) String description,
        @Min(0) int unitPrice,
        @Min(0) int costEstimate,
        @Size(max = 30) String unit,
        @Size(max = 12) String emoji,
        boolean active) {}

record SetCatalogItemActiveRequest(boolean active) {}

record CatalogItemResponse(Long id, String slug, String title, String category, String kind,
        String description, int unitPrice, String unit, String emoji) {
    static CatalogItemResponse from(CatalogItem item) {
        return new CatalogItemResponse(item.getId(), item.getSlug(), item.getTitle(), item.getCategory(),
                item.getKind(), item.getDescription(), item.getUnitPrice(), item.getUnit(), item.getEmoji());
    }
}

record ManagedCatalogItemResponse(Long id, String slug, String title, String category, String kind,
        String description, int unitPrice, int costEstimate, String unit, String emoji, boolean active, Instant createdAt) {
    static ManagedCatalogItemResponse from(CatalogItem item) {
        return new ManagedCatalogItemResponse(item.getId(), item.getSlug(), item.getTitle(), item.getCategory(),
                item.getKind(), item.getDescription(), item.getUnitPrice(), item.getCostEstimate(), item.getUnit(),
                item.getEmoji(), item.isActive(), item.getCreatedAt());
    }
}

record CatalogResponse(String currency, java.util.List<CatalogItemResponse> items) {}
