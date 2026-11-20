package com.menewa.identity;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Annuaire en lecture seule : API publique du module identity pour les autres modules. */
@Component
@Transactional(readOnly = true)
public class IdentityDirectory {
    private final IdentityUserRepository users;

    IdentityDirectory(IdentityUserRepository users) { this.users = users; }

    public Optional<UserRef> find(Long id) {
        if (id == null) return Optional.empty();
        return users.findById(id).map(UserRef::from);
    }

    public Map<Long, UserRef> findAll(Collection<Long> ids) {
        Map<Long, UserRef> out = new LinkedHashMap<>();
        var wanted = ids.stream().filter(Objects::nonNull).distinct().toList();
        if (wanted.isEmpty()) return out;
        users.findAllById(wanted).forEach(u -> out.put(u.getId(), UserRef.from(u)));
        return out;
    }

    /** Libellé d'affichage (nom ou téléphone) ou « — ». */
    public String nameOf(Long id) {
        return find(id).map(UserRef::displayName).orElse("—");
    }

    public Optional<UserRef> findActiveByPhone(String rawPhone) {
        String phone;
        try { phone = IdentityService.normalizePhone(rawPhone); } catch (RuntimeException ex) { return Optional.empty(); }
        return users.findByPhone(phone).filter(u -> "active".equals(u.getStatus())).map(UserRef::from);
    }

    public List<UserRef> activeWithRole(String role) {
        return users.findAllByRoleAndStatusOrderByNameAsc(role, "active").stream().map(UserRef::from).toList();
    }

    public List<UserRef> withRoles(Collection<String> roles) {
        return users.findAllByRoleInOrderByNameAsc(roles).stream().map(UserRef::from).toList();
    }

    /** Administrateurs actifs disposant de la permission (destinataires des notifications d'administration). */
    public List<UserRef> administratorsWith(String permission) {
        return users.findAllByRoleInAndStatus(List.of("admin", "subadmin"), "active").stream()
                .filter(u -> IdentityPermissions.has(u, permission)).map(UserRef::from).toList();
    }

    public List<UserRef> all() {
        return users.findAll().stream().map(UserRef::from).toList();
    }

    public long count() { return users.count(); }
}
