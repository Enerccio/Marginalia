package com.github.enerccio.marginalia.ui.dialogs.statistics;

import com.github.enerccio.marginalia.domain.collections.ActivityMetric;
import com.github.enerccio.marginalia.domain.components.StatisticCounts;
import com.github.enerccio.marginalia.domain.components.WritingActivity;
import com.github.enerccio.marginalia.domain.components.WritingGoal;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The layout of the activity calendar: the weeks as columns, the first one full, the last one ending with today.
 */
class ActivityCalendarGridTest {

    /** A Tuesday. */
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 20);

    private WritingActivity activity(Map<LocalDate, StatisticCounts> counts) {
        return WritingActivity.of(TODAY, counts, new HashMap<>(), new HashMap<>(), Set.of(), WritingGoal.NONE, null);
    }

    @Test
    void theWeeksStartOnTheFirstDayAndTheLastOneEndsWithToday() {
        List<ActivityCalendarGrid.Week> weeks = ActivityCalendarGrid.weeks(activity(Map.of()), ActivityMetric.WORDS, 4, DayOfWeek.MONDAY);

        assertThat(weeks).hasSize(4);
        assertThat(weeks.getFirst().days()).hasSize(7);
        assertThat(weeks.getFirst().days().getFirst().day().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(weeks.getLast().days()).hasSize(2);
        assertThat(weeks.getLast().days().getLast().day()).isEqualTo(TODAY);
        assertThat(weeks.getLast().days().getLast().today()).isTrue();
        assertThat(weeks.stream().flatMap(week -> week.days().stream()).filter(ActivityCalendarGrid.Cell::today)).hasSize(1);
    }

    @Test
    void theDaysFollowEachOtherWithoutAGap() {
        List<ActivityCalendarGrid.Cell> cells = ActivityCalendarGrid.weeks(activity(Map.of()), ActivityMetric.WORDS, 10, DayOfWeek.SUNDAY)
                .stream().flatMap(week -> week.days().stream()).toList();

        for (int i = 1; i < cells.size(); i++) {
            assertThat(cells.get(i).day()).isEqualTo(cells.get(i - 1).day().plusDays(1));
        }
        assertThat(cells.getFirst().day()).isEqualTo(ActivityCalendarGrid.start(activity(Map.of()), 10, DayOfWeek.SUNDAY));
        assertThat(cells.getFirst().day().getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
    }

    @Test
    void theWeekWithTheFirstOfAMonthIsLabelledWithIt() {
        List<ActivityCalendarGrid.Week> weeks = ActivityCalendarGrid.weeks(activity(Map.of()), ActivityMetric.WORDS, 8, DayOfWeek.MONDAY);

        List<LocalDate> labelled = weeks.stream().map(ActivityCalendarGrid.Week::month).filter(java.util.Objects::nonNull).toList();
        assertThat(labelled).containsExactly(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 1));
    }

    @Test
    void theShadeOfEveryDayComesFromTheActivity() {
        Map<LocalDate, StatisticCounts> counts = new HashMap<>();
        counts.put(TODAY, StatisticCounts.wordsWritten(1000));
        counts.put(TODAY.minusDays(1), StatisticCounts.wordsWritten(100));

        List<ActivityCalendarGrid.Cell> cells = ActivityCalendarGrid.weeks(activity(counts), ActivityMetric.WORDS, 3, DayOfWeek.MONDAY)
                .stream().flatMap(week -> week.days().stream()).toList();

        assertThat(cells.stream().filter(cell -> cell.day().equals(TODAY)).findFirst().orElseThrow().level()).isEqualTo(4);
        assertThat(cells.stream().filter(cell -> cell.day().equals(TODAY.minusDays(1))).findFirst().orElseThrow().level()).isEqualTo(1);
        assertThat(cells.stream().filter(cell -> cell.day().equals(TODAY.minusDays(2))).findFirst().orElseThrow().level()).isZero();
    }

    @Test
    void aBusyDayOutsideTheCalendarDoesNotSetItsScale() {
        Map<LocalDate, StatisticCounts> counts = new HashMap<>();
        counts.put(TODAY.minusDays(400), StatisticCounts.wordsWritten(1_000_000));
        counts.put(TODAY, StatisticCounts.wordsWritten(10));

        List<ActivityCalendarGrid.Cell> cells = ActivityCalendarGrid.weeks(activity(counts), ActivityMetric.WORDS, 4, DayOfWeek.MONDAY)
                .stream().flatMap(week -> week.days().stream()).toList();

        assertThat(cells.getLast().level()).isEqualTo(4);
    }
}
