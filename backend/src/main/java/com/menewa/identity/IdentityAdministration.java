package com.menewa.identity;

import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Administration des comptes : liste, statut (archivage, jamais de suppression), rôles et permissions. */
@RestController
@RequestMapping("/api/v1/admin/users")
class IdentityAdministration {
    private final IdentityAccess access;
    private final AdminUsers service;

    IdentityAdministration(IdentityAccess access, AdminUsers service) {
        this.access = access;
        this.service = service;
    }

    @GetMapping
    List<AdminUserView> list(@RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) String q, @RequestParam(required = false) String role) {
        IdentityUser me = access.requireAdmin(authorization);
        if (!IdentityPermissions.has(me, "users.manage") && !IdentityPermissions.has(me, "roles.manage"))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Permission insuffisante pour cette action.");
        return service.list(q, role);
    }

    /** Liste légère (sélecteurs de prestataires, responsables, ajustements) pour tout administrateur. */
    @GetMapping("/options")
    List<UserOption> options(@RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) List<String> role) {
        access.requireAdmin(authorization);
        return service.options(role);
    }

    @PatchMapping("/{id}/status")
    AdminUserView setStatus(@RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable long id, @Valid @RequestBody SetStatusRequest request) {
        IdentityUser me = access.requirePermission(authorization, "users.manage");
        return service.setStatus(me, id, request.status());
    }

    @PutMapping("/{id}/role")
    AdminUserView setRole(@RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable long id, @Valid @RequestBody SetRoleRequest request) {
        IdentityUser me = access.requirePermission(authorization, "roles.manage");
        return service.setRole(me, id, request.role(), request.permissions());
    }
}

@Service
class AdminUsers {
    private final IdentityUserRepository users;
    private final ApplicationEventPublisher events;

    AdminUsers(IdentityUserRepository users, ApplicationEventPublisher events) {
        this.users = users;
        this.events = events;
    }

    @Transactional(readOnly = true)
    List<AdminUserView> list(String q, String role) {
        String needle = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        return users.findAll().stream()
                .filter(u -> role == null || role.isBlank() || role.equals(u.getRole()))
                .filter(u -> needle.isEmpty() || u.getPhone().contains(needle) || (u.getName() != null && u.getName().toLowerCase(Locale.ROOT).contains(needle)))
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .limit(300).map(AdminUserView::from).toList();
    }

    @Transactional(readOnly = true)
    List<UserOption> options(List<String> roles) {
        var list = roles == null || roles.isEmpty() ? users.findAll() : users.findAllByRoleInOrderByNameAsc(roles);
        return list.stream().filter(u -> "active".equals(u.getStatus())).map(UserOption::from).toList();
    }

    @Transactional
    AdminUserView setStatus(IdentityUser me, long id, String status) {
        if (!Set.of("active", "suspended", "archived").contains(status)) throw bad("Statut invalide.");
        if (me.getId() == id) throw bad("Vous ne pouvez pas modifier votre propre statut.");
        IdentityUser target = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable."));
        if ("admin".equals(target.getRole()) && !"admin".equals(me.getRole()))
            throw bad("Seul un administrateur principal peut modifier un administrateur.");
        target.setStatus(status);
        events.publishEvent(new IdentityEvents.IdentityAudited(me.getId(), me.getRole(), "user.status", "user", String.valueOf(id), Map.of("status", status)));
        return AdminUserView.from(target);
    }

    @Transactional
    AdminUserView setRole(IdentityUser me, long id, String role, List<String> requested) {
        if (!IdentityPermissions.ROLES.contains(role)) throw bad("Rôle invalide.");
        IdentityUser target = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable."));
        if (target.getId().equals(me.getId()) && !"admin".equals(role)) throw bad("Vous ne pouvez pas retirer votre propre rôle d'administrateur.");
        if ("admin".equals(target.getRole()) && !"admin".equals(role) && users.countByRoleAndStatus("admin", "active") <= 1)
            throw bad("Impossible : il doit rester au moins un administrateur actif.");
        Set<String> perms = new HashSet<>();
        if ("subadmin".equals(role) && requested != null)
            requested.stream().filter(IdentityPermissions.ALL::contains).filter(p -> !"roles.manage".equals(p)).forEach(perms::add);
        String from = target.getRole();
        target.setRole(role, perms);
        events.publishEvent(new IdentityEvents.UserRoleChanged(id, from, role));
        events.publishEvent(new IdentityEvents.IdentityAudited(me.getId(), me.getRole(), "user.role", "user", String.valueOf(id),
                Map.of("from", from, "to", role, "permissions", List.copyOf(perms))));
        return AdminUserView.from(target);
    }

    private static ResponseStatusException bad(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
}
