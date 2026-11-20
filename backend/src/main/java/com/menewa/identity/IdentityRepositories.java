package com.menewa.identity;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface IdentityUserRepository extends JpaRepository<IdentityUser, Long> {
    Optional<IdentityUser> findByPhone(String phone);
    List<IdentityUser> findAllByRoleAndStatusOrderByNameAsc(String role, String status);
    List<IdentityUser> findAllByRoleInOrderByNameAsc(Collection<String> roles);
    List<IdentityUser> findAllByRoleInAndStatus(Collection<String> roles, String status);
    long countByRoleAndStatus(String role, String status);
}

interface OtpChallengeRepository extends JpaRepository<OtpChallenge, Long> {
    long countByPhoneAndCreatedAtAfter(String phone, Instant since);
    List<OtpChallenge> findAllByPhoneAndConsumedFalseAndExpiresAtAfterOrderByCreatedAtDesc(String phone, Instant now);
}

interface IdentitySessionRepository extends JpaRepository<IdentitySession, String> {}
