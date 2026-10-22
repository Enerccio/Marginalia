package com.github.enerccio.marginalia.domain.collections;

import java.util.EnumSet;
import java.util.Set;

/**
 * Where an {@code AgentRun} is in its life. Stored by name, do not rename a constant.
 * <p>
 * {@link #RUNNING}, {@link #IDLE} and {@link #WAITING_APPROVAL} are what the run does while it is going on: it works,
 * it waits for the user to write to it, it waits for the user to approve a command. {@link #IDLE} and
 * {@link #WAITING_APPROVAL} are stable states, so the run can be paused or stopped from them right away.
 * {@link #PAUSING} and {@link #STOPPING} are transitions that wait for the run to reach a stable state.
 */
public enum AgentRunStatus {

    /**
     * Working: a request is streaming or a command is going on.
     */
    RUNNING,

    /**
     * The model answered without a command, the run waits for the user (the console).
     */
    IDLE,

    /**
     * A command needs the approval of the user before it goes on.
     */
    WAITING_APPROVAL,

    /**
     * A pause was asked, the run goes on to the next stable state.
     */
    PAUSING,

    /**
     * Paused, the run keeps its copy and transcript and can be resumed, even much later.
     */
    PAUSED,

    /**
     * A stop was asked, the run waits for its workers to exit.
     */
    STOPPING,

    /**
     * Stopped by the user, the copy is kept for review. Can be resumed or applied.
     */
    STOPPED_KEPT,

    /**
     * Stopped by the user (or trashed), the copy is dropped.
     */
    STOPPED_DISCARDED,

    /**
     * The agent finished the task, the result waits for the review.
     */
    DONE,

    /**
     * Ended with an error that cannot be repaired by resuming.
     */
    FAILED,

    /**
     * The result was applied to the manuscript.
     */
    APPLIED;

    private static final Set<AgentRunStatus> ACTIVE = EnumSet.of(RUNNING, WAITING_APPROVAL, PAUSING, STOPPING);
    private static final Set<AgentRunStatus> LOCKING = EnumSet.of(RUNNING, IDLE, WAITING_APPROVAL, PAUSING, STOPPING);
    private static final Set<AgentRunStatus> TERMINAL = EnumSet.of(STOPPED_DISCARDED, FAILED, APPLIED);
    private static final Set<AgentRunStatus> RESUMABLE = EnumSet.of(PAUSED, STOPPED_KEPT);

    /**
     * The statuses a run can go to from this one; the other changes are refused.
     */
    public Set<AgentRunStatus> next() {
        return switch (this) {
            case RUNNING -> EnumSet.of(IDLE, WAITING_APPROVAL, PAUSING, STOPPING, DONE, FAILED);
            case IDLE -> EnumSet.of(RUNNING, PAUSED, STOPPING, DONE);
            case WAITING_APPROVAL -> EnumSet.of(RUNNING, PAUSED, STOPPING, FAILED);
            case PAUSING -> EnumSet.of(RUNNING, PAUSED, STOPPING, FAILED);
            case PAUSED -> EnumSet.of(RUNNING, STOPPED_KEPT, STOPPED_DISCARDED);
            case STOPPING -> EnumSet.of(STOPPED_KEPT, STOPPED_DISCARDED, FAILED);
            case STOPPED_KEPT -> EnumSet.of(RUNNING, STOPPED_DISCARDED, APPLIED);
            case DONE -> EnumSet.of(STOPPED_DISCARDED, APPLIED);
            case STOPPED_DISCARDED, FAILED, APPLIED -> EnumSet.noneOf(AgentRunStatus.class);
        };
    }

    public boolean canChangeTo(AgentRunStatus status) {
        return next().contains(status);
    }

    /**
     * Whether a runner is (supposed to be) busy with the run. A run in one of these statuses found after a restart of
     * the server was interrupted, see {@code AgentRunService#recoverInterrupted}.
     */
    public boolean isActive() {
        return ACTIVE.contains(this);
    }

    /**
     * Whether the manuscript of the agent is locked while a run has this status: the run is going on (or waits for the
     * user in the console), so the user can't change the book under it. A paused, stopped or finished run releases the
     * lock.
     */
    public boolean holdsLock() {
        return LOCKING.contains(this);
    }

    /**
     * Whether the user should hear that the run is in this status: it waits for them (an answer, the approval of a
     * command), it finished, it failed, or it was paused for a reason of its own (a limit, a model that can't be asked,
     * a restart of the server). A pause the user asked for has no reason and does not need their attention.
     *
     * @param reason the reason of the status
     */
    public boolean needsAttention(String reason) {
        return switch (this) {
            case IDLE, WAITING_APPROVAL, DONE, FAILED -> true;
            case PAUSED -> reason != null && !reason.isBlank();
            default -> false;
        };
    }

    /**
     * The run is over and cannot be resumed or applied; it only waits for the trash.
     */
    public boolean isTerminal() {
        return TERMINAL.contains(this);
    }

    /**
     * The run is not over: it blocks the agent from starting another one, because there is at most one unfinished run
     * per agent (a run waiting for the review blocks it too).
     */
    public boolean isUnfinished() {
        return !isTerminal();
    }

    /**
     * Runs that can be resumed by the user.
     */
    public boolean isResumable() {
        return RESUMABLE.contains(this);
    }

    public static Set<AgentRunStatus> unfinished() {
        Set<AgentRunStatus> statuses = EnumSet.allOf(AgentRunStatus.class);
        statuses.removeAll(TERMINAL);
        return statuses;
    }

    public static Set<AgentRunStatus> locking() {
        return EnumSet.copyOf(LOCKING);
    }

    public static Set<AgentRunStatus> active() {
        return EnumSet.copyOf(ACTIVE);
    }
}
