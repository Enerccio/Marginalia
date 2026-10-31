package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.components.StatisticFilter;
import com.github.enerccio.marginalia.domain.components.StatisticReport;
import com.github.enerccio.marginalia.domain.components.WritingActivity;

/**
 * Puts the three kinds of statistics ({@link ManuscriptStatisticService}, {@link ActivityIntervalService} and
 * {@link ManuscriptSizeService}) of a filter together into the report that the statistics windows and the commands show. Reads
 * the rows of the current user.
 */
public interface StatisticReportService {

    /**
     * The report of the filter. A filter that starts at {@code StatisticRange#BEGINNING} (all the time) is narrowed to the first
     * day that has anything, the report holds the filter that was read. A filter of a book gives no lines of books.
     */
    StatisticReport report(StatisticFilter filter) throws Exception;

    /**
     * The days the current user worked on, from the first one that has anything to today, for the calendar and the streaks: of one
     * book, or of all the books (with the goal of the user, which is a goal over all of them; a book has none). Only the author's
     * side makes a day, see {@link WritingActivity}.
     */
    WritingActivity activity(String bookUuid) throws Exception;

}
