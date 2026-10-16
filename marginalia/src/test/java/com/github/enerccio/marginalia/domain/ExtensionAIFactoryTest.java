package com.github.enerccio.marginalia.domain;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.InferenceException;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.extensions.ExtensionAIFactory;
import com.github.enerccio.marginalia.extensions.ExtensionAIForm;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.Component;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Providers of extensions: the registry in {@code InferenceServices} and the inference service of an
 * {@link ExtensionAI} (created by the factory, cached in the AI, gone with the extension).
 */
class ExtensionAIFactoryTest extends MarginaliaTestBase {

    private String extensionId;
    private TestFactory factory;

    @BeforeEach
    void register() {
        extensionId = uniqueName("ext");
        factory = new TestFactory(extensionId, "Test provider");
        inferenceServices.registerExtensionFactory(factory);
    }

    @AfterEach
    void unregister() {
        inferenceServices.unregisterExtensionFactory(factory);
    }

    private ExtensionAI createExtensionAI() throws Exception {
        login();
        ExtensionAI ai = new ExtensionAI();
        ai.setAiType(AIType.EXTENSION);
        ai.setExtensionId(extensionId);
        ai.setName(uniqueName("ext-ai"));
        ai.setMaxContext(4096);
        ai.setMaxCompletionTokens(256);
        return (ExtensionAI) aiService.save(ai);
    }

    @Test
    void factoryIsFoundById() {
        assertThat(inferenceServices.getExtensionFactory(extensionId)).isSameAs(factory);
        assertThat(inferenceServices.getExtensionFactory("nothing")).isNull();
        assertThat(inferenceServices.getExtensionFactory(null)).isNull();
        assertThat(inferenceServices.getExtensionFactories()).contains(factory);
    }

    @Test
    void factoriesAreOrderedByName() {
        TestFactory first = new TestFactory(uniqueName("ext"), "A first provider");
        TestFactory last = new TestFactory(uniqueName("ext"), "z last provider");
        inferenceServices.registerExtensionFactory(last);
        inferenceServices.registerExtensionFactory(first);
        try {
            List<ExtensionAIFactory> factories = inferenceServices.getExtensionFactories();

            assertThat(factories.indexOf(first)).isLessThan(factories.indexOf(factory));
            assertThat(factories.indexOf(factory)).isLessThan(factories.indexOf(last));
        } finally {
            inferenceServices.unregisterExtensionFactory(first);
            inferenceServices.unregisterExtensionFactory(last);
        }
    }

    @Test
    void secondFactoryWithTheSameIdIsRefused() {
        TestFactory duplicate = new TestFactory(extensionId, "Duplicate");

        assertThatThrownBy(() -> inferenceServices.registerExtensionFactory(duplicate))
                .isInstanceOf(IllegalStateException.class);
        assertThat(inferenceServices.getExtensionFactory(extensionId)).isSameAs(factory);
    }

    @Test
    void blankIdIsRefused() {
        assertThatThrownBy(() -> inferenceServices.registerExtensionFactory(new TestFactory(" ", "No id")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void unregisteringAnotherFactoryKeepsTheRegistered() {
        inferenceServices.unregisterExtensionFactory(new TestFactory(extensionId, "Impostor"));

        assertThat(inferenceServices.getExtensionFactory(extensionId)).isSameAs(factory);
    }

    @Test
    void serviceComesFromTheFactoryAndIsCached() throws Exception {
        ExtensionAI ai = createExtensionAI();

        InferenceService service = inferenceServices.forAI(ai);

        assertThat(service).isInstanceOf(TestService.class);
        assertThat(((TestService) service).ai).isSameAs(ai);
        assertThat(inferenceServices.forAI(ai)).isSameAs(service);
        assertThat(factory.created.get()).isEqualTo(1);
    }

    @Test
    void changingTheTypeDropsTheCachedService() throws Exception {
        ExtensionAI ai = createExtensionAI();
        InferenceService service = inferenceServices.forAI(ai);

        ai.setAiType(AIType.EXTENSION);

        assertThat(ai.getInferenceService()).isNull();
        assertThat(inferenceServices.forAI(ai)).isNotSameAs(service);
    }

    @Test
    void unloadedExtensionHasNoService() throws Exception {
        ExtensionAI ai = createExtensionAI();
        assertThat(inferenceServices.forAI(ai)).isNotNull();

        inferenceServices.unregisterExtensionFactory(factory);

        assertThat(inferenceServices.forAI(ai)).isNull();
        assertThat(ai.getInferenceService()).isNull();
    }

    @Test
    void unloadedExtensionIsReportedToTheUser() throws Exception {
        ExtensionAI ai = createExtensionAI();
        inferenceServices.unregisterExtensionFactory(factory);

        assertThatThrownBy(() -> inferenceServices.forAIOrThrow(ai))
                .isInstanceOfSatisfying(InferenceException.class,
                        e -> assertThat(e.getType()).isEqualTo(InferenceException.Type.EXTENSION_UNAVAILABLE));
    }

    @Test
    void noAiIsNotAnError() throws Exception {
        assertThat(inferenceServices.forAI(null)).isNull();
        assertThat(inferenceServices.forAIOrThrow(null)).isNull();
    }

    @Test
    void reloadedExtensionGetsANewService() throws Exception {
        ExtensionAI ai = createExtensionAI();
        InferenceService before = inferenceServices.forAI(ai);

        inferenceServices.unregisterExtensionFactory(factory);
        TestFactory reloaded = new TestFactory(extensionId, "Reloaded");
        inferenceServices.registerExtensionFactory(reloaded);
        factory = reloaded;

        InferenceService after = inferenceServices.forAI(ai);

        assertThat(after).isNotNull().isNotSameAs(before);
        assertThat(reloaded.created.get()).isEqualTo(1);
    }

    @Test
    void serviceStaysEvenIfTheExtensionWasReloadedBetweenCalls() throws Exception {
        ExtensionAI ai = createExtensionAI();
        InferenceService before = inferenceServices.forAI(ai);

        // unloaded and loaded again without anybody asking for the service in between
        inferenceServices.unregisterExtensionFactory(factory);
        TestFactory reloaded = new TestFactory(extensionId, "Reloaded");
        inferenceServices.registerExtensionFactory(reloaded);
        factory = reloaded;

        assertThat(inferenceServices.forAI(ai)).isNotSameAs(before);
    }

    @Test
    void factoryWithoutServiceIsReported() throws Exception {
        ExtensionAI ai = createExtensionAI();
        factory.returnNothing = true;

        assertThatThrownBy(() -> inferenceServices.forAI(ai)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secretsAreEncryptedAndRevealed() {
        String secret = inferenceServices.protectSecret("api-key");

        assertThat(secret).isNotEqualTo("api-key").startsWith("enc:");
        assertThat(inferenceServices.revealSecret(secret)).isEqualTo("api-key");
        assertThat(inferenceServices.protectSecret(null)).isNull();
        assertThat(inferenceServices.protectSecret("")).isEmpty();
        assertThat(inferenceServices.revealSecret("plain")).isEqualTo("plain");
    }

    private static class TestFactory implements ExtensionAIFactory {
        private final String id;
        private final String name;
        final AtomicInteger created = new AtomicInteger();
        boolean returnNothing;

        TestFactory(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String getExtensionId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public InferenceService createInferenceService(ExtensionAI ai) {
            created.incrementAndGet();
            return returnNothing ? null : new TestService(ai);
        }

        @Override
        public ExtensionAIForm createForm(ExtensionAI ai) {
            return new ExtensionAIForm() {
                @Override
                public Component getComponent() {
                    return null;
                }

                @Override
                public boolean validate() {
                    return true;
                }

                @Override
                public void apply(ExtensionAI target) {
                }
            };
        }
    }

    private static class TestService implements InferenceService {
        final AI ai;

        TestService(AI ai) {
            this.ai = ai;
        }

        @Override
        public List<String> getModels() {
            return List.of("model");
        }

        @Override
        public long countTokens(String text) {
            return text.length();
        }

        @Override
        public long countTokensApprox(String text) {
            return text.length();
        }

        @Override
        public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, InferenceAsyncCallback callback) {
            throw new UnsupportedOperationException();
        }
    }
}
