package com.menewa.identity;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Informations publiques du site vitrine (aucune donnée de contact). */
@RestController
class PublicDirectoryController {
    record PublicPartner(Long id, String name, String organization, String zone) {}

    private final IdentityUserRepository users;

    PublicDirectoryController(IdentityUserRepository users) { this.users = users; }

    @GetMapping("/api/v1/public/partners")
    List<PublicPartner> partners() {
        return users.findAllByRoleAndStatusOrderByNameAsc("partner", "active").stream()
                .map(u -> new PublicPartner(u.getId(), u.getName(), u.getOrganization(), u.getZone())).toList();
    }
}
