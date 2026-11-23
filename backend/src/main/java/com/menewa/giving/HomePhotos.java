package com.menewa.giving;

import com.menewa.identity.IdentityAccess;
import com.menewa.identity.IdentityUser;
import com.menewa.platform.Audit;
import com.menewa.platform.Problems;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Photos de la page d'accueil (site public et application mobile). L'administration les téléverse
 * (permission settings.manage) ; elles sont ensuite servies publiquement. Le stockage est celui des preuves
 * (signature du contenu vérifiée), limité ici aux images.
 */
@RestController
@RequestMapping("/api/v1")
class HomePhotos {
    /** Emplacements de la page d'accueil, dans l'ordre d'affichage. */
    static final Map<String, String> SLOTS = new LinkedHashMap<>();
    static {
        SLOTS.put("hero", "Bandeau principal");
        SLOTS.put("kitchen", "Social Kitchen");
        SLOTS.put("network", "Social Network");
        SLOTS.put("man", "Témoignage partenaire");
        SLOTS.put("woman", "Témoignage donatrice");
    }
    private static final Set<String> IMAGES = Set.of("image/jpeg", "image/png", "image/webp");

    private final JdbcClient jdbc;
    private final Storage storage;
    private final IdentityAccess access;
    private final Audit audit;

    HomePhotos(JdbcClient jdbc, Storage storage, IdentityAccess access, Audit audit) {
        this.jdbc = jdbc;
        this.storage = storage;
        this.access = access;
        this.audit = audit;
    }

    record Row(String slot, String fileKey, String mime, String alt, OffsetDateTime updatedAt) {}

    /** {@code version} change à chaque remplacement : à ajouter à l'URL pour contourner les caches. */
    record HomePhoto(String slot, String label, boolean available, String alt, Long version) {}

    private Map<String, Row> rows() {
        Map<String, Row> map = new LinkedHashMap<>();
        jdbc.sql("SELECT slot, file_key, mime, alt, updated_at FROM giving_home_photos").query(Row.class).list().forEach(r -> map.put(r.slot(), r));
        return map;
    }

    @GetMapping("/public/home-photos")
    List<HomePhoto> list() {
        var rows = rows();
        return SLOTS.entrySet().stream().map(e -> {
            Row r = rows.get(e.getKey());
            return new HomePhoto(e.getKey(), e.getValue(), r != null, r == null ? null : r.alt(), r == null ? null : r.updatedAt().toEpochSecond());
        }).toList();
    }

    @GetMapping("/public/home-photos/{slot}")
    ResponseEntity<byte[]> photo(@PathVariable String slot) throws IOException {
        Row r = rows().get(slot);
        if (r == null) throw Problems.notFound("Photo introuvable.");
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(r.mime()))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .body(storage.read(r.fileKey()));
    }

    @PutMapping(value = "/admin/home-photos/{slot}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    Map<String, String> upload(@RequestHeader(value = "Authorization", required = false) String auth, @PathVariable String slot,
            @RequestPart(value = "file", required = false) MultipartFile file, @RequestParam(required = false) String alt) {
        IdentityUser u = access.requirePermission(auth, "settings.manage");
        if (!SLOTS.containsKey(slot)) throw Problems.notFound("Emplacement inconnu.");
        if (file == null || file.isEmpty()) throw Problems.bad("Sélectionnez une photo.");
        try {
            if (!IMAGES.contains(Storage.sniff(file.getBytes()))) throw Problems.bad("Format non autorisé : JPG, PNG ou WEBP uniquement.");
        } catch (IOException e) {
            throw Problems.bad("Fichier illisible.");
        }
        var saved = storage.save(file, "home");
        String description = Problems.trimToNull(alt, 200);
        jdbc.sql("""
                INSERT INTO giving_home_photos (slot, file_key, mime, alt, updated_by, updated_at) VALUES (:s, :k, :m, :a, :u, :now)
                ON CONFLICT (slot) DO UPDATE SET file_key = :k, mime = :m, alt = :a, updated_by = :u, updated_at = :now""")
                .param("s", slot).param("k", saved.key()).param("m", saved.mime()).param("a", description)
                .param("u", u.getId()).param("now", GivingDb.now()).update();
        audit.record(u, "settings.home_photo", "home_photo", slot, Map.of("file", saved.name()));
        return Map.of("ok", "Photo « " + SLOTS.get(slot) + " » mise à jour.");
    }

    @DeleteMapping("/admin/home-photos/{slot}")
    @Transactional
    Map<String, String> remove(@RequestHeader(value = "Authorization", required = false) String auth, @PathVariable String slot) {
        IdentityUser u = access.requirePermission(auth, "settings.manage");
        if (!SLOTS.containsKey(slot)) throw Problems.notFound("Emplacement inconnu.");
        jdbc.sql("DELETE FROM giving_home_photos WHERE slot = :s").param("s", slot).update();
        audit.record(u, "settings.home_photo_remove", "home_photo", slot);
        return Map.of("ok", "Photo retirée.");
    }
}
