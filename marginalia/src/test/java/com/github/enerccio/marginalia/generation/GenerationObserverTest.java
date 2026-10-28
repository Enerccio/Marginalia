package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationControllerEvent.Registration;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationProperties;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Observers ({@code StoryGenerationService.addEventObserver}): read-only, synchronous, called before the listeners of
 * the event and unable to break or change the generation.
 */
class GenerationObserverTest extends GenerationTestBase {

    private Manuscript book() throws Exception {
        return manuscriptService.save(newManuscript());
    }

    @Test
    void observerSeesEveryChunkBeforeTheListeners() throws Exception {
        Manuscript manuscript = book();
        List<String> order = new CopyOnWriteArrayList<>();
        AtomicReference<String> runId = new AtomicReference<>();
        observe(Events.CHUNK_RECEIVED, manuscript, o -> {
            order.add("observer:" + o.getProperty(GenerationProperties.CHUNK));
            runId.set(o.getRunId());
        });
        onEvent(Events.CHUNK_RECEIVED, manuscript, e -> order.add("listener:" + e.getProperty(GenerationProperties.CHUNK)));
        llm.enqueue(MockLLMResponse.chunks("one ", "two ", "three"));

        GenerationRun run = generate(manuscript, "write");

        assertThat(run.getResponse()).isEqualTo("one two three");
        assertThat(order).containsExactly("observer:one ", "listener:one ", "observer:two ", "listener:two ",
                "observer:three", "listener:three");
        assertThat(runId.get()).isNotBlank();
    }

    @Test
    void observerSeesTheStateOfTheGeneration() throws Exception {
        Manuscript manuscript = book();
        AtomicReference<String> instructions = new AtomicReference<>();
        AtomicReference<Object> messageId = new AtomicReference<>();
        observe(Events.BEFORE_INFERENCE, manuscript, o -> {
            instructions.set(o.getInput().instructions());
            messageId.set(o.getMessage().getId());
            assertThat(o.getAi()).isNotNull();
            assertThat(o.getProtocol()).isNotNull();
            assertThat(o.getEvent()).isEqualTo(Events.BEFORE_INFERENCE);
        });
        llm.enqueue(MockLLMResponse.text("text"));

        GenerationRun run = generate(manuscript, "the instructions");

        assertThat(instructions.get()).isEqualTo("the instructions");
        assertThat(messageId.get()).isEqualTo(run.getMessage().getId());
    }

    @Test
    void observerCannotChangeTheProperties() throws Exception {
        Manuscript manuscript = book();
        AtomicBoolean refused = new AtomicBoolean();
        observe(Events.CHUNK_RECEIVED, manuscript, o -> {
            try {
                o.getProperties().put(GenerationProperties.CHUNK, "changed");
            } catch (UnsupportedOperationException e) {
                refused.set(true);
            }
        });
        llm.enqueue(MockLLMResponse.text("original"));

        GenerationRun run = generate(manuscript, "write");

        assertThat(refused).isTrue();
        assertThat(run.getResponse()).isEqualTo("original");
    }

    @Test
    void failingObserverDoesNotBreakTheGeneration() throws Exception {
        Manuscript manuscript = book();
        observe(Events.CHUNK_RECEIVED, manuscript, o -> {
            throw new IllegalStateException("observer bug");
        });
        List<String> chunks = new CopyOnWriteArrayList<>();
        observe(Events.CHUNK_RECEIVED, manuscript, o -> chunks.add(o.getProperty(GenerationProperties.CHUNK)));
        llm.enqueue(MockLLMResponse.chunks("a", "b"));

        GenerationRun run = generate(manuscript, "write");

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(run.getResponse()).isEqualTo("ab");
        // an observer that failed does not hide the chunks from the next one
        assertThat(chunks).containsExactly("a", "b");
    }

    @Test
    void unregisteredObserverIsNotCalledAgain() throws Exception {
        Manuscript manuscript = book();
        List<String> chunks = new CopyOnWriteArrayList<>();
        Registration registration = storyGenerationService.addEventObserver(Events.CHUNK_RECEIVED,
                o -> chunks.add(o.getProperty(GenerationProperties.CHUNK)));
        llm.enqueue(MockLLMResponse.text("first"), MockLLMResponse.text("second"));

        generate(manuscript, "one");
        registration.unregister();
        generate(manuscript, "two");

        assertThat(chunks).containsExactly("first");
    }
}
