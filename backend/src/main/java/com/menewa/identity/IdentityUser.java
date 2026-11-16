package com.menewa.identity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "identity_users")
public class IdentityUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 20)
    private String phone;
    @Column(nullable = false, length = 80)
    private String name;
    @Column(nullable = false, length = 24)
    private String role;
    @Column(nullable = false, length = 16)
    private String status = "active";
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "identity_user_permissions", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "permission", nullable = false, length = 80)
    private Set<String> permissions = new HashSet<>();
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    protected IdentityUser() {}

    IdentityUser(String phone, String name, String role) {
        this.phone = phone;
        this.name = name;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getPhone() { return phone; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public Set<String> getPermissions() { return Set.copyOf(permissions); }
    void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}
