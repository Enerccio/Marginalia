package com.github.enerccio.marginalia.inference;

import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncCallback;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncController;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A stop ends a streamed request at once: while the server is still reading the prompt (it sends nothing, not even the headers) and
 * while it is silent between two chunks. It must not wait for the next chunk to notice the stop.
 */
class StreamStopTest extends MarginaliaTestBase {

    private static final List<LLMChatMessage> PAYLOAD = List.of(LLMChatMessage.of(LLMRole.USER, "Write."));

    private static class Watch implements InferenceAsyncCallback {
        final CompletableFuture<String> ended = new CompletableFuture<>();
        final AtomicInteger chunks = new AtomicInteger();
        final CountDownLatch firstChunk = new CountDownLatch(1);

        public void onChunk(InferenceAsyncController controller, InferenceService.ChunkType type, String text) {
            chunks.incrementAndGet();
            firstChunk.countDown();
            controller.continueInference();
        }

        public void onCompletion() {
            ended.complete("completion");
        }

        public void onCancel() {
            ended.complete("cancel");
        }

        public void onError(Throwable exception) {
            ended.complete("error: " + exception);
        }

        public boolean isDead() {
            return false;
        }
    }

    @Test
    void aStopDoesNotWaitForTheServerToAnswer() throws Exception {
        login();
        CountDownLatch prompt = new CountDownLatch(1);
        // the server holds everything, the headers too, as a model does while it reads a long prompt
        llm.enqueue(MockLLMResponse.text("never read").waitFor(prompt, Duration.ofSeconds(30)));
        Watch watch = new Watch();

        try {
            CancellationToken token = inferenceServices.forAI(createAI()).stream(PAYLOAD, null, watch);
            Thread.sleep(300);
            assertThat(watch.ended).isNotDone();

            token.cancel();

            assertThat(watch.ended.get(5, TimeUnit.SECONDS)).isEqualTo("cancel");
            assertThat(watch.chunks.get()).isZero();
            // the interrupt that ends the wait must not make the client send the prompt again (it retries failed calls)
            Thread.sleep(1500);
            assertThat(llm.getCompletionRequests()).hasSize(1);
        } finally {
            prompt.countDown();
        }
    }

    @Test
    void aStopDoesNotWaitForTheNextChunk() throws Exception {
        login();
        llm.enqueue(MockLLMResponse.chunks("one ", "two ", "three ").chunkDelay(Duration.ofSeconds(8)));
        Watch watch = new Watch();

        CancellationToken token = inferenceServices.forAI(createAI()).stream(PAYLOAD, null, watch);
        assertThat(watch.firstChunk.await(5, TimeUnit.SECONDS)).isTrue();
        long stopped = System.nanoTime();

        token.cancel();

        assertThat(watch.ended.get(4, TimeUnit.SECONDS)).isEqualTo("cancel");
        assertThat(Duration.ofNanos(System.nanoTime() - stopped)).isLessThan(Duration.ofSeconds(4));
        assertThat(watch.chunks.get()).isEqualTo(1);
    }
}
