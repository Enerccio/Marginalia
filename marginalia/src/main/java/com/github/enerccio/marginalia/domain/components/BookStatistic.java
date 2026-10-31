package com.github.enerccio.marginalia.domain.components;

import java.util.Date;

/**
 * The counters of one book over a range, the line of the breakdown by book.
 *
 * @param bookName   the name the rows of the book keep, the last one for a book that is gone
 * @param folderPath the path of the folder the rows keep, empty for a book in no folder
 * @param state      whether the book still exists, see {@link BookState}
 * @param lastChange the last count of the range
 */
public record BookStatistic(String bookUuid, String bookName, String folderPath, BookState state, StatisticCounts counts,
                            Date lastChange) {

    /**
     * What became of the book the statistics are about.
     */
    public enum BookState {
        PRESENT,
        /**
         * In the trash, it comes back with its statistics when restored.
         */
        TRASHED,
        /**
         * Purged: only the statistics are left.
         */
        PURGED
    }

    public boolean deleted() {
        return state != BookState.PRESENT;
    }

}
