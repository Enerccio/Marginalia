package com.github.enerccio.marginalia.extensions.imagegenerator.service;

import com.github.enerccio.marginalia.extensions.imagegenerator.model.BackendConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * ComfyUI: the workflow of the backend is posted to {@code /prompt}, {@code /history/<id>} is polled until the job has outputs
 * and the first image is fetched from {@code /view}.
 */
public class ComfyUiBackend implements ImageBackend {

    private static final long POLL_MILLIS = 1000;

    private final BackendConfig config;
    private final String apiKey;
    private final long pollMillis;

    public ComfyUiBackend(BackendConfig config, String apiKey) {
        this(config, apiKey, POLL_MILLIS);
    }

    ComfyUiBackend(BackendConfig config, String apiKey, long pollMillis) {
        this.config = config;
        this.apiKey = apiKey;
        this.pollMillis = pollMillis;
    }

    @Override
    public byte[] generate(ImageRequest request, Cancel cancel) throws ImageException {
        JsonObject workflow = WorkflowTemplate.fill(config.getWorkflow(), request);
        JsonObject body = new JsonObject();
        body.add("prompt", workflow);
        body.addProperty("client_id", UUID.randomUUID().toString());

        HttpResponse<byte[]> queued = Http.send(builder("/prompt")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString())).build());
        Http.requireOk(queued, "Queueing the workflow");
        String promptId = parse(queued).getAsJsonObject().has("prompt_id")
                ? parse(queued).getAsJsonObject().get("prompt_id").getAsString() : null;
        if (promptId == null) {
            throw new ImageException("ComfyUI did not return a prompt id: " + Http.excerpt(queued));
        }

        long deadline = System.nanoTime() + config.getTimeoutSeconds() * 1_000_000_000L;
        while (true) {
            if (cancel.isCancelled()) {
                interrupt();
                throw new ImageException("Cancelled");
            }
            if (System.nanoTime() > deadline) {
                interrupt();
                throw new ImageException("ComfyUI did not finish in " + config.getTimeoutSeconds() + " s");
            }
            HttpResponse<byte[]> history = Http.send(builder("/history/" + promptId).GET().build());
            Http.requireOk(history, "Reading the history");
            JsonObject all = parse(history).getAsJsonObject();
            if (all.has(promptId)) {
                JsonObject entry = all.getAsJsonObject(promptId);
                JsonObject image = firstImage(entry);
                if (image != null) {
                    return fetch(image);
                }
                if (isFinished(entry)) {
                    throw new ImageException("The workflow finished without an image" + errorOf(entry));
                }
            }
            try {
                Thread.sleep(pollMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ImageException("Interrupted", e);
            }
        }
    }

    @Override
    public void test() throws ImageException {
        Http.requireOk(Http.send(builder("/system_stats").GET().build()), "Connecting to ComfyUI");
    }

    /** The image the outputs name first, a saved one before a preview. */
    static JsonObject firstImage(JsonObject entry) {
        if (!entry.has("outputs") || !entry.get("outputs").isJsonObject()) {
            return null;
        }
        JsonObject temp = null;
        for (var output : entry.getAsJsonObject("outputs").entrySet()) {
            if (!output.getValue().isJsonObject() || !output.getValue().getAsJsonObject().has("images")) {
                continue;
            }
            JsonArray images = output.getValue().getAsJsonObject().getAsJsonArray("images");
            for (JsonElement image : images) {
                JsonObject o = image.getAsJsonObject();
                if (!o.has("filename")) {
                    continue;
                }
                if (!o.has("type") || "output".equals(o.get("type").getAsString())) {
                    return o;
                }
                if (temp == null) {
                    temp = o;
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

    private byte[] fetch(JsonObject image) throws ImageException {
        String query = "filename=" + enc(image.get("filename").getAsString())
                + "&subfolder=" + enc(image.has("subfolder") ? image.get("subfolder").getAsString() : "")
                + "&type=" + enc(image.has("type") ? image.get("type").getAsString() : "output");
        HttpResponse<byte[]> response = Http.send(builder("/view?" + query).GET().build());
        Http.requireOk(response, "Fetching the image");
        return response.body();
    }

    private void interrupt() {
        try {
            Http.send(builder("/interrupt").POST(HttpRequest.BodyPublishers.noBody()).build());
        } catch (ImageException e) {
            ImageGeneratorLogs.warn("Could not interrupt ComfyUI: {}", e.getMessage());
        }
    }

    private HttpRequest.Builder builder(String path) throws ImageException {
        String base = config.getBaseUrl().replaceAll("/+$", "");
        URI uri = UrlGuard.check(base + path, config.isLocal());
        HttpRequest.Builder builder = Http.request(uri, 30);
        if (apiKey != null && !apiKey.isBlank()) {
            builder.header("Authorization", "Bearer " + apiKey);
        }
        return builder;
    }

    private static JsonElement parse(HttpResponse<byte[]> response) throws ImageException {
        try {
            return JsonParser.parseString(new String(response.body(), StandardCharsets.UTF_8));
        } catch (RuntimeException e) {
            throw new ImageException("ComfyUI answered with something that is not JSON: " + Http.excerpt(response), e);
        }
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
