package com.menewa.identity;

import java.util.Set;

public final class IdentityPermissions {
    public static final Set<String> ALL = Set.of(
            "requests.manage", "users.manage", "providers.manage", "beneficiaries.view",
            "missions.manage", "proofs.review", "payments.manage", "finances.view",
            "disputes.manage", "stats.view", "stats.export", "tokens.manage",
            "catalog.manage", "listings.moderate", "audit.view", "roles.manage", "settings.manage");

    private IdentityPermissions() {}

    public static boolean has(IdentityUser user, String permission) {
        if (user == null || !ALL.contains(permission)) return false;
        if ("admin".equals(user.getRole())) return true;
        return "subadmin".equals(user.getRole()) && user.getPermissions().contains(permission);
    }

    public static boolean isAdmin(IdentityUser user) {
        return user != null && Set.of("admin", "subadmin").contains(user.getRole());
    }
}
