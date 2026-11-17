package com.github.enerccio.marginalia.extensions.httphooks;

import com.github.enerccio.marginalia.domain.security.OutboundAddressGuard;

import java.net.InetAddress;
import java.net.URI;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The addresses a backend or a hook may call. A server in the cloud must not be a way to reach the machine of the user, so
 * the loopback, local network and link-local addresses are refused unless the backend is marked as local.
 * <p>
 * The judgement is the application's ({@link OutboundAddressGuard}). The check here gives the user an early, readable answer; the
 * address the call goes to is checked again by {@link Http} when it connects: an address {@link #check} passed for a backend that
 * is not local is sent by the client of {@link OutboundAddressGuard#publicOnly}, which looks the host up once and connects to the
 * address it checked, so a name whose DNS answer changes after the check (DNS rebinding) does not reach the private network.
 */
public final class UrlGuard {

    /** The addresses {@link #check} passed for a backend that is not local (by value, held while a request holds the URI). */
    private static final Set<URI> PUBLIC_ONLY = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

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
                OutboundAddressGuard.DEFAULT.resolvePublic(uri.getHost());
            } catch (OutboundAddressGuard.RefusedAddressException e) {
                throw new BackendException(e.getMessage().startsWith("Unknown host") ? e.getMessage()
                        : "The address " + uri.getHost() + " is on this machine or its network; mark the backend as local to use it", e);
            }
            holdToPublic(uri);
        }
        return uri;
    }

    /** Whether the address is the machine itself or a network that is not the internet ({@link OutboundAddressGuard#isPrivate}). */
    public static boolean isPrivate(InetAddress address) {
        return OutboundAddressGuard.isPrivate(address);
    }

    /**
     * Whether {@link #check} passed this address for a backend that is not local, so its call must connect to a public address
     * (a URI equal to it, checked as local by another backend at the same time, is held to that as well: it fails, never the other
     * way round).
     */
    static void holdToPublic(URI uri) {
        PUBLIC_ONLY.add(uri);
    }

    static boolean mustBePublic(URI uri) {
        return uri != null && PUBLIC_ONLY.contains(uri);
    }

    /**
     * The server an address belongs to, {@code scheme://host:port} in lower case: what two settings that call the same server
     * have in common. An unusable address is returned stripped and in lower case.
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
