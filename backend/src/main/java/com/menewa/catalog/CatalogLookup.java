package com.menewa.catalog;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Lecture du catalogue par les autres modules (prix et coûts de référence côté serveur). */
@Component
@Transactional(readOnly = true)
public class CatalogLookup {
    public record CatalogEntry(Long id, String title, String category, String kind, int unitPrice, int costEstimate,
            String unit, String emoji, boolean active) {
        static CatalogEntry from(CatalogItem i) {
            return new CatalogEntry(i.getId(), i.getTitle(), i.getCategory(), i.getKind(), i.getUnitPrice(), i.getCostEstimate(),
                    i.getUnit(), i.getEmoji(), i.isActive());
        }
    }

    private final CatalogRepository catalog;

    CatalogLookup(CatalogRepository catalog) { this.catalog = catalog; }

    public Optional<CatalogEntry> findActive(long id) {
        return catalog.findById(id).filter(CatalogItem::isActive).map(CatalogEntry::from);
    }

    public Map<Long, CatalogEntry> findAll(Collection<Long> ids) {
        Map<Long, CatalogEntry> out = new LinkedHashMap<>();
        catalog.findAllById(ids).forEach(i -> out.put(i.getId(), CatalogEntry.from(i)));
        return out;
    }
}
