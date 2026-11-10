package com.github.enerccio.marginalia.ui.dialogs.bookimport;

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
        ArchiveSource source = new ArchiveSource(loc);
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
    void theDialogOpensWithTheFirstSource() {
        BookImportDialog.show((run, name) -> {
        });
        // nothing to import yet: the dialog only has to build and open without failing
    }
}
