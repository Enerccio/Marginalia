package com.github.enerccio.marginalia.extensions.imagegenerator.service;

import com.github.enerccio.marginalia.extensions.imagegenerator.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.imagegenerator.model.BackendType;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class OpenAiImagesBackendTest {

    private static final ImageRequest REQUEST = new ImageRequest("a castle", "blurry", 1L, 768, 512);

    private BackendConfig config(FakeServer server, String suffix) {
        BackendConfig config = new BackendConfig();
        config.setType(BackendType.OPENAI);
        config.setBaseUrl(server.url() + suffix);
        config.setModel("sdxl");
        config.setTimeoutSeconds(10);
        return config;
    }

    @Test
    void sendsTheRequestAndDecodesBase64() throws Exception {
        try (FakeServer server = new FakeServer()) {
            String b64 = Base64.getEncoder().encodeToString(FakeServer.PNG);
            server.json("/v1/images/generations", 200, "{\"data\": [{\"b64_json\": \"" + b64 + "\"}]}");

            byte[] image = new OpenAiImagesBackend(config(server, ""), "key").generate(REQUEST, () -> false);

            assertArrayEquals(FakeServer.PNG, image);
            FakeServer.Call call = server.calls.getFirst();
            assertEquals("Bearer key", call.authorization());
            JsonObject sent = JsonParser.parseString(call.body()).getAsJsonObject();
            assertEquals("sdxl", sent.get("model").getAsString());
            assertEquals("a castle", sent.get("prompt").getAsString());
            assertEquals("768x512", sent.get("size").getAsString());
            assertEquals(1, sent.get("n").getAsInt());
            assertEquals("blurry", sent.get("negative_prompt").getAsString());
            assertFalse(sent.has("response_format"));
        }
    }

    @Test
    void followsAnUrlInTheAnswer() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/v1/images/generations", 200, "{\"data\": [{\"url\": \"" + server.url() + "/files/1.png\"}]}");
            server.on("/files/1.png", exchange -> FakeServer.reply(exchange, 200, "image/png", FakeServer.PNG));

            byte[] image = new OpenAiImagesBackend(config(server, ""), null).generate(REQUEST, () -> false);

            assertArrayEquals(FakeServer.PNG, image);
            // the key is for the API, not for wherever the answer points
            assertNull(server.calls.getLast().authorization());
        }
    }

    @Test
    void aBaseAddressWithV1IsNotExtendedTwice() throws Exception {
        try (FakeServer server = new FakeServer()) {
            String b64 = Base64.getEncoder().encodeToString(FakeServer.PNG);
            server.json("/v1/images/generations", 200, "{\"data\": [{\"b64_json\": \"" + b64 + "\"}]}");

            new OpenAiImagesBackend(config(server, "/v1/"), null).generate(REQUEST, () -> false);

            assertEquals(1, server.count("/v1/images/generations"));
        }
    }

    @Test
    void errorsAndEmptyAnswersAreExplained() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/v1/images/generations", 401, "{\"error\": {\"message\": \"bad key\"}}");
            var e = assertThrows(ImageBackend.ImageException.class,
                    () -> new OpenAiImagesBackend(config(server, ""), "x").generate(REQUEST, () -> false));
            assertTrue(e.getMessage().contains("401") && e.getMessage().contains("bad key"));
        }
        try (FakeServer server = new FakeServer()) {
            server.json("/v1/images/generations", 200, "{\"data\": []}");
            assertThrows(ImageBackend.ImageException.class,
                    () -> new OpenAiImagesBackend(config(server, ""), null).generate(REQUEST, () -> false));
        }
        try (FakeServer server = new FakeServer()) {
            server.json("/v1/images/generations", 200, "<html>");
            assertThrows(ImageBackend.ImageException.class,
                    () -> new OpenAiImagesBackend(config(server, ""), null).generate(REQUEST, () -> false));
        }
    }

    @Test
    void testListsTheModels() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/v1/models", 200, "{\"data\": []}");
            assertDoesNotThrow(() -> new OpenAiImagesBackend(config(server, ""), null).test());
        }
    }
}
