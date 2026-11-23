package com.github.enerccio.marginalia.domain.service.gpu;

import com.github.enerccio.marginalia.domain.service.gpu.GpuStepRunner.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntConsumer;

/**
 * The turns on the GPUs, in memory (the rules are described at {@code GpuService}). Knows the locks only through the resolver, so it
 * needs no database and is tested on its own; the steps run on a thread of their own, never under a lock and never on the caller's
 * thread of {@link #acquireAsync}.
 */
public final class GpuArbiter {

    private static final Logger log = LoggerFactory.getLogger(GpuArbiter.class);
    private static final long POLL_MILLIS = 200;

    /** The variable a text generation job brings next to {@code model}: the model it generates with. */
    public static final String TEXT_MODEL = "text_model";

    /**
     * The variable every job of a lock gets: the model of the last text generation job of the lock ({@link #TEXT_MODEL}), remembered in
     * memory from the moment that job is taken; empty before the first one.
     */
    public static final String LAST_TEXT_MODEL = "last_text_model";

    /**
     * A lock as the arbiter needs it, read from the database by the resolver.
     *
     * @param maxConcurrent 0 for no limit
     * @param acquireEachJob the GPU is used outside the application too: what it holds is never trusted, the acquire steps run before every job of
     *                       the lock, also when the GPU is loaded with it
     * @param always         run before every job of the lock, whether the GPU is loaded with it or not (before the acquire steps of a switch)
     */
    public record LockDef(String uuid, String name, String gpuUuid, int maxConcurrent, List<Step> acquire, List<Step> release, boolean acquireEachJob,
                          List<Step> always) {

        public LockDef {
            always = always == null ? List.of() : always;
        }

        public LockDef(String uuid, String name, String gpuUuid, int maxConcurrent, List<Step> acquire, List<Step> release, boolean acquireEachJob) {
            this(uuid, name, gpuUuid, maxConcurrent, acquire, release, acquireEachJob, List.of());
        }

        public LockDef(String uuid, String name, String gpuUuid, int maxConcurrent, List<Step> acquire, List<Step> release) {
            this(uuid, name, gpuUuid, maxConcurrent, acquire, release, false);
        }

        /** What runs before a job of the lock when the GPU is already loaded with it: the always steps, and the acquire steps too on a GPU used outside. */
        List<Step> eachJob() {
            if (!acquireEachJob) {
                return always;
            }
            List<Step> steps = new ArrayList<>(always);
            steps.addAll(acquire);
            return steps;
        }

        /** What runs when the GPU switches to the lock: the always steps, then the acquire steps. */
        List<Step> onSwitch() {
            if (always.isEmpty()) {
                return acquire;
            }
            List<Step> steps = new ArrayList<>(always);
            steps.addAll(acquire);
            return steps;
        }
    }

    /** Runs the steps; {@link GpuStepRunner} in the application, a fake in the tests. */
    public interface StepRunner {

        /** In order, the first failure stops them. */
        void runAll(List<Step> steps) throws GpuException;

        /** All of them; the first failure, null when all went well. */
        String runAllRegardless(List<Step> steps);
    }

    /** The runner of the application. */
    public static final StepRunner HTTP = new StepRunner() {
        @Override
        public void runAll(List<Step> steps) throws GpuException {
            GpuStepRunner.runAll(steps);
        }

        @Override
        public String runAllRegardless(List<Step> steps) {
            return GpuStepRunner.runAllRegardless(steps);
        }
    };

    /**
     * A job (or an unload, {@code lock == null}) in the queue of a GPU, with the variables its steps are filled with.
     */
    private record Waiter(LockDef lock, Map<String, String> variables, CompletableFuture<GpuLease> lease, CompletableFuture<String> unload,
                          IntConsumer position) {

        CompletableFuture<?> future() {
            return lock == null ? unload : lease;
        }
    }

    /** One GPU; everything is guarded by the monitor of the object. */
    private static final class State {
        final String gpuUuid;
        LockDef loaded;
        /** The variables of the last job of the loaded lock: its release steps are filled with them. */
        Map<String, String> loadedVariables = Map.of();
        /** {@link #LAST_TEXT_MODEL} per lock uuid. */
        final Map<String, String> lastTextModel = new HashMap<>();
        int active;
        boolean switching;
        final Deque<Waiter> queue = new ArrayDeque<>();

        State(String gpuUuid) {
            this.gpuUuid = gpuUuid;
        }
    }

    private final Function<String, LockDef> resolver;
    private final StepRunner runner;
    private final Executor executor;
    private final Map<String, State> states = new HashMap<>();

    /**
     * @param resolver the lock of a uuid as it is now, null when there is none (called with any uuid, also blank ones)
     */
    public GpuArbiter(Function<String, LockDef> resolver, StepRunner runner) {
        this(resolver, runner, Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("gpu-switch-", 0).factory()));
    }

    public GpuArbiter(Function<String, LockDef> resolver, StepRunner runner, Executor executor) {
        this.resolver = resolver;
        this.runner = runner;
        this.executor = executor;
    }

    /** See {@code GpuService#acquireAsync}. */
    public CompletableFuture<GpuLease> acquireAsync(String lockUuid, IntConsumer position) {
        return acquireAsync(lockUuid, Map.of(), position);
    }

    /** See {@code GpuService#acquireAsync}; {@code variables} fill the steps (null: none). */
    public CompletableFuture<GpuLease> acquireAsync(String lockUuid, Map<String, String> variables, IntConsumer position) {
        LockDef lock = lockUuid == null || lockUuid.isBlank() ? null : resolver.apply(lockUuid);
        if (lock == null) {
            if (lockUuid != null && !lockUuid.isBlank()) {
                log.debug("The GPU lock {} does not exist, the job runs without one", lockUuid);
            }
            return CompletableFuture.completedFuture(GpuLease.NONE);
        }
        CompletableFuture<GpuLease> future = new CompletableFuture<>();
        enqueue(new Waiter(lock, variables(lock, variables), future, null, position));
        return future;
    }

    /** See {@code GpuService#acquire}. */
    public GpuLease acquire(String lockUuid, BooleanSupplier cancelled) throws GpuException {
        return acquire(lockUuid, Map.of(), cancelled);
    }

    /** See {@code GpuService#acquire}; {@code variables} fill the steps (null: none). */
    public GpuLease acquire(String lockUuid, Map<String, String> variables, BooleanSupplier cancelled) throws GpuException {
        CompletableFuture<GpuLease> future = acquireAsync(lockUuid, variables, null);
        while (true) {
            try {
                return future.get(POLL_MILLIS, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                if (cancelled != null && cancelled.getAsBoolean()) {
                    giveUp(future);
                    throw GpuException.cancelled();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                giveUp(future);
                throw GpuException.cancelled();
            } catch (CancellationException e) {
                throw GpuException.cancelled();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof GpuException gpu) {
                    throw gpu;
                }
                throw new GpuException(String.valueOf(e.getCause() == null ? e.getMessage() : e.getCause().getMessage()), e.getCause());
            }
        }
    }

    /** Out of the queue; a lease given meanwhile goes back. */
    private static void giveUp(CompletableFuture<GpuLease> future) {
        if (!future.cancel(true)) {
            GpuLease granted = future.getNow(null);
            if (granted != null) {
                granted.close();
            }
        }
    }

    /**
     * Waits for the jobs of the GPU, runs the release steps of its lock and leaves it unknown.
     *
     * @return the error of a release step, null when they went well
     */
    public CompletableFuture<String> unload(String gpuUuid) {
        CompletableFuture<String> future = new CompletableFuture<>();
        enqueue(new Waiter(null, Map.of(), null, future, null), gpuUuid);
        return future;
    }

    public GpuStatus status(String gpuUuid) {
        State state = state(gpuUuid);
        synchronized (state) {
            state.queue.removeIf(w -> w.future().isDone());
            return new GpuStatus(gpuUuid, state.loaded == null ? null : state.loaded.name(), state.active, state.queue.size(), state.switching);
        }
    }

    /** The uuid of the lock the GPU is loaded with, null when none is known. */
    public String loadedLock(String gpuUuid) {
        State state = state(gpuUuid);
        synchronized (state) {
            return state.loaded == null ? null : state.loaded.uuid();
        }
    }

    // --- the queue ---

    private void enqueue(Waiter waiter) {
        enqueue(waiter, waiter.lock().gpuUuid());
    }

    private void enqueue(Waiter waiter, String gpuUuid) {
        State state = state(gpuUuid);
        List<Runnable> after = new ArrayList<>();
        synchronized (state) {
            state.queue.addLast(waiter);
            admit(state, after);
            positions(state, after);
        }
        // a future cancelled in the queue goes out of the way of the others
        waiter.future().whenComplete((value, error) -> {
            if (error instanceof CancellationException) {
                List<Runnable> moved = new ArrayList<>();
                synchronized (state) {
                    state.queue.removeIf(w -> w == waiter);
                    admit(state, moved);
                    positions(state, moved);
                }
                run(moved);
            }
        });
        run(after);
    }

    private State state(String gpuUuid) {
        synchronized (states) {
            return states.computeIfAbsent(gpuUuid == null ? "" : gpuUuid, State::new);
        }
    }

    /**
     * Lets in whom it is the turn of, the oldest first: jobs of the loaded lock while there is room, or one switch when the GPU is free.
     * Called with the monitor of the state; what is to be done outside of it goes to {@code after}.
     */
    private void admit(State state, List<Runnable> after) {
        while (!state.switching) {
            Waiter head = state.queue.peekFirst();
            if (head == null) {
                return;
            }
            if (head.future().isDone()) {
                state.queue.pollFirst();
                continue;
            }
            if (head.lock() != null && state.loaded != null && head.lock().uuid().equals(state.loaded.uuid())) {
                int max = head.lock().maxConcurrent();
                if (max > 0 && state.active >= max) {
                    return;
                }
                state.queue.pollFirst();
                state.active++;
                Map<String, String> variables = take(state, head);
                state.loadedVariables = variables;
                Held held = new Held(state, head.lock(), variables);
                if (!head.lock().eachJob().isEmpty()) {
                    // the always steps, and on a GPU used outside the acquire steps too: the GPU may have been used by someone else since
                    // (they make room themselves)
                    after.add(() -> executor.execute(() -> recheck(state, head, variables, held)));
                } else {
                    after.add(() -> grant(head, held));
                }
                continue;
            }
            if (state.active > 0) {
                // another lock works: everybody waits behind the head, so nobody starves it
                return;
            }
            state.queue.pollFirst();
            state.switching = true;
            LockDef from = state.loaded;
            Map<String, String> fromVariables = state.loadedVariables;
            Map<String, String> variables = head.lock() == null ? Map.of() : take(state, head);
            after.add(() -> executor.execute(() -> switchTo(state, from, fromVariables, head, variables)));
            return;
        }
    }

    /**
     * The variables of a job that is taken: a text job's model becomes the lock's {@link #LAST_TEXT_MODEL} first, so its own steps see it too;
     * every job gets the remembered one. Called with the monitor of the state.
     */
    private static Map<String, String> take(State state, Waiter waiter) {
        String uuid = waiter.lock().uuid();
        String text = waiter.variables().get(TEXT_MODEL);
        if (text != null && !text.isBlank()) {
            state.lastTextModel.put(uuid, text);
        }
        Map<String, String> all = new HashMap<>(waiter.variables());
        all.remove(LAST_TEXT_MODEL);
        String last = state.lastTextModel.get(uuid);
        if (last != null) {
            all.put(LAST_TEXT_MODEL, last);
        }
        return Map.copyOf(all);
    }

    /** The places of the ones that wait. Called with the monitor of the state. */
    private static void positions(State state, List<Runnable> after) {
        int ahead = state.active + (state.switching ? 1 : 0);
        for (Waiter waiter : state.queue) {
            if (waiter.position() != null && !waiter.future().isDone()) {
                int mine = ahead;
                after.add(() -> {
                    try {
                        waiter.position().accept(mine);
                    } catch (RuntimeException e) {
                        log.debug("A listener of the place in the GPU queue failed: {}", e.getMessage(), e);
                    }
                });
            }
            ahead++;
        }
    }

    private static void grant(Waiter waiter, Held held) {
        // false for a future that was cancelled meanwhile: the place goes to the next one
        if (!waiter.lease().complete(held)) {
            held.close();
        }
    }

    private static void run(List<Runnable> actions) {
        for (Runnable action : actions) {
            action.run();
        }
    }

    /**
     * Frees the GPU of the lock it is loaded with and loads the lock of the waiter (or nothing, for an unload). Runs on a thread of its own.
     */
    private void switchTo(State state, LockDef from, Map<String, String> fromVariables, Waiter waiter, Map<String, String> variables) {
        String releaseError = null;
        if (from != null) {
            // the steps as they are now (the administrator may have changed them since the lock was loaded)
            LockDef current = resolver.apply(from.uuid());
            List<Step> release = (current != null ? current : from).release();
            log.info("GPU {}: releasing \"{}\" for {}", state.gpuUuid, from.name(), waiter.lock() == null ? "an unload" : "\"" + waiter.lock().name() + "\"");
            releaseError = safeRelease(fill(release, fromVariables));
        }
        if (waiter.lock() == null) {
            // the administrator asked: the GPU is taken as free whatever the steps answered (a dead server must not block it for ever)
            finish(state, null, Map.of(), null);
            waiter.unload().complete(releaseError);
            return;
        }
        if (releaseError != null) {
            // the GPU may still be full: nothing is loaded on it, it stays with the lock it had (the next switch tries to free it again)
            log.warn("GPU {}: freeing it of \"{}\" failed, \"{}\" is not loaded: {}", state.gpuUuid, from.name(), waiter.lock().name(), releaseError);
            finish(state, from, fromVariables, null);
            waiter.lease().completeExceptionally(new GpuException("Freeing the GPU of \"" + from.name() + "\" failed: " + releaseError));
            return;
        }
        try {
            log.info("GPU {}: acquiring \"{}\"", state.gpuUuid, waiter.lock().name());
            runner.runAll(fill(waiter.lock().onSwitch(), variables));
        } catch (GpuException | RuntimeException e) {
            log.warn("GPU {}: the acquire steps of \"{}\" failed: {}", state.gpuUuid, waiter.lock().name(), e.getMessage());
            finish(state, null, Map.of(), null);
            waiter.lease().completeExceptionally(e instanceof GpuException ? e : new GpuException(e.getMessage(), e));
            return;
        }
        Held held = new Held(state, waiter.lock(), variables);
        finish(state, waiter.lock(), variables, held);
        grant(waiter, held);
    }

    /**
     * The always steps of the loaded lock before a job, and its acquire steps again on a GPU that is used outside the application too; the job's
     * place is already counted. A failure fails the job and leaves the GPU unknown. Runs on a thread of its own.
     */
    private void recheck(State state, Waiter waiter, Map<String, String> variables, Held held) {
        LockDef current = resolver.apply(waiter.lock().uuid());
        try {
            log.debug("GPU {}: the steps of \"{}\" before a job", state.gpuUuid, waiter.lock().name());
            runner.runAll(fill((current != null ? current : waiter.lock()).eachJob(), variables));
        } catch (GpuException | RuntimeException e) {
            log.warn("GPU {}: the acquire steps of \"{}\" failed: {}", state.gpuUuid, waiter.lock().name(), e.getMessage());
            synchronized (state) {
                if (state.loaded != null && state.loaded.uuid().equals(waiter.lock().uuid())) {
                    state.loaded = null;
                }
            }
            // gives the place back and lets the queue go on (a job of the same lock now switches: its steps run again)
            held.close();
            waiter.lease().completeExceptionally(e instanceof GpuException ? e : new GpuException(e.getMessage(), e));
            return;
        }
        grant(waiter, held);
    }

    /** The variables of a job: its own, and {@code lock}, the name of the lock. */
    private static Map<String, String> variables(LockDef lock, Map<String, String> given) {
        Map<String, String> all = new HashMap<>();
        if (given != null) {
            given.forEach((name, value) -> {
                if (name != null && value != null) {
                    all.put(name, value);
                }
            });
        }
        all.putIfAbsent("lock", lock.name());
        return Map.copyOf(all);
    }

    private static List<Step> fill(List<Step> steps, Map<String, String> variables) {
        return steps.stream().map(step -> step.with(variables)).toList();
    }

    private String safeRelease(List<Step> release) {
        try {
            return runner.runAllRegardless(release);
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }

    /** The end of a switch: the GPU is loaded with {@code loaded} (null: unknown), the holder of {@code held} counts, the queue goes on. */
    private void finish(State state, LockDef loaded, Map<String, String> variables, Held held) {
        List<Runnable> after = new ArrayList<>();
        synchronized (state) {
            state.loaded = loaded;
            state.loadedVariables = loaded == null ? Map.of() : variables;
            state.switching = false;
            if (held != null) {
                state.active++;
            }
            admit(state, after);
            positions(state, after);
        }
        run(after);
    }

    private final class Held implements GpuLease {

        private final State state;
        private final LockDef lock;
        private final Map<String, String> variables;
        private final AtomicBoolean closed = new AtomicBoolean();

        Held(State state, LockDef lock, Map<String, String> variables) {
            this.state = state;
            this.lock = lock;
            this.variables = variables;
        }

        @Override
        public void reload() throws GpuException {
            if (closed.get()) {
                throw new GpuException("The lease was given back");
            }
            LockDef current = resolver.apply(lock.uuid());
            LockDef steps = current != null ? current : lock;
            log.info("GPU {}: reloading \"{}\"", state.gpuUuid, lock.name());
            safeRelease(fill(steps.release(), variables));
            runner.runAll(fill(steps.onSwitch(), variables));
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                List<Runnable> after = new ArrayList<>();
                synchronized (state) {
                    state.active--;
                    admit(state, after);
                    positions(state, after);
                }
                run(after);
            }
        }
    }
}
