package com.menewa;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {
    @Test
    void modulesShouldRespectBoundaries() {
        ApplicationModules.of(MenewaApplication.class).verify();
    }
}
