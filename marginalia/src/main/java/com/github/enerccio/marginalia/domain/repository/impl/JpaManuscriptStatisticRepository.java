package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.components.*;
import com.github.enerccio.marginalia.domain.components.BookStatistic.BookState;
import com.github.enerccio.marginalia.domain.model.impl.ManuscriptStatistic;
import com.github.enerccio.marginalia.domain.repository.ManuscriptStatisticRepository;
import com.github.enerccio.marginalia.domain.security.model.User;
import jakarta.persistence.Query;

import java.time.LocalDate;
import java.util.*;

public class JpaManuscriptStatisticRepository extends JpaOwnedRepository<ManuscriptStatistic> implements ManuscriptStatisticRepository {

    private static final String SUMS = "COALESCE(SUM(s.prefillTokens), 0L), COALESCE(SUM(s.generatedTokens), 0L), "
            + "COALESCE(SUM(s.requests), 0L), COALESCE(SUM(s.wordsWritten), 0L), COALESCE(SUM(s.wordsGenerated), 0L), "
            + "COALESCE(SUM(s.wordsRemoved), 0L), COALESCE(SUM(s.partsCreated), 0L), COALESCE(SUM(s.partsDeleted), 0L), "
            + "COALESCE(SUM(s.edits), 0L)";
    private static final int SUM_COLUMNS = 9;

    @Override
    protected Class<ManuscriptStatistic> getEntityClass() {
        return ManuscriptStatistic.class;
    }

    @Override
    public int add(User owner, String bookUuid, LocalDate day, boolean agent, StatisticCounts delta, String bookName,
                   String folderPath, Date now) {
        return entityManager
                .createQuery("UPDATE ManuscriptStatistic s SET s.prefillTokens = s.prefillTokens + :prefill, "
                        + "s.generatedTokens = s.generatedTokens + :generated, s.requests = s.requests + :requests, "
                        + "s.wordsWritten = s.wordsWritten + :written, s.wordsGenerated = s.wordsGenerated + :wordsGenerated, "
                        + "s.wordsRemoved = s.wordsRemoved + :removed, s.partsCreated = s.partsCreated + :created, "
                        + "s.partsDeleted = s.partsDeleted + :deleted, s.edits = s.edits + :edits, "
                        + "s.bookName = :name, s.folderPath = :path, s.lastChange = :now, s.modification = :now "
                        + "WHERE s.bookUuid = :book AND s.day = :day AND s.agent = :agent AND s.owner.id = :owner AND s.deleted = false")
                .setParameter("prefill", delta.prefillTokens())
                .setParameter("generated", delta.generatedTokens())
                .setParameter("requests", delta.requests())
                .setParameter("written", delta.wordsWritten())
                .setParameter("wordsGenerated", delta.wordsGenerated())
                .setParameter("removed", delta.wordsRemoved())
                .setParameter("created", delta.partsCreated())
                .setParameter("deleted", delta.partsDeleted())
                .setParameter("edits", delta.edits())
                .setParameter("name", bookName)
                .setParameter("path", folderPath)
                .setParameter("now", now)
                .setParameter("book", bookUuid)
                .setParameter("day", day)
                .setParameter("agent", agent)
                .setParameter("owner", owner.getId())
                .executeUpdate();
    }

    @Override
    public ManuscriptStatistic find(User owner, String bookUuid, LocalDate day, boolean agent) {
        List<ManuscriptStatistic> found = entityManager
                .createQuery("SELECT s FROM ManuscriptStatistic s WHERE s.bookUuid = :book AND s.day = :day AND s.agent = :agent "
                        + "AND s.owner.id = :owner AND s.deleted = false", ManuscriptStatistic.class)
                .setParameter("book", bookUuid)
                .setParameter("day", day)
                .setParameter("agent", agent)
                .setParameter("owner", owner.getId())
                .setMaxResults(1)
                .getResultList();
        return found.isEmpty() ? null : found.getFirst();
    }

    @Override
    public List<ManuscriptStatistic> findRange(User owner, StatisticFilter filter) {
        return StatisticQueries.bind(entityManager.createQuery("SELECT s FROM ManuscriptStatistic s WHERE " + StatisticQueries.where("s", filter)
                        + " ORDER BY s.day, s.agent, s.id", ManuscriptStatistic.class), owner, filter)
                .getResultList();
    }

    @Override
    public StatisticCounts sum(User owner, StatisticFilter filter) {
        Object[] row = (Object[]) StatisticQueries.bind(entityManager.createQuery("SELECT " + SUMS + " FROM ManuscriptStatistic s WHERE "
                + StatisticQueries.where("s", filter)), owner, filter).getSingleResult();
        return counts(row, 0);
    }

    @Override
    public List<DayStatistic> sumByDay(User owner, StatisticFilter filter) {
        List<?> rows = StatisticQueries.bind(entityManager.createQuery("SELECT s.day, " + SUMS + " FROM ManuscriptStatistic s WHERE "
                + StatisticQueries.where("s", filter) + " GROUP BY s.day ORDER BY s.day"), owner, filter).getResultList();
        List<DayStatistic> days = new ArrayList<>(rows.size());
        for (Object o : rows) {
            Object[] row = (Object[]) o;
            days.add(new DayStatistic((LocalDate) row[0], counts(row, 1)));
        }
        return days;
    }

    @Override
    public List<BookStatistic> sumByBook(User owner, StatisticFilter filter) {
        StatisticFilter all = filter.forBook(null);
        List<?> rows = StatisticQueries.bind(entityManager.createQuery("SELECT s.bookUuid, MAX(s.bookName), MAX(s.folderPath), MAX(s.lastChange), "
                + SUMS + " FROM ManuscriptStatistic s WHERE " + StatisticQueries.where("s", all)
                + " GROUP BY s.bookUuid ORDER BY MAX(s.bookName), s.bookUuid"), owner, all).getResultList();
        List<String> uuids = new ArrayList<>(rows.size());
        for (Object o : rows) {
            uuids.add((String) ((Object[]) o)[0]);
        }
        Map<String, BookState> states = states(owner, uuids);
        List<BookStatistic> books = new ArrayList<>(rows.size());
        for (Object o : rows) {
            Object[] row = (Object[]) o;
            String uuid = (String) row[0];
            books.add(new BookStatistic(uuid, (String) row[1], row[2] == null ? "" : (String) row[2],
                    states.getOrDefault(uuid, BookState.PURGED), counts(row, 4), (Date) row[3]));
        }
        return books;
    }

    /**
     * Whether the books exist, are in the trash, or are gone: a book that is not found was purged.
     */
    private Map<String, BookState> states(User owner, List<String> uuids) {
        Map<String, BookState> states = new HashMap<>();
        for (List<String> chunk : chunks(uuids)) {
            List<?> rows = entityManager
                    .createQuery("SELECT m.uuid, m.deleted FROM Manuscript m WHERE m.owner.id = :owner AND m.uuid IN :uuids")
                    .setParameter("owner", owner.getId())
                    .setParameter("uuids", chunk)
                    .getResultList();
            for (Object o : rows) {
                Object[] row = (Object[]) o;
                states.put((String) row[0], Boolean.TRUE.equals(row[1]) ? BookState.TRASHED : BookState.PRESENT);
            }
        }
        return states;
    }

    @Override
    public List<FolderStatistic> sumByFolder(User owner, StatisticFilter filter) {
        List<?> rows = StatisticQueries.bind(entityManager.createQuery("SELECT COALESCE(s.folderPath, ''), " + SUMS
                + " FROM ManuscriptStatistic s WHERE " + StatisticQueries.where("s", filter)
                + " GROUP BY COALESCE(s.folderPath, '') ORDER BY COALESCE(s.folderPath, '')"), owner, filter).getResultList();
        List<FolderStatistic> folders = new ArrayList<>(rows.size());
        for (Object o : rows) {
            Object[] row = (Object[]) o;
            folders.add(new FolderStatistic((String) row[0], counts(row, 1)));
        }
        return folders;
    }

    @Override
    public int refreshBook(User owner, String bookUuid, String bookName, String folderPath) {
        String path = folderPath == null ? "" : ", s.folderPath = :path";
        Query query = entityManager
                .createQuery("UPDATE ManuscriptStatistic s SET s.bookName = :name" + path
                        + " WHERE s.bookUuid = :book AND s.owner.id = :owner")
                .setParameter("name", bookName)
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
                .createQuery("DELETE FROM ManuscriptStatistic s WHERE s.bookUuid = :book AND s.owner.id = :owner")
                .setParameter("book", bookUuid)
                .setParameter("owner", owner.getId())
                .executeUpdate();
    }

    @Override
    public int deleteAll(User owner) {
        return entityManager
                .createQuery("DELETE FROM ManuscriptStatistic s WHERE s.owner.id = :owner")
                .setParameter("owner", owner.getId())
                .executeUpdate();
    }

    private static StatisticCounts counts(Object[] row, int offset) {
        long[] v = new long[SUM_COLUMNS];
        for (int i = 0; i < SUM_COLUMNS; i++) {
            v[i] = ((Number) row[offset + i]).longValue();
        }
        return new StatisticCounts(v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8]);
    }
}
