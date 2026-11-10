package com.github.enerccio.marginalia.manuscript;

import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.components.RevisionRow;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ArchiveImportService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.MessageRevisionService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Parts cut from a document are added to a book, at the end or between two parts, as written by the author.
 */
class ManuscriptDocumentImportTest extends MarginaliaTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ArchiveImportService archiveImportService;

    @Autowired
    private MessageRevisionService messageRevisionService;

    private Manuscript book;

    @BeforeEach
    void createBook() throws Exception {
        login();
        Manuscript m = new Manuscript();
        m.setName(uniqueName("book"));
        book = manuscriptService.save(m);
    }

    @Test
    void textsBecomeAChainOfParts() throws Exception {
        ArchiveImportService.ImportResult<Manuscript> result = archiveImportService.appendParts(book,
                List.of("# One\n\nfirst words", "second", "# Two\n\nthird"), RevisionDetail.DOCUMENT_IMPORT, () -> false, () -> { });

        assertThat(result.isComplete()).isTrue();
        List<ChatMessage> branch = chatMessageService.getBranchFromLeaf(manuscriptService.find(book).getActiveLeaf());
        assertThat(branch).extracting(ChatMessage::getResponse).containsExactly("# One\n\nfirst words", "second", "# Two\n\nthird");
        assertThat(branch).allSatisfy(part -> assertThat(part.getWordCount()).isPositive());
    }

    @Test
    void aStoppedImportLeavesThePartsImportedUntilThen() throws Exception {
        int[] stored = {0};

        ArchiveImportService.ImportResult<Manuscript> result = archiveImportService.appendParts(book, List.of("a", "b", "c", "d"),
                RevisionDetail.DOCUMENT_IMPORT, () -> stored[0] == 2, () -> stored[0]++);

        assertThat(result.isComplete()).isFalse();
        assertThat(result.imported()).isEqualTo(2);
        assertThat(chatMessageService.getBranchFromLeaf(result.target().getActiveLeaf()))
                .extracting(ChatMessage::getResponse).containsExactly("a", "b");
    }

    private List<ChatMessage> story() throws Exception {
        return chatMessageService.getBranchFromLeaf(manuscriptService.find(book).getActiveLeaf());
    }

    private List<String> texts() throws Exception {
        return story().stream().map(ChatMessage::getResponse).toList();
    }

    private void insert(ArchiveImportService.Insertion place, String... texts) throws Exception {
        ArchiveImportService.ImportResult<Manuscript> result = archiveImportService.insertParts(manuscriptService.find(book), place,
                List.of(texts), RevisionDetail.DOCUMENT_IMPORT, () -> false, () -> { });
        assertThat(result.isComplete()).isTrue();
    }

    @Test
    void partsGoAfterAPartAndWhatFollowedItFollowsThem() throws Exception {
        archiveImportService.appendParts(book, List.of("a", "b", "c", "d"), RevisionDetail.DOCUMENT_IMPORT, () -> false, () -> { });
        ChatMessage b = story().get(1);

        insert(ArchiveImportService.Insertion.after(b), "x", "y");

        assertThat(texts()).containsExactly("a", "b", "x", "y", "c", "d");
        // the end of the story is where it was
        assertThat(story().getLast().getResponse()).isEqualTo("d");
        assertThat(story()).allSatisfy(part -> assertThat(part.getWordCount()).isPositive());
    }

    @Test
    void partsGoBeforeThePartsOfTheStoryToo() throws Exception {
        archiveImportService.appendParts(book, List.of("a", "b"), RevisionDetail.DOCUMENT_IMPORT, () -> false, () -> { });

        insert(ArchiveImportService.Insertion.START, "x", "y");

        assertThat(texts()).containsExactly("x", "y", "a", "b");
    }

    @Test
    void everyBranchBelowThePlaceFollowsTheNewParts() throws Exception {
        archiveImportService.appendParts(book, List.of("a", "b", "c"), RevisionDetail.DOCUMENT_IMPORT, () -> false, () -> { });
        ChatMessage b = story().get(1);
        // a second continuation of b, which is the end of the story now
        ChatMessage other = chatMessageService.createPart(manuscriptService.find(book), b, "other", null);

        insert(ArchiveImportService.Insertion.after(b), "x");

        assertThat(chatMessageService.getBranchFromLeaf(other)).extracting(ChatMessage::getResponse).containsExactly("a", "b", "x", "other");
        assertThat(texts()).containsExactly("a", "b", "x", "other");
    }

    @Test
    void partsGoAtTheEndOfTheStoryAndIntoABookWithoutParts() throws Exception {
        insert(ArchiveImportService.Insertion.END, "a");
        insert(ArchiveImportService.Insertion.END, "b", "c");

        assertThat(texts()).containsExactly("a", "b", "c");
    }

    @Test
    void theImportedPartsAreWrittenByTheAuthor() throws Exception {
        archiveImportService.appendParts(book, List.of("a", "b"), RevisionDetail.DOCUMENT_IMPORT, () -> false, () -> { });

        insert(ArchiveImportService.Insertion.after(story().getFirst()), "x");

        ChatMessage x = story().get(1);
        assertThat(x.getResponse()).isEqualTo("x");
        List<RevisionRow> history = messageRevisionService.list(x, true);
        assertThat(history).extracting(RevisionRow::source).containsExactly(RevisionSource.HUMAN);
        assertThat(messageRevisionService.find(history.getFirst().id()).getSourceDetail()).isEqualTo(RevisionDetail.DOCUMENT_IMPORT.code());
    }
}
