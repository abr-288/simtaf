package com.menewa.identity;

import java.util.Set;

/** Vue publique et immuable d'un utilisateur, exposée aux autres modules. */
public record UserRef(Long id, String phone, String name, String role, String status, Set<String> permissions,
        String zone, String organization, boolean notifInapp, boolean notifSms, boolean notifEmail) {

    static UserRef from(IdentityUser u) {
        return new UserRef(u.getId(), u.getPhone(), u.getName(), u.getRole(), u.getStatus(), u.getPermissions(),
                u.getZone(), u.getOrganization(), u.isNotifInapp(), u.isNotifSms(), u.isNotifEmail());
    }

    public String displayName() { return name == null || name.isBlank() ? phone : name; }

    public boolean active() { return "active".equals(status); }
}
