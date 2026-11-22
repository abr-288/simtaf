package com.menewa.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Envoi Twilio contre un faux serveur local : aucun appel réel, aucun identifiant réel. */
class SmsSenderTests {
    private HttpServer server;
    private final AtomicReference<String> path = new AtomicReference<>(), auth = new AtomicReference<>(), form = new AtomicReference<>();
    private final AtomicInteger status = new AtomicInteger(201);

    @BeforeEach
    void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            path.set(exchange.getRequestURI().getPath());
            auth.set(exchange.getRequestHeaders().getFirst("Authorization"));
            form.set(URLDecoder.decode(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8), StandardCharsets.UTF_8));
            byte[] body = (status.get() == 201 ? "{\"sid\":\"SM1\"}" : "{\"code\":21211,\"message\":\"Invalid 'To' Phone Number\"}")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status.get(), body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private SmsSender twilio(String from, String service) {
        return new SmsSender(new ObjectMapper(), "twilio", "", "", "AC123", "secret", from, service,
                "http://127.0.0.1:" + server.getAddress().getPort());
    }

    @Test
    void twilioReceivesTheMessageForTheIdentifiedNumber() {
        assertThat(twilio("", "").send("+22890000001", "Menewa Naya : votre code est 123456")).isNull();
        assertThat(path.get()).isEqualTo("/2010-04-01/Accounts/AC123/Messages.json");
        assertThat(auth.get()).isEqualTo("Basic " + Base64.getEncoder().encodeToString("AC123:secret".getBytes(StandardCharsets.UTF_8)));
        assertThat(form.get()).isEqualTo("To=+22890000001&From=MenewaNaya&Body=Menewa Naya : votre code est 123456");
    }

    @Test
    void messagingServiceReplacesTheSender() {
        assertThat(twilio("+15005550006", "MG42").send("+22890000001", "x")).isNull();
        assertThat(form.get()).contains("MessagingServiceSid=MG42").doesNotContain("From=");
    }

    @Test
    void twilioErrorsAreReportedWithoutException() {
        status.set(400);
        assertThat(twilio("", "").send("+22890000001", "x")).isEqualTo("Twilio HTTP 400 (21211) Invalid 'To' Phone Number");
    }

    @Test
    void missingCredentialsStopTheStartup() {
        assertThatThrownBy(() -> new SmsSender(new ObjectMapper(), "twilio", "", "", "", "", "", "", "https://api.twilio.com"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("TWILIO_ACCOUNT_SID");
        assertThat(new SmsSender(new ObjectMapper(), "", "", "", "", "", "", "", "https://api.twilio.com").configured()).isFalse();
    }
}
