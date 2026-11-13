package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.components.SummaryPlanner.Mode;
import com.github.enerccio.marginalia.domain.components.SummaryPlanner.Options;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.service.SummaryService.SummaryBlock;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link SummaryService#planSummaries} and {@link SummaryService#createSummaries}: summaries for a stretch of the story, cut by
 * tokens, chapters or parts, only for the parts after the newest summary, each one valid (its hash is the one of its block).
 * <p>
 * The story is m1 (root) ... m8 (leaf), a part is 5 tokens; m1, m4 and m7 start a chapter.
 */
class CreateSummariesTest extends GenerationTestBase {

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
            message.setResponse((i == 1 || i == 4 || i == 7 ? "# Chapter " + i + "\n\n" : "") + "part " + i);
            message.setTokenCount(5);
            ChatMessage saved = i == 1 ? chatMessageService.createRoot(manuscript, message)
                    : chatMessageService.addChild(story.getLast(), message);
            story.add(saved);
        }
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setActiveLeaf(story.getLast());
        manuscript = manuscriptService.save(loaded);
    }

    private Manuscript book() throws Exception {
        return manuscriptService.find(manuscript.getId());
    }

    private ChatMessage m(int n) throws Exception {
        return chatMessageService.find(story.get(n - 1).getId());
    }

    private List<Long> ids(List<ChatMessage> parts) {
        return parts.stream().map(ChatMessage::getId).toList();
    }

    private List<Long> storyIds(int... numbers) {
        List<Long> ids = new ArrayList<>();
        for (int n : numbers) {
            ids.add(story.get(n - 1).getId());
        }
        return ids;
    }

    private static final class Run implements SummaryService.BatchCallback {
        final CompletableFuture<String> outcome = new CompletableFuture<>();
        final List<String> steps = new CopyOnWriteArrayList<>();
        volatile Runnable beforeStep = () -> {
        };

        @Override
        public void onStep(int number, int total, ChatMessage part) {
            steps.add(number + "/" + total);
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
        public void onTerminated(int done) {
            outcome.complete("terminated after " + done);
        }

        @Override
        public void onError(Throwable throwable, int done) {
            outcome.complete("error after " + done);
        }
    }

    private void summarize(int n, String text) throws Exception {
        llm.enqueue(MockLLMResponse.text(text));
        CompletableFuture<Object> done = new CompletableFuture<>();
        summaryService.createSummary(book(), m(n), new SummaryService.AsyncCallback() {
            @Override
            public void onSummaryProgress(String reasoning, String summary) {
            }

            @Override
            public void onSummaryFinished(com.github.enerccio.marginalia.domain.model.impl.Summary summary) {
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

    @Test
    void theStoryAfterTheNewestSummaryIsWhatNeedsOne() throws Exception {
        assertThat(ids(summaryService.unsummarizedParts(book()))).isEqualTo(storyIds(1, 2, 3, 4, 5, 6, 7, 8));
        summarize(3, "S3");
        assertThat(ids(summaryService.unsummarizedParts(book()))).isEqualTo(storyIds(4, 5, 6, 7, 8));
    }

    @Test
    void plansByPartsChaptersAndTheEnd() throws Exception {
        assertThat(ids(summaryService.planSummaries(book(), new Options(Mode.PARTS, 3, 0, false), null))).isEqualTo(storyIds(3, 6));
        assertThat(ids(summaryService.planSummaries(book(), new Options(Mode.PARTS, 3, 0, true), null))).isEqualTo(storyIds(3, 6, 8));
        // the chapters start at m4 and m7: the blocks end at m3 and m6, the last chapter at m8
        assertThat(ids(summaryService.planSummaries(book(), new Options(Mode.CHAPTERS, 0, 0, true), null))).isEqualTo(storyIds(3, 6, 8));
        // up to a part: only the parts until it are cut
        assertThat(ids(summaryService.planSummaries(book(), new Options(Mode.PARTS, 3, 0, true), m(5)))).isEqualTo(storyIds(3, 5));
    }

    @Test
    void aPartThatHasASummaryIsNotOneToStopAt() throws Exception {
        summarize(3, "S3");
        assertThatThrownBy(() -> summaryService.planSummaries(book(), new Options(Mode.PARTS, 2, 0, true), m(2)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void writesTheSummariesOldestFirstEachOneValid() throws Exception {
        List<ChatMessage> plan = summaryService.planSummaries(book(), new Options(Mode.PARTS, 3, 0, true), null);
        llm.enqueue(MockLLMResponse.text("first"), MockLLMResponse.text("second"), MockLLMResponse.text("third"));

        Run run = new Run();
        CancellationToken token = summaryService.createSummaries(book(), plan, run);
        assertThat(token).isNotNull();
        assertThat(run.outcome.get(60, TimeUnit.SECONDS)).isEqualTo("finished 3");
        assertThat(run.steps).containsExactly("1/3", "2/3", "3/3");

        List<SummaryBlock> blocks = blocks();
        // newest first: m8 covers m7 and m8, m6 covers m4 to m6, m3 covers m1 to m3
        assertThat(blocks).extracting(b -> b.summary().getSummary()).containsExactly("third", "second", "first");
        assertThat(blocks).allMatch(SummaryBlock::isValid);
        assertThat(blocks).extracting(b -> b.head().getId()).containsExactlyElementsOf(storyIds(8, 6, 3));
    }

    @Test
    void onlyThePartsAfterTheNewestSummaryGetSummaries() throws Exception {
        summarize(4, "S4");
        int before = llm.getCompletionRequests().size();
        List<ChatMessage> plan = summaryService.planSummaries(book(), new Options(Mode.PARTS, 2, 0, true), null);
        assertThat(ids(plan)).isEqualTo(storyIds(6, 8));
        llm.enqueue(MockLLMResponse.text("a"), MockLLMResponse.text("b"));

        Run run = new Run();
        summaryService.createSummaries(book(), plan, run);
        assertThat(run.outcome.get(60, TimeUnit.SECONDS)).isEqualTo("finished 2");
        assertThat(llm.getCompletionRequests().size() - before).isEqualTo(2);
        // the summary before is as it was, the new ones follow it
        assertThat(blocks()).extracting(b -> b.summary().getSummary()).containsExactly("b", "a", "S4");
        assertThat(blocks()).allMatch(SummaryBlock::isValid);
    }

    @Test
    void aStopKeepsTheSummariesWrittenBefore() throws Exception {
        List<ChatMessage> plan = summaryService.planSummaries(book(), new Options(Mode.PARTS, 3, 0, true), null);
        llm.enqueue(MockLLMResponse.text("first"), MockLLMResponse.text("second"), MockLLMResponse.text("third"));

        Run run = new Run();
        // set by this thread, read by the summaries thread: needs a visible write
        AtomicReference<CancellationToken> token = new AtomicReference<>();
        // stopped when the second one is about to be written
        run.beforeStep = () -> {
            if (run.steps.size() == 2) {
                while (token.get() == null) {
                    Thread.onSpinWait();
                }
                token.get().cancel();
            }
        };
        token.set(summaryService.createSummaries(book(), plan, run));
        assertThat(run.outcome.get(60, TimeUnit.SECONDS)).isEqualTo("terminated after 1");
        assertThat(blocks()).extracting(b -> b.summary().getSummary()).containsExactly("first");
    }

    @Test
    void nothingIsStartedWithoutPartsOrAModel() throws Exception {
        assertThat(summaryService.createSummaries(book(), List.of(), new Run())).isNull();
    }
}
