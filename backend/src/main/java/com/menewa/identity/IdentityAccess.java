package com.menewa.identity;

import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Contrôle d'accès utilisé par les contrôleurs de tous les modules (jeton Bearer). */
@Component
public class IdentityAccess {
    private final IdentityService identity;

    IdentityAccess(IdentityService identity) { this.identity = identity; }

    public IdentityUser requireUser(String authorization) {
        return identity.currentUser(authorization);
    }

    public IdentityUser requirePermission(String authorization, String permission) {
        IdentityUser user = requireUser(authorization);
        if (!IdentityPermissions.has(user, permission))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Permission insuffisante pour cette action.");
        return user;
    }

    public IdentityUser requireAdmin(String authorization) {
        IdentityUser user = requireUser(authorization);
        if (!IdentityPermissions.isAdmin(user)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès réservé à l'administration.");
        return user;
    }

    public IdentityUser requireRole(String authorization, String... roles) {
        IdentityUser user = requireUser(authorization);
        if (Arrays.stream(roles).noneMatch(r -> r.equals(user.getRole())))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Action non disponible pour votre profil.");
        return user;
    }

    public UserRef ref(IdentityUser user) { return UserRef.from(user); }
}
