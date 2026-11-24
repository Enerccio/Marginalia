package com.github.enerccio.marginalia.extensions.audiobook.recognize;

import com.github.enerccio.marginalia.extensions.audiobook.model.RecognizerConfig;
import com.github.enerccio.marginalia.extensions.audiobook.service.FakeServer;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class RecognizerTest {

    private static final byte[] WAV = "RIFF-fake-wav".getBytes(StandardCharsets.US_ASCII);

    private static RecognizerConfig config(FakeServer server, RecognizerConfig.Type type) {
        RecognizerConfig config = new RecognizerConfig();
        config.setType(type);
        config.setBaseUrl(server.url());
        config.setLocal(true);
        config.setTimeoutSeconds(10);
        return config;
    }

    @Test
    void comfyUiRecognisesThroughGraniteAndFreesTheModelsAfterwards() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/upload/image", 200, "{\"name\": \"asr-x.wav\", \"subfolder\": \"\", \"type\": \"input\"}");
            server.json("/prompt", 200, "{\"prompt_id\": \"p1\", \"number\": 1}");
            server.json("/history/p1", 200, "{\"p1\": {\"outputs\": {\"text\": {\"text\": [\"the banging continued\"]}}, "
                    + "\"status\": {\"completed\": true, \"status_str\": \"success\"}}}");
            server.json("/free", 200, "");

            String text = new ComfyGraniteRecognizer(config(server, RecognizerConfig.Type.COMFYUI_GRANITE), "key", 10).transcribe(WAV, "wav", "en", null);

            assertEquals("the banging continued", text);
            JsonObject workflow = JsonParser.parseString(server.calls.stream().filter(c -> c.path().equals("/prompt")).findFirst().orElseThrow().body())
                    .getAsJsonObject().getAsJsonObject("prompt");
            assertEquals("GraniteASREngineNode", workflow.getAsJsonObject("engine").get("class_type").getAsString());
            assertEquals("granite-speech-4.1-2b", workflow.getAsJsonObject("engine").getAsJsonObject("inputs").get("model_name").getAsString());
            assertEquals("asr-x.wav", workflow.getAsJsonObject("clip").getAsJsonObject("inputs").get("audio").getAsString());
            assertEquals("English", workflow.getAsJsonObject("asr").getAsJsonObject("inputs").get("language").getAsString());
            assertEquals("PreviewAny", workflow.getAsJsonObject("text").get("class_type").getAsString());
            FakeServer.Call free = server.calls.stream().filter(c -> c.path().equals("/free")).findFirst().orElseThrow();
            assertTrue(free.body().contains("unload_models"), "the models are unloaded after the recognition");
            assertEquals("Bearer key", free.authorization());
        }
    }

    @Test
    void comfyUiWithoutTheNodesFailsTheTest() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/object_info/GraniteASREngineNode", 200, "{}");
            assertThrows(BackendException.class,
                    () -> new ComfyGraniteRecognizer(config(server, RecognizerConfig.Type.COMFYUI_GRANITE), null).test());
        }
    }

    @Test
    void anUnknownLanguageIsLeftToTheRecogniser() {
        assertEquals("Auto", ComfyGraniteRecognizer.languageName(null));
        assertEquals("Japanese", ComfyGraniteRecognizer.languageName("ja-JP"));
        assertEquals("Auto", ComfyGraniteRecognizer.languageName("xx"));
    }

    @Test
    void openAiPostsTheAudioToTheTranscriptions() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/v1/audio/transcriptions", 200, "{\"text\": \" Albus, open this door immediately! \"}");
            RecognizerConfig config = config(server, RecognizerConfig.Type.OPENAI);

            String text = new OpenAiRecognizer(config, "secret").transcribe(WAV, "wav", "en", null);

            assertEquals("Albus, open this door immediately!", text);
            FakeServer.Call call = server.calls.getFirst();
            assertEquals("Bearer secret", call.authorization());
            assertTrue(call.contentType().startsWith("multipart/form-data"));
            assertTrue(call.body().contains("name=\"model\"\r\n\r\nwhisper-1"), call.body());
            assertTrue(call.body().contains("name=\"language\"\r\n\r\nen"));
        }
    }

    @Test
    void openAiTakesABaseWithV1Too() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/v1/models", 200, "{\"data\": []}");
            RecognizerConfig config = config(server, RecognizerConfig.Type.OPENAI);
            config.setBaseUrl(server.url() + "/v1/");
            new OpenAiRecognizer(config, null).test();
            assertEquals("/v1/models", server.calls.getFirst().path());
        }
    }
}
