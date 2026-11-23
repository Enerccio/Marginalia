package com.github.enerccio.marginalia.security;

import com.github.enerccio.marginalia.domain.security.OutboundAddressGuard;
import com.github.enerccio.marginalia.domain.security.OutboundAddressGuard.RefusedAddressException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The server-side request forgery cases of the outbound address guard: the private ranges, a name that resolves to a private address,
 * and DNS rebinding (an answer that changes between the check and the connection). The tests stand 127.0.0.1 in for a public address
 * and 127.0.0.2 for a private one, with a lookup the test controls, so no network is needed.
 */
class OutboundAddressGuardTest {

    private static final InetAddress REACHABLE = address("127.0.0.1");
    private static final InetAddress INTERNAL = address("127.0.0.2");

    private HttpServer server;
    private final List<String> seen = new CopyOnWriteArrayList<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(REACHABLE, 0), 0);
        server.createContext("/", exchange -> {
            seen.add(exchange.getRequestURI() + " Host=" + exchange.getRequestHeaders().getFirst("Host"));
            byte[] body = "ok".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private int port() {
        return server.getAddress().getPort();
    }

    private static InetAddress address(String literal) {
        try {
            return InetAddress.getByName(literal);
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }

    /** A guard that refuses only {@link #INTERNAL}, with the given lookup. */
    private static OutboundAddressGuard guard(OutboundAddressGuard.Resolver resolver) {
        return new OutboundAddressGuard(resolver, INTERNAL::equals);
    }

    private static HttpClient client(OutboundAddressGuard guard) {
        return guard.publicOnly(HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NEVER).connectTimeout(Duration.ofSeconds(5))).build();
    }

    private HttpResponse<String> get(HttpClient client, String host) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://" + host + ":" + port() + "/hook?x=1")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void localMeansEverythingThatIsNotTheInternet() {
        for (String local : new String[]{"127.0.0.1", "127.1.2.3", "10.0.0.5", "172.16.4.1", "192.168.1.20", "169.254.169.254",
                "100.64.0.1", "100.127.255.254", "0.0.0.0", "0.1.2.3", "224.0.0.1", "::1", "fe80::1", "fd12:3456::1", "fc00::1",
                "::", "::ffff:127.0.0.1", "::ffff:10.1.1.1", "::10.0.0.1", "64:ff9b::a00:1"}) {
            assertThat(OutboundAddressGuard.isPrivate(address(local))).as(local).isTrue();
        }
        for (String internet : new String[]{"8.8.8.8", "1.1.1.1", "93.184.216.34", "172.32.0.1", "100.128.0.1",
                "2606:4700:4700::1111", "64:ff9b::808:808"}) {
            assertThat(OutboundAddressGuard.isPrivate(address(internet))).as(internet).isFalse();
        }
    }

    @Test
    void aNameWithOnePrivateAddressAmongPublicOnesIsRefused() {
        OutboundAddressGuard guard = guard(host -> new InetAddress[]{REACHABLE, INTERNAL});

        assertThatThrownBy(() -> guard.resolvePublic("mixed.test")).isInstanceOf(RefusedAddressException.class)
                .hasMessageContaining("mixed.test");
    }

    @Test
    void anUnknownOrEmptyHostIsRefused() {
        OutboundAddressGuard guard = guard(host -> {
            throw new UnknownHostException(host);
        });

        assertThatThrownBy(() -> guard.resolvePublic("nowhere.test")).isInstanceOf(RefusedAddressException.class)
                .hasMessageContaining("Unknown host");
        assertThatThrownBy(() -> guard.resolvePublic(" ")).isInstanceOf(RefusedAddressException.class);
        assertThatThrownBy(() -> guard(host -> new InetAddress[0]).resolvePublic("empty.test"))
                .isInstanceOf(RefusedAddressException.class);
    }

    @Test
    void theDefaultGuardRefusesTheLiteralsOfThePrivateNetworks() {
        for (String host : new String[]{"127.0.0.1", "localhost", "169.254.169.254", "[::1]", "10.0.0.1"}) {
            assertThatThrownBy(() -> OutboundAddressGuard.DEFAULT.resolvePublic(URI.create("http://" + host + "/").getHost()))
                    .as(host).isInstanceOf(RefusedAddressException.class);
        }
        assertThat(OutboundAddressGuard.DEFAULT.resolvePublic("8.8.8.8")).containsExactly(address("8.8.8.8"));
    }

    @Test
    void aPublicNameIsCalledAtTheAddressThatWasCheckedWithItsOwnHostHeader() throws Exception {
        AtomicInteger lookups = new AtomicInteger();
        // the first answer is the one that counts; any later lookup would get the internal address
        OutboundAddressGuard guard = guard(host -> new InetAddress[]{lookups.getAndIncrement() == 0 ? REACHABLE : INTERNAL});

        HttpResponse<String> response = get(client(guard), "hooks.test");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(lookups.get()).as("one lookup, at the connection").isEqualTo(1);
        assertThat(seen).containsExactly("http://hooks.test:" + port() + "/hook?x=1 Host=hooks.test:" + port());
    }

    @Test
    void aNameThatRebindsToAPrivateAddressAfterTheCheckIsNotCalled() throws Exception {
        AtomicInteger lookups = new AtomicInteger();
        // DNS rebinding: public when the caller checks the address, private when the client would connect
        OutboundAddressGuard guard = guard(host -> new InetAddress[]{lookups.getAndIncrement() == 0 ? REACHABLE : INTERNAL});
        guard.resolvePublic("rebind.test");

        HttpClient client = client(guard);
        assertThatThrownBy(() -> get(client, "rebind.test")).isInstanceOf(RefusedAddressException.class)
                .hasMessageContaining("rebind.test");
        assertThat(seen).isEmpty();
    }

    @Test
    void aPrivateLiteralIsNotCalled() {
        HttpClient client = client(OutboundAddressGuard.DEFAULT);

        assertThatThrownBy(() -> get(client, "127.0.0.1")).isInstanceOf(RefusedAddressException.class);
        assertThat(seen).isEmpty();
    }

    @Test
    void anAsynchronousCallIsRefusedAsWell() {
        HttpClient client = client(guard(host -> new InetAddress[]{INTERNAL}));
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://internal.test:" + port() + "/")).build();

        assertThatThrownBy(() -> client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).get())
                .satisfies(e -> assertThat(OutboundAddressGuard.refusal(e)).isNotNull());
        assertThat(seen).isEmpty();
    }

    @Test
    void httpsIsCheckedAndThenConnectedDirectlyForTheCertificateCheck() {
        OutboundAddressGuard guard = guard(host -> new InetAddress[]{host.startsWith("internal") ? INTERNAL : REACHABLE});

        assertThat(guard.selector().select(URI.create("https://public.test/v1"))).containsExactly(Proxy.NO_PROXY);
        assertThatThrownBy(() -> guard.selector().select(URI.create("https://internal.test/v1")))
                .isInstanceOf(RefusedAddressException.class);
        assertThat(guard.selector().select(URI.create("http://public.test/v1")))
                .containsExactly(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(REACHABLE, 80)));
    }

    @Test
    void theRefusalIsFoundInAChainOfCauses() {
        RefusedAddressException refusal = new RefusedAddressException("x.test", "refused");

        assertThat(OutboundAddressGuard.refusal(new IOException(new RuntimeException(refusal)))).isSameAs(refusal);
        assertThat(OutboundAddressGuard.refusal(new IOException("other"))).isNull();
        assertThat(OutboundAddressGuard.refusal(null)).isNull();
    }
}
