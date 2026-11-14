package com.github.enerccio.marginalia.inference;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.service.AIGate;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceResultCallback;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import org.springframework.web.context.request.RequestAttributes;
import com.github.enerccio.marginalia.domain.service.structured.SchemaCheck;
import com.github.enerccio.marginalia.domain.traits.LLMFunction;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every request to a model takes a connection of its AI ({@code GatedInferenceService}, design 7 of {@code TODO.CONCURRENCY.md}).
 */
class GatedInferenceServiceTest extends MarginaliaTestBase {

    private static final List<LLMChatMessage> PAYLOAD = List.of(LLMChatMessage.of(LLMRole.USER, "Write."));

    @Autowired
    private AIService aiService;

    @Autowired
    private AIGate gate;

    private AI ai;

    @BeforeEach
    void anAIWithoutTheSetting() throws Exception {
        login();
        ai = createAI();
        assertThat(ai.getMaxConcurrentRequests()).isNull();
    }

    private static class Collect implements InferenceResultCallback {
        final CompletableFuture<String> done = new CompletableFuture<>();
        final List<Integer> waited = new CopyOnWriteArrayList<>();

        @Override
        public void onResult(String text, SchemaCheck check) {
            done.complete(text);
        }

        @Override
        public void onCancel() {
            done.cancel(false);
        }

        @Override
        public void onError(Throwable error) {
            done.completeExceptionally(error);
        }

        @Override
        public void onWaiting(int ahead) {
            waited.add(ahead);
        }

        String await() throws Exception {
            return done.get(10, TimeUnit.SECONDS);
        }
    }

    private InferenceService service() throws Exception {
        return inferenceServices.forAI(ai);
    }

    @Test
    void aSecondRequestWaitsForTheOnlyConnectionAndIsSentWhenTheFirstEnds() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        llm.enqueue(MockLLMResponse.text("first").waitFor(release));
        llm.enqueue(MockLLMResponse.text("second"));
        Collect first = new Collect();
        Collect second = new Collect();

        service().infer(PAYLOAD, first);
        llm.awaitCompletionRequests(1, Duration.ofSeconds(10));
        service().infer(PAYLOAD, second);
        Thread.sleep(300);

        assertThat(llm.getCompletionRequests()).as("the second is not sent").hasSize(1);
        assertThat(second.waited).containsExactly(1);
        assertThat(gate.waiting(ai)).isEqualTo(1);

        release.countDown();
        assertThat(first.await()).isEqualTo("first");
        assertThat(second.await()).isEqualTo("second");
        assertThat(second.waited).as("told once more when it was sent").containsExactly(1, 0);
        assertThat(first.waited).as("it found a free connection").isEmpty();
        assertThat(llm.getCompletionRequests()).hasSize(2);
        assertThat(gate.active(ai)).as("both places were given back").isZero();
    }

    @Test
    void aRequestStoppedWhileItWaitsIsNeverSent() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        llm.enqueue(MockLLMResponse.text("first").waitFor(release));
        Collect first = new Collect();
        Collect second = new Collect();

        service().infer(PAYLOAD, first);
        llm.awaitCompletionRequests(1, Duration.ofSeconds(10));
        CancellationToken token = service().infer(PAYLOAD, second);
        token.cancel();

        assertThat(second.done).isCancelled();
        assertThat(gate.waiting(ai)).isZero();
        release.countDown();
        assertThat(first.await()).isEqualTo("first");
        Thread.sleep(200);
        assertThat(llm.getCompletionRequests()).hasSize(1);
        assertThat(gate.active(ai)).isZero();
    }

    interface Lookup {

        @LLMFunction(description = "Looks something up with the model")
        void lookup(FunctionContinuation<String> done, String name);
    }

    @Test
    void aFunctionTheModelCalledMayAskTheSameModelWithOneConnection() throws Exception {
        // the place of the request is given back while the function runs, or the question inside would wait for itself
        FunctionSet functions = inferenceServices.getFunctionRegistry().bind(Lookup.class, (Lookup) (done, name) -> {
            try {
                service().infer(PAYLOAD, new InferenceResultCallback() {
                    @Override
                    public void onResult(String text, SchemaCheck check) {
                        done.complete(text);
                    }

                    @Override
                    public void onCancel() {
                        done.fail("cancelled");
                    }

                    @Override
                    public void onError(Throwable exception) {
                        done.fail(exception);
                    }
                });
            } catch (Exception e) {
                done.fail(e);
            }
        });
        llm.enqueue(MockLLMResponse.toolCall("lookup", "{\"name\":\"Anna\"}"));
        llm.enqueue(MockLLMResponse.text("Anna is 30"));
        llm.enqueue(MockLLMResponse.text("She is thirty."));
        Collect outer = new Collect();

        service().infer(PAYLOAD, null, null, functions, outer);

        assertThat(outer.await()).isEqualTo("She is thirty.");
        assertThat(llm.getCompletionRequests()).hasSize(3);
        assertThat(llm.lastCompletionRequest().getBody().toString()).contains("Anna is 30");
        assertThat(gate.active(ai)).isZero();
    }

    @Test
    void anAIWithTwoConnectionsAnswersTwoAtOnce() throws Exception {
        AI stored = aiService.find(ai);
        stored.setMaxConcurrentRequests(2);
        ai = aiService.save(stored);
        CountDownLatch release = new CountDownLatch(1);
        llm.enqueue(MockLLMResponse.text("one").waitFor(release));
        llm.enqueue(MockLLMResponse.text("two").waitFor(release));
        Collect first = new Collect();
        Collect second = new Collect();

        service().infer(PAYLOAD, first);
        service().infer(PAYLOAD, second);

        llm.awaitCompletionRequests(2, Duration.ofSeconds(10));
        assertThat(second.waited).isEmpty();
        release.countDown();
        assertThat(first.await()).isIn("one", "two");
        assertThat(second.await()).isIn("one", "two");
    }

    @Test
    void aRequestOfARunTellsItsRunTheWaitAndWhatTheModelWroteAlsoForTheRequestItsCallbackStarts() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        llm.enqueue(MockLLMResponse.text("first").waitFor(release));
        llm.enqueue(MockLLMResponse.text("second"));
        llm.enqueue(MockLLMResponse.text("third"));
        Runs.Run one = Runs.run("test", () -> "One").start();
        Runs.Run two = Runs.run("test", () -> "Two").start();
        Collect first = new Collect();
        Collect third = new Collect();
        // the callback runs on a thread of the model's answer: the services there need the session of the test, as a page gives it
        InferenceService service = service();
        RequestAttributes session = ThreadCopyRequestAttributes.create();
        Collect second = new Collect() {
            @Override
            public void onResult(String text, SchemaCheck check) {
                // the next request of the same run, from the callback of its first
                try (ThreadCopyRequestAttributes.InRequestScope _ = new ThreadCopyRequestAttributes.InRequestScope(session)) {
                    service.infer(PAYLOAD, third);
                } catch (Exception e) {
                    third.done.completeExceptionally(e);
                }
                super.onResult(text, check);
            }
        };
        try {
            try (Runs.Binding _ = one.bind()) {
                service().infer(PAYLOAD, first);
            }
            llm.awaitCompletionRequests(1, Duration.ofSeconds(10));
            try (Runs.Binding _ = two.bind()) {
                service().infer(PAYLOAD, second);
            }
            assertThat(Runs.current()).as("the binding is closed").isNull();
            assertThat(two.getText().getWaiting()).isEqualTo(1);
            assertThat(one.getText().getRequests()).isEqualTo(1);

            release.countDown();
            assertThat(first.await()).isEqualTo("first");
            assertThat(second.await()).isEqualTo("second");
            assertThat(third.await()).isEqualTo("third");
            assertThat(one.getText().getAnswer()).isEqualTo("first");
            assertThat(two.getText().getWaiting()).as("sent").isEqualTo(-1);
            assertThat(two.getText().getRequests()).as("the request of its callback is its too").isEqualTo(2);
            assertThat(two.getText().getAnswer()).isEqualTo("third");
        } finally {
            one.end();
            two.end();
        }
    }
}
