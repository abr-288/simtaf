plugins {
    java
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.menewa"
version = "0.1.0-SNAPSHOT"

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }


extra["springModulithVersion"] = "1.4.3"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.flywaydb:flyway-core")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.modulith:spring-modulith-starter-core")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
    implementation("org.springframework.modulith:spring-modulith-starter-jdbc")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.modulith:spring-modulith-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // Vrai PostgreSQL 16 pour les tests, sans Docker (binaires embarqués, Windows/Linux/macOS).
    testImplementation("io.zonky.test:embedded-postgres:2.1.0")
    testImplementation(platform("io.zonky.test.postgres:embedded-postgres-binaries-bom:16.4.0"))
}

dependencyManagement {
    imports { mavenBom("org.springframework.modulith:spring-modulith-bom:${property("springModulithVersion")}") }
}

tasks.withType<Test> { useJUnitPlatform() }
