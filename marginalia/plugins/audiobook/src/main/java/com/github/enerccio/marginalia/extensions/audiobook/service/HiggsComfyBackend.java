package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.extensions.audiobook.AudiobookLog;
import com.github.enerccio.marginalia.extensions.audiobook.audio.BreathTrim;
import com.github.enerccio.marginalia.extensions.audiobook.audio.Ffmpeg;
import com.github.enerccio.marginalia.extensions.audiobook.audio.WavJoiner;
import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.extensions.httphooks.Http;
import com.github.enerccio.marginalia.extensions.httphooks.UrlGuard;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Higgs Audio v3 in ComfyUI, through the nodes of the TTS Audio Suite (diodiogod/TTS-Audio-Suite): a built-in workflow is posted to {@code /prompt},
 * {@code /history/<id>} is polled and the audio is fetched from {@code /view}, as the Image Generator does with images. Reference clips go to the input
 * folder of ComfyUI ({@code /upload/image} takes any file) under the same names made from their bytes as on a Chatterbox server; the voices of the suite
 * and the clips are read from {@code /object_info}.
 * <p>
 * <b>After every piece {@code POST /free {"free_memory": true}}</b>: on the author's AMD R9700 (ROCm 7.2) a second generation on the engine the suite keeps
 * loaded crashed the GPU (HSA_STATUS_ERROR_EXCEPTION, ComfyUI dies) within three pieces; with the cache emptied between pieces 17 of 17 were fine and the
 * weights stay loaded (about 3 s a piece). See TODO.md.
 * <p>
 * With ffmpeg the piece comes as flac and is trimmed here ({@link BreathTrim}: Higgs breathes on after the last word, cut off by the end of the clip),
 * then stored as wav or mp3; without it the mp3 of ComfyUI is stored as it is.
 */
public class HiggsComfyBackend implements SpeechBackend {

    /** The node of the engine; its presence says that the suite is installed. */
    static final String ENGINE_NODE = "HiggsAudioV3EngineNode";
    /** The model the suite downloads, and the name it lists once the files are in the models folder. */
    static final String MODEL = "higgs-audio-v3-tts-4b";
    static final String LOCAL_MODEL = "local:" + MODEL;

    private static final long POLL_MILLIS = 1000;
    private static final int SHORT_SECONDS = 30;
    private static final int MAX_AUDIO_BYTES = 64 * 1024 * 1024;
    private static final int MAX_CLIP_BYTES = 20 * 1024 * 1024;
    private static final String OUTPUT_PREFIX = "marginalia/audiobook";

    private final BackendConfig config;
    private final String apiKey;
    private final long pollMillis;
    /** Whether ffmpeg is there to trim the pieces (and to make wav). */
    private final boolean ffmpeg;
    /** The name of the model as the engine node lists it, read once. */
    private String model;

    public HiggsComfyBackend(BackendConfig config, String apiKey) {
        this(config, apiKey, POLL_MILLIS);
    }

    HiggsComfyBackend(BackendConfig config, String apiKey, long pollMillis) {
        this(config, apiKey, pollMillis, Ffmpeg.available());
    }

    HiggsComfyBackend(BackendConfig config, String apiKey, long pollMillis, boolean ffmpeg) {
        this.config = config;
        this.apiKey = apiKey;
        this.pollMillis = pollMillis;
        this.ffmpeg = ffmpeg;
    }

    /** What the settings' test says about the server: it is there and has the Higgs node. */
    public static ServerCheck.Result check(BackendConfig config, String apiKey) throws BackendException {
        HiggsComfyBackend backend = new HiggsComfyBackend(config, apiKey);
        backend.prepare();
        return new ServerCheck.Result(true, "Higgs Audio v3 (" + backend.model + ")");
    }

    @Override
    public void prepare() throws BackendException {
        if (config.getBaseUrl().isBlank()) {
            throw new BackendException("The address of ComfyUI is empty");
        }
        Http.requireOk(Http.send(builder("/system_stats", SHORT_SECONDS).GET().build()), "Connecting to ComfyUI");
        model = model();
    }

    /** The model the engine node offers: the local copy when the files are there, else the one it downloads. */
    private String model() throws BackendException {
        JsonObject info = objectInfo(ENGINE_NODE);
        if (info == null) {
            throw new BackendException("ComfyUI has no " + ENGINE_NODE + ": install the TTS Audio Suite (diodiogod/TTS-Audio-Suite) into its custom_nodes "
                    + "and restart it");
        }
        List<String> options = options(info, "model");
        if (options.contains(LOCAL_MODEL)) {
            return LOCAL_MODEL;
        }
        return options.contains(MODEL) || options.isEmpty() ? MODEL : options.getFirst();
    }

    @Override
    public byte[] synthesize(SpeechRequest request, Cancel cancel) throws BackendException {
        return com.github.enerccio.marginalia.extensions.httphooks.JobCalls.track(
                com.github.enerccio.marginalia.domain.service.job.JobCallType.AUDIO, "audiobook", config.getBaseUrl(),
                config.getModel(), () -> speak(request, cancel));
    }

    private byte[] speak(SpeechRequest request, Cancel cancel) throws BackendException {
        if (request.text() == null || request.text().isBlank()) {
            throw new BackendException("There is nothing to say");
        }
        if (request.voice() == null || request.voice().isBlank()) {
            throw new BackendException("The piece has no voice");
        }
        if (cancel != null && cancel.isCancelled()) {
            throw new BackendException("Cancelled");
        }
        if (model == null) {
            model = model();
        }
        boolean wav = "wav".equals(request.format());
        if (wav && !ffmpeg) {
            throw new BackendException("ComfyUI writes no wav: the audio is stored as wav here, which needs ffmpeg on the server of Marginalia to convert it");
        }
        try {
            byte[] audio = run(workflow(request, model, ffmpeg), cancel);
            return ffmpeg ? trimmed(audio, wav) : audio;
        } finally {
            free();
        }
    }

    /** The flac of a piece without what Higgs makes after the last word ({@link BreathTrim}), as wav or mp3. */
    private static byte[] trimmed(byte[] flac, boolean wav) throws BackendException {
        byte[] decoded = Ffmpeg.flacToWav(flac);
        WavJoiner.Format format = WavJoiner.formatOf(decoded);
        byte[] piece = WavJoiner.of(format, BreathTrim.trim(format, WavJoiner.data(decoded)));
        return wav ? piece : Ffmpeg.toMp3(piece);
    }

    private byte[] run(JsonObject workflow, Cancel cancel) throws BackendException {
        JsonObject body = new JsonObject();
        body.add("prompt", workflow);
        body.addProperty("client_id", UUID.randomUUID().toString());
        HttpResponse<byte[]> queued = Http.send(builder("/prompt", SHORT_SECONDS)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString())).build());
        if (queued.statusCode() == 400) {
            // ComfyUI says why it refuses a workflow (a missing node, a value it does not take)
            throw new BackendException("ComfyUI refused the workflow: " + Http.excerpt(queued));
        }
        Http.requireOk(queued, "Queueing the speech");
        JsonElement id = parse(queued).getAsJsonObject().get("prompt_id");
        if (id == null || !id.isJsonPrimitive()) {
            throw new BackendException("ComfyUI did not return a prompt id: " + Http.excerpt(queued));
        }
        String promptId = id.getAsString();
        long deadline = System.nanoTime() + config.getTimeoutSeconds() * 1_000_000_000L;
        while (true) {
            if (cancel != null && cancel.isCancelled()) {
                interrupt();
                throw new BackendException("Cancelled");
            }
            if (System.nanoTime() > deadline) {
                interrupt();
                throw new BackendException("ComfyUI did not finish the piece in " + config.getTimeoutSeconds() + " s");
            }
            HttpResponse<byte[]> history = Http.send(builder("/history/" + promptId, SHORT_SECONDS).GET().build());
            Http.requireOk(history, "Reading the history");
            JsonObject all = parse(history).getAsJsonObject();
            if (all.has(promptId) && all.get(promptId).isJsonObject()) {
                JsonObject entry = all.getAsJsonObject(promptId);
                JsonObject audio = firstAudio(entry);
                if (audio != null) {
                    return fetch(audio);
                }
                if (isFinished(entry)) {
                    throw new BackendException("ComfyUI finished without audio" + errorOf(entry));
                }
            }
            try {
                Thread.sleep(pollMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BackendException("Interrupted", e);
            }
        }
    }

    /**
     * The workflow of one piece in the API format of ComfyUI: the engine, the TTS text node of the suite with the voice (a clip of the input folder through
     * {@code LoadAudio} and the suite's {@code CharacterVoicesNode}, or one of the suite's voices by name) and a node that saves the audio (mp3 at a constant
     * 128 kbps, which {@code Mp3Joiner} joins; flac when the storage is wav, converted here).
     */
    static JsonObject workflow(SpeechRequest request, String model) {
        return workflow(request, model, "wav".equals(request.format()));
    }

    /** @param flac the audio comes as flac (to be trimmed and converted here) instead of mp3 */
    static JsonObject workflow(SpeechRequest request, String model, boolean flac) {
        JsonObject engine = new JsonObject();
        engine.addProperty("model", model);
        engine.addProperty("device", "auto");
        engine.addProperty("dtype", "auto");
        engine.addProperty("attention", "auto");
        engine.addProperty("temperature", request.temperature());
        engine.addProperty("top_p", 0.95);
        engine.addProperty("top_k", 50);
        engine.addProperty("max_new_tokens", 2048);

        boolean clip = request.voiceMode() == SpeechRequest.VoiceMode.CLONE;
        JsonObject text = new JsonObject();
        text.add("TTS_engine", link("engine"));
        text.addProperty("text", safeText(request.text()));
        text.addProperty("narrator_voice", clip ? "none" : request.voice());
        text.addProperty("seed", request.seed() != 0 ? request.seed() : ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE));
        text.addProperty("enable_chunking", true);
        text.addProperty("max_chars_per_chunk", 400);
        text.addProperty("chunk_combination_method", "auto");
        text.addProperty("silence_between_chunks_ms", 100);
        // the audio is cached here, by the key of the entry
        text.addProperty("enable_audio_cache", false);
        text.addProperty("batch_size", 0);

        JsonObject save = new JsonObject();
        save.add("audio", link("text"));
        save.addProperty("filename_prefix", OUTPUT_PREFIX);

        JsonObject workflow = new JsonObject();
        workflow.add("engine", node(ENGINE_NODE, engine));
        if (clip) {
            JsonObject load = new JsonObject();
            load.addProperty("audio", request.voice());
            JsonObject voice = new JsonObject();
            voice.addProperty("voice_name", "none");
            voice.addProperty("reference_text", "");
            voice.addProperty("trim_start", 0.0);
            voice.addProperty("trim_end", 0.0);
            voice.addProperty("customized", false);
            voice.add("opt_audio_input", link("clip"));
            workflow.add("clip", node("LoadAudio", load));
            workflow.add("voice", node("CharacterVoicesNode", voice));
            text.add("opt_narrator", link("voice"));
        }
        workflow.add("text", node("UnifiedTTSTextNode", text));
        if (flac) {
            workflow.add("save", node("SaveAudio", save));
        } else {
            save.addProperty("quality", "128k");
            workflow.add("save", node("SaveAudioMP3", save));
        }
        return workflow;
    }

    /** The workflow of a request as the studio shows it (the model as the backend would name it without asking the server). */
    public static String workflowOf(SpeechRequest request) {
        return workflow(request, MODEL, Ffmpeg.available()).toString();
    }

    /**
     * The text with the brackets of the book turned into parentheses: the suite reads {@code [Name]} as a switch to another character and {@code [pause:1]}
     * as a pause, so a bracket of the story must not reach it as one. The sound tags of the labelling are Higgs tokens by now ({@code <|sfx:...|>}).
     */
    static String safeText(String text) {
        return text.replace('[', '(').replace(']', ')');
    }

    private static JsonObject node(String type, JsonObject inputs) {
        JsonObject node = new JsonObject();
        node.addProperty("class_type", type);
        node.add("inputs", inputs);
        return node;
    }

    private static JsonArray link(String node) {
        JsonArray link = new JsonArray();
        link.add(node);
        link.add(0);
        return link;
    }

    /** Empties the memory of ComfyUI after a piece; the weights stay loaded. A failure is only logged: the next piece may still work. */
    private void free() {
        try {
            HttpResponse<byte[]> response = Http.send(builder("/free", SHORT_SECONDS)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"free_memory\": true}")).build());
            Http.requireOk(response, "Freeing the memory of ComfyUI");
        } catch (BackendException e) {
            AudiobookLog.get().warn("Could not free the memory of ComfyUI after a piece: {}", e.getMessage());
        }
    }

    private void interrupt() {
        try {
            Http.send(builder("/interrupt", SHORT_SECONDS).POST(HttpRequest.BodyPublishers.noBody()).build());
        } catch (BackendException e) {
            AudiobookLog.get().warn("Could not interrupt ComfyUI: {}", e.getMessage());
        }
    }

    /** The audio the outputs name first, a saved one before a temporary one. */
    static JsonObject firstAudio(JsonObject entry) {
        if (!entry.has("outputs") || !entry.get("outputs").isJsonObject()) {
            return null;
        }
        JsonObject temp = null;
        for (var output : entry.getAsJsonObject("outputs").entrySet()) {
            if (!output.getValue().isJsonObject() || !output.getValue().getAsJsonObject().has("audio")
                    || !output.getValue().getAsJsonObject().get("audio").isJsonArray()) {
                continue;
            }
            for (JsonElement element : output.getValue().getAsJsonObject().getAsJsonArray("audio")) {
                if (!element.isJsonObject() || !element.getAsJsonObject().has("filename")) {
                    continue;
                }
                JsonObject audio = element.getAsJsonObject();
                if (!audio.has("type") || "output".equals(audio.get("type").getAsString())) {
                    return audio;
                }
                if (temp == null) {
                    temp = audio;
                }
            }
        }
        return temp;
    }

    private static boolean isFinished(JsonObject entry) {
        if (!entry.has("status") || !entry.get("status").isJsonObject()) {
            return false;
        }
        JsonObject status = entry.getAsJsonObject("status");
        return status.has("completed") && status.get("completed").getAsBoolean()
                || status.has("status_str") && "error".equals(status.get("status_str").getAsString());
    }

    private static String errorOf(JsonObject entry) {
        try {
            for (JsonElement message : entry.getAsJsonObject("status").getAsJsonArray("messages")) {
                JsonArray pair = message.getAsJsonArray();
                if ("execution_error".equals(pair.get(0).getAsString())) {
                    JsonObject data = pair.get(1).getAsJsonObject();
                    return ": " + data.get("exception_message").getAsString().strip();
                }
            }
        } catch (RuntimeException ignored) {
            // no readable error, the generic message stays
        }
        return "";
    }

    private byte[] fetch(JsonObject audio) throws BackendException {
        String query = "filename=" + enc(audio.get("filename").getAsString())
                + "&subfolder=" + enc(audio.has("subfolder") ? audio.get("subfolder").getAsString() : "")
                + "&type=" + enc(audio.has("type") ? audio.get("type").getAsString() : "output");
        HttpResponse<byte[]> response = Http.send(builder("/view?" + query, SHORT_SECONDS).GET().build(), MAX_AUDIO_BYTES);
        Http.requireOk(response, "Fetching the audio");
        if (response.body().length == 0) {
            throw new BackendException("ComfyUI returned an empty audio file");
        }
        return response.body();
    }

    @Override
    public List<String> referenceFiles() throws BackendException {
        JsonObject info = objectInfo("LoadAudio");
        return info == null ? List.of() : options(info, "audio");
    }

    @Override
    public List<String> predefinedVoices() throws BackendException {
        JsonObject info = objectInfo("CharacterVoicesNode");
        List<String> voices = new ArrayList<>(info == null ? List.of() : options(info, "voice_name"));
        voices.remove("none");
        return voices;
    }

    @Override
    public String ensureReference(byte[] audio, String extension) throws BackendException {
        String ext = extension == null ? "" : extension.toLowerCase(Locale.ROOT).replace(".", "");
        if (!ext.equals("wav") && !ext.equals("mp3")) {
            throw new BackendException("A reference clip must be wav or mp3");
        }
        if (audio == null || audio.length == 0 || audio.length > MAX_CLIP_BYTES) {
            throw new BackendException("A reference clip must be between 1 byte and " + MAX_CLIP_BYTES / (1024 * 1024) + " MB");
        }
        String name = ChatterboxBackend.referenceName(audio, ext);
        if (referenceFiles().contains(name)) {
            return name;
        }
        // the upload of images takes any file into the input folder
        Multipart form = new Multipart().file("image", name, ext.equals("wav") ? "audio/wav" : "audio/mpeg", audio)
                .field("type", "input").field("overwrite", "true");
        HttpResponse<byte[]> response = Http.send(builder("/upload/image", config.getTimeoutSeconds())
                .header("Content-Type", form.contentType())
                .POST(HttpRequest.BodyPublishers.ofByteArray(form.body())).build());
        Http.requireOk(response, "Uploading the reference clip");
        JsonElement answer = parse(response);
        String kept = answer.isJsonObject() && answer.getAsJsonObject().has("name") ? answer.getAsJsonObject().get("name").getAsString() : null;
        if (!name.equals(kept)) {
            throw new BackendException("ComfyUI did not keep the reference clip under its name: " + Http.excerpt(response));
        }
        return name;
    }

    /** The description of a node, null when ComfyUI does not know it (it answers {@code {}}). */
    private JsonObject objectInfo(String node) throws BackendException {
        HttpResponse<byte[]> response = Http.send(builder("/object_info/" + node, SHORT_SECONDS).GET().build(), MAX_AUDIO_BYTES);
        Http.requireOk(response, "Reading the nodes of ComfyUI");
        JsonElement all = parse(response);
        return all.isJsonObject() && all.getAsJsonObject().has(node) && all.getAsJsonObject().get(node).isJsonObject()
                ? all.getAsJsonObject().getAsJsonObject(node) : null;
    }

    /**
     * The choices of an input of a node: {@code [[...], {...}]} in the older format of {@code /object_info}, {@code ["COMBO", {"options": [...]}]} in the
     * newer one.
     */
    static List<String> options(JsonObject info, String input) {
        List<String> values = new ArrayList<>();
        JsonElement spec = null;
        for (String section : List.of("required", "optional")) {
            JsonObject inputs = info.has("input") && info.get("input").isJsonObject() ? info.getAsJsonObject("input") : null;
            if (inputs != null && inputs.has(section) && inputs.get(section).isJsonObject() && inputs.getAsJsonObject(section).has(input)) {
                spec = inputs.getAsJsonObject(section).get(input);
                break;
            }
        }
        if (spec == null || !spec.isJsonArray() || spec.getAsJsonArray().isEmpty()) {
            return values;
        }
        JsonArray array = spec.getAsJsonArray();
        JsonElement choices = array.get(0).isJsonArray() ? array.get(0)
                : array.size() > 1 && array.get(1).isJsonObject() ? array.get(1).getAsJsonObject().get("options") : null;
        if (choices != null && choices.isJsonArray()) {
            for (JsonElement choice : choices.getAsJsonArray()) {
                if (choice.isJsonPrimitive()) {
                    values.add(choice.getAsString());
                }
            }
        }
        return values;
    }

    private HttpRequest.Builder builder(String path, int timeoutSeconds) throws BackendException {
        URI uri = UrlGuard.check(config.getBaseUrl().strip().replaceAll("/+$", "") + path, config.isLocal());
        HttpRequest.Builder builder = Http.request(uri, timeoutSeconds);
        if (apiKey != null && !apiKey.isBlank()) {
            builder.header("Authorization", "Bearer " + apiKey);
        }
        return builder;
    }

    private static JsonElement parse(HttpResponse<byte[]> response) throws BackendException {
        try {
            return JsonParser.parseString(new String(response.body(), StandardCharsets.UTF_8));
        } catch (RuntimeException e) {
            throw new BackendException("ComfyUI answered with something that is not JSON: " + Http.excerpt(response), e);
        }
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
