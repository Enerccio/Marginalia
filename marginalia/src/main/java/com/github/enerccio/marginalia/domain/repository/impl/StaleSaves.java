package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Finds a <em>stale save</em>: an entity saved from a copy that was read before the last save of its row. A save writes the whole row
 * (and the whole {@code extendedContent} JSON, with the data of every plugin), so such a save puts back what somebody else saved since
 * the copy was read. The way to change an entity held from an earlier time is the {@code update(entity, change)} of its service
 * ({@code ManuscriptService.update}, {@code ChatMessageService.update}): it reads the row fresh and saves it in one write transaction.
 * <p>
 * The check compares the {@code modification} of the copy with the one the last committed save of the row wrote, remembered here in
 * memory for the rows saved since the start (the most recent ones), by the persistence unit that owns the row (the ids of two databases
 * are the same numbers, a row of one is not the row of the other). Not with the {@code modification} in the database: the bulk updates
 * that only touch it (a saved part touches its book, a book its folder) change nothing a copy could put back, and a copy older than such
 * a touch is not stale. A row not saved since the start is not checked.
 * <p>
 * It only reports, it repairs nothing: in production it logs a warning with the caller ({@link Mode#LOG}, the default), in the tests it
 * fails the save ({@link Mode#FAIL}, the system property {@value #PROPERTY} set by the build), so a new stale save does not get in
 * unnoticed. {@link Mode#OFF} switches it off.
 */
public final class StaleSaves {

    private static final Logger log = LoggerFactory.getLogger(StaleSaves.class);

    /** The system property that chooses the {@link Mode} (case does not matter). */
    public static final String PROPERTY = "marginalia.staleSaves";

    /** How many rows are remembered for each persistence unit; the oldest saves are forgotten first. */
    private static final int REMEMBERED = 20_000;

    public enum Mode {OFF, LOG, FAIL}

    private static volatile Mode mode = readMode();

    /**
     * The modification the last committed save wrote, by the persistence unit of the row (held weakly: a closed context takes its
     * rows with it) and then by the class and the id of the entity.
     */
    private static final Map<Object, Map<String, Long>> saved = Collections.synchronizedMap(new WeakHashMap<>());

    private StaleSaves() {
    }

    private static Mode readMode() {
        String value = System.getProperty(PROPERTY);
        if (value == null || value.isBlank()) {
            return Mode.LOG;
        }
        try {
            return Mode.valueOf(value.strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Mode.LOG;
        }
    }

    public static Mode getMode() {
        return mode;
    }

    /** For a test that saves a stale copy on purpose; null is the mode of the system property again. */
    public static void setMode(Mode mode) {
        StaleSaves.mode = mode == null ? readMode() : mode;
    }

    /** Thrown in {@link Mode#FAIL} instead of the save. */
    public static class StaleSaveException extends IllegalStateException {
        public StaleSaveException(String message) {
            super(message);
        }
    }

    /** The rows remembered for a persistence unit. */
    private static Map<String, Long> rows(Object unit) {
        return saved.computeIfAbsent(unit, u -> new LinkedHashMap<>(1024, 0.75f, false) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
                return size() > REMEMBERED;
            }
        });
    }

    private static String key(BaseEntity entity) {
        return type(entity).getName() + "#" + entity.getId();
    }

    /** The class of the entity, without initializing a proxy (it may be detached). */
    private static Class<?> type(BaseEntity entity) {
        return entity instanceof HibernateProxy proxy ? proxy.getHibernateLazyInitializer().getPersistentClass() : entity.getClass();
    }

    /**
     * Before the save of a detached copy of a stored row: reports it when the copy is older than the last save of the row.
     *
     * @param unit the persistence unit that owns the row (its {@code EntityManagerFactory})
     */
    static void check(Object unit, BaseEntity entity) {
        if (mode == Mode.OFF || entity.getId() == null || entity.getModification() == null) {
            return;
        }
        Long last;
        Map<String, Long> rows = rows(unit);
        synchronized (rows) {
            last = rows.get(key(entity));
        }
        long copy = entity.getModification().getTime();
        if (last == null || copy >= last) {
            return;
        }
        String message = "Stale save of " + type(entity).getSimpleName() + " " + entity.getId() + ": the copy is from " + copy
                + ", the row was saved at " + last + " (" + (last - copy) + " ms later), saved by " + caller()
                + ". Change it through update(entity, change) of its service, or read it again first.";
        if (mode == Mode.FAIL) {
            throw new StaleSaveException(message);
        }
        log.warn(message);
    }

    /** After the row was written: its modification is remembered when the transaction commits. */
    static void written(Object unit, BaseEntity entity) {
        if (mode == Mode.OFF || entity.getId() == null || entity.getModification() == null) {
            return;
        }
        String key = key(entity);
        long modification = entity.getModification().getTime();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    remember(unit, key, modification);
                }
            });
        } else {
            remember(unit, key, modification);
        }
    }

    /** After a hard delete. */
    static void removed(Object unit, BaseEntity entity) {
        if (entity.getId() == null) {
            return;
        }
        Map<String, Long> rows = rows(unit);
        synchronized (rows) {
            rows.remove(key(entity));
        }
    }

    private static void remember(Object unit, String key, long modification) {
        Map<String, Long> rows = rows(unit);
        synchronized (rows) {
            rows.merge(key, modification, Math::max);
        }
    }

    /** Forgets every save (for a test that starts over). */
    public static void reset() {
        saved.clear();
    }

    /** For a test: the last save of the row in any persistence unit. */
    static Date lastSave(BaseEntity entity) {
        String key = key(entity);
        Long last = null;
        synchronized (saved) {
            for (Map<String, Long> rows : saved.values()) {
                synchronized (rows) {
                    Long value = rows.get(key);
                    if (value != null && (last == null || value > last)) {
                        last = value;
                    }
                }
            }
        }
        return last == null ? null : new Date(last);
    }

    /** The first frame of the application that is not the base of the services and repositories, nor a proxy. */
    private static String caller() {
        return StackWalker.getInstance().walk(frames -> frames
                .filter(f -> f.getClassName().startsWith("com.github.enerccio.marginalia."))
                .filter(f -> !f.getClassName().contains("$$"))
                .filter(f -> !f.getClassName().equals(StaleSaves.class.getName()))
                .filter(f -> !f.getClassName().startsWith("com.github.enerccio.marginalia.domain.repository."))
                .filter(f -> !f.getClassName().endsWith("ServiceImpl")
                        || !(f.getMethodName().startsWith("save") || f.getMethodName().equals("delete")))
                .findFirst()
                .map(f -> f.getClassName() + "." + f.getMethodName() + ":" + f.getLineNumber())
                .orElse("?"));
    }
}
