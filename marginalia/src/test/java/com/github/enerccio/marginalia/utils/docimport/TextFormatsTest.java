package com.github.enerccio.marginalia.utils.docimport;

import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Reading Markdown, plain text and RTF files.
 */
class TextFormatsTest {

    private static TextDocument read(DocumentFormat format, String text) {
        return format.read(text.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void formatsAreTakenFromTheExtension() {
        assertThat(DocumentFormat.of("a.rtf")).isEqualTo(DocumentFormat.RTF);
        assertThat(DocumentFormat.of("a.MD")).isEqualTo(DocumentFormat.MARKDOWN);
        assertThat(DocumentFormat.of("a.markdown")).isEqualTo(DocumentFormat.MARKDOWN);
        assertThat(DocumentFormat.of("a.txt")).isEqualTo(DocumentFormat.TEXT);
        assertThat(DocumentFormat.MARKDOWN.baseName("My Book.md")).isEqualTo("My Book");
        assertThat(DocumentFormat.fileTypes()).contains(".rtf", ".md", ".txt", "text/plain");
    }

    // ---- Markdown

    @Test
    void markdownHeadingsAndParagraphsAreKeptAsWritten() {
        TextDocument document = read(DocumentFormat.MARKDOWN, "# One\n\nShe said *no*.\nAnd left.\n\n## Deeper ##\n\n- a\n- b\n\nSetext\n======\n\nText");
        assertThat(document.blocks()).containsExactly(
                new TextBlock(1, "One"),
                new TextBlock(0, "She said *no*.\nAnd left."),
                new TextBlock(2, "Deeper"),
                new TextBlock(0, "- a\n- b"),
                new TextBlock(1, "Setext"),
                new TextBlock(0, "Text"));
    }

    @Test
    void markdownCodeFenceIsOneBlockAndHashesInItAreNoHeadings() {
        TextDocument document = read(DocumentFormat.MARKDOWN, "Before\n\n```\n# not a heading\n\nstill code\n```\n\nAfter");
        assertThat(document.blocks()).extracting(TextBlock::level).containsOnly(0);
        assertThat(document.blocks()).hasSize(3);
        assertThat(document.blocks().get(1).text()).contains("# not a heading").contains("still code");
    }

    @Test
    void markdownThematicBreakStaysAScenebreak() {
        TextDocument document = read(DocumentFormat.MARKDOWN, "First\n\n---\n\nSecond");
        assertThat(document.blocks()).extracting(TextBlock::text).containsExactly("First", "---", "Second");
    }

    @Test
    void markdownHeaderGivesTheTitleAndIsLeftOut() {
        TextDocument document = read(DocumentFormat.MARKDOWN, "---\ntitle: \"The Book\"\nauthor: me\n---\n\n# One\n\nText");
        assertThat(document.title()).isEqualTo("The Book");
        assertThat(document.blocks()).extracting(TextBlock::text).containsExactly("One", "Text");
    }

    @Test
    void markdownLoneTopHeadingIsTheTitleWhenChaptersAreSecondLevel() {
        TextDocument document = read(DocumentFormat.MARKDOWN, "# The Book\n\n## One\n\nText\n\n## Two\n\nMore");
        assertThat(document.title()).isEqualTo("The Book");
        assertThat(document.headingLevels()).containsExactly(2);
        // two top headings are chapters
        assertThat(read(DocumentFormat.MARKDOWN, "# A\n\nx\n\n# B\n\ny").title()).isNull();
        // a single one without anything below it is a chapter
        assertThat(read(DocumentFormat.MARKDOWN, "# A\n\nx").title()).isNull();
    }

    @Test
    void markdownFileSplitsIntoChapterParts() {
        StringBuilder text = new StringBuilder();
        for (int chapter = 1; chapter <= 2; chapter++) {
            text.append("# Chapter ").append(chapter).append("\n\n");
            for (int p = 0; p < 10; p++) {
                text.append("word ".repeat(100).strip()).append("\n\n");
            }
        }
        PartSplitter.Result result = PartSplitter.split(read(DocumentFormat.MARKDOWN, text.toString()), 1, 500);
        assertThat(result.chapters()).isEqualTo(2);
        assertThat(result.parts()).hasSize(4);
        assertThat(result.parts().get(0)).startsWith("# Chapter 1");
        assertThat(result.parts().get(2)).startsWith("# Chapter 2");
    }

    // ---- plain text

    @Test
    void textParagraphsAreSeparatedByBlankLinesAndWrappedLinesAreJoined() {
        TextDocument document = read(DocumentFormat.TEXT, (char) 0xFEFF + "First line\nsecond line.\n\n\n  Next paragraph.\n");
        assertThat(document.blocks()).extracting(TextBlock::text).containsExactly("First line second line.", "Next paragraph.");
    }

    @Test
    void textWithoutBlankLinesHasAParagraphOnEachLine() {
        TextDocument document = read(DocumentFormat.TEXT, "One.\r\nTwo.\r\nThree.");
        assertThat(document.blocks()).extracting(TextBlock::text).containsExactly("One.", "Two.", "Three.");
    }

    @Test
    void textMarkdownCharactersAreEscaped() {
        TextDocument document = read(DocumentFormat.TEXT, "* * *\n\n2 * 3 = 6 and snake_case\n\n# not a heading");
        assertThat(document.blocks()).extracting(TextBlock::level).containsOnly(0);
        assertThat(document.blocks().get(0).text()).isEqualTo("\\* \\* \\*");
        assertThat(document.blocks().get(1).text()).isEqualTo("2 \\* 3 = 6 and snake\\_case");
        assertThat(document.blocks().get(2).text()).isEqualTo("\\# not a heading");
    }

    @Test
    void textChapterLinesAreHeadings() {
        TextDocument document = read(DocumentFormat.TEXT, "Prologue\n\nIt began.\n\nCHAPTER 12\n\nText.\n\nChapter IV: The Return\n\n"
                + "Part of the plan was lost.\n\nBook two\n\nChapter 3.\n\nChapter 3 was long and hard.");
        assertThat(document.blocks()).filteredOn(TextBlock::isHeading).extracting(TextBlock::text)
                .containsExactly("Prologue", "CHAPTER 12", "Chapter IV: The Return", "Book two", "Chapter 3.");
    }

    @Test
    void textInTheWindowsCodePageIsDecoded() {
        TextDocument document = DocumentFormat.TEXT.read("Příliš žluťoučký kůň".getBytes(Charset.forName("windows-1250")));
        // not valid UTF-8, read as Windows-1252: the bytes are kept as letters, the text is not lost
        assertThat(document.blocks()).hasSize(1);
        assertThat(DocumentFormat.TEXT.read("Žluťoučký kůň".getBytes(StandardCharsets.UTF_8)).blocks().getFirst().text()).isEqualTo("Žluťoučký kůň");
        assertThat(DocumentFormat.TEXT.read("café".getBytes(Charset.forName("windows-1252"))).blocks().getFirst().text()).isEqualTo("café");
    }

    @Test
    void binaryDataIsNotText() {
        assertThatThrownBy(() -> DocumentFormat.TEXT.read(new byte[]{'a', 0, 'b'})).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DocumentFormat.TEXT.read("  \n \n".getBytes(StandardCharsets.UTF_8))).isInstanceOf(IllegalArgumentException.class);
    }

    // ---- RTF

    private static final String RTF_HEAD = "{\\rtf1\\ansi\\ansicpg1250\\deff0{\\fonttbl{\\f0 Times;}}{\\colortbl;\\red0\\green0\\blue0;}"
            + "{\\stylesheet{\\s0 Normal;}{\\s1\\sbasedon0\\snext0 heading 1;}{\\s2\\sbasedon0 heading 2;}{\\s3 Title;}"
            + "{\\s4\\outlinelvl1 Chapterish;}}{\\info{\\title Ignored}}";

    @Test
    void rtfHeadingsParagraphsEmphasisAndTitle() {
        String rtf = RTF_HEAD
                + "\\pard\\s3 The Book\\par"
                + "\\pard\\s1\\b Chapter One\\b0\\par"
                + "\\pard She said {\\i no} and {\\b\\i very} loudly, 2 * 3.\\par"
                + "\\pard\\s2 Deeper\\par"
                + "\\pard\\s4 By outline\\par"
                + "\\pard\\outlinelvl2 Direct outline\\par"
                + "}";
        TextDocument document = read(DocumentFormat.RTF, rtf);
        assertThat(document.title()).isEqualTo("The Book");
        assertThat(document.blocks()).containsExactly(
                new TextBlock(1, "Chapter One"),
                new TextBlock(0, "She said *no* and ***very*** loudly, 2 \\* 3."),
                new TextBlock(2, "Deeper"),
                new TextBlock(2, "By outline"),
                new TextBlock(3, "Direct outline"));
    }

    @Test
    void rtfLettersFromCodePageAndUnicode() {
        // \ansicpg1250: \'e8 is č, \'f8 is ř; u8212 is an em dash with the fallback ? that is skipped
        String rtf = RTF_HEAD + "\\pard \\'e8esk\\'e1 \\u8212? p\\u345?\\'edb\\u283?h \\u-10179?\\u-8704? \\{x\\} a\\~b\\emdash c\\par}";
        TextDocument document = read(DocumentFormat.RTF, rtf);
        assertThat(document.blocks().getFirst().text()).isEqualTo("česká — příběh 😀 {x} a b—c");
    }

    @Test
    void rtfLeavesOutDestinationsAndPictures() {
        String rtf = RTF_HEAD + "{\\header Page header\\par}{\\*\\generator Word;}\\pard Body {\\footnote note text}{\\pict\\pngblip 89504e47{}0d0a}"
                + "{\\field{\\*\\fldinst HYPERLINK \"x\"}{\\fldrslt link}} end\\par}";
        TextDocument document = read(DocumentFormat.RTF, rtf);
        assertThat(document.blocks()).extracting(TextBlock::text).containsExactly("Body link end");
    }

    @Test
    void rtfTableCellsAreParagraphs() {
        String rtf = "{\\rtf1\\ansi\\trowd\\pard\\intbl One\\cell Two\\cell\\row}";
        assertThat(read(DocumentFormat.RTF, rtf).blocks()).extracting(TextBlock::text).containsExactly("One", "Two");
    }

    @Test
    void rtfRefusesOtherData() {
        assertThatThrownBy(() -> read(DocumentFormat.RTF, "Just text")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> read(DocumentFormat.RTF, "{\\rtf1 }")).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no text");
    }
}
