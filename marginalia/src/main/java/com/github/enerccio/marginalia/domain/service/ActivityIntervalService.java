package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.collections.ActivityKind;
import com.github.enerccio.marginalia.domain.components.ActivityRow;
import com.github.enerccio.marginalia.domain.components.ActivityTimes;
import com.github.enerccio.marginalia.domain.components.StatisticFilter;
import com.github.enerccio.marginalia.domain.model.impl.ActivityInterval;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.ActivityIntervalRepository;

import java.time.LocalDate;
import java.util.*;

/**
 * The stretches of time ({@link ActivityInterval}) during which the book was open, edited, generated for or worked on by an
 * agent. The writes are for the tracker that turns what is going on into stretches; the reads give the union of the times
 * the stretches to add up. The writes work on a thread without a session, the owner of a stretch is the owner of the book.
 */
public interface ActivityIntervalService extends OwnedService<ActivityInterval, ActivityIntervalRepository> {

    /**
     * Opens a stretch of the kind in the book, starting (and for now ending) at the time. The stretch is inside the day of the
     * start, the caller cuts it at midnight.
     */
    ActivityInterval start(Manuscript book, ActivityKind kind, boolean agent, Date startTime) throws Exception;

    /**
     * Moves the end of a stretch forward.
     */
    void extend(Long id, Date endTime) throws Exception;

    /**
     * The stretches of the filter of the current user, ordered by start.
     *
     * @param kinds the kinds to read, null or empty for all
     */
    List<ActivityRow> findRows(StatisticFilter filter, Collection<ActivityKind> kinds) throws Exception;

    /**
     * The time of each kind in the filter of the current user: the union of the stretches of the kind ({@code TimeUnion}), so
     * books that were going on together are counted once. The side of the filter decides whose stretches there are, {@code ALL}
     * puts both sides together before the union.
     */
    ActivityTimes times(StatisticFilter filter) throws Exception;

    /**
     * The same for each day of the range that has a stretch.
     */
    SortedMap<LocalDate, ActivityTimes> timesByDay(StatisticFilter filter) throws Exception;

    /**
     * The own time of each book that has a stretch in the range (the deleted ones too). The books overlap, so the times are not
     * parts of {@link #times}.
     */
    Map<String, ActivityTimes> timesByBook(StatisticFilter filter) throws Exception;

    void clearBook(String bookUuid) throws Exception;

    void clearAll() throws Exception;

}
