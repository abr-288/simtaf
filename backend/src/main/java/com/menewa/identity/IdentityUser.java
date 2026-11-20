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
    @Column(length = 120)
    private String email;
    @Column(length = 100)
    private String zone;
    @Column(length = 60)
    private String country = "Togo";
    @Column(length = 120)
    private String organization;
    @Column(length = 600)
    private String bio;
    @Column(name = "notif_inapp", nullable = false)
    private boolean notifInapp = true;
    @Column(name = "notif_sms", nullable = false)
    private boolean notifSms = true;
    @Column(name = "notif_email", nullable = false)
    private boolean notifEmail = false;
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
    public String getEmail() { return email; }
    public String getZone() { return zone; }
    public String getCountry() { return country; }
    public String getOrganization() { return organization; }
    public String getBio() { return bio; }
    public boolean isNotifInapp() { return notifInapp; }
    public boolean isNotifSms() { return notifSms; }
    public boolean isNotifEmail() { return notifEmail; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastLoginAt() { return lastLoginAt; }

    /** Libellé court pour l'affichage (nom, sinon téléphone). */
    public String displayName() { return name == null || name.isBlank() ? phone : name; }

    void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    void setStatus(String status) { this.status = status; }
    void setRole(String role, Set<String> permissions) {
        this.role = role;
        this.permissions.clear();
        this.permissions.addAll(permissions);
    }
    void updateProfile(String name, String email, String zone, String organization, String bio,
            boolean notifInapp, boolean notifSms, boolean notifEmail) {
        this.name = name;
        this.email = email;
        this.zone = zone;
        this.organization = organization;
        this.bio = bio;
        this.notifInapp = notifInapp;
        this.notifSms = notifSms;
        this.notifEmail = notifEmail;
    }
}
