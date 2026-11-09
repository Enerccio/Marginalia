package com.github.enerccio.marginalia.extensions.httphooks;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;

class UrlGuardTest {

    @Test
    void localBackendsMayUsePrivateAddresses() throws Exception {
        assertEquals("localhost", UrlGuard.check("http://localhost:8188", true).getHost());
        assertEquals("192.168.1.5", UrlGuard.check("http://192.168.1.5:8188/prompt", true).getHost());
    }

    @Test
    void remoteBackendsMayNot() {
        assertThrows(BackendException.class, () -> UrlGuard.check("http://localhost:8188", false));
        assertThrows(BackendException.class, () -> UrlGuard.check("http://127.0.0.1/x", false));
        assertThrows(BackendException.class, () -> UrlGuard.check("http://10.0.0.3/x", false));
        assertThrows(BackendException.class, () -> UrlGuard.check("http://169.254.169.254/latest", false));
        assertThrows(BackendException.class, () -> UrlGuard.check("http://[::1]/x", false));
        assertThrows(BackendException.class, () -> UrlGuard.check("http://[fd12:3456::1]/x", false));
        assertThrows(BackendException.class, () -> UrlGuard.check("http://100.64.1.1/x", false));
    }

    @Test
    void onlyHttpIsAllowed() {
        assertThrows(BackendException.class, () -> UrlGuard.check("file:///etc/passwd", true));
        assertThrows(BackendException.class, () -> UrlGuard.check("ftp://example.com/x", true));
        assertThrows(BackendException.class, () -> UrlGuard.check("not a url", true));
        assertThrows(BackendException.class, () -> UrlGuard.check("", true));
        assertThrows(BackendException.class, () -> UrlGuard.check(null, true));
    }

    private static boolean isPrivate(String literal) throws Exception {
        return UrlGuard.isPrivate(InetAddress.getByName(literal));
    }

    @Test
    void theRangesJavaDoesNotKnowAreRefused() throws Exception {
        assertTrue(isPrivate("100.64.0.1"));        // shared address space (carrier grade NAT, Tailscale)
        assertTrue(isPrivate("100.127.255.254"));
        assertTrue(isPrivate("fc00::1"));           // unique-local, the IPv6 private network
        assertTrue(isPrivate("fd00::1"));
        assertTrue(isPrivate("0.1.2.3"));           // "this network"
        assertTrue(isPrivate("224.0.0.1"));
    }

    @Test
    void anIpv4AddressInsideAnIpv6OneIsJudgedAsIpv4() throws Exception {
        assertTrue(isPrivate("::ffff:10.0.0.1"));
        assertTrue(isPrivate("::10.0.0.1"));
        assertTrue(isPrivate("64:ff9b::a00:1"));    // NAT64 of 10.0.0.1
        assertFalse(isPrivate("64:ff9b::808:808")); // NAT64 of 8.8.8.8
    }

    @Test
    void publicAddressesPass() throws Exception {
        assertFalse(isPrivate("8.8.8.8"));
        assertFalse(isPrivate("100.128.0.1"));
        assertFalse(isPrivate("2001:4860:4860::8888"));
        assertEquals("8.8.8.8", UrlGuard.check("https://8.8.8.8/v1", false).getHost());
    }

    @Test
    void theOriginIsTheServerNotTheLocationOnIt() {
        assertEquals("http://comfy.lan:8188", UrlGuard.origin("http://Comfy.LAN:8188/prompt"));
        assertEquals("http://comfy.lan:80", UrlGuard.origin("http://comfy.lan"));
        assertEquals("https://x.example:443", UrlGuard.origin(" https://x.example/v1 "));
        assertEquals("", UrlGuard.origin(null));
        assertEquals("not a url", UrlGuard.origin("Not A Url"));
    }
}
