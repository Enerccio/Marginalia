package com.github.enerccio.marginalia.extensions.httphooks;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class HttpTest {

    private static HttpResponse<byte[]> get(FakeServer server, String path, int max) throws BackendException {
        return Http.send(Http.request(URI.create(server.url() + path), 10).GET().build(), max);
    }

    @Test
    void anAnswerWithinTheLimitIsRead() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.on("/a", exchange -> FakeServer.reply(exchange, 200, "application/octet-stream", new byte[1000]));

            assertEquals(1000, get(server, "/a", 1000).body().length);
        }
    }

    @Test
    void aBodyThatAnnouncesItsSizeIsRefusedWithoutReadingIt() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.on("/a", exchange -> FakeServer.reply(exchange, 200, "application/octet-stream", new byte[5000]));

            var e = assertThrows(BackendException.class, () -> get(server, "/a", 1000));
            assertTrue(e.getMessage().contains("too big"));
        }
    }

    @Test
    void aChunkedBodyIsCutAtTheLimit() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.on("/a", exchange -> FakeServer.replyChunked(exchange, new byte[5000]));

            var e = assertThrows(BackendException.class, () -> get(server, "/a", 1000));
            assertTrue(e.getMessage().contains("too big"));
        }
    }

    @Test
    void aRequestDoesNotAskForAnUpgradeToHttp2() throws Exception {
        try (FakeServer server = new FakeServer()) {
            java.util.concurrent.atomic.AtomicReference<String> upgrade = new java.util.concurrent.atomic.AtomicReference<>("not asked");
            server.on("/a", exchange -> {
                upgrade.set(exchange.getRequestHeaders().getFirst("Upgrade"));
                FakeServer.reply(exchange, 200, "application/json", "{}".getBytes());
            });

            Http.send(Http.request(java.net.URI.create(server.url() + "/a"), 5).POST(java.net.http.HttpRequest.BodyPublishers.ofString("{}")).build());

            // an ASGI server would take the body for upgrade data, so the request must not carry the header
            assertNull(upgrade.get());
        }
    }

    @Test
    void aRedirectIsNotFollowed() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.on("/a", exchange -> {
                exchange.getResponseHeaders().add("Location", "http://169.254.169.254/latest");
                exchange.sendResponseHeaders(302, -1);
                exchange.close();
            });

            assertEquals(302, get(server, "/a", 1000).statusCode());
        }
    }

    @Test
    void aServerThatIsNotThereIsReportedByItsHost() {
        var e = assertThrows(BackendException.class,
                () -> Http.send(Http.request(URI.create("http://127.0.0.1:9/x"), 2).GET().build()));
        assertTrue(e.getMessage().contains("Cannot reach 127.0.0.1"));
    }
}
