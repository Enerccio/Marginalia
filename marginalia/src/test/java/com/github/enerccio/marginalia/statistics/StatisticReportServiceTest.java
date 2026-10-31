package com.github.enerccio.marginalia.statistics;

import com.github.enerccio.marginalia.domain.collections.ActivityKind;
import com.github.enerccio.marginalia.domain.collections.StatisticRange;
import com.github.enerccio.marginalia.domain.collections.StatisticSide;
import com.github.enerccio.marginalia.domain.components.BookStatistic.BookState;
import com.github.enerccio.marginalia.domain.components.SizeSeries;
import com.github.enerccio.marginalia.domain.components.StatisticCounts;
import com.github.enerccio.marginalia.domain.components.StatisticFilter;
import com.github.enerccio.marginalia.domain.components.StatisticReport;
import com.github.enerccio.marginalia.domain.components.StatisticReport.BookLine;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.ManuscriptSizeServiceImpl;
import com.github.enerccio.marginalia.domain.service.impl.ManuscriptStatisticServiceImpl;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.AopTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The report of the statistics windows and of the commands: the counts, the times and the sizes of a book or of all the books put
 * together, with the lines of the days and of the books.
 */
class StatisticReportServiceTest extends MarginaliaTestBase {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 20);
    private static final long MINUTE = 60_000L;

    @Autowired
    private StatisticReportService reportService;

    @Autowired
    private ManuscriptStatisticService statisticService;

    @Autowired
    private ActivityIntervalService intervalService;

    @Autowired
    private ManuscriptSizeService sizeService;

    @Autowired
    private ManuscriptService manuscriptService;

    private Manuscript book;
    private Manuscript other;

    @BeforeEach
    void setUp() throws Exception {
        loginAdmin();
        // the size worker would write rows for today
        ((ManuscriptSizeServiceImpl) AopTestUtils.getUltimateTargetObject(sizeService)).setManual(true);
        setDay(DAY);
        book = book("The Saga");
        other = book("The Other");
    }

    @AfterEach
    void restoreClock() {
        ((ManuscriptStatisticServiceImpl) AopTestUtils.getUltimateTargetObject(statisticService)).setClock(Clock.systemDefaultZone());
        ((ManuscriptSizeServiceImpl) AopTestUtils.getUltimateTargetObject(sizeService)).setManual(false);
    }

    private void setDay(LocalDate day) {
        ((ManuscriptStatisticServiceImpl) AopTestUtils.getUltimateTargetObject(statisticService))
                .setClock(Clock.fixed(day.atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()));
    }

    private Manuscript book(String name) throws Exception {
        Manuscript manuscript = new Manuscript();
        manuscript.setName(name);
        return manuscriptService.save(manuscript);
    }

    private Instant at(LocalDate day, int hour, int minute) {
        return day.atTime(hour, minute).atZone(ZoneId.systemDefault()).toInstant();
    }

    private void stretch(Manuscript of, ActivityKind kind, boolean agent, LocalDate day, int hour, int minute, long minutes) throws Exception {
        Instant start = at(day, hour, minute);
        intervalService.extend(intervalService.start(of, kind, agent, Date.from(start)).getId(), Date.from(start.plusMillis(minutes * MINUTE)));
    }

    private static StatisticFilter filter(LocalDate from, LocalDate to, StatisticSide side) {
        return StatisticFilter.of(from, to, side);
    }

    private BookLine line(StatisticReport report, Manuscript of) {
        return report.books().stream().filter(l -> l.book().bookUuid().equals(of.getUuid())).findFirst().orElseThrow();
    }

    @Test
    void theReportOfABookHasItsCountsTimesAndSizeByDay() throws Exception {
        statisticService.count(book, StatisticCounts.wordsWritten(100).plus(StatisticCounts.partsCreated(2)));
        setDay(DAY.plusDays(2));
        statisticService.count(book, StatisticCounts.wordsGenerated(40).plus(StatisticCounts.wordsRemoved(10)));
        stretch(book, ActivityKind.OPEN, false, DAY, 9, 0, 30);
        stretch(book, ActivityKind.EDIT, false, DAY, 9, 10, 10);
        sizeService.set(book, DAY, 100, 130, 100, 130, "leaf", false);
        sizeService.set(book, DAY.plusDays(2), 130, 170, 130, 170, "leaf", false);

        StatisticReport report = reportService.report(filter(DAY, DAY.plusDays(3), StatisticSide.ALL).forBook(book.getUuid()));

        assertThat(report.total().wordsWritten()).isEqualTo(100);
        assertThat(report.total().wordsGenerated()).isEqualTo(40);
        assertThat(report.times().open()).isEqualTo(30 * MINUTE);
        assertThat(report.times().edit()).isEqualTo(10 * MINUTE);
        assertThat(report.days()).extracting(StatisticReport.DayLine::day).containsExactly(DAY, DAY.plusDays(2));
        assertThat(report.days().getFirst().times().open()).isEqualTo(30 * MINUTE);
        assertThat(report.days().get(1).counts().wordsRemoved()).isEqualTo(10);
        assertThat(report.sizes()).extracting(SizeSeries.Point::bookWords).containsExactly(100L, 100L, 130L, 130L);
        assertThat(report.sizes().get(2).growthWords()).isEqualTo(30);
        assertThat(report.growth().bookWords()).as("a book born in the range grows from nothing").isEqualTo(130);
        assertThat(report.books()).as("a report of a book has no lines of books").isEmpty();
    }

    @Test
    void theSideDecidesWhoseWorkIsCounted() throws Exception {
        statisticService.count(book, StatisticCounts.wordsWritten(100));
        statisticService.count(book, true, StatisticCounts.wordsGenerated(300));
        stretch(book, ActivityKind.EDIT, false, DAY, 9, 0, 10);
        stretch(book, ActivityKind.RUN, true, DAY, 9, 5, 20);

        StatisticFilter day = filter(DAY, DAY, StatisticSide.ALL);
        StatisticReport all = reportService.report(day);
        StatisticReport mine = reportService.report(filter(DAY, DAY, StatisticSide.AUTHOR));
        StatisticReport agents = reportService.report(filter(DAY, DAY, StatisticSide.AGENTS));

        assertThat(all.total().wordsAdded()).isEqualTo(400);
        assertThat(mine.total().wordsAdded()).isEqualTo(100);
        assertThat(mine.times().run()).isZero();
        assertThat(mine.times().edit()).isEqualTo(10 * MINUTE);
        assertThat(agents.total().wordsAdded()).isEqualTo(300);
        assertThat(agents.times().edit()).isZero();
        assertThat(agents.times().run()).isEqualTo(20 * MINUTE);
    }

    @Test
    void theReportOfAllTheBooksHasALineForEachAndTheTimeIsTheUnion() throws Exception {
        statisticService.count(book, StatisticCounts.wordsWritten(100));
        statisticService.count(other, StatisticCounts.wordsWritten(300));
        stretch(book, ActivityKind.OPEN, false, DAY, 9, 0, 30);
        stretch(other, ActivityKind.OPEN, false, DAY, 9, 20, 30);
        sizeService.set(book, DAY.minusDays(3), 50, 60, 50, 60, "leaf", false);
        sizeService.set(book, DAY, 100, 120, 100, 120, "leaf", false);
        sizeService.set(other, DAY, 300, 360, 300, 360, "leaf", false);

        StatisticReport report = reportService.report(filter(DAY, DAY, StatisticSide.ALL));

        assertThat(report.times().open()).as("9:00-9:30 and 9:20-9:50 are 50 minutes, not 60").isEqualTo(50 * MINUTE);
        assertThat(report.books()).hasSize(2);
        assertThat(line(report, book).book().bookName()).isEqualTo("The Saga");
        assertThat(line(report, book).times().open()).isEqualTo(30 * MINUTE);
        assertThat(line(report, other).times().open()).isEqualTo(30 * MINUTE);
        assertThat(line(report, book).growth().bookWords()).isEqualTo(50);
        assertThat(line(report, other).growth().bookWords()).isEqualTo(300);
        assertThat(report.growth().bookWords()).isEqualTo(350);
        assertThat(report.sizes().getFirst().bookWords()).isEqualTo(400);
    }

    @Test
    void aBookThatOnlyWasOpenedIsOnTheListWithItsName() throws Exception {
        stretch(book, ActivityKind.OPEN, false, DAY, 9, 0, 5);

        StatisticReport report = reportService.report(filter(DAY, DAY, StatisticSide.ALL));

        assertThat(report.books()).hasSize(1);
        assertThat(report.books().getFirst().book().bookName()).isEqualTo("The Saga");
        assertThat(report.books().getFirst().book().state()).isEqualTo(BookState.PRESENT);
        assertThat(report.books().getFirst().book().counts().isZero()).isTrue();
        assertThat(report.books().getFirst().times().open()).isEqualTo(5 * MINUTE);
    }

    @Test
    void aBookInTheTrashIsFlaggedAndKeepsItsLine() throws Exception {
        statisticService.count(book, StatisticCounts.wordsWritten(100));
        manuscriptService.delete(manuscriptService.find(book.getId()), false);

        StatisticReport report = reportService.report(filter(DAY, DAY, StatisticSide.ALL));

        assertThat(line(report, book).book().deleted()).isTrue();
        assertThat(line(report, book).book().counts().wordsWritten()).isEqualTo(100);
        assertThat(report.total().wordsWritten()).isEqualTo(100);
    }

    @Test
    void allTheTimeStartsAtTheFirstDayThatHasAnything() throws Exception {
        setDay(DAY.minusDays(40));
        statisticService.count(book, StatisticCounts.wordsWritten(5));
        setDay(DAY);
        stretch(book, ActivityKind.OPEN, false, DAY, 9, 0, 5);

        StatisticReport report = reportService.report(filter(StatisticRange.BEGINNING, DAY, StatisticSide.ALL));

        assertThat(report.filter().from()).isEqualTo(DAY.minusDays(40));
        assertThat(report.filter().to()).isEqualTo(DAY);
        assertThat(report.sizes()).hasSize(41);
        assertThat(report.days()).extracting(StatisticReport.DayLine::day).containsExactly(DAY.minusDays(40), DAY);
    }

    @Test
    void aRangeWithNothingIsEmptyAndKeepsItsDays() throws Exception {
        StatisticReport report = reportService.report(filter(DAY.plusDays(10), DAY.plusDays(12), StatisticSide.ALL));

        assertThat(report.isEmpty()).isTrue();
        assertThat(report.books()).isEmpty();
        assertThat(report.sizes()).hasSize(3).extracting(SizeSeries.Point::present).containsOnly(false);
    }

    @Test
    void allTheTimeOfNothingIsToday() throws Exception {
        StatisticReport report = reportService.report(filter(StatisticRange.BEGINNING, DAY, StatisticSide.ALL));

        assertThat(report.filter().from()).isEqualTo(DAY);
        assertThat(report.sizes()).hasSize(1);
    }

}
