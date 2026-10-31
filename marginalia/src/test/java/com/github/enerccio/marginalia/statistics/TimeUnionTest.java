package com.github.enerccio.marginalia.statistics;

import com.github.enerccio.marginalia.domain.collections.ActivityKind;
import com.github.enerccio.marginalia.domain.components.ActivityRow;
import com.github.enerccio.marginalia.domain.components.ActivityTimes;
import com.github.enerccio.marginalia.domain.components.TimeUnion;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The time that is shown is the union of the stretches: what ran together is counted once.
 */
class TimeUnionTest {

    private static final long SECOND = 1000;
    private static final long HOUR = 60 * 60 * SECOND;
    private static final LocalDate DAY = LocalDate.of(2026, 10, 30);
    private static final long T0 = 1_000_000_000_000L;

    private static ActivityRow row(String book, ActivityKind kind, boolean agent, long start, long end) {
        return new ActivityRow(book, kind, agent, DAY, new Date(T0 + start), new Date(T0 + end));
    }

    @Test
    void fiveAgentsThatRunAllDayAreOneDayInTotalAndOneDayEach() {
        List<ActivityRow> rows = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            rows.add(row("book-" + i, ActivityKind.RUN, true, 0, 24 * HOUR));
        }

        assertThat(TimeUnion.times(rows).run()).isEqualTo(24 * HOUR);
        Map<String, ActivityTimes> byBook = TimeUnion.byBook(rows);
        assertThat(byBook).hasSize(5);
        assertThat(byBook.values()).allSatisfy(t -> assertThat(t.run()).isEqualTo(24 * HOUR));
    }

    @Test
    void threeBooksOneAfterAnotherAreTheSumOfThem() {
        List<ActivityRow> rows = List.of(
                row("a", ActivityKind.RUN, true, 0, 8 * HOUR),
                row("b", ActivityKind.RUN, true, 8 * HOUR, 16 * HOUR),
                row("c", ActivityKind.RUN, true, 16 * HOUR, 24 * HOUR));

        assertThat(TimeUnion.times(rows).run()).isEqualTo(24 * HOUR);
        assertThat(TimeUnion.byBook(rows).get("b").run()).isEqualTo(8 * HOUR);
    }

    @Test
    void twoStretchesOfTenSecondsOffsetByFourAreFourteen() {
        List<ActivityRow> rows = List.of(
                row("a", ActivityKind.GENERATE, false, 0, 10 * SECOND),
                row("b", ActivityKind.GENERATE, false, 4 * SECOND, 14 * SECOND));

        assertThat(TimeUnion.union(rows)).isEqualTo(14 * SECOND);
    }

    @Test
    void aStretchInsideAnotherAddsNothingAndGapsAreNotBridged() {
        List<ActivityRow> rows = List.of(
                row("a", ActivityKind.OPEN, false, 0, 100 * SECOND),
                row("b", ActivityKind.OPEN, false, 10 * SECOND, 20 * SECOND),
                row("c", ActivityKind.OPEN, false, 200 * SECOND, 210 * SECOND));

        assertThat(TimeUnion.union(rows)).isEqualTo(110 * SECOND);
    }

    @Test
    void theOrderOfTheRowsDoesNotMatter() {
        List<ActivityRow> rows = new ArrayList<>(List.of(
                row("a", ActivityKind.OPEN, false, 50 * SECOND, 60 * SECOND),
                row("b", ActivityKind.OPEN, false, 0, 55 * SECOND),
                row("c", ActivityKind.OPEN, false, 58 * SECOND, 70 * SECOND)));

        assertThat(TimeUnion.union(rows)).isEqualTo(70 * SECOND);
    }

    @Test
    void touchingStretchesAreOne() {
        assertThat(TimeUnion.union(List.of(
                row("a", ActivityKind.OPEN, false, 0, 10 * SECOND),
                row("a", ActivityKind.OPEN, false, 10 * SECOND, 25 * SECOND)))).isEqualTo(25 * SECOND);
    }

    @Test
    void theKindsAreNotAddedToEachOther() {
        List<ActivityRow> rows = List.of(
                row("a", ActivityKind.OPEN, false, 0, 100 * SECOND),
                row("a", ActivityKind.EDIT, false, 10 * SECOND, 40 * SECOND),
                row("a", ActivityKind.GENERATE, false, 50 * SECOND, 60 * SECOND));

        ActivityTimes times = TimeUnion.times(rows);

        assertThat(times).isEqualTo(new ActivityTimes(100 * SECOND, 30 * SECOND, 10 * SECOND, 0));
        assertThat(times.of(ActivityKind.EDIT)).isEqualTo(30 * SECOND);
    }

    @Test
    void bothSidesAreOneUnionWhenTheyAreAskedTogether() {
        ActivityRow author = row("a", ActivityKind.GENERATE, false, 0, 10 * SECOND);
        ActivityRow agent = row("b", ActivityKind.GENERATE, true, 6 * SECOND, 16 * SECOND);

        assertThat(TimeUnion.times(List.of(author, agent)).generate()).isEqualTo(16 * SECOND);
        assertThat(TimeUnion.times(List.of(author)).generate()).isEqualTo(10 * SECOND);
        assertThat(TimeUnion.times(List.of(agent)).generate()).isEqualTo(10 * SECOND);
    }

    @Test
    void eachDayIsItsOwnUnion() {
        ActivityRow today = row("a", ActivityKind.RUN, true, 0, 10 * SECOND);
        ActivityRow other = new ActivityRow("b", ActivityKind.RUN, true, DAY.plusDays(1), new Date(T0), new Date(T0 + 4 * SECOND));
        ActivityRow same = row("b", ActivityKind.RUN, true, 5 * SECOND, 20 * SECOND);

        var byDay = TimeUnion.byDay(List.of(today, other, same));

        assertThat(byDay.get(DAY).run()).isEqualTo(20 * SECOND);
        assertThat(byDay.get(DAY.plusDays(1)).run()).isEqualTo(4 * SECOND);
    }

    @Test
    void nothingIsNothing() {
        assertThat(TimeUnion.union(List.of())).isZero();
        assertThat(TimeUnion.union(null)).isZero();
        assertThat(TimeUnion.times(List.of()).isZero()).isTrue();
        assertThat(TimeUnion.byDay(null)).isEmpty();
    }
}
