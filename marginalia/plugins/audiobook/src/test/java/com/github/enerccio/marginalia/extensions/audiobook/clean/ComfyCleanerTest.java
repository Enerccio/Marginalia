package com.github.enerccio.marginalia.extensions.audiobook.clean;

import com.github.enerccio.marginalia.extensions.audiobook.comfy.ComfyClient;
import com.github.enerccio.marginalia.extensions.audiobook.model.CleanupConfig;
import com.github.enerccio.marginalia.extensions.audiobook.model.CleanupPreset;
import com.github.enerccio.marginalia.extensions.audiobook.model.CleanupStep;
import com.github.enerccio.marginalia.extensions.audiobook.service.FakeServer;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ComfyCleanerTest {

    private static final byte[] WAV = "RIFF....WAVEfake-clip".getBytes(StandardCharsets.US_ASCII);

    /** Without ffmpeg: no conversion, ComfyUI saves an mp3. */
    private static final ComfyCleaner.Local NO_FFMPEG = new ComfyCleaner.Local() {
        @Override
        public boolean available() {
            return false;
        }

        @Override
        public byte[] clean(byte[] audio) {
            throw new AssertionError();
        }

        @Override
        public byte[] normalize(byte[] audio, double lufs) {
            throw new AssertionError();
        }

        @Override
        public byte[] toWav(byte[] flac) {
            throw new AssertionError();
        }
    };

    /** A pretend ffmpeg: marks what it did. */
    private static ComfyCleaner.Local fakeFfmpeg(List<String> did) {
        return new ComfyCleaner.Local() {
            @Override
            public boolean available() {
                return true;
            }

            @Override
            public byte[] clean(byte[] audio) {
                did.add("clean");
                return "RIFF....WAVEcleaned".getBytes(StandardCharsets.US_ASCII);
            }

            @Override
            public byte[] normalize(byte[] audio, double lufs) {
                did.add("normalize:" + lufs);
                return audio;
            }

            @Override
            public byte[] toWav(byte[] flac) {
                did.add("wav:" + new String(flac, StandardCharsets.US_ASCII));
                return "RIFF....WAVEfromflac".getBytes(StandardCharsets.US_ASCII);
            }
        };
    }

    private static void serve(FakeServer server) {
        server.json("/upload/image", 200, "{\"name\": \"clean-x.wav\", \"subfolder\": \"\", \"type\": \"input\"}");
        server.json("/prompt", 200, "{\"prompt_id\": \"p1\", \"number\": 1}");
        server.json("/history/p1", 200, "{\"p1\": {\"outputs\": {\"out\": {\"audio\": [{\"filename\": \"c.flac\", \"subfolder\": \"marginalia\", "
                + "\"type\": \"output\"}]}}, \"status\": {\"completed\": true, \"status_str\": \"success\"}}}");
        server.on("/view", exchange -> FakeServer.reply(exchange, 200, "audio/flac", "FLACDATA".getBytes(StandardCharsets.US_ASCII)));
        server.json("/free", 200, "");
    }

    private static ComfyCleaner cleaner(FakeServer server, ComfyCleaner.Local local) {
        return new ComfyCleaner(new ComfyClient(server.url(), true, "key", 10, 10), local, server.url());
    }

    private static JsonObject prompt(FakeServer server, int index) {
        return JsonParser.parseString(server.calls.stream().filter(c -> c.path().equals("/prompt")).toList().get(index).body())
                .getAsJsonObject().getAsJsonObject("prompt");
    }

    @Test
    void theStepsOfComfyUiAreChainedInOneWorkflowAndTheKeptOutputIsLinked() {
        List<CleanupStep> steps = List.of(CleanupStep.separate("MELBAND/a.ckpt", CleanupStep.Keep.EXTRACTED),
                CleanupStep.separate("UVR/b.pth", CleanupStep.Keep.REMAINING), CleanupStep.voiceFixer(2));

        JsonObject workflow = ComfyCleaner.workflow("in.wav", steps, true);

        assertEquals("in.wav", workflow.getAsJsonObject("in").getAsJsonObject("inputs").get("audio").getAsString());
        JsonObject s0 = workflow.getAsJsonObject("s0").getAsJsonObject("inputs");
        assertEquals("MELBAND/a.ckpt", s0.get("model").getAsString());
        assertEquals("[\"in\",0]", s0.get("audio").toString());
        assertFalse(s0.get("use_cache").getAsBoolean());
        assertEquals("[\"s0\",0]", workflow.getAsJsonObject("s1").getAsJsonObject("inputs").get("audio").toString());
        // the second separation keeps its second output, which the voice fixer takes
        JsonObject s2 = workflow.getAsJsonObject("s2");
        assertEquals("VoiceFixerNode", s2.get("class_type").getAsString());
        assertEquals("[\"s1\",1]", s2.getAsJsonObject("inputs").get("audio").toString());
        assertTrue(s2.getAsJsonObject("inputs").get("restoration_mode").getAsString().startsWith("2 "));
        assertEquals("SaveAudio", workflow.getAsJsonObject("out").get("class_type").getAsString());
        assertEquals("[\"s2\",0]", workflow.getAsJsonObject("out").getAsJsonObject("inputs").get("audio").toString());
        assertEquals("SaveAudioMP3", ComfyCleaner.workflow("in.wav", steps, false).getAsJsonObject("out").get("class_type").getAsString());
    }

    @Test
    void aCleanupRunsOnComfyUiFetchesTheResultAndFreesTheModels() throws Exception {
        try (FakeServer server = new FakeServer()) {
            serve(server);
            List<String> did = new ArrayList<>();
            CleanupPreset preset = new CleanupPreset("Film", List.of(CleanupStep.separate(CleanupConfig.MODEL_VOCALS, CleanupStep.Keep.EXTRACTED)));

            ComfyCleaner.Result result = cleaner(server, fakeFfmpeg(did)).clean(WAV, "wav", preset, null);

            assertEquals("wav", result.extension());
            assertEquals("RIFF....WAVEfromflac", new String(result.audio(), StandardCharsets.US_ASCII));
            assertEquals(List.of("wav:FLACDATA"), did);
            assertTrue(server.calls.stream().anyMatch(c -> c.path().equals("/free") && c.body().contains("unload_models")));
            FakeServer.Call view = server.calls.stream().filter(c -> c.path().startsWith("/view")).findFirst().orElseThrow();
            assertEquals("Bearer key", view.authorization());
        }
    }

    @Test
    void aPresetOfLocalStepsOnlyNeverCallsComfyUi() throws Exception {
        try (FakeServer server = new FakeServer()) {
            List<String> did = new ArrayList<>();
            CleanupPreset preset = new CleanupPreset("Louder", List.of(CleanupStep.normalize(-20)));

            ComfyCleaner.Result result = cleaner(server, fakeFfmpeg(did)).clean(WAV, "wav", preset, null);

            assertFalse(preset.usesComfy());
            assertEquals("wav", result.extension());
            assertArrayEquals(WAV, result.audio());
            assertEquals(List.of("normalize:-20.0"), did);
            assertTrue(server.calls.isEmpty());
            assertEquals("loudness -20 LUFS", preset.describe());
        }
    }

    @Test
    void withoutFfmpegTheResultIsTheMp3OfComfyUi() throws Exception {
        try (FakeServer server = new FakeServer()) {
            serve(server);
            CleanupPreset preset = new CleanupPreset("Fix", List.of(CleanupStep.voiceFixer(0)));

            ComfyCleaner.Result result = cleaner(server, NO_FFMPEG).clean(WAV, "wav", preset, null);

            assertEquals("mp3", result.extension());
            assertEquals("SaveAudioMP3", prompt(server, 0).getAsJsonObject("out").get("class_type").getAsString());
        }
    }

    @Test
    void anFfmpegStepRunsHereBetweenTwoWorkflows() throws Exception {
        try (FakeServer server = new FakeServer()) {
            serve(server);
            List<String> did = new ArrayList<>();
            CleanupPreset preset = new CleanupPreset("Mixed", List.of(CleanupStep.separate("A", CleanupStep.Keep.EXTRACTED), CleanupStep.ffmpeg(),
                    CleanupStep.voiceFixer(0)));

            cleaner(server, fakeFfmpeg(did)).clean(WAV, "wav", preset, null);

            assertEquals(List.of("wav:FLACDATA", "clean", "wav:FLACDATA"), did);
            assertEquals(2, server.calls.stream().filter(c -> c.path().equals("/prompt")).count());
            assertEquals("VocalRemovalNode", prompt(server, 0).getAsJsonObject("s0").get("class_type").getAsString());
            assertEquals("VoiceFixerNode", prompt(server, 1).getAsJsonObject("s0").get("class_type").getAsString());
        }
    }

    @Test
    void anFfmpegStepWithoutFfmpegFails() {
        CleanupPreset preset = new CleanupPreset("Local", List.of(CleanupStep.ffmpeg()));
        try (FakeServer server = new FakeServer()) {
            assertThrows(BackendException.class, () -> cleaner(server, NO_FFMPEG).clean(WAV, "wav", preset, null));
        } catch (java.io.IOException e) {
            fail(e);
        }
    }

    @Test
    void anErrorOfComfyUiIsReported() throws Exception {
        try (FakeServer server = new FakeServer()) {
            serve(server);
            server.off("/history/p1");
            server.json("/history/p1", 200, "{\"p1\": {\"outputs\": {}, \"status\": {\"completed\": false, \"status_str\": \"error\", \"messages\": "
                    + "[[\"execution_error\", {\"exception_message\": \"model not found\"}]]}}}");
            CleanupPreset preset = new CleanupPreset("Bad", List.of(CleanupStep.separate("X", CleanupStep.Keep.EXTRACTED)));

            BackendException e = assertThrows(BackendException.class, () -> cleaner(server, NO_FFMPEG).clean(WAV, "wav", preset, null));
            assertTrue(e.getMessage().contains("model not found"), e.getMessage());
            assertTrue(server.calls.stream().anyMatch(c -> c.path().equals("/free")), "freed also after an error");
        }
    }

    @Test
    void theDefaultPresetsAreSortedAndDescribeTheirSteps() {
        CleanupConfig config = new CleanupConfig();
        List<String> names = config.getPresets().stream().map(CleanupPreset::getName).toList();
        assertEquals(names.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList(), names);
        assertEquals("MelBandRoformer_fp32", CleanupStep.separate(CleanupConfig.MODEL_VOCALS, CleanupStep.Keep.EXTRACTED).describe());
        assertEquals("UVR-DeNoise (remaining)", CleanupStep.separate("UVR/UVR-DeNoise.pth", CleanupStep.Keep.REMAINING).describe());
    }
}
