package com.menewa.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "menewa_catalog_items")
class CatalogItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 180)
    private String slug;
    @Column(nullable = false, length = 120)
    private String title;
    @Column(nullable = false, length = 40)
    private String category;
    @Column(nullable = false, length = 20)
    private String kind;
    @Column(length = 1000)
    private String description;
    @Column(name = "unit_price", nullable = false)
    private int unitPrice;
    @Column(name = "cost_estimate", nullable = false)
    private int costEstimate;
    @Column(nullable = false, length = 30)
    private String unit;
    @Column(length = 12)
    private String emoji;
    @Column(nullable = false)
    private boolean active;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected CatalogItem() {}

    CatalogItem(String slug, String title, String category, String kind, String description,
            int unitPrice, int costEstimate, String unit, String emoji, boolean active) {
        this.slug = slug;
        update(title, category, kind, description, unitPrice, costEstimate, unit, emoji, active);
    }

    void update(String title, String category, String kind, String description,
            int unitPrice, int costEstimate, String unit, String emoji, boolean active) {
        this.title = title;
        this.category = category;
        this.kind = kind;
        this.description = description;
        this.unitPrice = unitPrice;
        this.costEstimate = costEstimate;
        this.unit = unit;
        this.emoji = emoji;
        this.active = active;
    }

    void setActive(boolean active) { this.active = active; }
    Long getId() { return id; }
    String getSlug() { return slug; }
    String getTitle() { return title; }
    String getCategory() { return category; }
    String getKind() { return kind; }
    String getDescription() { return description; }
    int getUnitPrice() { return unitPrice; }
    int getCostEstimate() { return costEstimate; }
    String getUnit() { return unit; }
    String getEmoji() { return emoji; }
    boolean isActive() { return active; }
    Instant getCreatedAt() { return createdAt; }
}
