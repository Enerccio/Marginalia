package com.github.enerccio.marginalia.extensions.httphooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

/**
 * A key for the requests around the work: a proxy in front of the models (LiteLLM) wants {@code Authorization: Bearer} on the unload
 * request too, and the plugin keeps that key encrypted in one field instead of in the headers of every request.
 */
public final class HookKeys {

    private HookKeys() {
    }

    /**
     * The requests with {@code Authorization: Bearer <key>} added to every one that has no {@code Authorization} header of its own. The
     * copies hold the key in plain text and live only as long as the work; the stored hooks are not changed. Empty requests are left out.
     *
     * @param key    the plain key; null or blank: the requests as they are
     * @param reveal opens the protected headers of a request
     */
    public static List<HttpHook> withBearer(List<HttpHook> hooks, String key, UnaryOperator<String> reveal) {
        if (hooks == null || key == null || key.isBlank()) {
            return hooks;
        }
        List<HttpHook> copies = new ArrayList<>();
        for (HttpHook hook : hooks) {
            if (hook == null || hook.isEmpty()) {
                continue;
            }
            String headers = hook.headersText(reveal);
            if (headers != null && headers.lines().anyMatch(line -> line.strip().toLowerCase(Locale.ROOT).startsWith("authorization:"))) {
                copies.add(hook);
                continue;
            }
            HttpHook copy = new HttpHook();
            copy.setUrl(hook.getUrl());
            copy.setMethod(hook.getMethod());
            copy.setBody(hook.getBody());
            copy.setTimeoutSeconds(hook.getTimeoutSeconds());
            copy.setWaitSeconds(hook.getWaitSeconds());
            copy.setHeaders((headers == null || headers.isBlank() ? "" : headers + "\n") + "Authorization: Bearer " + key.strip());
            copies.add(copy);
        }
        return copies;
    }
}
