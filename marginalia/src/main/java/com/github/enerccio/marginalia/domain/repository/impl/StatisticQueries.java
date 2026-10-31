package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.components.StatisticFilter;
import com.github.enerccio.marginalia.domain.security.model.User;
import jakarta.persistence.Query;

/**
 * The part of a query that the three statistics tables share: the user, the days, the side and the book of a
 * {@link StatisticFilter}, on rows with the same column names.
 */
final class StatisticQueries {

    private StatisticQueries() {
    }

    /**
     * The WHERE of a filter on rows aliased {@code alias}; the parameters are set by {@link #bind}.
     */
    static String where(String alias, StatisticFilter filter) {
        StringBuilder where = new StringBuilder(alias + ".owner.id = :owner AND " + alias + ".deleted = false AND "
                + alias + ".day >= :from AND " + alias + ".day <= :to");
        if (filter.side().agent() != null) {
            where.append(" AND ").append(alias).append(".agent = :agent");
        }
        if (filter.bookUuid() != null) {
            where.append(" AND ").append(alias).append(".bookUuid = :book");
        }
        return where.toString();
    }

    static <Q extends Query> Q bind(Q query, User owner, StatisticFilter filter) {
        query.setParameter("owner", owner.getId());
        query.setParameter("from", filter.from());
        query.setParameter("to", filter.to());
        if (filter.side().agent() != null) {
            query.setParameter("agent", filter.side().agent());
        }
        if (filter.bookUuid() != null) {
            query.setParameter("book", filter.bookUuid());
        }
        return query;
    }
}
