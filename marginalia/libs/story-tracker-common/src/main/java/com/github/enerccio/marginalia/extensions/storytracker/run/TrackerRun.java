package com.github.enerccio.marginalia.extensions.storytracker.run;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.run.Runs;
import com.github.enerccio.marginalia.extensions.storytracker.llm.Live;
import com.github.enerccio.marginalia.extensions.storytracker.process.Part;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * One processing run of a book on a worker thread: reads the next parts (a series of runs of N parts), asks the model what changed,
 * and either waits for the user to review the proposal (guided) or applies it at once (automatic, with a log). The user can keep
 * working meanwhile; a review is waited for, never forced on the user. What a tracker proposes and applies is its {@link Steps}.
 * <p>
 * Cancel stops the series: what was applied stays, what was proposed and not applied is dropped and proposed again next time.
 *
 * @param <P> the proposal of the tracker
 */
public final class TrackerRun<P extends TrackerRun.Proposal> {

    private static final Logger log = LoggerFactory.getLogger(TrackerRun.class);

    public enum Phase {
        PREPARING, ASKING, REVIEW, APPLYING, DONE, FAILED, CANCELLED;

        public boolean isFinished() {
            return this == DONE || this == FAILED || this == CANCELLED;
        }
    }

    /** What the model proposed for some parts (or for the tidy). */
    public interface Proposal {
        /** The parts the run covers (the cursor moves over them when it is committed); none for the tidy. */
        List<Part> parts();

        /** Whether the model was asked at all (not when no tracked character is present in the parts). */
        boolean asked();

        /** How many checked changes the review shows. */
        int changeCount();

        /** How many changes of the answer were dropped as unusable. */
        int dropped();

        /** What the user should know about the run (parts left for the next run, the lorebook did not fit). */
        String note();

        /** What the model answered. */
        String raw();

        /** What the model thought first, empty when the provider sends none. */
        String reasoning();

        /** How many settled parts were not read by this run because it was limited. */
        int pending();

        /** Whether it is a proposal of the tidy pass. */
        boolean tidy();

        /** Nothing was waiting: the series is over. */
        default boolean isEmpty() {
            return parts().isEmpty();
        }
    }

    /**
     * What applying a proposal did.
     *
     * @param added   the count shown first ("added", "set")
     * @param removed the count shown second ("retracted", "removed")
     * @param summary the words of the log line, after "Run N: P part(s), " (for example "2 added, 1 retracted")
     */
    public record Applied(int added, int removed, String summary) {
    }

    /** What the tracker does in a run, for one book. Every method runs on the worker thread in the request context of the user. */
    public interface Steps<P extends Proposal> {
        /** Whether the book reads with the model (else only what costs nothing is noted). */
        boolean readsWithModel() throws Exception;

        /** N, the parts of one request. */
        int partsPerRun() throws Exception;

        /**
         * Reads the settled parts without the model.
         *
         * @return how many parts were read
         */
        int readWithoutModel(String excludeFrom, int maxParts) throws Exception;

        /** Asks the model about the next parts; a proposal without parts when nothing is waiting. */
        P propose(int maxParts, String excludeFrom, Live live) throws Exception;

        /** Applies the proposal (with the choices the user made in it) and moves the cursor over its parts. */
        Applied commit(P proposal, boolean logged) throws Exception;

        /** Asks the model which values to tidy; an empty proposal (not asked) when there is nothing to tidy. */
        P proposeTidy(Live live) throws Exception;

        /** Applies a reviewed tidy. */
        Applied commitTidy(P proposal, boolean logged) throws Exception;

        /** Whether the automatic tidy is due at the end of a run that was not guided. */
        boolean tidyDue(boolean guided) throws Exception;

        /** A tidy found nothing: the parts are counted from here. */
        void markTidied() throws Exception;

        /** The log line of a tidy that changed nothing (asked: the model found nothing; else: nothing to tidy). */
        String nothingToTidy(boolean asked);
    }

    /**
     * What the user sees of the run.
     *
     * @param run       the number of the run of the series, from 1
     * @param partsRead how many parts the series has read so far
     * @param added     the first count so far (see {@link Applied})
     * @param removed   the second count so far
     * @param proposal  the proposal that waits for review (phase {@link Phase#REVIEW}), else {@code null}
     * @param message   the error (phase {@link Phase#FAILED}) or a note on the run
     * @param log       what was applied, one line per run
     */
    public record Status<P extends Proposal>(Phase phase, int run, int partsRead, int added, int removed, P proposal, String message,
                                             List<String> log) {
    }

    /**
     * What the model thought and answered in one request of the run, so that it can be read also when it proposed nothing.
     *
     * @param run       the number of the request in the run, from 1
     * @param tidy      whether it was a tidy request
     * @param reasoning what the model thought first, empty when the provider sends none
     * @param answer    what the model answered (JSON)
     * @param changes   how many changes it proposed after they were checked
     */
    public record Thought(int run, boolean tidy, String reasoning, String answer, int changes) {
    }

    private final Manuscript manuscript;
    private final Steps<P> steps;
    private final String source;
    private final Supplier<String> label;
    /**
     * The request attributes the worker runs in: those of the request that started the run, replaced by those of the request that
     * applies a reviewed proposal (the session of the first one may be over by then).
     */
    private volatile RequestAttributes attributes;
    private final boolean guided;
    private final String excludeFrom;
    /** The series ends after this many parts, 0 = no limit (all the parts that are waiting). */
    private final int maxParts;
    /** The run is the tidy pass: one request about the values, no cursor. */
    private final boolean tidy;
    private final List<Consumer<Status<P>>> listeners = new CopyOnWriteArrayList<>();
    /** Told that the live text changed (not a change of the status): it must stay cheap for the listener. */
    private final List<Runnable> liveListeners = new CopyOnWriteArrayList<>();
    private final List<String> lines = new ArrayList<>();
    private final List<Thought> thoughts = new CopyOnWriteArrayList<>();
    /** What the model writes now, shown while the request goes on; reset at every request. */
    private final StringBuilder liveReasoning = new StringBuilder();
    private final StringBuilder liveAnswer = new StringBuilder();
    private volatile long lastLiveNotify;
    private final Live live = new Live() {
        @Override
        public void started() {
            synchronized (liveReasoning) {
                liveReasoning.setLength(0);
                liveAnswer.setLength(0);
            }
        }

        @Override
        public void chunk(boolean reasoning, String piece) {
            synchronized (liveReasoning) {
                (reasoning ? liveReasoning : liveAnswer).append(piece);
            }
            // the panel is told about once a second, not for every word; the status listeners (which rebuild views) are not called
            long now = System.currentTimeMillis();
            if (now - lastLiveNotify >= 1000) {
                lastLiveNotify = now;
                notifyLiveListeners();
            }
        }
    };
    private final CompletableFuture<Void> finished = new CompletableFuture<>();

    private volatile Status<P> status;
    /** The run of the core's registry: the panel of the runs shows it (what the model writes, Stop), a delete of the book names it. */
    private volatile Runs.Run registryRun;
    private volatile Thread worker;
    private volatile boolean cancelled;
    private volatile CompletableFuture<Boolean> decision;

    /**
     * Created on the thread of the user's request (the services of the worker need its attributes).
     *
     * @param source      the source of the run in the core's registry (the plugin)
     * @param label       the label of the run in the panel of the runs
     * @param guided      whether the proposals wait for the user (false: they are applied at once)
     * @param excludeFrom a part that is being replaced, it and the later ones are left alone; may be {@code null}
     * @param maxParts    the series reads at most this many parts, 0 = all that are waiting
     * @param tidy        the run is the tidy pass instead of reading parts
     */
    public TrackerRun(Manuscript manuscript, Steps<P> steps, String source, Supplier<String> label, boolean guided, String excludeFrom,
                      int maxParts, boolean tidy) {
        this.manuscript = manuscript;
        this.steps = steps;
        this.source = source;
        this.label = label;
        this.guided = guided;
        this.excludeFrom = excludeFrom;
        this.maxParts = Math.max(0, maxParts);
        this.tidy = tidy;
        this.attributes = ThreadCopyRequestAttributes.create();
        this.status = new Status<>(Phase.PREPARING, 0, 0, 0, 0, null, "", List.of());
    }

    public Status<P> getStatus() {
        return status;
    }

    /** Whether the run is the tidy pass. */
    public boolean isTidy() {
        return tidy;
    }

    /** The reasoning and the answer of every request of the run, oldest first (the last 30). */
    public List<Thought> getThoughts() {
        return List.copyOf(thoughts);
    }

    private void remember(int run, boolean tidyRequest, P proposal) {
        if (!proposal.asked()) {
            return;
        }
        thoughts.add(new Thought(run, tidyRequest, proposal.reasoning(), proposal.raw(), proposal.changeCount()));
        while (thoughts.size() > 30) {
            thoughts.removeFirst();
        }
    }

    /** What the model has written so far in the request that goes on: {@code [reasoning, answer]}, empty strings when none. */
    public String[] getLive() {
        synchronized (liveReasoning) {
            return new String[]{liveReasoning.toString(), liveAnswer.toString()};
        }
    }

    public boolean isGuided() {
        return guided;
    }

    /** Called with every change of the status, from the worker thread (or from the thread that reviews). */
    public void addListener(Consumer<Status<P>> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<Status<P>> listener) {
        listeners.remove(listener);
    }

    /** Called (from the worker thread, about once a second) while the model writes, see {@link #getLive()}. */
    public void addLiveListener(Runnable listener) {
        liveListeners.add(listener);
    }

    public void removeLiveListener(Runnable listener) {
        liveListeners.remove(listener);
    }

    /** Starts the worker; called on the thread of the user's request (the page and the user of the registry's run). */
    void start() {
        registryRun = Runs.run(source, label).book(manuscript).quiet().currentPage().onStop(this::cancel).start();
        worker = Thread.ofVirtual().name(source + "-run").start(this::work);
    }

    /** The run of the registry ({@code Runs}) this run is, null before it started. */
    public Runs.Run getRegistryRun() {
        return registryRun;
    }

    /** The book the run reads. */
    public Manuscript getManuscript() {
        return manuscript;
    }

    /** What a click on the run's entry in the panel of the runs does, in the page of the click; null for nothing. */
    public void setOnOpen(Runnable open) {
        Runs.Run registered = registryRun;
        if (registered != null) {
            registered.setOnOpen(open);
        }
    }

    /** The user reviewed the proposal (the changes carry their choices): apply it. */
    public void apply() {
        CompletableFuture<Boolean> waiting = decision;
        if (waiting != null) {
            RequestAttributes now = ThreadCopyRequestAttributes.create();
            if (now != null) {
                attributes = now;
            }
            waiting.complete(true);
        }
    }

    /** Stops the series, in whatever phase it is. */
    public void cancel() {
        cancelled = true;
        CompletableFuture<Boolean> waiting = decision;
        if (waiting != null) {
            waiting.complete(false);
        }
        Thread thread = worker;
        if (thread != null && !status.phase().isFinished()) {
            thread.interrupt();
        }
    }

    /** Completes when the run has ended, in any way. */
    public CompletableFuture<Void> whenFinished() {
        return finished;
    }

    // --- the work ---

    private void work() {
        try (InRequestScope _ = new InRequestScope(attributes); Runs.Binding _ = registryRun.bind()) {
            if (tidy) {
                tidyWithModel();
            } else if (!steps.readsWithModel()) {
                readWithoutModel();
            } else {
                readWithModel();
            }
        } catch (CancellationException e) {
            publish(Phase.CANCELLED, status.run(), null, "");
        } catch (Exception e) {
            if (cancelled) {
                publish(Phase.CANCELLED, status.run(), null, "");
            } else {
                log.warn("The processing of the story failed: {}", e.getMessage(), e);
                publish(Phase.FAILED, status.run(), null, String.valueOf(e.getMessage()));
            }
        } finally {
            Phase phase = status.phase();
            registryRun.end(phase == Phase.DONE ? Runs.Outcome.FINISHED : phase == Phase.FAILED ? Runs.Outcome.FAILED : Runs.Outcome.STOPPED);
            finished.complete(null);
        }
    }

    /** No model: only what costs nothing is noted. */
    private void readWithoutModel() throws Exception {
        publish(Phase.APPLYING, 1, null, "");
        int read = steps.readWithoutModel(excludeFrom, maxParts > 0 ? maxParts : Integer.MAX_VALUE);
        status = new Status<>(Phase.DONE, 1, read, 0, 0, null, "", List.copyOf(lines));
        notifyListeners();
    }

    private void readWithModel() throws Exception {
        int partsRead = 0;
        int added = 0;
        int removed = 0;
        int run = 0;
        status = new Status<>(Phase.PREPARING, 0, 0, 0, 0, null, "", List.of());
        notifyListeners();
        int partsPerRun = steps.partsPerRun();
        while (!cancelled) {
            run++;
            publish(Phase.ASKING, run, null, "", partsRead, added, removed);
            int perRun = maxParts > 0 ? Math.min(partsPerRun, maxParts - partsRead) : partsPerRun;
            P proposal = steps.propose(perRun, excludeFrom, live);
            remember(run, false, proposal);
            if (proposal.isEmpty()) {
                break;
            }
            if (guided && proposal.asked() && proposal.changeCount() > 0) {
                waitForReview(run, proposal, partsRead, added, removed);
            }
            publish(Phase.APPLYING, run, null, proposal.note(), partsRead, added, removed);
            Applied result = steps.commit(proposal, !guided);
            partsRead += proposal.parts().size();
            added += result.added();
            removed += result.removed();
            lines.add("Run " + run + ": " + proposal.parts().size() + " part(s), " + result.summary()
                    + (proposal.dropped() == 0 ? "" : ", " + proposal.dropped() + " unusable answers dropped")
                    + (proposal.note().isBlank() ? "" : " - " + proposal.note()));
            if (proposal.pending() == 0 || (maxParts > 0 && partsRead >= maxParts)) {
                break;
            }
        }
        if (!cancelled) {
            tidyIfDue();
        }
        status = new Status<>(cancelled ? Phase.CANCELLED : Phase.DONE, run, partsRead, added, removed, null, "", List.copyOf(lines));
        notifyListeners();
    }

    /** Waits until the user applies (go on) or cancels (stop the series) the review of the proposal. */
    private void waitForReview(int run, P proposal, int partsRead, int added, int removed) {
        decision = new CompletableFuture<>();
        publish(Phase.REVIEW, run, proposal, proposal.note(), partsRead, added, removed);
        boolean go;
        try {
            go = decision.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CancellationException();
        } catch (Exception e) {
            throw new CancellationException();
        } finally {
            decision = null;
        }
        if (!go) {
            throw new CancellationException();
        }
        // the worker goes on in the session of the one who applied the review, not in the one that may be gone
        RequestContextHolder.setRequestAttributes(attributes);
    }

    /**
     * The automatic tidy at the end of a run that applied its changes without asking, when it is due. What it finds is applied and
     * logged; a failure only goes into the log of the run, the reading is done.
     */
    private void tidyIfDue() {
        try {
            if (!steps.tidyDue(guided)) {
                return;
            }
            publish(Phase.ASKING, status.run(), null, "");
            P proposal = steps.proposeTidy(live);
            remember(status.run(), true, proposal);
            if (proposal.changeCount() == 0) {
                steps.markTidied();
                lines.add("Tidy: " + steps.nothingToTidy(proposal.asked()));
                return;
            }
            Applied result = steps.commitTidy(proposal, true);
            lines.add("Tidy: " + result.summary());
        } catch (CancellationException e) {
            throw e;
        } catch (Exception e) {
            if (cancelled) {
                throw new CancellationException();
            }
            log.warn("The automatic tidy failed: {}", e.getMessage(), e);
            lines.add("Tidy failed: " + e.getMessage());
        }
    }

    /** The tidy pass: one request about the values; the changes are reviewed (guided) or applied and logged. */
    private void tidyWithModel() throws Exception {
        status = new Status<>(Phase.PREPARING, 0, 0, 0, 0, null, "", List.of());
        notifyListeners();
        publish(Phase.ASKING, 1, null, "");
        P proposal = steps.proposeTidy(live);
        remember(1, true, proposal);
        if (proposal.changeCount() == 0) {
            lines.add("Tidy: " + steps.nothingToTidy(proposal.asked()));
            status = new Status<>(Phase.DONE, 1, 0, 0, 0, null, "Tidy: nothing to do", List.copyOf(lines));
            notifyListeners();
            return;
        }
        if (guided) {
            waitForReview(1, proposal, 0, 0, 0);
        }
        publish(Phase.APPLYING, 1, null, proposal.note());
        Applied result = steps.commitTidy(proposal, !guided);
        lines.add("Tidy: " + result.summary() + (proposal.dropped() == 0 ? "" : ", " + proposal.dropped() + " unusable answers dropped"));
        status = new Status<>(Phase.DONE, 1, 0, result.added(), result.removed(), null, "Tidy: " + result.summary(), List.copyOf(lines));
        notifyListeners();
    }

    private void publish(Phase phase, int run, P proposal, String message) {
        publish(phase, run, proposal, message, status.partsRead(), status.added(), status.removed());
    }

    private void publish(Phase phase, int run, P proposal, String message, int partsRead, int added, int removed) {
        status = new Status<>(phase, run, partsRead, added, removed, proposal, message, List.copyOf(lines));
        Runs.Run registered = registryRun;
        if (registered != null) {
            registered.setWaitingForUser(phase == Phase.REVIEW);
        }
        notifyListeners();
    }

    private void notifyLiveListeners() {
        for (Runnable listener : liveListeners) {
            try {
                listener.run();
            } catch (RuntimeException e) {
                log.debug("A live listener of a run failed: {}", e.getMessage());
            }
        }
    }

    private void notifyListeners() {
        for (Consumer<Status<P>> listener : listeners) {
            try {
                listener.accept(status);
            } catch (RuntimeException e) {
                log.debug("A listener of a run failed: {}", e.getMessage());
            }
        }
    }
}
