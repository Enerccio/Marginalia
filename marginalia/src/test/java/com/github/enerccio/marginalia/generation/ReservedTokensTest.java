package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.TokenLimits;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationProperties;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tokens reserved by extensions in {@code PrePromptData.reservedTokens} are taken from the room for the story.
 */
class ReservedTokensTest extends GenerationTestBase {

    @Autowired
    private Localization loc;

    private Manuscript manuscriptWithTwoParts() throws Exception {
        Manuscript manuscript = manuscriptService.save(newManuscript());
        llm.enqueue(MockLLMResponse.text("FIRST"));
        assertThat(generate(manuscript, "one").await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        llm.enqueue(MockLLMResponse.text("SECOND"));
        assertThat(generate(manuscript, "two").await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        return manuscript;
    }

    private void reserve(Manuscript manuscript, long tokens) {
        onEvent(Events.BEFORE_PREPARE_CONTENT, manuscript,
                e -> e.getPrePromptData().setReservedTokens(e.getPrePromptData().getReservedTokens() + tokens));
    }

    @SuppressWarnings("unchecked")
    private AtomicReference<List<String>> captureChronicle(Manuscript manuscript) {
        AtomicReference<List<String>> chronicle = new AtomicReference<>();
        onEvent(Events.AFTER_MANUSCRIPT_CONCATENATION, manuscript,
                e -> chronicle.set(List.copyOf((List<String>) e.getProperty(GenerationProperties.MANUSCRIPT_CHRONICLE))));
        return chronicle;
    }

    @Test
    void storyFitsWithoutReservation() throws Exception {
        Manuscript manuscript = manuscriptWithTwoParts();
        AtomicReference<List<String>> chronicle = captureChronicle(manuscript);

        assertThat(generate(manuscript, "three").await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);

        assertThat(chronicle.get()).containsExactly("FIRST", "SECOND");
    }

    @Test
    void reservationTakesRoomFromStory() throws Exception {
        Manuscript manuscript = manuscriptWithTwoParts();
        int limit = TokenLimits.promptTokens(ai, protocol);
        // the rest of the prompt (an empty template and the instructions) fits, no story part does
        reserve(manuscript, limit - 300);
        AtomicReference<List<String>> chronicle = captureChronicle(manuscript);

        GenerationRun run = generate(manuscript, "three");

        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(run.getSimpleErrors()).isEmpty();
        assertThat(chronicle.get()).isEmpty();
    }

    @Test
    void reservationOfWholeContextFailsGeneration() throws Exception {
        Manuscript manuscript = manuscriptService.save(newManuscript());
        reserve(manuscript, TokenLimits.promptTokens(ai, protocol));

        GenerationRun run = generate(manuscript, "write");

        assertThat(run.getSimpleErrors()).containsExactly(loc.getValue(L.ERROR_CONTEXT_INSUFFICIENT));
        assertThat(llm.getCompletionRequests()).isEmpty();
    }

    @Test
    void reservationsOfListenersAddUp() throws Exception {
        Manuscript manuscript = manuscriptService.save(newManuscript());
        long half = TokenLimits.promptTokens(ai, protocol) / 2 + 1;
        reserve(manuscript, half);
        reserve(manuscript, half);

        GenerationRun run = generate(manuscript, "write");

        assertThat(run.getSimpleErrors()).containsExactly(loc.getValue(L.ERROR_CONTEXT_INSUFFICIENT));
    }
}
