package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.AgentRun;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * The commands an agent in the {@code ASK} mode wants to run and waits for the user to allow (see
 * {@link com.github.enerccio.marginalia.domain.collections.DestructiveCommandMode}). The runner {@link #request}s an
 * approval and the run goes to {@code WAITING_APPROVAL}; the console of the agent shows the pending ones and the user
 * {@link #decide}s. They live in memory only: a restart of the server pauses every working run, and the call that
 * was waiting is reported to the model as interrupted when the run goes on.
 */
public interface AgentApprovalService {

    /**
     * A command that waits for the decision of the user.
     *
     * @param id          names the approval for {@link #decide}
     * @param command     the command the agent wants to run
     * @param arguments   what it passes, by parameter name
     * @param requestedAt when the agent asked
     */
    record PendingApproval(String id, String command, Map<String, Object> arguments, Date requestedAt) {
    }

    /**
     * Registers an approval of the run, for the runner. The future completes with the decision, and the approval is
     * pending until it does.
     */
    CompletableFuture<Boolean> request(AgentRun run, String command, Map<String, Object> arguments);

    /**
     * Whether any approval of the run is pending, for the runner.
     */
    boolean hasPending(Long runId);

    /**
     * The approvals of the run that wait, the oldest first.
     *
     * @throws IllegalArgumentException when the run is not a run of the current user
     */
    List<PendingApproval> pending(AgentRun run) throws Exception;

    /**
     * The user's decision.
     *
     * @return false when the approval is not pending (decided already, or the run was paused meanwhile)
     * @throws IllegalArgumentException when the run is not a run of the current user
     */
    boolean decide(AgentRun run, String id, boolean approved) throws Exception;

    /**
     * Refuses everything that waits, so the calls in the air end: for a run that is paused, stopped or thrown away.
     *
     * @return how many approvals were refused
     */
    int rejectAll(Long runId);

}
