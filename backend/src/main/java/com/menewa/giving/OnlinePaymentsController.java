package com.menewa.giving;

import com.menewa.identity.IdentityAccess;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Paiement en ligne Mobile Money (encaissement, reversement, webhook du fournisseur, simulation de test). */
@RestController
@RequestMapping("/api/v1")
class OnlinePaymentsController {
    private static final String AUTH = "Authorization";
    private final IdentityAccess access;
    private final OnlinePayments online;

    OnlinePaymentsController(IdentityAccess access, OnlinePayments online) {
        this.access = access;
        this.online = online;
    }

    record PayRequest(String phone, String operator) {}

    record SimulateRequest(String outcome) {}

    /** Fournisseur actif (« simulated » tant qu'aucun agrégateur réel n'est branché) et opérateurs proposés. */
    @GetMapping("/payments/online/config")
    OnlinePayments.OnlinePaymentConfig config() {
        return online.config();
    }

    @PostMapping("/payments/{id}/pay")
    @ResponseStatus(HttpStatus.CREATED)
    OnlinePayments.PaymentTransactionView pay(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id, @RequestBody PayRequest r) {
        return online.collect(access.requireUser(auth), id, r.phone(), r.operator());
    }

    @GetMapping("/payments/{id}/transactions")
    List<OnlinePayments.PaymentTransaction> transactions(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id) {
        return online.forPayment(access.requireUser(auth), id);
    }

    /** Simulation uniquement (fournisseur « simulated ») : choisir l'issue d'une transaction en attente. */
    @PostMapping("/payments/transactions/{txId}/simulate")
    OnlinePayments.PaymentTransactionView simulate(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long txId,
            @RequestBody SimulateRequest r) {
        return online.simulate(access.requireUser(auth), txId, r.outcome());
    }

    /** Webhook du fournisseur : public, authentifié par signature HMAC (en-tête X-Menewa-Signature pour le simulé). */
    @PostMapping("/payments/webhook")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void webhook(@RequestBody String body, @RequestHeader(value = "X-Menewa-Signature", required = false) String signature) {
        online.webhook(body, signature);
    }

    @PostMapping("/admin/payments/{id}/payout")
    @ResponseStatus(HttpStatus.CREATED)
    OnlinePayments.PaymentTransactionView payout(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id, @RequestBody PayRequest r) {
        return online.payout(access.requirePermission(auth, "payments.manage"), id, r.phone(), r.operator());
    }

    @GetMapping("/admin/payment-transactions")
    List<OnlinePayments.PaymentTransaction> recent(@RequestHeader(value = AUTH, required = false) String auth) {
        access.requirePermission(auth, "payments.manage");
        return online.recent();
    }
}
