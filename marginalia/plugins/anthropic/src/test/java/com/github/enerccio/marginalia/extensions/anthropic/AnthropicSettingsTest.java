package com.github.enerccio.marginalia.extensions.anthropic;

import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Reading the settings the AI dialog stored.
 */
class AnthropicSettingsTest {

    private static ExtensionAI ai() {
        ExtensionAI ai = new ExtensionAI();
        ai.setExtensionId(AnthropicFactory.EXTENSION_ID);
        return ai;
    }

    @Test
    void emptySettingsHaveTheDefaults() {
        AnthropicSettings settings = AnthropicSettings.of(ai(), key -> key);

        assertEquals("", settings.apiKey());
        assertEquals("", settings.model());
        assertEquals("", settings.baseUrl());
        assertEquals(AnthropicSettings.DEFAULT_TIMEOUT_SECONDS, settings.timeoutSeconds());
        assertEquals(AnthropicSettings.DEFAULT_MAX_RETRIES, settings.maxRetries());
    }

    @Test
    void storedValuesAreRead() {
        ExtensionAI ai = ai();
        ai.getSettings().addProperty("apiKey", "enc:secret");
        ai.getSettings().addProperty("model", " claude-opus-5-5 ");
        ai.getSettings().addProperty("baseUrl", " https://proxy.example/ ");
        ai.getSettings().addProperty("requestTimeoutSeconds", 60);
        ai.getSettings().addProperty("maxRetries", 4);

        AnthropicSettings settings = AnthropicSettings.of(ai, key -> key.replace("enc:", ""));

        assertEquals("secret", settings.apiKey());
        assertEquals("claude-opus-5-5", settings.model());
        assertEquals("https://proxy.example/", settings.baseUrl());
        assertEquals(60, settings.timeoutSeconds());
        assertEquals(4, settings.maxRetries());
    }

    @Test
    void keyThatCannotBeDecryptedIsEmpty() {
        ExtensionAI ai = ai();
        ai.getSettings().addProperty("apiKey", "enc:from-another-installation");

        assertEquals("", AnthropicSettings.of(ai, key -> null).apiKey());
    }

    @Test
    void nonsenseNumbersFallBackToTheDefaults() {
        ExtensionAI ai = ai();
        ai.getSettings().addProperty("requestTimeoutSeconds", -5);
        ai.getSettings().addProperty("maxRetries", "many");

        AnthropicSettings settings = AnthropicSettings.of(ai, key -> key);

        assertEquals(AnthropicSettings.DEFAULT_TIMEOUT_SECONDS, settings.timeoutSeconds());
        assertEquals(AnthropicSettings.DEFAULT_MAX_RETRIES, settings.maxRetries());
    }
}
