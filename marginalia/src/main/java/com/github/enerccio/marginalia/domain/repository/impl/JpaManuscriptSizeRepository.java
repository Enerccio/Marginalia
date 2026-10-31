package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.ManuscriptSize;
import com.github.enerccio.marginalia.domain.repository.ManuscriptSizeRepository;
import com.github.enerccio.marginalia.domain.security.model.User;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;

public class JpaManuscriptSizeRepository extends JpaOwnedRepository<ManuscriptSize> implements ManuscriptSizeRepository {

    @Override
    protected Class<ManuscriptSize> getEntityClass() {
        return ManuscriptSize.class;
    }

    @Override
    public ManuscriptSize find(User owner, String bookUuid, LocalDate day) {
        List<ManuscriptSize> found = entityManager
                .createQuery("SELECT s FROM ManuscriptSize s WHERE s.bookUuid = :book AND s.day = :day AND s.owner.id = :owner "
                        + "AND s.deleted = false", ManuscriptSize.class)
                .setParameter("book", bookUuid)
                .setParameter("day", day)
                .setParameter("owner", owner.getId())
                .setMaxResults(1)
                .getResultList();
        return found.isEmpty() ? null : found.getFirst();
    }

    @Override
    public ManuscriptSize findAtOrBefore(User owner, String bookUuid, LocalDate day) {
        return latest(owner, bookUuid, day, "<=");
    }

    @Override
    public ManuscriptSize findBefore(User owner, String bookUuid, LocalDate day) {
        return latest(owner, bookUuid, day, "<");
    }

    private ManuscriptSize latest(User owner, String bookUuid, LocalDate day, String comparison) {
        List<ManuscriptSize> found = entityManager
                .createQuery("SELECT s FROM ManuscriptSize s WHERE s.bookUuid = :book AND s.day " + comparison + " :day "
                        + "AND s.owner.id = :owner AND s.deleted = false ORDER BY s.day DESC", ManuscriptSize.class)
                .setParameter("book", bookUuid)
                .setParameter("day", day)
                .setParameter("owner", owner.getId())
                .setMaxResults(1)
                .getResultList();
        return found.isEmpty() ? null : found.getFirst();
    }

    @Override
    public List<ManuscriptSize> findAllAtOrBefore(User owner, LocalDate day) {
        return entityManager
                .createQuery("SELECT s FROM ManuscriptSize s WHERE s.owner.id = :owner AND s.deleted = false AND s.day = ("
                        + "SELECT MAX(l.day) FROM ManuscriptSize l WHERE l.bookUuid = s.bookUuid AND l.owner.id = :owner "
                        + "AND l.deleted = false AND l.day <= :day) ORDER BY s.bookName, s.bookUuid", ManuscriptSize.class)
                .setParameter("owner", owner.getId())
                .setParameter("day", day)
                .getResultList();
    }

    @Override
    public List<ManuscriptSize> findRange(User owner, String bookUuid, LocalDate from, LocalDate to) {
        TypedQuery<ManuscriptSize> query = entityManager
                .createQuery("SELECT s FROM ManuscriptSize s WHERE s.owner.id = :owner AND s.deleted = false AND s.day >= :from "
                        + "AND s.day <= :to" + (bookUuid == null ? "" : " AND s.bookUuid = :book")
                        + " ORDER BY s.day, s.bookUuid", ManuscriptSize.class)
                .setParameter("owner", owner.getId())
                .setParameter("from", from)
                .setParameter("to", to);
        if (bookUuid != null) {
            query.setParameter("book", bookUuid);
        }
        return query.getResultList();
    }

    @Override
    public int refreshBook(User owner, String bookUuid, String bookName, String folderPath) {
        // only the rows that differ are written: a book that is saved again and again has the same name
        String name = bookName == null ? "" : bookName;
        String path = folderPath == null ? "" : ", s.folderPath = :path";
        String differs = "COALESCE(s.bookName, '') <> :name" + (folderPath == null ? "" : " OR COALESCE(s.folderPath, '') <> :path");
        Query query = entityManager
                .createQuery("UPDATE ManuscriptSize s SET s.bookName = :name" + path
                        + " WHERE s.bookUuid = :book AND s.owner.id = :owner AND (" + differs + ")")
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
                .createQuery("DELETE FROM ManuscriptSize s WHERE s.bookUuid = :book AND s.owner.id = :owner")
                .setParameter("book", bookUuid)
                .setParameter("owner", owner.getId())
                .executeUpdate();
    }

    @Override
    public int deleteAll(User owner) {
        return entityManager
                .createQuery("DELETE FROM ManuscriptSize s WHERE s.owner.id = :owner")
                .setParameter("owner", owner.getId())
                .executeUpdate();
    }
}
