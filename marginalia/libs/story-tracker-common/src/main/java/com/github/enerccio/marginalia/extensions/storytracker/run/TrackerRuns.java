package com.github.enerccio.marginalia.extensions.storytracker.run;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The runs of the books of one tracker: one at a time for a book. The last run is kept (finished or not) so that a tab that is
 * opened later can show it and its log. Also keeps a proposal a command returned for an agent to review.
 *
 * @param <P> the proposal of the tracker
 */
public final class TrackerRuns<P extends TrackerRun.Proposal> {

    /** What the registry needs of the tracker. */
    public interface Factory<P extends TrackerRun.Proposal> {
        /** The steps of a run of the book. */
        TrackerRun.Steps<P> steps(Manuscript manuscript);

        /** Whether the book asks before applying (a run that is not told otherwise is guided then). */
        boolean askBeforeApplying(Manuscript manuscript) throws Exception;
    }

    private final Factory<P> factory;
    private final String source;
    private final Supplier<String> label;
    private final Map<String, TrackerRun<P>> runs = new ConcurrentHashMap<>();
    private final Map<String, P> held = new ConcurrentHashMap<>();
    /** Shows a run in the page of the click on its entry in the panel of the runs; set by the extension while it is loaded. */
    private volatile Consumer<TrackerRun<P>> opener;

    /**
     * @param source the source of the runs in the core's registry (the plugin)
     * @param label  the label of a run in the panel of the runs
     */
    public TrackerRuns(Factory<P> factory, String source, Supplier<String> label) {
        this.factory = factory;
        this.source = source;
        this.label = label;
    }

    /** What a click on a run's entry in the panel of the runs does (in the page of the click); null for nothing. */
    public void setOpener(Consumer<TrackerRun<P>> opener) {
        this.opener = opener;
    }

    /** The latest run of the book, or {@code null}. */
    public TrackerRun<P> latest(Manuscript manuscript) {
        return runs.get(manuscript.getUuid());
    }

    /** The run of the book that is going on, or {@code null}. */
    public TrackerRun<P> running(Manuscript manuscript) {
        TrackerRun<P> run = latest(manuscript);
        return run != null && !run.getStatus().phase().isFinished() ? run : null;
    }

    /**
     * Starts a series of runs on a worker thread; call it on the thread of the user's request.
     *
     * @param guided      whether the proposals are reviewed by the user; {@code null} = the setting of the book
     * @param excludeFrom a part that is being replaced (a regenerate), it and the later ones are left alone; may be {@code null}
     * @throws IllegalStateException when the book has a run going on already
     */
    public TrackerRun<P> start(Manuscript manuscript, Boolean guided, String excludeFrom) throws Exception {
        return start(manuscript, guided, excludeFrom, 0);
    }

    /**
     * @param maxParts the series reads at most this many parts, 0 = all that are waiting
     */
    public TrackerRun<P> start(Manuscript manuscript, Boolean guided, String excludeFrom, int maxParts) throws Exception {
        return start(manuscript, guided, excludeFrom, maxParts, false);
    }

    /**
     * Starts the tidy pass (one request, no cursor) on a worker thread; call it on the thread of the user's request.
     *
     * @param guided whether the proposal is reviewed by the user; {@code null} = the setting of the book
     * @throws IllegalStateException when the book has a run going on already
     */
    public TrackerRun<P> startTidy(Manuscript manuscript, Boolean guided) throws Exception {
        return start(manuscript, guided, null, 0, true);
    }

    private synchronized TrackerRun<P> start(Manuscript manuscript, Boolean guided, String excludeFrom, int maxParts, boolean tidy)
            throws Exception {
        if (running(manuscript) != null) {
            throw new IllegalStateException("The book is being processed already");
        }
        TrackerRun<P> run = new TrackerRun<>(manuscript, factory.steps(manuscript), source, label,
                guided != null ? guided : factory.askBeforeApplying(manuscript), excludeFrom, maxParts, tidy);
        runs.put(manuscript.getUuid(), run);
        run.start();
        Consumer<TrackerRun<P>> open = opener;
        if (open != null) {
            run.setOnOpen(() -> open.accept(run));
        }
        return run;
    }

    /** Keeps a proposal that a command returned for review by an agent, until {@link #takeHeld} or a newer one. */
    public void hold(Manuscript manuscript, P proposal) {
        held.put(manuscript.getUuid(), proposal);
    }

    /** The proposal that was held for the book, removed; {@code null} when none. */
    public P takeHeld(Manuscript manuscript) {
        return held.remove(manuscript.getUuid());
    }

    /** The proposal that is held for the book, left in place; {@code null} when none. */
    public P peekHeld(Manuscript manuscript) {
        return held.get(manuscript.getUuid());
    }
}
