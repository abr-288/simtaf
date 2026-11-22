package com.menewa.giving;

/**
 * Fournisseur de paiement Mobile Money (agrégateur). Une seule implémentation active, choisie par
 * {@code menewa.payments.provider} : « simulated » tant qu'aucun agrégateur réel n'est branché.
 *
 * <p>Brancher un vrai agrégateur (CinetPay, PayDunya, FedaPay…) = écrire une implémentation de cette interface :
 * l'appel d'encaissement ou de reversement, la relecture du statut, et la vérification de signature du webhook.
 */
interface PaymentGateway {
    /** Identifiant enregistré sur chaque transaction (ex. « simulated », « cinetpay »). */
    String name();

    /** Vrai pour le fournisseur simulé : aucun argent ne circule et les écrans proposent de simuler l'issue. */
    boolean simulated();

    /** Réponse du fournisseur à une demande d'encaissement ou de reversement. */
    record Initiation(String providerRef, String message) {}

    /** Demande au client de confirmer le paiement sur son téléphone. {@code reference} : notre référence unique. */
    Initiation collect(String reference, int amount, String phone, String operator, String label);

    /** Envoie l'argent sur le compte Mobile Money du bénéficiaire. */
    Initiation payout(String reference, int amount, String phone, String operator, String label);

    /** Statut lu chez le fournisseur : « pending », « succeeded » ou « failed ». Seule source de vérité. */
    String fetchStatus(String providerRef);

    /** Vérifie que le webhook vient bien du fournisseur. */
    boolean verifyWebhook(String body, String signature);
}
