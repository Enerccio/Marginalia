package com.github.enerccio.marginalia.bound.migration;

import com.github.enerccio.marginalia.bound.Migration;
import com.github.enerccio.marginalia.bound.migration.StatisticsReconstruction.*;
import com.github.enerccio.marginalia.domain.collections.RevisionSource;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.ManuscriptSize;
import com.github.enerccio.marginalia.domain.model.impl.ManuscriptStatistic;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * App version 8 → 9: the writing statistics (V23) of the books that existed before they were kept. The counts (tokens, words, parts,
 * edits, deletes) and the size of every book are rebuilt from the parts and the versions of their text, see
 * {@link StatisticsReconstruction} for the rules, and marked {@code reconstructed}: an estimate. A book that has rows already is left
 * alone, so the migration can be repeated after a failure, and so are the days from today on (they are counted live). Books in the
 * trash are rebuilt too (they come back with their statistics), the hidden working copies of agents are not books of the author.
 * <p>
 * The time cannot be rebuilt: nobody recorded it. Each user gets {@code UserSetting#statisticsSince}, the day the time is recorded
 * from (today, or the day of their first stretch if there is one already), and the windows show no time for the days before it.
 * The names of the books and of their folders are the current ones.
 */
public class StatisticsBackfillMigration implements Migration {
    private static final Logger log = LoggerFactory.getLogger(StatisticsBackfillMigration.class);

    private static final int WRITE_ATTEMPTS = 3;

    private Clock clock = Clock.systemDefaultZone();

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private SettingService settingService;

    @Override
    public int migrate(int cversion, MigrationType migrationType) throws Exception {
        if (migrationType != MigrationType.APP || cversion != 8) {
            return cversion;
        }
        log.info("Rebuilt the statistics of {} books", rebuild());
        log.info("Set the day the time is recorded from for {} users", setSince());
        return 9;
    }

    /**
     * For tests: the day that is today.
     */
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    private record Book(long id, String uuid, String name, Long ownerId, Long folderId, boolean deleted, Long activeLeaf) {
    }

    private int rebuild() throws Exception {
        LocalDate today = LocalDate.now(clock);
        ZoneId zone = clock.getZone();
        List<Book> books = new ArrayList<>();
        Map<Long, String> folderNames = new HashMap<>();
        Map<Long, Long> folderParents = new HashMap<>();
        Set<String> done = new HashSet<>();
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            for (Object[] row : em.createQuery("select m.id, m.uuid, m.name, o.id, f.id, m.deleted, l.id from Manuscript m "
                    + "join m.owner o left join m.folder f left join m.activeLeaf l where m.agentic = false", Object[].class).getResultList()) {
                books.add(new Book((Long) row[0], (String) row[1], (String) row[2], (Long) row[3], (Long) row[4], (Boolean) row[5], (Long) row[6]));
            }
            for (Object[] row : em.createQuery("select f.id, f.name, p.id from Folder f left join f.parent p", Object[].class).getResultList()) {
                folderNames.put((Long) row[0], (String) row[1]);
                folderParents.put((Long) row[0], (Long) row[2]);
            }
            done.addAll(em.createQuery("select s.bookUuid from ManuscriptStatistic s", String.class).getResultList());
            done.addAll(em.createQuery("select s.bookUuid from ManuscriptSize s", String.class).getResultList());
        }
        int rebuilt = 0;
        for (Book book : books) {
            if (done.contains(book.uuid())) {
                continue;
            }
            Result result = read(book, today, zone);
            if (!result.isEmpty()) {
                write(book, pathOf(book.folderId(), folderNames, folderParents), result);
                rebuilt++;
            }
        }
        return rebuilt;
    }

    private Result read(Book book, LocalDate today, ZoneId zone) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            List<PartRow> parts = new ArrayList<>();
            // the prompt tokens and the model are extended attributes: the parts are loaded, not selected
            for (ChatMessage message : em.createQuery("select m from ChatMessage m where m.parentScript.id = :book", ChatMessage.class)
                    .setParameter("book", book.id()).getResultList()) {
                parts.add(new PartRow(message.getId(), message.getUuid(), message.getParent() == null ? null : message.getParent().getId(),
                        message.getCreation(), message.getModification() == null ? message.getCreation() : message.getModification(),
                        message.isDeleted(), message.getWordCount(), message.getTokenCount(), message.getPromptTokens(), message.getModelUsed()));
            }
            List<VersionRow> versions = new ArrayList<>();
            for (Object[] row : em.createQuery("select r.message.id, r.creation, r.source, r.sourceDetail, r.agent, r.wordCount, r.tokenCount "
                            + "from MessageRevision r where r.message.parentScript.id = :book order by r.message.id, r.creation, r.id", Object[].class)
                    .setParameter("book", book.id()).getResultList()) {
                versions.add(new VersionRow((Long) row[0], (Date) row[1], (RevisionSource) row[2], (String) row[3], (Boolean) row[4],
                        (Integer) row[5], (Long) row[6]));
            }
            return StatisticsReconstruction.rebuild(new BookRow(book.uuid(), book.deleted(), book.activeLeaf()), parts, versions, today, zone);
        }
    }

    private static String pathOf(Long folderId, Map<Long, String> names, Map<Long, Long> parents) {
        LinkedList<String> path = new LinkedList<>();
        Set<Long> seen = new HashSet<>();
        for (Long at = folderId; at != null && seen.add(at) && names.containsKey(at); at = parents.get(at)) {
            path.addFirst(names.get(at));
        }
        return String.join(String.valueOf(FolderService.SEPARATOR), path);
    }

    /**
     * One transaction for the rows of one book, so a failure leaves the books that are done as they are.
     */
    private void write(Book book, String path, Result result) throws Exception {
        for (int attempt = 1; ; attempt++) {
            try {
                writeOnce(book, path, result);
                return;
            } catch (PessimisticLockException | LockTimeoutException e) {
                if (attempt == WRITE_ATTEMPTS) {
                    throw e;
                }
                log.warn("The database is locked, writing the statistics of the book {} again ({}/{})", book.uuid(), attempt, WRITE_ATTEMPTS);
                Thread.sleep(200L * attempt);
            }
        }
    }

    private void writeOnce(Book book, String path, Result result) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            em.getTransaction().begin();
            try {
                User owner = em.getReference(User.class, book.ownerId());
                for (CountRow row : result.counts()) {
                    ManuscriptStatistic statistic = new ManuscriptStatistic();
                    Date now = new Date();
                    statistic.setUuid(UUID.randomUUID().toString());
                    statistic.setCreation(now);
                    statistic.setModification(now);
                    statistic.setOwner(owner);
                    statistic.setBookUuid(book.uuid());
                    statistic.setBookName(book.name());
                    statistic.setFolderPath(path);
                    statistic.setDay(row.day());
                    statistic.setAgent(row.agent());
                    statistic.setCounts(row.counts());
                    statistic.setReconstructed(true);
                    statistic.setLastChange(row.lastChange());
                    em.persist(statistic);
                }
                for (SizeRow row : result.sizes()) {
                    ManuscriptSize size = new ManuscriptSize();
                    Date now = new Date();
                    size.setUuid(UUID.randomUUID().toString());
                    size.setCreation(now);
                    size.setModification(now);
                    size.setOwner(owner);
                    size.setBookUuid(book.uuid());
                    size.setBookName(book.name());
                    size.setFolderPath(path);
                    size.setDay(row.day());
                    size.setBookWords(row.bookWords());
                    size.setBookTokens(row.bookTokens());
                    size.setBranchWords(row.branchWords());
                    size.setBranchTokens(row.branchTokens());
                    size.setLeafUuid(row.leafUuid());
                    size.setBaseline(false);
                    size.setReconstructed(true);
                    size.setLastChange(Date.from(row.day().atTime(12, 0).atZone(clock.getZone()).toInstant()));
                    em.persist(size);
                }
                em.getTransaction().commit();
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
    }

    /**
     * The users that have none yet get the day the time is recorded from: the first stretch that exists, or today.
     */
    private int setSince() throws Exception {
        LocalDate today = LocalDate.now(clock);
        Map<User, LocalDate> users = new LinkedHashMap<>();
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            for (User user : em.createQuery("select u from User u where u.deleted = false", User.class).getResultList()) {
                Date first = em.createQuery("select min(a.startTime) from ActivityInterval a where a.owner.id = :user", Date.class)
                        .setParameter("user", user.getId()).getSingleResult();
                LocalDate since = first == null ? today : first.toInstant().atZone(clock.getZone()).toLocalDate();
                users.put(user, since.isAfter(today) ? today : since);
            }
        }
        int set = 0;
        for (Map.Entry<User, LocalDate> entry : users.entrySet()) {
            UserSetting setting = settingService.getOrCreate(UserSetting.class, entry.getKey());
            if (setting.getStatisticsSince() == null) {
                setting.setStatisticsSince(entry.getValue().toString());
                settingService.save(setting);
                set++;
            }
        }
        return set;
    }
}
