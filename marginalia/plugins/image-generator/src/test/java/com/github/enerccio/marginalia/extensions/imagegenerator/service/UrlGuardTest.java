package com.github.enerccio.marginalia.extensions.imagegenerator.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UrlGuardTest {

    @Test
    void localBackendsMayUsePrivateAddresses() throws Exception {
        assertEquals("localhost", UrlGuard.check("http://localhost:8188", true).getHost());
        assertEquals("192.168.1.5", UrlGuard.check("http://192.168.1.5:8188/prompt", true).getHost());
    }

    @Test
    void remoteBackendsMayNot() {
        assertThrows(ImageBackend.ImageException.class, () -> UrlGuard.check("http://localhost:8188", false));
        assertThrows(ImageBackend.ImageException.class, () -> UrlGuard.check("http://127.0.0.1/x", false));
        assertThrows(ImageBackend.ImageException.class, () -> UrlGuard.check("http://10.0.0.3/x", false));
        assertThrows(ImageBackend.ImageException.class, () -> UrlGuard.check("http://169.254.169.254/latest", false));
        assertThrows(ImageBackend.ImageException.class, () -> UrlGuard.check("http://[::1]/x", false));
    }

    @Test
    void onlyHttpIsAllowed() {
        assertThrows(ImageBackend.ImageException.class, () -> UrlGuard.check("file:///etc/passwd", true));
        assertThrows(ImageBackend.ImageException.class, () -> UrlGuard.check("ftp://example.com/x", true));
        assertThrows(ImageBackend.ImageException.class, () -> UrlGuard.check("not a url", true));
        assertThrows(ImageBackend.ImageException.class, () -> UrlGuard.check("", true));
        assertThrows(ImageBackend.ImageException.class, () -> UrlGuard.check(null, true));
    }
}
