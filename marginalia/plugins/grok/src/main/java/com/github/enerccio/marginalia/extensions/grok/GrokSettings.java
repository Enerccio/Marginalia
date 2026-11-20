package com.github.enerccio.marginalia.extensions.grok;

import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * The provider's part of an AI: what the dialog stores in {@link ExtensionAI#getSettings()}. The API key is kept
 * encrypted there ({@code InferenceServices.protectSecret}).
 *
 * @param apiKey         the key of the xAI Console, decrypted; empty when there is none
 * @param model          the model, e.g. {@code grok-4}
 * @param baseUrl        another endpoint (a proxy), empty for xAI's ({@value #DEFAULT_BASE_URL})
 * @param timeoutSeconds how long to wait for the first text and between chunks
 * @param maxRetries     retries after 429 / 5xx / connection errors
 * @param effortMode     whether {@code reasoning_effort} is sent
 */
record GrokSettings(String apiKey, String model, String baseUrl, int timeoutSeconds, int maxRetries, EffortMode effortMode) {

    /** When the reasoning effort of the AI is sent: only {@code grok-3-mini} takes it, the others refuse the parameter. */
    enum EffortMode {
        AUTO, ALWAYS, NEVER;

        static EffortMode of(String name) {
            try {
                return name == null || name.isBlank() ? AUTO : valueOf(name.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return AUTO;
            }
        }
    }

    static final String API_KEY = "apiKey";
    static final String MODEL = "model";
    static final String BASE_URL = "baseUrl";
    static final String TIMEOUT_SECONDS = "requestTimeoutSeconds";
    static final String MAX_RETRIES = "maxRetries";
    static final String EFFORT_MODE = "reasoningEffortMode";

    static final String DEFAULT_BASE_URL = "https://api.x.ai/v1";
    static final int DEFAULT_TIMEOUT_SECONDS = 300;
    static final int DEFAULT_MAX_RETRIES = 2;

    private static final Pattern TAKES_EFFORT = Pattern.compile("^grok-3-mini.*$");

    /**
     * @param reveal decrypts the stored API key
     */
    static GrokSettings of(ExtensionAI ai, UnaryOperator<String> reveal) {
        JsonObject settings = ai.getSettings();
        String key = reveal.apply(string(settings, API_KEY));
        int timeout = integer(settings, TIMEOUT_SECONDS, DEFAULT_TIMEOUT_SECONDS);
        int retries = integer(settings, MAX_RETRIES, DEFAULT_MAX_RETRIES);
        return new GrokSettings(
                StringUtils.defaultString(key),
                StringUtils.trimToEmpty(string(settings, MODEL)),
                StringUtils.trimToEmpty(string(settings, BASE_URL)),
                timeout > 0 ? timeout : DEFAULT_TIMEOUT_SECONDS,
                Math.max(0, retries),
                EffortMode.of(string(settings, EFFORT_MODE)));
    }

    /** The endpoint requests go to, without a trailing slash: the proxy, or xAI's. */
    String endpoint() {
        String url = baseUrl.isEmpty() ? DEFAULT_BASE_URL : baseUrl;
        return StringUtils.removeEnd(url, "/");
    }

    /** Whether the request carries {@code reasoning_effort}. */
    boolean sendsEffort() {
        return switch (effortMode) {
            case ALWAYS -> true;
            case NEVER -> false;
            case AUTO -> TAKES_EFFORT.matcher(model).matches();
        };
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
