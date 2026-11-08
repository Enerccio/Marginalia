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
        assertEquals("mp3", config.getFormat(), "only mp3 can be joined");
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
}
