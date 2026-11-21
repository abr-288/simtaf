package com.menewa.identity;

import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ménage planifié du module identity (toutes les heures par défaut) :
 * sessions expirées, codes de connexion anciens (conservés 24 h pour la limite par numéro) et compteurs par IP périmés.
 */
@Component
class IdentityMaintenance {
    private static final Logger log = LoggerFactory.getLogger(IdentityMaintenance.class);

    private final IdentitySessionRepository sessions;
    private final OtpChallengeRepository challenges;
    private final IpRateLimiter ipLimiter;
    private final Duration otpRetention;
    private final Duration ipWindow;

    IdentityMaintenance(IdentitySessionRepository sessions, OtpChallengeRepository challenges, IpRateLimiter ipLimiter,
            @Value("${menewa.identity.otp.retention:P1D}") Duration otpRetention,
            @Value("${menewa.identity.otp.ip-window:PT1H}") Duration ipWindow) {
        this.sessions = sessions;
        this.challenges = challenges;
        this.ipLimiter = ipLimiter;
        this.otpRetention = otpRetention;
        this.ipWindow = ipWindow;
    }

    record Result(int expiredSessions, int oldChallenges, int trackedIps) {}

    @Scheduled(cron = "${menewa.identity.maintenance-cron:0 7 * * * *}")
    @Transactional
    public Result purge() {
        Instant now = Instant.now();
        int expired = sessions.deleteExpired(now);
        int old = challenges.deleteCreatedBefore(now.minus(otpRetention));
        ipLimiter.purge(ipWindow);
        var result = new Result(expired, old, ipLimiter.trackedKeys());
        if (expired + old > 0) log.info("Ménage identity : {} session(s) expirée(s), {} code(s) ancien(s) supprimé(s)", expired, old);
        return result;
    }
}
