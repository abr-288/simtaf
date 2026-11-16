package com.menewa.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "identity_sessions")
class IdentitySession {
    @Id
    @Column(name = "token_hash", length = 64)
    private String tokenHash;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private IdentityUser user;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected IdentitySession() {}
    IdentitySession(String tokenHash, IdentityUser user, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.user = user;
        this.expiresAt = expiresAt;
    }
    IdentityUser getUser() { return user; }
    Instant getExpiresAt() { return expiresAt; }
}
