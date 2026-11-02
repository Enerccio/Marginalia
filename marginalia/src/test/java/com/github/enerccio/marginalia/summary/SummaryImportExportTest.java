package com.github.enerccio.marginalia.summary;

import com.github.enerccio.marginalia.domain.collections.SummaryType;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.service.SummaryService.ImportEntry;
import com.github.enerccio.marginalia.domain.service.SummaryService.ImportStatus;
import com.github.enerccio.marginalia.test.GenerationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Export of the summaries of a branch to a text file and the import of the (edited) file.
 * <p>
 * The story: 1 "Anna met John." (summary), 2 "John went home." and 3 "He slept." (summary), all on one branch.
 */
class SummaryImportExportTest extends GenerationTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SummaryService summaryService;

    private Manuscript manuscript;
    private ChatMessage first, second, third;

    @BeforeEach
    void setUp() throws Exception {
        manuscript = story();
        List<ChatMessage> parts = chatMessageService.getBranchFromLeaf(manuscript.getActiveLeaf());
        first = parts.get(0);
        second = parts.get(1);
        third = parts.get(2);
        summarize(first, "Anna meets John.");
        summarize(third, "John goes home and sleeps.");
    }

    private Manuscript story() throws Exception {
        Manuscript book = manuscriptService.save(newManuscript());
        ChatMessage a = add(book, null, "Anna met John.");
        ChatMessage b = add(book, a, "John went home.");
        ChatMessage c = add(book, b, "He slept.");
        Manuscript loaded = manuscriptService.find(book.getId());
        loaded.setActiveLeaf(c);
        return manuscriptService.save(loaded);
    }

    private ChatMessage add(Manuscript book, ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        message.setWordCount(chatMessageService.countWords(text));
        Thread.sleep(3);  // messages are ordered by creation time
        return parent == null ? chatMessageService.createRoot(book, message) : chatMessageService.addChild(parent, message);
    }

    private void summarize(ChatMessage message, String text) throws Exception {
        Summary summary = new Summary();
        summary.setSummary(text);
        summary.setSummaryTokens(5L);
        summary.setSummaryType(SummaryType.SUMMARY);
        summary = summaryService.save(summary);
        ChatMessage loaded = chatMessageService.find(message.getId());
        loaded.setSummary(summary);
        chatMessageService.save(loaded);
    }

    private String summaryOf(ChatMessage message) throws Exception {
        return summaryService.find(chatMessageService.find(message.getId()).getSummary()).getSummary();
    }

    @Test
    void theSummariesOfTheBranchAreExportedOldestFirstWithTheirParts() throws Exception {
        String file = summaryService.exportSummaries(manuscript);

        assertThat(file).startsWith("<!-- marginalia-summaries 1 -->");
        assertThat(file).containsSubsequence(
                "<!-- summary part=\"" + first.getUuid() + "\" order=\"1\" -->", "Anna meets John.",
                "<!-- summary part=\"" + third.getUuid() + "\" order=\"3\" -->", "John goes home and sleeps.");
        assertThat(file).doesNotContain(second.getUuid());
    }

    @Test
    void anUntouchedExportChangesNothing() throws Exception {
        List<ImportEntry> entries = summaryService.analyzeImport(manuscript, summaryService.exportSummaries(manuscript), false);

        assertThat(entries).extracting(ImportEntry::status).containsExactly(ImportStatus.UNCHANGED, ImportStatus.UNCHANGED);
        assertThat(summaryService.applyImport(manuscript, entries)).isZero();
    }

    @Test
    void anEditedTextReplacesTheSummaryOfItsPart() throws Exception {
        String file = summaryService.exportSummaries(manuscript).replace("John goes home and sleeps.", "John goes home.\nHe sleeps soundly.");

        List<ImportEntry> entries = summaryService.analyzeImport(manuscript, file, false);
        assertThat(entries).extracting(ImportEntry::status).containsExactly(ImportStatus.UNCHANGED, ImportStatus.CHANGED);
        assertThat(summaryOf(third)).isEqualTo("John goes home and sleeps.");  // the analysis writes nothing

        assertThat(summaryService.applyImport(manuscript, entries)).isEqualTo(1);

        assertThat(summaryOf(third)).isEqualTo("John goes home.\nHe sleeps soundly.");
        assertThat(summaryOf(first)).isEqualTo("Anna meets John.");
        Summary changed = summaryService.find(chatMessageService.find(third.getId()).getSummary());
        assertThat(changed.getSummaryTokens()).isPositive();
    }

    @Test
    void aPartOfAnotherBookIsFoundByPositionOnlyWhenAsked() throws Exception {
        // a copy of the book: the same story and summaries on parts with other uuids
        Manuscript copy = story();
        List<ChatMessage> parts = chatMessageService.getBranchFromLeaf(copy.getActiveLeaf());
        summarize(parts.get(0), "Old text one.");
        summarize(parts.get(2), "Old text three.");
        String file = summaryService.exportSummaries(manuscript);

        List<ImportEntry> byPart = summaryService.analyzeImport(copy, file, false);
        assertThat(byPart).extracting(ImportEntry::status).containsExactly(ImportStatus.NOT_FOUND, ImportStatus.NOT_FOUND);
        assertThat(summaryService.applyImport(copy, byPart)).isZero();

        List<ImportEntry> byPosition = summaryService.analyzeImport(copy, file, true);
        assertThat(byPosition).extracting(ImportEntry::status)
                .containsExactly(ImportStatus.CHANGED_BY_POSITION, ImportStatus.CHANGED_BY_POSITION);
        assertThat(summaryService.applyImport(copy, byPosition)).isEqualTo(2);

        assertThat(summaryOf(parts.get(0))).isEqualTo("Anna meets John.");
        assertThat(summaryOf(parts.get(2))).isEqualTo("John goes home and sleeps.");
        assertThat(summaryOf(third)).isEqualTo("John goes home and sleeps.");  // the book of the file stays as it was
    }

    @Test
    void aPartWithoutASummaryAndAnEmptyTextAreLeftAlone() throws Exception {
        String file = """
                <!-- summary part="%s" order="2" -->
                A summary for a part that has none.

                <!-- summary part="%s" order="1" -->


                <!-- summary part="no-such-part" order="9" -->
                Nobody's.
                """.formatted(second.getUuid(), first.getUuid());

        List<ImportEntry> entries = summaryService.analyzeImport(manuscript, file, true);

        assertThat(entries).extracting(ImportEntry::status)
                .containsExactly(ImportStatus.NOT_FOUND, ImportStatus.EMPTY, ImportStatus.NOT_FOUND);
        assertThat(summaryService.applyImport(manuscript, entries)).isZero();
        assertThat(summaryOf(first)).isEqualTo("Anna meets John.");
        assertThat(chatMessageService.find(second.getId()).getSummary()).isNull();
    }

    @Test
    void aFileThatIsNotAnExportIsRefused() {
        assertThatThrownBy(() -> summaryService.analyzeImport(manuscript, "{\"json\": true}", false))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
