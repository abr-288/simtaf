package com.menewa.identity;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Modifying
    @Query("DELETE FROM OtpChallenge c WHERE c.createdAt < :before")
    int deleteCreatedBefore(@Param("before") Instant before);
}

interface IdentitySessionRepository extends JpaRepository<IdentitySession, String> {
    @Modifying
    @Query("DELETE FROM IdentitySession s WHERE s.expiresAt < :now")
    int deleteExpired(@Param("now") Instant now);
}
