package com.github.enerccio.marginalia.statistics;

import com.github.enerccio.marginalia.domain.collections.ActivityKind;
import com.github.enerccio.marginalia.domain.collections.StatisticSide;
import com.github.enerccio.marginalia.domain.components.ActivityRow;
import com.github.enerccio.marginalia.domain.components.StatisticFilter;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ActivityIntervalService;
import com.github.enerccio.marginalia.domain.service.ActivityTracker;
import com.github.enerccio.marginalia.domain.service.ManuscriptStatisticService;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The time a model call is on the wire is the generation time of the book it was made for, from the moment the request is sent to
 * the end of the answer, whichever way it ends.
 */
class GenerationTimeTest extends GenerationTestBase {

    @Autowired
    private ActivityTracker tracker;

    @Autowired
    private ActivityIntervalService intervalService;

    @Autowired
    private ManuscriptStatisticService statisticService;

    private Manuscript manuscript;

    @BeforeEach
    void createBook() throws Exception {
        manuscript = manuscriptService.save(newManuscript());
    }

    private List<ActivityRow> rows(Manuscript of) throws Exception {
        // the answer ends a moment after the callback of the caller, and the tracker writes a moment after that
        Thread.sleep(300);
        tracker.flush();
        StatisticFilter filter = StatisticFilter.of(statisticService.today().minusDays(1), statisticService.today().plusDays(1),
                StatisticSide.ALL).forBook(of.getUuid());
        return intervalService.findRows(filter, List.of(ActivityKind.GENERATE));
    }

    private void generate(MockLLMResponse response) throws Exception {
        llm.enqueue(response);
        GenerationRun run = new GenerationRun();
        storyGenerationService.generateNextTurn(manuscriptService.find(manuscript.getId()), new TurnInput(null, null, null, "go"),
                GenerationRequest.newMessage(), run);
        run.await(TIMEOUT);
    }

    @Test
    void aGenerationIsAStretchOfTheBookThatLastsAsLongAsTheAnswer() throws Exception {
        generate(MockLLMResponse.chunks("a ", "b ", "c ", "d ").chunkDelay(Duration.ofMillis(150)).usage(10, 4));

        List<ActivityRow> rows = rows(manuscript);

        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().agent()).isFalse();
        assertThat(rows.getFirst().millis()).isBetween(400L, 5000L);
    }

    @Test
    void aGenerationThatWasStoppedCountsUntilTheStop() throws Exception {
        generate(MockLLMResponse.chunks("partial ", "text ", "never sent").chunkDelay(Duration.ofMillis(100)).disconnectAfter(2));

        List<ActivityRow> rows = rows(manuscript);

        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().millis()).isPositive();
    }

    @Test
    void theOtherBooksHaveNoGenerationTime() throws Exception {
        Manuscript other = manuscriptService.save(newManuscript());
        generate(MockLLMResponse.text("The rain fell").usage(10, 4));

        assertThat(rows(manuscript)).hasSize(1);
        assertThat(rows(other)).isEmpty();
    }
}
