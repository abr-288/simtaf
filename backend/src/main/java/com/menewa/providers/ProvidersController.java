package com.menewa.providers;

import com.menewa.identity.IdentityAccess;
import com.menewa.identity.IdentityPermissions;
import com.menewa.identity.IdentityUser;
import com.menewa.platform.Audit;
import com.menewa.platform.Problems;
import com.menewa.rewards.Reputation;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
class ProvidersController {
    private final ProviderDirectory providers;
    private final IdentityAccess access;
    private final JdbcClient jdbc;
    private final Reputation reputation;
    private final Audit audit;

    ProvidersController(ProviderDirectory providers, IdentityAccess access, JdbcClient jdbc, Reputation reputation, Audit audit) {
        this.providers = providers;
        this.access = access;
        this.jdbc = jdbc;
        this.reputation = reputation;
        this.audit = audit;
    }

    record MyProvider(ProviderDirectory.Profile profile, List<ProviderDirectory.Service> services, List<Reputation.Review> reviews, long reviewCount) {}

    @GetMapping("/providers/me")
    @Transactional
    MyProvider me(@RequestHeader(value = "Authorization", required = false) String auth) {
        IdentityUser u = access.requireRole(auth, "provider");
        providers.ensureProfile(u.getId());
        var profile = jdbc.sql("SELECT user_id, category, description, zone, available, verified FROM providers_profiles WHERE user_id = :u")
                .param("u", u.getId()).query(ProviderDirectory.Profile.class).single();
        var services = jdbc.sql("SELECT id, provider_id, label, category, unit_cost, unit, description, active FROM providers_services WHERE provider_id = :u AND active = TRUE ORDER BY id")
                .param("u", u.getId()).query(ProviderDirectory.Service.class).list();
        return new MyProvider(profile, services, reputation.receivedBy(u.getId(), 8), reputation.countReceivedBy(u.getId()));
    }

    record ProfileRequest(String category, String description, String zone, boolean available) {}

    @PutMapping("/providers/me")
    @Transactional
    Map<String, String> updateMe(@RequestHeader(value = "Authorization", required = false) String auth, @RequestBody ProfileRequest r) {
        IdentityUser u = access.requireRole(auth, "provider");
        providers.ensureProfile(u.getId());
        jdbc.sql("UPDATE providers_profiles SET category = :c, description = :d, zone = :z, available = :a WHERE user_id = :u")
                .param("c", r.category() == null || r.category().isBlank() ? "autre" : Problems.trim(r.category(), 30))
                .param("d", Problems.trimToNull(r.description(), 800)).param("z", Problems.trimToNull(r.zone(), 100))
                .param("a", r.available()).param("u", u.getId()).update();
        return Map.of("ok", "Profil prestataire mis à jour.");
    }

    record ServiceRequest(String label, String category, int unitCost, String unit, String description) {}

    @PostMapping("/providers/me/services")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    Map<String, String> addService(@RequestHeader(value = "Authorization", required = false) String auth, @RequestBody ServiceRequest r) {
        IdentityUser u = access.requireRole(auth, "provider");
        String label = Problems.trim(r.label(), 150);
        if (label.length() < 3) throw Problems.bad("Intitulé requis.");
        jdbc.sql("INSERT INTO providers_services (provider_id, label, category, unit_cost, unit, description) VALUES (:p, :l, :c, :cost, :unit, :d)")
                .param("p", u.getId()).param("l", label).param("c", r.category() == null || r.category().isBlank() ? "autre" : Problems.trim(r.category(), 30))
                .param("cost", Math.max(r.unitCost(), 0)).param("unit", r.unit() == null || r.unit().isBlank() ? "prestation" : Problems.trim(r.unit(), 40))
                .param("d", Problems.trimToNull(r.description(), 400)).update();
        return Map.of("ok", "Service ajouté.");
    }

    @DeleteMapping("/providers/me/services/{id}")
    @Transactional
    Map<String, String> removeService(@RequestHeader(value = "Authorization", required = false) String auth, @PathVariable long id) {
        IdentityUser u = access.requireRole(auth, "provider");
        jdbc.sql("UPDATE providers_services SET active = FALSE WHERE id = :id AND provider_id = :p").param("id", id).param("p", u.getId()).update();
        return Map.of("ok", "Service retiré.");
    }

    @GetMapping("/providers/search")
    List<ProviderDirectory.ProviderCard> search(@RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam(required = false) String q, @RequestParam(required = false) String cat, @RequestParam(required = false) String zone) {
        IdentityUser u = access.requireUser(auth);
        if (IdentityPermissions.isAdmin(u)) throw Problems.forbidden("Les administrateurs ne peuvent pas commander de service.");
        return providers.cards(true, q, cat, zone).stream().filter(p -> !p.id().equals(u.getId()))
                .map(p -> new ProviderDirectory.ProviderCard(p.id(), p.name(), null, p.zone(), p.category(), p.description(), p.available(),
                        p.verified(), p.rating(), p.reviews(), p.services()))
                .toList();
    }

    /** Annuaire public du Social Network : profil, note et services, sans téléphone. */
    @GetMapping("/public/providers")
    List<ProviderDirectory.ProviderCard> publicProviders(@RequestParam(required = false) String q, @RequestParam(required = false) String cat) {
        return providers.cards(true, q, cat, null).stream()
                .map(p -> new ProviderDirectory.ProviderCard(p.id(), p.name(), null, p.zone(), p.category(), p.description(), p.available(),
                        p.verified(), p.rating(), p.reviews(), p.services()))
                .toList();
    }

    /* ───────── Administration ───────── */
    @GetMapping("/admin/providers")
    List<ProviderDirectory.ProviderCard> adminList(@RequestHeader(value = "Authorization", required = false) String auth) {
        access.requirePermission(auth, "providers.manage");
        return providers.cards(false, null, null, null);
    }

    @GetMapping("/admin/providers/options")
    List<ProviderDirectory.ProviderOption> options(@RequestHeader(value = "Authorization", required = false) String auth) {
        access.requireAdmin(auth);
        return providers.activeOptions();
    }

    /** Répertoire des prestataires et coûts (page Finances). */
    @GetMapping("/admin/providers/directory")
    List<ProviderDirectory.ProviderCard> directory(@RequestHeader(value = "Authorization", required = false) String auth) {
        access.requirePermission(auth, "finances.view");
        return providers.cards(false, null, null, null);
    }

    record VerifyRequest(boolean verified) {}

    @PatchMapping("/admin/providers/{id}/verified")
    @Transactional
    Map<String, String> verify(@RequestHeader(value = "Authorization", required = false) String auth, @PathVariable long id, @RequestBody VerifyRequest r) {
        IdentityUser u = access.requirePermission(auth, "providers.manage");
        int n = jdbc.sql("UPDATE providers_profiles SET verified = :v WHERE user_id = :u").param("v", r.verified()).param("u", id).update();
        if (n == 0) throw Problems.notFound("Prestataire introuvable.");
        audit.record(u, r.verified() ? "provider.verify" : "provider.unverify", "user", id);
        return Map.of("ok", r.verified() ? "Prestataire vérifié." : "Vérification retirée.");
    }
}
