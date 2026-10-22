package com.github.enerccio.marginalia.search;

import com.github.enerccio.marginalia.domain.service.search.LikePatterns;
import com.github.enerccio.marginalia.domain.service.search.replace.TextHit;
import com.github.enerccio.marginalia.domain.service.search.replace.TextSearch;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TextSearchTest {

    @Test
    void plainSearchIsLiteralAndCaseSensitive() {
        TextSearch.Result result = TextSearch.of("a.c", "X", false).apply("abc a.c A.C a.c");

        assertThat(result.newText()).isEqualTo("abc X A.C X");
        assertThat(result.hits()).extracting(TextHit::match).containsExactly("a.c", "a.c");
    }

    @Test
    void plainSearchCanIgnoreTheCase() {
        TextSearch.Result result = TextSearch.of("a.c", "$1\\", false, false).apply("A.C a.c abc ÀÉ");

        assertThat(result.newText()).isEqualTo("$1\\ $1\\ abc ÀÉ");
        assertThat(result.hits()).extracting(TextHit::match).containsExactly("A.C", "a.c");
        assertThat(TextSearch.of("àé", "x", false, false).apply("ÀÉ").newText()).isEqualTo("x");
    }

    @Test
    void caseDoesNotMatterForRegexOption() {
        // a regular expression is case sensitive whatever the flag, it can use (?i)
        assertThat(TextSearch.of("john", "x", true, false).matches("John")).isFalse();
        assertThat(TextSearch.of("(?i)john", "x", true, false).matches("John")).isTrue();
    }

    @Test
    void plainReplacementIsNotInterpreted() {
        TextSearch.Result result = TextSearch.of("a", "$1\\", false).apply("banana");

        assertThat(result.newText()).isEqualTo("b$1\\n$1\\n$1\\");
    }

    @Test
    void regexReplacementFillsGroups() {
        TextSearch.Result result = TextSearch.of("(\\w+)@(\\w+)", "$2:$1", true).apply("mail bob@home and amy@work!");

        assertThat(result.newText()).isEqualTo("mail home:bob and work:amy!");
        assertThat(result.hits()).extracting(TextHit::replacement).containsExactly("home:bob", "work:amy");
        assertThat(result.hits()).extracting(TextHit::match).containsExactly("bob@home", "amy@work");
    }

    @Test
    void hitsKeepTheTextAround() {
        String words = IntStream.rangeClosed(1, 30).mapToObj(i -> "w" + i).collect(Collectors.joining(" "));
        TextHit hit = TextSearch.of("eight", "8", false).apply("response", words + " eight " + words).hits().getFirst();

        assertThat(hit.field()).isEqualTo("response");
        assertThat(hit.before()).startsWith("…w").endsWith("w29 w30 ");
        assertThat(hit.after()).startsWith(" w1 w2").endsWith("…");
        assertThat(hit.before().length()).isLessThanOrEqualTo(TextSearch.CONTEXT_RADIUS + 1);
        assertThat(hit.after().length()).isLessThanOrEqualTo(TextSearch.CONTEXT_RADIUS + 1);
    }

    @Test
    void emptyMatchesAreIgnored() {
        TextSearch.Result result = TextSearch.of("x*", "-", true).apply("axxb");

        assertThat(result.newText()).isEqualTo("a-b");
        assertThat(result.hits()).hasSize(1);
    }

    @Test
    void noMatchLeavesTheText() {
        TextSearch.Result result = TextSearch.of("zzz", "y", false).apply("abc");

        assertThat(result.found()).isFalse();
        assertThat(result.newText()).isEqualTo("abc");
        assertThat(TextSearch.of("zzz", "y", false).matches("abc")).isFalse();
        assertThat(TextSearch.of("b+", "y", true).matches("abbc")).isTrue();
    }

    @Test
    void invalidRegexAndReplacementAreReported() {
        assertThatThrownBy(() -> TextSearch.of("(", "x", true))
                .isInstanceOfSatisfying(TextSearch.InvalidSearchException.class,
                        e -> assertThat(e.getKind()).isEqualTo(TextSearch.InvalidSearchException.Kind.SEARCH));
        assertThatThrownBy(() -> TextSearch.of("(a)", "$2", true))
                .isInstanceOfSatisfying(TextSearch.InvalidSearchException.class,
                        e -> assertThat(e.getKind()).isEqualTo(TextSearch.InvalidSearchException.Kind.REPLACEMENT));
        assertThatThrownBy(() -> TextSearch.of("a", "$", true))
                .isInstanceOf(TextSearch.InvalidSearchException.class);
    }

    @Test
    void literalOnlyForPlainSearch() {
        assertThat(TextSearch.of("a_b", "", false).literal()).isEqualTo("a_b");
        assertThat(TextSearch.of("a_b", "", true).literal()).isNull();
        // the database ignores the case of A-Z only
        assertThat(TextSearch.of("a_b", "", false, false).literal()).isEqualTo("a_b");
        assertThat(TextSearch.of("é", "", false, false).literal()).isNull();
        assertThat(TextSearch.of("é", "", false, true).literal()).isEqualTo("é");
    }

    @Test
    void likeLiteralEscapesTheSpecialCharacters() {
        assertThat(LikePatterns.literal("50%_a*\\")).isEqualTo("50\\%\\_a*\\\\");
    }

}
