package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationRequest;
import com.github.enerccio.marginalia.test.GenerationRun;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code {{closest-message::n}}}, {@code {{closest-summary::n}}} and {@code {{current-message}}}: the parts and summaries
 * counted back from the part being written.
 */
class ClosestMacroTest extends GenerationTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SummaryService summaryService;

    private Manuscript manuscript;
    private List<ChatMessage> story;

    /**
     * A book of four parts, "part 1" to "part 4", with the master template given.
     */
    private void story(String template) throws Exception {
        Manuscript book = newManuscript();
        book.setTemplate(template);
        manuscript = manuscriptService.save(book);
        story = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            ChatMessage message = new ChatMessage();
            message.setResponse("part " + i);
            message.setTokenCount(5);
            story.add(i == 1 ? chatMessageService.createRoot(manuscript, message) : chatMessageService.addChild(story.getLast(), message));
        }
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setActiveLeaf(story.getLast());
        manuscript = manuscriptService.save(loaded);
    }

    private void summarize(int n, String text) throws Exception {
        llm.enqueue(MockLLMResponse.text(text));
        CompletableFuture<Object> done = new CompletableFuture<>();
        summaryService.createSummary(manuscriptService.find(manuscript.getId()), chatMessageService.find(story.get(n - 1).getId()),
                new SummaryService.AsyncCallback() {
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

    private String systemPromptOf(GenerationRequest request) throws Exception {
        llm.enqueue(MockLLMResponse.text("generated"));
        GenerationRun run = new GenerationRun();
        storyGenerationService.generateNextTurn(manuscriptService.find(manuscript.getId()),
                new TurnInput(null, null, null, "go"), request, run);
        assertThat(run.await(TIMEOUT)).isEqualTo(GenerationRun.Outcome.COMPLETED);
        assertThat(run.getSimpleErrors()).isEmpty();
        return String.join("\n", llm.lastCompletionRequest().getMessages("system"));
    }

    private String systemPrompt() throws Exception {
        return systemPromptOf(GenerationRequest.newMessage());
    }

    @Test
    void closestMessageIsTheLastPartAndNCountsBack() throws Exception {
        story("[{{closest-message}}] [{{closest-message::1}}] [{{closest-message::2}}] [{{closest-message::4}}]");

        assertThat(systemPrompt()).contains("[part 4] [part 4] [part 3] [part 1]");
    }

    @Test
    void aPositionThatIsNotThereIsEmpty() throws Exception {
        story("[{{closest-message::5}}] [{{closest-message::0}}] [{{closest-message::-1}}] [{{closest-message::two}}]");

        assertThat(systemPrompt()).contains("[] [] [] []");
    }

    @Test
    void theArgumentCanBeAMacro() throws Exception {
        story("{{setvar::back::3}}[{{closest-message::{{getvar::back}}}}] [{{closest-message {{getvar::back}}}}]");

        assertThat(systemPrompt()).contains("[part 2] [part 2]");
    }

    @Test
    void camelCaseNamesWork() throws Exception {
        story("[{{closestMessage::2}}] [{{currentMessage}}]");

        assertThat(systemPrompt()).contains("[part 3] []");
    }

    @Test
    void closestSummaryIsTheNewestAndNCountsBack() throws Exception {
        story("[{{closest-summary}}] [{{closest-summary::2}}] [{{closest-summary::3}}]");
        summarize(2, "SUMMARY TWO");
        summarize(4, "SUMMARY FOUR");

        assertThat(systemPrompt()).contains("[SUMMARY FOUR] [SUMMARY TWO] []");
    }

    @Test
    void closestSummaryIsEmptyWithoutSummaries() throws Exception {
        story("[{{closest-summary}}]");

        assertThat(systemPrompt()).contains("[]");
    }

    @Test
    void currentMessageIsThePartARegenerateReplaces() throws Exception {
        story("[{{current-message}}] [{{closest-message}}]");

        // the last part is regenerated: it is the current one, the one before it is the closest
        String prompt = systemPromptOf(GenerationRequest.regenerate(chatMessageService.find(story.getLast().getId())));

        assertThat(prompt).contains("[part 4] [part 3]");
    }

    @Test
    void currentMessageIsThePartASwipeReplacesAndEmptyForANewPart() throws Exception {
        story("[{{current-message}}]");

        assertThat(systemPromptOf(GenerationRequest.newSwipe(chatMessageService.find(story.getLast().getId())))).contains("[part 4]");
        assertThat(systemPrompt()).contains("[]");
    }
}
