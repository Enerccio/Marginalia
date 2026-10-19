package com.github.enerccio.marginalia.generation;

import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService.PartEdit;
import com.github.enerccio.marginalia.domain.service.ManuscriptService.BookSettings;
import com.github.enerccio.marginalia.domain.service.ManuscriptService.SettingsSaved;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * What the editing of a part and the book settings do in the services, for the screens and for the commands.
 * <pre>
 * m1 ─ m2 ─ m3[S3] ─ m4 ─ m5[S5]      active branch
 *            └─ m4b                   a swipe of m4
 * </pre>
 */
class PartEditServicesTest extends GenerationTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SummaryService summaryService;

    private Manuscript manuscript;
    private List<ChatMessage> story;
    private ChatMessage m4b;

    @BeforeEach
    void createStory() throws Exception {
        manuscript = manuscriptService.save(newManuscript());
        story = new ArrayList<>();
        for (String text : new String[]{"Anna met John", "John went home", "Nothing here", "John smiled", "John left"}) {
            story.add(add(story.isEmpty() ? null : story.getLast(), text));
        }
        m4b = add(story.get(2), "John frowned");
        summarize(story.get(2), "John and Anna");
        summarize(story.get(4), "John smiled and left");
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

    // ------------------------------------------------------------------------------------------- the edit of a part

    @Test
    void updateResponseStoresTheTextAndCountsIt() throws Exception {
        PartEdit edit = chatMessageService.updateResponse(manuscript, story.get(3), "John smiled at the sea");

        ChatMessage part = stored(story.get(3));
        assertThat(edit.message().getId()).isEqualTo(part.getId());
        assertThat(part.getResponse()).isEqualTo("John smiled at the sea");
        assertThat(part.isEdited()).isTrue();
        assertThat(part.getWordCount()).isEqualTo(5);
        assertThat(part.getTokenCount()).isPositive();
    }

    @Test
    void updateResponseSaysWhichSummariesBecameOutdated() throws Exception {
        // m4 is in the block of S5 (m4-m5), S3 (m1-m3) is not touched
        PartEdit edit = chatMessageService.updateResponse(manuscript, story.get(3), "John smiled at the sea");

        assertThat(edit.outdated()).extracting(block -> block.head().getId()).containsExactly(story.get(4).getId());
        assertThat(edit.outdated()).noneMatch(SummaryBlock::isValid);

        // a second edit of the same block makes nothing new outdated
        PartEdit again = chatMessageService.updateResponse(manuscript, story.get(4), "John left the sea");
        assertThat(again.outdated()).isEmpty();

        PartEdit first = chatMessageService.updateResponse(manuscript, story.get(0), "Anna met Jon");
        assertThat(first.outdated()).extracting(block -> block.head().getId()).containsExactly(story.get(2).getId());
    }

    @Test
    void anEditOfThePartNobodyCoversOutdatesNothing() throws Exception {
        PartEdit edit = chatMessageService.updateResponse(manuscript, m4b, "John frowned a lot");

        assertThat(edit.outdated()).isEmpty();
        assertThat(stored(m4b).getResponse()).isEqualTo("John frowned a lot");
    }

    @Test
    void updateResponseRefusesAPartOfAnotherBook() throws Exception {
        Manuscript other = manuscriptService.save(newManuscript());

        assertThatThrownBy(() -> chatMessageService.updateResponse(other, story.get(0), "x")).isInstanceOf(IllegalArgumentException.class);
        assertThat(stored(story.get(0)).getResponse()).isEqualTo("Anna met John");
    }

    // ------------------------------------------------------------------------------------ branch and swipe

    @Test
    void branchAndSwitchMakesTheCopyTheActiveLeaf() throws Exception {
        Manuscript saved = chatMessageService.branchAndSwitch(manuscript, story.get(3));

        ChatMessage branch = stored(saved.getActiveLeaf());
        assertThat(branch.getId()).isNotEqualTo(story.get(3).getId());
        assertThat(branch.getResponse()).isEqualTo("John smiled");
        assertThat(branch.getParent().getId()).isEqualTo(story.get(2).getId());
        assertThat(manuscriptService.find(manuscript.getId()).getActiveLeaf().getId()).isEqualTo(branch.getId());
        assertThat(stored(story.get(3)).getResponse()).isEqualTo("John smiled");
    }

    @Test
    void swipeAndSwitchGoesToTheNewestEndBelowThePart() throws Exception {
        Manuscript onSwipe = chatMessageService.swipeAndSwitch(manuscript, m4b);
        assertThat(onSwipe.getActiveLeaf().getId()).isEqualTo(m4b.getId());
        assertThat(manuscriptService.find(manuscript.getId()).getActiveLeaf().getId()).isEqualTo(m4b.getId());

        Manuscript back = chatMessageService.swipeAndSwitch(manuscript, story.get(3));
        assertThat(back.getActiveLeaf().getId()).isEqualTo(story.get(4).getId());
    }

    @Test
    void branchingRefusesAPartOfAnotherBook() throws Exception {
        Manuscript other = manuscriptService.save(newManuscript());

        assertThatThrownBy(() -> chatMessageService.branchAndSwitch(other, story.get(0))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> chatMessageService.swipeAndSwitch(other, story.get(0))).isInstanceOf(IllegalArgumentException.class);
    }

    // ------------------------------------------------------------------------------------------ book settings

    @Test
    void updateSettingsChangesOnlyWhatIsSet() throws Exception {
        String name = manuscriptService.find(manuscript.getId()).getName();

        SettingsSaved saved = manuscriptService.updateSettings(manuscript,
                new BookSettings().description("A story about John").language(Language.JAPANESE).pov("third").tense("past").style("terse"));

        assertThat(saved.warnings()).isEmpty();
        Manuscript book = manuscriptService.find(manuscript.getId());
        assertThat(book.getName()).isEqualTo(name);
        assertThat(book.getDescription()).isEqualTo("A story about John");
        assertThat(book.getLanguage()).isEqualTo(Language.JAPANESE);
        assertThat(book.getPov()).isEqualTo("third");
        assertThat(book.getTense()).isEqualTo("past");
        assertThat(book.getStyle()).isEqualTo("terse");
    }

    @Test
    void updateSettingsRefusesABlankName() throws Exception {
        assertThatThrownBy(() -> manuscriptService.updateSettings(manuscript, new BookSettings().name("  ")))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(manuscriptService.updateSettings(manuscript, new BookSettings().name(" Renamed ")).manuscript().getName()).isEqualTo("Renamed");
    }

    @Test
    void anInvalidPromptTemplateChangesNothing() throws Exception {
        assertThatThrownBy(() -> manuscriptService.updateSettings(manuscript,
                new BookSettings().description("changed").userPrompt("{{#if}} never closed")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("userPrompt");

        assertThat(manuscriptService.find(manuscript.getId()).getDescription()).isNotEqualTo("changed");
    }

    @Test
    void aValidPromptTemplateIsSavedAndUnknownNamesAreWarnings() throws Exception {
        SettingsSaved saved = manuscriptService.updateSettings(manuscript,
                new BookSettings().summaryPrompt("Summarize {{nonexistentName}} please"));

        assertThat(saved.warnings()).hasSize(1);
        assertThat(saved.warnings().getFirst()).contains("summaryPrompt").contains("nonexistentName");
        assertThat(manuscriptService.find(manuscript.getId()).getSummaryPrompt()).isEqualTo("Summarize {{nonexistentName}} please");
    }

    @Test
    void updateSettingsRefusesABookOfAnotherUser() throws Exception {
        login();

        assertThatThrownBy(() -> manuscriptService.updateSettings(manuscript, new BookSettings().description("x")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
