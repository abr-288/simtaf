package com.menewa.identity;

import com.menewa.sms.SmsSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Code OTP envoyé par SMS au numéro saisi, jamais renvoyé à l'écran (OTP_DEV_MODE=false). */
@Component
@ConditionalOnProperty(name = "menewa.identity.otp.dev-mode", havingValue = "false")
class SmsOtpDelivery implements OtpDelivery {
    private static final Logger log = LoggerFactory.getLogger(SmsOtpDelivery.class);
    private final SmsSender sms;

    SmsOtpDelivery(SmsSender sms) {
        if (!sms.configured())
            throw new IllegalStateException("OTP_DEV_MODE=false exige un fournisseur SMS (SMS_PROVIDER=twilio et ses identifiants).");
        this.sms = sms;
    }

    @Override
    public void send(String phone, String code) {
        String error = sms.send(phone, "Menewa Naya : votre code de vérification est " + code + ". Valable 5 minutes. Ne le communiquez à personne.");
        if (error != null) {
            log.error("Échec d'envoi de l'OTP à {} : {}", phone, error);
            // Annule la transaction : le code non reçu n'est pas enregistré.
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "sms_unavailable");
        }
    }
}
