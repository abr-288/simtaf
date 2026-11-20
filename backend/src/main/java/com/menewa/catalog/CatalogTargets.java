package com.menewa.catalog;

import com.menewa.identity.IdentityAccess;
import com.menewa.identity.IdentityUser;
import com.menewa.platform.Audit;
import com.menewa.platform.Problems;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Cibles des dons proposées aux donateurs (famille, école, orphelinat…). */
@RestController
@RequestMapping("/api/v1")
class CatalogTargets {
    record Target(String id, String label, String description, boolean active) {}
    record SaveTarget(String label, String description, boolean active) {}

    private final JdbcClient jdbc;
    private final IdentityAccess access;
    private final Audit audit;

    CatalogTargets(JdbcClient jdbc, IdentityAccess access, Audit audit) {
        this.jdbc = jdbc;
        this.access = access;
        this.audit = audit;
    }

    @GetMapping("/catalog/targets")
    List<Target> active() {
        return jdbc.sql("SELECT id, label, description, active FROM catalog_targets WHERE active = TRUE ORDER BY sort_order, label")
                .query(Target.class).list();
    }

    @GetMapping("/admin/catalog/targets")
    List<Target> all(@RequestHeader(value = "Authorization", required = false) String auth) {
        access.requirePermission(auth, "catalog.manage");
        return jdbc.sql("SELECT id, label, description, active FROM catalog_targets ORDER BY sort_order, label").query(Target.class).list();
    }

    @PostMapping("/admin/catalog/targets")
    @Transactional
    Target create(@RequestHeader(value = "Authorization", required = false) String auth, @RequestBody SaveTarget r) {
        IdentityUser u = access.requirePermission(auth, "catalog.manage");
        String label = validate(r, null);
        String id = slug(label) + "-" + Long.toString(System.currentTimeMillis(), 36);
        int next = jdbc.sql("SELECT COALESCE(MAX(sort_order), 0) + 1 FROM catalog_targets").query(Integer.class).single();
        jdbc.sql("INSERT INTO catalog_targets (id, label, description, active, sort_order) VALUES (:id, :l, :d, :a, :o)")
                .param("id", id).param("l", label).param("d", Problems.trim(r.description(), 200)).param("a", r.active()).param("o", next).update();
        audit.record(u, "target.create", "target", id, Map.of("label", label));
        return new Target(id, label, Problems.trim(r.description(), 200), r.active());
    }

    @PutMapping("/admin/catalog/targets/{id}")
    @Transactional
    Target update(@RequestHeader(value = "Authorization", required = false) String auth, @PathVariable String id, @RequestBody SaveTarget r) {
        IdentityUser u = access.requirePermission(auth, "catalog.manage");
        Target before = find(id);
        String label = validate(r, id);
        Target after = new Target(id, label, Problems.trim(r.description(), 200), r.active());
        jdbc.sql("UPDATE catalog_targets SET label = :l, description = :d, active = :a WHERE id = :id")
                .param("l", after.label()).param("d", after.description()).param("a", after.active()).param("id", id).update();
        audit.record(u, "target.update", "target", id, Map.of("from", before, "to", after));
        return after;
    }

    @DeleteMapping("/admin/catalog/targets/{id}")
    @Transactional
    Map<String, String> delete(@RequestHeader(value = "Authorization", required = false) String auth, @PathVariable String id) {
        IdentityUser u = access.requirePermission(auth, "catalog.manage");
        Target t = find(id);
        jdbc.sql("DELETE FROM catalog_targets WHERE id = :id").param("id", id).update();
        audit.record(u, "target.delete", "target", id, Map.of("label", t.label()));
        return Map.of("ok", "Cible supprimée.");
    }

    private Target find(String id) {
        return jdbc.sql("SELECT id, label, description, active FROM catalog_targets WHERE id = :id").param("id", id)
                .query(Target.class).optional().orElseThrow(() -> Problems.notFound("Cible introuvable."));
    }

    private String validate(SaveTarget r, String selfId) {
        String label = Problems.trim(r.label(), 60);
        if (label.length() < 2) throw Problems.bad("Le nom de la cible doit contenir au moins 2 caractères.");
        long dup = jdbc.sql("SELECT COUNT(*) FROM catalog_targets WHERE LOWER(label) = :l AND id <> :id")
                .param("l", label.toLowerCase(Locale.ROOT)).param("id", selfId == null ? "" : selfId).query(Long.class).single();
        if (dup > 0) throw Problems.bad("Une cible porte déjà ce nom.");
        return label;
    }

    private static String slug(String label) {
        return Normalizer.normalize(label, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
