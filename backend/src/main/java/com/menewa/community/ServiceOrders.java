package com.menewa.community;

import com.menewa.giving.ServicePayments;
import com.menewa.identity.IdentityAccess;
import com.menewa.identity.IdentityDirectory;
import com.menewa.identity.IdentityPermissions;
import com.menewa.identity.IdentityUser;
import com.menewa.platform.Audit;
import com.menewa.platform.Notifications;
import com.menewa.platform.Problems;
import com.menewa.rewards.Reputation;
import com.menewa.rewards.Rewards;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Services communautaires : demande à un prestataire, négociation du prix ou échange, suivi et notation. */
@RestController
@RequestMapping("/api/v1/services/orders")
class ServiceOrders {
    private static final String AUTH = "Authorization";
    private static final String COLS = "id, client_id, provider_id, offer_id, title, description, zone, proposed_price, agreed_price, compensation_mode, exchange_description, status, created_at, updated_at";

    record Order(Long id, Long clientId, Long providerId, Long offerId, String title, String description, String zone, int proposedPrice,
            Integer agreedPrice, String compensationMode, String exchangeDescription, String status, OffsetDateTime createdAt, OffsetDateTime updatedAt) {}
    record OrderView(Order order, String clientName, String providerName, boolean iAmProvider) {}
    record ServiceMessage(Long id, Long orderId, Long authorId, String body, Integer price, OffsetDateTime createdAt) {}
    record ServiceMessageView(ServiceMessage message, String authorName) {}
    record OrderDetail(Order order, String clientName, String providerName, boolean iAmProvider, List<ServiceMessageView> messages,
            List<ServicePayments.ServicePayment> payments) {}

    private final JdbcClient jdbc;
    private final IdentityAccess access;
    private final IdentityDirectory directory;
    private final Notifications notifications;
    private final Audit audit;
    private final Rewards rewards;
    private final Reputation reputation;
    private final ServicePayments payments;

    ServiceOrders(JdbcClient jdbc, IdentityAccess access, IdentityDirectory directory, Notifications notifications, Audit audit,
            Rewards rewards, Reputation reputation, ServicePayments payments) {
        this.jdbc = jdbc;
        this.access = access;
        this.directory = directory;
        this.notifications = notifications;
        this.audit = audit;
        this.rewards = rewards;
        this.reputation = reputation;
        this.payments = payments;
    }

    @GetMapping
    List<OrderView> mine(@RequestHeader(value = AUTH, required = false) String auth) {
        IdentityUser u = access.requireUser(auth);
        var orders = jdbc.sql("SELECT " + COLS + " FROM community_service_orders WHERE client_id = :u OR provider_id = :u ORDER BY id DESC")
                .param("u", u.getId()).query(Order.class).list();
        var ids = new java.util.ArrayList<Long>();
        orders.forEach(o -> { ids.add(o.clientId()); ids.add(o.providerId()); });
        var names = directory.findAll(ids);
        return orders.stream().map(o -> new OrderView(o, names.containsKey(o.clientId()) ? names.get(o.clientId()).displayName() : "—",
                names.containsKey(o.providerId()) ? names.get(o.providerId()).displayName() : "—", o.providerId().equals(u.getId()))).toList();
    }

    record CreateOrder(Long providerId, String title, String description, String zone, String compensationMode, int price, String exchangeDescription) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    Map<String, Object> create(@RequestHeader(value = AUTH, required = false) String auth, @RequestBody CreateOrder r) {
        IdentityUser u = access.requireUser(auth);
        if (IdentityPermissions.isAdmin(u)) throw Problems.bad("Les administrateurs ne peuvent pas commander.");
        var provider = directory.find(r.providerId()).orElse(null);
        if (provider == null || !"provider".equals(provider.role()) || !provider.active() || provider.id().equals(u.getId()))
            throw Problems.bad("Prestataire invalide.");
        String title = Problems.trim(r.title(), 150);
        if (title.length() < 4) throw Problems.bad("Décrivez le service demandé.");
        String mode = "exchange".equals(r.compensationMode()) ? "exchange" : "money";
        int price = Math.max(r.price(), 0);
        String ex = Problems.trim(r.exchangeDescription(), 600);
        if ("exchange".equals(mode) && ex.length() < 5) throw Problems.bad("Décrivez les engagements réciproques de l'échange de services.");
        if ("money".equals(mode) && price < 500) throw Problems.bad("Proposez un prix (≥ 500 FCFA).");
        Map<String, Object> p = new HashMap<>();
        p.put("c", u.getId()); p.put("p", provider.id()); p.put("t", title); p.put("d", Problems.trimToNull(r.description(), 1500));
        p.put("z", Problems.trimToNull(r.zone(), 100)); p.put("price", price); p.put("mode", mode); p.put("ex", "exchange".equals(mode) ? ex : null);
        var keys = new GeneratedKeyHolder();
        jdbc.sql("""
                INSERT INTO community_service_orders (client_id, provider_id, title, description, zone, proposed_price, compensation_mode, exchange_description)
                VALUES (:c, :p, :t, :d, :z, :price, :mode, :ex)""").params(p).update(keys, "id");
        long id = keys.getKey().longValue();
        jdbc.sql("INSERT INTO community_service_messages (order_id, author_id, body, price) VALUES (:o, :a, :b, :price)")
                .param("o", id).param("a", u.getId()).param("b", "Demande : " + title).param("price", "money".equals(mode) ? price : null).update();
        notifications.notify(provider.id(), "service", "Nouvelle demande de service : " + title, null, "/espace/services/" + id);
        audit.record(u, "service.order", "service_order", id);
        return Map.of("ok", "Demande envoyée au prestataire. Suivez la négociation dans « Services communautaires ».", "id", id);
    }

    private Order load(long id, IdentityUser u) {
        return jdbc.sql("SELECT " + COLS + " FROM community_service_orders WHERE id = :id AND (client_id = :u OR provider_id = :u)")
                .param("id", id).param("u", u.getId()).query(Order.class).optional().orElseThrow(() -> Problems.notFound("Commande introuvable."));
    }

    @GetMapping("/{id}")
    OrderDetail detail(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id) {
        IdentityUser u = access.requireUser(auth);
        Order o = load(id, u);
        var msgs = jdbc.sql("SELECT id, order_id, author_id, body, price, created_at FROM community_service_messages WHERE order_id = :o ORDER BY id")
                .param("o", id).query(ServiceMessage.class).list();
        var ids = new java.util.ArrayList<>(msgs.stream().map(ServiceMessage::authorId).toList());
        ids.add(o.clientId()); ids.add(o.providerId());
        var names = directory.findAll(ids);
        return new OrderDetail(o, names.get(o.clientId()) == null ? "—" : names.get(o.clientId()).displayName(),
                names.get(o.providerId()) == null ? "—" : names.get(o.providerId()).displayName(), o.providerId().equals(u.getId()),
                msgs.stream().map(m -> new ServiceMessageView(m, names.containsKey(m.authorId()) ? names.get(m.authorId()).displayName() : "—")).toList(),
                payments.forOrder(id));
    }

    record PostMessage(String body, Integer price) {}

    @PostMapping("/{id}/messages")
    @Transactional
    Map<String, String> message(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id, @RequestBody PostMessage r) {
        IdentityUser u = access.requireUser(auth);
        Order o = load(id, u);
        if (!Set.of("requested", "negotiating", "agreed", "in_progress").contains(o.status())) throw Problems.bad("Échanges clos.");
        String body = Problems.trim(r.body(), 1000);
        Integer price = r.price();
        if (body.length() < 2 && price == null) throw Problems.bad("Message vide.");
        boolean canPrice = Set.of("requested", "negotiating").contains(o.status()) && "money".equals(o.compensationMode());
        if (price != null && !canPrice) throw Problems.bad("Le prix ne peut plus être modifié.");
        if (price != null && price < 0) throw Problems.bad("Prix invalide.");
        jdbc.sql("INSERT INTO community_service_messages (order_id, author_id, body, price) VALUES (:o, :a, :b, :p)")
                .param("o", id).param("a", u.getId()).param("b", body.isEmpty() ? "Nouvelle proposition de prix" : body).param("p", canPrice ? price : null).update();
        if (price != null)
            jdbc.sql("UPDATE community_service_orders SET proposed_price = :p, status = 'negotiating', updated_at = :now WHERE id = :id")
                    .param("p", price).param("now", OffsetDateTime.now()).param("id", id).update();
        Long other = u.getId().equals(o.clientId()) ? o.providerId() : o.clientId();
        notifications.notify(other, "service", "Nouveau message — " + o.title(),
                price != null ? "Proposition : " + price + " FCFA" : Problems.trim(body, 100), "/espace/services/" + id);
        return Map.of("ok", "Message envoyé.");
    }

    record Transition(String action, Integer rating, String comment) {}

    @PostMapping("/{id}/transition")
    @Transactional
    Map<String, String> transition(@RequestHeader(value = AUTH, required = false) String auth, @PathVariable long id, @RequestBody Transition r) {
        IdentityUser u = access.requireUser(auth);
        Order o = load(id, u);
        boolean isProvider = o.providerId().equals(u.getId());
        Long other = isProvider ? o.clientId() : o.providerId();
        String act = r.action() == null ? "" : r.action();
        boolean negotiating = Set.of("requested", "negotiating").contains(o.status());
        if ("accept".equals(act) && negotiating) {
            Long lastPriceAuthor = jdbc.sql("SELECT author_id FROM community_service_messages WHERE order_id = :o AND price IS NOT NULL ORDER BY id DESC")
                    .param("o", id).query(Long.class).list().stream().findFirst().orElse(o.clientId());
            if ("money".equals(o.compensationMode()) && lastPriceAuthor.equals(u.getId()))
                throw Problems.bad("Vous ne pouvez pas accepter votre propre proposition : attendez la réponse de l'autre partie.");
            if ("exchange".equals(o.compensationMode()) && !isProvider) throw Problems.bad("Le prestataire doit accepter l'échange de services.");
            setStatus(id, "agreed", "money".equals(o.compensationMode()) ? o.proposedPrice() : null);
            payments.open(id, o.clientId(), o.compensationMode(), o.proposedPrice(), o.exchangeDescription(), o.title());
            notifications.notify(other, "service", "Accord conclu : " + o.title(), null, "/espace/services/" + id);
            audit.record(u, "service.agree", "service_order", id, Map.of("price", o.proposedPrice(), "mode", o.compensationMode()));
        } else if ("decline".equals(act) && negotiating) {
            setStatus(id, isProvider ? "declined" : "cancelled", o.agreedPrice());
            notifications.notify(other, "service", "Demande " + (isProvider ? "déclinée" : "annulée") + " : " + o.title(), null, "/espace/services/" + id);
        } else if ("start".equals(act) && isProvider && "agreed".equals(o.status())) {
            setStatus(id, "in_progress", o.agreedPrice());
            payments.setExchangeStatus(id, "in_progress");
        } else if ("done".equals(act) && isProvider && "in_progress".equals(o.status())) {
            setStatus(id, "done", o.agreedPrice());
            notifications.notify(o.clientId(), "service", "Service réalisé — à confirmer : " + o.title(), null, "/espace/services/" + id);
        } else if ("confirm".equals(act) && !isProvider && "done".equals(o.status())) {
            int rating = r.rating() == null ? 0 : r.rating();
            if (rating < 1 || rating > 5) throw Problems.bad("Donnez une note de 1 à 5.");
            setStatus(id, "confirmed", o.agreedPrice());
            reputation.add("service", id, u.getId(), o.providerId(), rating, Problems.trimToNull(r.comment(), 800));
            payments.setExchangeStatus(id, "done");
            rewards.award(o.providerId(), "service_confirmed", "service_order", id, u.getId());
            rewards.award(u.getId(), "review_given", "service_order", id, null);
            notifications.notify(o.providerId(), "review", "Service confirmé — note " + rating + "/5", o.title(), "/espace/services/" + id);
        } else {
            throw Problems.bad("Action impossible dans l'état actuel.");
        }
        return Map.of("ok", "Mis à jour.");
    }

    private void setStatus(long id, String status, Integer agreedPrice) {
        jdbc.sql("UPDATE community_service_orders SET status = :s, agreed_price = :a, updated_at = :now WHERE id = :id")
                .param("s", status).param("a", agreedPrice).param("now", OffsetDateTime.now()).param("id", id).update();
    }
}
