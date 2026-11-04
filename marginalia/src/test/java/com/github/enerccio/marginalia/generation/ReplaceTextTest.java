package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.domain.components.RevisionOrigin;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessageRevision;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.MessageRevisionService;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.service.SummaryService.BlockTexts;
import com.github.enerccio.marginalia.domain.service.SummaryService.SummaryBlock;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code ChatMessageService.replaceText}: a text that says what the stored one says in other words (the Conlang Translator's
 * translation). It is not an edit of the author: the part is not flagged, the tokens are those of what the model reads, the summaries
 * that were valid still are, and the history has a version of its own.
 * <pre>
 * m1 ─ m2[S2] ─ m3 ─ m4[S4]      active branch
 * </pre>
 */
class ReplaceTextTest extends GenerationTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private MessageRevisionService messageRevisionService;

    private Manuscript manuscript;
    private List<ChatMessage> story;

    @BeforeEach
    void createStory() throws Exception {
        manuscript = manuscriptService.save(newManuscript());
        story = new ArrayList<>();
        for (String text : new String[]{"Anna met John", "John said I see the enemy", "John went home", "John left"}) {
            story.add(add(story.isEmpty() ? null : story.getLast(), text));
        }
        summarize(story.get(1), "Anna met John, who saw the enemy");
        summarize(story.get(3), "John went home and left");
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setActiveLeaf(story.getLast());
        manuscript = manuscriptService.save(loaded);
    }

    private ChatMessage add(ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        Thread.sleep(3);  // messages are ordered by creation time
        return parent == null ? chatMessageService.createRoot(manuscript, message) : chatMessageService.addChild(parent, message);
    }

    private void summarize(ChatMessage message, String text) throws Exception {
        Summary summary = new Summary();
        summary.setSummary(text);
        summary = summaryService.save(summary);
        message = chatMessageService.find(message.getId());
        message.setSummary(summary);
        chatMessageService.save(message);
        List<ChatMessage> newestFirst = chatMessageService.getBranchFromLeaf(chatMessageService.find(message.getId())).reversed();
        SummaryBlock block = summaryService.collectFirstBlock(newestFirst.iterator(), BlockTexts.CURRENT);
        summary = summaryService.find(summary.getId());
        summary.setSummaryMessageHash(block.hash());
        summaryService.save(summary);
    }

    private ChatMessage stored(ChatMessage message) throws Exception {
        return chatMessageService.find(message.getId());
    }

    private List<SummaryBlock> blocks() throws Exception {
        return summaryService.collectBlocks(chatMessageService.getBranchFromLeaf(stored(story.getLast())).reversed());
    }

    private static final RevisionOrigin TRANSLATION = RevisionOrigin.automatic(RevisionDetail.TRANSLATION, false);

    @Test
    void theTextIsReplacedWithoutFlaggingItAsEdited() throws Exception {
        ChatMessage saved = chatMessageService.replaceText(manuscript, story.get(1), "John said Da ek se za skag",
                "John said I see the enemy", "John said Da ek se za skag", TRANSLATION);

        ChatMessage part = stored(story.get(1));
        assertThat(saved.getId()).isEqualTo(part.getId());
        assertThat(part.getResponse()).isEqualTo("John said Da ek se za skag");
        assertThat(part.isEdited()).isFalse();
        assertThat(part.getOriginalResponse()).isEqualTo("John said Da ek se za skag");
        assertThat(part.getWordCount()).isEqualTo(7);
    }

    @Test
    void anEditOfTheAuthorIsFlaggedAsBefore() throws Exception {
        chatMessageService.updateResponse(manuscript, story.get(2), "John went away");

        assertThat(stored(story.get(2)).isEdited()).isTrue();
    }

    @Test
    void theTokensAreThoseOfWhatTheModelReads() throws Exception {
        String model = "John said I see the enemy and he saw the sea";
        chatMessageService.replaceText(manuscript, story.get(1), "John said Da ek se za skag", model, null, TRANSLATION);
        chatMessageService.updateResponse(manuscript, story.get(2), model);

        assertThat(stored(story.get(1)).getTokenCount()).isEqualTo(stored(story.get(2)).getTokenCount());
        assertThat(stored(story.get(1)).getTokenCount()).isGreaterThan(chatMessageService.countWords("John said Da ek se za skag"));
    }

    @Test
    void withoutAModelTextTheTextItselfIsCounted() throws Exception {
        String text = "John said something else entirely";
        chatMessageService.replaceText(manuscript, story.get(1), text, null, null, TRANSLATION);
        chatMessageService.updateResponse(manuscript, story.get(2), text);

        assertThat(stored(story.get(1)).getTokenCount()).isEqualTo(stored(story.get(2)).getTokenCount());
    }

    @Test
    void withoutAGeneratedTextTheOriginalResponseStaysAsItWas() throws Exception {
        ChatMessage part = stored(story.get(1));
        part.setOriginalResponse("what the model generated");
        chatMessageService.save(part);

        chatMessageService.replaceText(manuscript, story.get(1), "John said Da ek se za skag", null, null, TRANSLATION);

        assertThat(stored(story.get(1)).getOriginalResponse()).isEqualTo("what the model generated");
    }

    @Test
    void theSummariesThatWereValidStayValid() throws Exception {
        assertThat(blocks()).hasSize(2).allMatch(SummaryBlock::isValid);

        // m2 is in the block of S2 (m1-m2): an edit of the author would outdate it, a translation does not
        chatMessageService.replaceText(manuscript, story.get(1), "John said Da ek se za skag", "John said I see the enemy", null, TRANSLATION);

        assertThat(blocks()).hasSize(2).allMatch(SummaryBlock::isValid);
    }

    @Test
    void anEditOfTheAuthorOutdatesThemAsBefore() throws Exception {
        chatMessageService.updateResponse(manuscript, story.get(1), "John said Da ek se za skag");

        assertThat(blocks()).anyMatch(block -> !block.isValid());
    }

    @Test
    void aSummaryThatWasOutdatedStaysOutdated() throws Exception {
        // the author edits m1: S2 is outdated
        chatMessageService.updateResponse(manuscript, story.get(0), "Anna met Jon");
        // the blocks are the newest first: S4 is valid, S2 is not
        assertThat(blocks()).extracting(SummaryBlock::isValid).containsExactly(true, false);

        // a translation of m2, in the same block, does not make it valid
        chatMessageService.replaceText(manuscript, story.get(1), "John said Da ek se za skag", "John said I see the enemy", null, TRANSLATION);

        assertThat(blocks()).extracting(SummaryBlock::isValid).containsExactly(true, false);
    }

    @Test
    void theHistoryHasAVersionOfTheTranslation() throws Exception {
        chatMessageService.replaceText(manuscript, story.get(1), "John said Da ek se za skag", null, null, TRANSLATION);

        List<MessageRevision> versions = new ArrayList<>();
        messageRevisionService.list(stored(story.get(1)), true).forEach(row -> {
            try {
                versions.add(messageRevisionService.find(row.id()));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        assertThat(versions).anyMatch(version -> RevisionDetail.TRANSLATION.code().equals(version.getSourceDetail())
                && "John said Da ek se za skag".equals(version.getResponse()));
        // the text that was replaced is in the history too
        assertThat(versions).anyMatch(version -> "John said I see the enemy".equals(version.getResponse()));
    }
}
