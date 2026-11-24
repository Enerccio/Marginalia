package com.github.enerccio.marginalia.utils;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A regular expression that backtracks is stopped when its time is over, one that does not is not affected.
 */
class TimeSafeTextTest {

    // the JDK remembers the steps of plain nested repetitions like (a+)+$, a back reference defeats that
    private static final Pattern BACKTRACKS = Pattern.compile("(a+)+\\1b");
    private static final String TEXT = "a".repeat(40) + "c";

    @Test
    void aPatternThatBacktracksIsStoppedAtTheDeadline() {
        long start = System.nanoTime();
        long deadline = TimeSafeText.deadlineIn(200);

        assertThatThrownBy(() -> TimeSafeText.matcher(BACKTRACKS, TEXT, deadline).find()).isInstanceOf(RegexTimeoutException.class)
                .hasMessageContaining("too long");

        long took = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        assertThat(took).as("not before the deadline").isGreaterThanOrEqualTo(200).as("and soon after it").isLessThan(5_000);
    }

    @Test
    void aPatternThatFinishesGivesTheSameResultAsWithoutTheLimit() {
        Pattern pattern = Pattern.compile("(?i)jo(h)?n");
        String text = "John went home, jon stayed. ".repeat(200);

        Matcher limited = TimeSafeText.matcher(pattern, text, TimeSafeText.deadlineIn(30_000));
        Matcher plain = pattern.matcher(text);
        int count = 0;
        while (plain.find()) {
            assertThat(limited.find()).isTrue();
            assertThat(limited.start()).isEqualTo(plain.start());
            assertThat(limited.group()).isEqualTo(plain.group());
            count++;
        }
        assertThat(limited.find()).isFalse();
        assertThat(count).isEqualTo(400);
    }

    @Test
    void theTextIsTheTextItWraps() {
        TimeSafeText text = new TimeSafeText("abcdef", TimeSafeText.deadlineIn(1_000));

        assertThat(text).hasToString("abcdef");
        assertThat(text.length()).isEqualTo(6);
        assertThat(text.subSequence(1, 3)).hasToString("bc");
    }

    @Test
    void aPartOfTheTextKeepsTheDeadline() {
        TimeSafeText text = new TimeSafeText("a".repeat(40) + "c", TimeSafeText.deadlineIn(200));

        assertThatThrownBy(() -> BACKTRACKS.matcher(text.subSequence(0, 41)).find()).isInstanceOf(RegexTimeoutException.class);
    }

    @Test
    void theTimeoutIsThirtySecondsUnlessThePropertySaysOtherwise() {
        assertThat(TimeSafeText.timeoutMillis()).isEqualTo(30_000);
        System.setProperty(TimeSafeText.TIMEOUT_PROPERTY, "1500");
        try {
            assertThat(TimeSafeText.timeoutMillis()).isEqualTo(1_500);
        } finally {
            System.clearProperty(TimeSafeText.TIMEOUT_PROPERTY);
        }
    }

    @Test
    void theViewGivesEveryTextItsOwnTime() {
        System.setProperty(TimeSafeText.TIMEOUT_PROPERTY, "200");
        try {
            var view = TimeSafeText.view();
            assertThatThrownBy(() -> BACKTRACKS.matcher(view.apply(TEXT)).find()).isInstanceOf(RegexTimeoutException.class);
            // the next text starts with a new deadline
            assertThat(Pattern.compile("a+").matcher(view.apply("caab")).find()).isTrue();
        } finally {
            System.clearProperty(TimeSafeText.TIMEOUT_PROPERTY);
        }
    }
}
