package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.collections.ActivityKind;
import com.github.enerccio.marginalia.domain.components.ActivityRow;
import com.github.enerccio.marginalia.domain.components.StatisticFilter;
import com.github.enerccio.marginalia.domain.model.impl.ActivityInterval;
import com.github.enerccio.marginalia.domain.security.model.User;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface ActivityIntervalRepository extends OwnedRepository<ActivityInterval> {

    /**
     * Moves the end of a stretch (to now, while it is open).
     *
     * @return number of updated rows
     */
    int moveEnd(Long id, Date endTime) throws Exception;

    /**
     * The stretches of the filter ordered by start; only the columns the union of the times reads. The side of the filter
     * is matched with the {@code agent} flag of the rows.
     *
     * @param kinds the kinds to read, null or empty for all
     */
    List<ActivityRow> findRows(User owner, StatisticFilter filter, Collection<ActivityKind> kinds) throws Exception;

    /**
     * Makes the name and the folder path of all the rows of the book the given ones.
     *
     * @param folderPath null leaves the path as it is
     * @return number of updated rows
     */
    int refreshBook(User owner, String bookUuid, String bookName, String folderPath) throws Exception;

    int deleteByBook(User owner, String bookUuid) throws Exception;

    int deleteAll(User owner) throws Exception;

}
