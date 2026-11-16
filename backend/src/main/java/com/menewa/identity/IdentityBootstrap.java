package com.menewa.identity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class IdentityBootstrap implements ApplicationRunner {
    private final IdentityUserRepository users;
    private final String adminPhone;
    private final String adminName;

    IdentityBootstrap(IdentityUserRepository users,
            @Value("${menewa.identity.bootstrap-admin-phone:}") String adminPhone,
            @Value("${menewa.identity.bootstrap-admin-name:Administrateur Menewa}") String adminName) {
        this.users = users;
        this.adminPhone = adminPhone;
        this.adminName = adminName;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!adminPhone.isBlank()) {
            String phone = IdentityService.normalizePhone(adminPhone);
            users.findByPhone(phone).orElseGet(() -> users.save(new IdentityUser(phone, adminName, "admin")));
        }
    }
}
