package com.github.enerccio.marginalia.extensions.audiobook.recognize;

import com.github.enerccio.marginalia.domain.service.job.JobCallType;
import com.github.enerccio.marginalia.extensions.audiobook.AudiobookLog;
import com.github.enerccio.marginalia.extensions.audiobook.model.RecognizerConfig;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.extensions.httphooks.Http;
import com.github.enerccio.marginalia.extensions.httphooks.JobCalls;
import com.github.enerccio.marginalia.extensions.httphooks.UrlGuard;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Speech recognition in ComfyUI with the TTS Audio Suite: the audio is uploaded into the input folder, a workflow of {@code GraniteASREngineNode},
 * {@code UnifiedASRTranscribeNode} and the core {@code PreviewAny} (which puts the text into the history) is queued and the text read from
 * {@code /history}. The engine computes in float32: in bf16 the recognitions after the first came out as gibberish on an AMD R9700. Every recognition ends with {@code POST /free} that unloads the models: on the author's AMD R9700 jobs of the suite that ran one
 * after the other without it crashed the GPU.
 */
public class ComfyGraniteRecognizer implements SpeechRecognizer {

    static final String ENGINE_NODE = "GraniteASREngineNode";
    static final String TRANSCRIBE_NODE = "UnifiedASRTranscribeNode";
    static final String PREVIEW_NODE = "PreviewAny";

    private static final int SHORT_SECONDS = 30;
    private static final long POLL_MILLIS = 1000;
    private static final int MAX_AUDIO_BYTES = 64 * 1024 * 1024;

    /** The languages the transcribe node lists, by code; any other code is recognised as {@code Auto}. */
    private static final Map<String, String> LANGUAGES = Map.ofEntries(
            Map.entry("en", "English"), Map.entry("zh", "Chinese"), Map.entry("de", "German"), Map.entry("fr", "French"), Map.entry("es", "Spanish"),
            Map.entry("pt", "Portuguese"), Map.entry("it", "Italian"), Map.entry("ko", "Korean"), Map.entry("ru", "Russian"), Map.entry("ja", "Japanese"),
            Map.entry("nl", "Dutch"), Map.entry("pl", "Polish"), Map.entry("cs", "Czech"), Map.entry("tr", "Turkish"), Map.entry("ar", "Arabic"),
            Map.entry("sv", "Swedish"), Map.entry("da", "Danish"), Map.entry("fi", "Finnish"), Map.entry("el", "Greek"), Map.entry("hu", "Hungarian"),
            Map.entry("ro", "Romanian"), Map.entry("hi", "Hindi"), Map.entry("th", "Thai"), Map.entry("vi", "Vietnamese"), Map.entry("id", "Indonesian"));

    private final RecognizerConfig config;
    private final String apiKey;
    private final long pollMillis;

    public ComfyGraniteRecognizer(RecognizerConfig config, String apiKey) {
        this(config, apiKey, POLL_MILLIS);
    }

    ComfyGraniteRecognizer(RecognizerConfig config, String apiKey, long pollMillis) {
        this.config = config;
        this.apiKey = apiKey;
        this.pollMillis = pollMillis;
    }

    @Override
    public void test() throws BackendException {
        if (config.getBaseUrl().isBlank()) {
            throw new BackendException("The address of ComfyUI is empty");
        }
        for (String node : new String[]{ENGINE_NODE, TRANSCRIBE_NODE, PREVIEW_NODE}) {
            HttpResponse<byte[]> response = Http.send(builder("/object_info/" + node, SHORT_SECONDS).GET().build(), MAX_AUDIO_BYTES);
            Http.requireOk(response, "Reading the nodes of ComfyUI");
            JsonElement info = parse(response);
            if (!info.isJsonObject() || !info.getAsJsonObject().has(node)) {
                throw new BackendException("ComfyUI has no " + node + ": install the TTS Audio Suite (diodiogod/TTS-Audio-Suite) and restart it");
            }
        }
    }

    @Override
    public String transcribe(byte[] audio, String extension, String language, Cancel cancel) throws BackendException {
        return JobCalls.track(JobCallType.AUDIO, "audiobook", config.getBaseUrl(), config.getModel(), () -> recognize(audio, extension, language, cancel));
    }

    private String recognize(byte[] audio, String extension, String language, Cancel cancel) throws BackendException {
        if (audio == null || audio.length == 0) {
            throw new BackendException("There is no audio to recognise");
        }
        String name = upload(audio, extension);
        try {
            return run(workflow(name, config.getModel(), language), cancel);
        } finally {
            free();
        }
    }

    /** The workflow of one recognition in the API format of ComfyUI. */
    static JsonObject workflow(String inputFile, String model, String language) {
        JsonObject engine = new JsonObject();
        engine.addProperty("model_name", model);
        engine.addProperty("device", "auto");
        engine.addProperty("max_new_tokens", 1024);
        // bf16 (the default on an AMD R9700 with ROCm) gave gibberish from the second recognition on, as it did for Higgs
        engine.addProperty("dtype", "float32");
        JsonObject load = new JsonObject();
        load.addProperty("audio", inputFile);
        JsonObject transcribe = new JsonObject();
        transcribe.add("engine", link("engine"));
        transcribe.add("audio", link("clip"));
        transcribe.addProperty("language", languageName(language));
        JsonObject preview = new JsonObject();
        preview.add("source", link("asr"));
        JsonObject workflow = new JsonObject();
        workflow.add("engine", node(ENGINE_NODE, engine));
        workflow.add("clip", node("LoadAudio", load));
        workflow.add("asr", node(TRANSCRIBE_NODE, transcribe));
        workflow.add("text", node(PREVIEW_NODE, preview));
        return workflow;
    }

    static String languageName(String code) {
        if (code == null || code.isBlank()) {
            return "Auto";
        }
        return LANGUAGES.getOrDefault(code.strip().toLowerCase(Locale.ROOT).split("[-_]")[0], "Auto");
    }

    private String upload(byte[] audio, String extension) throws BackendException {
        String ext = "mp3".equalsIgnoreCase(extension) ? "mp3" : "wav";
        String name = "asr-" + hash(audio) + "." + ext;
        String boundary = "----marginalia" + UUID.randomUUID().toString().replace("-", "");
        byte[] body = multipart(boundary, name, ext.equals("wav") ? "audio/wav" : "audio/mpeg", audio);
        HttpResponse<byte[]> response = Http.send(builder("/upload/image", config.getTimeoutSeconds())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body)).build());
        Http.requireOk(response, "Uploading the audio");
        JsonElement answer = parse(response);
        return answer.isJsonObject() && answer.getAsJsonObject().has("name") ? answer.getAsJsonObject().get("name").getAsString() : name;
    }

    private String run(JsonObject workflow, Cancel cancel) throws BackendException {
        JsonObject body = new JsonObject();
        body.add("prompt", workflow);
        body.addProperty("client_id", UUID.randomUUID().toString());
        HttpResponse<byte[]> queued = Http.send(builder("/prompt", SHORT_SECONDS)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString())).build());
        if (queued.statusCode() == 400) {
            throw new BackendException("ComfyUI refused the recognition: " + Http.excerpt(queued));
        }
        Http.requireOk(queued, "Queueing the recognition");
        JsonElement id = parse(queued).getAsJsonObject().get("prompt_id");
        if (id == null || !id.isJsonPrimitive()) {
            throw new BackendException("ComfyUI did not return a prompt id: " + Http.excerpt(queued));
        }
        String promptId = id.getAsString();
        long deadline = System.nanoTime() + config.getTimeoutSeconds() * 1_000_000_000L;
        while (true) {
            if (cancel != null && cancel.isCancelled()) {
                throw new BackendException("Cancelled");
            }
            if (System.nanoTime() > deadline) {
                throw new BackendException("ComfyUI did not finish the recognition in " + config.getTimeoutSeconds() + " s");
            }
            HttpResponse<byte[]> history = Http.send(builder("/history/" + promptId, SHORT_SECONDS).GET().build());
            Http.requireOk(history, "Reading the history");
            JsonObject all = parse(history).getAsJsonObject();
            if (all.has(promptId) && all.get(promptId).isJsonObject()) {
                JsonObject entry = all.getAsJsonObject(promptId);
                String text = textOf(entry);
                if (text != null) {
                    return text.strip();
                }
                if (finished(entry)) {
                    throw new BackendException("ComfyUI finished without a text" + errorOf(entry));
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

    /** The text the preview node put into the outputs, null when it is not there (yet). */
    static String textOf(JsonObject entry) {
        if (!entry.has("outputs") || !entry.get("outputs").isJsonObject()) {
            return null;
        }
        for (var output : entry.getAsJsonObject("outputs").entrySet()) {
            JsonElement value = output.getValue();
            if (value.isJsonObject() && value.getAsJsonObject().has("text") && value.getAsJsonObject().get("text").isJsonArray()) {
                JsonArray texts = value.getAsJsonObject().getAsJsonArray("text");
                StringBuilder all = new StringBuilder();
                for (JsonElement text : texts) {
                    if (text.isJsonPrimitive()) {
                        all.append(all.isEmpty() ? "" : " ").append(text.getAsString());
                    }
                }
                return all.toString();
            }
        }
        return null;
    }

    private static boolean finished(JsonObject entry) {
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
                    return ": " + pair.get(1).getAsJsonObject().get("exception_message").getAsString().strip();
                }
            }
        } catch (RuntimeException ignored) {
            // no readable error
        }
        return "";
    }

    /** Unloads the models of ComfyUI; a failure is only logged. */
    private void free() {
        try {
            Http.requireOk(Http.send(builder("/free", SHORT_SECONDS)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"unload_models\": true, \"free_memory\": true}")).build()), "Freeing ComfyUI");
        } catch (BackendException e) {
            AudiobookLog.get().warn("Could not free the memory of ComfyUI after a recognition: {}", e.getMessage());
        }
    }

    private static byte[] multipart(String boundary, String filename, String contentType, byte[] content) {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        out.writeBytes(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"image\"; filename=\"" + filename + "\"\r\nContent-Type: "
                + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        out.writeBytes(content);
        out.writeBytes(("\r\n--" + boundary + "\r\nContent-Disposition: form-data; name=\"type\"\r\n\r\ninput\r\n--" + boundary
                + "\r\nContent-Disposition: form-data; name=\"overwrite\"\r\n\r\ntrue\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }

    private static String hash(byte[] audio) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(audio)).substring(0, 24);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
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
}
