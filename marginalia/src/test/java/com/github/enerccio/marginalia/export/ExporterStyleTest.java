package com.github.enerccio.marginalia.export;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.ExporterService;
import com.github.enerccio.marginalia.domain.service.ExporterService.ExportOptions;
import com.github.enerccio.marginalia.domain.service.ExporterService.Exporter;
import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.domain.styles.PartStyleOption;
import com.github.enerccio.marginalia.domain.styles.PartStyleRegistry;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.dialogs.ProgressBarDialog;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The exporters that have fonts and colors (HTML, EPUB, PDF, Word) take the style the author chooses in the export dialog; without
 * one (or with the plain look) the exports are the ones they always were.
 */
class ExporterStyleTest extends MarginaliaTestBase {

    @Autowired
    private ExporterService exporterService;

    private static ChatMessage message(String text) {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        return message;
    }

    private static List<ChatMessage> story() {
        return List.of(message("# Chapter\n\nThe *first* paragraph with **bold**.\n\nSecond paragraph.\n\n---\n\nAfter the break."));
    }

    private static PartStyle book() {
        PartStyleRegistry registry = new PartStyleRegistry();
        registry.afterPropertiesSet();
        return registry.find(PartStyleRegistry.BOOK);
    }

    private static ExportOptions options(PartStyle style) {
        return new ExportOptions("Book", "Author", true, "en", style);
    }

    private Exporter exporter(String extension) {
        return exporterService.getExporters().stream().filter(e -> e.getFileExtension().equals(extension)).findFirst().orElseThrow();
    }

    private byte[] export(String extension, PartStyle style) throws Exception {
        return exporter(extension).export(options(style), story(), new ProgressBarDialog(false));
    }

    @Test
    void onlyTheFormatsWithFontsSupportStyles() {
        assertThat(exporterService.getExporters().stream().filter(Exporter::supportsStyles).map(Exporter::getFileExtension))
                .containsExactlyInAnyOrder("html", "epub", "pdf", "docx");
    }

    @Test
    void htmlHasTheStylesheetOfTheStyleAndTheTextInsideItsScope() throws Exception {
        String html = new String(export("html", book()), StandardCharsets.UTF_8);

        assertThat(html).contains(".part-style-book .chat-message-markdown {").contains("color: #1a1a1a;").doesNotContain("var(--lumo");
        assertThat(html).contains("<div class=\"part-style-book\"><div class=\"chat-message-markdown\">");
        // the title page is outside of it
        assertThat(html.indexOf("title-page\">")).isLessThan(html.indexOf("<div class=\"part-style-book\">"));
        assertThat(html.indexOf("</div></div>")).isGreaterThan(html.indexOf("After the break."));
    }

    @Test
    void htmlWithoutAStyleHasTheLookOfTheHtmlExportStyle() throws Exception {
        String plain = new String(export("html", null), StandardCharsets.UTF_8);
        String normal = new String(export("html", new PartStyle("normal", "Normal")), StandardCharsets.UTF_8);

        assertThat(plain).contains(".part-style-export-html .chat-message-markdown {")
                .contains("font-family: Georgia, 'Times New Roman', serif;").contains("line-height: 1.6;").contains("content: \"* * *\";")
                .contains("<div class=\"part-style-export-html\"><div class=\"chat-message-markdown\">");
        // the page, the title page and the contents are the format's
        assertThat(plain).contains("main { max-width: 42em;").contains(".title-page {");
        assertThat(normal).as("a style that sets nothing is the browser's look").doesNotContain("part-style-");
    }

    @Test
    void epubWithoutAStyleHasTheLookOfTheEpubExportStyle() throws Exception {
        String css = null;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(export("epub", null)))) {
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (entry.getName().equals("OEBPS/style.css")) {
                    css = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }

        assertThat(css).contains("section.chapter { break-before: page;").contains(".part-style-export-epub .chat-message-markdown {")
                .contains("border-left: 3px solid #999;").contains("content: \"* * *\";");
    }

    @Test
    void eachFormatNamesTheStyleThatIsItsOwnLook() {
        assertThat(exporter("html").getDefaultStyleId()).isEqualTo("export-html");
        assertThat(exporter("epub").getDefaultStyleId()).isEqualTo("export-epub");
        assertThat(exporter("pdf").getDefaultStyleId()).isEqualTo("export-pdf");
        assertThat(exporter("docx").getDefaultStyleId()).isEqualTo("export-docx");
        assertThat(exporter("txt").getDefaultStyleId()).isNull();
    }

    @Test
    void epubHasTheStylesheetWithoutTheCustomCssAndTheTextInsideItsScope() throws Exception {
        PartStyle style = book().copy("mine", "Mine");
        style.setCustomCss("p { color: red; }");

        String css = null;
        String part = null;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(export("epub", style)))) {
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (entry.getName().equals("OEBPS/style.css")) {
                    css = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                } else if (entry.getName().equals("OEBPS/part1.xhtml")) {
                    part = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }

        assertThat(css).contains(".part-style-mine .chat-message-markdown {").doesNotContain("color: red").doesNotContain("var(--");
        assertThat(part).contains("<div class=\"part-style-mine\"><div class=\"chat-message-markdown\">");
    }

    /**
     * The fonts the text of the PDF is drawn in (not the ones merely listed in the resources of a page).
     */
    private static Set<String> fontNames(byte[] pdf) throws Exception {
        Set<String> names = new LinkedHashSet<>();
        try (PDDocument document = Loader.loadPDF(pdf)) {
            new PDFTextStripper() {
                @Override
                protected void writeString(String text, List<TextPosition> positions) {
                    if (!text.isBlank()) {
                        names.add(positions.getFirst().getFont().getName());
                    }
                }
            }.getText(document);
        }
        return names;
    }

    @Test
    void pdfUsesTheFontFamilyOfTheStyle() throws Exception {
        PartStyle sans = new PartStyle("s1", "Sans").set(PartStyleOption.FONT_FAMILY, "Arial, sans-serif")
                .set(PartStyleOption.FONT_SIZE, "16px").set(PartStyleOption.FIRST_LINE_INDENT, "1.5em");

        assertThat(fontNames(export("pdf", sans))).anyMatch(name -> name.contains("LiberationSans"));
        assertThat(fontNames(export("pdf", null))).noneMatch(name -> name.contains("LiberationSans"))
                .anyMatch(name -> name.contains("LiberationSerif"));
        assertThat(fontNames(export("pdf", book()))).anyMatch(name -> name.contains("LiberationSerif"));
    }

    @Test
    void pdfOfAStyleIsALargerPageCountWhenTheTextIsLarger() throws Exception {
        List<ChatMessage> long_ = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            long_.add(message("A paragraph of the book that goes on for a while so that the pages fill up, " + "and on. ".repeat(40)));
        }
        PartStyle large = new PartStyle("s1", "Large").set(PartStyleOption.FONT_SIZE, "24px");

        int plain;
        int larger;
        try (PDDocument a = Loader.loadPDF(exporter("pdf").export(options(null), long_, new ProgressBarDialog(false)));
             PDDocument b = Loader.loadPDF(exporter("pdf").export(options(large), long_, new ProgressBarDialog(false)))) {
            plain = a.getNumberOfPages();
            larger = b.getNumberOfPages();
        }

        assertThat(larger).isGreaterThan(plain);
    }

    @Test
    void docxUsesTheFontTheSizeTheColorsAndTheIndentOfTheStyle() throws Exception {
        PartStyle style = new PartStyle("s1", "Mine").set(PartStyleOption.FONT_FAMILY, "\"Garamond\", serif")
                .set(PartStyleOption.FONT_SIZE, "16px").set(PartStyleOption.TEXT_COLOR, "#102030")
                .set(PartStyleOption.EMPHASIS_COLOR, "#aa0000").set(PartStyleOption.FIRST_LINE_INDENT, "2em")
                .set(PartStyleOption.SCENE_BREAK_TEXT, "~ ~ ~").set(PartStyleOption.TEXT_ALIGN, "left");

        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(export("docx", style)))) {
            XWPFParagraph second = document.getParagraphs().stream().filter(p -> p.getText().startsWith("Second paragraph")).findFirst().orElseThrow();
            XWPFRun run = second.getRuns().getFirst();
            assertThat(run.getFontFamily()).isEqualTo("Garamond");
            assertThat(run.getFontSize()).isEqualTo(12);
            assertThat(run.getColor()).isEqualToIgnoringCase("102030");
            assertThat(second.getFirstLineIndent()).as("2em of 12 pt in twips").isEqualTo(480);
            assertThat(second.getAlignment().name()).isEqualTo("LEFT");

            XWPFParagraph first = document.getParagraphs().stream().filter(p -> p.getText().startsWith("The first paragraph")).findFirst().orElseThrow();
            assertThat(first.getFirstLineIndent()).as("the first paragraph after a heading is not indented").isLessThanOrEqualTo(0);
            assertThat(first.getRuns().stream().filter(XWPFRun::isItalic).findFirst().orElseThrow().getColor()).isEqualToIgnoringCase("AA0000");
            assertThat(document.getParagraphs()).anyMatch(p -> p.getText().equals("~ ~ ~"));
        }
    }

    @Test
    void docxWithoutAStyleKeepsTimesNewRoman() throws Exception {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(export("docx", null)))) {
            XWPFParagraph second = document.getParagraphs().stream().filter(p -> p.getText().startsWith("Second paragraph")).findFirst().orElseThrow();
            assertThat(second.getRuns().getFirst().getFontFamily()).isEqualTo("Times New Roman");
            assertThat(second.getRuns().getFirst().getFontSize()).isEqualTo(12);
            assertThat(second.getRuns().getFirst().getColor()).isNull();
            assertThat(document.getParagraphs()).anyMatch(p -> p.getText().equals("*   *   *"));
        }
    }
}
