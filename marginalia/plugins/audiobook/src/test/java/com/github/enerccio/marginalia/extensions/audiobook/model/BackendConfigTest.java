package com.github.enerccio.marginalia.extensions.audiobook.model;

import com.github.enerccio.marginalia.extensions.httphooks.HttpHook;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BackendConfigTest {

    private static final Gson GSON = new Gson();

    @Test
    void aBackendWithoutSettingsHasTheDefaults() {
        BackendConfig config = GSON.fromJson("{\"name\": \"Chatterbox\"}", BackendConfig.class);

        assertEquals(BackendConfig.MODEL_TURBO, config.getModel());
        assertEquals("mp3", config.getFormat());
        assertEquals(BackendConfig.DEFAULT_TIMEOUT_SECONDS, config.getTimeoutSeconds());
        assertEquals(0.8, config.getTemperature());
        assertEquals(0.5, config.getCfgWeight());
        assertEquals(1.0, config.getSpeed());
        assertEquals(240, config.getChunkSize());
        assertTrue(config.isLocal());
        assertTrue(config.getBeforeHooks().isEmpty());
        assertTrue(config.getAfterHooks().isEmpty());
    }

    @Test
    void valuesTheServerWouldRefuseAreKeptInRange() {
        BackendConfig config = new BackendConfig();
        config.setModel("something-else");
        config.setFormat("flac");
        config.setChunkSize(5);
        assertEquals(BackendConfig.MODEL_TURBO, config.getModel());
        assertEquals("mp3", config.getFormat());
        assertEquals(BackendConfig.MIN_CHUNK_SIZE, config.getChunkSize());
        config.setChunkSize(100000);
        assertEquals(BackendConfig.MAX_CHUNK_SIZE, config.getChunkSize());
        config.setTemperature(-1);
        config.setTimeoutSeconds(-1);
        assertEquals(BackendConfig.DEFAULT_TEMPERATURE, config.getTemperature());
        assertEquals(BackendConfig.DEFAULT_TIMEOUT_SECONDS, config.getTimeoutSeconds());
        config.setModel(BackendConfig.MODEL_MULTILINGUAL);
        assertEquals(BackendConfig.MODEL_MULTILINGUAL, config.getModel());
        config.setFormat("wav");
        assertEquals("wav", config.getFormat());
        config.setFormat("opus");
        assertEquals("mp3", config.getFormat(), "only mp3 and wav can be joined");
        assertEquals("wav", config.withFormat("wav").getFormat());
        assertEquals("mp3", config.getFormat(), "the copy does not change the original");
    }

    @Test
    void theHookListsRoundTripWithTheirProtectedHeaders() {
        BackendConfig config = new BackendConfig();
        HttpHook unload = new HttpHook();
        unload.setUrl("http://litellm/v1/chat/completions");
        unload.setProtectedHeaders("sealed");
        HttpHook start = new HttpHook();
        start.setUrl("http://litellm/gpu/tts/start");
        start.setTimeoutSeconds(120);
        start.setWaitSeconds(3);
        config.setBeforeHooks(List.of(unload, start));
        HttpHook stop = new HttpHook();
        stop.setUrl("http://litellm/gpu/tts/stop");
        config.setAfterHooks(List.of(stop));
        config.setProtectedApiKey("sealed-key");

        BackendConfig again = GSON.fromJson(GSON.toJson(config), BackendConfig.class);

        assertEquals(2, again.getBeforeHooks().size());
        assertEquals("http://litellm/v1/chat/completions", again.getBeforeHooks().get(0).getUrl());
        assertEquals("sealed", again.getBeforeHooks().get(0).getProtectedHeaders());
        assertEquals(120, again.getBeforeHooks().get(1).getTimeoutSeconds());
        assertEquals(3, again.getBeforeHooks().get(1).getWaitSeconds());
        assertEquals("http://litellm/gpu/tts/stop", again.getAfterHooks().getFirst().getUrl());
        assertEquals("sealed-key", again.getProtectedApiKey());
        assertFalse(GSON.toJson(config).contains("Bearer"), "the plain key is never in the settings");
    }

    @Test
    void theListsAreCopies() {
        BackendConfig config = new BackendConfig();
        config.getBeforeHooks().add(new HttpHook());
        assertTrue(config.getBeforeHooks().isEmpty());
    }

    @Test
    void theSettingsFindTheSelectedBackendOrTheFirst() {
        AudiobookSettings settings = new AudiobookSettings();
        assertNull(settings.selected());
        BackendConfig first = new BackendConfig();
        BackendConfig second = new BackendConfig();
        settings.setBackends(List.of(first, second));
        assertSame(first, settings.selected());
        settings.setSelectedBackendId(second.getId());
        assertSame(second, settings.selected());
        settings.setSelectedBackendId("gone");
        assertSame(first, settings.selected());
        assertNull(settings.find(null));
    }

    @Test
    void aDuplicateHasEverythingButTheIdAndTheNameAndSharesNothing() {
        BackendConfig chatterbox = new BackendConfig();
        chatterbox.setName("Chatterbox");
        chatterbox.setBaseUrl("http://litellm:4000");
        chatterbox.setProtectedApiKey("protected-key");
        chatterbox.setGpuLock("lock-uuid");
        chatterbox.setNarratorVoice("clip:abc");
        chatterbox.setTemperature(0.6);
        HttpHook unload = new HttpHook();
        unload.setUrl("http://litellm:4000/v1/chat/completions");
        unload.setHeaders("X-Llama-Unload: all");
        chatterbox.setBeforeHooks(java.util.List.of(unload));
        HttpHook stop = new HttpHook();
        stop.setUrl("http://litellm:4000/gpu/tts/stop");
        chatterbox.setAfterHooks(java.util.List.of(stop));

        BackendConfig higgs = chatterbox.duplicate("Chatterbox (copy)");

        assertNotEquals(chatterbox.getId(), higgs.getId());
        assertEquals("Chatterbox (copy)", higgs.getName());
        assertEquals("http://litellm:4000", higgs.getBaseUrl());
        assertEquals("protected-key", higgs.getProtectedApiKey(), "the key as it is stored: nothing to type again");
        assertEquals("lock-uuid", higgs.getGpuLock());
        assertEquals("clip:abc", higgs.getNarratorVoice());
        assertEquals(0.6, higgs.getTemperature());
        assertEquals("X-Llama-Unload: all", higgs.getBeforeHooks().getFirst().getHeaders());
        assertEquals("http://litellm:4000/gpu/tts/stop", higgs.getAfterHooks().getFirst().getUrl());

        higgs.getAfterHooks().getFirst().setUrl("http://litellm:4000/gpu/comfy/stop");
        higgs.setModel(BackendConfig.MODEL_HIGGS);
        assertEquals("http://litellm:4000/gpu/tts/stop", chatterbox.getAfterHooks().getFirst().getUrl(), "the requests are not shared");
        assertEquals(BackendConfig.MODEL_TURBO, chatterbox.getModel());
    }

    @Test
    void theChecksHaveDefaultsAndLimits() {
        BackendConfig config = new BackendConfig();
        assertTrue(config.isCheckLength(), "the length is checked unless turned off");
        assertFalse(config.isCheckWords());
        assertFalse(config.isSendClipText());
        assertEquals(BackendConfig.DEFAULT_MAX_WORD_ERROR_PERCENT, config.getMaxWordErrorPercent());
        assertEquals(BackendConfig.DEFAULT_RETRIES, config.getRetries());
        config.setRetries(99);
        assertEquals(BackendConfig.MAX_RETRIES, config.getRetries());
        // 6 words: 11.4 s of babble is too long, 6.6 s is not
        assertTrue(BackendConfig.maxSecondsFor(6) < 11.4);
        assertTrue(BackendConfig.maxSecondsFor(6) > 6.6);
        BackendConfig saved = new com.google.gson.Gson().fromJson("{\"model\": \"higgs-audio-v3\"}", BackendConfig.class);
        assertTrue(saved.isCheckLength());
        assertEquals(com.github.enerccio.marginalia.extensions.audiobook.model.HiggsPrecision.FP32, saved.getPrecision());
    }

    @Test
    void theTextOfAClipGoesWithItsReplacement() {
        AudiobookSettings settings = new AudiobookSettings();
        assertNull(settings.clipText("a"));
        settings.setClipText("a", "  the words  ");
        assertEquals("the words", settings.clipText("a"));
        settings.moveClipText("a", "b");
        assertEquals("the words", settings.clipText("b"));
        settings.setClipText("b", " ");
        assertNull(settings.clipText("b"));
        assertNull(settings.recognizer(), "a recogniser without an address is none");
        RecognizerConfig recognizer = new RecognizerConfig();
        recognizer.setBaseUrl("http://comfy:8188");
        settings.setRecognizer(recognizer);
        assertSame(recognizer, settings.recognizer());
        assertEquals(RecognizerConfig.DEFAULT_GRANITE_MODEL, recognizer.getModel());
        recognizer.setType(RecognizerConfig.Type.OPENAI);
        assertEquals(RecognizerConfig.DEFAULT_OPENAI_MODEL, recognizer.getModel());
    }
}
