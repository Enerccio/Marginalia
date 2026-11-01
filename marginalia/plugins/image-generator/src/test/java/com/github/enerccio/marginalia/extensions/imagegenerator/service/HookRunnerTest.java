package com.github.enerccio.marginalia.extensions.imagegenerator.service;

import com.github.enerccio.marginalia.extensions.imagegenerator.model.HttpHook;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HookRunnerTest {

    @Test
    void anEmptyHookDoesNothing() {
        assertDoesNotThrow(() -> HookRunner.run(null, true));
        assertDoesNotThrow(() -> HookRunner.run(new HttpHook(), true));
    }

    @Test
    void sendsTheMethodHeadersAndBody() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/free", 200, "{}");
            HttpHook hook = new HttpHook();
            hook.setUrl(server.url() + "/free");
            hook.setHeaders("X-Token: abc\nnot a header line\nAuthorization: Bearer t");
            hook.setBody("{\"unload_models\": true}");

            HookRunner.run(hook, true);

            FakeServer.Call call = server.calls.getFirst();
            assertEquals("POST", call.method());
            assertEquals("{\"unload_models\": true}", call.body());
            assertEquals("Bearer t", call.authorization());
        }
    }

    @Test
    void aGetHasNoBody() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/wake", 200, "{}");
            HttpHook hook = new HttpHook();
            hook.setUrl(server.url() + "/wake");
            hook.setMethod("get");
            hook.setBody("ignored");

            HookRunner.run(hook, true);

            assertEquals("GET", server.calls.getFirst().method());
            assertEquals("", server.calls.getFirst().body());
        }
    }

    @Test
    void anErrorStatusFails() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/free", 500, "{\"error\": \"boom\"}");
            HttpHook hook = new HttpHook();
            hook.setUrl(server.url() + "/free");

            var e = assertThrows(ImageBackend.ImageException.class, () -> HookRunner.run(hook, true));
            assertTrue(e.getMessage().contains("500"));
        }
    }

    @Test
    void aPrivateAddressIsRefusedForARemoteBackend() {
        HttpHook hook = new HttpHook();
        hook.setUrl("http://127.0.0.1:9/free");

        assertThrows(ImageBackend.ImageException.class, () -> HookRunner.run(hook, false));
    }
}
