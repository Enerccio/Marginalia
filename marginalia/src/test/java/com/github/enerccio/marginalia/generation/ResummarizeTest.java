package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.collections.SummaryType;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.service.SummaryService.SummaryBlock;
import com.github.enerccio.marginalia.domain.service.job.FinishedJob;
import com.github.enerccio.marginalia.domain.service.job.JobStatus;
import com.github.enerccio.marginalia.domain.service.job.Jobs;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link SummaryService#resummarize}: every summary of the active branch is written again, the inner ones of a meta
 * summary first, and the hashes are worked out again.
 * <p>
 * The story is m1 (root) ... m8 (leaf), a summary on mN covers mN down to the message after the previous summary.
 */
class ResummarizeTest extends GenerationTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SummaryService summaryService;

    private Manuscript manuscript;
    private List<ChatMessage> story;

    @BeforeEach
    void createStory() throws Exception {
        manuscript = manuscriptService.save(newManuscript());
        story = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            ChatMessage message = new ChatMessage();
            message.setResponse("part " + i);
            message.setTokenCount(5);
            ChatMessage saved = i == 1 ? chatMessageService.createRoot(manuscript, message)
                    : chatMessageService.addChild(story.getLast(), message);
            story.add(saved);
        }
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setActiveLeaf(story.getLast());
        manuscript = manuscriptService.save(loaded);
    }

    private ChatMessage m(int n) throws Exception {
        return chatMessageService.find(story.get(n - 1).getId());
    }

    private Summary summaryOf(int n) throws Exception {
        ChatMessage message = m(n);
        return message.getSummary() == null ? null : summaryService.find(message.getSummary());
    }

    private Manuscript book() throws Exception {
        return manuscriptService.find(manuscript.getId());
    }

    private void summarize(int n, String text) throws Exception {
        llm.enqueue(MockLLMResponse.text(text));
        CompletableFuture<Summary> done = new CompletableFuture<>();
        summaryService.createSummary(book(), m(n), new SummaryService.AsyncCallback() {
            @Override
            public void onSummaryProgress(String reasoning, String summary) {
            }

            @Override
            public void onSummaryFinished(Summary summary) {
                done.complete(summary);
            }

            @Override
            public void onSummaryTerminated() {
                done.completeExceptionally(new IllegalStateException("terminated"));
            }

            @Override
            public void onError(Throwable throwable) {
                done.completeExceptionally(throwable);
            }
        });
        done.get(30, TimeUnit.SECONDS);
    }

    private void meta(int from, int to, String text) throws Exception {
        llm.enqueue(MockLLMResponse.text(text));
        CompletableFuture<Summary> done = new CompletableFuture<>();
        summaryService.createMetaSummary(book(), m(from), m(to), new SummaryService.AsyncCallback() {
            @Override
            public void onSummaryProgress(String reasoning, String summary) {
            }

            @Override
            public void onSummaryFinished(Summary summary) {
                done.complete(summary);
            }

            @Override
            public void onSummaryTerminated() {
                done.completeExceptionally(new IllegalStateException("terminated"));
            }

            @Override
            public void onError(Throwable throwable) {
                done.completeExceptionally(throwable);
            }
        });
        done.get(30, TimeUnit.SECONDS);
    }

    private List<SummaryBlock> blocks() throws Exception {
        return summaryService.collectBlocks(chatMessageService.getBranchFromLeaf(m(8)).reversed());
    }

    private List<String> blockTexts() throws Exception {
        return blocks().stream().map(b -> b.summary().getSummary()).toList();
    }

    /**
     * What a run reported, the outcome is the future.
     */
    private static final class Run implements SummaryService.ResummaryCallback {
        final CompletableFuture<String> outcome = new CompletableFuture<>();
        final List<String> steps = new CopyOnWriteList();
        volatile Runnable beforeStep = () -> {
        };

        @Override
        public void onStep(int number, int total, ChatMessage part, boolean meta) {
            steps.add(number + "/" + total + (meta ? " meta" : ""));
            beforeStep.run();
        }

        @Override
        public void onSummaryProgress(String reasoning, String summary) {
        }

        @Override
        public void onFinished(int count) {
            outcome.complete("finished " + count);
        }

        @Override
        public void onTerminated() {
            outcome.complete("terminated");
        }

        @Override
        public void onError(Throwable throwable) {
            outcome.complete("error " + throwable.getMessage());
        }
    }

    private static final class CopyOnWriteList extends java.util.concurrent.CopyOnWriteArrayList<String> {
    }

    private String resummarize(Run run) throws Exception {
        CancellationToken token = summaryService.resummarize(book(), run);
        assertThat(token).isNotNull();
        return run.outcome.get(60, TimeUnit.SECONDS);
    }

    private String prompt(int request) {
        return llm.getCompletionRequests().get(request).getPromptText();
    }

    @Test
    void nothingToDoWithoutSummaries() throws Exception {
        assertThat(summaryService.resummarize(book(), new Run())).isNull();
    }

    @Test
    void writesEverySummaryAgainOldestFirstAndKeepsUuids() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");
        summarize(6, "S6");
        String uuid2 = summaryOf(2).getUuid();
        String uuid6 = summaryOf(6).getUuid();
        int before = llm.getCompletionRequests().size();

        llm.enqueue(MockLLMResponse.text("R2"), MockLLMResponse.text("R4"), MockLLMResponse.text("R6"));
        Run run = new Run();
        assertThat(resummarize(run)).isEqualTo("finished 3");

        assertThat(run.steps).containsExactly("1/3", "2/3", "3/3");
        assertThat(blockTexts()).containsExactly("R6", "R4", "R2");
        assertThat(summaryOf(2).getUuid()).isEqualTo(uuid2);
        assertThat(summaryOf(6).getUuid()).isEqualTo(uuid6);
        assertThat(summaryOf(6).getSummaryTokens()).isPositive();
        assertThat(blocks()).allMatch(SummaryBlock::isValid);

        // the oldest summary first, each one reads its own parts
        assertThat(prompt(before)).contains("part 1").contains("part 2").doesNotContain("part 3");
        assertThat(prompt(before + 1)).contains("part 3").contains("part 4").doesNotContain("part 5");
        assertThat(prompt(before + 2)).contains("part 5").contains("part 6").doesNotContain("part 7");
    }

    @Test
    void theWholeRunIsOneJobWithARequestPerStep() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");
        summarize(6, "S6");
        List<FinishedJob> ended = Collections.synchronizedList(new ArrayList<>());
        Consumer<FinishedJob> sink = ended::add;
        Jobs.addSink(sink);
        try {
            llm.enqueue(MockLLMResponse.text("R2"), MockLLMResponse.text("R4"), MockLLMResponse.text("R6"));
            assertThat(resummarize(new Run())).isEqualTo("finished 3");
        } finally {
            Jobs.removeSink(sink);
        }

        assertThat(ended).singleElement().satisfies(job -> {
            assertThat(job.kind()).isEqualTo("summary");
            assertThat(job.status()).isEqualTo(JobStatus.COMPLETED);
            assertThat(job.calls()).hasSize(3);
        });
    }

    @Test
    void aStoppedRunIsACancelledJob() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");
        List<FinishedJob> ended = Collections.synchronizedList(new ArrayList<>());
        Consumer<FinishedJob> sink = ended::add;
        Jobs.addSink(sink);
        try {
            llm.enqueue(MockLLMResponse.text("R2"), MockLLMResponse.text("R4"));
            Run run = new Run();
            AtomicReference<CancellationToken> token = new AtomicReference<>();
            // stop before the second step is asked
            run.beforeStep = () -> {
                if (run.steps.size() == 2 && token.get() != null) {
                    token.get().cancel();
                }
            };
            token.set(summaryService.resummarize(book(), run));
            assertThat(run.outcome.get(60, TimeUnit.SECONDS)).isEqualTo("terminated");
        } finally {
            Jobs.removeSink(sink);
        }

        assertThat(ended).singleElement().satisfies(job -> assertThat(job.status()).isEqualTo(JobStatus.CANCELLED));
    }

    @Test
    void outdatedSummaryIsValidAfterwards() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");
        ChatMessage edited = m(3);
        edited.setResponse("part 3 rewritten");
        chatMessageService.save(edited);
        assertThat(blocks().getFirst().isValid()).isFalse();

        llm.enqueue(MockLLMResponse.text("R2"), MockLLMResponse.text("R4"));
        assertThat(resummarize(new Run())).isEqualTo("finished 2");

        assertThat(blocks()).allMatch(SummaryBlock::isValid);
        assertThat(blockTexts()).containsExactly("R4", "R2");
    }

    @Test
    void metaSummaryIsMadeFromTheNewTextsOfItsSummaries() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");
        summarize(6, "S6");
        meta(6, 4, "META");
        int before = llm.getCompletionRequests().size();

        // S2, then what the meta summary stands in for (S4, the replaced S6), then the meta summary
        llm.enqueue(MockLLMResponse.text("R2"), MockLLMResponse.text("R4"), MockLLMResponse.text("R6"),
                MockLLMResponse.text("RMETA"));
        Run run = new Run();
        assertThat(resummarize(run)).isEqualTo("finished 4");

        assertThat(run.steps).containsExactly("1/4", "2/4", "3/4", "4/4 meta");
        assertThat(prompt(before)).contains("part 2");
        assertThat(prompt(before + 1)).contains("part 3").contains("part 4");
        assertThat(prompt(before + 2)).contains("part 5").contains("part 6");
        String metaPrompt = prompt(before + 3);
        assertThat(metaPrompt).contains("R4").contains("R6").doesNotContain("S4").doesNotContain("S6");
        assertThat(metaPrompt.indexOf("R4")).isLessThan(metaPrompt.indexOf("R6"));

        Summary meta = summaryOf(6);
        assertThat(meta.getSummaryType()).isEqualTo(SummaryType.META_SUMMARY);
        assertThat(meta.getSummary()).isEqualTo("RMETA");
        assertThat(meta.getReplacedSummary()).contains("R6").doesNotContain("\"S6\"");
        assertThat(summaryOf(4).getSummary()).isEqualTo("R4");
        assertThat(blockTexts()).containsExactly("RMETA", "R2");
        // the hash is made of the new texts of the summaries inside
        assertThat(blocks()).allMatch(SummaryBlock::isValid);

        // what the meta summary replaced is the new text too
        summaryService.removeSummary(m(6), true);
        assertThat(blockTexts()).containsExactly("R6", "R4", "R2");
        assertThat(blocks()).allMatch(SummaryBlock::isValid);
    }

    @Test
    void metaSummariesOfMetaSummariesGoInnerFirst() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");
        summarize(6, "S6");
        summarize(8, "S8");
        meta(4, 2, "META A");
        meta(8, 6, "META B");
        meta(8, 4, "META C");
        int before = llm.getCompletionRequests().size();

        // META A: S2 and the replaced S4; META B: S6 and the replaced S8; META C: both (it replaced META B on 8)
        llm.enqueue(MockLLMResponse.text("R2"), MockLLMResponse.text("R4"), MockLLMResponse.text("RA"),
                MockLLMResponse.text("R6"), MockLLMResponse.text("R8"), MockLLMResponse.text("RB"),
                MockLLMResponse.text("RC"));
        Run run = new Run();
        assertThat(resummarize(run)).isEqualTo("finished 7");

        assertThat(run.steps.getLast()).isEqualTo("7/7 meta");
        String last = prompt(before + 6);
        assertThat(last).contains("RA").contains("RB").doesNotContain("META A").doesNotContain("META B");
        assertThat(last.indexOf("RA")).isLessThan(last.indexOf("RB"));
        assertThat(blockTexts()).containsExactly("RC");
        assertThat(blocks()).allMatch(SummaryBlock::isValid);

        // unwinding finds the new texts all the way down
        summaryService.removeSummary(m(8), true);
        assertThat(blockTexts()).containsExactly("RB", "RA");
        assertThat(blocks()).allMatch(SummaryBlock::isValid);
        summaryService.removeSummary(m(8), true);
        assertThat(blockTexts()).containsExactly("R8", "R6", "RA");
        summaryService.removeSummary(m(4), true);
        assertThat(blockTexts()).containsExactly("R8", "R6", "R4", "R2");
        assertThat(blocks()).allMatch(SummaryBlock::isValid);
    }

    @Test
    void stopLeavesEverythingAsItWas() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");
        summarize(6, "S6");
        AtomicReference<CancellationToken> token = new AtomicReference<>();

        llm.enqueue(MockLLMResponse.text("R2"), MockLLMResponse.text("R4"), MockLLMResponse.text("R6"));
        Run run = new Run();
        run.beforeStep = () -> {
            if (run.steps.size() == 2) {
                token.get().cancel();
            }
        };
        token.set(summaryService.resummarize(book(), run));

        assertThat(run.outcome.get(60, TimeUnit.SECONDS)).isEqualTo("terminated");
        assertThat(blockTexts()).containsExactly("S6", "S4", "S2");
        assertThat(blocks()).allMatch(SummaryBlock::isValid);
    }

    @Test
    void emptyAnswerFailsAndChangesNothing() throws Exception {
        summarize(2, "S2");
        summarize(4, "S4");

        llm.enqueue(MockLLMResponse.text("R2"), MockLLMResponse.text(""));
        String outcome = resummarize(new Run());

        assertThat(outcome).startsWith("error");
        assertThat(blockTexts()).containsExactly("S4", "S2");
    }
}
