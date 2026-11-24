package com.github.enerccio.marginalia.command;

import com.github.enerccio.marginalia.domain.service.command.CommandLine;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link CommandLine}: a command typed as text. Arguments are separated by spaces, {@code \ } is a space inside an
 * argument and {@code \\} a backslash; double quotes group words, {@code \"} is a quote.
 */
class CommandLineTest {

    @Test
    void theNameAndTheArgumentsAreSeparatedBySpaces() {
        CommandLine line = CommandLine.parse("/my-command one two  three");
        assertThat(line.command()).isTrue();
        assertThat(line.name()).isEqualTo("my-command");
        assertThat(line.arguments()).containsExactly("one", "two", "three");
        assertThat(line.trailingSeparator()).isFalse();
    }

    @Test
    void anEscapedSpaceStaysInTheArgument() {
        assertThat(CommandLine.parse("/c a\\ b c").arguments()).containsExactly("a b", "c");
        assertThat(CommandLine.parse("/c \\ ").arguments()).containsExactly(" ");
    }

    @Test
    void aDoubleBackslashIsOneBackslash() {
        assertThat(CommandLine.parse("/c a\\\\b").arguments()).containsExactly("a\\b");
        assertThat(CommandLine.parse("/c a\\\\ b").arguments()).containsExactly("a\\", "b");
        assertThat(CommandLine.parse("/c a\\\\\\ b").arguments()).containsExactly("a\\ b");
    }

    @Test
    void otherBackslashesAreKept() {
        assertThat(CommandLine.parse("/c C:\\dir\\file").arguments()).containsExactly("C:\\dir\\file");
        assertThat(CommandLine.parse("/c trailing\\").arguments()).containsExactly("trailing\\");
    }

    @Test
    void quotesGroupWordsWithSpaces() {
        assertThat(CommandLine.parse("/c \"a b\" c").arguments()).containsExactly("a b", "c");
        assertThat(CommandLine.parse("/c \"a   b\"").arguments()).containsExactly("a   b");
        assertThat(CommandLine.parse("/c a\"b c\"d e").arguments()).containsExactly("ab cd", "e");
        assertThat(CommandLine.parse("/c \"a\\\\\" b").arguments()).containsExactly("a\\", "b");
    }

    @Test
    void emptyQuotesAreAnEmptyArgument() {
        assertThat(CommandLine.parse("/c \"\" x").arguments()).containsExactly("", "x");
        assertThat(CommandLine.parse("/c \"\"").arguments()).containsExactly("");
    }

    @Test
    void anEscapedQuoteIsLiteral() {
        assertThat(CommandLine.parse("/c say\\\"hi\\\"").arguments()).containsExactly("say\"hi\"");
        assertThat(CommandLine.parse("/c \"say \\\"hi\\\" now\"").arguments()).containsExactly("say \"hi\" now");
    }

    @Test
    void anUnclosedQuoteRunsToTheEnd() {
        CommandLine line = CommandLine.parse("/c \"a b ");
        assertThat(line.arguments()).containsExactly("a b ");
        assertThat(line.trailingSeparator()).isFalse();
        assertThat(line.currentArgument()).isEqualTo(0);
        assertThat(CommandLine.parse("/c \"").arguments()).containsExactly("");
    }

    @Test
    void aSpaceAfterAClosingQuoteStartsTheNextArgument() {
        CommandLine line = CommandLine.parse("/c \"a b\" ");
        assertThat(line.arguments()).containsExactly("a b");
        assertThat(line.trailingSeparator()).isTrue();
        assertThat(line.currentArgument()).isEqualTo(1);
    }

    @Test
    void textWithoutASlashIsNotACommand() {
        CommandLine line = CommandLine.parse("hello there");
        assertThat(line.command()).isFalse();
        assertThat(line.name()).isEqualTo("hello");
    }

    @Test
    void emptyAndNullTextHasNoName() {
        assertThat(CommandLine.parse(null).name()).isEmpty();
        assertThat(CommandLine.parse("").command()).isFalse();
        CommandLine slash = CommandLine.parse("/");
        assertThat(slash.command()).isTrue();
        assertThat(slash.name()).isEmpty();
        assertThat(slash.typingName()).isTrue();
    }

    @Test
    void theArgumentBeingTypedIsKnown() {
        assertThat(CommandLine.parse("/he").currentArgument()).isEqualTo(-1);
        assertThat(CommandLine.parse("/he").typingName()).isTrue();
        assertThat(CommandLine.parse("/hello ").currentArgument()).isEqualTo(0);
        assertThat(CommandLine.parse("/hello ").typingName()).isFalse();
        assertThat(CommandLine.parse("/hello a").currentArgument()).isEqualTo(0);
        assertThat(CommandLine.parse("/hello a ").currentArgument()).isEqualTo(1);
        assertThat(CommandLine.parse("/hello a b").currentArgument()).isEqualTo(1);
        assertThat(CommandLine.parse("/hello a\\ ").currentArgument()).isEqualTo(0);
    }

    @Test
    void escapedArgumentsReadBackAsTheyWere() {
        for (String argument : new String[]{"plain", "with space", "back\\slash", " \\ ", "a\\ b", "say \"hi\"", "\"", "\\\""}) {
            assertThat(CommandLine.parse("/c " + CommandLine.escape(argument)).arguments()).containsExactly(argument);
        }
    }
}
