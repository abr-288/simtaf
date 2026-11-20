package com.menewa.identity;

import java.util.Set;

public final class IdentityPermissions {
    public static final Set<String> ALL = Set.of(
            "requests.manage", "users.manage", "providers.manage", "beneficiaries.view",
            "missions.manage", "proofs.review", "payments.manage", "finances.view",
            "disputes.manage", "stats.view", "stats.export", "tokens.manage",
            "catalog.manage", "listings.moderate", "audit.view", "roles.manage", "settings.manage");

    public static final Set<String> ROLES = Set.of("donor", "client", "provider", "partner", "beneficiary", "subadmin", "admin");

    private IdentityPermissions() {}

    /** L'administrateur principal a toutes les permissions ; un secondaire uniquement celles qui lui sont accordées, jamais `roles.manage`. */
    public static boolean has(IdentityUser user, String permission) {
        if (user == null || !ALL.contains(permission)) return false;
        if ("admin".equals(user.getRole())) return true;
        return "subadmin".equals(user.getRole()) && !"roles.manage".equals(permission) && user.getPermissions().contains(permission);
    }

    public static boolean has(UserRef user, String permission) {
        if (user == null || !ALL.contains(permission)) return false;
        if ("admin".equals(user.role())) return true;
        return "subadmin".equals(user.role()) && !"roles.manage".equals(permission) && user.permissions().contains(permission);
    }

    public static boolean isAdmin(IdentityUser user) {
        return user != null && isAdminRole(user.getRole());
    }

    public static boolean isAdminRole(String role) {
        return "admin".equals(role) || "subadmin".equals(role);
    }

    public static boolean isRequester(String role) {
        return "donor".equals(role) || "client".equals(role);
    }
}
