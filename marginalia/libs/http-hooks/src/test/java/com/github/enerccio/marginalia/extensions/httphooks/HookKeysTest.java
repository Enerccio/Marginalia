package com.github.enerccio.marginalia.extensions.httphooks;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;

class HookKeysTest {

    private static final UnaryOperator<String> PLAIN = s -> s;

    private static HttpHook hook(String url, String headers) {
        HttpHook hook = new HttpHook();
        hook.setUrl(url);
        hook.setHeaders(headers);
        hook.setBody("{}");
        hook.setTimeoutSeconds(30);
        return hook;
    }

    @Test
    void theKeyIsAddedToARequestWithoutAuthorization() {
        HttpHook stored = hook("http://proxy/v1/chat/completions", "X-Llama-Unload: all");

        List<HttpHook> sent = HookKeys.withBearer(List.of(stored), " secret ", PLAIN);

        assertEquals(1, sent.size());
        assertEquals("X-Llama-Unload: all\nAuthorization: Bearer secret", sent.getFirst().headersText(PLAIN));
        assertEquals("http://proxy/v1/chat/completions", sent.getFirst().getUrl());
        assertEquals("{}", sent.getFirst().getBody());
        assertEquals(30, sent.getFirst().getTimeoutSeconds());
        assertEquals("X-Llama-Unload: all", stored.headersText(PLAIN), "the stored request is not changed");
    }

    @Test
    void anAuthorizationOfItsOwnIsKept() {
        HttpHook own = hook("http://other/free", "authorization: Bearer mine");

        assertSame(own, HookKeys.withBearer(List.of(own), "secret", PLAIN).getFirst());
    }

    @Test
    void withoutAKeyTheRequestsStayAndEmptyOnesAreLeftOutWithOne() {
        List<HttpHook> hooks = new ArrayList<>(List.of(hook("http://a/", null), new HttpHook()));

        assertSame(hooks, HookKeys.withBearer(hooks, " ", PLAIN));
        assertSame(hooks, HookKeys.withBearer(hooks, null, PLAIN));
        assertNull(HookKeys.withBearer(null, "secret", PLAIN));
        List<HttpHook> sent = HookKeys.withBearer(hooks, "secret", PLAIN);
        assertEquals(1, sent.size());
        assertEquals("Authorization: Bearer secret", sent.getFirst().headersText(PLAIN));
    }
}
