package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerCheckTest {

    private static BackendConfig backend(FakeServer server) {
        BackendConfig config = new BackendConfig();
        config.setBaseUrl(server.url() + "/");
        return config;
    }

    @Test
    void aServerWithTheModelLoadedSaysSo() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/api/model-info", 200, "{\"loaded\": true, \"class_name\": \"ChatterboxTurboTTS\", \"type\": \"turbo\"}");

            ServerCheck.Result result = ServerCheck.check(backend(server), null);

            assertTrue(result.modelLoaded());
            assertEquals("ChatterboxTurboTTS", result.model());
            assertEquals("GET", server.calls.getFirst().method());
            assertNull(server.calls.getFirst().authorization());
        }
    }

    @Test
    void aServerWithoutAModelIsStillThere() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/api/model-info", 200, "{\"loaded\": false, \"class_name\": null}");

            ServerCheck.Result result = ServerCheck.check(backend(server), null);

            assertFalse(result.modelLoaded());
            assertNull(result.model());
        }
    }

    @Test
    void theKeyIsOpenedAndSentAsABearerToken() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/api/model-info", 200, "{\"loaded\": true}");
            BackendConfig config = backend(server);
            config.setProtectedApiKey("yek");

            ServerCheck.check(config, text -> new StringBuilder(text).reverse().toString());

            assertEquals("Bearer key", server.calls.getFirst().authorization());
        }
    }

    @Test
    void anErrorAnswerFailsWithTheStatus() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/api/model-info", 500, "{\"error\": \"boom\"}");

            var e = assertThrows(BackendException.class, () -> ServerCheck.check(backend(server), null));

            assertTrue(e.getMessage().contains("500"), e.getMessage());
        }
    }

    @Test
    void somethingThatIsNotASpeechServerIsReported() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.on("/api/model-info", exchange -> FakeServer.reply(exchange, 200, "text/html", "<html>hello</html>".getBytes()));

            var e = assertThrows(BackendException.class, () -> ServerCheck.check(backend(server), null));

            assertTrue(e.getMessage().contains("speech server"), e.getMessage());
        }
    }

    @Test
    void anEmptyAddressAndAPrivateAddressOfARemoteServerAreRefused() {
        assertThrows(BackendException.class, () -> ServerCheck.check(new BackendConfig(), null));
        BackendConfig remote = new BackendConfig();
        remote.setBaseUrl("http://127.0.0.1:9");
        remote.setLocal(false);
        assertThrows(BackendException.class, () -> ServerCheck.check(remote, null));
    }
}
