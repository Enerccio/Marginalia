package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.components.BranchTokens;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The tokens the story of a branch takes with and without its summaries, counted before anything is generated.
 */
class BranchTokensTest extends MarginaliaTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private SummaryService summaryService;

    private Manuscript manuscript;
    private ChatMessage p1, p2, p3, p4;

    private ChatMessage part(ChatMessage parent, String text, long tokens) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        message.setTokenCount(tokens);
        Thread.sleep(3);
        return parent == null
                ? chatMessageService.createRoot(manuscript, message)
                : chatMessageService.addChild(parent, message);
    }

    @BeforeEach
    void createStory() throws Exception {
        login();
        Manuscript m = new Manuscript();
        m.setName(uniqueName("book"));
        manuscript = manuscriptService.save(m);
        p1 = part(null, "one", 10);
        p2 = part(p1, "two", 20);
        p3 = part(p2, "three", 30);
        p4 = part(p3, "four", 40);
        manuscript = manuscriptService.find(manuscript);
        manuscript.setActiveLeaf(p4);
        manuscript = manuscriptService.save(manuscript);
    }

    private void summarize(ChatMessage message, long tokens) throws Exception {
        Summary summary = new Summary();
        summary.setSummary("summary of " + message.getResponse());
        summary.setSummaryTokens(tokens);
        summary = summaryService.save(summary);
        ChatMessage loaded = chatMessageService.find(message.getId());
        loaded.setSummary(summary);
        chatMessageService.save(loaded);
    }

    private void markValid() throws Exception {
        List<ChatMessage> newestFirst = chatMessageService.getBranchFromLeaf(chatMessageService.find(p4.getId())).reversed();
        summaryService.revalidate(manuscript, newestFirst, null);
    }

    @Test
    void withoutSummariesTheWholeStoryIsInThePrompt() throws Exception {
        BranchTokens tokens = chatMessageService.getBranchTokens(p4);

        assertThat(tokens.parts()).isEqualTo(4);
        assertThat(tokens.partTokens()).isEqualTo(100);
        assertThat(tokens.summaries()).isZero();
        assertThat(tokens.summarizedParts()).isZero();
        assertThat(tokens.withSummariesTokens()).isEqualTo(100);
        assertThat(tokens.staleSummaries()).isZero();
    }

    @Test
    void aSummaryStandsInForThePartsUpToItsMessage() throws Exception {
        summarize(p2, 5);
        markValid();

        BranchTokens tokens = chatMessageService.getBranchTokens(p4);

        assertThat(tokens.summaries()).isEqualTo(1);
        assertThat(tokens.summaryTokens()).isEqualTo(5);
        assertThat(tokens.summarizedParts()).isEqualTo(2);
        assertThat(tokens.summarizedTokens()).isEqualTo(30);
        assertThat(tokens.unsummarizedParts()).isEqualTo(2);
        assertThat(tokens.unsummarizedTokens()).isEqualTo(70);
        assertThat(tokens.withSummariesTokens()).isEqualTo(75);
        assertThat(tokens.partTokens()).isEqualTo(100);
    }

    @Test
    void severalSummariesAreAddedUp() throws Exception {
        summarize(p2, 5);
        summarize(p3, 6);
        markValid();

        BranchTokens tokens = chatMessageService.getBranchTokens(p4);

        assertThat(tokens.summaries()).isEqualTo(2);
        assertThat(tokens.summaryTokens()).isEqualTo(11);
        assertThat(tokens.summarizedParts()).isEqualTo(3);
        assertThat(tokens.withSummariesTokens()).isEqualTo(11 + 40);
    }

    @Test
    void aSummaryThatNoLongerMatchesIsNotUsed() throws Exception {
        summarize(p2, 5);
        // never made valid for the story, as if a part had been edited after it

        BranchTokens tokens = chatMessageService.getBranchTokens(p4);

        assertThat(tokens.staleSummaries()).isEqualTo(1);
        assertThat(tokens.summaries()).isZero();
        assertThat(tokens.withSummariesTokens()).isEqualTo(100);
    }

    @Test
    void theBranchOfAnEarlierLeafIsCountedOnly() throws Exception {
        BranchTokens tokens = chatMessageService.getBranchTokens(p2);

        assertThat(tokens.parts()).isEqualTo(2);
        assertThat(tokens.partTokens()).isEqualTo(30);
    }
}
