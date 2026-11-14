package com.github.enerccio.marginalia.domain.service.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Tags the work that talks to an external AI with a <b>job</b>, so that the administrator can see what the server does now and
 * what it did: who started it, when it started and ended, and every <b>call</b> it made to a model, a speech or an image backend
 * with its own start and end. Metadata only, never the text.
 * <p>
 * Every request to a model is a call (the inference services report it, see {@code JobRecorder}); a request outside of any job
 * is a job of its own with one call. A <i>unit of work</i> that makes many calls (summarizing a book, converting a chapter to
 * audio, an agent run) opens a job around them, so its calls belong together:
 * <pre>
 * try (Jobs.Handle job = Jobs.open("audiobook", "Chapter 3", JobCallType.AUDIO)) {
 *     // work on this thread; its calls are in the job
 * }
 * </pre>
 * Closing the handle ends the job. For work that goes on after the method returned (an asynchronous callback) use
 * {@link #openAsync}, which only leaves the thread when closed and ends when {@link Handle#finish} is called; a worker thread
 * of the job takes it over with {@link Handle#enter()}. A job nobody ended and that makes no call for {@link #IDLE_LIMIT} is ended
 * by the application as completed.
 * <p>
 * A backend that is not a model (an HTTP call of a plugin) reports itself with {@link #beginCall}; it only counts inside a job.
 */
public final class Jobs {

    private static final Logger log = LoggerFactory.getLogger(Jobs.class);

    /** The kind of the job of a request that was made outside of any {@code InferenceSession}. */
    public static final String UNTAGGED = "untagged";

    /** The longest error message of a call that is kept: a backend may echo a part of what it was sent. */
    static final int MAX_ERROR = 300;

    /** How many calls of one job are kept in the log (the others are only counted). */
    public static final int MAX_KEPT_CALLS = 5000;

    /** A job nobody ended and that made no call for this long is ended as completed. */
    public static final Duration IDLE_LIMIT = Duration.ofMinutes(30);

    private static final ThreadLocal<Handle> ACTIVE = new ThreadLocal<>();
    private static final ConcurrentHashMap<String, Running> RUNNING = new ConcurrentHashMap<>();
    private static final List<Consumer<FinishedJob>> SINKS = new CopyOnWriteArrayList<>();
    private static volatile Supplier<Owner> ownerResolver = () -> null;
    private static volatile ScheduledExecutorService sweeper;

    private Jobs() {
    }

    /**
     * Who a job runs for.
     */
    public record Owner(Long userId, String login) {
    }

    /**
     * Tells the jobs how to find the user of the calling thread (called on the thread that opens the job); the resolver may return
     * null and may throw (a thread without a session). Done by the service that keeps the log.
     */
    public static void setOwnerResolver(Supplier<Owner> resolver) {
        ownerResolver = resolver == null ? () -> null : resolver;
    }

    /**
     * The user the current thread works for (the session's user), null without one.
     */
    public static Owner currentOwner() {
        try {
            return ownerResolver.get();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Passes every job that ends to the sink (on the thread that ended it), until {@link #removeSink}.
     */
    public static void addSink(Consumer<FinishedJob> sink) {
        if (sink != null) {
            SINKS.add(sink);
        }
    }

    public static void removeSink(Consumer<FinishedJob> sink) {
        SINKS.remove(sink);
    }

    /**
     * Forgets the sinks, the resolver of the user and the jobs that run; for tests.
     */
    public static void reset() {
        ownerResolver = () -> null;
        SINKS.clear();
        RUNNING.clear();
    }

    /**
     * Starts a job for the work that is done on this thread now; closing the handle ends it.
     */
    public static Handle open(String kind, String label) {
        return open(kind, label, JobCallType.OTHER);
    }

    /**
     * @param callType what the calls without a type of their own (see {@link #beginCall(String)}) are
     */
    public static Handle open(String kind, String label, JobCallType callType) {
        return start(kind, label, callType, true);
    }

    /**
     * Starts a job that goes on after the code that opened it returned: closing the handle only leaves it on this thread, the job
     * ends with {@link Handle#finish}.
     */
    public static Handle openAsync(String kind, String label, JobCallType callType) {
        return start(kind, label, callType, false);
    }

    private static Handle start(String kind, String label, JobCallType callType, boolean endOnClose) {
        if (kind == null || kind.isBlank()) {
            throw new IllegalArgumentException("The kind of the job is required");
        }
        Running job = new Running(kind, label, callType == null ? JobCallType.OTHER : callType, false);
        RUNNING.put(job.uuid, job);
        ensureSweeper();
        Handle handle = new Handle(job, endOnClose);
        handle.previous = ACTIVE.get();
        ACTIVE.set(handle);
        return handle;
    }

    /**
     * @return the job whose work this thread does now, null outside of one
     */
    public static Handle current() {
        return ACTIVE.get();
    }

    /**
     * The jobs that run now, oldest first.
     */
    public static List<ActiveJob> active() {
        List<ActiveJob> jobs = new ArrayList<>();
        for (Running job : RUNNING.values()) {
            jobs.add(job.snapshot());
        }
        jobs.sort((a, b) -> a.startedAt().compareTo(b.startedAt()));
        return jobs;
    }

    /**
     * A call to an external backend starts. It belongs to the job of this thread; outside of a job it is ignored (null), which is
     * how the checks of a connection or the hooks of a user stay out of the log.
     *
     * @param backend the name of the AI, or the host of the server (not the path, not the query)
     * @return the call to end with {@link Call#finish}, null when it is not recorded
     */
    public static Call beginCall(JobCallType type, String backend, String model) {
        Handle handle = ACTIVE.get();
        return handle == null ? null : handle.job.beginCall(type, backend, model);
    }

    /**
     * Like {@link #beginCall(JobCallType, String, String)} with the type of the job ({@link #open(String, String, JobCallType)}).
     */
    public static Call beginCall(String backend) {
        Handle handle = ACTIVE.get();
        return handle == null ? null : handle.job.beginCall(handle.job.defaultType, backend, null);
    }

    /**
     * A call that is a job of its own when the thread has none: the call of a request made outside of any job.
     *
     * @param autoKind the kind of the job it makes then
     */
    public static Call beginCallOrJob(JobCallType type, String backend, String model, String autoKind) {
        Handle handle = ACTIVE.get();
        if (handle != null) {
            return handle.job.beginCall(type, backend, model);
        }
        Running job = new Running(autoKind == null ? UNTAGGED : autoKind, null, type, true);
        RUNNING.put(job.uuid, job);
        ensureSweeper();
        return job.beginCall(type, backend, model);
    }

    /**
     * The running job and the way to end it. Not for sharing between threads except through {@link #enter()}.
     */
    public static final class Handle implements AutoCloseable {

        private final Running job;
        private final boolean endOnClose;
        private Handle previous;
        private boolean left;

        private Handle(Running job, boolean endOnClose) {
            this.job = job;
            this.endOnClose = endOnClose;
        }

        public String uuid() {
            return job.uuid;
        }

        /**
         * Makes this job the one of the calling thread (a worker thread of the job), until the scope is closed.
         */
        public Scope enter() {
            Scope scope = new Scope(ACTIVE.get());
            ACTIVE.set(this);
            return scope;
        }

        /**
         * Ends the job with the status; the first end counts, the later ones are ignored.
         */
        public void finish(JobStatus status) {
            job.finish(status == null || status == JobStatus.RUNNING ? JobStatus.COMPLETED : status);
        }

        /**
         * Ends the job as failed.
         */
        public void fail() {
            job.finish(JobStatus.FAILED);
        }

        /**
         * Leaves the job on this thread, and ends it as completed when it was opened with {@link #open}.
         */
        @Override
        public void close() {
            if (!left) {
                left = true;
                if (previous == null) {
                    ACTIVE.remove();
                } else {
                    ACTIVE.set(previous);
                }
            }
            if (endOnClose) {
                job.finish(JobStatus.COMPLETED);
            }
        }
    }

    /**
     * A job taken over on another thread, see {@link Handle#enter()}.
     */
    public static final class Scope implements AutoCloseable {

        private final Handle previous;

        private Scope(Handle previous) {
            this.previous = previous;
        }

        @Override
        public void close() {
            if (previous == null) {
                ACTIVE.remove();
            } else {
                ACTIVE.set(previous);
            }
        }
    }

    /**
     * A call in flight. Ending is idempotent and may happen on any thread.
     */
    public static final class Call {

        private final Running job;
        private final Entry entry;

        private Call(Running job, Entry entry) {
            this.job = job;
            this.entry = entry;
        }

        /**
         * @param status how it ended ({@code RUNNING} is taken as completed)
         * @param error  the message of the failure, null when there is none
         */
        public void finish(JobStatus status, String error) {
            job.endCall(entry, status == null || status == JobStatus.RUNNING ? JobStatus.COMPLETED : status, error);
        }
    }

    private static final class Entry {

        final JobCallType type;
        final String backend;
        final String model;
        final Instant startedAt = Instant.now();
        volatile JobStatus status = JobStatus.RUNNING;
        volatile Instant endedAt;
        volatile String error;

        Entry(JobCallType type, String backend, String model) {
            this.type = type;
            this.backend = backend;
            this.model = model;
        }
    }

    private static final class Running {

        final String uuid = UUID.randomUUID().toString();
        final Long userId;
        final String login;
        final String kind;
        final String label;
        final JobCallType defaultType;
        final boolean auto;
        final Instant startedAt = Instant.now();
        final List<Entry> calls = new CopyOnWriteArrayList<>();
        final AtomicInteger callCount = new AtomicInteger();
        final AtomicInteger failedCalls = new AtomicInteger();
        final AtomicInteger inFlight = new AtomicInteger();
        final AtomicBoolean finished = new AtomicBoolean();
        volatile Instant lastActivity = startedAt;

        Running(String kind, String label, JobCallType defaultType, boolean auto) {
            Owner owner = null;
            try {
                owner = ownerResolver.get();
            } catch (Exception e) {
                // a thread without a session has no user: the job then has no owner
                log.debug("No user for the job: {}", e.getMessage());
            }
            this.userId = owner == null ? null : owner.userId();
            this.login = owner == null ? null : owner.login();
            this.kind = kind;
            this.label = label == null || label.length() <= 255 ? label : label.substring(0, 255);
            this.defaultType = defaultType;
            this.auto = auto;
        }

        Call beginCall(JobCallType type, String backend, String model) {
            Entry entry = new Entry(type == null ? defaultType : type, backend, model);
            callCount.incrementAndGet();
            inFlight.incrementAndGet();
            lastActivity = entry.startedAt;
            if (calls.size() < MAX_KEPT_CALLS) {
                calls.add(entry);
            }
            return new Call(this, entry);
        }

        synchronized void endCall(Entry entry, JobStatus status, String error) {
            if (entry.status != JobStatus.RUNNING) {
                return;
            }
            entry.endedAt = Instant.now();
            entry.error = error == null || error.length() <= MAX_ERROR ? error : error.substring(0, MAX_ERROR);
            entry.status = status;
            lastActivity = entry.endedAt;
            inFlight.decrementAndGet();
            if (status == JobStatus.FAILED) {
                failedCalls.incrementAndGet();
            }
            if (auto && inFlight.get() == 0) {
                finish(status);
            }
        }

        void finish(JobStatus status) {
            if (!finished.compareAndSet(false, true)) {
                return;
            }
            RUNNING.remove(uuid);
            Instant end = Instant.now();
            List<FinishedCall> done = new ArrayList<>();
            for (Entry entry : calls) {
                // a call still in flight when its job ends was abandoned by it
                JobStatus callStatus = entry.status == JobStatus.RUNNING ? JobStatus.CANCELLED : entry.status;
                done.add(new FinishedCall(entry.type, entry.backend, entry.model, callStatus, entry.startedAt,
                        entry.endedAt == null ? end : entry.endedAt, entry.error));
            }
            FinishedJob result = new FinishedJob(uuid, userId, login, kind, label, status, startedAt, end, done, callCount.get(),
                    failedCalls.get());
            for (Consumer<FinishedJob> sink : SINKS) {
                try {
                    sink.accept(result);
                } catch (Exception e) {
                    log.warn("The job {} ({}) was not logged: {}", uuid, kind, e.getMessage());
                }
            }
        }

        ActiveJob snapshot() {
            List<ActiveJob.ActiveCall> view = new ArrayList<>();
            for (Entry entry : calls) {
                view.add(new ActiveJob.ActiveCall(entry.type, entry.backend, entry.model, entry.status, entry.startedAt,
                        entry.endedAt));
            }
            return new ActiveJob(uuid, userId, login, kind, label, startedAt, view, Math.max(0, inFlight.get()), callCount.get());
        }
    }

    private static void ensureSweeper() {
        if (sweeper != null) {
            return;
        }
        synchronized (Jobs.class) {
            if (sweeper == null) {
                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(task -> {
                    Thread thread = new Thread(task, "job-sweeper");
                    thread.setDaemon(true);
                    return thread;
                });
                executor.scheduleWithFixedDelay(() -> sweep(IDLE_LIMIT), 1, 1, TimeUnit.MINUTES);
                sweeper = executor;
            }
        }
    }

    /**
     * Ends the jobs nobody ended: no call in flight and no activity for the idle time ({@link #IDLE_LIMIT} when the application
     * does it). Public for tests.
     */
    public static void sweep(Duration idle) {
        Instant limit = Instant.now().minus(idle);
        for (Running job : RUNNING.values()) {
            if (job.inFlight.get() <= 0 && job.lastActivity.isBefore(limit)) {
                job.finish(JobStatus.COMPLETED);
            }
        }
    }
}
