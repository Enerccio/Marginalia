package com.github.enerccio.marginalia.extensions.audiobook.queue;

import com.github.enerccio.marginalia.extensions.audiobook.AudiobookLog;
import com.github.enerccio.marginalia.extensions.audiobook.audio.AudioKind;
import com.github.enerccio.marginalia.extensions.audiobook.audio.Ffmpeg;
import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.audiobook.queue.ConversionJob.ScriptSource;
import com.github.enerccio.marginalia.extensions.audiobook.queue.ConversionJob.State;
import com.github.enerccio.marginalia.extensions.audiobook.script.PartScript;
import com.github.enerccio.marginalia.extensions.audiobook.script.ScriptEntry;
import com.github.enerccio.marginalia.extensions.audiobook.service.SpeechBackend;
import com.github.enerccio.marginalia.extensions.audiobook.service.SpeechBatch;
import com.github.enerccio.marginalia.extensions.audiobook.service.SpeechRequest;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The conversion of parts to audio, one user's queue: jobs wait, and one worker takes <b>all</b> that wait as one run. A run makes the pieces
 * that are not stored yet, <b>grouped by speech model</b> (one batch on the server of each model: its requests before, all its pieces, its
 * requests after; so the language model is swapped out once per model and not once per part), and then joins the pieces of every part in
 * order with their pauses. Everything is cached by content: a piece with a stored audio is not made again, a part whose script is the one stored
 * costs no batch at all, and a retry after a failure keeps what was made.
 * <p>
 * The queue knows the server, the store and the voices only through the {@link Environment}, so it runs without any of them in tests. Batches of
 * several users on one GPU are serialised by the gate the {@code BatchRunner} goes through, not by this class.
 */
public class ConversionQueue implements AutoCloseable {

    /** Runs a batch on the server of a backend (the requests before and after it around the work): {@code AudiobookService::runBatch}. */
    @FunctionalInterface
    public interface BatchRunner {
        <T> SpeechBatch.Outcome<T> run(BackendConfig backend, BooleanSupplier cancelled, SpeechBatch.Work<T> work) throws BackendException;
    }

    /**
     * @param backends  the backend that makes a model, null when the user has none for it
     * @param paused    true while a generation of the user runs: the language model would be loaded again under the speech server, so a batch waits
     * @param pollMillis how often a waiting batch looks at {@code paused}
     * @param loudness  the loudness in LUFS the joined audio of a part is levelled to, null for none (the user turned it off, or there is no ffmpeg)
     * @param leveller  what levels the audio
     */
    public record Environment(AudioStore store, Voices voices, Function<String, BackendConfig> backends, BatchRunner runner, BooleanSupplier paused,
                              long pollMillis, Supplier<Double> loudness, Leveller leveller) {

        /** An environment that does not level the loudness. */
        public Environment(AudioStore store, Voices voices, Function<String, BackendConfig> backends, BatchRunner runner, BooleanSupplier paused,
                           long pollMillis) {
            this(store, voices, backends, runner, paused, pollMillis, () -> null, Ffmpeg::normalizeLoudness);
        }

        /** An environment that levels the loudness with ffmpeg when the supplier gives a target. */
        public Environment(AudioStore store, Voices voices, Function<String, BackendConfig> backends, BatchRunner runner, BooleanSupplier paused,
                           long pollMillis, Supplier<Double> loudness) {
            this(store, voices, backends, runner, paused, pollMillis, loudness, Ffmpeg::normalizeLoudness);
        }
    }

    /** Levels the joined audio of a part to a loudness in LUFS (it needs ffmpeg). */
    @FunctionalInterface
    public interface Leveller {
        byte[] level(byte[] audio, double lufs) throws BackendException;
    }

    private final Environment env;
    private final ExecutorService worker;
    private final List<ConversionJob> jobs = new ArrayList<>();
    private final List<Consumer<ConversionJob.Snapshot>> listeners = new CopyOnWriteArrayList<>();
    private boolean draining;
    private volatile boolean stopRun;

    public ConversionQueue(Environment env) {
        this(env, Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "audiobook-queue");
            t.setDaemon(true);
            return t;
        }));
    }

    /** @param worker the one thread the run is made on (the application's gives it the request context of the user); closed with the queue */
    public ConversionQueue(Environment env, ExecutorService worker) {
        this.env = env;
        this.worker = worker;
    }

    /** Called (from the worker thread) with the snapshot of a job each time its state or progress changes. */
    public void addListener(Consumer<ConversionJob.Snapshot> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<ConversionJob.Snapshot> listener) {
        listeners.remove(listener);
    }

    /** A part to convert, or (by {@code mode}) only to annotate, or only to speak some of its pieces. */
    public record Task(String partId, String title, ScriptSource source, ConversionJob.Mode mode) {

        public Task(String partId, String title, ScriptSource source) {
            this(partId, title, source, ConversionJob.Mode.PART);
        }
    }

    /** Adds a job for the part; it waits for the next run. A part that already has a waiting or running job gets none (the job is returned). */
    public ConversionJob submit(String partId, String title, ScriptSource source) {
        return submitAll(List.of(new Task(partId, title, source))).getFirst();
    }

    /**
     * Adds the jobs for the parts <b>together</b>: the worker is started after all of them are in, so they are one run (one batch per model), not
     * the first one alone and the rest in a second run (which would swap the language model out once more).
     *
     * @return the jobs in the order of the tasks; a part that already waits or runs gets its job returned
     */
    public synchronized List<ConversionJob> submitAll(List<Task> tasks) {
        List<ConversionJob> result = new ArrayList<>();
        for (Task task : tasks) {
            ConversionJob existing = jobs.stream()
                    .filter(j -> j.partId().equals(task.partId()) && (j.state() == State.WAITING || j.state() == State.RUNNING)).findFirst().orElse(null);
            if (existing != null) {
                result.add(existing);
                continue;
            }
            ConversionJob job = new ConversionJob(task.partId(), task.title(), task.source(), task.mode());
            jobs.add(job);
            fire(job);
            result.add(job);
        }
        start();
        return result;
    }

    /** The jobs in the order they were submitted. */
    public synchronized List<ConversionJob.Snapshot> jobs() {
        return jobs.stream().map(ConversionJob::snapshot).toList();
    }

    /** Stops the job: a waiting one is cancelled at once, a running one when the piece that is being made is done. */
    public synchronized void cancel(String jobId) {
        for (ConversionJob job : jobs) {
            if (job.id().equals(jobId)) {
                job.requestCancel();
                if (job.state() == State.WAITING) {
                    job.set(State.CANCELLED, "");
                    fire(job);
                }
            }
        }
    }

    /** Stops everything: the jobs that wait are cancelled, the run that is going stops after the piece it is making. */
    public synchronized void cancelAll() {
        stopRun = true;
        for (ConversionJob job : jobs) {
            job.requestCancel();
            if (job.state() == State.WAITING) {
                job.set(State.CANCELLED, "");
                fire(job);
            }
        }
    }

    /** Tries again a job that failed or was cancelled; the pieces that were made are kept. */
    public synchronized void retry(String jobId) {
        for (ConversionJob job : jobs) {
            if (job.id().equals(jobId) && (job.state() == State.FAILED || job.state() == State.CANCELLED)) {
                job.reset();
                fire(job);
                start();
            }
        }
    }

    /** Forgets the jobs that are over. */
    public synchronized void clearFinished() {
        jobs.removeIf(j -> j.snapshot().finished());
    }

    /** Waits until nothing waits or runs; for tests and for shutting down. */
    public boolean awaitIdle(long timeout, TimeUnit unit) throws InterruptedException {
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        while (System.nanoTime() < deadline) {
            synchronized (this) {
                if (!draining && jobs.stream().noneMatch(j -> j.state() == State.WAITING || j.state() == State.RUNNING)) {
                    return true;
                }
            }
            Thread.sleep(10);
        }
        return false;
    }

    @Override
    public void close() {
        cancelAll();
        worker.shutdownNow();
    }

    // --- the run ---

    private synchronized void start() {
        if (!draining) {
            draining = true;
            stopRun = false;
            worker.execute(this::drain);
        }
    }

    private void drain() {
        try {
            while (true) {
                List<ConversionJob> batch;
                synchronized (this) {
                    batch = jobs.stream().filter(j -> j.state() == State.WAITING).toList();
                    if (batch.isEmpty()) {
                        draining = false;
                        return;
                    }
                    stopRun = false;
                    batch.forEach(j -> j.set(State.RUNNING, ""));
                }
                batch.forEach(this::fire);
                try {
                    run(batch);
                } catch (RuntimeException e) {
                    AudiobookLog.get().warn("The conversion failed: {}", e.getMessage(), e);
                    for (ConversionJob job : batch) {
                        if (job.state() == State.RUNNING) {
                            finish(job, State.FAILED, String.valueOf(e.getMessage()));
                        }
                    }
                }
            }
        } finally {
            synchronized (this) {
                draining = false;
            }
        }
    }

    /** What a job needs: the script and the pieces that are not stored. */
    private record Plan(ConversionJob job, PartScript script) {
    }

    private record Need(Plan plan, ScriptEntry entry) {
    }

    private void run(List<ConversionJob> batch) {
        // 1. the scripts, and what is stored already
        List<Plan> plans = new ArrayList<>();
        Map<String, List<Need>> byModel = new LinkedHashMap<>();
        for (ConversionJob job : batch) {
            if (cancelled(job)) {
                continue;
            }
            PartScript script;
            try {
                // making the script may ask the language model for the labels: Stop ends that request too
                script = com.github.enerccio.marginalia.extensions.audiobook.label.LabelCancel.during(() -> stopRun || job.cancelRequested(),
                        () -> job.source().get());
            } catch (Exception e) {
                if (job.cancelRequested() || stopRun) {
                    finish(job, State.CANCELLED, "");
                    continue;
                }
                finish(job, State.FAILED, "The script of the part could not be made: " + e.getMessage());
                continue;
            }
            if (script.entries().isEmpty()) {
                finish(job, State.FAILED, "The part has no text to read");
                continue;
            }
            if (job.mode() == ConversionJob.Mode.SCRIPT_ONLY) {
                finish(job, State.DONE, script.entries().size() + " segments");
                continue;
            }
            AudioStore.PartAudio stored = env.store().partAudio(job.partId());
            // up to date: the same pieces of the same text and, when the user wants the loudness levelled, levelled to it (a part made before is joined again from
            // its stored pieces and levelled, which asks nothing of the speech server); turning the levelling off leaves the parts as they are
            Double wanted = env.loudness().get();
            if (job.mode() == ConversionJob.Mode.PART && stored != null && stored.joinKey().equals(script.joinKey()) && stored.sourceHash().equals(script.sourceHash())
                    && (wanted == null || wanted.equals(stored.loudness()))) {
                job.progress(distinct(script), distinct(script));
                finish(job, State.DONE, "Up to date");
                continue;
            }
            Plan plan = new Plan(job, script);
            plans.add(plan);
            int total = 0;
            int present = 0;
            java.util.Set<String> seen = new java.util.HashSet<>();
            for (ScriptEntry entry : script.entries()) {
                if (!seen.add(entry.key())) {
                    continue;
                }
                total++;
                if (env.store().segment(job.partId(), entry.key()) != null) {
                    present++;
                } else {
                    byModel.computeIfAbsent(entry.model(), m -> new ArrayList<>()).add(new Need(plan, entry));
                }
            }
            job.progress(present, total);
            fire(job);
        }

        // 2. the pieces, one batch per model
        for (Map.Entry<String, List<Need>> group : byModel.entrySet()) {
            speak(group.getKey(), group.getValue());
        }

        // 3. the parts
        for (Plan plan : plans) {
            if (plan.job().state() == State.RUNNING) {
                if (plan.job().mode() == ConversionJob.Mode.SEGMENTS_ONLY) {
                    finish(plan.job(), State.DONE, distinct(plan.script()) + " pieces");
                } else {
                    join(plan);
                }
            }
        }
    }

    private static int distinct(PartScript script) {
        return (int) script.entries().stream().map(ScriptEntry::key).distinct().count();
    }

    private void speak(String model, List<Need> needs) {
        List<Need> live = needs.stream().filter(n -> n.plan().job().state() == State.RUNNING).toList();
        if (live.isEmpty()) {
            return;
        }
        BackendConfig backend = env.backends().apply(model);
        if (backend == null) {
            failAll(live, "There is no backend for the model " + model + " (add one in Settings → Extension Settings → Audiobook Settings)");
            return;
        }
        BooleanSupplier stop = () -> stopRun || live.stream().allMatch(n -> n.plan().job().cancelRequested() || n.plan().job().state() != State.RUNNING);
        try {
            waitWhilePaused(stop);
            SpeechBatch.Outcome<Void> outcome = env.runner().run(backend, stop, speaker -> {
                makePieces(speaker, backend, live, stop);
                return null;
            });
            if (outcome.afterError() != null) {
                for (Need need : live) {
                    ConversionJob job = need.plan().job();
                    if (job.state() == State.RUNNING) {
                        job.set(State.RUNNING, "The request after the batch failed: " + outcome.afterError());
                    }
                }
            }
        } catch (BackendException e) {
            // the requests before the batch, the model check or the wait failed (or the user stopped it): the pieces made so far are kept
            for (Need need : live) {
                ConversionJob job = need.plan().job();
                if (job.state() == State.RUNNING) {
                    if (job.cancelRequested() || stopRun) {
                        finish(job, State.CANCELLED, "");
                    } else {
                        finish(job, State.FAILED, e.getMessage());
                    }
                }
            }
        }
    }

    private void makePieces(SpeechBackend speaker, BackendConfig backend, List<Need> needs, BooleanSupplier stop) throws BackendException {
        java.util.Set<String> made = new java.util.HashSet<>();
        for (Need need : needs) {
            ConversionJob job = need.plan().job();
            if (job.state() != State.RUNNING) {
                continue;
            }
            if (stop.getAsBoolean()) {
                throw new BackendException("Cancelled");
            }
            if (job.cancelRequested()) {
                finish(job, State.CANCELLED, "");
                continue;
            }
            ScriptEntry entry = need.entry();
            String id = job.partId() + "/" + entry.key();
            if (!made.add(id)) {
                continue;
            }
            try {
                Voices.Resolved voice = env.voices().resolve(speaker, entry.voice());
                SpeechRequest request = new SpeechRequest(entry.text(), voice.mode(), voice.name(), entry.language(), entry.exaggeration(),
                        entry.cfgWeight(), entry.temperature(), entry.speed(), entry.seed(), entry.format(), entry.chunkSize());
                byte[] audio = speaker.synthesize(request, stop::getAsBoolean);
                env.store().putSegment(job.partId(), entry.key(), audio);
                job.advance();
                fire(job);
            } catch (BackendException e) {
                if (stop.getAsBoolean()) {
                    throw e;
                }
                finish(job, State.FAILED, "A piece could not be made: " + e.getMessage());
            }
        }
    }

    private void join(Plan plan) {
        ConversionJob job = plan.job();
        List<byte[]> pieces = new ArrayList<>();
        List<Double> pauses = new ArrayList<>();
        for (ScriptEntry entry : plan.script().entries()) {
            byte[] audio = env.store().segment(job.partId(), entry.key());
            if (audio == null) {
                finish(job, State.FAILED, "A piece is missing");
                return;
            }
            pieces.add(audio);
            pauses.add(entry.pauseAfter());
        }
        try {
            byte[] joined = AudioKind.join(pieces, pauses);
            Double wanted = env.loudness().get();
            Double levelled = null;
            String note = "";
            if (wanted != null) {
                try {
                    joined = env.leveller().level(joined, wanted);
                    levelled = wanted;
                } catch (BackendException e) {
                    // the audio is good without it: it is kept as it is, and the part is asked to be levelled again the next time
                    note = "not levelled: " + e.getMessage();
                }
            }
            env.store().putPart(job.partId(), plan.script().joinKey(), plan.script().sourceHash(), joined,
                    plan.script().liveKeys(), levelled);
            String warning = job.snapshot().message();      // a request after the batch that failed, noted by speak()
            warning = warning.isEmpty() ? note : note.isEmpty() ? warning : warning + "; " + note;
            finish(job, State.DONE, plan.script().entries().size() + " pieces, " + Math.round(AudioKind.seconds(joined)) + " s"
                    + (warning.isEmpty() ? "" : " (" + warning + ")"));
        } catch (BackendException | RuntimeException e) {
            finish(job, State.FAILED, "The pieces could not be joined: " + e.getMessage());
        }
    }

    /** A batch waits while a generation of the user runs; the user's own stop ends the wait. */
    private void waitWhilePaused(BooleanSupplier stop) throws BackendException {
        while (env.paused() != null && env.paused().getAsBoolean()) {
            if (stop.getAsBoolean()) {
                throw new BackendException("Cancelled");
            }
            try {
                Thread.sleep(env.pollMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BackendException("Interrupted", e);
            }
        }
    }

    private boolean cancelled(ConversionJob job) {
        if (job.cancelRequested()) {
            finish(job, State.CANCELLED, "");
            return true;
        }
        return false;
    }

    private void failAll(List<Need> needs, String message) {
        for (Need need : needs) {
            if (need.plan().job().state() == State.RUNNING) {
                finish(need.plan().job(), State.FAILED, message);
            }
        }
    }

    private void finish(ConversionJob job, State state, String message) {
        job.set(state, message);
        try {
            env.store().flush(job.partId());
        } catch (RuntimeException e) {
            AudiobookLog.get().warn("The audio of the part could not be saved: {}", e.getMessage(), e);
            job.set(State.FAILED, "The audio could not be saved: " + e.getMessage());
        }
        fire(job);
    }

    private void fire(ConversionJob job) {
        ConversionJob.Snapshot snapshot = job.snapshot();
        for (Consumer<ConversionJob.Snapshot> listener : listeners) {
            try {
                listener.accept(snapshot);
            } catch (RuntimeException e) {
                AudiobookLog.get().warn("A listener of the queue failed: {}", e.getMessage(), e);
            }
        }
    }
}
