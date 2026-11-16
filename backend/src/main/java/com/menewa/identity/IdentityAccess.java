package com.menewa.identity;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Component
public class IdentityAccess {
    private final IdentityService identity;

    IdentityAccess(IdentityService identity) { this.identity = identity; }

    public IdentityUser requireUser(String authorization) {
        return identity.currentUser(authorization);
    }

    public IdentityUser requirePermission(String authorization, String permission) {
        IdentityUser user = requireUser(authorization);
        if (!IdentityPermissions.has(user, permission)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "forbidden");
        return user;
    }
}
