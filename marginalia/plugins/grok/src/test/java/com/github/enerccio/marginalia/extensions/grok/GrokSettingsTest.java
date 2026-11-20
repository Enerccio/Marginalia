package com.github.enerccio.marginalia.extensions.grok;

import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Reading the settings the AI dialog stored. */
class GrokSettingsTest {

    private static ExtensionAI ai() {
        ExtensionAI ai = new ExtensionAI();
        ai.setExtensionId(GrokFactory.EXTENSION_ID);
        return ai;
    }

    @Test
    void emptySettingsHaveTheDefaults() {
        GrokSettings settings = GrokSettings.of(ai(), key -> key);

        assertEquals("", settings.apiKey());
        assertEquals("", settings.model());
        assertEquals(GrokSettings.DEFAULT_BASE_URL, settings.endpoint());
        assertEquals(GrokSettings.DEFAULT_TIMEOUT_SECONDS, settings.timeoutSeconds());
        assertEquals(GrokSettings.DEFAULT_MAX_RETRIES, settings.maxRetries());
        assertEquals(GrokSettings.EffortMode.AUTO, settings.effortMode());
    }

    @Test
    void storedValuesAreRead() {
        ExtensionAI ai = ai();
        ai.getSettings().addProperty("apiKey", "enc:secret");
        ai.getSettings().addProperty("model", " grok-4 ");
        ai.getSettings().addProperty("baseUrl", " https://proxy.example/v1/ ");
        ai.getSettings().addProperty("requestTimeoutSeconds", 60);
        ai.getSettings().addProperty("maxRetries", 4);
        ai.getSettings().addProperty("reasoningEffortMode", "always");

        GrokSettings settings = GrokSettings.of(ai, key -> key.replace("enc:", ""));

        assertEquals("secret", settings.apiKey());
        assertEquals("grok-4", settings.model());
        assertEquals("https://proxy.example/v1", settings.endpoint());
        assertEquals(60, settings.timeoutSeconds());
        assertEquals(4, settings.maxRetries());
        assertEquals(GrokSettings.EffortMode.ALWAYS, settings.effortMode());
    }

    @Test
    void invalidNumbersFallBack() {
        ExtensionAI ai = ai();
        ai.getSettings().addProperty("requestTimeoutSeconds", -5);
        ai.getSettings().addProperty("maxRetries", "many");
        ai.getSettings().addProperty("reasoningEffortMode", "sometimes");

        GrokSettings settings = GrokSettings.of(ai, key -> key);

        assertEquals(GrokSettings.DEFAULT_TIMEOUT_SECONDS, settings.timeoutSeconds());
        assertEquals(GrokSettings.DEFAULT_MAX_RETRIES, settings.maxRetries());
        assertEquals(GrokSettings.EffortMode.AUTO, settings.effortMode());
    }

    @Test
    void effortIsSentByTheModeAndTheModel() {
        ExtensionAI ai = ai();
        ai.getSettings().addProperty("model", "grok-3-mini");
        assertTrue(GrokSettings.of(ai, k -> k).sendsEffort());

        ai.getSettings().addProperty("model", "grok-4");
        assertFalse(GrokSettings.of(ai, k -> k).sendsEffort());

        ai.getSettings().addProperty("reasoningEffortMode", "ALWAYS");
        assertTrue(GrokSettings.of(ai, k -> k).sendsEffort());

        ai.getSettings().addProperty("model", "grok-3-mini");
        ai.getSettings().addProperty("reasoningEffortMode", "NEVER");
        assertFalse(GrokSettings.of(ai, k -> k).sendsEffort());
    }
}
