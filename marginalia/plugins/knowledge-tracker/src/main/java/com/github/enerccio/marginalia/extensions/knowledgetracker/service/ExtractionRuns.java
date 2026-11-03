package com.github.enerccio.marginalia.extensions.knowledgetracker.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.BookSettings;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The runs of the books: one at a time for a book. The last run is kept (finished or not) so that a tab that is opened
 * later can show it and its log.
 */
public final class ExtractionRuns {

    private final ExtractionService extraction;
    private final KnowledgeProcessService processService;
    private final KnowledgeStateService stateService;
    private final Map<String, ExtractionRun> runs = new ConcurrentHashMap<>();
    private final Map<String, ExtractionService.Proposal> held = new ConcurrentHashMap<>();

    public ExtractionRuns(ExtractionService extraction, KnowledgeProcessService processService, KnowledgeStateService stateService) {
        this.extraction = extraction;
        this.processService = processService;
        this.stateService = stateService;
    }

    /** The latest run of the book, or {@code null}. */
    public ExtractionRun latest(Manuscript manuscript) {
        return runs.get(manuscript.getUuid());
    }

    /** The run of the book that is going on, or {@code null}. */
    public ExtractionRun running(Manuscript manuscript) {
        ExtractionRun run = latest(manuscript);
        return run != null && !run.getStatus().phase().isFinished() ? run : null;
    }

    /**
     * Starts a series of runs on a worker thread; call it on the thread of the user's request.
     *
     * @param guided whether the proposals are reviewed by the user (false: applied at once and logged); {@code null} = the
     *               setting of the book
     * @param excludeFrom a part that is being replaced (a regenerate), it and the later ones are left alone; may be {@code null}
     * @throws IllegalStateException when the book has a run going on already
     */
    public synchronized ExtractionRun start(Manuscript manuscript, Boolean guided, String excludeFrom) throws Exception {
        if (running(manuscript) != null) {
            throw new IllegalStateException("The book is being processed already");
        }
        BookSettings settings = stateService.load(manuscript).getSettings();
        ExtractionRun run = new ExtractionRun(manuscript, extraction, processService, stateService,
                guided != null ? guided : settings.isAskBeforeApplying(), excludeFrom);
        runs.put(manuscript.getUuid(), run);
        run.start();
        return run;
    }

    /** Keeps a proposal that a command returned for review by an agent, until {@link #takeHeld} or a newer one. */
    public void hold(Manuscript manuscript, ExtractionService.Proposal proposal) {
        held.put(manuscript.getUuid(), proposal);
    }

    /** The proposal that was held for the book, removed; {@code null} when none. */
    public ExtractionService.Proposal takeHeld(Manuscript manuscript) {
        return held.remove(manuscript.getUuid());
    }

    /** The proposal that is held for the book, left in place; {@code null} when none. */
    public ExtractionService.Proposal peekHeld(Manuscript manuscript) {
        return held.get(manuscript.getUuid());
    }
}
