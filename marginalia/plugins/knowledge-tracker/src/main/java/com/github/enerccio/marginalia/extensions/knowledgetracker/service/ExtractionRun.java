package com.github.enerccio.marginalia.extensions.knowledgetracker.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ChangeApplier;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.BookSettings;
import com.github.enerccio.marginalia.extensions.knowledgetracker.process.BranchProcessor.Report;
import com.github.enerccio.marginalia.extensions.knowledgetracker.service.ExtractionService.Proposal;
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

/**
 * One processing run of a book on a worker thread: reads the next parts (a series of runs of N parts), asks the model what
 * the tracked characters learned, and either waits for the user to review the proposal (guided) or applies it at once
 * (automatic, with a log). The user can keep working meanwhile; a review is waited for, never forced on the user.
 * <p>
 * Cancel stops the series: what was applied stays, what was proposed and not applied is dropped and proposed again next time.
 */
public final class ExtractionRun {

    private static final Logger log = LoggerFactory.getLogger(ExtractionRun.class);

    public enum Phase {
        PREPARING, ASKING, REVIEW, APPLYING, DONE, FAILED, CANCELLED;

        public boolean isFinished() {
            return this == DONE || this == FAILED || this == CANCELLED;
        }
    }

    /**
     * What the user sees of the run.
     *
     * @param run      the number of the run of the series, from 1
     * @param partsRead how many parts the series has read so far
     * @param added    items added so far
     * @param retracted items retracted so far
     * @param proposal  the proposal that waits for review (phase {@link Phase#REVIEW}), else {@code null}
     * @param message   the error (phase {@link Phase#FAILED}) or a note on the run
     * @param log       what was applied, one line per run
     */
    public record Status(Phase phase, int run, int partsRead, int added, int retracted, Proposal proposal, String message,
                         List<String> log) {
    }

    private final Manuscript manuscript;
    private final ExtractionService extraction;
    private final KnowledgeProcessService processService;
    private final KnowledgeStateService stateService;
    /**
     * The request attributes the worker runs in: those of the request that started the run, replaced by those of the request that
     * applies a reviewed proposal (the session of the first one may be over by then: the tab was closed while the review waited).
     */
    private volatile RequestAttributes attributes;
    private final boolean guided;
    private final String excludeFrom;
    /** The series ends after this many parts, 0 = no limit (all the parts that are waiting). */
    private final int maxParts;
    private final List<Consumer<Status>> listeners = new CopyOnWriteArrayList<>();
    private final List<String> lines = new ArrayList<>();
    private final CompletableFuture<Void> finished = new CompletableFuture<>();

    private volatile Status status;
    private volatile Thread worker;
    private volatile boolean cancelled;
    private volatile CompletableFuture<Boolean> decision;

    /**
     * Created on the thread of the user's request (the services of the worker need its attributes).
     *
     * @param guided      whether the proposals wait for the user (false: they are applied at once)
     * @param excludeFrom a part that is being replaced, it and the later ones are left alone; may be {@code null}
     * @param maxParts    the series reads at most this many parts, 0 = all that are waiting
     */
    ExtractionRun(Manuscript manuscript, ExtractionService extraction, KnowledgeProcessService processService,
                  KnowledgeStateService stateService, boolean guided, String excludeFrom, int maxParts) {
        this.manuscript = manuscript;
        this.extraction = extraction;
        this.processService = processService;
        this.stateService = stateService;
        this.guided = guided;
        this.excludeFrom = excludeFrom;
        this.maxParts = Math.max(0, maxParts);
        this.attributes = ThreadCopyRequestAttributes.create();
        this.status = new Status(Phase.PREPARING, 0, 0, 0, 0, null, "", List.of());
    }

    public Status getStatus() {
        return status;
    }

    public boolean isGuided() {
        return guided;
    }

    /** Called with every change of the status, from the worker thread (or from the thread that reviews). */
    public void addListener(Consumer<Status> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<Status> listener) {
        listeners.remove(listener);
    }

    void start() {
        worker = Thread.ofVirtual().name("knowledge-tracker-run").start(this::work);
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
        try (InRequestScope _ = new InRequestScope(attributes)) {
            BookSettings settings = stateService.load(manuscript).getSettings();
            if (!settings.isExtractWithModel()) {
                readWithoutModel();
            } else {
                readWithModel(settings);
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
            finished.complete(null);
        }
    }

    /** No model: only who has met whom is noted, which costs nothing. */
    private void readWithoutModel() throws Exception {
        publish(Phase.APPLYING, 1, null, "");
        Report report = processService.process(manuscript, true, excludeFrom, maxParts > 0 ? maxParts : KnowledgeProcessService.NO_LIMIT);
        status = new Status(Phase.DONE, 1, report.processed(), 0, 0, null, "", List.copyOf(lines));
        notifyListeners();
    }

    private void readWithModel(BookSettings settings) throws Exception {
        int partsRead = 0;
        int added = 0;
        int retracted = 0;
        int run = 0;
        status = new Status(Phase.PREPARING, 0, 0, 0, 0, null, "", List.of());
        notifyListeners();
        while (!cancelled) {
            run++;
            publish(Phase.ASKING, run, null, "", partsRead, added, retracted);
            int perRun = maxParts > 0 ? Math.min(settings.getPartsPerRun(), maxParts - partsRead) : settings.getPartsPerRun();
            Proposal proposal = extraction.propose(manuscript, perRun, true, excludeFrom);
            if (proposal.isEmpty()) {
                break;
            }
            boolean needsReview = guided && proposal.asked() && !proposal.changes().isEmpty();
            if (needsReview) {
                decision = new CompletableFuture<>();
                publish(Phase.REVIEW, run, proposal, proposal.note(), partsRead, added, retracted);
                boolean go;
                try {
                    go = decision.get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new CancellationException();
                } finally {
                    decision = null;
                }
                if (!go) {
                    throw new CancellationException();
                }
            }
            // the worker goes on in the session of the one who applied the review, not in the one that may be gone
            RequestContextHolder.setRequestAttributes(attributes);
            publish(Phase.APPLYING, run, null, proposal.note(), partsRead, added, retracted);
            ChangeApplier.Result result = extraction.commit(manuscript, proposal, !guided);
            partsRead += proposal.parts().size();
            added += result.added();
            retracted += result.retracted();
            lines.add("Run " + run + ": " + proposal.parts().size() + " part(s), " + result.added() + " added, "
                    + result.retracted() + " retracted" + (result.rejected() > 0 ? ", " + result.rejected() + " rejected (kept as old knowledge)" : "")
                    + (proposal.changes().dropped().isEmpty() ? "" : ", " + proposal.changes().dropped().size() + " unusable answers dropped")
                    + (proposal.note().isBlank() ? "" : " - " + proposal.note()));
            if (proposal.pending() == 0 || (maxParts > 0 && partsRead >= maxParts)) {
                break;
            }
        }
        status = new Status(cancelled ? Phase.CANCELLED : Phase.DONE, run, partsRead, added, retracted, null, "", List.copyOf(lines));
        notifyListeners();
    }

    private void publish(Phase phase, int run, Proposal proposal, String message) {
        publish(phase, run, proposal, message, status.partsRead(), status.added(), status.retracted());
    }

    private void publish(Phase phase, int run, Proposal proposal, String message, int partsRead, int added, int retracted) {
        status = new Status(phase, run, partsRead, added, retracted, proposal, message, List.copyOf(lines));
        notifyListeners();
    }

    private void notifyListeners() {
        for (Consumer<Status> listener : listeners) {
            try {
                listener.accept(status);
            } catch (RuntimeException e) {
                log.debug("A listener of a run failed: {}", e.getMessage());
            }
        }
    }
}
