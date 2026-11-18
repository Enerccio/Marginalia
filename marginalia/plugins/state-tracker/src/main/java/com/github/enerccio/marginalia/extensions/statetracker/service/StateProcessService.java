package com.github.enerccio.marginalia.extensions.statetracker.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.extensions.PartTextRegistry;
import com.github.enerccio.marginalia.extensions.statetracker.block.StateBlockBuilder;
import com.github.enerccio.marginalia.extensions.statetracker.extract.StateLog;
import com.github.enerccio.marginalia.extensions.statetracker.model.StateBook;
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
import com.google.gson.JsonElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Follows the active branch of a book with the cursor of the State Tracker: loads the branch and the state, stores the result and
 * tells the open tabs. One run per book at a time.
 */
@Configurable
public class StateProcessService {

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

    private final StateService stateService;
    private final TrackerCheckpoints checkpoints = new TrackerCheckpoints(StateExtractionService.SOURCE);
    private final Map<String, Object> locks = new ConcurrentHashMap<>();

    public StateProcessService(StateService stateService) {
        this.stateService = stateService;
    }

    /**
     * What the user sees of the processing.
     *
     * @param parts   how many parts the active branch has
     * @param cursor  the position (the first part is 1) of the last processed part, 0 when none
     * @param changed the uuids of the processed parts whose text changed since
     * @param unread  how many parts after the cursor are not processed yet
     */
    public record Status(int parts, int cursor, List<String> changed, int unread) {
    }

    /** The parts of the active branch of the book as it is stored now, the first part of the story first. */
    public List<Part> branch(Manuscript manuscript) throws Exception {
        return StoryBranch.read(manuscript, manuscriptService, chatMessageService, partTexts);
    }

    /**
     * Moves the cursor over the settled parts without asking the model (nothing is read from them).
     *
     * @param lastSettled whether the last part of the branch counts as settled
     * @param excludeFrom the uuid of a part that is being replaced, it and the later parts are left alone; may be {@code null}
     * @param max         the most parts to pass, {@link #NO_LIMIT} for all
     */
    public Report process(Manuscript manuscript, boolean lastSettled, String excludeFrom, int max) throws Exception {
        synchronized (lockOf(manuscript)) {
            StateBook state = stateService.load(manuscript);
            List<Part> branch = branch(manuscript);
            Report report = BranchProcessor.process(state, branch, BranchProcessor.settledCount(branch, lastSettled, excludeFrom),
                    StateBlockBuilder.effectiveSeparators(state), System.currentTimeMillis(), max);
            if (report.processed() > 0 || report.rewound() > 0) {
                stateService.save(manuscript, state);
                stateService.changed(manuscript);
            }
            return report;
        }
    }

    /**
     * Takes back what came from the part and the parts after it; the next run reads them again.
     *
     * @return how many things were taken back, -1 when the part is not on the active branch
     */
    public int dropFrom(Manuscript manuscript, String partUuid) throws Exception {
        synchronized (lockOf(manuscript)) {
            StateBook state = stateService.load(manuscript);
            int dropped = BranchProcessor.reprocessFrom(state, branch(manuscript), partUuid);
            if (dropped >= 0) {
                stateService.save(manuscript, state);
                stateService.changed(manuscript);
            }
            return dropped;
        }
    }

    /**
     * Brings the state in line with the active branch (a swipe or another branch takes back what came from the parts that are not on
     * it any more), and stores it when that changed anything.
     */
    public int align(Manuscript manuscript) throws Exception {
        synchronized (lockOf(manuscript)) {
            StateBook state = stateService.load(manuscript);
            int rewound = BranchProcessor.align(state, branch(manuscript));
            if (rewound > 0) {
                stateService.save(manuscript, state);
                stateService.changed(manuscript);
            }
            return rewound;
        }
    }

    /**
     * Sets where processing begins; {@code null} = the start of the book. Moving it back makes the parts from the new start unread
     * again, moving it forward leaves what was read.
     */
    public void setStart(Manuscript manuscript, String partUuid) throws Exception {
        synchronized (lockOf(manuscript)) {
            StateBook state = stateService.load(manuscript);
            state.setStartMessage(partUuid);
            stateService.save(manuscript, state);
            stateService.changed(manuscript);
        }
    }

    public Status status(Manuscript manuscript) throws Exception {
        StateBook state = stateService.load(manuscript);
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

    /** What a series with the model over the parts after the cursor would cost, see {@link RunEstimate}. */
    public RunEstimate estimate(Manuscript manuscript) throws Exception {
        StateBook state = stateService.load(manuscript);
        List<Part> branch = branch(manuscript);
        List<Part> pending = BranchProcessor.pending(state, branch, branch.size(), Integer.MAX_VALUE);
        long characters = 0;
        for (Part part : pending) {
            characters += part.text() == null ? 0 : part.text().length();
        }
        return RunEstimate.of(pending.size(), state.getSettings().getPartsPerRun(), characters);
    }

    /** The lock of the book: one processing run at a time. */
    /** Takes back one change of the log of the runs that applied without asking, see {@link StateLog#revert}. */
    public TrackerChangeLog.Outcome revertChange(Manuscript manuscript, String entryId) throws Exception {
        synchronized (lockOf(manuscript)) {
            StateBook state = stateService.load(manuscript);
            TrackerChangeLog.Outcome outcome = StateLog.revert(state, entryId);
            if (outcome == TrackerChangeLog.Outcome.REVERTED) {
                stateService.save(manuscript, state);
                stateService.changed(manuscript);
            }
            return outcome;
        }
    }

    // --- checkpoints ---

    /** The state as a run found it, kept until the run knows what it changed, see {@link #checkpoint}. */
    record Before(JsonElement state, String cursor) {
    }

    /** The state as it was loaded, before the run changes it (a copy: the run goes on changing the state). */
    Before before(StateBook state) {
        return new Before(stateService.toJson(state), state.getCursor());
    }

    /**
     * Takes a checkpoint of the state as the run found it (see {@link TrackerCheckpoints}), so that the run can be undone and its parts
     * read again; called by the commits of the runs with the lock of the book held, after the run was stored. A failure is logged, the
     * run stays committed.
     *
     * @param changes how many changes the run applied
     */
    void checkpoint(Manuscript manuscript, Before before, Checkpoint.Kind kind, int changes) {
        try {
            checkpoints.take(files(manuscript), before.state(), before.cursor(), kind, changes, 0);
        } catch (Exception e) {
            // the book is gone or not stored; there is nothing to return to then
        }
    }

    /** The checkpoints of the book whose part is on the active branch, newest first. */
    public List<TrackerCheckpoints.Placed> checkpoints(Manuscript manuscript, List<Part> branch) throws Exception {
        return TrackerCheckpoints.onBranch(checkpoints.list(files(manuscript)), branch);
    }

    /**
     * Returns the tracker to a checkpoint: the characters with their values, the cursor, the processed parts, the marks of the clock and
     * the log are the checkpoint's, the settings, the start point and the categories stay. The next run reads the parts after the part
     * of the checkpoint.
     */
    public TrackerCheckpoints.Restore restoreCheckpoint(Manuscript manuscript, String checkpointId) throws Exception {
        synchronized (lockOf(manuscript)) {
            List<Part> branch = branch(manuscript);
            StateBook current = stateService.load(manuscript);
            TrackerCheckpoints.Restore outcome = checkpoints.restore(files(manuscript), checkpointId, branch, stateService.toJson(current),
                    current.getCursor(), json -> {
                        StateBook restored = restore(current, stateService.fromJson(json), branch);
                        stateService.save(manuscript, restored);
                    });
            if (outcome == TrackerCheckpoints.Restore.RESTORED) {
                stateService.changed(manuscript);
            }
            return outcome;
        }
    }

    /** The state of a checkpoint in the place of the current one, in line with the branch. */
    public static StateBook restore(StateBook current, StateBook checkpoint, List<Part> branch) {
        checkpoint.keepSetupOf(current);
        BranchProcessor.align(checkpoint, branch);
        return checkpoint;
    }

    private CheckpointFiles files(Manuscript manuscript) throws Exception {
        Manuscript stored = manuscript.getId() != null ? manuscript : manuscriptService.find(manuscript);
        return new ResourceCheckpointFiles(resourceService, stored != null ? stored : manuscript);
    }

    public Object lockOf(Manuscript manuscript) {
        return locks.computeIfAbsent(manuscript.getUuid(), _ -> new Object());
    }
}
