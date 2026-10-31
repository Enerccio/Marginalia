package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.collections.ActivityKind;
import com.github.enerccio.marginalia.domain.components.ActivityRow;
import com.github.enerccio.marginalia.domain.components.StatisticFilter;
import com.github.enerccio.marginalia.domain.model.impl.ActivityInterval;
import com.github.enerccio.marginalia.domain.repository.ActivityIntervalRepository;
import com.github.enerccio.marginalia.domain.security.model.User;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public class JpaActivityIntervalRepository extends JpaOwnedRepository<ActivityInterval> implements ActivityIntervalRepository {

    @Override
    protected Class<ActivityInterval> getEntityClass() {
        return ActivityInterval.class;
    }

    @Override
    public int moveEnd(Long id, Date endTime) {
        return entityManager
                .createQuery("UPDATE ActivityInterval i SET i.endTime = :end WHERE i.id = :id")
                .setParameter("end", endTime)
                .setParameter("id", id)
                .executeUpdate();
    }

    @Override
    public List<ActivityRow> findRows(User owner, StatisticFilter filter, Collection<ActivityKind> kinds) {
        boolean limited = kinds != null && !kinds.isEmpty();
        TypedQuery<ActivityRow> query = entityManager.createQuery("SELECT new com.github.enerccio.marginalia.domain.components.ActivityRow("
                + "i.bookUuid, i.kind, i.agent, i.day, i.startTime, i.endTime) FROM ActivityInterval i WHERE "
                + StatisticQueries.where("i", filter) + (limited ? " AND i.kind IN :kinds" : "")
                + " ORDER BY i.startTime, i.id", ActivityRow.class);
        StatisticQueries.bind(query, owner, filter);
        if (limited) {
            query.setParameter("kinds", kinds);
        }
        return query.getResultList();
    }

    @Override
    public int refreshBook(User owner, String bookUuid, String bookName, String folderPath) {
        // only the rows that differ are written: a book that is saved again and again has the same name
        String name = bookName == null ? "" : bookName;
        String path = folderPath == null ? "" : ", i.folderPath = :path";
        String differs = "COALESCE(i.bookName, '') <> :name" + (folderPath == null ? "" : " OR COALESCE(i.folderPath, '') <> :path");
        Query query = entityManager
                .createQuery("UPDATE ActivityInterval i SET i.bookName = :name" + path
                        + " WHERE i.bookUuid = :book AND i.owner.id = :owner AND (" + differs + ")")
                .setParameter("name", name)
                .setParameter("book", bookUuid)
                .setParameter("owner", owner.getId());
        if (folderPath != null) {
            query.setParameter("path", folderPath);
        }
        return query.executeUpdate();
    }

    @Override
    public int deleteByBook(User owner, String bookUuid) {
        return entityManager
                .createQuery("DELETE FROM ActivityInterval i WHERE i.bookUuid = :book AND i.owner.id = :owner")
                .setParameter("book", bookUuid)
                .setParameter("owner", owner.getId())
                .executeUpdate();
    }

    @Override
    public int deleteAll(User owner) {
        return entityManager
                .createQuery("DELETE FROM ActivityInterval i WHERE i.owner.id = :owner")
                .setParameter("owner", owner.getId())
                .executeUpdate();
    }
}
