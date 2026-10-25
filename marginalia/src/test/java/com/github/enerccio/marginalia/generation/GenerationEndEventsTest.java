package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationProperties;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.GenerationFailure;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.GenerationOutcome;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link Events#AFTER_CLEANUP} ends every generation - completed, stopped or failed - and tells how it ended
 * ({@link GenerationProperties#OUTCOME}, {@link GenerationProperties#FAILURE}), and {@link GenerationProperties#RUN_ID}
 * names the generation in every event.
 */
class GenerationEndEventsTest extends GenerationTestBase {

    /**
     * What a listener of {@link Events#AFTER_CLEANUP} saw.
     */
    record Ended(GenerationOutcome outcome, GenerationFailure failure, String runId, ChatMessage message) {
    }

    /**
     * Records the end of the generations of the manuscript; {@code await} waits for the next one.
     */
    private static class EndWatcher {
        final List<Ended> ended = new CopyOnWriteArrayList<>();
        final CountDownLatch latch = new CountDownLatch(1);

        Ended await() throws Exception {
            assertThat(latch.await(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)).as("AFTER_CLEANUP reached").isTrue();
            return ended.getLast();
        }
    }

    private EndWatcher watchEnd(Manuscript manuscript) {
        EndWatcher watcher = new EndWatcher();
        onEvent(Events.AFTER_CLEANUP, manuscript, e -> {
            watcher.ended.add(new Ended(e.getProperty(GenerationProperties.OUTCOME),
                    e.getProperty(GenerationProperties.FAILURE), e.getProperty(GenerationProperties.RUN_ID),
                    e.getMessage()));
            watcher.latch.countDown();
        });
        return watcher;
    }

    private Manuscript book() throws Exception {
        return manuscriptService.save(newManuscript());
    }

    @Test
    void completedGenerationEndsWithTheOutcome() throws Exception {
        Manuscript manuscript = book();
        EndWatcher watcher = watchEnd(manuscript);
        AtomicReference<String> runIdAtStart = new AtomicReference<>();
        onEvent(Events.BEFORE_PREPARE_GENERATION, manuscript,
                e -> runIdAtStart.set(e.getProperty(GenerationProperties.RUN_ID)));
        llm.enqueue(MockLLMResponse.text("The end."));

        GenerationRun run = generate(manuscript, "write");
        Ended ended = watcher.await();

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(watcher.ended).hasSize(1);
        assertThat(ended.outcome()).isEqualTo(GenerationOutcome.COMPLETED);
        assertThat(ended.failure()).isNull();
        assertThat(ended.message().getResponse()).isEqualTo("The end.");
        assertThat(ended.runId()).isNotBlank().isEqualTo(runIdAtStart.get());
    }

    @Test
    void runIdIsDifferentForEveryGeneration() throws Exception {
        Manuscript manuscript = book();
        List<String> ids = new CopyOnWriteArrayList<>();
        onEvent(Events.AFTER_PREPARE_PAYLOAD, manuscript, e -> ids.add(e.getProperty(GenerationProperties.RUN_ID)));
        llm.enqueue(MockLLMResponse.text("one"), MockLLMResponse.text("two"));

        generate(manuscript, "first");
        generate(manuscript, "second");

        assertThat(ids).hasSize(2).doesNotHaveDuplicates();
    }

    @Test
    void failedGenerationEndsWithTheFailure() throws Exception {
        Manuscript manuscript = book();
        EndWatcher watcher = watchEnd(manuscript);
        llm.enqueue(MockLLMResponse.error(401, "wrong key"));

        GenerationRun run = generate(manuscript, "write");
        Ended ended = watcher.await();

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.CANCELLED);
        assertThat(ended.outcome()).isEqualTo(GenerationOutcome.FAILED);
        assertThat(ended.failure()).isNotNull();
        assertThat(ended.failure().message()).isNotBlank();
        // no text arrived: the new part was removed again
        assertThat(ended.message()).isNull();
    }

    @Test
    void stoppedGenerationEndsCancelledAndListenersStillRun() throws Exception {
        Manuscript manuscript = book();
        EndWatcher watcher = watchEnd(manuscript);
        List<GenerationOutcome> beforeCleanup = new CopyOnWriteArrayList<>();
        onEvent(Events.BEFORE_CLEANUP, manuscript, e -> beforeCleanup.add(e.getProperty(GenerationProperties.OUTCOME)));
        // the user stops it when the first text arrives
        onEvent(Events.CHUNK_RECEIVED, manuscript, e -> e.getCancellationToken().cancel());
        llm.enqueue(MockLLMResponse.chunks("one ", "two ", "three ", "four ").chunkDelay(Duration.ofMillis(50)));

        GenerationRun run = generate(manuscript, "write");
        Ended ended = watcher.await();

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.CANCELLED);
        assertThat(ended.outcome()).isEqualTo(GenerationOutcome.CANCELLED);
        assertThat(ended.failure()).isNull();
        assertThat(ended.message()).as("the text that arrived is kept").isNotNull();
        assertThat(watcher.ended).hasSize(1);
        // listeners of the other events do not run once the generation is stopped, this one does
        assertThat(beforeCleanup).isEmpty();
    }

    @Test
    void stoppedBeforeAnyTextRemovesThePartAndEndsCancelled() throws Exception {
        Manuscript manuscript = book();
        EndWatcher watcher = watchEnd(manuscript);
        onEvent(Events.BEFORE_INFERENCE, manuscript, e -> e.getCancellationToken().cancel());
        llm.enqueue(MockLLMResponse.text("never read"));

        generate(manuscript, new TurnInput(null, null, null, "write"));
        Ended ended = watcher.await();

        assertThat(ended.outcome()).isEqualTo(GenerationOutcome.CANCELLED);
        assertThat(ended.message()).isNull();
    }
}
