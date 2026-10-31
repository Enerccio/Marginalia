package com.github.enerccio.marginalia.statistics;

import com.github.enerccio.marginalia.domain.collections.StatisticRange;
import com.github.enerccio.marginalia.domain.collections.StatisticRange.Span;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The ranges of days of the statistics windows and of the commands.
 */
class StatisticRangeTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 3, 14);

    @Test
    void thePresetsAreDaysEndingToday() {
        assertThat(StatisticRange.TODAY.span(TODAY)).isEqualTo(new Span(TODAY, TODAY));
        assertThat(StatisticRange.YESTERDAY.span(TODAY)).isEqualTo(new Span(TODAY.minusDays(1), TODAY.minusDays(1)));
        assertThat(StatisticRange.LAST_7_DAYS.span(TODAY)).isEqualTo(new Span(LocalDate.of(2026, 3, 8), TODAY));
        assertThat(StatisticRange.LAST_7_DAYS.span(TODAY).days()).isEqualTo(7);
        assertThat(StatisticRange.LAST_30_DAYS.span(TODAY).days()).isEqualTo(30);
        assertThat(StatisticRange.THIS_MONTH.span(TODAY)).isEqualTo(new Span(LocalDate.of(2026, 3, 1), TODAY));
        assertThat(StatisticRange.ALL_TIME.span(TODAY)).isEqualTo(new Span(StatisticRange.BEGINNING, TODAY));
    }

    @Test
    void aCustomRangeHasNoDaysOfItsOwn() {
        assertThatThrownBy(() -> StatisticRange.CUSTOM.span(TODAY)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void aCommandNamesAPresetInAnyOfItsSpellings() {
        assertThat(StatisticRange.parse("last-7-days", TODAY)).isEqualTo(StatisticRange.LAST_7_DAYS.span(TODAY));
        assertThat(StatisticRange.parse("Last 30 Days", TODAY)).isEqualTo(StatisticRange.LAST_30_DAYS.span(TODAY));
        assertThat(StatisticRange.parse("this_month", TODAY)).isEqualTo(StatisticRange.THIS_MONTH.span(TODAY));
        assertThat(StatisticRange.parse(" all-time ", TODAY)).isEqualTo(StatisticRange.ALL_TIME.span(TODAY));
    }

    @Test
    void aCommandNamesOneDayOrTwo() {
        assertThat(StatisticRange.parse("2026-03-01", TODAY)).isEqualTo(new Span(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 1)));
        assertThat(StatisticRange.parse("2026-03-01..2026-03-10", TODAY)).isEqualTo(new Span(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 10)));
        assertThat(StatisticRange.parse("2026-03-10..2026-03-01", TODAY)).as("the order does not matter")
                .isEqualTo(new Span(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 10)));
    }

    @Test
    void noRangeIsTheLastWeek() {
        assertThat(StatisticRange.parse(null, TODAY)).isEqualTo(StatisticRange.LAST_7_DAYS.span(TODAY));
        assertThat(StatisticRange.parse("  ", TODAY)).isEqualTo(StatisticRange.LAST_7_DAYS.span(TODAY));
    }

    @Test
    void somethingElseIsRefused() {
        assertThatThrownBy(() -> StatisticRange.parse("sometime", TODAY)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StatisticRange.parse("2026-13-01", TODAY)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StatisticRange.parse("custom", TODAY)).isInstanceOf(IllegalArgumentException.class);
    }

}
