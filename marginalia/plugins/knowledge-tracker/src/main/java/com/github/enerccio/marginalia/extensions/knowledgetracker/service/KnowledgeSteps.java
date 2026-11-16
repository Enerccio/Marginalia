package com.github.enerccio.marginalia.extensions.knowledgetracker.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ChangeApplier;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.TidyTrigger;
import com.github.enerccio.marginalia.extensions.knowledgetracker.service.ExtractionService.Proposal;
import com.github.enerccio.marginalia.extensions.storytracker.llm.Live;
import com.github.enerccio.marginalia.extensions.storytracker.run.TrackerRun;
import com.github.enerccio.marginalia.extensions.storytracker.run.TrackerRuns;

import static com.github.enerccio.marginalia.extensions.knowledgetracker.KnowledgeTrackerLocalization.KNOWLEDGE_TRACKER_READ_THE_STORY;
import static com.github.enerccio.marginalia.extensions.knowledgetracker.KnowledgeTrackerLocalization.T;

/**
 * What a run of the Knowledge Tracker does for one book (see {@link TrackerRun}): it reads with the model what the characters learned,
 * or without it only who has met whom, and tidies the items the model learned.
 */
public final class KnowledgeSteps implements TrackerRun.Steps<Proposal> {

    /** The source of its runs in the core's registry. */
    public static final String SOURCE = "knowledge-tracker";

    private final Manuscript manuscript;
    private final ExtractionService extraction;
    private final KnowledgeProcessService processService;
    private final KnowledgeStateService stateService;

    public KnowledgeSteps(Manuscript manuscript, ExtractionService extraction, KnowledgeProcessService processService,
                          KnowledgeStateService stateService) {
        this.manuscript = manuscript;
        this.extraction = extraction;
        this.processService = processService;
        this.stateService = stateService;
    }

    /** The runs of the books of the Knowledge Tracker. */
    public static TrackerRuns<Proposal> runs(ExtractionService extraction, KnowledgeProcessService processService,
                                             KnowledgeStateService stateService) {
        return new TrackerRuns<>(new TrackerRuns.Factory<>() {
            @Override
            public TrackerRun.Steps<Proposal> steps(Manuscript manuscript) {
                return new KnowledgeSteps(manuscript, extraction, processService, stateService);
            }

            @Override
            public boolean askBeforeApplying(Manuscript manuscript) throws Exception {
                return stateService.load(manuscript).getSettings().isAskBeforeApplying();
            }
        }, SOURCE, () -> T.localized(KNOWLEDGE_TRACKER_READ_THE_STORY));
    }

    @Override
    public boolean readsWithModel() throws Exception {
        return stateService.load(manuscript).getSettings().isExtractWithModel();
    }

    @Override
    public int partsPerRun() throws Exception {
        return stateService.load(manuscript).getSettings().getPartsPerRun();
    }

    /** No model: only who has met whom is noted, which costs nothing. */
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
        ChangeApplier.Result result = extraction.commit(manuscript, proposal, logged);
        return new TrackerRun.Applied(result.added(), result.retracted(), result.added() + " added, " + result.retracted() + " retracted"
                + (result.rejected() > 0 ? ", " + result.rejected() + " rejected (kept as old knowledge)" : ""));
    }

    @Override
    public Proposal proposeTidy(Live live) throws Exception {
        return extraction.proposeTidy(manuscript, live);
    }

    @Override
    public TrackerRun.Applied commitTidy(Proposal proposal, boolean logged) throws Exception {
        ChangeApplier.Result result = extraction.commitTidy(manuscript, proposal, logged);
        return new TrackerRun.Applied(result.added(), result.retracted(),
                result.retracted() + " item(s) became old knowledge, " + result.added() + " merged item(s) added");
    }

    @Override
    public boolean tidyDue(boolean guided) throws Exception {
        return TidyTrigger.due(stateService.load(manuscript), guided);
    }

    @Override
    public void markTidied() throws Exception {
        extraction.markTidied(manuscript);
    }

    @Override
    public String nothingToTidy(boolean asked) {
        return asked ? "nothing to drop or merge" : "nothing to tidy (no character has enough items that the model learned)";
    }
}
