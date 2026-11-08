package com.github.enerccio.marginalia.extensions.httphooks;

import org.junit.jupiter.api.Test;

import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

class HookRunnerTest {

    /** The "protection" of the tests: reversible, and visibly not the plain text. */
    private static final UnaryOperator<String> REVEAL = text -> new StringBuilder(text).reverse().toString();

    @Test
    void anEmptyHookDoesNothing() {
        assertDoesNotThrow(() -> HookRunner.run(null, true, null));
        assertDoesNotThrow(() -> HookRunner.run(new HttpHook(), true, null));
    }

    @Test
    void sendsTheMethodHeadersAndBody() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/free", 200, "{}");
            HttpHook hook = new HttpHook();
            hook.setUrl(server.url() + "/free");
            hook.setHeaders("X-Token: abc\nnot a header line\nAuthorization: Bearer t");
            hook.setBody("{\"unload_models\": true}");

            HookRunner.run(hook, true, null);

            FakeServer.Call call = server.calls.getFirst();
            assertEquals("POST", call.method());
            assertEquals("{\"unload_models\": true}", call.body());
            assertEquals("Bearer t", call.authorization());
        }
    }

    @Test
    void protectedHeadersAreOpenedByTheCallerAndWinOverThePlainOnes() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/free", 200, "{}");
            HttpHook hook = new HttpHook();
            hook.setUrl(server.url() + "/free");
            hook.setHeaders("Authorization: Bearer old");
            hook.setProtectedHeaders(REVEAL.apply("Authorization: Bearer secret"));

            HookRunner.run(hook, true, REVEAL);

            assertEquals("Bearer secret", server.calls.getFirst().authorization());
        }
    }

    @Test
    void protectedHeadersTheCallerCannotOpenAreLeftOut() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/free", 200, "{}");
            HttpHook hook = new HttpHook();
            hook.setUrl(server.url() + "/free");
            hook.setProtectedHeaders("garbage");

            HookRunner.run(hook, true, null);

            assertNull(server.calls.getFirst().authorization());
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

            HookRunner.run(hook, true, null);

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

            var e = assertThrows(BackendException.class, () -> HookRunner.run(hook, true, null));
            assertTrue(e.getMessage().contains("500"));
        }
    }

    @Test
    void aHugeAnswerOfAHookIsRefused() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.on("/free", exchange -> FakeServer.replyChunked(exchange, new byte[2 * 1024 * 1024]));
            HttpHook hook = new HttpHook();
            hook.setUrl(server.url() + "/free");

            var e = assertThrows(BackendException.class, () -> HookRunner.run(hook, true, null));
            assertTrue(e.getMessage().contains("too big"));
        }
    }

    @Test
    void aPrivateAddressIsRefusedForARemoteBackend() {
        HttpHook hook = new HttpHook();
        hook.setUrl("http://127.0.0.1:9/free");

        assertThrows(BackendException.class, () -> HookRunner.run(hook, false, null));
    }

    private static HttpHook hook(FakeServer server, String path) {
        HttpHook hook = new HttpHook();
        hook.setUrl(server.url() + path);
        return hook;
    }

    @Test
    void aListRunsInOrderAndSkipsEmptyHooks() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/a", 200, "{}");
            server.json("/b", 200, "{}");
            server.json("/c", 200, "{}");

            HookRunner.runAll(java.util.Arrays.asList(hook(server, "/a"), new HttpHook(), null, hook(server, "/b"), hook(server, "/c")), true, null);

            assertEquals(java.util.List.of("/a", "/b", "/c"), server.calls.stream().map(FakeServer.Call::path).toList());
        }
    }

    @Test
    void aListStopsAtTheFirstFailureAndNamesIt() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/a", 200, "{}");
            server.json("/b", 500, "{\"error\": \"boom\"}");
            server.json("/c", 200, "{}");

            var e = assertThrows(BackendException.class,
                    () -> HookRunner.runAll(java.util.List.of(hook(server, "/a"), hook(server, "/b"), hook(server, "/c")), true, null));

            assertTrue(e.getMessage().startsWith("Request 2 of 3:"), e.getMessage());
            assertEquals(java.util.List.of("/a", "/b"), server.calls.stream().map(FakeServer.Call::path).toList());
        }
    }

    @Test
    void aSingleRequestKeepsItsPlainMessage() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/a", 500, "{}");

            var e = assertThrows(BackendException.class, () -> HookRunner.runAll(java.util.List.of(hook(server, "/a")), true, null));

            assertFalse(e.getMessage().startsWith("Request"), e.getMessage());
        }
    }

    @Test
    void theAfterListRunsEveryRequestAndReportsTheFirstFailure() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/a", 500, "{}");
            server.json("/b", 200, "{}");

            String error = HookRunner.runAllRegardless(java.util.List.of(hook(server, "/a"), hook(server, "/b")), true, null);

            assertNotNull(error);
            assertTrue(error.startsWith("Request 1 of 2:"), error);
            assertEquals(2, server.calls.size());
            assertNull(HookRunner.runAllRegardless(java.util.List.of(hook(server, "/b")), true, null));
            assertNull(HookRunner.runAllRegardless(null, true, null));
        }
    }

    @Test
    void aRequestWaitsAfterItsAnswer() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/a", 200, "{}");
            HttpHook hook = hook(server, "/a");
            hook.setWaitSeconds(1);

            long started = System.nanoTime();
            HookRunner.run(hook, true, null);

            assertTrue((System.nanoTime() - started) / 1_000_000 >= 950);
        }
    }

    @Test
    void aRequestHasItsOwnTimeout() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.on("/slow", exchange -> {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                FakeServer.reply(exchange, 200, "application/json", "{}".getBytes());
            });
            HttpHook hook = hook(server, "/slow");
            hook.setTimeoutSeconds(1);

            assertThrows(BackendException.class, () -> HookRunner.run(hook, true, null));
        }
    }

    @Test
    void theTimeoutAndTheWaitAreKeptInTheirLimits() {
        HttpHook hook = new HttpHook();
        assertEquals(60, hook.getTimeoutSeconds());
        hook.setTimeoutSeconds(-5);
        assertEquals(60, hook.getTimeoutSeconds());
        hook.setTimeoutSeconds(100000);
        assertEquals(600, hook.getTimeoutSeconds());
        hook.setWaitSeconds(-1);
        assertEquals(0, hook.getWaitSeconds());
        hook.setWaitSeconds(100000);
        assertEquals(300, hook.getWaitSeconds());
    }
}
