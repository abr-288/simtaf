package com.menewa.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "identity_otp_challenges")
class OtpChallenge {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 20)
    private String phone;
    @Column(name = "code_hash", nullable = false, length = 64)
    private String codeHash;
    @Column(name = "registration_name", length = 80)
    private String registrationName;
    @Column(name = "registration_role", length = 24)
    private String registrationRole;
    @Column(nullable = false)
    private int attempts;
    @Column(nullable = false)
    private boolean consumed;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected OtpChallenge() {}

    OtpChallenge(String phone, String codeHash, String name, String role, Instant expiresAt) {
        this.phone = phone;
        this.codeHash = codeHash;
        this.registrationName = name;
        this.registrationRole = role;
        this.expiresAt = expiresAt;
    }

    String getPhone() { return phone; }
    String getCodeHash() { return codeHash; }
    String getRegistrationName() { return registrationName; }
    String getRegistrationRole() { return registrationRole; }
    int getAttempts() { return attempts; }
    Instant getExpiresAt() { return expiresAt; }
    boolean isConsumed() { return consumed; }
    void incrementAttempts() { attempts++; }
    void consume() { consumed = true; }
}
