package com.github.enerccio.marginalia.statistics;

import com.github.enerccio.marginalia.domain.collections.ActivityMetric;
import com.github.enerccio.marginalia.domain.components.StatisticCounts;
import com.github.enerccio.marginalia.domain.components.WritingActivity;
import com.github.enerccio.marginalia.domain.components.WritingGoal;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What makes a day worked on, the streaks, the goal and the shade of a day: the rules of the activity calendar, without a
 * database.
 */
class WritingActivityTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 20);
    private static final long MINUTE = 60_000L;

    private final Map<LocalDate, StatisticCounts> counts = new HashMap<>();
    private final Map<LocalDate, Long> edit = new HashMap<>();

    private WritingActivity build(WritingGoal goal, LocalDate timeSince) {
        return WritingActivity.of(TODAY, counts, edit, new HashMap<>(), Set.of(), goal, timeSince);
    }

    private WritingActivity build() {
        return build(WritingGoal.NONE, null);
    }

    private void words(int daysAgo, long words) {
        counts.put(TODAY.minusDays(daysAgo), StatisticCounts.wordsWritten(words));
    }

    @Test
    void aDayIsWorkedOnByWordsAnEditAPartOrEditingTime() {
        assertThat(WritingActivity.worked(StatisticCounts.wordsWritten(1), 0)).isTrue();
        assertThat(WritingActivity.worked(StatisticCounts.edits(1), 0)).isTrue();
        assertThat(WritingActivity.worked(StatisticCounts.partsCreated(1), 0)).isTrue();
        assertThat(WritingActivity.worked(StatisticCounts.ZERO, 5 * MINUTE)).isTrue();
    }

    @Test
    void generatedWordsRemovalsTokensAndRequestsAloneDoNotMakeADay() {
        assertThat(WritingActivity.worked(StatisticCounts.wordsGenerated(500), 0)).isFalse();
        assertThat(WritingActivity.worked(StatisticCounts.wordsRemoved(500), 0)).isFalse();
        assertThat(WritingActivity.worked(StatisticCounts.partsDeleted(3), 0)).isFalse();
        assertThat(WritingActivity.worked(StatisticCounts.tokens(1000, 1000), 0)).isFalse();
    }

    @Test
    void onlyTheDaysThatWereWorkedOnAreKept() {
        counts.put(TODAY, StatisticCounts.tokens(10, 10));
        words(1, 5);
        edit.put(TODAY.minusDays(2), MINUTE);
        counts.put(TODAY.plusDays(1), StatisticCounts.wordsWritten(9));

        WritingActivity activity = build();

        assertThat(activity.days().keySet()).containsExactly(TODAY.minusDays(2), TODAY.minusDays(1));
        assertThat(activity.day(TODAY).worked()).isFalse();
    }

    @Test
    void theCurrentStreakCountsTheDaysInARowEndingToday() {
        words(0, 10);
        words(1, 10);
        words(2, 10);
        words(4, 10);

        WritingActivity activity = build();

        assertThat(activity.currentStreak()).isEqualTo(3);
        assertThat(activity.longestStreak()).isEqualTo(3);
    }

    @Test
    void aTodayThatIsNotWorkedOnYetDoesNotBreakTheStreak() {
        words(1, 10);
        words(2, 10);

        assertThat(build().currentStreak()).isEqualTo(2);
    }

    @Test
    void aMissedDayBreaksTheCurrentStreakButNotTheLongest() {
        for (int ago : new int[]{2, 3, 4, 5, 6}) {
            words(ago, 10);
        }
        words(9, 10);

        WritingActivity activity = build();

        assertThat(activity.currentStreak()).isZero();
        assertThat(activity.longestStreak()).isEqualTo(5);
    }

    @Test
    void noDaysMeansNoStreaks() {
        WritingActivity activity = build();

        assertThat(activity.currentStreak()).isZero();
        assertThat(activity.longestStreak()).isZero();
        assertThat(activity.currentGoalStreak()).isZero();
    }

    @Test
    void theGoalStreakCountsOnlyTheDaysThatReachedEverySetGoal() {
        words(0, 500);
        words(1, 600);
        words(2, 100);
        words(3, 900);
        WritingGoal goal = new WritingGoal(500, 0);

        WritingActivity activity = build(goal, null);

        assertThat(activity.currentStreak()).isEqualTo(4);
        assertThat(activity.currentGoalStreak()).isEqualTo(2);
        assertThat(activity.longestGoalStreak()).isEqualTo(2);
    }

    @Test
    void twoGoalsMustBothBeReached() {
        words(0, 500);
        edit.put(TODAY, 10 * MINUTE);
        words(1, 500);
        edit.put(TODAY.minusDays(1), 30 * MINUTE);
        WritingGoal goal = new WritingGoal(500, 20);

        WritingActivity activity = build(goal, null);

        assertThat(goal.reachedBy(activity.day(TODAY))).isFalse();
        assertThat(goal.reachedBy(activity.day(TODAY.minusDays(1)))).isTrue();
        assertThat(activity.currentGoalStreak()).isEqualTo(1);
    }

    @Test
    void withoutAGoalNothingReachesIt() {
        words(0, 100000);

        WritingActivity activity = build();

        assertThat(WritingGoal.NONE.isSet()).isFalse();
        assertThat(WritingGoal.NONE.reachedBy(activity.day(TODAY))).isFalse();
        assertThat(activity.currentGoalStreak()).isZero();
        assertThat(new WritingGoal(-4, -1).isSet()).isFalse();
    }

    @Test
    void theShadeGoesByTheGoalWhenThereIsOne() {
        words(0, 600);
        words(1, 400);
        words(2, 250);
        words(3, 50);
        words(4, 10_000);
        WritingActivity activity = build(new WritingGoal(500, 0), null);

        long max = activity.max(ActivityMetric.WORDS, TODAY.minusDays(10), TODAY);

        assertThat(activity.level(TODAY, ActivityMetric.WORDS, max)).isEqualTo(4);
        assertThat(activity.level(TODAY.minusDays(1), ActivityMetric.WORDS, max)).isEqualTo(3);
        assertThat(activity.level(TODAY.minusDays(2), ActivityMetric.WORDS, max)).isEqualTo(2);
        assertThat(activity.level(TODAY.minusDays(3), ActivityMetric.WORDS, max)).isEqualTo(1);
        assertThat(activity.level(TODAY.minusDays(4), ActivityMetric.WORDS, max)).as("above the goal is the top").isEqualTo(4);
        assertThat(activity.level(TODAY.minusDays(7), ActivityMetric.WORDS, max)).as("not worked on").isZero();
    }

    @Test
    void withoutAGoalTheShadeGoesByTheBusiestDay() {
        words(0, 1000);
        words(1, 500);
        words(2, 100);
        WritingActivity activity = build();

        long max = activity.max(ActivityMetric.WORDS, TODAY.minusDays(30), TODAY);

        assertThat(max).isEqualTo(1000);
        assertThat(activity.level(TODAY, ActivityMetric.WORDS, max)).isEqualTo(4);
        assertThat(activity.level(TODAY.minusDays(1), ActivityMetric.WORDS, max)).isEqualTo(2);
        assertThat(activity.level(TODAY.minusDays(2), ActivityMetric.WORDS, max)).isEqualTo(1);
    }

    @Test
    void aWorkedDayWithoutWordsStillHasTheFirstShade() {
        counts.put(TODAY, StatisticCounts.edits(2));
        WritingActivity activity = build();

        assertThat(activity.level(TODAY, ActivityMetric.WORDS, 0)).isEqualTo(1);
    }

    @Test
    void theTimeShadeGoesByEditingTimeAndADayWithoutRecordedTimeIsOnlyMarked() {
        edit.put(TODAY, 60 * MINUTE);
        edit.put(TODAY.minusDays(1), 15 * MINUTE);
        words(5, 300);
        WritingActivity activity = build(WritingGoal.NONE, TODAY.minusDays(3));

        long max = activity.max(ActivityMetric.TIME, TODAY.minusDays(30), TODAY);

        assertThat(activity.level(TODAY, ActivityMetric.TIME, max)).isEqualTo(4);
        assertThat(activity.level(TODAY.minusDays(1), ActivityMetric.TIME, max)).isEqualTo(1);
        assertThat(activity.level(TODAY.minusDays(5), ActivityMetric.TIME, max)).as("worked on, the time was not recorded").isEqualTo(1);
        assertThat(activity.timeKnown(TODAY.minusDays(5))).isFalse();
    }

    @Test
    void daysWorkedAreCountedInARangeWithBothEndsIncluded() {
        words(0, 1);
        words(3, 1);
        words(10, 1);

        assertThat(build().daysWorked(TODAY.minusDays(3), TODAY)).isEqualTo(2);
    }

    @Test
    void theLastActivityAndTheRebuiltMarkAreKeptOnTheDay() {
        words(2, 40);
        Date last = new Date(1_000_000L);
        WritingActivity activity = WritingActivity.of(TODAY, counts, edit, Map.of(TODAY.minusDays(2), last),
                Set.of(TODAY.minusDays(2)), WritingGoal.NONE, null);

        WritingActivity.Day day = activity.day(TODAY.minusDays(2));

        assertThat(day.lastActivity()).isEqualTo(last);
        assertThat(day.reconstructed()).isTrue();
        assertThat(day.wordsWritten()).isEqualTo(40);
    }
}
