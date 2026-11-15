package com.github.enerccio.marginalia.extensions.audiobook.comfy;

import com.github.enerccio.marginalia.extensions.audiobook.AudiobookLog;
import com.github.enerccio.marginalia.extensions.audiobook.service.Multipart;
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
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * The calls of a ComfyUI that the jobs of the plugin other than the speech share (the speech recognition, the cleanup of clips): the upload of a file
 * into the input folder, queueing a workflow and waiting for it in {@code /history}, fetching an output through {@code /view}, the description of a
 * node and {@code /free}.
 */
public class ComfyClient {

    /** Whether the job was cancelled; asked while it waits. */
    public interface Cancel {
        boolean isCancelled();
    }

    private static final int SHORT_SECONDS = 30;
    private static final int MAX_BYTES = 64 * 1024 * 1024;

    private final String baseUrl;
    private final boolean local;
    private final String apiKey;
    private final int timeoutSeconds;
    private final long pollMillis;

    /**
     * @param timeoutSeconds how long an upload and a whole workflow may take
     * @param pollMillis     how often {@code /history} is read
     */
    public ComfyClient(String baseUrl, boolean local, String apiKey, int timeoutSeconds, long pollMillis) {
        this.baseUrl = baseUrl == null ? "" : baseUrl;
        this.local = local;
        this.apiKey = apiKey;
        this.timeoutSeconds = timeoutSeconds;
        this.pollMillis = pollMillis;
    }

    /** Throws when the address is empty or ComfyUI lacks one of the nodes (with the advice to install the TTS Audio Suite). */
    public void requireNodes(String... nodes) throws BackendException {
        if (baseUrl.isBlank()) {
            throw new BackendException("The address of ComfyUI is empty");
        }
        for (String node : nodes) {
            if (objectInfo(node) == null) {
                throw new BackendException("ComfyUI has no " + node + ": install the TTS Audio Suite (diodiogod/TTS-Audio-Suite) and restart it");
            }
        }
    }

    /** Puts the file into the input folder under a name made of the prefix and its bytes; the name ComfyUI kept. */
    public String upload(byte[] content, String prefix, String extension, String contentType) throws BackendException {
        String name = prefix + hash(content) + "." + extension;
        Multipart form = new Multipart().file("image", name, contentType, content).field("type", "input").field("overwrite", "true");
        HttpResponse<byte[]> response = Http.send(builder("/upload/image", timeoutSeconds)
                .header("Content-Type", form.contentType())
                .POST(HttpRequest.BodyPublishers.ofByteArray(form.body())).build());
        Http.requireOk(response, "Uploading the audio");
        JsonElement answer = parse(response);
        return answer.isJsonObject() && answer.getAsJsonObject().has("name") ? answer.getAsJsonObject().get("name").getAsString() : name;
    }

    /**
     * Queues the workflow and waits until {@code result} finds what it wants in its history entry (non-null) or the workflow ended without it.
     *
     * @param what what the workflow does, for the messages ("the recognition")
     */
    public <T> T run(JsonObject workflow, String what, Function<JsonObject, T> result, Cancel cancel) throws BackendException {
        JsonObject body = new JsonObject();
        body.add("prompt", workflow);
        body.addProperty("client_id", UUID.randomUUID().toString());
        HttpResponse<byte[]> queued = Http.send(builder("/prompt", SHORT_SECONDS)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString())).build());
        if (queued.statusCode() == 400) {
            throw new BackendException("ComfyUI refused " + what + ": " + Http.excerpt(queued));
        }
        Http.requireOk(queued, "Queueing " + what);
        JsonElement id = parse(queued).getAsJsonObject().get("prompt_id");
        if (id == null || !id.isJsonPrimitive()) {
            throw new BackendException("ComfyUI did not return a prompt id: " + Http.excerpt(queued));
        }
        String promptId = id.getAsString();
        long deadline = System.nanoTime() + timeoutSeconds * 1_000_000_000L;
        while (true) {
            if (cancel != null && cancel.isCancelled()) {
                interrupt();
                throw new BackendException("Cancelled");
            }
            if (System.nanoTime() > deadline) {
                interrupt();
                throw new BackendException("ComfyUI did not finish " + what + " in " + timeoutSeconds + " s");
            }
            HttpResponse<byte[]> history = Http.send(builder("/history/" + promptId, SHORT_SECONDS).GET().build(), MAX_BYTES);
            Http.requireOk(history, "Reading the history");
            JsonObject all = parse(history).getAsJsonObject();
            if (all.has(promptId) && all.get(promptId).isJsonObject()) {
                JsonObject entry = all.getAsJsonObject(promptId);
                if (isFinished(entry) && !errorOf(entry).isEmpty()) {
                    throw new BackendException("ComfyUI failed " + what + errorOf(entry));
                }
                T found = result.apply(entry);
                if (found != null) {
                    return found;
                }
                if (isFinished(entry)) {
                    throw new BackendException("ComfyUI finished " + what + " without a result");
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

    /** The audio files a node of the workflow saved (the {@code audio} of its output), in order; empty when it saved none. */
    public static List<JsonObject> audiosOf(JsonObject entry, String nodeId) {
        List<JsonObject> audios = new ArrayList<>();
        if (!entry.has("outputs") || !entry.get("outputs").isJsonObject()) {
            return audios;
        }
        JsonElement output = entry.getAsJsonObject("outputs").get(nodeId);
        if (output == null || !output.isJsonObject() || !output.getAsJsonObject().has("audio") || !output.getAsJsonObject().get("audio").isJsonArray()) {
            return audios;
        }
        for (JsonElement element : output.getAsJsonObject().getAsJsonArray("audio")) {
            if (element.isJsonObject() && element.getAsJsonObject().has("filename")) {
                audios.add(element.getAsJsonObject());
            }
        }
        return audios;
    }

    /** The bytes of a file an output names ({@code filename}, {@code subfolder}, {@code type}). */
    public byte[] fetch(JsonObject file) throws BackendException {
        String query = "filename=" + enc(file.get("filename").getAsString())
                + "&subfolder=" + enc(file.has("subfolder") ? file.get("subfolder").getAsString() : "")
                + "&type=" + enc(file.has("type") ? file.get("type").getAsString() : "output");
        HttpResponse<byte[]> response = Http.send(builder("/view?" + query, SHORT_SECONDS).GET().build(), MAX_BYTES);
        Http.requireOk(response, "Fetching the result");
        if (response.body().length == 0) {
            throw new BackendException("ComfyUI returned an empty file");
        }
        return response.body();
    }

    /** The description of a node, null when ComfyUI does not know it (it answers {@code {}}). */
    public JsonObject objectInfo(String node) throws BackendException {
        HttpResponse<byte[]> response = Http.send(builder("/object_info/" + node, SHORT_SECONDS).GET().build(), MAX_BYTES);
        Http.requireOk(response, "Reading the nodes of ComfyUI");
        JsonElement all = parse(response);
        return all.isJsonObject() && all.getAsJsonObject().has(node) && all.getAsJsonObject().get(node).isJsonObject()
                ? all.getAsJsonObject().getAsJsonObject(node) : null;
    }

    /**
     * The choices of an input of a node: {@code [[...], {...}]} in the older format of {@code /object_info}, {@code ["COMBO", {"options": [...]}]} in the
     * newer one.
     */
    public static List<String> options(JsonObject info, String input) {
        List<String> values = new ArrayList<>();
        if (info == null) {
            return values;
        }
        JsonElement spec = null;
        JsonObject inputs = info.has("input") && info.get("input").isJsonObject() ? info.getAsJsonObject("input") : null;
        for (String section : List.of("required", "optional")) {
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

    public static boolean isFinished(JsonObject entry) {
        if (!entry.has("status") || !entry.get("status").isJsonObject()) {
            return false;
        }
        JsonObject status = entry.getAsJsonObject("status");
        return status.has("completed") && status.get("completed").getAsBoolean()
                || status.has("status_str") && "error".equals(status.get("status_str").getAsString());
    }

    /** {@code ": <message>"} of the error the workflow ended with, empty when there is none. */
    public static String errorOf(JsonObject entry) {
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
    public void free() {
        try {
            Http.requireOk(Http.send(builder("/free", SHORT_SECONDS)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"unload_models\": true, \"free_memory\": true}")).build()), "Freeing ComfyUI");
        } catch (BackendException e) {
            AudiobookLog.get().warn("Could not free the memory of ComfyUI: {}", e.getMessage());
        }
    }

    /** Stops the workflow that runs; a failure is only logged. */
    private void interrupt() {
        try {
            Http.send(builder("/interrupt", SHORT_SECONDS).POST(HttpRequest.BodyPublishers.noBody()).build());
        } catch (BackendException e) {
            AudiobookLog.get().warn("Could not interrupt ComfyUI: {}", e.getMessage());
        }
    }

    /** A node of a workflow in the API format. */
    public static JsonObject node(String type, JsonObject inputs) {
        JsonObject node = new JsonObject();
        node.addProperty("class_type", type);
        node.add("inputs", inputs);
        return node;
    }

    /** A link to an output of another node. */
    public static JsonArray link(String node, int output) {
        JsonArray link = new JsonArray();
        link.add(node);
        link.add(output);
        return link;
    }

    private HttpRequest.Builder builder(String path, int timeout) throws BackendException {
        URI uri = UrlGuard.check(baseUrl.strip().replaceAll("/+$", "") + path, local);
        HttpRequest.Builder builder = Http.request(uri, timeout);
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

    private static String hash(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content)).substring(0, 24);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
