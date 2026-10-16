package com.github.enerccio.marginalia.bound;

import com.github.enerccio.marginalia.bound.UiProblem.Kind;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the session remembers of the problems the user was shown, and the text it keeps of them.
 */
class ProblemLogTest {

    @Test
    void remembersTheLastProblemsNewestFirst() {
        ProblemLog log = new ProblemLog();
        log.add(Kind.ERROR, "first", "");
        log.add(Kind.WARNING, "second", "");
        log.add(Kind.EXCEPTION, "third", "NullPointerException");

        assertThat(log.last(2)).extracting(UiProblem::message).containsExactly("third", "second");
        assertThat(log.last(10)).extracting(UiProblem::message).containsExactly("third", "second", "first");
    }

    @Test
    void keepsOnlyTheLatestOnes() {
        ProblemLog log = new ProblemLog();
        for (int i = 0; i < ProblemLog.CAPACITY + 5; i++) {
            log.add(Kind.ERROR, "problem " + i, "");
        }

        List<UiProblem> all = log.last(100);
        assertThat(all).hasSize(ProblemLog.CAPACITY);
        assertThat(all.getFirst().message()).isEqualTo("problem " + (ProblemLog.CAPACITY + 4));
        assertThat(all.getLast().message()).isEqualTo("problem 5");
    }

    @Test
    void theSameMessageToldTwiceIsOneProblemAndTheSpecificOneStays() {
        ProblemLog log = new ProblemLog();
        // the code that failed says it, then the notification that shows it
        UiProblem inference = log.add(Kind.INFERENCE, "The provider did not accept the key.", "InferenceException: 401");
        UiProblem again = log.add(Kind.ERROR, "The provider did not accept the key.", "");

        assertThat(log.last(10)).hasSize(1);
        assertThat(again.id()).isEqualTo(inference.id());
        assertThat(log.last(1).getFirst().kind()).isEqualTo(Kind.INFERENCE);
        assertThat(log.last(1).getFirst().detail()).contains("401");
    }

    @Test
    void aPlainErrorIsReplacedByTheMoreSpecificOneThatComesAfterIt() {
        ProblemLog log = new ProblemLog();
        log.add(Kind.ERROR, "Fetching the models failed.", "");
        log.add(Kind.EXCEPTION, "Fetching the models failed.", "IOException: connection refused");

        assertThat(log.last(10)).hasSize(1);
        assertThat(log.last(1).getFirst().kind()).isEqualTo(Kind.EXCEPTION);
    }

    @Test
    void differentMessagesAreDifferentProblems() {
        ProblemLog log = new ProblemLog();
        log.add(Kind.ERROR, "a", "");
        log.add(Kind.ERROR, "b", "");
        log.add(Kind.ERROR, "a", "");

        assertThat(log.last(10)).hasSize(3);
    }

    @Test
    void textIsOneLineWithoutKeysAndShort() {
        assertThat(UiProblem.clean("line one\n\n   line   two")).isEqualTo("line one line two");
        assertThat(UiProblem.clean("Authorization: Bearer abcdef1234567890xyz was refused")).doesNotContain("abcdef1234567890xyz")
                .contains("Bearer [removed]");
        assertThat(UiProblem.clean("key sk-proj-AbCdEf123456 is wrong")).doesNotContain("AbCdEf123456");
        assertThat(UiProblem.clean("{\"apiKey\": \"secretvalue123\"}")).doesNotContain("secretvalue123");
        assertThat(UiProblem.clean("password=hunter22hunter")).doesNotContain("hunter22hunter");
        assertThat(UiProblem.clean("x".repeat(1000))).hasSize(UiProblem.MAX_TEXT).endsWith("...");
        assertThat(UiProblem.clean(null)).isEmpty();
    }

    @Test
    void clearForgetsEverything() {
        ProblemLog log = new ProblemLog();
        log.add(Kind.ERROR, "a", "");
        log.clear();

        assertThat(log.last(10)).isEmpty();
    }
}
