package com.github.enerccio.marginalia.inference;

import com.github.enerccio.marginalia.domain.model.impl.OpenAICompatible;
import com.github.enerccio.marginalia.domain.service.InferenceListener;
import com.github.enerccio.marginalia.domain.service.InferenceListener.Outcome;
import com.github.enerccio.marginalia.domain.service.InferenceListener.Request;
import com.github.enerccio.marginalia.domain.service.InferenceListener.Response;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceResultCallback;
import com.github.enerccio.marginalia.domain.service.InferenceSession;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.structured.SchemaCheck;
import com.github.enerccio.marginalia.test.InferenceCollector;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link InferenceSession#addListener}: a listener sees what is sent and what comes back (the reasoning too), with the kind
 * of work, whichever call started the request.
 */
class InferenceListenerTest extends MarginaliaTestBase {

    private static final List<LLMChatMessage> PAYLOAD = List.of(
            LLMChatMessage.of(LLMRole.SYSTEM, "Be brief."), LLMChatMessage.of(LLMRole.USER, "Hello."));

    /** Remembers the requests and the end of each one. */
    private static class Recorder implements InferenceListener {
        final List<Request> requests = new ArrayList<>();
        final List<CompletableFuture<Response>> responses = new ArrayList<>();
        final List<String> chunks = Collections.synchronizedList(new ArrayList<>());

        @Override
        public synchronized Observation onRequest(Request request) {
            CompletableFuture<Response> end = new CompletableFuture<>();
            requests.add(request);
            responses.add(end);
            return new Observation() {
                @Override
                public void onChunk(InferenceService.ChunkType type, String text) {
                    chunks.add(type + ":" + text);
                }

                @Override
                public void onFinish(Response response) {
                    end.complete(response);
                }
            };
        }

        Response response(int index) throws Exception {
            return responses.get(index).get(10, TimeUnit.SECONDS);
        }
    }

    private Recorder recorder;

    private Recorder install() {
        recorder = new Recorder();
        InferenceSession.addListener(recorder);
        return recorder;
    }

    @AfterEach
    void removeListener() {
        if (recorder != null) {
            InferenceSession.removeListener(recorder);
        }
    }

    @Test
    void streamIsReportedWithRequestKindModelReasoningAndAnswer() throws Exception {
        login();
        OpenAICompatible ai = createAI();
        Recorder seen = install();
        llm.enqueue(MockLLMResponse.chunks("Hi ", "there").withReasoning("thinking..."));

        InferenceCollector collector = new InferenceCollector();
        try (InferenceSession _ = InferenceSession.open(InferenceSession.ANNOTATE)) {
            inferenceServices.forAI(ai).stream(PAYLOAD, null, collector);
        }
        collector.await(Duration.ofSeconds(10));

        Request request = seen.requests.get(0);
        assertThat(request.kind()).isEqualTo(InferenceSession.ANNOTATE);
        assertThat(request.aiName()).isEqualTo(ai.getName());
        assertThat(request.model()).isEqualTo(ai.getModel());
        assertThat(request.messages()).extracting(InferenceListener.Message::role).containsExactly("SYSTEM", "USER");
        assertThat(request.messages().get(1).content()).isEqualTo("Hello.");

        Response response = seen.response(0);
        assertThat(response.outcome()).isEqualTo(Outcome.COMPLETED);
        assertThat(response.text()).isEqualTo("Hi there");
        assertThat(response.reasoning()).isEqualTo("thinking...");
        assertThat(response.error()).isNull();
        // the pieces arrived before the end, reasoning first
        assertThat(seen.chunks).startsWith("REASONING:thinking...").contains("RESPONSE:Hi ", "RESPONSE:there");
    }

    @Test
    void requestOutsideOfAScopeHasNoKind() throws Exception {
        login();
        Recorder seen = install();
        llm.enqueue(MockLLMResponse.text("ok"));

        InferenceCollector collector = new InferenceCollector();
        inferenceServices.forAI(createAI()).stream(PAYLOAD, null, collector);
        collector.await(Duration.ofSeconds(10));

        assertThat(seen.requests.get(0).kind()).isNull();
        assertThat(seen.response(0).outcome()).isEqualTo(Outcome.COMPLETED);
    }

    @Test
    void inferIsReported() throws Exception {
        login();
        Recorder seen = install();
        llm.enqueue(MockLLMResponse.text("whole answer"));

        CompletableFuture<String> done = new CompletableFuture<>();
        try (InferenceSession _ = InferenceSession.open("reviewer")) {
            inferenceServices.forAI(createAI()).infer(PAYLOAD, new InferenceResultCallback() {
                public void onResult(String text, SchemaCheck check) {
                    done.complete(text);
                }

                public void onCancel() {
                    done.cancel(true);
                }

                public void onError(Throwable exception) {
                    done.completeExceptionally(exception);
                }
            });
        }

        assertThat(done.get(10, TimeUnit.SECONDS)).isEqualTo("whole answer");
        assertThat(seen.requests.get(0).kind()).isEqualTo("reviewer");
        assertThat(seen.response(0).text()).isEqualTo("whole answer");
    }

    @Test
    void failedRequestIsReportedAsFailed() throws Exception {
        login();
        Recorder seen = install();
        llm.enqueue(MockLLMResponse.error(500, "the server is on fire"));

        InferenceCollector collector = new InferenceCollector();
        inferenceServices.forAI(createAI()).stream(PAYLOAD, null, collector);
        collector.await(Duration.ofSeconds(10));

        Response response = seen.response(0);
        assertThat(response.outcome()).isEqualTo(Outcome.FAILED);
        assertThat(response.error()).isNotBlank();
    }

    @Test
    void failingListenerDoesNotBreakTheRequest() throws Exception {
        login();
        InferenceListener broken = request -> {
            throw new IllegalStateException("broken listener");
        };
        InferenceSession.addListener(broken);
        try {
            llm.enqueue(MockLLMResponse.text("fine"));
            InferenceCollector collector = new InferenceCollector();
            inferenceServices.forAI(createAI()).stream(PAYLOAD, null, collector);

            assertThat(collector.await(Duration.ofSeconds(10))).isEqualTo(InferenceCollector.Outcome.COMPLETED);
            assertThat(collector.getResponse()).isEqualTo("fine");
        } finally {
            InferenceSession.removeListener(broken);
        }
    }

    @Test
    void removedListenerSeesNothingAndServiceIsNotWrapped() throws Exception {
        login();
        OpenAICompatible ai = createAI();
        Recorder seen = install();
        InferenceService wrapped = inferenceServices.forAI(ai);
        InferenceSession.removeListener(seen);
        InferenceService plain = inferenceServices.forAI(ai);

        assertThat(plain).isNotSameAs(wrapped);
        assertThat(inferenceServices.forAI(ai)).isSameAs(plain);

        llm.enqueue(MockLLMResponse.text("ok"));
        InferenceCollector collector = new InferenceCollector();
        plain.stream(PAYLOAD, null, collector);
        collector.await(Duration.ofSeconds(10));
        assertThat(seen.requests).isEmpty();
    }
}
