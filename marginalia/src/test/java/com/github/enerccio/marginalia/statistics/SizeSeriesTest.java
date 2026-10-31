package com.github.enerccio.marginalia.statistics;

import com.github.enerccio.marginalia.domain.components.SizeGrowth;
import com.github.enerccio.marginalia.domain.components.SizeSeries;
import com.github.enerccio.marginalia.domain.components.SizeSeries.Point;
import com.github.enerccio.marginalia.domain.model.impl.ManuscriptSize;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The size of the books on each day of a range, from the rows of the days on which it changed.
 */
class SizeSeriesTest {

    private static final LocalDate D1 = LocalDate.of(2026, 3, 1);
    private static final LocalDate D2 = D1.plusDays(1);
    private static final LocalDate D3 = D1.plusDays(2);
    private static final LocalDate D4 = D1.plusDays(3);

    private static ManuscriptSize row(String book, LocalDate day, long words, long branchWords, String leaf, boolean baseline) {
        ManuscriptSize size = new ManuscriptSize();
        size.setBookUuid(book);
        size.setDay(day);
        size.setBookWords(words);
        size.setBookTokens(words * 2);
        size.setBranchWords(branchWords);
        size.setBranchTokens(branchWords * 2);
        size.setLeafUuid(leaf);
        size.setBaseline(baseline);
        return size;
    }

    private static ManuscriptSize row(String book, LocalDate day, long words) {
        return row(book, day, words, words, "leaf", false);
    }

    @Test
    void aDayWithoutARowHasTheSizeOfTheLastOneBeforeIt() {
        List<Point> points = SizeSeries.build(List.of(row("a", D1.minusDays(5), 100)), List.of(row("a", D3, 130)), D1, D4);

        assertThat(points).extracting(Point::bookWords).containsExactly(100L, 100L, 130L, 130L);
        assertThat(points).extracting(Point::growthWords).containsExactly(0L, 0L, 30L, 0L);
        assertThat(points).extracting(Point::present).containsOnly(true);
    }

    @Test
    void aBookBornInTheRangeGrowsFromNothingAndTheDaysBeforeHaveNoSize() {
        List<Point> points = SizeSeries.build(List.of(), List.of(row("a", D2, 40)), D1, D3);

        assertThat(points).extracting(Point::present).containsExactly(false, true, true);
        assertThat(points.get(1).growthWords()).isEqualTo(40);
        assertThat(points.get(1).growthKnown()).isTrue();
        assertThat(points.get(0).bookWords()).isZero();
    }

    @Test
    void aBaselineRowSaysHowBigTheBookWasAndNothingAboutTheChange() {
        List<Point> points = SizeSeries.build(List.of(), List.of(row("a", D1, 80, 80, "leaf", true), row("a", D3, 90)), D1, D3);

        assertThat(points.get(0).bookWords()).isEqualTo(80);
        assertThat(points.get(0).growthWords()).isZero();
        assertThat(points.get(0).growthKnown()).isFalse();
        assertThat(points.get(2).growthWords()).isEqualTo(10);
        assertThat(points.get(2).growthKnown()).isTrue();
    }

    @Test
    void theBooksAreAddedUpAndTheyShrinkToo() {
        List<Point> points = SizeSeries.build(
                List.of(row("a", D1.minusDays(1), 100), row("b", D1.minusDays(1), 50)),
                List.of(row("a", D2, 70), row("b", D2, 60)), D1, D2);

        assertThat(points.get(0).bookWords()).isEqualTo(150);
        assertThat(points.get(1).bookWords()).isEqualTo(130);
        assertThat(points.get(1).growthWords()).as("-30 and +10").isEqualTo(-20);
        assertThat(points.get(1).growthTokens()).isEqualTo(-40);
    }

    @Test
    void aDayOnWhichTheBranchChangedIsMarked() {
        List<Point> points = SizeSeries.build(List.of(row("a", D1.minusDays(1), 100, 60, "one", false)),
                List.of(row("a", D2, 100, 40, "two", false)), D1, D2);

        assertThat(points.get(0).branchChanged()).isFalse();
        assertThat(points.get(1).branchChanged()).isTrue();
        assertThat(points.get(1).bookWords()).isEqualTo(100);
        assertThat(points.get(1).branchWords()).isEqualTo(40);
        assertThat(points.get(1).branchGrowthWords()).isEqualTo(-20);
    }

    @Test
    void theGrowthOfEachBookIsTheLastRowMinusTheOneBeforeTheRange() {
        Map<String, SizeGrowth> growth = SizeSeries.perBook(
                List.of(row("old", D1.minusDays(3), 100)),
                List.of(row("old", D2, 120), row("old", D3, 150), row("born", D2, 10),
                        row("seen", D3, 70, 70, "leaf", true)));

        assertThat(growth.get("old").bookWords()).isEqualTo(50);
        assertThat(growth.get("old").bookTokens()).isEqualTo(100);
        assertThat(growth.get("born").bookWords()).as("from nothing").isEqualTo(10);
        assertThat(growth.get("seen").known()).as("only a baseline is known").isFalse();
    }

    @Test
    void aBookThatDidNotChangeHasNoGrowthAndTheUnknownOnesAreLeftOutOfTheSum() {
        Map<String, SizeGrowth> growth = SizeSeries.perBook(List.of(row("quiet", D1.minusDays(2), 100)),
                List.of(row("seen", D2, 70, 70, "leaf", true), row("born", D2, 30)));

        assertThat(growth.get("quiet")).isEqualTo(SizeGrowth.ZERO);
        assertThat(SizeSeries.sum(growth.values()).bookWords()).isEqualTo(30);
    }

}
