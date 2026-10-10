package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.InferenceException.Type;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.InferenceCollector;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Timeouts and retries of the provider, readable errors and the connection test.
 */
class InferenceRobustnessTest extends GenerationTestBase {

    @Autowired
    private Localization loc;

    @Autowired
    private InferenceServices inferenceServices;

    private Manuscript manuscript() throws Exception {
        return manuscriptService.save(newManuscript());
    }

    private void configure(Integer timeoutSeconds, Integer retries) throws Exception {
        ai.setRequestTimeoutSeconds(timeoutSeconds);
        ai.setMaxRetries(retries);
        ai = (OpenAICompatible) aiService.save(ai);
    }

    private InferenceException failure(Runnable action) {
        return (InferenceException) catchThrowable(action);
    }

    private static Throwable catchThrowable(Runnable action) {
        try {
            action.run();
        } catch (Throwable t) {
            return t;
        }
        throw new AssertionError("expected a failure");
    }

    // --- error mapping ---

    @Test
    void wrongKeyIsExplained() throws Exception {
        Manuscript manuscript = manuscript();
        llm.enqueue(MockLLMResponse.error(401, "Incorrect API key"));

        GenerationRun run = generate(manuscript, "write");

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.CANCELLED);
        assertThat(run.getErrors()).isEmpty();
        assertThat(run.getSimpleErrors()).hasSize(1);
        assertThat(run.getSimpleErrors().getFirst())
                .startsWith(loc.getValue(L.ERROR_INFERENCE_AUTHENTICATION))
                .contains("Incorrect API key");
    }

    @Test
    void contextOverflowIsExplained() throws Exception {
        Manuscript manuscript = manuscript();
        llm.enqueue(MockLLMResponse.error(400, "This model's maximum context length is 8192 tokens"));

        GenerationRun run = generate(manuscript, "write");

        assertThat(run.getSimpleErrors()).hasSize(1);
        assertThat(run.getSimpleErrors().getFirst()).startsWith(loc.getValue(L.ERROR_INFERENCE_CONTEXT_OVERFLOW));
    }

    @Test
    void statusesAreClassified() throws Exception {
        configure(null, 0);
        InferenceService service = inferenceServices.forAI(ai);

        record Case(int status, String message, Type type) {}
        for (Case c : List.of(
                new Case(403, "no access", Type.PERMISSION),
                new Case(404, "The model `gpt-x` does not exist", Type.MODEL_NOT_FOUND),
                new Case(404, "Not Found", Type.NOT_FOUND),
                new Case(429, "slow down", Type.RATE_LIMIT),
                new Case(500, "boom", Type.SERVER),
                new Case(400, "context too long", Type.CONTEXT_OVERFLOW),
                new Case(400, "unknown parameter foo", Type.BAD_REQUEST))) {
            llm.enqueue(MockLLMResponse.error(c.status(), c.message()));
            InferenceException e = failure(service::testConnection);
            assertThat(e.getType()).as(c.status() + " " + c.message()).isEqualTo(c.type());
            assertThat(e.getStatusCode()).isEqualTo(c.status());
            assertThat(e.getProviderMessage()).isEqualTo(c.message());
        }
    }

    @Test
    void notAnInferenceFailureHasNoDescription() {
        assertThat(InferenceErrors.describe(loc, new IllegalStateException("bug"))).isNull();
    }

    @Test
    void unreachableServerIsAConnectionError() throws Exception {
        ai.setUri("http://127.0.0.1:1/v1");
        configure(5, 0);

        InferenceService service = inferenceServices.forAI(ai);
        InferenceException e = failure(service::getModels);

        assertThat(e.getType()).isEqualTo(Type.CONNECTION);
    }

    // --- retries and timeouts ---

    @Test
    void rateLimitIsRetried() throws Exception {
        Manuscript manuscript = manuscript();
        configure(null, 2);
        llm.enqueue(MockLLMResponse.error(429, "slow down"), MockLLMResponse.error(503, "busy"), MockLLMResponse.text("finally"));

        GenerationRun run = generate(manuscript, "write");

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(run.getResponse()).isEqualTo("finally");
        assertThat(llm.getCompletionRequests()).hasSize(3);
    }

    @Test
    void noRetriesWhenDisabled() throws Exception {
        Manuscript manuscript = manuscript();
        configure(null, 0);
        llm.enqueue(MockLLMResponse.error(429, "slow down"), MockLLMResponse.text("never"));

        GenerationRun run = generate(manuscript, "write");

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.CANCELLED);
        assertThat(run.getSimpleErrors().getFirst()).startsWith(loc.getValue(L.ERROR_INFERENCE_RATE_LIMIT));
        assertThat(llm.getCompletionRequests()).hasSize(1);
    }

    @Test
    void retriesAreLimited() throws Exception {
        Manuscript manuscript = manuscript();
        configure(null, 1);
        llm.fallback(MockLLMResponse.error(500, "down"));

        GenerationRun run = generate(manuscript, "write");

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.CANCELLED);
        assertThat(run.getSimpleErrors().getFirst()).startsWith(loc.getValue(L.ERROR_INFERENCE_SERVER));
        assertThat(llm.getCompletionRequests()).hasSize(2);
    }

    @Test
    void silentProviderTimesOut() throws Exception {
        Manuscript manuscript = manuscript();
        configure(1, 0);
        CountDownLatch never = new CountDownLatch(1);
        llm.enqueue(MockLLMResponse.text("late").waitFor(never, Duration.ofSeconds(10)));

        long start = System.currentTimeMillis();
        GenerationRun run = generate(manuscript, "write");
        never.countDown();

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.CANCELLED);
        assertThat(System.currentTimeMillis() - start).isLessThan(9000);
        assertThat(run.getSimpleErrors()).hasSize(1);
        assertThat(run.getSimpleErrors().getFirst()).startsWith(loc.getValue(L.ERROR_INFERENCE_TIMEOUT));
    }

    @Test
    void failureInCompletionCallbackIsReportedAsError() throws Exception {
        llm.enqueue(MockLLMResponse.text("done"));
        InferenceCollector collector = new InferenceCollector() {
            @Override
            public void onCompletion() {
                throw new IllegalStateException("completion failed");
            }
        };

        inferenceServices.forAI(ai).stream(List.of(LLMChatMessage.of(LLMRole.USER, "write")), collector);

        assertThat(collector.await(Duration.ofSeconds(10))).isEqualTo(InferenceCollector.Outcome.ERROR);
        assertThat(collector.getError()).hasMessageContaining("completion failed");
    }

    // --- connection test ---

    @Test
    void connectionTestListsModelsAndAsksForOneToken() throws Exception {
        llm.models("alpha", "beta");
        llm.enqueue(MockLLMResponse.text("OK"));

        List<String> models = inferenceServices.forAI(ai).testConnection();

        assertThat(models).containsExactly("alpha", "beta");
        assertThat(llm.getCompletionRequests()).hasSize(1);
        assertThat(llm.lastCompletionRequest().isStream()).isFalse();
        assertThat(llm.lastCompletionRequest().getBody().get("max_completion_tokens").getAsInt()).isEqualTo(1);
        assertThat(llm.lastCompletionRequest().getModel()).isEqualTo(ai.getModel());
    }

    @Test
    void connectionTestFailsOnBadModel() throws Exception {
        llm.enqueue(MockLLMResponse.error(404, "The model `nope` does not exist"));

        assertThatThrownBy(() -> inferenceServices.forAI(ai).testConnection())
                .isInstanceOfSatisfying(InferenceException.class, e -> assertThat(e.getType()).isEqualTo(Type.MODEL_NOT_FOUND));
    }

    @Test
    void connectionTestFailsOnBadKey() throws Exception {
        llm.enqueue(MockLLMResponse.error(401, "bad key"));

        assertThatThrownBy(() -> inferenceServices.forAI(ai).testConnection())
                .isInstanceOfSatisfying(InferenceException.class, e -> assertThat(e.getType()).isEqualTo(Type.AUTHENTICATION));
    }

    // --- context overflow warning ---

    @Test
    void warnsWhenPromptDoesNotFit() throws Exception {
        Manuscript manuscript = manuscript();
        int context = TokenLimits.contextTokens(ai, protocol);
        // an extension adds more than the budget allowed
        onEvent(Events.AFTER_PREPARE_PAYLOAD, manuscript,
                e -> e.getPayload().add(LLMChatMessage.of(LLMRole.USER, "word ".repeat(context))));

        GenerationRun run = generate(manuscript, "write");

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(run.getWarnings()).hasSize(1);
        assertThat(run.getWarnings().getFirst()).contains(String.valueOf(context));
    }

    @Test
    void noWarningWhenPromptFits() throws Exception {
        GenerationRun run = generate(manuscript(), "write");

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(run.getWarnings()).isEmpty();
    }
}
