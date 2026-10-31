package com.github.enerccio.marginalia.statistics;

import com.github.enerccio.marginalia.domain.collections.StatisticSide;
import com.github.enerccio.marginalia.domain.components.*;
import com.github.enerccio.marginalia.domain.components.BookStatistic.BookState;
import com.github.enerccio.marginalia.domain.components.StatisticReport.BookLine;
import com.github.enerccio.marginalia.domain.components.StatisticReport.DayLine;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.ui.dialogs.statistics.StatisticCharts;
import com.github.enerccio.marginalia.ui.dialogs.statistics.StatisticCharts.BookMetric;
import com.github.enerccio.marginalia.ui.dialogs.statistics.StatisticCharts.Grain;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The Chart.js configurations of the statistics: the zero days are filled in, a long range is grouped, the removed words are
 * below the axis, the kinds of time stand side by side, and the books are the first ten and the rest.
 */
class StatisticChartsTest {

    private static final Function<L, String> TEXT = L::name;
    private static final Gson GSON = new Gson();
    private static final long HOUR = 3_600_000L;
    private static final LocalDate D1 = LocalDate.of(2026, 3, 2);  // a Monday

    private static StatisticReport report(LocalDate from, LocalDate to, List<DayLine> days, List<BookLine> books, List<SizeSeries.Point> sizes) {
        return new StatisticReport(StatisticFilter.of(from, to, StatisticSide.ALL), StatisticCounts.ZERO, ActivityTimes.ZERO, days, books,
                sizes, SizeGrowth.ZERO);
    }

    private static DayLine day(LocalDate day, StatisticCounts counts, ActivityTimes times) {
        return new DayLine(day, counts, times);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> datasets(Map<String, Object> config) {
        return (List<Map<String, Object>>) ((Map<String, Object>) config.get("data")).get("datasets");
    }

    @SuppressWarnings("unchecked")
    private static List<String> labels(Map<String, Object> config) {
        return (List<String>) ((Map<String, Object>) config.get("data")).get("labels");
    }

    @SuppressWarnings("unchecked")
    private static List<Object> data(Map<String, Object> config, int dataset) {
        return (List<Object>) datasets(config).get(dataset).get("data");
    }

    @Test
    void theDaysWithoutARowAreFilledInWithZero() {
        StatisticReport report = report(D1, D1.plusDays(4), List.of(
                day(D1.plusDays(1), StatisticCounts.wordsWritten(100), ActivityTimes.ZERO),
                day(D1.plusDays(3), StatisticCounts.wordsGenerated(40), ActivityTimes.ZERO)), List.of(), List.of());

        Map<String, Object> config = StatisticCharts.words(report, TEXT);

        assertThat(labels(config)).hasSize(5).first().isEqualTo("2026-03-02");
        assertThat(data(config, 0)).containsExactly(0L, 100L, 0L, 0L, 0L);
        assertThat(data(config, 1)).containsExactly(0L, 0L, 0L, 40L, 0L);
    }

    @Test
    void theRemovedWordsAreNegativeSoTheyHangBelowTheAxis() {
        StatisticReport report = report(D1, D1, List.of(day(D1, StatisticCounts.wordsRemoved(25).plus(StatisticCounts.wordsWritten(10)),
                ActivityTimes.ZERO)), List.of(), List.of());

        Map<String, Object> config = StatisticCharts.words(report, TEXT);

        assertThat(data(config, 2)).containsExactly(-25L);
        assertThat(datasets(config).get(2).get("label")).isEqualTo("LABEL_WORDS_REMOVED");
        assertThat(config.get("type")).isEqualTo("bar");
        @SuppressWarnings("unchecked")
        Map<String, Object> scales = (Map<String, Object>) ((Map<String, Object>) config.get("options")).get("scales");
        assertThat(GSON.toJson(scales)).contains("\"stacked\":true");
    }

    @Test
    void theKindsOfTimeStandSideBySideInHours() {
        StatisticReport report = report(D1, D1, List.of(day(D1, StatisticCounts.ZERO,
                new ActivityTimes(3 * HOUR, 90 * 60_000L, 20 * 60_000L, 24 * HOUR))), List.of(), List.of());

        Map<String, Object> config = StatisticCharts.time(report, TEXT);

        assertThat(datasets(config)).hasSize(4);
        assertThat(data(config, 0)).containsExactly(3.0);
        assertThat(data(config, 1)).containsExactly(1.5);
        assertThat(data(config, 2)).containsExactly(0.33);
        assertThat(data(config, 3)).containsExactly(24.0);
        @SuppressWarnings("unchecked")
        Map<String, Object> scales = (Map<String, Object>) ((Map<String, Object>) config.get("options")).get("scales");
        assertThat(GSON.toJson(scales)).as("not stacked: the kinds overlap").contains("\"stacked\":false").doesNotContain("\"stacked\":true");
        assertThat(GSON.toJson(scales)).contains("LABEL_STATISTICS_HOURS");
    }

    @Test
    void aLongRangeIsGroupedByWeekAndAVeryLongOneByMonth() {
        assertThat(StatisticCharts.grainOf(D1, D1.plusDays(89))).isEqualTo(Grain.DAY);
        assertThat(StatisticCharts.grainOf(D1, D1.plusDays(90))).isEqualTo(Grain.WEEK);
        assertThat(StatisticCharts.grainOf(D1, D1.plusDays(365))).isEqualTo(Grain.WEEK);
        assertThat(StatisticCharts.grainOf(D1, D1.plusDays(366))).isEqualTo(Grain.MONTH);

        List<StatisticCharts.Bucket> weeks = StatisticCharts.buckets(D1.plusDays(2), D1.plusDays(120));
        assertThat(weeks.getFirst().from()).isEqualTo(D1.plusDays(2));
        assertThat(weeks.getFirst().to()).as("the week ends on Sunday").isEqualTo(D1.plusDays(6));
        assertThat(weeks.get(1).from()).isEqualTo(D1.plusDays(7));
        assertThat(weeks.getLast().to()).as("the last bar is cut by the range").isEqualTo(D1.plusDays(120));

        List<StatisticCharts.Bucket> months = StatisticCharts.buckets(LocalDate.of(2025, 1, 10), LocalDate.of(2026, 3, 5));
        assertThat(months.getFirst().to()).isEqualTo(LocalDate.of(2025, 1, 31));
        assertThat(months.get(1).from()).isEqualTo(LocalDate.of(2025, 2, 1));
        assertThat(months).hasSize(15);
    }

    @Test
    void theWeeksAddUpTheirDaysAndTheChartSaysItIsGrouped() {
        List<DayLine> days = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            days.add(day(D1.plusDays(i), StatisticCounts.wordsWritten(10), new ActivityTimes(0, HOUR, 0, 0)));
        }
        StatisticReport report = report(D1, D1.plusDays(99), days, List.of(), List.of());

        Map<String, Object> words = StatisticCharts.words(report, TEXT);
        Map<String, Object> time = StatisticCharts.time(report, TEXT);

        assertThat(labels(words)).hasSize(15);
        assertThat(data(words, 0).getFirst()).isEqualTo(70L);
        assertThat(data(words, 0).stream().mapToLong(v -> (Long) v).sum()).isEqualTo(1000L);
        assertThat(data(time, 1).getFirst()).isEqualTo(7.0);
        assertThat(GSON.toJson(words)).contains("MSG_STATISTICS_GROUPED_WEEK");
        assertThat(GSON.toJson(StatisticCharts.words(report(D1, D1.plusDays(5), List.of(), List.of(), List.of()), TEXT)))
                .doesNotContain("MSG_STATISTICS_GROUPED");
    }

    private static SizeSeries.Point point(LocalDate day, boolean present, long words, long branch, long growth, boolean changed) {
        return new SizeSeries.Point(day, present, words, words * 2, branch, branch * 2, growth, growth * 2, growth, growth * 2, true, changed);
    }

    @Test
    void theSizeIsAStepLineAndADayBeforeTheFirstSizeHasNoPoint() {
        StatisticReport report = report(D1, D1.plusDays(3), List.of(), List.of(), List.of(
                point(D1, false, 0, 0, 0, false),
                point(D1.plusDays(1), true, 100, 60, 100, false),
                point(D1.plusDays(2), true, 100, 40, 0, true),
                point(D1.plusDays(3), true, 130, 70, 30, false)));

        Map<String, Object> words = StatisticCharts.size(report, false, TEXT);
        Map<String, Object> tokens = StatisticCharts.size(report, true, TEXT);

        assertThat(words.get("type")).isEqualTo("line");
        assertThat(data(words, 0)).containsExactly(null, 100L, 100L, 130L);
        assertThat(data(words, 1)).containsExactly(null, 60L, 40L, 70L);
        assertThat(datasets(words).get(0).get("stepped")).isEqualTo(true);
        assertThat((List<Object>) datasets(words).get(1).get("pointRadius")).as("the day the branch changed is marked").containsExactly(0, 0, 5, 0);
        assertThat(data(tokens, 0)).containsExactly(null, 200L, 200L, 260L);
        assertThat(GSON.toJson(words)).contains("null");
    }

    @Test
    void theGrowthIsABarPerDayAndNegativeWhenTheBookShrank() {
        StatisticReport report = report(D1, D1.plusDays(2), List.of(), List.of(), List.of(
                point(D1, true, 100, 100, 100, false),
                point(D1.plusDays(1), true, 80, 80, -20, false),
                new SizeSeries.Point(D1.plusDays(2), true, 80, 160, 80, 160, 55, 110, 55, 110, false, false)));

        Map<String, Object> config = StatisticCharts.growth(report, false, TEXT);

        assertThat(data(config, 0)).as("a day with an unknown growth adds nothing").containsExactly(100L, -20L, 0L);
    }

    private static BookLine book(String uuid, String name, String folder, long words, boolean deleted, long openMillis) {
        return new BookLine(new BookStatistic(uuid, name, folder, deleted ? BookState.TRASHED : BookState.PRESENT,
                StatisticCounts.wordsWritten(words), null), new ActivityTimes(openMillis, 0, 0, 0), SizeGrowth.ZERO);
    }

    @Test
    void theBooksAreTheFirstTenAndTheRestIsOther() {
        List<BookLine> books = new ArrayList<>();
        for (int i = 1; i <= 13; i++) {
            books.add(book("00000000-0000-0000-0000-0000000000" + String.format("%02d", i), "Book " + i, "", i * 10L, false, 0));
        }
        StatisticReport report = report(D1, D1, List.of(), books, List.of());

        Map<String, Object> config = StatisticCharts.byBook(report, BookMetric.WORDS, TEXT);

        assertThat(labels(config)).hasSize(StatisticCharts.TOP_BOOKS + 1);
        assertThat(labels(config).getFirst()).isEqualTo("Book 13");
        assertThat(labels(config).getLast()).isEqualTo("LABEL_STATISTICS_OTHER");
        assertThat(data(config, 0).getFirst()).isEqualTo(130.0);
        assertThat(data(config, 0).getLast()).as("10 + 20 + 30").isEqualTo(60.0);
        assertThat(((Map<?, ?>) config.get("options")).get("indexAxis")).isEqualTo("y");
    }

    @Test
    void aDeletedBookIsNamedAndFlaggedAndTwoBooksThatLookAlikeGetATag() {
        BookLine gone = book("aaaaaaaa-0000-0000-0000-000000000001", "Saga", "Fantasy", 50, true, 0);
        BookLine twin = book("bbbbbbbb-0000-0000-0000-000000000002", "Saga", "Fantasy", 40, false, 0);
        BookLine other = book("cccccccc-0000-0000-0000-000000000003", "Saga", "Sci-fi", 30, false, 0);
        StatisticReport report = report(D1, D1, List.of(), List.of(gone, twin, other), List.of());

        Map<String, String> tags = StatisticCharts.duplicateTags(report.books());

        assertThat(tags).containsOnlyKeys(gone.book().bookUuid(), twin.book().bookUuid());
        assertThat(tags.get(gone.book().bookUuid())).isEqualTo("aaaaaaaa");
        assertThat(labels(StatisticCharts.byBook(report, BookMetric.WORDS, TEXT)))
                .containsExactly("Saga #aaaaaaaa (LABEL_SOFT_DELETED)", "Saga #bbbbbbbb", "Saga");
    }

    @Test
    void theTimesOfTheBooksHaveNoOtherAndTheTitleSaysTheyOverlap() {
        List<BookLine> books = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            books.add(book("0000000" + (char) ('a' + i) + "-0000-0000-0000-000000000000", "Book " + i, "", 0, false, i * HOUR));
        }
        StatisticReport report = report(D1, D1, List.of(), books, List.of());

        Map<String, Object> config = StatisticCharts.byBook(report, BookMetric.TIME_OPEN, TEXT);

        assertThat(labels(config)).hasSize(StatisticCharts.TOP_BOOKS).doesNotContain("LABEL_STATISTICS_OTHER");
        assertThat(data(config, 0).getFirst()).isEqualTo(12.0);
        assertThat(GSON.toJson(config)).contains("MSG_STATISTICS_TIMES_OVERLAP");
    }

}
