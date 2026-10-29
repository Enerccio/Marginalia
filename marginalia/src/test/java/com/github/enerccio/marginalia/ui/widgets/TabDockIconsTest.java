package com.github.enerccio.marginalia.ui.widgets;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The letters and the SVG of the icons of the minimized tabs.
 */
class TabDockIconsTest {

    @Test
    void severalWordsGiveTheirInitials() {
        assertThat(TabDockIcons.letters("Side Query")).isEqualTo("SQ");
        assertThat(TabDockIcons.letters("author's note")).isEqualTo("AN");
        assertThat(TabDockIcons.letters("ST Converter Plus")).isEqualTo("SC");
    }

    @Test
    void oneWordGivesItsFirstTwoLetters() {
        assertThat(TabDockIcons.letters("Lorebook")).isEqualTo("Lo");
        assertThat(TabDockIcons.letters("  arcs ")).isEqualTo("Ar");
        assertThat(TabDockIcons.letters("X")).isEqualTo("X");
    }

    @Test
    void anEmptyCaptionHasAQuestionMark() {
        assertThat(TabDockIcons.letters(null)).isEqualTo("?");
        assertThat(TabDockIcons.letters(" - ")).isEqualTo("?");
    }

    @Test
    void theIconIsACircleWithTheLetters() {
        String svg = decode(TabDockIcons.dataUri("Ch", "chapters"));

        assertThat(svg).contains("<circle").contains(">Ch</text>");
        // the same key, the same color
        assertThat(TabDockIcons.dataUri("Ch", "chapters")).isEqualTo(TabDockIcons.dataUri("Ch", "chapters"));
    }

    @Test
    void theIconHasAtMostTwoLettersAndEscapesThem() {
        assertThat(decode(TabDockIcons.dataUri("Abc", "x"))).contains(">Ab</text>");
        assertThat(decode(TabDockIcons.dataUri("<&", "x"))).contains("&lt;&amp;");
        assertThat(decode(TabDockIcons.dataUri(" ", "x"))).contains(">?</text>");
    }

    private static String decode(String uri) {
        assertThat(uri).startsWith("data:image/svg+xml;base64,");
        return new String(Base64.getDecoder().decode(uri.substring(uri.indexOf(',') + 1)), StandardCharsets.UTF_8);
    }

}
