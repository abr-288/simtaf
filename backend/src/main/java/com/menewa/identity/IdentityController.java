package com.menewa.identity;

import jakarta.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/auth", "/api/v1/auth"})
public class IdentityController {
    private final IdentityService identity;

    IdentityController(IdentityService identity) { this.identity = identity; }

    @PostMapping("/otp/request")
    OtpResponse requestOtp(@Valid @RequestBody OtpRequest request) {
        return identity.requestOtp(request);
    }

    @PostMapping("/otp/verify")
    SessionResponse verifyOtp(@Valid @RequestBody OtpVerification request) {
        return identity.verifyOtp(request);
    }

    @GetMapping("/me")
    IdentityUserResponse me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return IdentityUserResponse.from(identity.currentUser(authorization));
    }

    @PutMapping("/me")
    IdentityUserResponse updateMe(@RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ProfileUpdate update) {
        return IdentityUserResponse.from(identity.updateProfile(authorization, update));
    }

    @GetMapping("/permissions")
    Set<String> permissions(@RequestHeader(value = "Authorization", required = false) String authorization) {
        IdentityUser user = identity.currentUser(authorization);
        return IdentityPermissions.ALL.stream().filter(permission -> IdentityPermissions.has(user, permission)).collect(Collectors.toUnmodifiableSet());
    }

    @PostMapping("/refresh")
    SessionResponse refresh(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return identity.refresh(authorization);
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        identity.logout(authorization);
        return ResponseEntity.noContent().build();
    }
}
