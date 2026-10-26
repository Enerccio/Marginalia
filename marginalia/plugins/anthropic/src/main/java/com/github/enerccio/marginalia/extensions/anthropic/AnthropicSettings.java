package com.github.enerccio.marginalia.extensions.anthropic;

import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.function.UnaryOperator;

/**
 * The provider's part of an AI: what the dialog stores in {@link ExtensionAI#getSettings()}. The API key is kept
 * encrypted there ({@code InferenceServices.protectSecret}).
 *
 * @param apiKey         the key of the Anthropic Console, decrypted; empty when there is none
 * @param model          the model, e.g. {@code claude-opus-5-5}
 * @param baseUrl        another endpoint (a proxy), empty for Anthropic's
 * @param timeoutSeconds how long to wait for the first text and between chunks
 * @param maxRetries     retries after 429 / 5xx / connection errors
 */
record AnthropicSettings(String apiKey, String model, String baseUrl, int timeoutSeconds, int maxRetries) {

    static final String API_KEY = "apiKey";
    static final String MODEL = "model";
    static final String BASE_URL = "baseUrl";
    static final String TIMEOUT_SECONDS = "requestTimeoutSeconds";
    static final String MAX_RETRIES = "maxRetries";

    static final int DEFAULT_TIMEOUT_SECONDS = 300;
    static final int DEFAULT_MAX_RETRIES = 2;

    /**
     * @param reveal decrypts the stored API key
     */
    static AnthropicSettings of(ExtensionAI ai, UnaryOperator<String> reveal) {
        JsonObject settings = ai.getSettings();
        String key = reveal.apply(string(settings, API_KEY));
        int timeout = integer(settings, TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS);
        int retries = integer(settings, MAX_RETRIES, DEFAULT_MAX_RETRIES);
        return new AnthropicSettings(
                StringUtils.defaultString(key),
                StringUtils.trimToEmpty(string(settings, MODEL)),
                StringUtils.trimToEmpty(string(settings, BASE_URL)),
                timeout > 0 ? timeout : DEFAULT_TIMEOUT_SECONDS,
                Math.max(0, retries));
    }

    /** The stored (encrypted) key of the AI, empty when there is none. */
    static String storedKey(ExtensionAI ai) {
        return string(ai.getSettings(), API_KEY);
    }

    private static String string(JsonObject object, String key) {
        return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsString() : "";
    }

    private static int integer(JsonObject object, String key, int fallback) {
        try {
            return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsInt() : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
