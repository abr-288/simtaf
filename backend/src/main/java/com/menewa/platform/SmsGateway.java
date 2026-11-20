package com.menewa.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Envoi de SMS de notification via la passerelle HTTP JSON {to, message, sender}.
 * Sans SMS_GATEWAY_URL, le message est seulement journalisé. Un échec d'envoi ne fait jamais échouer l'action métier.
 */
@Component
class SmsGateway {
    private static final Logger log = LoggerFactory.getLogger(SmsGateway.class);
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper json;
    private final String url;
    private final String token;

    SmsGateway(ObjectMapper json, @Value("${SMS_GATEWAY_URL:}") String url, @Value("${SMS_GATEWAY_TOKEN:}") String token) {
        this.json = json;
        this.url = url;
        this.token = token;
    }

    void send(String to, String message) {
        if (url == null || url.isBlank()) {
            log.info("[SMS:demo] -> {}: {}", to, message);
            return;
        }
        try {
            String body = json.writeValueAsString(Map.of("to", to, "message", message, "sender", "MenewaNaya"));
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body));
            if (token != null && !token.isBlank()) request.header("Authorization", "Bearer " + token);
            int status = client.send(request.build(), HttpResponse.BodyHandlers.discarding()).statusCode();
            if (status < 200 || status >= 300) log.warn("SMS gateway returned HTTP {}", status);
        } catch (Exception ex) {
            log.warn("SMS delivery failed: {}", ex.getMessage());
        }
    }
}
