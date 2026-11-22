package com.menewa.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.menewa.sms.SmsSender;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

/** En production, le code part par SMS au numéro saisi ; un échec d'envoi est signalé, pas masqué. */
class SmsOtpDeliveryTests {
    private static SmsSender sender(String result, List<String> sent) {
        return new SmsSender(new ObjectMapper(), "twilio", "", "", "AC123", "secret", "", "", "http://127.0.0.1:9") {
            @Override public String send(String to, String message) { sent.add(to + "|" + message); return result; }
        };
    }

    @Test
    void codeIsSentToTheIdentifiedNumber() {
        List<String> sent = new ArrayList<>();
        new SmsOtpDelivery(sender(null, sent)).send("+22890000001", "123456");
        assertThat(sent).singleElement().asString().startsWith("+22890000001|").contains("123456");
    }

    @Test
    void failedSendIsReportedToTheUser() {
        assertThatThrownBy(() -> new SmsOtpDelivery(sender("Twilio HTTP 401", new ArrayList<>())).send("+22890000001", "123456"))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("sms_unavailable");
    }

    @Test
    void productionModeRefusesToStartWithoutProvider() {
        var demo = new SmsSender(new ObjectMapper(), "", "", "", "", "", "", "", "https://api.twilio.com");
        assertThatThrownBy(() -> new SmsOtpDelivery(demo)).isInstanceOf(IllegalStateException.class);
    }
}
