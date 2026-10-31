package com.github.enerccio.marginalia.domain.components;

import java.time.LocalDate;
import java.util.List;

/**
 * Everything the statistics windows and the commands show for a filter: the counts, the times (unions, see {@code TimeUnion}) and
 * the size of the books, in total, by day and by book.
 *
 * @param filter the days, the side and the book the report is about; its days are the ones that were read
 * @param total  the counts of the whole range
 * @param times  the time of the whole range: the union over the books, not the sum of the lines
 * @param days   the days that have a count, a stretch of time or a size row, the oldest first
 * @param books  the books that have anything in the range, one line each (also the deleted ones); only for a report of all the books
 * @param sizes  the size on every day of the range
 * @param growth the growth of the size over the range, summed over the books
 */
public record StatisticReport(StatisticFilter filter, StatisticCounts total, ActivityTimes times, List<DayLine> days,
                              List<BookLine> books, List<SizeSeries.Point> sizes, SizeGrowth growth) {

    /**
     * One day of the range: the counts and the time of the day, the size and growth are in {@link #sizes} (same day).
     */
    public record DayLine(LocalDate day, StatisticCounts counts, ActivityTimes times) {
    }

    /**
     * One book: its counts, its own time (the times of books overlap, so the lines are not parts of the total) and its growth.
     */
    public record BookLine(BookStatistic book, ActivityTimes times, SizeGrowth growth) {
    }

    public boolean isEmpty() {
        return total.isZero() && times.isZero() && days.isEmpty();
    }

}
