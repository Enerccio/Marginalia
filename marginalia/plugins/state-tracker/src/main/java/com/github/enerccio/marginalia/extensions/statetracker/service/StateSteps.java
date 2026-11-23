package com.github.enerccio.marginalia.extensions.statetracker.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.statetracker.extract.StateApplier;
import com.github.enerccio.marginalia.extensions.statetracker.service.StateExtractionService.Proposal;
import com.github.enerccio.marginalia.extensions.storytracker.llm.Live;
import com.github.enerccio.marginalia.extensions.storytracker.run.TrackerRun;
import com.github.enerccio.marginalia.extensions.storytracker.run.TrackerRuns;

import static com.github.enerccio.marginalia.extensions.statetracker.StateTrackerLocalization.READ_THE_STORY_FOR_THE_STATE;
import static com.github.enerccio.marginalia.extensions.statetracker.StateTrackerLocalization.T;

/**
 * What a run of the State Tracker does for one book (see {@link TrackerRun}): it asks the model what changed in the state of the
 * tracked characters in the next parts, and the tidy asks which current values faded.
 */
public final class StateSteps implements TrackerRun.Steps<Proposal> {

    private final Manuscript manuscript;
    private final StateExtractionService extraction;
    private final StateProcessService processService;
    private final StateService stateService;

    public StateSteps(Manuscript manuscript, StateExtractionService extraction, StateProcessService processService, StateService stateService) {
        this.manuscript = manuscript;
        this.extraction = extraction;
        this.processService = processService;
        this.stateService = stateService;
    }

    /** The runs of the books of the State Tracker. */
    public static TrackerRuns<Proposal> runs(StateExtractionService extraction, StateProcessService processService, StateService stateService) {
        return new TrackerRuns<>(new TrackerRuns.Factory<>() {
            @Override
            public TrackerRun.Steps<Proposal> steps(Manuscript manuscript) {
                return new StateSteps(manuscript, extraction, processService, stateService);
            }

            @Override
            public boolean askBeforeApplying(Manuscript manuscript) throws Exception {
                return stateService.load(manuscript).getSettings().isAskBeforeApplying();
            }
        }, StateExtractionService.SOURCE, () -> T.localized(READ_THE_STORY_FOR_THE_STATE));
    }

    /** The State Tracker always reads with the model: there is nothing to note without it. */
    @Override
    public boolean readsWithModel() {
        return true;
    }

    @Override
    public int partsPerRun() throws Exception {
        return stateService.load(manuscript).getSettings().getPartsPerRun();
    }

    @Override
    public int readWithoutModel(String excludeFrom, int maxParts) throws Exception {
        return processService.process(manuscript, true, excludeFrom, maxParts).processed();
    }

    @Override
    public Proposal propose(int maxParts, String excludeFrom, Live live) throws Exception {
        return extraction.propose(manuscript, maxParts, true, excludeFrom, live);
    }

    @Override
    public TrackerRun.Applied commit(Proposal proposal, boolean logged) throws Exception {
        StateApplier.Result result = extraction.commit(manuscript, proposal, logged);
        return new TrackerRun.Applied(result.applied(), result.rejected(), result.applied() + " applied"
                + (result.rejected() > 0 ? ", " + result.rejected() + " rejected" : "")
                + (result.skipped() > 0 ? ", " + result.skipped() + " skipped (the state changed meanwhile)" : ""));
    }

    @Override
    public Proposal proposeTidy(Live live) throws Exception {
        return extraction.proposeTidy(manuscript, live);
    }

    @Override
    public TrackerRun.Applied commitTidy(Proposal proposal, boolean logged) throws Exception {
        StateApplier.Result result = extraction.commitTidy(manuscript, proposal, logged);
        return new TrackerRun.Applied(result.applied(), result.rejected(), result.applied() + " value(s) tidied"
                + (result.rejected() > 0 ? ", " + result.rejected() + " kept" : "")
                + (result.skipped() > 0 ? ", " + result.skipped() + " skipped (the state changed meanwhile)" : ""));
    }

    @Override
    public boolean tidyDue(boolean guided) throws Exception {
        return StateExtractionService.tidyDue(stateService.load(manuscript), guided);
    }

    @Override
    public void markTidied() throws Exception {
        extraction.markTidied(manuscript);
    }

    @Override
    public String nothingToTidy(boolean asked) {
        return asked ? "nothing to drop, update or merge" : "nothing to tidy (no tracked character with a value is in the last parts)";
    }
}
