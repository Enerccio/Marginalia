package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.extensions.audiobook.audio.BreathTrimTest;
import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.audiobook.service.SpeechRequest.VoiceMode;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class HiggsComfyBackendTest {

    private static final byte[] MP3 = "ID3-higgs-audio".getBytes(StandardCharsets.US_ASCII);
    private static final String ENGINE_INFO = """
            {"HiggsAudioV3EngineNode": {"input": {"required": {"model": [["local:higgs-audio-v3-tts-4b", "higgs-audio-v3-tts-4b"], {}]}}}}""";
    private static final String DONE = """
            {"p1": {"status": {"status_str": "success", "completed": true, "messages": []},
                    "outputs": {"save": {"audio": [{"filename": "audiobook_00001_.mp3", "subfolder": "marginalia", "type": "output"}]}}}}""";

    private static BackendConfig config(FakeServer server) {
        BackendConfig config = new BackendConfig();
        config.setModel(BackendConfig.MODEL_HIGGS);
        config.setBaseUrl(server.url());
        return config;
    }

    /** A ComfyUI that has the node, queues the prompt as p1, has no outputs at the first look and the audio at the second. */
    private static void comfy(FakeServer server, String finished) {
        server.json("/system_stats", 200, "{}");
        server.json("/object_info/HiggsAudioV3EngineNode", 200, ENGINE_INFO);
        server.json("/prompt", 200, "{\"prompt_id\": \"p1\", \"number\": 1}");
        AtomicInteger looks = new AtomicInteger();
        server.on("/history/", exchange -> FakeServer.reply(exchange, 200, "application/json",
                (looks.getAndIncrement() == 0 ? "{}" : finished).getBytes(StandardCharsets.UTF_8)));
        server.on("/view", exchange -> FakeServer.reply(exchange, 200, "audio/mpeg", MP3));
        server.json("/free", 200, "");
        server.json("/interrupt", 200, "");
    }

    private static JsonObject posted(FakeServer server) {
        String body = server.calls.stream().filter(c -> c.path().equals("/prompt")).findFirst().orElseThrow().body();
        return JsonParser.parseString(body).getAsJsonObject().getAsJsonObject("prompt");
    }

    private static List<FakeServer.Call> calls(FakeServer server, String path) {
        return server.calls.stream().filter(c -> c.path().equals(path)).toList();
    }

    @Test
    void aClonedVoiceGoesThroughTheInputFolderAndTheMemoryIsFreedAfterThePiece() throws Exception {
        try (FakeServer server = new FakeServer()) {
            comfy(server, DONE);
            BackendConfig config = config(server);
            SpeechRequest request = SpeechRequest.of(config, "<|emotion:amusement|>Hello [there] <|sfx:laughter|>Haha!", VoiceMode.CLONE, "m-abc.wav")
                    .withSeed(42);

            byte[] audio = new HiggsComfyBackend(config, "secret", 1, false).synthesize(request, null);

            assertArrayEquals(MP3, audio);
            JsonObject workflow = posted(server);
            JsonObject engine = workflow.getAsJsonObject("engine").getAsJsonObject("inputs");
            assertEquals("HiggsAudioV3EngineNode", workflow.getAsJsonObject("engine").get("class_type").getAsString());
            assertEquals("local:higgs-audio-v3-tts-4b", engine.get("model").getAsString(), "the local copy when the node lists it");
            assertEquals(0.8, engine.get("temperature").getAsDouble());
            JsonObject text = workflow.getAsJsonObject("text").getAsJsonObject("inputs");
            assertEquals("<|emotion:amusement|>Hello (there) <|sfx:laughter|>Haha!", text.get("text").getAsString(),
                    "a bracket of the book is no character switch of the suite");
            assertEquals(42, text.get("seed").getAsInt());
            assertEquals("none", text.get("narrator_voice").getAsString());
            assertEquals("voice", text.getAsJsonArray("opt_narrator").get(0).getAsString());
            assertEquals("m-abc.wav", workflow.getAsJsonObject("clip").getAsJsonObject("inputs").get("audio").getAsString());
            assertEquals("clip", workflow.getAsJsonObject("voice").getAsJsonObject("inputs").getAsJsonArray("opt_audio_input").get(0).getAsString());
            assertEquals("SaveAudioMP3", workflow.getAsJsonObject("save").get("class_type").getAsString());
            assertEquals("128k", workflow.getAsJsonObject("save").getAsJsonObject("inputs").get("quality").getAsString(), "constant bit rate: Mp3Joiner joins it");

            assertEquals("Bearer secret", calls(server, "/prompt").getFirst().authorization());
            List<FakeServer.Call> free = calls(server, "/free");
            assertEquals(1, free.size(), "the memory is emptied after every piece (the ROCm crash)");
            assertTrue(JsonParser.parseString(free.getFirst().body()).getAsJsonObject().get("free_memory").getAsBoolean());
            assertFalse(JsonParser.parseString(free.getFirst().body()).getAsJsonObject().has("unload_models"), "the weights stay loaded");
        }
    }

    @Test
    void aVoiceOfTheSuiteIsNamedAndNeedsNoClipNodes() throws Exception {
        try (FakeServer server = new FakeServer()) {
            comfy(server, DONE);
            BackendConfig config = config(server);

            new HiggsComfyBackend(config, null, 1, false).synthesize(SpeechRequest.of(config, "Text", VoiceMode.PREDEFINED, "voices_examples/female/female_01.wav"), null);

            JsonObject workflow = posted(server);
            assertEquals("voices_examples/female/female_01.wav", workflow.getAsJsonObject("text").getAsJsonObject("inputs").get("narrator_voice").getAsString());
            assertFalse(workflow.has("clip"));
            assertFalse(workflow.has("voice"));
            assertTrue(workflow.getAsJsonObject("text").getAsJsonObject("inputs").get("seed").getAsInt() > 0, "no seed: a random one, never 0");
            assertNull(calls(server, "/prompt").getFirst().authorization());
        }
    }

    /** A flac of the audio, made by ffmpeg; null when there is no ffmpeg. */
    private static byte[] flac(byte[] wav) {
        for (String ffmpeg : List.of("ffmpeg", "/opt/homebrew/bin/ffmpeg", "/usr/local/bin/ffmpeg", "/usr/bin/ffmpeg")) {
            try {
                Process p = new ProcessBuilder(ffmpeg, "-v", "error", "-f", "wav", "-i", "pipe:0", "-f", "flac", "pipe:1").start();
                Thread feed = Thread.ofVirtual().start(() -> {
                    try (var in = p.getOutputStream()) {
                        in.write(wav);
                    } catch (java.io.IOException ignored) {
                        // ffmpeg ended
                    }
                });
                byte[] out = p.getInputStream().readAllBytes();
                feed.join();
                if (p.waitFor() == 0 && out.length > 0) {
                    return out;
                }
            } catch (Exception ignored) {
                // not there, the next one
            }
        }
        return null;
    }

    @Test
    void withFfmpegThePieceComesAsFlacAndLosesWhatFollowsTheLastWord() throws Exception {
        byte[] flac = flac(com.github.enerccio.marginalia.extensions.audiobook.audio.WavJoiner.of(BreathTrimTest.MONO,
                new BreathTrimTest.Audio().voice(1.0, -20).silence(0.35).voice(0.15, -30).data()));
        org.junit.jupiter.api.Assumptions.assumeTrue(flac != null && com.github.enerccio.marginalia.extensions.audiobook.audio.Ffmpeg.available(),
                "ffmpeg is not installed");
        try (FakeServer server = new FakeServer()) {
            comfy(server, DONE.replace("audiobook_00001_.mp3", "audiobook_00001_.flac"));
            server.off("/view");
            server.on("/view", exchange -> FakeServer.reply(exchange, 200, "audio/flac", flac));
            BackendConfig config = config(server);

            byte[] mp3 = new HiggsComfyBackend(config, null, 1, true).synthesize(SpeechRequest.of(config, "The Kitchen", VoiceMode.CLONE, "m-abc.wav"), null);

            assertEquals("SaveAudio", posted(server).getAsJsonObject("save").get("class_type").getAsString(), "flac, trimmed here");
            assertEquals(com.github.enerccio.marginalia.extensions.audiobook.audio.AudioKind.MP3,
                    com.github.enerccio.marginalia.extensions.audiobook.audio.AudioKind.of(mp3));
            // 1 s of speech and 50 ms of the gap stay (the mp3 adds a frame or two); the 0.3 s of the gap and the 0.15 s after it are gone
            assertEquals(1.05, com.github.enerccio.marginalia.extensions.audiobook.audio.Mp3Joiner.seconds(mp3), 0.08, "the breath after the gap is gone");
        }
    }

    @Test
    void anErrorOfTheWorkflowIsToldAndTheMemoryIsStillFreed() throws Exception {
        try (FakeServer server = new FakeServer()) {
            comfy(server, """
                    {"p1": {"status": {"status_str": "error", "completed": false, "messages": [["execution_error",
                        {"exception_message": "HSA_STATUS_ERROR_EXCEPTION\\n"}]]}, "outputs": {}}}""");
            BackendConfig config = config(server);

            BackendException e = assertThrows(BackendException.class, () -> new HiggsComfyBackend(config, null, 1, false)
                    .synthesize(SpeechRequest.of(config, "Text", VoiceMode.CLONE, "m-abc.wav"), null));

            assertTrue(e.getMessage().endsWith("without audio: HSA_STATUS_ERROR_EXCEPTION"), e.getMessage());
            assertEquals(1, calls(server, "/free").size());
        }
    }

    @Test
    void aRefusedWorkflowSaysWhy() throws Exception {
        try (FakeServer server = new FakeServer()) {
            comfy(server, DONE);
            server.off("/prompt");
            server.json("/prompt", 400, "{\"error\": {\"message\": \"Cannot execute because node UnifiedTTSTextNode does not exist.\"}}");
            BackendConfig config = config(server);

            BackendException e = assertThrows(BackendException.class, () -> new HiggsComfyBackend(config, null, 1, false)
                    .synthesize(SpeechRequest.of(config, "Text", VoiceMode.CLONE, "m-abc.wav"), null));

            assertTrue(e.getMessage().contains("UnifiedTTSTextNode does not exist"), e.getMessage());
        }
    }

    @Test
    void aCancelInterruptsComfyUi() throws Exception {
        try (FakeServer server = new FakeServer()) {
            comfy(server, DONE);
            BackendConfig config = config(server);
            AtomicBoolean cancelled = new AtomicBoolean();
            server.off("/history/");
            server.on("/history/", exchange -> {
                cancelled.set(true);
                FakeServer.reply(exchange, 200, "application/json", "{}".getBytes(StandardCharsets.UTF_8));
            });

            BackendException e = assertThrows(BackendException.class, () -> new HiggsComfyBackend(config, null, 1, false)
                    .synthesize(SpeechRequest.of(config, "Text", VoiceMode.CLONE, "m-abc.wav"), cancelled::get));

            assertEquals("Cancelled", e.getMessage());
            assertEquals(1, calls(server, "/interrupt").size());
            assertEquals(1, calls(server, "/free").size());
        }
    }

    @Test
    void withoutTheNodeTheCheckSaysToInstallTheSuite() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/system_stats", 200, "{}");
            server.json("/object_info/HiggsAudioV3EngineNode", 200, "{}");

            BackendException e = assertThrows(BackendException.class, () -> ServerCheck.checkWithKey(config(server), null));

            assertTrue(e.getMessage().contains("TTS Audio Suite"), e.getMessage());
        }
    }

    @Test
    void theCheckNamesTheModelOfTheNode() throws Exception {
        try (FakeServer server = new FakeServer()) {
            comfy(server, DONE);

            ServerCheck.Result result = ServerCheck.checkWithKey(config(server), null);

            assertTrue(result.modelLoaded());
            assertEquals("Higgs Audio v3 (local:higgs-audio-v3-tts-4b)", result.model());
        }
    }

    @Test
    void aClipIsUploadedIntoTheInputFolderOnce() throws Exception {
        try (FakeServer server = new FakeServer()) {
            byte[] clip = "RIFF-clip".getBytes(StandardCharsets.US_ASCII);
            String name = ChatterboxBackend.referenceName(clip, "wav");
            AtomicBoolean uploaded = new AtomicBoolean();
            server.on("/object_info/LoadAudio", exchange -> FakeServer.reply(exchange, 200, "application/json",
                    ("{\"LoadAudio\": {\"input\": {\"required\": {\"audio\": [\"COMBO\", {\"options\": [" + (uploaded.get() ? "\"" + name + "\"" : "")
                            + "], \"audio_upload\": true}]}}}}").getBytes(StandardCharsets.UTF_8)));
            server.on("/upload/image", exchange -> {
                uploaded.set(true);
                FakeServer.reply(exchange, 200, "application/json", ("{\"name\": \"" + name + "\", \"subfolder\": \"\", \"type\": \"input\"}")
                        .getBytes(StandardCharsets.UTF_8));
            });
            HiggsComfyBackend backend = new HiggsComfyBackend(config(server), null, 1, false);

            assertEquals(name, backend.ensureReference(clip, "wav"));
            assertEquals(name, backend.ensureReference(clip, "wav"));

            List<FakeServer.Call> uploads = calls(server, "/upload/image");
            assertEquals(1, uploads.size(), "the second time the clip is listed already");
            String body = uploads.getFirst().body();
            assertTrue(body.contains("name=\"image\"; filename=\"" + name + "\""), body);
            assertTrue(body.contains("name=\"type\"\r\n\r\ninput"), body);
        }
    }

    @Test
    void theVoicesOfTheSuiteAreItsChoicesWithoutNone() throws Exception {
        try (FakeServer server = new FakeServer()) {
            server.json("/object_info/CharacterVoicesNode", 200, """
                    {"CharacterVoicesNode": {"input": {"required": {"voice_name": [["none", "voices_examples/male/male_01.wav"], {}]}}}}""");

            assertEquals(List.of("voices_examples/male/male_01.wav"), new HiggsComfyBackend(config(server), null, 1, false).predefinedVoices());
        }
    }
}
