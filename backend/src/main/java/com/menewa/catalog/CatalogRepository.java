package com.menewa.catalog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface CatalogRepository extends JpaRepository<CatalogItem, Long> {
    List<CatalogItem> findAllByActiveTrueOrderByCategoryAscTitleAsc();
    List<CatalogItem> findAllByOrderByCategoryAscTitleAsc();
    boolean existsBySlug(String slug);
}
