package com.menewa.identity;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "menewa.identity.otp.dev-mode", havingValue = "false")
class HttpOtpDelivery implements OtpDelivery {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String url;
    private final String token;

    HttpOtpDelivery(@Value("${SMS_GATEWAY_URL}") String url,
            @Value("${SMS_GATEWAY_TOKEN:}") String token) {
        this.url = url;
        this.token = token;
    }

    @Override
    public void send(String phone, String code) {
        try {
            String json = "{\"to\":\"" + phone + "\",\"message\":\"Menewa Naya : votre code de vérification est " + code + ". Valable 5 minutes.\",\"sender\":\"MenewaNaya\"}";
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json));
            if (!token.isBlank()) request.header("Authorization", "Bearer " + token);
            int status = client.send(request.build(), HttpResponse.BodyHandlers.discarding()).statusCode();
            if (status < 200 || status >= 300) throw new IllegalStateException("SMS gateway returned HTTP " + status);
        } catch (Exception ex) {
            throw new IllegalStateException("OTP delivery failed", ex);
        }
    }
}
