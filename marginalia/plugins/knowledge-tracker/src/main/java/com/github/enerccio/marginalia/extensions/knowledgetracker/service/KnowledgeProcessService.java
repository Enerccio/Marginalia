package com.github.enerccio.marginalia.extensions.knowledgetracker.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.extensions.PartTextRegistry;
import com.github.enerccio.marginalia.extensions.knowledgetracker.block.KnowledgeBlockBuilder;
import com.github.enerccio.marginalia.extensions.knowledgetracker.extract.ChangeLog;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.storytracker.checkpoint.Checkpoint;
import com.github.enerccio.marginalia.extensions.storytracker.checkpoint.CheckpointFiles;
import com.github.enerccio.marginalia.extensions.storytracker.checkpoint.ResourceCheckpointFiles;
import com.github.enerccio.marginalia.extensions.storytracker.checkpoint.TrackerCheckpoints;
import com.github.enerccio.marginalia.extensions.storytracker.log.TrackerChangeLog;
import com.github.enerccio.marginalia.extensions.storytracker.process.BranchProcessor;
import com.github.enerccio.marginalia.extensions.storytracker.process.BranchProcessor.Report;
import com.github.enerccio.marginalia.extensions.storytracker.process.Part;
import com.github.enerccio.marginalia.extensions.storytracker.process.RunEstimate;
import com.github.enerccio.marginalia.extensions.storytracker.process.StoryBranch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runs the processor on the active branch of a book: loads the branch and the state, stores the result and tells the
 * open tabs. One run per book at a time (a generation waits for a run that is going on, it does not start another).
 */
@Configurable
public class KnowledgeProcessService {

    /** The most parts a call reads when the caller does not limit it. */
    public static final int NO_LIMIT = Integer.MAX_VALUE;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private PartTextRegistry partTexts;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ResourceService resourceService;

    private final KnowledgeStateService stateService;
    private final TrackerCheckpoints checkpoints = new TrackerCheckpoints(KnowledgeSteps.SOURCE);
    private final Map<String, Object> locks = new ConcurrentHashMap<>();

    public KnowledgeProcessService(KnowledgeStateService stateService) {
        this.stateService = stateService;
    }

    /**
     * What the user sees of the processing.
     *
     * @param parts     how many parts the active branch has
     * @param cursor    the position (the first part is 1) of the last processed part, 0 when none
     * @param changed   the uuids of the processed parts whose text changed since
     * @param unread    how many parts after the cursor are not processed yet
     */
    public record Status(int parts, int cursor, List<String> changed, int unread) {
    }

    /**
     * The parts of the active branch of the book as it is stored now, the first part of the story first. Empty when the
     * book has no part.
     */
    public List<Part> branch(Manuscript manuscript) throws Exception {
        return StoryBranch.read(manuscript, manuscriptService, chatMessageService, partTexts);
    }

    /**
     * Reads the settled parts after the cursor.
     *
     * @param lastSettled whether the last part of the branch counts as settled (a new generation starts, or the user asks)
     * @param excludeFrom the uuid of a part that is being replaced, it and the later parts are left alone; may be {@code null}
     * @param max         the most parts to read, {@link #NO_LIMIT} for all
     */
    public Report process(Manuscript manuscript, boolean lastSettled, String excludeFrom, int max) throws Exception {
        synchronized (lock(manuscript)) {
            KnowledgeState state = stateService.load(manuscript);
            List<Part> branch = branch(manuscript);
            Report report = BranchProcessor.process(state, branch, BranchProcessor.settledCount(branch, lastSettled, excludeFrom),
                    separators(state), System.currentTimeMillis(), max);
            if (report.processed() > 0 || report.rewound() > 0) {
                stateService.save(manuscript, state);
                if (report.processed() > 0 && !state.getSettings().isExtractWithModel()) {
                    // a book that reads with the model takes its checkpoints after the runs of the model
                    checkpoint(manuscript, state, Checkpoint.Kind.READ, 0, 0);
                }
                stateService.changed(manuscript);
            }
            return report;
        }
    }

    /**
     * Takes back what came from the part and the parts after it and reads them again.
     *
     * @return the report of the reading, or {@code null} when the part is not on the active branch
     */
    public Report reprocessFrom(Manuscript manuscript, String partUuid, boolean lastSettled) throws Exception {
        synchronized (lock(manuscript)) {
            KnowledgeState state = stateService.load(manuscript);
            List<Part> branch = branch(manuscript);
            int dropped = BranchProcessor.reprocessFrom(state, branch, partUuid);
            if (dropped < 0) {
                return null;
            }
            Report report = BranchProcessor.process(state, branch, BranchProcessor.settledCount(branch, lastSettled, null),
                    separators(state), System.currentTimeMillis(), NO_LIMIT);
            stateService.save(manuscript, state);
            stateService.changed(manuscript);
            return new Report(dropped + report.rewound(), report.processed(), report.pending());
        }
    }

    /**
     * Takes back what came from the part and the parts after it, without reading them again (the model reads them: a part was
     * regenerated, or a reprocess when the book reads with the model).
     *
     * @return how many things were taken back, -1 when the part is not on the active branch
     */
    public int dropFrom(Manuscript manuscript, String partUuid) throws Exception {
        synchronized (lock(manuscript)) {
            KnowledgeState state = stateService.load(manuscript);
            int dropped = BranchProcessor.reprocessFrom(state, branch(manuscript), partUuid);
            if (dropped > 0) {
                stateService.save(manuscript, state);
                stateService.changed(manuscript);
            }
            return dropped;
        }
    }

    /**
     * Sets where processing begins; {@code null} = the start of the book. Moving it back makes the parts from the new
     * start unread again, moving it forward leaves what was read.
     */
    public void setStart(Manuscript manuscript, String partUuid) throws Exception {
        synchronized (lock(manuscript)) {
            KnowledgeState state = stateService.load(manuscript);
            state.setStartMessage(partUuid);
            stateService.save(manuscript, state);
            stateService.changed(manuscript);
        }
    }

    public Status status(Manuscript manuscript) throws Exception {
        KnowledgeState state = stateService.load(manuscript);
        List<Part> branch = branch(manuscript);
        BranchProcessor.align(state, branch);
        int cursor = BranchProcessor.cursorPosition(state, branch);
        // the parts before the start point are never read, they are not unread
        int start = 0;
        for (int i = 0; i < branch.size() && state.getStartMessage() != null; i++) {
            if (branch.get(i).uuid().equals(state.getStartMessage())) {
                start = i;
            }
        }
        return new Status(branch.size(), cursor, BranchProcessor.changedSinceProcessed(state, branch),
                Math.max(0, branch.size() - Math.max(cursor, start)));
    }

    /**
     * Takes back one change of the log of automatic runs, see {@link ChangeLog#revert}.
     */
    public TrackerChangeLog.Outcome revertChange(Manuscript manuscript, String entryId) throws Exception {
        synchronized (lock(manuscript)) {
            KnowledgeState state = stateService.load(manuscript);
            TrackerChangeLog.Outcome outcome = ChangeLog.revert(state, entryId);
            if (outcome == TrackerChangeLog.Outcome.REVERTED) {
                stateService.save(manuscript, state);
            }
            return outcome;
        }
    }

    /** Empties the log of automatic runs (the knowledge stays as it is). */
    public void clearChangeLog(Manuscript manuscript) throws Exception {
        synchronized (lock(manuscript)) {
            KnowledgeState state = stateService.load(manuscript);
            ChangeLog.clear(state);
            stateService.save(manuscript, state);
        }
    }

    // --- checkpoints ---

    /**
     * Takes a checkpoint of the state as it was just stored (see {@link TrackerCheckpoints}); called by the commits of the runs with the
     * lock of the book held. A failure is logged, the run stays committed.
     */
    void checkpoint(Manuscript manuscript, KnowledgeState state, Checkpoint.Kind kind, int added, int retracted) {
        try {
            checkpoints.take(files(manuscript), stateService.toJson(state), state.getCursor(), kind, added, retracted);
        } catch (Exception e) {
            // the book is gone or not stored; there is nothing to return to then
        }
    }

    /** The checkpoints of the book whose part is on the active branch, newest first. */
    public List<TrackerCheckpoints.Placed> checkpoints(Manuscript manuscript, List<Part> branch) throws Exception {
        return TrackerCheckpoints.onBranch(checkpoints.list(files(manuscript)), branch);
    }

    /**
     * Returns the tracker to a checkpoint: the characters, the cursor, the processed parts and the log are the checkpoint's, the settings
     * and the start point stay. The next run reads the parts after the part of the checkpoint.
     */
    public TrackerCheckpoints.Restore restoreCheckpoint(Manuscript manuscript, String checkpointId) throws Exception {
        synchronized (lock(manuscript)) {
            List<Part> branch = branch(manuscript);
            TrackerCheckpoints.Restore outcome = checkpoints.restore(files(manuscript), checkpointId, branch, json -> {
                KnowledgeState restored = restore(stateService.load(manuscript), stateService.fromJson(json), branch);
                stateService.save(manuscript, restored);
            });
            if (outcome == TrackerCheckpoints.Restore.RESTORED) {
                stateService.changed(manuscript);
            }
            return outcome;
        }
    }

    /** The state of a checkpoint in the place of the current one, in line with the branch. */
    public static KnowledgeState restore(KnowledgeState current, KnowledgeState checkpoint, List<Part> branch) {
        checkpoint.keepSetupOf(current);
        BranchProcessor.align(checkpoint, branch);
        return checkpoint;
    }

    private CheckpointFiles files(Manuscript manuscript) throws Exception {
        Manuscript stored = manuscript.getId() != null ? manuscript : manuscriptService.find(manuscript);
        return new ResourceCheckpointFiles(resourceService, stored != null ? stored : manuscript);
    }

    /**
     * What a series with the model over the parts after the cursor would cost, see {@link RunEstimate}. The last part
     * of the branch may be unsettled and is left out by the run itself; the estimate counts it, so it is a little high.
     */
    public RunEstimate estimate(Manuscript manuscript) throws Exception {
        KnowledgeState state = stateService.load(manuscript);
        List<Part> branch = branch(manuscript);
        List<Part> pending = BranchProcessor.pending(state, branch, branch.size(), Integer.MAX_VALUE);
        long characters = 0;
        for (Part part : pending) {
            characters += part.text() == null ? 0 : part.text().length();
        }
        return RunEstimate.of(pending.size(), state.getSettings().getPartsPerRun(), characters);
    }

    private List<String> separators(KnowledgeState state) throws Exception {
        return KnowledgeBlockBuilder.effectiveSeparators(state, stateService.getUserSeparators());
    }

    /** The lock of the book: one processing run at a time. */
    public Object lockOf(Manuscript manuscript) {
        return lock(manuscript);
    }

    private Object lock(Manuscript manuscript) {
        return locks.computeIfAbsent(manuscript.getUuid(), _ -> new Object());
    }
}
