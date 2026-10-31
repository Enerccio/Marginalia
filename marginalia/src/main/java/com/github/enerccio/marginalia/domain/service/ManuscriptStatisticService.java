package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.components.*;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.ManuscriptStatistic;
import com.github.enerccio.marginalia.domain.repository.ManuscriptStatisticRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * What happened to a book, day by day: the tokens spent on it, the words written, generated and removed, the parts made and
 * deleted, the edits ({@link ManuscriptStatistic}); with {@link ActivityIntervalService} (the time) and
 * {@link ManuscriptSizeService} (the size of the book) they are the statistics of the writing. The statistics are the author's
 * record of their work, not a part of the book: they hold the book by its uuid and outlive it, and are cleared only by the
 * user ({@link #clearBook}, {@link #clearAll}). Not to be confused with {@link UsageService}, which counts what a user used
 * of an AI and knows nothing of books.
 * <p>
 * The days are the days of the time zone of the server. The reads are of the rows of the current user, the writes of the owner
 * of the book, so the counting works on a thread without a session.
 */
public interface ManuscriptStatisticService extends OwnedService<ManuscriptStatistic, ManuscriptStatisticRepository> {

    /**
     * Adds the counts to today's row of the author's side of the book, creating the row on the first count of the day.
     * Never throws: a failure to count is logged and must not fail the work that is counted. Nothing is counted for a
     * book that is not saved or an empty change.
     *
     * @see #count(Manuscript, boolean, StatisticCounts)
     */
    void count(Manuscript book, StatisticCounts delta);

    /**
     * Adds the counts to today's row of the book for the side: the author's, or the agents' ({@code agent}).
     */
    void count(Manuscript book, boolean agent, StatisticCounts delta);

    /**
     * @return the row of the book, day and side of the current user, null if there is none
     */
    ManuscriptStatistic find(String bookUuid, LocalDate day, boolean agent) throws Exception;

    /**
     * The rows of the filter of the current user, the oldest day first.
     */
    List<ManuscriptStatistic> findRange(StatisticFilter filter) throws Exception;

    /**
     * The sum of the counters of the filter, zero when there are no rows.
     */
    StatisticCounts total(StatisticFilter filter) throws Exception;

    /**
     * The sums per day, the oldest first; the days without a row are not in the list.
     */
    List<DayStatistic> sumByDay(StatisticFilter filter) throws Exception;

    /**
     * The sums per book, also of the books that are in the trash or gone ({@link BookStatistic#state()}); the book of the
     * filter is ignored.
     */
    List<BookStatistic> sumByBook(StatisticFilter filter) throws Exception;

    /**
     * The sums per exact folder path.
     */
    List<FolderStatistic> sumByFolder(StatisticFilter filter) throws Exception;

    /**
     * Clears the statistics of the book, of all three kinds (the counts, the time and the size), for good. The user's own
     * act, also for a book that is gone.
     */
    void clearBook(String bookUuid) throws Exception;

    /**
     * Clears all the statistics of the current user, for good.
     */
    void clearAll() throws Exception;

    /**
     * Today, the day of the time zone of the server.
     */
    LocalDate today();

    /**
     * The path of the folder of the book as the statistics keep it ({@link FolderService#getPath}), empty for a book in no
     * folder. Needs no current user.
     */
    String folderPathOf(Manuscript book) throws Exception;

}
