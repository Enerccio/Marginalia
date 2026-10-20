package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.agent.CheckpointStore.Info;

import java.nio.file.Path;
import java.util.List;

/**
 * The checkpoints of an agent's run: restore points of its working copy and its workspace, kept in the folder of the run
 * ({@link com.github.enerccio.marginalia.domain.service.agent.CheckpointStore}), so that the agent, or the user, can go back
 * when a job went wrong.
 * <p>
 * All the checkpoints of a run share one quota (the permission {@code AGENTS}, {@link #DEFAULT_QUOTA_BYTES} when it
 * does not say). Part of it is the <b>reserve</b> the runner needs: the checkpoint of the start of the run, which is never
 * rolled out, and the {@link com.github.enerccio.marginalia.domain.service.agent.CheckpointStore#RING last automatic ones},
 * made before a round that changes something; an automatic checkpoint replaces the oldest of the ring and is not refused for
 * lack of room taken by the others. The checkpoints made on purpose ({@link #makeManual}) use what is left and are
 * <b>refused</b> when they do not fit: nothing is deleted behind the back of whoever made them.
 * <p>
 * The methods work for the runs of the current user and take a lock for the run, so the worker and the console can use them
 * at the same time. Only a run that is not working may be restored from outside of its worker.
 */
public interface CheckpointService {

    /**
     * What the checkpoints of a run may take when the permission does not say otherwise.
     */
    long DEFAULT_QUOTA_BYTES = 100L * 1024 * 1024;

    /**
     * A checkpoint that was made.
     *
     * @param dropped the automatic checkpoints the ring let go of for it
     */
    record Made(Info checkpoint, List<Info> dropped) {
    }

    /**
     * A restored run.
     *
     * @param checkpoint   the one the run went back to
     * @param safety       the checkpoint of the state before the restore; null when there was no room for it
     * @param removedFiles the files of the workspace that were not in the checkpoint and are gone, as paths
     * @param run          the run with its new working copy
     */
    record Restored(Info checkpoint, Info safety, List<String> removedFiles, AgentRun run) {
    }

    /**
     * What the checkpoints of a run take.
     *
     * @param quotaBytes    what they may take
     * @param reserveBytes  what is kept for the automatic ones that are not made yet
     */
    record Usage(long usedBytes, long quotaBytes, long reserveBytes, int count) {

        /**
         * What a checkpoint made on purpose may still take.
         */
        public long freeBytes() {
            return Math.max(0, quotaBytes - usedBytes - reserveBytes);
        }
    }

    /**
     * What goes wrong with a checkpoint, in words for whoever asked (the agent reads them).
     */
    class CheckpointException extends RuntimeException {
        public CheckpointException(String message) {
            super(message);
        }
    }

    /**
     * A checkpoint does not fit the quota.
     */
    class CheckpointFullException extends CheckpointException {
        public CheckpointFullException(String message) {
            super(message);
        }
    }

    /**
     * The checkpoint of the start of the run, made when the run has none.
     *
     * @return the checkpoint of the start, the one that was already there too
     * @throws CheckpointFullException when the quota is too small for it
     */
    Info ensureStart(AgentRun run) throws Exception;

    /**
     * A checkpoint of the runner, before a round that changes something.
     *
     * @throws CheckpointFullException when the quota can't hold it even after the ring let go of its older ones
     */
    Made makeAutomatic(AgentRun run, String label) throws Exception;

    /**
     * A checkpoint of the runner that is made in the middle of a round, when the answer of the model that asked for the changes
     * is already in the transcript: a rewind to it forgets that answer too, so the model decides again.
     *
     * @param forgetLast how many of the last rows of the transcript are not part of the state the checkpoint saves
     * @throws CheckpointFullException when the quota can't hold it even after the ring let go of its older ones
     */
    Made makeAutomatic(AgentRun run, String label, int forgetLast) throws Exception;

    /**
     * A checkpoint made on purpose, by the agent or by the user.
     *
     * @throws CheckpointFullException when it does not fit what is left of the quota, the message tells how much it needs
     *                                 and lists the checkpoints
     */
    Made makeManual(AgentRun run, String label) throws Exception;

    /**
     * The checkpoints of the run, the oldest first.
     */
    List<Info> list(AgentRun run) throws Exception;

    /**
     * How much of the quota the checkpoints take.
     */
    Usage usage(AgentRun run) throws Exception;

    /**
     * Deletes a checkpoint.
     *
     * @param byAgent whether the agent asks: it can't delete the checkpoint of the start or the newest one, so that there
     *                is always a state to go back to
     * @return the checkpoint that was deleted
     * @throws CheckpointException when there is none or it may not be deleted
     */
    Info delete(AgentRun run, String id, boolean byAgent) throws Exception;

    /**
     * Brings the run back to the checkpoint: its working copy is made again from the checkpoint (a new book, the old one is
     * deleted), the pairs with the original and the handles of the agent are kept for the items that were in it, and the
     * workspace is the one of the checkpoint (the files written after it are gone). The state before is saved as an
     * automatic checkpoint first, if there is room. Call it only for a run that does not work, or from its worker between
     * two rounds.
     *
     * @throws CheckpointException when there is no such checkpoint
     */
    Restored restore(AgentRun run, String id) throws Exception;

    /**
     * A rewind: a restore, and the conversation goes back to where the checkpoint was made.
     *
     * @param restored  what the restore did
     * @param forgotten how many messages the model no longer reads
     */
    record Rewound(Restored restored, int forgotten) {
    }

    /**
     * The checkpoints the run can be rewound to: the ones that know their place in the conversation, when that place is still
     * part of it (a rewind to an earlier checkpoint left it behind).
     *
     * @return their ids
     */
    java.util.Set<String> rewindable(AgentRun run) throws Exception;

    /**
     * Brings the run back to the checkpoint like {@link #restore}, and the conversation with it: the messages after the place
     * where the checkpoint was made are kept for the user to read, marked rewound, and the model no longer reads them. What the
     * calls of the last kept round that were not answered yet get as their answer is that they were not made, and the model is
     * told what happened. The run is not resumed.
     * <p>
     * The run has to be paused or stopped and kept: nothing works on it.
     *
     * @throws CheckpointException when there is no such checkpoint, it does not know its place in the conversation, the place was
     *                             rewound over before, or the run is not paused or stopped
     */
    Rewound rewind(AgentRun run, String id) throws Exception;

    /**
     * What going back to before a message does.
     *
     * @param checkpoint the checkpoint the run goes back to: the newest one that was made before the message
     * @param forgotten  how many messages the model no longer reads afterwards, the message and what came after it and also
     *                   what came between the checkpoint and the message
     */
    record RewindPlan(Info checkpoint, int forgotten) {
    }

    /**
     * What {@link #rewindToMessage} would do, for the user to confirm.
     *
     * @param messageUuid the uuid of the row of the transcript ({@code AgentMessage})
     * @throws CheckpointException when the message is not one of the run, was rewound over before, or no checkpoint was made
     *                             before it
     */
    RewindPlan planRewindToMessage(AgentRun run, String messageUuid) throws Exception;

    /**
     * Goes back in the conversation to before the message: the state of the run is the one of the newest checkpoint that was made
     * before it (the state of a run is only known at its checkpoints), and the conversation goes back to the place of that
     * checkpoint, see {@link #rewind}. The message and everything after it is kept for the user to read, marked rewound.
     * <p>
     * The run has to be paused or stopped and kept, like for {@link #rewind}.
     *
     * @param messageUuid the uuid of the row of the transcript ({@code AgentMessage}) the run goes back before
     * @throws CheckpointException when the message is not one of the run, was rewound over before, no checkpoint was made before
     *                             it, or the run is not paused or stopped
     */
    Rewound rewindToMessage(AgentRun run, String messageUuid) throws Exception;

    /**
     * The archive of a checkpoint, to download.
     *
     * @throws CheckpointException when there is none
     */
    Path file(AgentRun run, String id) throws Exception;
}
