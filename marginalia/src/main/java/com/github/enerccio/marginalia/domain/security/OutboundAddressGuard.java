package com.github.enerccio.marginalia.domain.security;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * The one place that decides whether the server may call an address someone typed (a webhook, a backend of a plugin), and that
 * makes the HTTP client connect to the address that was checked.
 * <p>
 * Checking a host and then letting the HTTP client look it up again lets a name through whose DNS answer changes between the two
 * lookups (DNS rebinding: a public address for the check, {@code 127.0.0.1} for the connection). A client built with
 * {@link #publicOnly(HttpClient.Builder)} looks the host up once, when it opens the connection, refuses it when one of the
 * addresses is private ({@link #isPrivate(InetAddress)}) and then:
 * <ul>
 *     <li>{@code http}: connects to the address it checked. The client is told that this address is a proxy, so it sends the whole
 *     URL in the request line ({@code GET http://host/path}) with the usual {@code Host} header, which every HTTP/1.1 server
 *     must accept (RFC 9112, 3.2.2);</li>
 *     <li>{@code https}: connects directly, which makes the client look the host up a second time. A changed answer can't be
 *     used there: the certificate of the server it reaches must be valid for the host name, which a server in the private
 *     network can not show for the name of someone else, and no request is sent before that check.</li>
 * </ul>
 * The client refuses with a {@link RefusedAddressException} (unchecked: the client does not wrap what a proxy selector throws),
 * thrown by {@code send} itself; {@link #refusal(Throwable)} finds it in a chain of causes.
 * <p>
 * Plain JDK, no Spring: plugins use it directly (the {@code http-hooks} library does).
 */
public final class OutboundAddressGuard {

    /** Looks up the addresses of a host name. */
    @FunctionalInterface
    public interface Resolver {
        InetAddress[] resolve(String host) throws UnknownHostException;
    }

    /** The guard of the application: the lookup of the JVM, {@link #isPrivate(InetAddress)}. */
    public static final OutboundAddressGuard DEFAULT = new OutboundAddressGuard(InetAddress::getAllByName, OutboundAddressGuard::isPrivate);

    private final Resolver resolver;
    private final Predicate<InetAddress> refused;
    private final ProxySelector selector = new PinningSelector();

    /**
     * A guard with its own lookup and judgement, for tests (a name whose answer changes, a "private" address that is reachable).
     */
    public OutboundAddressGuard(Resolver resolver, Predicate<InetAddress> refused) {
        this.resolver = resolver;
        this.refused = refused;
    }

    /** Refused: the address is private, or the host is not known. The message names the host, never the path. */
    public static class RefusedAddressException extends IllegalArgumentException {
        private final String host;

        public RefusedAddressException(String host, String message) {
            super(message);
            this.host = host;
        }

        public String getHost() {
            return host;
        }
    }

    /**
     * Looks the host up once and checks every address.
     *
     * @return the addresses, all of them public
     * @throws RefusedAddressException when one of them is private, or the host is not known (one answer for both would say less
     *                                 about the internal names; the message keeps them apart for the user who typed the address)
     */
    public InetAddress[] resolvePublic(String host) {
        if (host == null || host.isBlank()) {
            throw new RefusedAddressException(host, "The address has no host");
        }
        InetAddress[] addresses;
        try {
            addresses = resolver.resolve(host);
        } catch (UnknownHostException e) {
            throw new RefusedAddressException(host, "Unknown host " + host);
        }
        if (addresses == null || addresses.length == 0) {
            throw new RefusedAddressException(host, "Unknown host " + host);
        }
        for (InetAddress address : addresses) {
            if (refused.test(address)) {
                throw new RefusedAddressException(host, "The address " + host + " is on this machine or its network");
            }
        }
        return addresses;
    }

    /** The builder, with the proxy selector that looks the host up once, refuses a private one and connects to what it checked. */
    public HttpClient.Builder publicOnly(HttpClient.Builder builder) {
        return builder.proxy(selector);
    }

    /** The selector {@link #publicOnly(HttpClient.Builder)} sets. */
    public ProxySelector selector() {
        return selector;
    }

    /** @return the refusal in the chain of causes, or null */
    public static RefusedAddressException refusal(Throwable error) {
        for (Throwable t = error; t != null; t = t.getCause() == t ? null : t.getCause()) {
            if (t instanceof RefusedAddressException refusal) {
                return refusal;
            }
        }
        return null;
    }

    /**
     * Whether the address is the machine itself or a network that is not the internet: the loopback, the any address, the
     * private, link-local (the metadata service of a cloud host is one), shared (100.64.0.0/10), "this network" (0.0.0.0/8),
     * unique-local (fc00::/7) and multicast ranges, and the IPv4 addresses that an IPv6 address carries (IPv4-mapped,
     * IPv4-compatible, NAT64 64:ff9b::/96).
     */
    public static boolean isPrivate(InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isSiteLocalAddress()
                || address.isLinkLocalAddress() || address.isMulticastAddress()) {
            return true;
        }
        byte[] b = address.getAddress();
        if (b.length == 4) {
            int first = b[0] & 0xFF;
            int second = b[1] & 0xFF;
            return first == 0 || (first == 100 && (second & 0xC0) == 0x40);
        }
        if ((b[0] & 0xFE) == 0xFC) {
            return true;
        }
        if (address instanceof Inet6Address v6 && v6.isIPv4CompatibleAddress()) {
            return embedded(b);
        }
        // 64:ff9b::/96, the NAT64 prefix: the last four bytes are an IPv4 address
        boolean nat64 = b[0] == 0 && b[1] == 0x64 && (b[2] & 0xFF) == 0xFF && (b[3] & 0xFF) == 0x9B;
        for (int i = 4; nat64 && i < 12; i++) {
            nat64 = b[i] == 0;
        }
        return nat64 && embedded(b);
    }

    private static boolean embedded(byte[] v6) {
        try {
            return isPrivate(InetAddress.getByAddress(new byte[]{v6[12], v6[13], v6[14], v6[15]}));
        } catch (UnknownHostException e) {
            return true;
        }
    }

    private final class PinningSelector extends ProxySelector {

        @Override
        public List<Proxy> select(URI uri) {
            String host = uri.getHost();
            InetAddress[] addresses = resolvePublic(host);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!scheme.equals("http")) {
                // https: the certificate check of the client stands for the second lookup (see the class)
                return List.of(Proxy.NO_PROXY);
            }
            int port = uri.getPort() >= 0 ? uri.getPort() : 80;
            return List.of(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(addresses[0], port)));
        }

        @Override
        public void connectFailed(URI uri, SocketAddress address, IOException error) {
            // one address is tried; the client reports the failure
        }
    }
}
