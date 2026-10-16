package com.github.enerccio.marginalia.extensions.googlegenai;

import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.function.UnaryOperator;

/**
 * The provider's part of an AI: what the dialog stores in {@link ExtensionAI#getSettings()}. The API key is kept
 * encrypted there ({@code InferenceServices.protectSecret}).
 *
 * @param apiKey         the key of Google AI Studio, decrypted; empty when there is none
 * @param model          the model, e.g. {@code gemini-2.5-flash} (without the {@code models/} prefix)
 * @param baseUrl        another endpoint (a proxy), empty for Google's
 * @param timeoutSeconds how long to wait for the first text and between chunks
 * @param maxRetries     retries after 429 / 5xx / connection errors
 * @param safetyOff      switch off the adjustable safety filters of Gemini
 */
record GoogleGenAiSettings(String apiKey, String model, String baseUrl, int timeoutSeconds, int maxRetries,
                           boolean safetyOff) {

    static final String API_KEY = "apiKey";
    static final String MODEL = "model";
    static final String BASE_URL = "baseUrl";
    static final String TIMEOUT_SECONDS = "requestTimeoutSeconds";
    static final String MAX_RETRIES = "maxRetries";
    static final String SAFETY_OFF = "safetyFiltersOff";

    static final int DEFAULT_TIMEOUT_SECONDS = 300;
    static final int DEFAULT_MAX_RETRIES = 2;

    /**
     * @param reveal decrypts the stored API key
     */
    static GoogleGenAiSettings of(ExtensionAI ai, UnaryOperator<String> reveal) {
        JsonObject settings = ai.getSettings();
        String key = reveal.apply(string(settings, API_KEY));
        int timeout = integer(settings, TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS);
        int retries = integer(settings, MAX_RETRIES, DEFAULT_MAX_RETRIES);
        return new GoogleGenAiSettings(
                StringUtils.defaultString(key),
                normalizeModel(string(settings, MODEL)),
                StringUtils.trimToEmpty(string(settings, BASE_URL)),
                timeout > 0 ? timeout : DEFAULT_TIMEOUT_SECONDS,
                Math.max(0, retries),
                settings.has(SAFETY_OFF) && settings.get(SAFETY_OFF).isJsonPrimitive() && settings.get(SAFETY_OFF).getAsBoolean());
    }

    /** The stored (encrypted) key of the AI, empty when there is none. */
    static String storedKey(ExtensionAI ai) {
        return string(ai.getSettings(), API_KEY);
    }

    /** {@code models/gemini-2.5-flash} is the name the API lists, the model is addressed without the prefix. */
    static String normalizeModel(String model) {
        String trimmed = StringUtils.trimToEmpty(model);
        return trimmed.startsWith("models/") ? trimmed.substring("models/".length()) : trimmed;
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
