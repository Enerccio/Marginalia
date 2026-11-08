package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.extensions.httphooks.Http;
import com.github.enerccio.marginalia.extensions.httphooks.UrlGuard;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.function.UnaryOperator;

/**
 * Asks a speech server whether it is there: {@code GET /api/model-info}, which Chatterbox-TTS-Server answers also when no model is
 * loaded (it says the server is up, not the model). Nothing is started or loaded by it.
 */
public final class ServerCheck {

    /** What the server said. {@code model} is the class of the loaded model, null when none is loaded. */
    public record Result(boolean modelLoaded, String model) {
    }

    private static final int TIMEOUT_SECONDS = 15;
    private static final int MAX_ANSWER_BYTES = 256 * 1024;

    private ServerCheck() {
    }

    /** @param reveal opens the protected api key (null: no key is sent) */
    public static Result check(BackendConfig config, UnaryOperator<String> reveal) throws BackendException {
        return checkWithKey(config, config.getProtectedApiKey() == null || reveal == null ? null : reveal.apply(config.getProtectedApiKey()));
    }

    /** @param apiKey the key as it is sent, null for none */
    public static Result checkWithKey(BackendConfig config, String apiKey) throws BackendException {
        if (config.getBaseUrl().isBlank()) {
            throw new BackendException("The address of the server is empty");
        }
        URI uri = UrlGuard.check(config.getBaseUrl().strip().replaceAll("/+$", "") + "/api/model-info", config.isLocal());
        HttpRequest.Builder builder = Http.request(uri, TIMEOUT_SECONDS).GET();
        if (apiKey != null && !apiKey.isBlank()) {
            builder.header("Authorization", "Bearer " + apiKey);
        }
        HttpResponse<byte[]> response = Http.send(builder.build(), MAX_ANSWER_BYTES);
        Http.requireOk(response, "The speech server " + uri.getHost());
        try {
            JsonObject json = JsonParser.parseString(new String(response.body(), StandardCharsets.UTF_8)).getAsJsonObject();
            boolean loaded = json.has("loaded") && json.get("loaded").getAsBoolean();
            String model = loaded && json.has("class_name") && !json.get("class_name").isJsonNull() ? json.get("class_name").getAsString() : null;
            return new Result(loaded, model);
        } catch (RuntimeException e) {
            throw new BackendException("The server did not answer as a speech server (/api/model-info): " + Http.excerpt(response), e);
        }
    }
}
