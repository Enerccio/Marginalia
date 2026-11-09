package com.github.enerccio.marginalia.extensions.imagegenerator.model;

import com.github.enerccio.marginalia.extensions.httphooks.HttpHook;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BackendConfigTest {

    private static final Gson GSON = new Gson();

    @Test
    void theOneRequestOfAnOldSettingBecomesTheList() {
        // saved before the lists: a single "before" and "after" request, some headers still in plain text
        BackendConfig config = GSON.fromJson("""
                {"name": "ComfyUI", "baseUrl": "http://x:8188",
                 "before": {"url": "http://proxy/unload", "method": "POST", "headers": "Authorization: Bearer old"},
                 "after": {"url": "", "method": "POST"}}""", BackendConfig.class);

        List<HttpHook> before = config.getBeforeHooks();
        assertEquals(1, before.size());
        assertEquals("http://proxy/unload", before.getFirst().getUrl());
        assertEquals("Authorization: Bearer old", before.getFirst().headersText(null));
        assertTrue(config.getAfterHooks().isEmpty(), "an empty request is no request");
    }

    @Test
    void aSettingWithoutAnyRequestHasEmptyLists() {
        BackendConfig config = GSON.fromJson("{\"name\": \"OpenAI\"}", BackendConfig.class);
        assertTrue(config.getBeforeHooks().isEmpty());
        assertTrue(config.getAfterHooks().isEmpty());
        assertTrue(new BackendConfig().getBeforeHooks().isEmpty());
    }

    @Test
    void settingTheListsReplacesTheOldRequestAndRoundTrips() {
        BackendConfig config = GSON.fromJson("{\"before\": {\"url\": \"http://old/\"}}", BackendConfig.class);
        HttpHook unload = new HttpHook();
        unload.setUrl("http://litellm/v1/chat/completions");
        unload.setProtectedHeaders("sealed");
        HttpHook start = new HttpHook();
        start.setUrl("http://litellm/gpu/comfy/start");
        start.setTimeoutSeconds(120);
        start.setWaitSeconds(3);

        config.setBeforeHooks(List.of(unload, start));
        BackendConfig again = GSON.fromJson(GSON.toJson(config), BackendConfig.class);

        List<HttpHook> hooks = again.getBeforeHooks();
        assertEquals(2, hooks.size(), "the old single request is gone");
        assertEquals("http://litellm/v1/chat/completions", hooks.get(0).getUrl());
        assertEquals("sealed", hooks.get(0).getProtectedHeaders());
        assertEquals(120, hooks.get(1).getTimeoutSeconds());
        assertEquals(3, hooks.get(1).getWaitSeconds());
        assertFalse(GSON.toJson(config).contains("http://old/"));
    }

    @Test
    void theListsAreCopies() {
        BackendConfig config = new BackendConfig();
        HttpHook hook = new HttpHook();
        hook.setUrl("http://a/");
        config.setAfterHooks(List.of(hook));

        config.getAfterHooks().clear();

        assertEquals(1, config.getAfterHooks().size());
    }
}
