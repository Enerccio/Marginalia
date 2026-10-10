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
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ExporterTest extends MarginaliaTestBase {
    private static final String CZECH = "Příliš žluťoučký kůň úpěl ďábelské ódy.";

    @Autowired
    private ExporterService exporterService;

    private static ChatMessage message(String response) {
        ChatMessage message = new ChatMessage();
        message.setResponse(response);
        return message;
    }

    private static List<ChatMessage> story() {
        return List.of(
                message("The *first* paragraph with **bold** and `code`.\n\nSecond paragraph, <script>alert(1)</script> & more.\n\n> A quote\n\n- one\n- two\n\n1. first\n2. second\n\n---\n\n```\nlet x = 1;\n```"),
                message("   "),
                message("# Heading\n\n" + CZECH));
    }

    private static ExportOptions options(boolean headers) {
        return new ExportOptions("Tom & Jerry <1>", "Some Author", headers, "cs");
    }

    private byte[] export(Exporter exporter, boolean headers) throws Exception {
        return exporter.export(options(headers), story(), new ProgressBarDialog(false));
    }

    private Exporter exporter(String extension) {
        return exporterService.getExporters().stream()
                .filter(e -> e.getFileExtension().equals(extension)).findFirst().orElseThrow();
    }

    @Test
    void allFormatsAreRegistered() {
        assertThat(exporterService.getExporters()).extracting(Exporter::getFileExtension)
                .containsExactlyInAnyOrder("txt", "html", "docx", "pdf", "epub");
    }

    @Test
    void txtIsPlainText() throws Exception {
        String text = new String(export(exporter("txt"), true), StandardCharsets.UTF_8);

        assertThat(text).startsWith("Tom & Jerry <1>\n\nSome Author\n\n");
        assertThat(text).contains("The first paragraph with bold and code.")
                .contains("> A quote").contains("• one").contains("2. second").contains("* * *")
                .contains("    let x = 1;").contains(CZECH)
                .doesNotContain("**").doesNotContain("`").doesNotContain("# Heading");
    }

    @Test
    void txtWithoutHeaders() throws Exception {
        String text = new String(export(exporter("txt"), false), StandardCharsets.UTF_8);

        assertThat(text).startsWith("The first paragraph").doesNotContain("Some Author");
    }

    @Test
    void htmlIsStyledAndEscaped() throws Exception {
        String html = new String(export(exporter("html"), true), StandardCharsets.UTF_8);

        assertThat(html).contains("<style>").contains("<title>Tom &amp; Jerry &lt;1&gt;</title>")
                .contains("<em>first</em>").contains("<strong>bold</strong>").contains("<blockquote>")
                .contains("&lt;script&gt;").contains("title-page").contains(CZECH)
                .doesNotContain("<script>");
    }

    @Test
    void docxCanBeReadBack() throws Exception {
        byte[] data = export(exporter("docx"), true);

        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(data));
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            String text = extractor.getText();
            assertThat(text).contains("Tom & Jerry <1>").contains("Some Author").contains("The first paragraph with bold")
                    .contains("let x = 1;").contains("Heading").contains(CZECH);
            assertThat(document.getProperties().getCoreProperties().getCreator()).isEqualTo("Some Author");
        }
    }

    @Test
    void pdfKeepsUnicodeText() throws Exception {
        byte[] data = export(exporter("pdf"), true);

        try (PDDocument document = Loader.loadPDF(data)) {
            // title page + at least one page of the story
            assertThat(document.getNumberOfPages()).isGreaterThanOrEqualTo(2);
            String text = new PDFTextStripper().getText(document);
            assertThat(text).contains("Tom & Jerry <1>").contains("Some Author").contains("let x = 1;")
                    .contains("Příliš žluťoučký");
        }
    }

    @Test
    void epubIsValidPackage() throws Exception {
        byte[] data = export(exporter("epub"), true);

        List<String> names = new ArrayList<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                names.add(entry.getName());
                byte[] content = zip.readAllBytes();
                if (entry.getName().equals("mimetype")) {
                    assertThat(entry.getMethod()).isEqualTo(ZipEntry.STORED);
                    assertThat(new String(content, StandardCharsets.US_ASCII)).isEqualTo("application/epub+zip");
                } else if (entry.getName().endsWith("xhtml") || entry.getName().endsWith("xml") || entry.getName().endsWith("opf")) {
                    // all documents must be well formed XML
                    var factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                    var document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(content));
                    assertThat(document.getDocumentElement()).isNotNull();
                    if (entry.getName().equals("OEBPS/part1.xhtml")) {
                        assertThat(new String(content, StandardCharsets.UTF_8)).contains(CZECH).contains("&lt;script&gt;");
                    }
                }
            }
        }
        assertThat(names).first().isEqualTo("mimetype");
        assertThat(names).contains("META-INF/container.xml", "OEBPS/content.opf", "OEBPS/nav.xhtml",
                "OEBPS/title.xhtml", "OEBPS/part1.xhtml");
    }

    @Test
    void epubSplitsLongStory() throws Exception {
        List<ChatMessage> messages = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            messages.add(message(("Lorem ipsum dolor sit amet. ".repeat(150) + "\n\n").repeat(3)));
        }
        byte[] data = exporter("epub").export(options(false), messages, new ProgressBarDialog(false));

        List<String> names = new ArrayList<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                names.add(entry.getName());
            }
        }
        assertThat(names).contains("OEBPS/part1.xhtml", "OEBPS/part2.xhtml").doesNotContain("OEBPS/title.xhtml");
    }

    @Test
    void emptyStoryProducesFile() throws Exception {
        for (Exporter exporter : exporterService.getExporters()) {
            byte[] data = exporter.export(options(false), List.of(), new ProgressBarDialog(false));
            // plain text of nothing is nothing, every other format is a container with its own structure
            if (!exporter.getFileExtension().equals("txt")) {
                assertThat(data).as(exporter.getName()).isNotEmpty();
            }
        }
    }
}
