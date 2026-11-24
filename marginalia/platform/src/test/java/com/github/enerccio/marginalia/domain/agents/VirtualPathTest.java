package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.service.agent.VirtualFileException;
import com.github.enerccio.marginalia.domain.service.agent.VirtualPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The text the model writes becomes a {@link VirtualPath} or an error; nothing in between can leave the root.
 */
class VirtualPathTest {

    @Test
    void parsesAbsolutePaths() {
        VirtualPath path = VirtualPath.parse("/notes/plot.md");

        assertThat(path.getSegments()).containsExactly("notes", "plot.md");
        assertThat(path.getName()).isEqualTo("plot.md");
        assertThat(path.getFirst()).isEqualTo("notes");
        assertThat(path.getParent()).isEqualTo(VirtualPath.parse("/notes"));
        assertThat(path.getDepth()).isEqualTo(2);
        assertThat(path).hasToString("/notes/plot.md");
        assertThat(VirtualPath.parse("/")).isSameAs(VirtualPath.ROOT).hasToString("/");
        assertThat(VirtualPath.ROOT.isRoot()).isTrue();
        assertThat(VirtualPath.ROOT.getParent()).isNull();
        assertThat(VirtualPath.ROOT.getName()).isNull();
    }

    @Test
    void aSingleTrailingSlashIsDropped() {
        assertThat(VirtualPath.parse("/notes/")).isEqualTo(VirtualPath.parse("/notes"));
    }

    @Test
    void dotDotGoesUpAndStopsAtTheRoot() {
        assertThat(VirtualPath.parse("/a/b/..")).isEqualTo(VirtualPath.parse("/a"));
        assertThat(VirtualPath.parse("/a/../../b")).isEqualTo(VirtualPath.parse("/b"));
        assertThat(VirtualPath.parse("/..")).isEqualTo(VirtualPath.ROOT);
        assertThat(VirtualPath.parse("/../../../etc/passwd")).isEqualTo(VirtualPath.parse("/etc/passwd"));
        assertThat(VirtualPath.parse("/a/b/../../../..")).isEqualTo(VirtualPath.ROOT);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",                         // nothing
            "notes/plot.md",            // relative
            "./notes",                  // relative
            "../notes",                 // relative
            "//notes",                  // empty part, and the start of a UNC path
            "//server/share/file",      // UNC
            "/a//b",                    // empty part
            "/./a",                     // dot
            "/a/./b",                   // dot
            "/a\\b",                    // backslash
            "\\\\server\\share",        // UNC with backslashes
            "C:/Users/me",              // drive letter
            "/C:/Users/me",             // drive letter below the root
            "/a/b:stream",              // NTFS stream
            "/a\0b",                    // NUL
            "/a\nb",                    // control
            "/a\tb",                    // control
            "/a\u202eb",                // right to left override
            "/a\u200bb",                // zero width space
            "/a\ud800b",                // unpaired surrogate
            "/a<b", "/a>b", "/a\"b", "/a|b", "/a?b", "/a*b",
            "/ a", "/a ", "/a/ b", "/a /b", "/\u00a0a",  // spaces
            "/a.", "/a./b", "/a...",    // trailing dot
            "/CON", "/con", "/a/NUL", "/nul.txt", "/COM1", "/com9.log", "/LPT1", "/Lpt3.md", "/CONIN$", "/COM\u00b9",
    })
    void refusesWhatIsNotAPath(String text) {
        assertThatThrownBy(() -> VirtualPath.parse(text)).isInstanceOf(VirtualFileException.class);
    }

    @Test
    void refusesNull() {
        assertThatThrownBy(() -> VirtualPath.parse(null)).isInstanceOf(VirtualFileException.class);
    }

    @Test
    void namesThatLookLikeDotDotAreOnlyNames() {
        // not decoded, not normalized to dots: they are letters like any other and name a file below the root
        assertThat(VirtualPath.parse("/%2e%2e/x").getSegments()).containsExactly("%2e%2e", "x");
        assertThat(VirtualPath.parse("/%2e%2e%2fetc").getSegments()).containsExactly("%2e%2e%2fetc");
        assertThat(VirtualPath.parse("/\uff0e\uff0e/x").getSegments()).containsExactly("\uff0e\uff0e", "x");
        assertThat(VirtualPath.parse("/\u2025/x").getSegments()).containsExactly("\u2025", "x");
        assertThat(VirtualPath.parse("/..%2f..%2f").getSegments()).hasSize(1);
        // (a name may start with dots, only a dot at the end and a name that is only dots are refused)
        assertThat(VirtualPath.parse("/..hidden").getSegments()).containsExactly("..hidden");
        assertThat(VirtualPath.parse("/.notes").getSegments()).containsExactly(".notes");
    }

    @Test
    void normalizesToNfc() {
        // e + combining acute accent, and the single letter
        assertThat(VirtualPath.parse("/caf\u0065\u0301")).isEqualTo(VirtualPath.parse("/caf\u00e9"));
        assertThat(VirtualPath.parse("/caf\u0065\u0301").getName()).isEqualTo("caf\u00e9");
    }

    @Test
    void acceptsNamesInAnyScript() {
        assertThat(VirtualPath.parse("/\u7269\u8a9e/\u3042\u3044.txt").getSegments()).containsExactly("\u7269\u8a9e", "\u3042\u3044.txt");
        assertThat(VirtualPath.parse("/my notes/chapter 1.md").getName()).isEqualTo("chapter 1.md");
        assertThat(VirtualPath.parse("/a-b_c.d (2).md").getName()).isEqualTo("a-b_c.d (2).md");
    }

    @Test
    void limitsLengthAndDepth() {
        assertThat(VirtualPath.parse("/" + "a".repeat(VirtualPath.MAX_NAME_LENGTH))).isNotNull();
        assertThatThrownBy(() -> VirtualPath.parse("/" + "a".repeat(VirtualPath.MAX_NAME_LENGTH + 1)))
                .isInstanceOf(VirtualFileException.class);
        // short in characters, long in bytes
        assertThatThrownBy(() -> VirtualPath.parse("/" + "\u7269".repeat(80))).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> VirtualPath.parse("/" + "a/".repeat(VirtualPath.MAX_DEPTH + 1) + "b"))
                .isInstanceOf(VirtualFileException.class).hasMessageContaining("deep");
        assertThat(VirtualPath.parse("/" + "a/".repeat(VirtualPath.MAX_DEPTH - 1) + "b").getDepth()).isEqualTo(VirtualPath.MAX_DEPTH);
        assertThatThrownBy(() -> VirtualPath.parse("/" + ("a".repeat(90) + "/").repeat(5) + "b"))
                .isInstanceOf(VirtualFileException.class).hasMessageContaining("long");
        assertThatThrownBy(() -> VirtualPath.parse("/" + "a".repeat(5_000))).isInstanceOf(VirtualFileException.class);
    }

    @Test
    void depthIsCountedAfterDotDotIsApplied() {
        String up = "/a/b/c/d/e/f/g/h/i/j/k/l/m/n/o/p" + "/..".repeat(10);
        assertThat(VirtualPath.parse(up).getDepth()).isEqualTo(6);
    }

    @Test
    void resolveAddsOneCheckedName() {
        VirtualPath notes = VirtualPath.parse("/notes");

        assertThat(notes.resolve("plot.md")).isEqualTo(VirtualPath.parse("/notes/plot.md"));
        assertThatThrownBy(() -> notes.resolve("..")).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> notes.resolve("a/b")).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> notes.resolve("")).isInstanceOf(VirtualFileException.class);
        assertThatThrownBy(() -> notes.resolve("NUL")).isInstanceOf(VirtualFileException.class);
        VirtualPath deepest = VirtualPath.parse("/" + "a/".repeat(VirtualPath.MAX_DEPTH - 1) + "b");
        assertThatThrownBy(() -> deepest.resolve("c")).isInstanceOf(VirtualFileException.class);
    }

    @Test
    void startsWithIsPerName() {
        assertThat(VirtualPath.parse("/notes/plot.md").startsWith(VirtualPath.parse("/notes"))).isTrue();
        assertThat(VirtualPath.parse("/notes").startsWith(VirtualPath.parse("/notes"))).isTrue();
        assertThat(VirtualPath.parse("/notes2").startsWith(VirtualPath.parse("/notes"))).isFalse();
        assertThat(VirtualPath.parse("/a").startsWith(VirtualPath.ROOT)).isTrue();
    }

    @Test
    void errorsDoNotEchoControlCharactersOrLongText() {
        assertThatThrownBy(() -> VirtualPath.parse("a\nb" + "x".repeat(300)))
                .hasMessageNotContaining("\n").satisfies(e -> assertThat(e.getMessage().length()).isLessThan(300));
    }
}
