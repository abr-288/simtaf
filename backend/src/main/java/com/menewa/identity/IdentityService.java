package com.menewa.identity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Set;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
class IdentityService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Set<String> SELF_REGISTER_ROLES = Set.of("donor", "client", "provider", "partner", "beneficiary");

    private final IdentityUserRepository users;
    private final OtpChallengeRepository challenges;
    private final IdentitySessionRepository sessions;
    private final OtpDelivery delivery;
    private final ApplicationEventPublisher events;
    private final String otpSecret;
    private final boolean devMode;
    private final Duration otpTtl;
    private final Duration sendWindow;
    private final Duration sessionTtl;
    private final int maxSends;
    private final int maxAttempts;

    IdentityService(IdentityUserRepository users, OtpChallengeRepository challenges,
            IdentitySessionRepository sessions, OtpDelivery delivery, ApplicationEventPublisher events,
            @Value("${menewa.identity.otp.secret}") String otpSecret,
            @Value("${menewa.identity.otp.dev-mode:true}") boolean devMode,
            @Value("${menewa.identity.otp.ttl:PT5M}") Duration otpTtl,
            @Value("${menewa.identity.otp.send-window:PT10M}") Duration sendWindow,
            @Value("${menewa.identity.session.ttl:P14D}") Duration sessionTtl,
            @Value("${menewa.identity.otp.max-sends:3}") int maxSends,
            @Value("${menewa.identity.otp.max-attempts:5}") int maxAttempts) {
        this.users = users;
        this.challenges = challenges;
        this.sessions = sessions;
        this.delivery = delivery;
        this.events = events;
        this.otpSecret = otpSecret;
        this.devMode = devMode;
        this.otpTtl = otpTtl;
        this.sendWindow = sendWindow;
        this.sessionTtl = sessionTtl;
        this.maxSends = maxSends;
        this.maxAttempts = maxAttempts;
    }

    @Transactional
    OtpResponse requestOtp(OtpRequest request) {
        String phone = normalizePhone(request.phone());
        boolean register = "register".equals(request.mode());
        var existing = users.findByPhone(phone).orElse(null);
        if (register && existing != null) throw failure(HttpStatus.CONFLICT, "account_exists");
        if (!register && existing == null) throw failure(HttpStatus.NOT_FOUND, "account_not_found");
        if (existing != null && !"active".equals(existing.getStatus())) throw failure(HttpStatus.FORBIDDEN, "account_unavailable");
        if (register && (request.name() == null || request.name().trim().length() < 2)) throw failure(HttpStatus.BAD_REQUEST, "name_required");
        String role = register && SELF_REGISTER_ROLES.contains(request.role()) ? request.role() : "donor";
        Instant now = Instant.now();
        if (challenges.countByPhoneAndCreatedAtAfter(phone, now.minus(sendWindow)) >= maxSends)
            throw failure(HttpStatus.TOO_MANY_REQUESTS, "otp_rate_limited");
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        challenges.save(new OtpChallenge(phone, hash(phone, code), register ? request.name().trim() : null,
                register ? role : null, now.plus(otpTtl)));
        delivery.send(phone, code);
        return OtpResponse.success(devMode ? code : null);
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    SessionResponse verifyOtp(OtpVerification request) {
        String phone = normalizePhone(request.phone());
        var candidates = challenges.findAllByPhoneAndConsumedFalseAndExpiresAtAfterOrderByCreatedAtDesc(phone, Instant.now());
        if (candidates.isEmpty()) throw failure(HttpStatus.UNAUTHORIZED, "otp_expired_or_missing");
        OtpChallenge challenge = candidates.getFirst();
        if (challenge.getAttempts() >= maxAttempts) throw failure(HttpStatus.TOO_MANY_REQUESTS, "otp_attempts_exceeded");
        challenge.incrementAttempts();
        if (!MessageDigest.isEqual(hash(phone, request.code()).getBytes(StandardCharsets.US_ASCII),
                challenge.getCodeHash().getBytes(StandardCharsets.US_ASCII))) {
            challenges.save(challenge);
            throw failure(HttpStatus.UNAUTHORIZED, "otp_invalid");
        }
        challenge.consume();
        var existing = users.findByPhone(phone);
        var user = existing.orElseGet(() -> users.save(new IdentityUser(
                phone, challenge.getRegistrationName() == null ? "" : challenge.getRegistrationName(),
                challenge.getRegistrationRole() == null ? "donor" : challenge.getRegistrationRole())));
        if (!"active".equals(user.getStatus())) throw failure(HttpStatus.FORBIDDEN, "account_unavailable");
        user.setLastLoginAt(Instant.now());
        if (existing.isEmpty()) events.publishEvent(new IdentityEvents.UserRegistered(user.getId(), user.getRole()));
        events.publishEvent(new IdentityEvents.UserLoggedIn(user.getId(), user.getRole()));
        String token = newToken();
        Instant expires = Instant.now().plus(sessionTtl);
        sessions.save(new IdentitySession(sha256(token), user, expires));
        return new SessionResponse(token, "Bearer", expires, IdentityUserResponse.from(user));
    }

    @Transactional(readOnly = true)
    IdentityUser currentUser(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) throw failure(HttpStatus.UNAUTHORIZED, "unauthenticated");
        var session = sessions.findById(sha256(authorization.substring(7))).orElse(null);
        if (session == null || !session.getExpiresAt().isAfter(Instant.now()) || !"active".equals(session.getUser().getStatus()))
            throw failure(HttpStatus.UNAUTHORIZED, "session_expired");
        return session.getUser();
    }

    @Transactional
    IdentityUser updateProfile(String authorization, ProfileUpdate update) {
        IdentityUser user = users.findById(currentUser(authorization).getId()).orElseThrow();
        String name = update.name() == null ? "" : update.name().trim();
        if (name.length() < 2) throw failure(HttpStatus.BAD_REQUEST, "Nom trop court.");
        user.updateProfile(name, blankToNull(update.email()), blankToNull(update.zone()), blankToNull(update.organization()),
                blankToNull(update.bio()), update.notifInapp(), update.notifSms(), update.notifEmail());
        return user;
    }

    private static String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    @Transactional
    void logout(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) return;
        sessions.findById(sha256(authorization.substring(7))).ifPresent(session -> {
            sessions.delete(session);
            IdentityUser user = session.getUser();
            events.publishEvent(new IdentityEvents.UserLoggedOut(user.getId(), user.getRole()));
        });
    }

    static String normalizePhone(String raw) {
        String phone = raw.replaceAll("[\\s().-]", "");
        if (phone.startsWith("00")) phone = "+" + phone.substring(2);
        if (!phone.startsWith("+")) phone = phone.matches("\\d{8}") ? "+228" + phone : "+" + phone;
        if (!phone.matches("\\+[0-9]{8,15}")) throw failure(HttpStatus.BAD_REQUEST, "invalid_phone");
        return phone;
    }

    private String hash(String phone, String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(otpSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal((phone + ":" + code).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) { throw new IllegalStateException("OTP hashing unavailable", ex); }
    }

    private static String sha256(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception ex) { throw new IllegalStateException(ex); }
    }

    private static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static ResponseStatusException failure(HttpStatus status, String code) {
        return new ResponseStatusException(status, code);
    }
}
