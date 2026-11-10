package com.github.enerccio.marginalia.ui.dialogs.bookimport;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ArchiveImportService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.UI;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFStyle;
import org.apache.poi.xwpf.usermodel.XWPFStyles;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTStyle;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The sources of the import window: what each reads, says and offers, and the dialog that holds them.
 */
class BookImportSourcesTest extends MarginaliaTestBase {

    @Autowired
    private Localization loc;

    @Autowired
    private ManuscriptService manuscriptService;

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
    private UI ui;

    @BeforeEach
    void setUp() {
        ui = new UI();
        UI.setCurrent(ui);
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private static byte[] docx(int chapters, int paragraphsInChapter) throws Exception {
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFStyles styles = document.createStyles();
            CTStyle style = CTStyle.Factory.newInstance();
            style.setStyleId("Heading1");
            style.addNewName().setVal("heading 1");
            styles.addStyle(new XWPFStyle(style));
            for (int c = 1; c <= chapters; c++) {
                XWPFParagraph heading = document.createParagraph();
                heading.setStyle("Heading1");
                heading.createRun().setText("Chapter " + c);
                for (int p = 0; p < paragraphsInChapter; p++) {
                    document.createParagraph().createRun().setText("word ".repeat(100).trim());
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);
            return out.toByteArray();
        }
    }

    @Test
    void aDocumentHasOptionsAndTellsWhatItWillMake() throws Exception {
        DocumentSource source = new DocumentSource(loc);
        assertThat(source.ready()).isFalse();
        assertThat(source.options()).isNotNull();

        AtomicReference<Integer> changes = new AtomicReference<>(0);
        source.onChange(() -> changes.updateAndGet(n -> n + 1));
        source.read(docx(2, 12), "My Novel.docx");

        assertThat(source.ready()).isTrue();
        assertThat(source.suggestedName()).isEqualTo("My Novel");
        // two chapters of 1202 words (the heading counts): a part each
        assertThat(source.status()).isEqualTo("2 chapters, 2404 words, 2 parts.");
        assertThat(source.prepare()).isNotNull();
        assertThat(changes.get()).isPositive();

        source.reset();
        assertThat(source.ready()).isFalse();
        assertThat(source.status()).isEmpty();
    }

    @Test
    void textFilesAreDocumentsToo() {
        DocumentSource source = new DocumentSource(loc);
        source.read("# One\n\nSome words here.\n\n# Two\n\nMore words.".getBytes(StandardCharsets.UTF_8), "Story.md");
        assertThat(source.suggestedName()).isEqualTo("Story");
        assertThat(source.status()).isEqualTo("2 chapters, 7 words, 2 parts.");
        source.read("Chapter 1\n\nWords.\n".getBytes(StandardCharsets.UTF_8), "Notes.txt");
        assertThat(source.suggestedName()).isEqualTo("Notes");
        assertThat(source.status()).isEqualTo("1 chapters, 3 words, 1 parts.");
        source.read("{\\rtf1\\ansi Plain words\\par}".getBytes(StandardCharsets.UTF_8), "a.rtf");
        assertThat(source.status()).isEqualTo("1 chapters, 2 words, 1 parts.");
        assertThat(source.fileTypes()).contains(".rtf", ".md", ".txt", ".docx", ".odt");
    }

    @Test
    void vaultsAndProjectsAreDocumentsInAZip() throws Exception {
        ByteArrayOutputStream vault = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(vault)) {
            zip.putNextEntry(new ZipEntry("One.md"));
            zip.write("Some words here.".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        DocumentSource obsidian = DocumentSource.obsidian(loc);
        obsidian.read(vault.toByteArray(), "My Vault.zip");
        assertThat(obsidian.suggestedName()).isEqualTo("My Vault");
        assertThat(obsidian.status()).isEqualTo("1 chapters, 4 words, 1 parts.");
        assertThat(obsidian.fileTypes()).contains(".zip");
        assertThatThrownBy(() -> obsidian.read("plain".getBytes(StandardCharsets.UTF_8), "x.zip")).isInstanceOf(IllegalArgumentException.class);
        assertThat(obsidian.invalid("x.zip")).contains("x.zip").contains("Obsidian");

        DocumentSource scrivener = DocumentSource.scrivener(loc);
        assertThatThrownBy(() -> scrivener.read(vault.toByteArray(), "My Novel.scriv.zip")).isInstanceOf(IllegalArgumentException.class);
        assertThat(scrivener.invalid("x.zip")).contains("Scrivener");
        assertThat(scrivener.ready()).isFalse();
    }

    @Test
    void aFileThatIsNotADocumentIsRefusedAndTheOneBeforeStays() throws Exception {
        DocumentSource source = new DocumentSource(loc);
        source.read(docx(1, 3), "a.docx");

        assertThatThrownBy(() -> source.read("text".getBytes(StandardCharsets.UTF_8), "b.docx")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> source.read(new byte[]{1}, "c.pdf")).isInstanceOf(IllegalArgumentException.class);
        assertThat(source.ready()).isTrue();
        assertThat(source.suggestedName()).isEqualTo("a");
        assertThat(source.invalid("b.docx")).contains("b.docx");
    }

    @Test
    void anArchiveHasNoOptions() throws Exception {
        ArchiveSource source = new ArchiveSource(loc, false);
        assertThat(source.options()).isNull();
        assertThat(source.ready()).isFalse();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry("1.md"));
            zip.write("one".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        source.read(out.toByteArray(), "story.zip");

        assertThat(source.ready()).isTrue();
        assertThat(source.suggestedName()).isEqualTo("story");
        assertThat(source.status()).contains("1");
        assertThatThrownBy(() -> source.read("plain".getBytes(StandardCharsets.UTF_8), "x.zip")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chatsHaveTheMergeOptionAndTakeManyFiles() {
        ChatSource source = new ChatSource(loc, com.github.enerccio.marginalia.ui.dialogs.ChatImportFormat.SILLYTAVERN);

        assertThat(source.options()).isNotNull();
        assertThat(source.multiple()).isTrue();
        assertThat(source.ready()).isFalse();
        assertThatThrownBy(() -> source.read("not a chat".getBytes(StandardCharsets.UTF_8), "x.jsonl")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chatsHaveNoPlaceInTheMiddleOfAStory() {
        assertThat(new ChatSource(loc, com.github.enerccio.marginalia.ui.dialogs.ChatImportFormat.SILLYTAVERN).insertable()).isFalse();
        assertThat(new ArchiveSource(loc, true).insertable()).isTrue();
        assertThat(new DocumentSource(loc).insertable()).isTrue();
        // the text of an archive for a book does not speak of a new book
        assertThat(new ArchiveSource(loc, true).description()).isNotEqualTo(new ArchiveSource(loc, false).description());
    }

    private static ChatMessage part(long id, String text) {
        ChatMessage part = new ChatMessage();
        part.setId(id);
        part.setResponse(text);
        return part;
    }

    @Test
    void thePickerPutsPartsAtTheEndBeforeOrAfterAPart() {
        List<ChatMessage> branch = List.of(part(1, "first"), part(2, "second"), part(3, "third"));
        InsertionPicker picker = new InsertionPicker(loc, branch);
        assertThat(picker.isVisible()).isTrue();
        assertThat(picker.insertion()).isEqualTo(ArchiveImportService.Insertion.END);

        picker.choose("AFTER", 1);
        assertThat(picker.insertion().after()).isSameAs(branch.get(1));
        assertThat(picker.insertion().end()).isFalse();

        // before a part is after the part above it, before the first part is the start of the story
        picker.choose("BEFORE", 2);
        assertThat(picker.insertion().after()).isSameAs(branch.get(1));
        picker.choose("BEFORE", 0);
        assertThat(picker.insertion()).isEqualTo(ArchiveImportService.Insertion.START);

        // a part still has to be picked
        picker.choose("AFTER", null);
        assertThat(picker.insertion()).isNull();
    }

    @Test
    void aBookWithoutPartsHasOnlyTheEnd() {
        InsertionPicker picker = new InsertionPicker(loc, List.of());
        assertThat(picker.isVisible()).isFalse();
        assertThat(picker.insertion()).isEqualTo(ArchiveImportService.Insertion.END);
    }

    @Test
    void theDialogOpensOnABookToo() throws Exception {
        login();
        Manuscript created = new Manuscript();
        created.setName(uniqueName("book"));
        Manuscript book = manuscriptService.save(created);
        BookImportDialog.showInto(manuscriptService.find(book), imported -> {
        });
    }

    @Test
    void theDialogOpensWithTheFirstSource() {
        BookImportDialog.show((run, name) -> {
        });
        // nothing to import yet: the dialog only has to build and open without failing
    }
}
