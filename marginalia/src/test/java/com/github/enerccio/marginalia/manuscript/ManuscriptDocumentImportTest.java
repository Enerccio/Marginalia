package com.github.enerccio.marginalia.manuscript;

import com.github.enerccio.marginalia.domain.collections.RevisionDetail;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ArchiveImportService;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Parts cut from a document are added to the end of a book, as written by the author.
 */
class ManuscriptDocumentImportTest extends MarginaliaTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ArchiveImportService archiveImportService;

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
}
