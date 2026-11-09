package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.audiobook.service.SpeechRequest.VoiceMode;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ChatterboxBackendTest {

    private static final byte[] MP3 = "ID3-audio-bytes".getBytes(StandardCharsets.US_ASCII);

    private static BackendConfig config(FakeServer server) {
        BackendConfig config = new BackendConfig();
        config.setBaseUrl(server.url());
        return config;
    }

    private static JsonObject sent(FakeServer server) {
        return JsonParser.parseString(server.calls.getFirst().body()).getAsJsonObject();
    }

    private static void audio(FakeServer server) {
        server.on("/tts", exchange -> FakeServer.reply(exchange, 200, "audio/mpeg", MP3));
    }

    @Test
    void aClonedVoiceIsSentWithAllTheParameters() throws Exception {
        try (FakeServer server = new FakeServer()) {
            audio(server);
            BackendConfig config = config(server);
            config.setProtectedApiKey("ignored");
            SpeechRequest request = SpeechRequest.of(config, "Hello there.", VoiceMode.CLONE, "m-abc.wav").withExaggeration(0.7).withSeed(42);

            byte[] audio = new ChatterboxBackend(config, "secret").synthesize(request, null);

            assertArrayEquals(MP3, audio);
            FakeServer.Call call = server.calls.getFirst();
            assertEquals("POST", call.method());
            assertEquals("Bearer secret", call.authorization());
            JsonObject body = sent(server);
            assertEquals("Hello there.", body.get("text").getAsString());
            assertEquals("clone", body.get("voice_mode").getAsString());
            assertEquals("m-abc.wav", body.get("reference_audio_filename").getAsString());
            assertFalse(body.has("predefined_voice_id"));
            assertEquals("mp3", body.get("output_format").getAsString());
            assertTrue(body.get("split_text").getAsBoolean());
            assertEquals(240, body.get("chunk_size").getAsInt());
            assertEquals(0.7, body.get("exaggeration").getAsDouble());
            assertEquals(0.5, body.get("cfg_weight").getAsDouble());
            assertEquals(0.8, body.get("temperature").getAsDouble());
            assertEquals(1.0, body.get("speed_factor").getAsDouble());
            assertEquals(42, body.get("seed").getAsInt());
        }
    }

    @Test
    void aPredefinedVoiceHasItsOwnFieldAndNoSeedWhenNoneIsSet() throws Exception {
        try (FakeServer server = new FakeServer()) {
            audio(server);
            BackendConfig config = config(server);

            new ChatterboxBackend(config, null).synthesize(SpeechRequest.of(config, "Text", VoiceMode.PREDEFINED, "Henry.wav"), null);

            JsonObject body = sent(server);
            assertEquals("predefined", body.get("voice_mode").getAsString());
            assertEquals("Henry.wav", body.get("predefined_voice_id").getAsString());
            assertFalse(body.has("reference_audio_filename"));
            assertFalse(body.has("seed"));
            assertNull(server.calls.getFirst().authorization());
        }
    }

    @Test
    void theLanguageGoesToTheMultilingualModelOnly() throws Exception {
        try (FakeServer server = new FakeServer()) {
            audio(server);
            BackendConfig config = config(server);
            SpeechRequest request = SpeechRequest.of(config, "いきをしてる。", VoiceMode.CLONE, "m-x.wav").withLanguage("ja");

            new ChatterboxBackend(config, null).synthesize(request, null);
            assertFalse(sent(server).has("language"), "Turbo has no use for it");

            server.calls.clear();
            config.setModel(BackendConfig.MODEL_MULTILINGUAL);
            new ChatterboxBackend(config, null).synthesize(request, null);
            assertEquals("ja", sent(server).get("language").getAsString());
            assertEquals("いきをしてる。", sent(server).get("text").getAsString());
        }
    }

    @Test
    void anAnswerThatIsNotAudioIsAnError() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/tts", 200, "{\"detail\": \"nothing\"}");
            BackendConfig config = config(server);

            var e = assertThrows(BackendException.class,
                    () -> new ChatterboxBackend(config, null).synthesize(SpeechRequest.of(config, "Text", VoiceMode.CLONE, "m-x.wav"), null));

            assertTrue(e.getMessage().contains("did not return audio"), e.getMessage());
        }
    }

    @Test
    void aServerWithoutAModelIsToldAsThat() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/tts", 503, "{\"detail\": \"TTS engine model is not currently loaded\"}");
            BackendConfig config = config(server);

            var e = assertThrows(BackendException.class,
                    () -> new ChatterboxBackend(config, null).synthesize(SpeechRequest.of(config, "Text", VoiceMode.CLONE, "m-x.wav"), null));

            assertTrue(e.getMessage().contains("no model loaded"), e.getMessage());
        }
    }

    @Test
    void nothingIsSentForEmptyTextNoVoiceOrACancel() throws Exception {
        try (FakeServer server = new FakeServer()) {
            audio(server);
            BackendConfig config = config(server);
            ChatterboxBackend backend = new ChatterboxBackend(config, null);

            assertThrows(BackendException.class, () -> backend.synthesize(SpeechRequest.of(config, "  ", VoiceMode.CLONE, "m-x.wav"), null));
            assertThrows(BackendException.class, () -> backend.synthesize(SpeechRequest.of(config, "Text", VoiceMode.CLONE, " "), null));
            assertThrows(BackendException.class, () -> backend.synthesize(SpeechRequest.of(config, "Text", VoiceMode.CLONE, "m-x.wav"), () -> true));
            assertTrue(server.calls.isEmpty());
        }
    }

    @Test
    void prepareNeedsAModel() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/api/model-info", 200, "{\"loaded\": false}");
            BackendConfig config = config(server);

            var e = assertThrows(BackendException.class, () -> new ChatterboxBackend(config, null).prepare());
            assertTrue(e.getMessage().contains("no model loaded"), e.getMessage());
        }
        try (FakeServer server = new FakeServer()) {
            server.json("/api/model-info", 200, "{\"loaded\": true, \"class_name\": \"ChatterboxTurboTTS\"}");
            assertDoesNotThrow(() -> new ChatterboxBackend(config(server), null).prepare());
        }
    }

    @Test
    void aClipThatIsOnTheServerIsNotUploadedAgain() throws Exception {
        try (FakeServer server = new FakeServer()) {
            byte[] clip = "RIFF-clip".getBytes(StandardCharsets.US_ASCII);
            String name = "m-" + java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(clip), 0, 12) + ".wav";
            server.json("/get_reference_files", 200, "[\"Robert.wav\", \"" + name + "\"]");

            assertEquals(name, new ChatterboxBackend(config(server), null).ensureReference(clip, "wav"));

            assertEquals(1, server.calls.size(), "only the list was asked for");
        }
    }

    @Test
    void aMissingClipIsUploadedUnderTheNameOfItsBytes() throws Exception {
        try (FakeServer server = new FakeServer()) {
            byte[] clip = "RIFF-clip".getBytes(StandardCharsets.US_ASCII);
            server.json("/get_reference_files", 200, "[\"Robert.wav\"]");
            server.on("/upload_reference", exchange -> {
                String name = server.calls.getLast().body().replaceAll("(?s).*filename=\"([^\"]+)\".*", "$1");
                FakeServer.reply(exchange, 200, "application/json",
                        ("{\"message\": \"Processed 1 file(s).\", \"uploaded_files\": [\"" + name + "\"], \"all_reference_files\": [], \"errors\": []}").getBytes());
            });

            String name = new ChatterboxBackend(config(server), null).ensureReference(clip, ".WAV");

            assertTrue(name.matches("m-[0-9a-f]{24}\\.wav"), name);
            FakeServer.Call upload = server.calls.getLast();
            assertEquals("/upload_reference", upload.path());
            assertTrue(upload.contentType().startsWith("multipart/form-data; boundary="), upload.contentType());
            assertTrue(upload.body().contains("name=\"files\"; filename=\"" + name + "\""), upload.body());
            assertTrue(upload.body().contains("RIFF-clip"));
            // the same bytes, the same name
            assertEquals(name, new ChatterboxBackend(config(server), null).ensureReference(clip, "wav"));
        }
    }

    @Test
    void aClipTheServerRefusesIsAnError() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/get_reference_files", 200, "[]");
            server.json("/upload_reference", 200, "{\"uploaded_files\": [], \"errors\": [{\"filename\": \"x\", \"error\": \"too long\"}]}");

            var e = assertThrows(BackendException.class, () -> new ChatterboxBackend(config(server), null).ensureReference(new byte[]{1, 2, 3}, "mp3"));

            assertTrue(e.getMessage().contains("refused"), e.getMessage());
        }
    }

    @Test
    void onlyWavAndMp3AreClips() throws Exception {
        try (FakeServer server = new FakeServer()) {
            ChatterboxBackend backend = new ChatterboxBackend(config(server), null);
            assertThrows(BackendException.class, () -> backend.ensureReference(new byte[]{1}, "flac"));
            assertThrows(BackendException.class, () -> backend.ensureReference(new byte[0], "wav"));
            assertTrue(server.calls.isEmpty());
        }
    }

    @Test
    void theVoicesAreListedWhateverTheShapeOfTheEntry() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/get_predefined_voices", 200,
                    "[{\"display_name\": \"Henry\", \"filename\": \"Henry.wav\"}, {\"name\": \"Gianna.wav\"}, \"Robert.wav\", {\"display_name\": \"?\"}]");

            assertEquals(java.util.List.of("Henry.wav", "Gianna.wav", "Robert.wav"), new ChatterboxBackend(config(server), null).predefinedVoices());
        }
    }
}
