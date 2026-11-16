package com.menewa.catalog;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
class CatalogBootstrap implements ApplicationRunner {
    private final CatalogRepository catalog;
    private final boolean seedDemoData;

    CatalogBootstrap(CatalogRepository catalog, @Value("${menewa.catalog.seed-demo-data:false}") boolean seedDemoData) {
        this.catalog = catalog;
        this.seedDemoData = seedDemoData;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!seedDemoData || catalog.count() > 0) return;
        catalog.saveAll(List.of(
                new CatalogItem("pack-alimentaire-famille", "Pack alimentaire famille", "nourriture", "don",
                        "Riz, huile, haricots et farine pour une famille pendant un mois.", 15000, 12000, "pack", "🍚", true),
                new CatalogItem("kit-scolaire", "Kit de fournitures scolaires", "fournitures", "don",
                        "Cahiers, stylos, ardoise et sac pour la rentrée.", 7500, 6000, "kit", "🎒", true),
                new CatalogItem("visite-personne", "Visite à une personne isolée", "action", "action",
                        "Un bénévole rend visite à une personne âgée, malade ou isolée.", 5000, 3000, "visite", "🤝", true)));
    }
}
