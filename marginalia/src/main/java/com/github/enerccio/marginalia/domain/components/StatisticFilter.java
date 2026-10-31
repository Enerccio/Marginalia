package com.github.enerccio.marginalia.domain.components;

import com.github.enerccio.marginalia.domain.collections.StatisticSide;

import java.time.LocalDate;

/**
 * Which rows of the statistics are read: the days (both ends included, in the zone of the server), a side, and one book or
 * all of them. The rows are always the ones of the current user.
 *
 * @param bookUuid the book by its uuid, null for all the books (also the deleted ones, see the statistics of a book that is gone)
 */
public record StatisticFilter(LocalDate from, LocalDate to, StatisticSide side, String bookUuid) {

    public StatisticFilter {
        if (from == null || to == null) {
            throw new IllegalArgumentException("A range needs both of its dates");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("The range ends before it starts");
        }
        side = side == null ? StatisticSide.ALL : side;
    }

    public static StatisticFilter of(LocalDate from, LocalDate to, StatisticSide side) {
        return new StatisticFilter(from, to, side, null);
    }

    public StatisticFilter forBook(String bookUuid) {
        return new StatisticFilter(from, to, side, bookUuid);
    }

}
