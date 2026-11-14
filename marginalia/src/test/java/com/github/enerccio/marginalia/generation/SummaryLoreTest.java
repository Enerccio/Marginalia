package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The lore a summary of a part is written with is the one of the lorebooks now, not the one stored when the part was generated.
 */
class SummaryLoreTest extends GenerationTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private MessagePromptService messagePromptService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Test
    void theSummaryGetsTheLoreOfTheLorebooksNow() throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName("world");
        lorebook = lorebookService.save(lorebook);
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName("dragons");
        entry.setPayload("Dragons are small.");
        entry = lorebookEntryService.save(entry);
        Manuscript manuscript = newManuscript();
        manuscript.setLorebook(lorebook);
        manuscript = manuscriptService.save(manuscript);
        ChatMessage message = new ChatMessage();
        message.setResponse("TEXT-1");
        message.setTokenCount(5);
        ChatMessage part = chatMessageService.createRoot(manuscript, message);
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setActiveLeaf(part);
        manuscript = manuscriptService.save(loaded);
        messagePromptService.store(part, null, "Dragons are small.");

        entry.setPayload("Dragons are huge.");
        lorebookEntryService.save(entry);
        llm.enqueue(MockLLMResponse.text("A SUMMARY"));
        CompletableFuture<Object> done = new CompletableFuture<>();
        summaryService.createSummary(manuscriptService.find(manuscript.getId()), chatMessageService.find(part.getId()),
                new SummaryService.AsyncCallback() {
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

        assertThat(llm.lastCompletionRequest().getPromptText()).contains("Dragons are huge.").doesNotContain("Dragons are small.");
    }
}
