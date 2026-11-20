package com.menewa.giving;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** API publique : suivi financier des services communautaires (paiement ou échange de services). */
@Component
public class ServicePayments {
    public record ServicePayment(Long id, String status, String mode, int amountPlanned, int amountPaid, String exchangeStatus) {}

    private final GivingDb db;

    ServicePayments(GivingDb db) { this.db = db; }

    public void open(Long orderId, Long clientId, String mode, int amount, String exchangeDescription, String title) {
        boolean exchange = "exchange".equals(mode);
        Map<String, Object> p = new HashMap<>();
        p.put("o", orderId); p.put("u", clientId); p.put("mode", exchange ? "exchange" : "money"); p.put("method", exchange ? "exchange" : null);
        p.put("a", exchange ? 0 : amount); p.put("ex", exchangeDescription); p.put("es", exchange ? "pending" : null); p.put("n", "Service : " + title);
        db.insert("""
                INSERT INTO giving_payments (service_order_id, user_id, direction, kind, mode, method, amount_planned, exchange_description, exchange_status, note)
                VALUES (:o, :u, 'in', 'service', :mode, :method, :a, :ex, :es, :n)""", p);
    }

    public void setExchangeStatus(Long orderId, String status) {
        db.jdbc.sql("UPDATE giving_payments SET exchange_status = :s WHERE service_order_id = :o AND mode = 'exchange'")
                .param("s", status).param("o", orderId).update();
    }

    public List<ServicePayment> forOrder(Long orderId) {
        return db.jdbc.sql("SELECT id, status, mode, amount_planned, amount_paid, exchange_status FROM giving_payments WHERE service_order_id = :o ORDER BY id")
                .param("o", orderId).query(ServicePayment.class).list();
    }
}
