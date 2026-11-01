package com.github.enerccio.marginalia.statistics;

import com.github.enerccio.marginalia.domain.collections.StatisticSide;
import com.github.enerccio.marginalia.domain.components.StatisticCounts;
import com.github.enerccio.marginalia.domain.components.StatisticFilter;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Usage;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The tokens a book cost are counted where a model call is counted for the user and the AI ({@code UsageService#record}), for the
 * book of the {@link UsageContext} of the thread that started the call.
 */
class TokenCountingTest extends GenerationTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptStatisticService statisticService;

    @Autowired
    private UsageService usageService;

    @Autowired
    private RedraftService redraftService;

    private Manuscript manuscript;

    @BeforeEach
    void createBook() throws Exception {
        manuscript = manuscriptService.save(newManuscript());
    }

    private Manuscript book() throws Exception {
        return manuscriptService.find(manuscript.getId());
    }

    private StatisticCounts counts() throws Exception {
        return counts(manuscript);
    }

    private StatisticCounts counts(Manuscript of) throws Exception {
        return statisticService.total(StatisticFilter.of(statisticService.today(), statisticService.today(), StatisticSide.ALL)
                .forBook(of.getUuid()));
    }

    /** The model call is counted when its answer has ended, which may be a moment after the callback of the caller. */
    private StatisticCounts countsWhen(Supplier<Boolean> ready) throws Exception {
        for (int i = 0; i < 100 && !ready.get(); i++) {
            Thread.sleep(50);
        }
        return counts();
    }

    private ChatMessage generate(GenerationRequest request, MockLLMResponse response) throws Exception {
        llm.enqueue(response);
        GenerationRun run = new GenerationRun();
        storyGenerationService.generateNextTurn(book(), new TurnInput(null, null, null, "go"), request, run);
        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        return chatMessageService.find(run.getMessage().getId());
    }

    @Test
    void aGenerationCostsTheBookWhatItCostsTheAi() throws Exception {
        generate(GenerationRequest.newMessage(), MockLLMResponse.text("The rain fell").usage(120, 30));

        StatisticCounts counts = countsWhen(() -> true);
        assertThat(counts.prefillTokens()).isEqualTo(120);
        assertThat(counts.generatedTokens()).isEqualTo(30);
        assertThat(counts.requests()).isEqualTo(1);
    }

    @Test
    void swipesAndRegenerationsAreRequestsToo() throws Exception {
        ChatMessage first = generate(GenerationRequest.newMessage(), MockLLMResponse.text("The rain fell").usage(100, 10));
        generate(GenerationRequest.newSwipe(first), MockLLMResponse.text("The snow fell").usage(110, 11));
        generate(GenerationRequest.regenerate(first), MockLLMResponse.text("The sun rose").usage(120, 12));

        StatisticCounts counts = counts();
        assertThat(counts.prefillTokens()).isEqualTo(330);
        assertThat(counts.generatedTokens()).isEqualTo(33);
        assertThat(counts.requests()).isEqualTo(3);
    }

    @Test
    void aGenerationThatWasStoppedCountsWhatArrived() throws Exception {
        llm.enqueue(MockLLMResponse.chunks("partial ", "text ", "never sent").disconnectAfter(2));
        GenerationRun run = new GenerationRun();
        storyGenerationService.generateNextTurn(book(), new TurnInput(null, null, null, "go"), GenerationRequest.newMessage(), run);
        run.await(TIMEOUT);

        StatisticCounts counts = counts();
        assertThat(counts.requests()).isEqualTo(1);
        assertThat(counts.generatedTokens()).isPositive();
    }

    @Test
    void theBooksAreCountedApart() throws Exception {
        Manuscript other = manuscriptService.save(newManuscript());
        generate(GenerationRequest.newMessage(), MockLLMResponse.text("The rain fell").usage(100, 10));

        assertThat(counts().requests()).isEqualTo(1);
        assertThat(counts(other).requests()).isZero();
    }

    @Test
    void aRedraftIsCountedForTheBook() throws Exception {
        ChatMessage part = chatMessageService.createPart(book(), null, "The rain fell", null);
        llm.enqueue(MockLLMResponse.text("The rain fell hard").usage(70, 7));
        CompletableFuture<Boolean> done = new CompletableFuture<>();
        redraftService.redraft(book(), part, "harder", new RedraftService.Callback() {
            @Override
            public void onProgress(String reasoning, String text) {
            }

            @Override
            public void onFinished(String reasoning, String text) {
                done.complete(true);
            }

            @Override
            public void onTerminated(String reasoning, String text) {
                done.complete(false);
            }

            @Override
            public void onError(Throwable throwable) {
                done.completeExceptionally(throwable);
            }
        });
        done.get(30, TimeUnit.SECONDS);

        StatisticCounts counts = countsWhen(() -> {
            try {
                return counts().requests() > 0;
            } catch (Exception e) {
                return false;
            }
        });
        assertThat(counts.prefillTokens()).isEqualTo(70);
        assertThat(counts.generatedTokens()).isEqualTo(7);
    }

    @Test
    void aCallWithoutABookCountsForTheAiOnly() throws Exception {
        AI ai = createAI();

        usageService.record(ai, 40, 4);

        assertThat(counts()).isEqualTo(StatisticCounts.ZERO);
        Usage usage = usageService.findForUser(ai);
        assertThat(usage.getPrefillTokens()).isEqualTo(40);
    }

    @Test
    void theContextOfTheCallerIsTheBookOfTheCount() throws Exception {
        AI ai = createAI();

        try (UsageContext _ = UsageContext.open(book())) {
            assertThat(UsageContext.current()).isEqualTo(manuscript.getId());
            usageService.record(ai, 40, 4);
            try (UsageContext _ = UsageContext.open((Long) null)) {
                assertThat(UsageContext.current()).isNull();
                usageService.record(ai, 1000, 100);
            }
            assertThat(UsageContext.current()).isEqualTo(manuscript.getId());
        }
        assertThat(UsageContext.current()).isNull();

        assertThat(counts()).isEqualTo(StatisticCounts.tokens(40, 4));
        assertThat(usageService.findForUser(ai).getPrefillTokens()).isEqualTo(1040);
    }

    @Test
    void aCallOnAnotherThreadCountsForTheBookItWasStartedFor() throws Exception {
        AI ai = createAI();
        Long id;
        try (UsageContext _ = UsageContext.open(book())) {
            id = UsageContext.current();
        }
        // what an inference service does for the threads of a request: the book was read where the request started
        Thread thread = new Thread(() -> {
            try (UsageContext _ = UsageContext.open(id)) {
                usageService.record(ai, 5, 6);
            }
        });
        usageService.record(ai, 1, 1);
        thread.start();
        thread.join(30_000);

        // the thread has no user, so the usage of the AI is not counted, the book is
        assertThat(counts()).isEqualTo(StatisticCounts.tokens(5, 6));
    }

    @Test
    void aCallForABookThatIsGoneCountsNothingAndFailsNothing() throws Exception {
        AI ai = createAI();

        try (UsageContext _ = UsageContext.open(Long.MAX_VALUE - 1)) {
            usageService.record(ai, 40, 4);
        }

        assertThat(usageService.findForUser(ai).getPrefillTokens()).isEqualTo(40);
    }
}
