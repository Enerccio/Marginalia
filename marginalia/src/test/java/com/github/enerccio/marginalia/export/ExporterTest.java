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
                .containsExactlyInAnyOrder("txt", "md", "html", "docx", "pdf", "epub");
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
    void markdownKeepsTheSourceText() throws Exception {
        String text = new String(export(exporter("md"), true), StandardCharsets.UTF_8);

        assertThat(text).startsWith("# Tom & Jerry <1>\n\n*Some Author*\n\nThe *first* paragraph with **bold** and `code`.")
                .contains("> A quote").contains("- one").contains("1. first").contains("---")
                .contains("```\nlet x = 1;\n```").contains("<script>alert(1)</script>")
                .contains("# Heading\n\n" + CZECH).endsWith(CZECH + "\n");
        // the empty message adds nothing
        assertThat(text).doesNotContain("\n\n\n");
    }

    @Test
    void markdownWithoutHeaders() throws Exception {
        String text = new String(export(exporter("md"), false), StandardCharsets.UTF_8);

        assertThat(text).startsWith("The *first* paragraph").doesNotContain("Some Author");
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

    private static List<ChatMessage> chapters() {
        return List.of(
                message("Prologue without a heading."),
                message("# Chapter One\n\nText of the first chapter."),
                message("More of the first chapter."),
                message("## The *Second* Chapter\n\nText & more."),
                message("Plain ending."));
    }

    private byte[] exportChapters(Exporter exporter, boolean headers) throws Exception {
        return exporter.export(options(headers), chapters(), new ProgressBarDialog(false));
    }

    private static String entry(byte[] epub, String name) throws Exception {
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(epub))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.getName().equals(name)) {
                    return new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }
        throw new AssertionError(name + " is not in the file");
    }

    @Test
    void htmlHasLinkedContents() throws Exception {
        String html = new String(exportChapters(exporter("html"), true), StandardCharsets.UTF_8);

        assertThat(html).contains("<nav class=\"contents\">")
                .contains("<a href=\"#chapter_1\">Chapter One</a>")
                // the title is shown as it is written, like the Chapter Marker does in the sidebar
                .contains("<a href=\"#chapter_2\">The *Second* Chapter</a>")
                .contains("<section id=\"chapter_1\">").contains("<section id=\"chapter_2\">")
                .doesNotContain("chapter_3");
        // title page, then contents, then the story
        assertThat(html.indexOf("title-page\">")).isLessThan(html.indexOf("<nav class=\"contents\">"));
        assertThat(html.indexOf("<nav class=\"contents\">")).isLessThan(html.indexOf("Prologue"));
    }

    @Test
    void storyWithoutHeadingsHasNoContents() throws Exception {
        List<ChatMessage> plain = List.of(message("Just a text."), message("And another one."));
        for (String extension : List.of("html", "epub")) {
            String content = new String(exporter(extension).export(options(true), plain, new ProgressBarDialog(false)),
                    StandardCharsets.ISO_8859_1);
            assertThat(content).as(extension).doesNotContain("chapter_1").doesNotContain("class=\"contents\"");
        }
        byte[] pdf = exporter("pdf").export(options(false), plain, new ProgressBarDialog(false));
        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertThat(new PDFTextStripper().getText(document)).doesNotContain("Contents");
        }
    }

    @Test
    void epubNavigationLinksToChapters() throws Exception {
        byte[] data = exportChapters(exporter("epub"), true);

        String nav = entry(data, "OEBPS/nav.xhtml");
        assertThat(nav).contains("epub:type=\"toc\"").contains("<a href=\"part1.xhtml#chapter_1\">Chapter One</a>")
                .contains("<a href=\"part1.xhtml#chapter_2\">The *Second* Chapter</a>");
        assertThat(entry(data, "OEBPS/part1.xhtml")).contains("<section class=\"chapter\" id=\"chapter_1\">").contains("<section class=\"chapter\" id=\"chapter_2\">");
        // chapters start on a new page
        assertThat(entry(data, "OEBPS/style.css")).contains("section.chapter { break-before: page;");
        // the navigation is also read as a page, right after the title page
        assertThat(entry(data, "OEBPS/content.opf")).containsSubsequence("<itemref idref=\"c0\"/>", "<itemref idref=\"nav\"/>", "<itemref idref=\"c1\"/>");
    }

    @Test
    void epubNavigationFollowsSplitParts() throws Exception {
        List<ChatMessage> messages = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            messages.add(message("# Chapter " + (i + 1) + "\n\n" + ("Lorem ipsum dolor sit amet. ".repeat(150) + "\n\n").repeat(3)));
        }
        byte[] data = exporter("epub").export(options(false), messages, new ProgressBarDialog(false));

        String nav = entry(data, "OEBPS/nav.xhtml");
        assertThat(nav).contains("<a href=\"part1.xhtml#chapter_1\">Chapter 1</a>");
        for (int chapter = 1; chapter <= 30; chapter++) {
            java.util.regex.Matcher link = java.util.regex.Pattern.compile("href=\"(part\\d+\\.xhtml)#chapter_" + chapter + "\"").matcher(nav);
            assertThat(link.find()).as("chapter " + chapter).isTrue();
            assertThat(entry(data, "OEBPS/" + link.group(1))).contains("<section class=\"chapter\" id=\"chapter_" + chapter + "\">");
        }
        assertThat(nav).doesNotContain("part1.xhtml#chapter_30");
    }

    @Test
    void pdfHasLinkedContents() throws Exception {
        byte[] data = exportChapters(exporter("pdf"), true);

        try (PDDocument document = Loader.loadPDF(data)) {
            // title page, contents, story
            assertThat(document.getNumberOfPages()).isGreaterThanOrEqualTo(3);
            String text = new PDFTextStripper().getText(document);
            assertThat(text).contains("Contents").contains("Chapter One").contains("The *Second* Chapter");

            var links = document.getPage(1).getAnnotations().stream()
                    .filter(a -> a instanceof org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink).toList();
            // the title and the page number of every entry
            assertThat(links).hasSize(4);
            var names = document.getDocumentCatalog().getNames().getDests().getNames();
            assertThat(names).containsKeys("chapter_1", "chapter_2");

            var outline = document.getDocumentCatalog().getDocumentOutline();
            assertThat(outline).isNotNull();
            assertThat(outline.getFirstChild().getTitle()).isEqualTo("Chapter One");
        }
    }

    @Test
    void pdfContentsShowPagesOfChapters() throws Exception {
        List<ChatMessage> messages = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            messages.add(message("# Chapter " + i + "\n\n" + ("Lorem ipsum dolor sit amet. ".repeat(60) + "\n\n").repeat(2)));
        }
        byte[] data = exporter("pdf").export(options(true), messages, new ProgressBarDialog(false));

        try (PDDocument document = Loader.loadPDF(data)) {
            var destinations = document.getDocumentCatalog().getNames().getDests().getNames();
            var stripper = new PDFTextStripper();
            stripper.setStartPage(2);
            stripper.setEndPage(2);
            String contents = stripper.getText(document);
            assertThat(contents).startsWith("Contents");

            int previous = 0;
            for (int chapter = 1; chapter <= 12; chapter++) {
                var destination = (org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination)
                        destinations.get("chapter_" + chapter);
                int page = destination.retrievePageNumber() + 1;
                assertThat(page).as("chapter " + chapter).isGreaterThan(2);
                assertThat(contents).as("chapter " + chapter).containsPattern("Chapter " + chapter + " \\.*\\s*" + page + "\\s");
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                // the chapter starts its page, so the page has no part of the chapter before
                assertThat(java.util.regex.Pattern.compile("Chapter \\d+").matcher(stripper.getText(document)).results()
                        .map(java.util.regex.MatchResult::group)).as("chapter " + chapter).containsExactly("Chapter " + chapter);
                assertThat(page).isGreaterThan(previous);
                previous = page;
            }
            // page numbers in the footer, but not on the title page
            stripper.setStartPage(1);
            stripper.setEndPage(1);
            assertThat(stripper.getText(document).trim()).doesNotEndWith("1");
            stripper.setStartPage(3);
            stripper.setEndPage(3);
            assertThat(stripper.getText(document).trim()).endsWith("3");
        }
    }

    @Test
    void docxHasLinkedContents() throws Exception {
        byte[] data = exportChapters(exporter("docx"), true);

        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(data))) {
            String xml = document.getDocument().xmlText();
            assertThat(xml).contains("w:anchor=\"chapter_1\"").contains("w:anchor=\"chapter_2\"")
                    .contains("w:name=\"chapter_1\"").contains("w:name=\"chapter_2\"");
            // the title page, the contents and every chapter start a page
            for (String chapter : List.of("chapter_1", "chapter_2")) {
                var start = document.getParagraphs().stream()
                        .filter(p -> p.getCTP().xmlText().contains("w:name=\"" + chapter + "\"")).findFirst().orElseThrow();
                assertThat(start.isPageBreak()).as(chapter).isTrue();
            }
            assertThat(document.getParagraphs().stream().filter(org.apache.poi.xwpf.usermodel.XWPFParagraph::isPageBreak).count()).isEqualTo(4);
            assertThat(xml).contains("PAGEREF chapter_1").contains("PAGEREF chapter_2");
            assertThat(document.getFooterList()).hasSize(1);
            assertThat(document.getFooterList().getFirst().getParagraphs().getFirst().getCTP().xmlText()).contains("PAGE");
            assertThat(new XWPFWordExtractor(document).getText()).contains("Contents").contains("Chapter One");
            // every paragraph has at most one set of properties, Word refuses the file otherwise
            document.getParagraphs().forEach(p ->
                    assertThat(org.apache.commons.lang3.StringUtils.countMatches(p.getCTP().xmlText(), "<w:pPr>")).isLessThanOrEqualTo(1));
        }
    }

    @Test
    void emptyStoryProducesFile() throws Exception {
        for (Exporter exporter : exporterService.getExporters()) {
            byte[] data = exporter.export(options(false), List.of(), new ProgressBarDialog(false));
            // plain text and Markdown of nothing are nothing, every other format is a container with its own structure
            if (!List.of("txt", "md").contains(exporter.getFileExtension())) {
                assertThat(data).as(exporter.getName()).isNotEmpty();
            }
        }
    }
}
