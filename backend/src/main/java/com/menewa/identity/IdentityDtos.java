package com.menewa.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.Set;

record OtpRequest(
        @NotBlank @Size(max = 30) String phone,
        @Size(max = 80) String name,
        @Size(max = 24) String role,
        @Pattern(regexp = "login|register") String mode) {}

record OtpVerification(
        @NotBlank @Size(max = 30) String phone,
        @NotBlank @Pattern(regexp = "[0-9]{6}") String code) {}

record OtpResponse(boolean ok, String devCode, String error) {
    static OtpResponse success(String code) { return new OtpResponse(true, code, null); }
    static OtpResponse failure(String message) { return new OtpResponse(false, null, message); }
}

record NotificationPreferences(boolean inapp, boolean sms, boolean email) {}

record IdentityUserResponse(Long id, String phone, String name, String role, Set<String> permissions, String status,
        String email, String zone, String country, String organization, String bio,
        NotificationPreferences notifPrefs, Instant createdAt) {
    static IdentityUserResponse from(IdentityUser user) {
        return new IdentityUserResponse(user.getId(), user.getPhone(), user.getName(), user.getRole(), user.getPermissions(),
                user.getStatus(), user.getEmail(), user.getZone(), user.getCountry(), user.getOrganization(), user.getBio(),
                new NotificationPreferences(user.isNotifInapp(), user.isNotifSms(), user.isNotifEmail()), user.getCreatedAt());
    }
}

record SessionResponse(String accessToken, String tokenType, Instant expiresAt, IdentityUserResponse user) {}

record IdentityError(String error) {}

record ProfileUpdate(
        @Size(max = 80) String name,
        @Size(max = 120) String email,
        @Size(max = 100) String zone,
        @Size(max = 120) String organization,
        @Size(max = 600) String bio,
        boolean notifInapp,
        boolean notifSms,
        boolean notifEmail) {}

record AdminUserView(Long id, String phone, String name, String role, String status, Set<String> permissions,
        String zone, String organization, Instant createdAt, Instant lastLoginAt) {
    static AdminUserView from(IdentityUser u) {
        return new AdminUserView(u.getId(), u.getPhone(), u.getName(), u.getRole(), u.getStatus(), u.getPermissions(),
                u.getZone(), u.getOrganization(), u.getCreatedAt(), u.getLastLoginAt());
    }
}

record UserOption(Long id, String name, String phone, String role) {
    static UserOption from(IdentityUser u) { return new UserOption(u.getId(), u.getName(), u.getPhone(), u.getRole()); }
}

record SetStatusRequest(@NotBlank String status) {}

record SetRoleRequest(@NotBlank String role, List<String> permissions) {}
