package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.traits.LLMFunction;
import com.github.enerccio.marginalia.domain.traits.LLMParam;

/**
 * What an agent's model can do about the checkpoints of its run: save a restore point of its working copy and its
 * workspace, list them, go back to one and delete the ones it does not need. The runner binds them (see
 * {@link CheckpointToolsImpl}) next to the run tools.
 */
public interface CheckpointTools {

    @LLMFunction(name = "checkpoint", description = "Saves a restore point of the book you work on (the parts, summaries, "
            + "lorebooks and the data of the extensions) and of your workspace, to go back to with checkpoint_restore if a job "
            + "goes wrong. Make one before a risky step: a replace over many parts or entries, a delete, a long rewrite. "
            + "The run also makes checkpoints by itself before the rounds that change something, but only the last few of "
            + "those are kept; yours stay until you delete them. Checkpoints share a quota: when it is full the call fails "
            + "and lists them, then delete the ones you do not need with checkpoint_delete.")
    void checkpoint(FunctionContinuation<String> done,
                    @LLMParam(description = "A few words for what this state is, e.g. \"before renaming the characters\"",
                            required = false) String label);

    @LLMFunction(name = "checkpoint_list", description = "Lists your checkpoints: the number, who made it (start of the run, "
            + "automatic or manual), the round, the size and the label, and how much of the quota they take.")
    void list(FunctionContinuation<String> done);

    @LLMFunction(name = "checkpoint_restore", description = "Goes back to a checkpoint: when this round ends your working copy "
            + "of the book and your workspace are replaced by what was saved. What you changed after it is gone, in the book "
            + "and in the workspace, and what you read since may be out of date. The handles of the items that were in the "
            + "checkpoint still work. The state you leave is saved as an automatic checkpoint first, so a restore can be "
            + "taken back. Make no other change in the same round.")
    void restore(FunctionContinuation<String> done,
                 @LLMParam(description = "The number of the checkpoint, e.g. 0003 or 3") String id);

    @LLMFunction(name = "checkpoint_diff", description = "Shows what changed between a checkpoint and now: in the book "
            + "(settings, parts, lorebooks and entries that were added, changed or deleted) and in your workspace (files added, "
            + "changed or removed). With a path of your workspace only that file or folder is compared, and for a text file the "
            + "sentences that differ are shown. Use it to see what a step did before you decide to go back with "
            + "checkpoint_restore.")
    void diff(FunctionContinuation<String> done,
              @LLMParam(description = "The number of the checkpoint, e.g. 0003 or 3") String id,
              @LLMParam(description = "A file or folder of your workspace to compare instead of everything, e.g. /notes/plot.md",
                      required = false) String path);

    @LLMFunction(name = "checkpoint_delete", description = "Deletes a checkpoint to free the quota. The checkpoint of the start "
            + "of the run and the newest one can't be deleted.")
    void delete(FunctionContinuation<String> done,
                @LLMParam(description = "The number of the checkpoint, e.g. 0003 or 3") String id);
}
