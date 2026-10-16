package com.github.enerccio.marginalia.extensions.googlegenai;

import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reading the settings the AI dialog stored.
 */
class GoogleGenAiSettingsTest {

    private static ExtensionAI ai() {
        ExtensionAI ai = new ExtensionAI();
        ai.setExtensionId(GoogleGenAiFactory.EXTENSION_ID);
        return ai;
    }

    @Test
    void emptySettingsHaveTheDefaults() {
        GoogleGenAiSettings settings = GoogleGenAiSettings.of(ai(), key -> key);

        assertEquals("", settings.apiKey());
        assertEquals("", settings.model());
        assertEquals("", settings.baseUrl());
        assertEquals(GoogleGenAiSettings.DEFAULT_TIMEOUT_SECONDS, settings.timeoutSeconds());
        assertEquals(GoogleGenAiSettings.DEFAULT_MAX_RETRIES, settings.maxRetries());
        assertFalse(settings.safetyOff());
    }

    @Test
    void storedValuesAreRead() {
        ExtensionAI ai = ai();
        ai.getSettings().addProperty("apiKey", "enc:secret");
        ai.getSettings().addProperty("model", "models/gemini-2.5-flash");
        ai.getSettings().addProperty("baseUrl", " https://proxy.example/ ");
        ai.getSettings().addProperty("requestTimeoutSeconds", 60);
        ai.getSettings().addProperty("maxRetries", 4);
        ai.getSettings().addProperty("safetyFiltersOff", true);

        GoogleGenAiSettings settings = GoogleGenAiSettings.of(ai, key -> key.replace("enc:", ""));

        assertEquals("secret", settings.apiKey());
        assertEquals("gemini-2.5-flash", settings.model());
        assertEquals("https://proxy.example/", settings.baseUrl());
        assertEquals(60, settings.timeoutSeconds());
        assertEquals(4, settings.maxRetries());
        assertTrue(settings.safetyOff());
    }

    @Test
    void keyThatCannotBeDecryptedIsEmpty() {
        ExtensionAI ai = ai();
        ai.getSettings().addProperty("apiKey", "enc:from-another-installation");

        assertEquals("", GoogleGenAiSettings.of(ai, key -> null).apiKey());
    }

    @Test
    void nonsenseNumbersFallBackToTheDefaults() {
        ExtensionAI ai = ai();
        ai.getSettings().addProperty("requestTimeoutSeconds", -5);
        ai.getSettings().addProperty("maxRetries", "many");

        GoogleGenAiSettings settings = GoogleGenAiSettings.of(ai, key -> key);

        assertEquals(GoogleGenAiSettings.DEFAULT_TIMEOUT_SECONDS, settings.timeoutSeconds());
        assertEquals(GoogleGenAiSettings.DEFAULT_MAX_RETRIES, settings.maxRetries());
    }
}
