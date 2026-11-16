package com.menewa.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
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

record IdentityUserResponse(Long id, String phone, String name, String role, Set<String> permissions) {
    static IdentityUserResponse from(IdentityUser user) {
        return new IdentityUserResponse(user.getId(), user.getPhone(), user.getName(), user.getRole(), user.getPermissions());
    }
}

record SessionResponse(String accessToken, String tokenType, Instant expiresAt, IdentityUserResponse user) {}

record IdentityError(String error) {}
