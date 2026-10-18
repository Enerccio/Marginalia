package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.model.impl.ExtensionAI;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.InferenceErrors;
import com.github.enerccio.marginalia.domain.service.InferenceException;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.extensions.ExtensionAIFactory;
import com.github.enerccio.marginalia.extensions.ExtensionAIForm;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import com.vaadin.flow.component.Component;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestAttributes;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A book whose AI belongs to an extension is written by the inference service the extension provides; when the
 * extension is gone the generation says so and sends nothing.
 */
class ExtensionAIGenerationTest extends GenerationTestBase {

    private static final String ANSWER = "written by the extension";

    private ExtensionAI extensionAI;
    private ExtensionAIFactory factory;

    @BeforeEach
    void registerExtension() throws Exception {
        String extensionId = uniqueName("ext");
        factory = new ExtensionAIFactory() {
            @Override
            public String getExtensionId() {
                return extensionId;
            }

            @Override
            public String getName() {
                return "Test extension";
            }

            @Override
            public InferenceService createInferenceService(ExtensionAI ai) {
                return new StreamingService();
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
        };
        inferenceServices.registerExtensionFactory(factory);

        ExtensionAI created = new ExtensionAI();
        created.setAiType(AIType.EXTENSION);
        created.setExtensionId(extensionId);
        created.setName(uniqueName("ext-ai"));
        created.setMaxContext(32000);
        created.setMaxCompletionTokens(1000);
        extensionAI = (ExtensionAI) aiService.save(created);
    }

    @AfterEach
    void unregisterExtension() {
        inferenceServices.unregisterExtensionFactory(factory);
    }

    private Manuscript extensionManuscript() throws Exception {
        Manuscript manuscript = newManuscript();
        manuscript.setAi(extensionAI);
        return manuscriptService.save(manuscript);
    }

    @Test
    void generatesWithTheServiceOfTheExtension() throws Exception {
        GenerationRun run = generate(extensionManuscript(), "write");

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(run.getResponse()).isEqualTo(ANSWER);
        assertThat(run.getErrors()).isEmpty();
        assertThat(llm.getCompletionRequests()).isEmpty();
    }

    @Test
    void generationWithoutTheExtensionSaysSo() throws Exception {
        Manuscript manuscript = extensionManuscript();
        inferenceServices.unregisterExtensionFactory(factory);

        GenerationRun run = generate(manuscript, "write");

        assertThat(run.getErrors()).extracting(InferenceErrors::find).extracting(InferenceException::getType)
                .contains(InferenceException.Type.EXTENSION_UNAVAILABLE);
        assertThat(llm.getCompletionRequests()).isEmpty();
    }

    /**
     * Answers with one chunk, like a provider that streams in one piece: the generation asks for the next with
     * {@code continueInference}, which ends the stream.
     */
    private static class StreamingService implements InferenceService {

        @Override
        public List<String> getModels() {
            return List.of();
        }

        @Override
        public long countTokens(String text) {
            return text == null ? 0 : text.length();
        }

        @Override
        public long countTokensApprox(String text) {
            return countTokens(text);
        }

        @Override
        public CancellationToken stream(List<LLMChatMessage> payload, Protocol protocol, InferenceAsyncCallback callback) {
            RequestAttributes attributes = ThreadCopyRequestAttributes.create();
            InferenceAsyncController controller = new InferenceAsyncController() {
                @Override
                public void continueInference() {
                    run(attributes, callback::onCompletion);
                }

                @Override
                public void terminateInference() {
                }
            };
            run(attributes, () -> callback.onChunk(controller, ChunkType.RESPONSE, ANSWER));
            return new CancellationToken();
        }

        private interface Action {
            void run() throws Exception;
        }

        private static void run(RequestAttributes attributes, Action action) {
            CompletableFuture.runAsync(() -> {
                try (InRequestScope _ = new InRequestScope(attributes)) {
                    action.run();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
