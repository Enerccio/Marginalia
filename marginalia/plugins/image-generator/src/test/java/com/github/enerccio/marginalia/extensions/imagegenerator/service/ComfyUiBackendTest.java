package com.github.enerccio.marginalia.extensions.imagegenerator.service;

import com.github.enerccio.marginalia.extensions.imagegenerator.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.imagegenerator.model.BackendType;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ComfyUiBackendTest {

    private static final String WORKFLOW = "{\"6\": {\"inputs\": {\"text\": \"{{prompt}}\", \"seed\": {{seed}}}}}";
    private static final ImageRequest REQUEST = new ImageRequest("a castle", "", 7L, 512, 512);

    private BackendConfig config(FakeServer server) {
        BackendConfig config = new BackendConfig();
        config.setType(BackendType.COMFYUI);
        config.setBaseUrl(server.url() + "/");
        config.setWorkflow(WORKFLOW);
        config.setTimeoutSeconds(10);
        return config;
    }

    @Test
    void queuesPollsAndFetchesTheImage() throws Exception {
        try (FakeServer server = new FakeServer()) {
            AtomicInteger polls = new AtomicInteger();
            server.json("/prompt", 200, "{\"prompt_id\": \"abc\"}");
            server.on("/history/abc", exchange -> {
                // not ready the first time
                String body = polls.incrementAndGet() < 2 ? "{}" : """
                        {"abc": {"status": {"completed": true}, "outputs": {
                          "9": {"images": [{"filename": "preview.png", "subfolder": "", "type": "temp"}]},
                          "10": {"images": [{"filename": "final image.png", "subfolder": "sub", "type": "output"}]}}}}""";
                FakeServer.reply(exchange, 200, "application/json", body.getBytes());
            });
            server.on("/view", exchange -> FakeServer.reply(exchange, 200, "image/png", FakeServer.PNG));

            byte[] image = new ComfyUiBackend(config(server), "secret", 10).generate(REQUEST, () -> false);

            assertArrayEquals(FakeServer.PNG, image);
            FakeServer.Call queued = server.calls.getFirst();
            assertEquals("POST", queued.method());
            assertEquals("Bearer secret", queued.authorization());
            JsonObject sent = JsonParser.parseString(queued.body()).getAsJsonObject();
            assertEquals("a castle", sent.getAsJsonObject("prompt").getAsJsonObject("6").getAsJsonObject("inputs").get("text").getAsString());
            assertEquals(7, sent.getAsJsonObject("prompt").getAsJsonObject("6").getAsJsonObject("inputs").get("seed").getAsInt());
            // the saved image is fetched, not the preview, and the name is encoded
            FakeServer.Call view = server.calls.getLast();
            assertEquals("filename=final+image.png&subfolder=sub&type=output", view.query());
        }
    }

    @Test
    void anErrorOfTheServerIsShown() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/prompt", 400, "{\"error\": {\"message\": \"Prompt outputs failed validation\"}}");

            var e = assertThrows(ImageBackend.ImageException.class,
                    () -> new ComfyUiBackend(config(server), null, 10).generate(REQUEST, () -> false));

            assertTrue(e.getMessage().contains("400"));
            assertTrue(e.getMessage().contains("failed validation"));
        }
    }

    @Test
    void aWorkflowThatEndsWithoutAnImageIsAnError() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/prompt", 200, "{\"prompt_id\": \"abc\"}");
            server.json("/history/abc", 200, """
                    {"abc": {"outputs": {}, "status": {"status_str": "error", "completed": false, "messages": [
                      ["execution_error", {"exception_message": "Out of memory"}]]}}}""");

            var e = assertThrows(ImageBackend.ImageException.class,
                    () -> new ComfyUiBackend(config(server), null, 10).generate(REQUEST, () -> false));

            assertTrue(e.getMessage().contains("Out of memory"), e.getMessage());
        }
    }

    @Test
    void cancellingInterruptsComfyUi() throws Exception {
        try (FakeServer server = new FakeServer()) {
            AtomicBoolean cancelled = new AtomicBoolean();
            server.json("/prompt", 200, "{\"prompt_id\": \"abc\"}");
            server.on("/history/abc", exchange -> {
                cancelled.set(true);
                FakeServer.reply(exchange, 200, "application/json", "{}".getBytes());
            });
            server.json("/interrupt", 200, "{}");

            var e = assertThrows(ImageBackend.ImageException.class,
                    () -> new ComfyUiBackend(config(server), null, 10).generate(REQUEST, cancelled::get));

            assertEquals("Cancelled", e.getMessage());
            assertEquals(1, server.count("/interrupt"));
        }
    }

    @Test
    void aSlowJobTimesOut() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/prompt", 200, "{\"prompt_id\": \"abc\"}");
            server.json("/history/abc", 200, "{}");
            server.json("/interrupt", 200, "{}");
            BackendConfig config = config(server);
            config.setTimeoutSeconds(1);

            var e = assertThrows(ImageBackend.ImageException.class,
                    () -> new ComfyUiBackend(config, null, 100).generate(REQUEST, () -> false));

            assertTrue(e.getMessage().contains("did not finish"));
            assertEquals(1, server.count("/interrupt"));
        }
    }

    @Test
    void testChecksTheServer() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/system_stats", 200, "{}");
            assertDoesNotThrow(() -> new ComfyUiBackend(config(server), null, 10).test());
        }
        BackendConfig down = new BackendConfig();
        down.setBaseUrl("http://127.0.0.1:1");
        assertThrows(ImageBackend.ImageException.class, () -> new ComfyUiBackend(down, null, 10).test());
    }

    @Test
    void aRemoteBackendCannotReachThisMachine() throws Exception {
        try (FakeServer server = new FakeServer()) {
            BackendConfig config = config(server);
            config.setLocal(false);

            var e = assertThrows(ImageBackend.ImageException.class,
                    () -> new ComfyUiBackend(config, null, 10).generate(REQUEST, () -> false));

            assertTrue(e.getMessage().contains("local"));
            assertEquals(0, server.calls.size());
        }
    }
}
