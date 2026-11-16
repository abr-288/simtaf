package com.menewa.identity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "menewa.identity.otp.dev-mode", havingValue = "true", matchIfMissing = true)
class DevelopmentOtpDelivery implements OtpDelivery {
    private static final Logger log = LoggerFactory.getLogger(DevelopmentOtpDelivery.class);

    @Override
    public void send(String phone, String code) {
        log.warn("Development OTP for {}: {}", phone, code);
    }
}
