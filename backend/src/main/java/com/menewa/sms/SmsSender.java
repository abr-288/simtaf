package com.menewa.sms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Envoi d'un SMS. Fournisseur choisi par {@code SMS_PROVIDER} :
 * <ul>
 *   <li>{@code twilio} : API Messages de Twilio ({@code TWILIO_ACCOUNT_SID}, {@code TWILIO_AUTH_TOKEN}, et
 *       {@code TWILIO_MESSAGING_SERVICE_SID} ou {@code TWILIO_FROM}, numéro ou identifiant alphanumérique) ;</li>
 *   <li>{@code http} : passerelle JSON {to, message, sender} ({@code SMS_GATEWAY_URL}, {@code SMS_GATEWAY_TOKEN}) ;</li>
 *   <li>vide : aucun envoi, le message est seulement journalisé (mode démo).</li>
 * </ul>
 */
@Component
public class SmsSender {
    private static final Logger log = LoggerFactory.getLogger(SmsSender.class);
    private static final String SENDER = "MenewaNaya";
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper json;
    private final String provider;
    private final String gatewayUrl, gatewayToken;
    private final String twilioSid, twilioToken, twilioFrom, twilioService, twilioBaseUrl;

    public SmsSender(ObjectMapper json,
            @Value("${SMS_PROVIDER:}") String provider,
            @Value("${SMS_GATEWAY_URL:}") String gatewayUrl,
            @Value("${SMS_GATEWAY_TOKEN:}") String gatewayToken,
            @Value("${TWILIO_ACCOUNT_SID:}") String twilioSid,
            @Value("${TWILIO_AUTH_TOKEN:}") String twilioToken,
            @Value("${TWILIO_FROM:}") String twilioFrom,
            @Value("${TWILIO_MESSAGING_SERVICE_SID:}") String twilioService,
            @Value("${TWILIO_API_URL:https://api.twilio.com}") String twilioBaseUrl) {
        this.json = json;
        String p = provider == null ? "" : provider.trim().toLowerCase();
        // Compatibilité : une SMS_GATEWAY_URL seule active la passerelle HTTP, comme avant.
        this.provider = p.isEmpty() && !gatewayUrl.isBlank() ? "http" : p;
        this.gatewayUrl = gatewayUrl.trim();
        this.gatewayToken = gatewayToken.trim();
        this.twilioSid = twilioSid.trim();
        this.twilioToken = twilioToken.trim();
        this.twilioFrom = twilioFrom.isBlank() ? SENDER : twilioFrom.trim();
        this.twilioService = twilioService.trim();
        this.twilioBaseUrl = twilioBaseUrl.trim().replaceAll("/+$", "");
        switch (this.provider) {
            case "twilio" -> {
                if (this.twilioSid.isEmpty() || this.twilioToken.isEmpty())
                    throw new IllegalStateException("SMS_PROVIDER=twilio : TWILIO_ACCOUNT_SID et TWILIO_AUTH_TOKEN sont requis.");
            }
            case "http" -> {
                if (this.gatewayUrl.isEmpty()) throw new IllegalStateException("SMS_PROVIDER=http : SMS_GATEWAY_URL est requise.");
            }
            case "" -> { }
            default -> throw new IllegalStateException("SMS_PROVIDER inconnu : " + this.provider + " (attendu : twilio, http ou vide).");
        }
        log.info("Envoi des SMS : {}", this.provider.isEmpty() ? "démo (journalisés, aucun envoi)" : this.provider);
    }

    /** Vrai si un fournisseur réel est configuré (sinon les SMS sont seulement journalisés). */
    public boolean configured() {
        return !provider.isEmpty();
    }

    /** Envoie le SMS. Renvoie null si le fournisseur l'a accepté, sinon la raison de l'échec (jamais d'exception). */
    public String send(String to, String message) {
        if (!configured()) {
            log.info("[SMS:demo] -> {}: {}", to, message);
            return null;
        }
        try {
            return "twilio".equals(provider) ? sendTwilio(to, message) : sendHttp(to, message);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return "Interrompu";
        } catch (Exception ex) {
            return ex.getClass().getSimpleName() + (ex.getMessage() == null ? "" : " : " + ex.getMessage());
        }
    }

    private String sendTwilio(String to, String message) throws Exception {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("To", to);
        if (!twilioService.isEmpty()) form.put("MessagingServiceSid", twilioService);
        else form.put("From", twilioFrom);
        form.put("Body", message);
        String body = form.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        String auth = Base64.getEncoder().encodeToString((twilioSid + ":" + twilioToken).getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder(URI.create(twilioBaseUrl + "/2010-04-01/Accounts/"
                        + URLEncoder.encode(twilioSid, StandardCharsets.UTF_8) + "/Messages.json"))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) return null;
        // Twilio renvoie {code, message} : on ne garde que ces deux champs.
        try {
            JsonNode error = json.readTree(response.body());
            return "Twilio HTTP " + response.statusCode() + " (" + error.path("code").asText("?") + ") " + error.path("message").asText("");
        } catch (Exception ignored) {
            return "Twilio HTTP " + response.statusCode();
        }
    }

    private String sendHttp(String to, String message) throws Exception {
        String body = json.writeValueAsString(Map.of("to", to, "message", message, "sender", SENDER));
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(gatewayUrl)).timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body));
        if (!gatewayToken.isEmpty()) request.header("Authorization", "Bearer " + gatewayToken);
        int status = client.send(request.build(), HttpResponse.BodyHandlers.discarding()).statusCode();
        return status >= 200 && status < 300 ? null : "HTTP " + status;
    }
}
