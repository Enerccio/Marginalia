package com.github.enerccio.marginalia.extensions.audiobook.clean;

import com.github.enerccio.marginalia.domain.service.job.JobCallType;
import com.github.enerccio.marginalia.extensions.audiobook.audio.AudioKind;
import com.github.enerccio.marginalia.extensions.audiobook.audio.ClipQuality;
import com.github.enerccio.marginalia.extensions.audiobook.audio.Ffmpeg;
import com.github.enerccio.marginalia.extensions.audiobook.comfy.ComfyClient;
import com.github.enerccio.marginalia.extensions.audiobook.model.CleanupConfig;
import com.github.enerccio.marginalia.extensions.audiobook.model.CleanupPreset;
import com.github.enerccio.marginalia.extensions.audiobook.model.CleanupStep;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.extensions.httphooks.JobCalls;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Cleans a reference clip with a {@link CleanupPreset}: the steps that run on ComfyUI (the {@code VocalRemovalNode} and {@code VoiceFixerNode} of the
 * TTS Audio Suite) one after the other go into one workflow ({@code LoadAudio}, the steps chained, {@code SaveAudio}), a step of ffmpeg runs here between
 * them. ComfyUI's models are unloaded ({@code /free}) after every workflow. The result is a 16-bit wav when ffmpeg is here (ComfyUI saves flac), an mp3
 * of ComfyUI otherwise.
 */
public class ComfyCleaner {

    static final String SEPARATE_NODE = "VocalRemovalNode";
    static final String FIXER_NODE = "VoiceFixerNode";
    static final String[] FIXER_MODES = {"0 - Original (Default)", "1 - With High-Freq Removal", "2 - Train Mode (Seriously Degraded)"};
    private static final long POLL_MILLIS = 1000;

    /** What is done here, without ComfyUI (ffmpeg; replaced in the tests). */
    public interface Local {
        /** Whether ffmpeg is here: the result is then lossless. */
        boolean available();

        /** The noise filter of the ffmpeg step. */
        byte[] clean(byte[] audio) throws BackendException;

        /** Levels the loudness (normalize step). */
        byte[] normalize(byte[] audio, double lufs) throws BackendException;

        /** A wav of a flac. */
        byte[] toWav(byte[] flac) throws BackendException;

        Local FFMPEG = new Local() {
            @Override
            public boolean available() {
                return Ffmpeg.available();
            }

            @Override
            public byte[] clean(byte[] audio) throws BackendException {
                ClipQuality.Result quality = ClipQuality.measure(audio);
                if (quality == null) {
                    throw new BackendException("The clip can't be read without ffmpeg");
                }
                return Ffmpeg.cleanVoice(audio, quality.floorDb());
            }

            @Override
            public byte[] normalize(byte[] audio, double lufs) throws BackendException {
                return Ffmpeg.normalizeLoudness(audio, lufs);
            }

            @Override
            public byte[] toWav(byte[] flac) throws BackendException {
                return Ffmpeg.flacToWav(flac);
            }
        };
    }

    /** The cleaned audio and its kind. */
    public record Result(byte[] audio, String extension) {
    }

    private final ComfyClient client;
    private final Local local;
    private final String baseUrl;

    public ComfyCleaner(CleanupConfig config, String apiKey) {
        this(new ComfyClient(config.getBaseUrl(), config.isLocal(), apiKey, config.getTimeoutSeconds(), POLL_MILLIS), Local.FFMPEG, config.getBaseUrl());
    }

    ComfyCleaner(ComfyClient client, Local local, String baseUrl) {
        this.client = client;
        this.local = local;
        this.baseUrl = baseUrl;
    }

    /** Throws when ComfyUI lacks the nodes. */
    public void test() throws BackendException {
        client.requireNodes("LoadAudio", SEPARATE_NODE, FIXER_NODE);
    }

    /** The separation models ComfyUI offers, sorted. */
    public List<String> separationModels() throws BackendException {
        List<String> models = new ArrayList<>(ComfyClient.options(client.objectInfo(SEPARATE_NODE), "model"));
        models.sort(String.CASE_INSENSITIVE_ORDER);
        return models;
    }

    /**
     * Runs the preset on the clip.
     *
     * @param extension {@code wav} or {@code mp3}
     */
    public Result clean(byte[] clip, String extension, CleanupPreset preset, ComfyClient.Cancel cancel) throws BackendException {
        if (preset.getSteps().isEmpty()) {
            throw new BackendException("The cleanup has no steps");
        }
        if (!preset.usesComfy()) {
            return run(clip, extension, preset, cancel);
        }
        return JobCalls.track(JobCallType.AUDIO, "audiobook", baseUrl, preset.describe(), () -> run(clip, extension, preset, cancel));
    }

    private Result run(byte[] clip, String extension, CleanupPreset preset, ComfyClient.Cancel cancel) throws BackendException {
        byte[] audio = clip;
        String ext = extension;
        List<CleanupStep> pending = new ArrayList<>();
        for (CleanupStep step : preset.getSteps()) {
            if (!step.getType().isLocal()) {
                pending.add(step);
                continue;
            }
            if (!pending.isEmpty()) {
                Result done = onComfy(audio, pending, cancel);
                audio = done.audio();
                ext = done.extension();
                pending.clear();
            }
            if (!local.available()) {
                throw new BackendException("The " + step.describe() + " step needs ffmpeg on the machine of the application");
            }
            if (step.getType() == CleanupStep.Type.NORMALIZE) {
                // keeps the kind of the audio (an mp3 stays an mp3)
                audio = local.normalize(audio, step.getLoudness());
                ext = AudioKind.of(audio) == AudioKind.WAV ? "wav" : "mp3";
            } else {
                audio = local.clean(audio);
                ext = "wav";
            }
        }
        if (!pending.isEmpty()) {
            return onComfy(audio, pending, cancel);
        }
        return new Result(audio, ext);
    }

    private Result onComfy(byte[] audio, List<CleanupStep> steps, ComfyClient.Cancel cancel) throws BackendException {
        boolean lossless = local.available();
        String kind = AudioKind.of(audio) == AudioKind.WAV ? "wav" : "mp3";
        String name = client.upload(audio, "clean-", kind, kind.equals("mp3") ? "audio/mpeg" : "audio/wav");
        try {
            JsonObject result = client.run(workflow(name, steps, lossless), "the cleanup", entry -> {
                List<JsonObject> audios = ComfyClient.audiosOf(entry, "out");
                return audios.isEmpty() ? null : audios.getFirst();
            }, cancel);
            byte[] bytes = client.fetch(result);
            return lossless ? new Result(local.toWav(bytes), "wav") : new Result(bytes, "mp3");
        } finally {
            client.free();
        }
    }

    /** The workflow of steps that run on ComfyUI, in its API format: {@code in} loads the clip, {@code s0}, {@code s1}... the steps, {@code out} saves. */
    static JsonObject workflow(String inputFile, List<CleanupStep> steps, boolean lossless) {
        JsonObject workflow = new JsonObject();
        JsonObject load = new JsonObject();
        load.addProperty("audio", inputFile);
        workflow.add("in", ComfyClient.node("LoadAudio", load));
        String previous = "in";
        int output = 0;
        for (int i = 0; i < steps.size(); i++) {
            CleanupStep step = steps.get(i);
            String id = "s" + i;
            JsonObject inputs = new JsonObject();
            inputs.add("audio", ComfyClient.link(previous, output));
            switch (step.getType()) {
                case SEPARATE -> {
                    inputs.addProperty("model", step.getModel());
                    inputs.addProperty("aggressiveness", step.getAggressiveness());
                    inputs.addProperty("format", "flac");
                    // the node keeps its results by the input and answers a second cleanup from them, also when the user changed the model files
                    inputs.addProperty("use_cache", false);
                    workflow.add(id, ComfyClient.node(SEPARATE_NODE, inputs));
                    output = step.getKeep() == CleanupStep.Keep.REMAINING ? 1 : 0;
                }
                case VOICE_FIXER -> {
                    inputs.addProperty("restoration_mode", FIXER_MODES[step.getMode()]);
                    inputs.addProperty("use_cuda", true);
                    workflow.add(id, ComfyClient.node(FIXER_NODE, inputs));
                    output = 0;
                }
                case FFMPEG, NORMALIZE -> throw new IllegalArgumentException("ffmpeg does not run on ComfyUI");
            }
            previous = id;
        }
        JsonObject save = new JsonObject();
        save.add("audio", ComfyClient.link(previous, output));
        save.addProperty("filename_prefix", "marginalia/audiobook-clean");
        if (!lossless) {
            save.addProperty("quality", "320k");
        }
        workflow.add("out", ComfyClient.node(lossless ? "SaveAudio" : "SaveAudioMP3", save));
        return workflow;
    }
}
