package com.menewa.giving;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Fournisseur simulé : aucun argent ne circule. Il se comporte comme un agrégateur réel (référence, statut en attente,
 * webhook signé), mais c'est le testeur qui choisit l'issue avec {@link #settle}. L'état est gardé en mémoire :
 * après un redémarrage, une transaction en attente reste en attente et peut toujours être simulée.
 */
@Component
@ConditionalOnProperty(name = "menewa.payments.provider", havingValue = "simulated", matchIfMissing = true)
class SimulatedPaymentGateway implements PaymentGateway {
    private final Map<String, String> statuses = new ConcurrentHashMap<>();
    private final byte[] secret;

    SimulatedPaymentGateway(@Value("${menewa.payments.webhook-secret:}") String secret) {
        // Le simulé ne vérifie que ses propres webhooks : sans secret configuré, un secret aléatoire suffit.
        this.secret = (secret == null || secret.isBlank() ? UUID.randomUUID().toString() : secret).getBytes(StandardCharsets.UTF_8);
    }

    @Override public String name() { return "simulated"; }

    @Override public boolean simulated() { return true; }

    @Override
    public Initiation collect(String reference, int amount, String phone, String operator, String label) {
        return start("SIM-IN-", "Paiement simulé : confirmez ou faites échouer la transaction avec les boutons de test.");
    }

    @Override
    public Initiation payout(String reference, int amount, String phone, String operator, String label) {
        return start("SIM-OUT-", "Reversement simulé : choisissez l'issue avec les boutons de test.");
    }

    private Initiation start(String prefix, String message) {
        String ref = prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        statuses.put(ref, "pending");
        return new Initiation(ref, message);
    }

    @Override
    public String fetchStatus(String providerRef) {
        return statuses.getOrDefault(providerRef, "pending");
    }

    /** Le testeur choisit l'issue ; renvoie le corps du webhook que le « fournisseur » envoie alors. */
    String settle(String providerRef, boolean success) {
        statuses.put(providerRef, success ? "succeeded" : "failed");
        return "{\"reference\":\"" + providerRef + "\",\"status\":\"" + (success ? "succeeded" : "failed") + "\"}";
    }

    String sign(String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean verifyWebhook(String body, String signature) {
        if (signature == null || body == null) return false;
        return MessageDigest.isEqual(sign(body).getBytes(StandardCharsets.US_ASCII), signature.trim().toLowerCase().getBytes(StandardCharsets.US_ASCII));
    }
}
