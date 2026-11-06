package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.ExporterService;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import com.github.enerccio.marginalia.domain.service.ExporterService.Exporter;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.dialogs.ProgressBarDialog;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A text with Markdown footnotes (<code>[^1]</code> and <code>[^1]: the note</code>) in every format: the authors' own, and the ones the
 * Conlang Translator writes into the parts it exports.
 */
class ExporterFootnotesTest extends MarginaliaTestBase {

    private static final String TEXT = "Mara said, \"Da ja-se vek.\"[^7] and left.\n\n[^7]: Mara: I see the enemy.";

    @Autowired
    private ExporterService exporterService;

    private byte[] export(String extension) throws Exception {
        return export(extension, null);
    }

    private byte[] export(String extension, String name) throws Exception {
        Exporter exporter = exporterService.getExporters().stream()
                .filter(e -> e.getFileExtension().equals(extension) && (name == null ? !e.getName().contains("Archive") : e.getName().equals(name)))
                .findFirst().orElseThrow();
        ChatMessage message = new ChatMessage();
        message.setResponse(TEXT);
        return exporter.export(new ExportOptions("Book", "Author", false, "en"), List.of(message), new ProgressBarDialog(false));
    }

    @Test
    void markdownKeepsTheNotes() throws Exception {
        assertThat(new String(export("md"), StandardCharsets.UTF_8)).contains("\"[^7] and left.").contains("[^7]: Mara: I see the enemy.");
    }

    @Test
    void txtPrintsTheNoteAfterTheText() throws Exception {
        String text = new String(export("txt"), StandardCharsets.UTF_8);

        assertThat(text).contains("Mara said, \"Da ja-se vek.\"[7] and left.").contains("[7] Mara: I see the enemy.");
        assertThat(text.indexOf("[7] Mara")).isGreaterThan(text.indexOf("and left."));
    }

    @Test
    void htmlLinksTheMarkAndTheNote() throws Exception {
        String html = new String(export("html"), StandardCharsets.UTF_8);

        assertThat(html).contains("href=\"#fn-7\"").contains("id=\"fn-7\"").contains("Mara: I see the enemy.");
    }

    @Test
    void epubHasTheNote() throws Exception {
        StringBuilder all = new StringBuilder();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(export("epub")))) {
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (entry.getName().endsWith(".xhtml")) {
                    byte[] page = zip.readAllBytes();
                    // the EPUB is XML: a page that does not parse (an attribute without a value) is a broken book
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                    factory.newDocumentBuilder().parse(new ByteArrayInputStream(page));
                    all.append(new String(page, StandardCharsets.UTF_8));
                }
            }
        }

        assertThat(all.toString()).contains("href=\"#fn-7\"").contains("Mara: I see the enemy.");
    }

    @Test
    void ao3KeepsTheNoteWithoutWhatAo3WouldCut() throws Exception {
        String html = new String(export("html", "HTML for Archive of Our Own"), StandardCharsets.UTF_8);

        assertThat(html).contains("<sup><a href=\"#fn-7\">1</a></sup>").contains("Mara: I see the enemy.")
                .doesNotContain("data-footnote").doesNotContain("footnote-backref").doesNotContain("id=\"fn-7\"");
    }

    @Test
    void docxHasTheNote() throws Exception {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(export("docx")));
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            assertThat(extractor.getText()).contains("and left.").contains("[7]").contains("Mara: I see the enemy.");
        }
    }

    @Test
    void pdfHasTheNote() throws Exception {
        try (PDDocument document = Loader.loadPDF(export("pdf"))) {
            assertThat(new PDFTextStripper().getText(document)).contains("and left.").contains("Mara: I see the enemy.");
        }
    }
}
