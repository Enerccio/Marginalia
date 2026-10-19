package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.agent.MergePlan;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Takes over what a run changed: compares the working copy of the agent with the book it was made from and with the book as it
 * is now (see {@link com.github.enerccio.marginalia.domain.service.agent.MergeDiff}) and applies the changes the user picked, to
 * the book and its lorebooks in place, item by item. The book is never restored from a backup: that would change the uuid of
 * every part.
 * <p>
 * The run has to be {@link com.github.enerccio.marginalia.domain.collections.AgentRunStatus#DONE finished} or
 * {@link com.github.enerccio.marginalia.domain.collections.AgentRunStatus#STOPPED_KEPT stopped and kept}: it has its copy
 * until the changes are applied or the run is discarded. Works for the runs of the current user only.
 */
public interface AgentMergeService {

    /**
     * What the run changed, compared with the book as it is now.
     *
     * @throws IllegalArgumentException when the run is not a run of the user, or its book is gone
     * @throws IllegalStateException    when the run has no copy to compare
     */
    MergePlan plan(AgentRun run) throws Exception;

    /**
     * Applies the picked changes in one transaction, after a backup of the book. The plan is made again first, so the changes are
     * applied to the book as it is at this moment; an item the plan does not have any more is not applied. The run is
     * {@link com.github.enerccio.marginalia.domain.collections.AgentRunStatus#APPLIED applied} when it is done, its copy and
     * checkpoints are deleted.
     *
     * @throws IllegalArgumentException when nothing is picked, or the run is not a run of the user
     * @throws IllegalStateException    when the run has no copy to take over
     */
    MergeResult apply(AgentRun run, MergeSelection selection) throws Exception;

    /**
     * @param items                the ids of the items to apply ({@link MergePlan.Item#id()})
     * @param edits                the text the user wrote in place of the agent's, by item id and field name; only for the fields
     *                             that are {@link MergePlan.Field#editable() editable}
     * @param switchToAgentBranch  whether the book shows the branch the agent ended on, when it is among the applied parts
     */
    record MergeSelection(Set<String> items, Map<String, Map<String, String>> edits, boolean switchToAgentBranch) {

        public MergeSelection {
            items = items == null ? Set.of() : items;
            edits = edits == null ? Map.of() : edits;
        }
    }

    /**
     * @param run     the run after it was applied
     * @param applied how many items of each kind were applied
     * @param skipped the picked items that could not be applied, with the reason
     * @param backup  the file of the backup of the book made before the apply; null when none was made
     */
    record MergeResult(AgentRun run, Map<MergePlan.Kind, Integer> applied, List<Skipped> skipped, String backup) {

        public int total() {
            return applied.values().stream().mapToInt(Integer::intValue).sum();
        }
    }

    record Skipped(String id, String label, String reason) {
    }
}
