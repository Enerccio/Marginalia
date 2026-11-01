package com.github.enerccio.marginalia.styles;

import com.github.enerccio.marginalia.domain.styles.PartStyle;
import com.github.enerccio.marginalia.domain.styles.PartStyleOption;
import com.github.enerccio.marginalia.domain.styles.PartStyleRegistry;
import com.github.enerccio.marginalia.domain.styles.PrintStyle;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * A text style worked out for paper: the values of CSS that formats without CSS (PDF, Word) can use, and the stylesheet for HTML and
 * EPUB without the variables and the dark theme of the application.
 */
class PrintStyleTest {

    private static PartStyle book() {
        PartStyleRegistry registry = new PartStyleRegistry();
        registry.afterPropertiesSet();
        return registry.find(PartStyleRegistry.BOOK);
    }

    @Test
    void variablesAndColorPairsAreWorkedOutForAWhitePage() {
        assertThat(PartStyle.resolveForPrint("var(--lumo-primary-color, #7e57c2)")).isEqualTo("#7e57c2");
        assertThat(PartStyle.resolveForPrint("light-dark(#5e35b1, #d1c4e9)")).isEqualTo("#5e35b1");
        assertThat(PartStyle.resolveForPrint("var(--lumo-header-text-color, light-dark(#000000, #ffffff))")).isEqualTo("#000000");
        assertThat(PartStyle.resolveForPrint("var(--lumo-body-text-color, #e0e0e0)")).as("light text of the dark theme is not for paper").isEqualTo("#1a1a1a");
        assertThat(PartStyle.resolveForPrint("var(--unknown)")).isEmpty();
        assertThat(PartStyle.resolveForPrint("var(--lumo-contrast-5pct, rgba(255, 255, 255, 0.03))")).isEqualTo("rgba(0, 0, 0, 0.03)");
        assertThat(PartStyle.resolveForPrint("var(--lumo-contrast-40pct, #888888)")).isEqualTo("#888888");
        assertThat(PartStyle.resolveForPrint("1.2rem")).isEqualTo("1.2rem");
    }

    @Test
    void thePrintStylesheetOfTheBookStyleHasNoVariables() {
        String css = book().toPrintCss(true);

        assertThat(css).doesNotContain("var(--").doesNotContain("light-dark(");
        assertThat(css).contains("color: #1a1a1a;").contains("border-left: 3px solid #7e57c2;").contains("color: #5e35b1;")
                .contains("font-size: 1.04rem;");
        // the screen stylesheet keeps them
        assertThat(book().toCss()).contains("var(--lumo-body-text-color").contains("light-dark(#5e35b1, #d1c4e9)");
    }

    @Test
    void theCustomCssComesWithThePrintStylesheetOnlyWhenAsked() {
        PartStyle style = new PartStyle("s1", "Mine").set(PartStyleOption.FONT_SIZE, "1rem");
        style.setCustomCss("p { color: red; }");

        assertThat(style.toPrintCss(true)).contains("p { color: red; }");
        assertThat(style.toPrintCss(false)).doesNotContain("color: red");
    }

    @Test
    void theBackgroundIsAColorOfThePaperOnlyWhenItIsOne() {
        assertThat(PrintStyle.of(new PartStyle("s1", "A").set(PartStyleOption.BACKGROUND_COLOR, "#fbf7ee")).getBackgroundColor()).isEqualTo(0xFBF7EE);
        assertThat(PrintStyle.of(new PartStyle("s2", "B").set(PartStyleOption.BACKGROUND_COLOR, "var(--page, #123)")).getBackgroundColor()).isEqualTo(0x112233);
        assertThat(PrintStyle.of(new PartStyle("s3", "C").set(PartStyleOption.BACKGROUND_COLOR, "ivory")).getBackgroundColor()).isNull();
        assertThat(PrintStyle.of(null).getBackgroundColor()).isNull();
    }

    @Test
    void theNewOptionsMakeTheirRules() {
        PartStyle style = new PartStyle("s1", "Novel");
        style.set(PartStyleOption.FIRST_LINE_INDENT, "1.5em").set(PartStyleOption.DROP_CAP, "true")
                .set(PartStyleOption.TEXT_WIDTH, "34em").set(PartStyleOption.BACKGROUND_COLOR, "#fbf7ee")
                .set(PartStyleOption.LINK_COLOR, "#225599").set(PartStyleOption.WORD_SPACING, "0.05em")
                .set(PartStyleOption.CODE_FONT_FAMILY, "\"Fira Code\", monospace").set(PartStyleOption.HEADING_SIZE, "2rem");

        String css = style.toCss();

        assertThat(css).contains("text-indent: 1.5em !important;");
        assertThat(css).contains(".part-style-s1 .chat-message-markdown h1 + p,\n").contains("hr + p {\n    text-indent: 0 !important;");
        assertThat(css).contains("h1 + p::first-letter").contains("float: left;");
        assertThat(css).contains("max-width: 34em !important;").contains("margin-left: auto;");
        assertThat(css).contains("background-color: #fbf7ee;").contains("word-spacing: 0.05em;");
        assertThat(css).contains("a {\n    color: #225599;");
        assertThat(css).contains("font-family: \"Fira Code\", monospace;");
        assertThat(css).contains("font-size: 2rem;").contains("font-size: calc(2rem * 0.89);").contains("font-size: calc(2rem * 0.8);");
    }

    @Test
    void anUnstyledOrNormalPrintStyleIsTheLookTheExportersAlwaysHad() {
        for (PrintStyle print : new PrintStyle[]{PrintStyle.of(null), PrintStyle.of(new PartStyle("n", "Normal"))}) {
            assertThat(print.isStyled()).isFalse();
            assertThat(print.getFamily()).isEqualTo(PrintStyle.Family.SERIF);
            assertThat(print.getBodySize()).isEqualTo(12f);
            assertThat(print.getLineHeight()).isEqualTo(1.4f);
            assertThat(print.isLineHeightSet()).isFalse();
            assertThat(print.getAlign()).isEqualTo(PrintStyle.Align.JUSTIFY);
            assertThat(print.getParagraphSpacing()).isEqualTo(8f);
            assertThat(print.getFirstLineIndent()).isZero();
            assertThat(print.getTextColor()).isNull();
            assertThat(print.getHeadingAlign()).isEqualTo(PrintStyle.Align.LEFT);
            assertThat(print.getHeadingScale()).isEqualTo(2f);
            assertThat(print.getSceneBreak()).isEqualTo("*   *   *");
        }
    }

    @Test
    void theBookStyleInPointsAndRgb() {
        PrintStyle print = PrintStyle.of(book());

        assertThat(print.isStyled()).isTrue();
        assertThat(print.getFamily()).isEqualTo(PrintStyle.Family.SERIF);
        assertThat(print.getFontName()).isEqualTo("Garamond");
        assertThat(print.getBodySize()).isCloseTo(12.48f, within(0.01f));
        assertThat(print.getLineHeight()).isEqualTo(1.8f);
        assertThat(print.isLineHeightSet()).isTrue();
        assertThat(print.getAlign()).isEqualTo(PrintStyle.Align.JUSTIFY);
        assertThat(print.getParagraphSpacing()).isCloseTo(0.85f * 12.48f, within(0.01f));
        assertThat(print.getTextColor()).isEqualTo(0x1a1a1a);
        assertThat(print.getEmphasisColor()).isEqualTo(0x5e35b1);
        assertThat(print.getHeadingColor()).isEqualTo(0x000000);
        assertThat(print.getHeadingAlign()).isEqualTo(PrintStyle.Align.CENTER);
        assertThat(print.isHeadingSmallCaps()).isTrue();
        assertThat(print.getSceneBreak()).isEqualTo("•  •  •");
        assertThat(print.getSceneBreakColor()).isEqualTo(0x888888);
    }

    @Test
    void unitsFontsAndColorsAreParsed() {
        PartStyle style = new PartStyle("s1", "Mine");
        style.set(PartStyleOption.FONT_SIZE, "14px").set(PartStyleOption.FIRST_LINE_INDENT, "2em")
                .set(PartStyleOption.PARAGRAPH_SPACING, "0").set(PartStyleOption.LINE_HEIGHT, "20px")
                .set(PartStyleOption.FONT_FAMILY, "\"Helvetica Neue\", Helvetica, Arial, sans-serif")
                .set(PartStyleOption.TEXT_COLOR, "rgb(10, 20, 30)").set(PartStyleOption.EMPHASIS_COLOR, "#abc")
                .set(PartStyleOption.STRONG_COLOR, "red").set(PartStyleOption.TEXT_ALIGN, "left")
                .set(PartStyleOption.HEADING_SIZE, "18pt");

        PrintStyle print = PrintStyle.of(style);

        assertThat(print.getBodySize()).isCloseTo(10.5f, within(0.01f));
        assertThat(print.getFirstLineIndent()).isCloseTo(21f, within(0.01f));
        assertThat(print.getParagraphSpacing()).isZero();
        assertThat(print.getLineHeight()).as("20 px on a 10.5 pt text").isCloseTo(15f / 10.5f, within(0.01f));
        assertThat(print.getFamily()).isEqualTo(PrintStyle.Family.SANS);
        assertThat(print.getFontName()).isEqualTo("Helvetica Neue");
        assertThat(print.getTextColor()).isEqualTo((10 << 16) | (20 << 8) | 30);
        assertThat(print.getEmphasisColor()).isEqualTo(0xaabbcc);
        assertThat(print.getStrongColor()).as("a color name is not turned into a number").isNull();
        assertThat(print.getAlign()).isEqualTo(PrintStyle.Align.LEFT);
        assertThat(print.getHeadingScale()).isCloseTo(18f / 10.5f, within(0.01f));
    }

    @Test
    void aMonospaceStackIsMono() {
        PartStyle style = new PartStyle("s1", "Typewriter").set(PartStyleOption.FONT_FAMILY, "\"Courier New\", Courier, monospace");

        assertThat(PrintStyle.of(style).getFamily()).isEqualTo(PrintStyle.Family.MONO);
    }
}
