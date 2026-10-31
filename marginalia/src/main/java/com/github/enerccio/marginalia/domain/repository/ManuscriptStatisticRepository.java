package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.components.*;
import com.github.enerccio.marginalia.domain.model.impl.ManuscriptStatistic;
import com.github.enerccio.marginalia.domain.security.model.User;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface ManuscriptStatisticRepository extends OwnedRepository<ManuscriptStatistic> {

    /**
     * Adds to the counters of the row of the book, day and side, in SQL so parallel additions don't lose counts, and makes
     * the name and the folder path of the row the given ones.
     *
     * @return number of updated rows, 0 when there is none yet
     */
    int add(User owner, String bookUuid, LocalDate day, boolean agent, StatisticCounts delta, String bookName,
            String folderPath, Date now) throws Exception;

    /**
     * @return the row of the book, day and side of the user, null if there is none
     */
    ManuscriptStatistic find(User owner, String bookUuid, LocalDate day, boolean agent) throws Exception;

    /**
     * The rows of the filter, the oldest day first.
     */
    List<ManuscriptStatistic> findRange(User owner, StatisticFilter filter) throws Exception;

    /**
     * @return the sum of the counters of the filter, zero when there are no rows
     */
    StatisticCounts sum(User owner, StatisticFilter filter) throws Exception;

    /**
     * The sums per day, the oldest first; the days without a row are not in the list.
     */
    List<DayStatistic> sumByDay(User owner, StatisticFilter filter) throws Exception;

    /**
     * The sums per book (by its uuid), with the name and the folder path its rows keep and whether the book still exists;
     * the books without a row in the range are not in the list. The filter's book is ignored.
     */
    List<BookStatistic> sumByBook(User owner, StatisticFilter filter) throws Exception;

    /**
     * The sums per exact folder path.
     */
    List<FolderStatistic> sumByFolder(User owner, StatisticFilter filter) throws Exception;

    /**
     * Makes the name and the folder path of all the rows of the book the given ones (a rename, a move).
     *
     * @param folderPath null leaves the path as it is
     * @return number of updated rows
     */
    int refreshBook(User owner, String bookUuid, String bookName, String folderPath) throws Exception;

    /**
     * Deletes (for good) the rows of the book.
     *
     * @return number of deleted rows
     */
    int deleteByBook(User owner, String bookUuid) throws Exception;

    /**
     * Deletes (for good) all the rows of the user.
     *
     * @return number of deleted rows
     */
    int deleteAll(User owner) throws Exception;

}
