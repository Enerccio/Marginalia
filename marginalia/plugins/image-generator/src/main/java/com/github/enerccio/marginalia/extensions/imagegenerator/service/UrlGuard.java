package com.github.enerccio.marginalia.extensions.imagegenerator.service;

import java.net.InetAddress;
import java.net.URI;

/**
 * The addresses a backend or a hook may call. A server in the cloud must not be a way to reach the machine of the
 * administrator, so the loopback, local network and link-local addresses are refused unless the backend is marked as local.
 */
public final class UrlGuard {

    private UrlGuard() {
    }

    /**
     * @return the address, parsed
     * @throws ImageBackend.ImageException when it is not an http(s) address, or it points to a private address of a non local backend
     */
    public static URI check(String url, boolean local) throws ImageBackend.ImageException {
        if (url == null || url.isBlank()) {
            throw new ImageBackend.ImageException("The address is empty");
        }
        URI uri;
        try {
            uri = URI.create(url.strip());
        } catch (IllegalArgumentException e) {
            throw new ImageBackend.ImageException("Not an address: " + url, e);
        }
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) || uri.getHost() == null) {
            throw new ImageBackend.ImageException("Only http and https addresses are allowed: " + url);
        }
        if (!local) {
            try {
                for (InetAddress address : InetAddress.getAllByName(uri.getHost())) {
                    if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isSiteLocalAddress()
                            || address.isLinkLocalAddress()) {
                        throw new ImageBackend.ImageException("The address " + uri.getHost()
                                + " is on this machine or its network; mark the backend as local to use it");
                    }
                }
            } catch (java.net.UnknownHostException e) {
                throw new ImageBackend.ImageException("Unknown host " + uri.getHost(), e);
            }
        }
        return uri;
    }
}
