package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.collections.ActivityKind;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The registrations are notes in memory under one lock; the tracker's own thread is the only one that writes the rows
 * ({@link ActivityIntervalService}), a moment after a change and on every tick. The tick moves the end of every open
 * stretch to now, cuts a stretch at midnight when it is written, drops a registration that never closed and asks which agents
 * run.
 * <p>
 * A stretch is the registrations of one key (kind, book, side) that are open together. It has a current segment, the row that
 * grows; a segment ends when the stretch is bridged over a gap longer than two ticks (a suspended computer), and a new one
 * starts. The stretch is forgotten when nobody holds it any more and the linger of its kind has passed.
 */
public class ActivityTrackerImpl implements ActivityTracker {

    private static final Logger log = LoggerFactory.getLogger(ActivityTrackerImpl.class);

    public static final long DEFAULT_TICK_MILLIS = 60_000;
    /**
     * A generation keeps its stretch open this long after the last registration closed, the next round of an agent follows at
     * once: a few long rows instead of thousands of short ones. The row ends at the last close.
     */
    public static final long GENERATE_LINGER_MILLIS = 2_000;
    /**
     * A generation that has not closed after this long is a leak (the request timeout of the AI is never longer by default).
     */
    public static final long GENERATE_CAP_MILLIS = 60 * 60 * 1000L;

    private record Key(ActivityKind kind, String bookUuid, boolean agent) {
    }

    private record Resolved(Long id, String uuid, boolean agent) {
    }

    /** One row: the start and the end are moved by the lock, the id of the row and the start (a cut) by the writer. */
    private static final class Segment {
        long start;
        long end;
        Long rowId;

        Segment(long start) {
            this.start = start;
            this.end = start;
        }
    }

    private static final class Stretch {
        final Key key;
        final Long bookId;
        final Set<Object> holders = new LinkedHashSet<>();
        Segment current;
        final List<Segment> done = new ArrayList<>();
        long lastClose;

        Stretch(Key key, Long bookId, long now) {
            this.key = key;
            this.bookId = bookId;
            this.current = new Segment(now);
        }
    }

    private static final BookPresence NO_PRESENCE = new BookPresence() {
        @Override
        public void visible(boolean visible) {
        }

        @Override
        public void editing(boolean typing) {
        }

        @Override
        public void close() {
        }
    };
    private static final Object RUN_HOLDER = new Object();

    @Autowired
    @Lazy
    private ActivityIntervalService intervalService;

    @Autowired
    @Lazy
    private AgentRunService agentRunService;

    @Autowired
    @Lazy
    private ManuscriptService manuscriptService;

    @Autowired
    @Lazy
    private ManuscriptSizeService sizeService;

    private final Object lock = new Object();
    private final Map<Key, Stretch> stretches = new LinkedHashMap<>();
    private final Set<Presence> presences = new LinkedHashSet<>();
    // the uuid of a book never changes
    private final Map<Long, String> uuids = new ConcurrentHashMap<>();
    private final Object writeLock = new Object();
    private final AtomicBoolean wakePending = new AtomicBoolean();

    private volatile Clock clock = Clock.systemDefaultZone();
    private volatile long tickMillis = DEFAULT_TICK_MILLIS;
    private volatile long generateLingerMillis = GENERATE_LINGER_MILLIS;
    private volatile boolean manual;
    private ScheduledFuture<?> ticking;

    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "activity-tracker");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * Starts the tick (the init method of the bean).
     */
    public void start() {
        synchronized (lock) {
            schedule();
        }
    }

    /**
     * Writes what is open up to now and stops the thread (the destroy method of the bean).
     */
    public void stop() {
        try {
            synchronized (lock) {
                long now = now();
                stretches.values().forEach(s -> {
                    if (!s.holders.isEmpty()) {
                        s.current.end = now;
                    }
                });
            }
            flush();
        } catch (RuntimeException e) {
            log.warn("Failed to write the activity at shutdown", e);
        }
        worker.shutdownNow();
    }

    private void schedule() {
        if (ticking != null) {
            ticking.cancel(false);
            ticking = null;
        }
        if (!manual) {
            ticking = worker.scheduleWithFixedDelay(this::tickQuietly, tickMillis, tickMillis, TimeUnit.MILLISECONDS);
        }
    }

    // ---- registrations ----

    @Override
    public BookPresence open(Manuscript book) {
        try {
            Resolved resolved = resolve(book);
            if (resolved == null || resolved.agent()) {
                // the copy of an agent is not a window of the author
                return NO_PRESENCE;
            }
            Presence presence = new Presence(resolved);
            // a book that is opened has a size for today, and one that drifted is corrected
            sizeService.touch(resolved.id());
            synchronized (lock) {
                presences.add(presence);
                presence.sync(now());
            }
            wake();
            return presence;
        } catch (RuntimeException e) {
            log.warn("Failed to register the open book", e);
            return NO_PRESENCE;
        }
    }

    @Override
    public Registration generate(Long bookId) {
        if (bookId == null) {
            return NONE;
        }
        try {
            Resolved resolved = resolve(manuscriptService.find(bookId));
            if (resolved == null) {
                return NONE;
            }
            Held held = new Held(new Key(ActivityKind.GENERATE, resolved.uuid(), resolved.agent()), resolved.id(), now());
            synchronized (lock) {
                enter(held.key, held.bookId, held, held.opened);
            }
            wake();
            return held;
        } catch (Exception e) {
            log.warn("Failed to register the generation for book {}", bookId, e);
            return NONE;
        }
    }

    /**
     * The book the time is counted on: the book, or the original of the hidden working copy of an agent run on the agents'
     * side; null for a book that is not saved or a copy that belongs to no run.
     */
    private Resolved resolve(Manuscript book) {
        if (book == null || book.getId() == null || book.getUuid() == null) {
            return null;
        }
        uuids.put(book.getId(), book.getUuid());
        if (!book.isAgentic()) {
            return new Resolved(book.getId(), book.getUuid(), false);
        }
        try {
            Manuscript original = agentRunService.findOriginalOfCopy(book);
            if (original == null || original.getId() == null || original.getUuid() == null) {
                return null;
            }
            uuids.put(original.getId(), original.getUuid());
            return new Resolved(original.getId(), original.getUuid(), true);
        } catch (Exception e) {
            log.warn("Failed to find the book of the working copy {}", book.getUuid(), e);
            return null;
        }
    }

    private final class Held implements Registration {
        final Key key;
        final Long bookId;
        final long opened;
        private boolean closed;

        Held(Key key, Long bookId, long opened) {
            this.key = key;
            this.bookId = bookId;
            this.opened = opened;
        }

        @Override
        public void close() {
            boolean linger;
            synchronized (lock) {
                if (closed) {
                    return;
                }
                closed = true;
                leave(key, this, now());
                linger = key.kind() == ActivityKind.GENERATE;
            }
            wake();
            if (linger) {
                wakeLater(generateLingerMillis + 50);
            }
        }
    }

    private final class Presence implements BookPresence {
        final Resolved book;
        private boolean visible = true;
        private boolean typing;
        private long typingAt;
        private boolean closed;
        private final Object openHolder = new Object();
        private final Object editHolder = new Object();

        Presence(Resolved book) {
            this.book = book;
        }

        @Override
        public void visible(boolean visible) {
            synchronized (lock) {
                this.visible = visible;
                sync(now());
            }
            wake();
        }

        @Override
        public void editing(boolean typing) {
            synchronized (lock) {
                this.typing = typing;
                this.typingAt = now();
                sync(now());
            }
            wake();
        }

        @Override
        public void close() {
            synchronized (lock) {
                if (closed) {
                    return;
                }
                closed = true;
                presences.remove(this);
                sync(now());
            }
            // a book that is closed has its size measured now, not in a moment
            sizeService.touchNow(book.id());
            wake();
        }

        /** Called with the lock. */
        void sync(long now) {
            boolean open = !closed && visible;
            boolean edit = open && typing;
            Key openKey = new Key(ActivityKind.OPEN, book.uuid(), false);
            Key editKey = new Key(ActivityKind.EDIT, book.uuid(), false);
            if (open) {
                enter(openKey, book.id(), openHolder, now);
            } else {
                leave(openKey, openHolder, now);
            }
            if (edit) {
                enter(editKey, book.id(), editHolder, now);
            } else {
                leave(editKey, editHolder, now);
            }
        }

        /** Called with the lock by the tick: a lost message does not keep the author typing. */
        void expire(long now) {
            if (typing && now - typingAt > IDLE_CUTOFF_MILLIS + tickMillis) {
                // it would have said so at the cut-off
                typing = false;
                sync(typingAt + IDLE_CUTOFF_MILLIS);
            }
        }
    }

    // ---- the stretches, always with the lock ----

    private void enter(Key key, Long bookId, Object holder, long now) {
        Stretch stretch = stretches.get(key);
        if (stretch == null) {
            stretch = new Stretch(key, bookId, now);
            stretches.put(key, stretch);
        } else if (stretch.holders.isEmpty() && now - stretch.lastClose > lingerOf(key)) {
            // nobody held it for longer than the linger and the writer has not got to forget it yet: that is a stretch of its own
            stretch.done.add(stretch.current);
            stretch.current = new Segment(now);
        }
        stretch.holders.add(holder);
    }

    /**
     * @param endAt when the holder stopped: now, or earlier when the tick finds out late (the end then moves back from where
     *              the tick had put it)
     */
    private void leave(Key key, Object holder, long endAt) {
        Stretch stretch = stretches.get(key);
        if (stretch == null || !stretch.holders.remove(holder)) {
            return;
        }
        if (stretch.holders.isEmpty()) {
            stretch.current.end = Math.max(stretch.current.start, endAt);
            stretch.lastClose = stretch.current.end;
        }
    }

    private long lingerOf(Key key) {
        return key.kind() == ActivityKind.GENERATE ? generateLingerMillis : 0;
    }

    // ---- the tick ----

    private void tickQuietly() {
        try {
            tick();
        } catch (RuntimeException e) {
            log.warn("The activity tick failed", e);
        }
    }

    /**
     * One tick: the open stretches are moved to now and everything is written. The worker does it every
     * {@code tickMillis}; a test that drives the clock calls it.
     */
    public void tick() {
        Map<String, Long> running = running();
        synchronized (lock) {
            long now = now();
            for (Presence presence : new ArrayList<>(presences)) {
                presence.expire(now);
            }
            if (running != null) {
                for (Map.Entry<String, Long> run : running.entrySet()) {
                    enter(new Key(ActivityKind.RUN, run.getKey(), true), run.getValue(), RUN_HOLDER, now);
                }
                for (Stretch stretch : new ArrayList<>(stretches.values())) {
                    if (stretch.key.kind() == ActivityKind.RUN && !running.containsKey(stretch.key.bookUuid())) {
                        leave(stretch.key, RUN_HOLDER, now);
                    }
                }
            }
            for (Stretch stretch : stretches.values()) {
                dropLeaks(stretch, now);
                if (stretch.holders.isEmpty()) {
                    continue;
                }
                if (now - stretch.current.end > 2 * tickMillis) {
                    // nobody moved it for two ticks: the computer slept, the time between is not ours to count
                    stretch.done.add(stretch.current);
                    stretch.current = new Segment(now);
                } else {
                    stretch.current.end = now;
                }
            }
        }
        flush();
    }

    /** The books the agents work on now, by uuid, null when it could not be asked (the open stretches stay as they are). */
    private Map<String, Long> running() {
        try {
            Map<String, Long> books = new LinkedHashMap<>();
            for (Long id : agentRunService.findRunningBookIds()) {
                String uuid = uuids.get(id);
                if (uuid == null) {
                    Manuscript book = manuscriptService.find(id);
                    uuid = book == null ? null : book.getUuid();
                    if (uuid != null) {
                        uuids.put(id, uuid);
                    }
                }
                if (uuid != null) {
                    books.put(uuid, id);
                }
            }
            return books;
        } catch (Exception e) {
            log.warn("Failed to ask which agents run", e);
            return null;
        }
    }

    private void dropLeaks(Stretch stretch, long now) {
        for (Iterator<Object> it = stretch.holders.iterator(); it.hasNext(); ) {
            Object holder = it.next();
            if (holder instanceof Held held && now - held.opened > GENERATE_CAP_MILLIS) {
                log.warn("A generation of book {} did not close in {} ms, it is dropped", stretch.key.bookUuid(), now - held.opened);
                held.closed = true;
                it.remove();
                if (stretch.holders.isEmpty()) {
                    // the end stays where the last tick put it: after that nobody can say it went on
                    stretch.lastClose = stretch.current.end;
                }
            }
        }
    }

    // ---- writing ----

    private void wake() {
        if (manual || !wakePending.compareAndSet(false, true)) {
            return;
        }
        try {
            worker.execute(() -> {
                wakePending.set(false);
                flushQuietly();
            });
        } catch (RejectedExecutionException e) {
            // the server is going down
            wakePending.set(false);
        }
    }

    private void wakeLater(long delayMillis) {
        if (manual) {
            return;
        }
        try {
            worker.schedule(this::flushQuietly, delayMillis, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
            // the server is going down
        }
    }

    private void flushQuietly() {
        try {
            flush();
        } catch (RuntimeException e) {
            log.warn("Failed to write the activity", e);
        }
    }

    private record Snapshot(Stretch stretch, Segment segment, long start, long end, Long rowId, boolean last, boolean finishing) {
    }

    @Override
    public void flush() {
        synchronized (writeLock) {
            List<Snapshot> jobs = new ArrayList<>();
            synchronized (lock) {
                long now = now();
                for (Stretch stretch : stretches.values()) {
                    boolean finishing = stretch.holders.isEmpty() && now - stretch.lastClose >= lingerOf(stretch.key);
                    for (Segment segment : stretch.done) {
                        jobs.add(new Snapshot(stretch, segment, segment.start, segment.end, segment.rowId, false, true));
                    }
                    Segment current = stretch.current;
                    jobs.add(new Snapshot(stretch, current, current.start, current.end, current.rowId, true, finishing));
                }
            }
            for (Snapshot job : jobs) {
                try {
                    write(job);
                } catch (Exception e) {
                    log.warn("Failed to write the activity of book {}", job.stretch().key.bookUuid(), e);
                }
            }
        }
    }

    private void write(Snapshot job) throws Exception {
        Key key = job.stretch().key;
        long start = job.start();
        long end = job.end();
        Long rowId = job.rowId();
        ZoneId zone = clock.getZone();
        Manuscript[] book = new Manuscript[1];
        boolean gone = false;

        if (rowId == null && end == start && job.finishing()) {
            // opened and closed in the same instant: nothing to record
            finished(job, start, null);
            return;
        }
        while (true) {
            LocalDate day = Instant.ofEpochMilli(start).atZone(zone).toLocalDate();
            long midnight = day.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli();
            if (end <= midnight) {
                break;
            }
            // a stretch is inside one day: this one ends at midnight and the next day's starts at the same instant
            if (rowId == null) {
                rowId = insert(job, book, start);
                if (rowId == null) {
                    gone = true;
                    break;
                }
            }
            intervalService.extend(rowId, new Date(midnight));
            start = midnight;
            rowId = null;
        }
        if (!gone) {
            if (rowId == null) {
                rowId = insert(job, book, start);
            }
            if (rowId != null) {
                intervalService.extend(rowId, new Date(end));
            }
        }
        if (rowId == null && !job.finishing()) {
            // the book is gone: nothing more is recorded for it
            synchronized (lock) {
                stretches.remove(key, job.stretch());
            }
            return;
        }
        finished(job, start, rowId);
    }

    private Long insert(Snapshot job, Manuscript[] book, long start) throws Exception {
        if (book[0] == null) {
            book[0] = manuscriptService.find(job.stretch().bookId);
        }
        if (book[0] == null || book[0].getOwner() == null) {
            return null;
        }
        Key key = job.stretch().key;
        return intervalService.start(book[0], key.kind(), key.agent(), new Date(start)).getId();
    }

    /** Takes the result of the write back and forgets the stretch when it is over. */
    private void finished(Snapshot job, long start, Long rowId) {
        synchronized (lock) {
            Stretch stretch = job.stretch();
            if (!job.last()) {
                stretch.done.remove(job.segment());
                return;
            }
            job.segment().start = start;
            job.segment().rowId = rowId;
            boolean over = stretch.holders.isEmpty() && now() - stretch.lastClose >= lingerOf(stretch.key)
                    && job.segment().end == job.end() && stretch.done.isEmpty();
            if (over) {
                stretches.remove(stretch.key, stretch);
            }
        }
    }

    private long now() {
        return clock.millis();
    }

    // ---- for the tests ----

    /**
     * Gives the time to the tracker; a test drives it.
     */
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public void setTickMillis(long tickMillis) {
        synchronized (lock) {
            this.tickMillis = tickMillis;
            schedule();
        }
    }

    public void setGenerateLingerMillis(long millis) {
        this.generateLingerMillis = millis;
    }

    /**
     * Stops the worker from writing and ticking by itself: whoever sets it calls {@link #tick} and {@link #flush}.
     */
    public void setManual(boolean manual) {
        synchronized (lock) {
            this.manual = manual;
            schedule();
        }
    }

    /**
     * Forgets everything that is open and not written (a test starts clean).
     */
    public void forget() {
        synchronized (writeLock) {
            synchronized (lock) {
                stretches.clear();
                presences.clear();
            }
        }
    }
}
