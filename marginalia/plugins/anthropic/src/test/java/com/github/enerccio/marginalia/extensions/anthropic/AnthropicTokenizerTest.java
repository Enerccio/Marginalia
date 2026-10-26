package com.github.enerccio.marginalia.extensions.anthropic;

import com.github.enerccio.marginalia.domain.components.TokenizerKey;
import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.github.enerccio.marginalia.domain.service.InferenceException;
import com.github.enerccio.marginalia.domain.service.InferenceServices;
import com.github.enerccio.marginalia.domain.service.TokenizerResolver;
import com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer.TokenizerStrategy;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.*;

/**
 * How the tokens are counted by Anthropic: the key the application caches by, the strategy, and the pause after a failure.
 */
class AnthropicTokenizerTest {

    /** Decrypting returns the stored text, the service of an AI is not there. */
    private static final InferenceServices SERVICES = (InferenceServices) Proxy.newProxyInstance(
            AnthropicTokenizerTest.class.getClassLoader(), new Class<?>[]{InferenceServices.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "revealSecret" -> args[0];
                case "forAI" -> null;
                default -> throw new UnsupportedOperationException(method.getName());
            });

    private final AnthropicFactory factory = new AnthropicFactory(SERVICES, LoggerFactory.getLogger("test"));
    private final TokenizerResolver resolver = factory.createTokenizerResolver();

    private static ExtensionAI ai(String model, String apiKey, String baseUrl) {
        ExtensionAI ai = new ExtensionAI();
        ai.setExtensionId(AnthropicFactory.EXTENSION_ID);
        ai.getSettings().addProperty("model", model);
        ai.getSettings().addProperty("apiKey", apiKey);
        if (baseUrl != null) {
            ai.getSettings().addProperty("baseUrl", baseUrl);
        }
        return ai;
    }

    @Test
    void sameEndpointModelAndKeyShareTheKey() {
        assertEquals(resolver.keyFor(ai("claude-opus-5-5", "k", null)), resolver.keyFor(ai(" claude-opus-5-5", "k", null)));
    }

    @Test
    void anotherModelKeyOrEndpointIsAnotherKey() {
        TokenizerKey key = resolver.keyFor(ai("claude-opus-5-5", "k", null));

        assertNotEquals(key, resolver.keyFor(ai("claude-sonnet-5-5", "k", null)));
        assertNotEquals(key, resolver.keyFor(ai("claude-opus-5-5", "other", null)));
        assertNotEquals(key, resolver.keyFor(ai("claude-opus-5-5", "k", "https://proxy.example")));
    }

    @Test
    void keyShowsTheEndpointAndTheModelButNotTheApiKey() {
        TokenizerKey key = resolver.keyFor(ai("claude-opus-5-5", "sk-ant-very-secret", null));

        assertEquals("anthropic#anthropic#claude-opus-5-5", key.id());
        assertFalse(key.toString().contains("sk-ant-very-secret"));
    }

    @Test
    void anthropicIsTheOnlyStrategyOfTheChain() {
        ExtensionAI ai = ai("claude-opus-5-5", "k", null);

        TokenizerStrategy strategy = resolver.chain(resolver.keyFor(ai), ai).get(0);

        assertEquals(1, resolver.chain(resolver.keyFor(ai), ai).size());
        assertEquals("ANTHROPIC_COUNT_TOKENS", strategy.getName());
        // the service of the AI is not there (the extension was unloaded): the application counts approximately
        assertThrows(IllegalStateException.class, () -> strategy.countTokens(ai, "text"));
    }

    @Test
    void failedCountPausesAskingAnthropic() {
        ExtensionAI ai = ai("claude-opus-5-5", "", null);
        AnthropicInferenceService service = new AnthropicInferenceService(ai,
                AnthropicSettings.of(ai, key -> key), LoggerFactory.getLogger("test"));

        // no API key: the first count fails with the reason...
        assertThrows(InferenceException.class, () -> service.countTokensOnline("text"));
        // ...and the next ones do not try Anthropic at all
        IllegalStateException paused = assertThrows(IllegalStateException.class, () -> service.countTokensOnline("another text"));
        assertTrue(paused.getMessage().contains("paused"));
    }
}
