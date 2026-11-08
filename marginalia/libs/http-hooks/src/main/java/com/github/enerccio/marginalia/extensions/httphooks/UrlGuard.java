package com.github.enerccio.marginalia.extensions.httphooks;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;

/**
 * The addresses a backend or a hook may call. A server in the cloud must not be a way to reach the machine of the user, so
 * the loopback, local network and link-local addresses are refused unless the backend is marked as local.
 * <p>
 * The host is resolved here and again by the HTTP client when it connects. The JVM keeps a lookup for 30 seconds by default
 * (networkaddress.cache.ttl), so the client gets the same answer, but a JVM configured without that cache would let a name that
 * changes its address between the two lookups through.
 */
public final class UrlGuard {

    private UrlGuard() {
    }

    /**
     * @return the address, parsed
     * @throws BackendException when it is not an http(s) address, or it points to a private address of a non local backend
     */
    public static URI check(String url, boolean local) throws BackendException {
        if (url == null || url.isBlank()) {
            throw new BackendException("The address is empty");
        }
        URI uri;
        try {
            uri = URI.create(url.strip());
        } catch (IllegalArgumentException e) {
            throw new BackendException("Not an address: " + url, e);
        }
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) || uri.getHost() == null) {
            throw new BackendException("Only http and https addresses are allowed: " + url);
        }
        if (!local) {
            try {
                for (InetAddress address : InetAddress.getAllByName(uri.getHost())) {
                    if (isPrivate(address)) {
                        throw new BackendException("The address " + uri.getHost()
                                + " is on this machine or its network; mark the backend as local to use it");
                    }
                }
            } catch (UnknownHostException e) {
                throw new BackendException("Unknown host " + uri.getHost(), e);
            }
        }
        return uri;
    }

    /**
     * Whether the address is the machine itself or a network that is not the internet: the loopback, the any address, the
     * private, link-local, shared (100.64.0.0/10), unique-local (fc00::/7) and multicast ranges, and the IPv4 addresses that
     * an IPv6 address carries (IPv4-compatible, NAT64 64:ff9b::/96).
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

    /**
     * The server an address belongs to, {@code scheme://host:port} in lower case: what two settings that call the same server
     * have in common (the key of a {@link GpuGate}). An unusable address is returned stripped and in lower case.
     */
    public static String origin(String url) {
        if (url == null) {
            return "";
        }
        try {
            URI uri = URI.create(url.strip());
            if (uri.getScheme() != null && uri.getHost() != null) {
                String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
                int port = uri.getPort() >= 0 ? uri.getPort() : scheme.equals("https") ? 443 : 80;
                return scheme + "://" + uri.getHost().toLowerCase(Locale.ROOT) + ":" + port;
            }
        } catch (IllegalArgumentException ignored) {
            // not an address
        }
        return url.strip().toLowerCase(Locale.ROOT);
    }
}
