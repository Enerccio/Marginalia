package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.components.TokenizerKey;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.github.enerccio.marginalia.domain.service.impl.inference.tokenizer.TokenizerStrategy;
import com.github.enerccio.marginalia.extensions.ExtensionAIFactory;
import com.github.enerccio.marginalia.extensions.ExtensionAIForm;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An extension provider that brings its own tokenizers ({@link ExtensionAIFactory#createTokenizerResolver()}): the keys and the
 * strategies come from the provider, and only the AIs of the provider reach them.
 */
class ExtensionTokenizerResolverTest extends MarginaliaTestBase {

    @Autowired
    private TokenizerService tokenizerService;

    private String extensionId;
    private ResolvingFactory factory;

    @BeforeEach
    void register() {
        extensionId = uniqueName("ext");
        factory = new ResolvingFactory(extensionId);
        inferenceServices.registerExtensionFactory(factory);
    }

    @AfterEach
    void unregister() {
        inferenceServices.unregisterExtensionFactory(factory);
    }

    private ExtensionAI extensionAI(long id, String url) {
        ExtensionAI ai = new ExtensionAI();
        ai.setId(id);
        ai.setAiType(AIType.EXTENSION);
        ai.setExtensionId(extensionId);
        ai.getSettings().addProperty("url", url);
        return ai;
    }

    @Test
    void tokensAreCountedByTheStrategyOfTheProvider() throws Exception {
        assertThat(tokenizerService.countTokens(extensionAI(9001L, "a"), "some text")).isEqualTo(4242);
        assertThat(factory.created).hasValue(1);
    }

    @Test
    void resolverIsAskedOnceAndAisWithTheSameKeyShareTheProbe() throws Exception {
        tokenizerService.countTokens(extensionAI(9001L, "a"), "some text");
        tokenizerService.countTokens(extensionAI(9002L, "a"), "some text");
        tokenizerService.countTokens(extensionAI(9003L, "b"), "some text");

        assertThat(factory.created).hasValue(1);
        // the first AI of each key probes the chain, the others use what was found
        assertThat(factory.chains).containsExactly("ext#a", "ext#b");
    }

    @Test
    void invalidatingAnAiMakesItProbeAgain() throws Exception {
        tokenizerService.countTokens(extensionAI(9001L, "a"), "some text");
        tokenizerService.invalidateCache(9001L);
        tokenizerService.countTokens(extensionAI(9001L, "a"), "some text");

        assertThat(factory.chains).containsExactly("ext#a", "ext#a");
    }

    @Test
    void reloadedProviderIsAskedAgain() throws Exception {
        tokenizerService.countTokens(extensionAI(9001L, "a"), "some text");
        inferenceServices.unregisterExtensionFactory(factory);
        factory = new ResolvingFactory(extensionId);
        inferenceServices.registerExtensionFactory(factory);

        tokenizerService.countTokens(extensionAI(9001L, "a"), "some text");

        assertThat(factory.created).hasValue(1);
        assertThat(factory.chains).containsExactly("ext#a");
    }

    @Test
    void providerWithoutResolverCountsLocally() throws Exception {
        factory.withResolver = false;

        long tokens = tokenizerService.countTokens(extensionAI(9001L, "a"), "The quick brown fox jumps over the lazy dog.");

        assertThat(tokens).isEqualTo(tokenizerService.countTokensApprox("The quick brown fox jumps over the lazy dog."));
        assertThat(factory.created).hasValue(0);
    }

    private static class ResolvingFactory implements ExtensionAIFactory {

        private final String id;
        private final AtomicInteger created = new AtomicInteger();
        private final List<String> chains = new ArrayList<>();
        private boolean withResolver = true;

        private ResolvingFactory(String id) {
            this.id = id;
        }

        @Override
        public String getExtensionId() {
            return id;
        }

        @Override
        public String getName() {
            return "Resolving provider";
        }

        @Override
        public InferenceService createInferenceService(ExtensionAI ai) {
            return null;
        }

        @Override
        public ExtensionAIForm createForm(ExtensionAI ai) {
            return null;
        }

        @Override
        public TokenizerResolver createTokenizerResolver() {
            if (!withResolver) {
                return null;
            }
            created.incrementAndGet();
            return new TokenizerResolver() {
                @Override
                public TokenizerKey keyFor(AI ai) {
                    return TokenizerKey.of("ext#" + ((ExtensionAI) ai).getSettings().get("url").getAsString());
                }

                @Override
                public List<TokenizerStrategy> chain(TokenizerKey key, AI ai) {
                    chains.add(key.id());
                    return List.of(new TokenizerStrategy() {
                        @Override
                        public long countTokens(AI ai, String text) {
                            return 4242;
                        }

                        @Override
                        public String getName() {
                            return "PROVIDER";
                        }
                    });
                }
            };
        }
    }
}
