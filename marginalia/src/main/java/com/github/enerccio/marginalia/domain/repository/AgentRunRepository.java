package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.security.model.User;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface AgentRunRepository extends ExtendableRepository<AgentRun> {

    /**
     * Non-deleted runs of the agent, the newest first.
     */
    List<AgentRun> findAllByAgent(Agent agent) throws Exception;

    /**
     * The non-deleted run of the agent in one of the statuses, the oldest one if there are several; null when there is
     * none.
     */
    AgentRun findByAgent(Agent agent, Collection<AgentRunStatus> statuses) throws Exception;

    /**
     * The non-deleted run of the non-deleted agent of the manuscript that is in one of the statuses, whoever owns it;
     * null when there is none.
     */
    AgentRun findByManuscript(Manuscript manuscript, Collection<AgentRunStatus> statuses) throws Exception;

    /**
     * Non-deleted runs of all users in one of the statuses.
     */
    List<AgentRun> findAllByStatus(Collection<AgentRunStatus> statuses) throws Exception;

    /**
     * Number of the non-deleted runs of the user in one of the statuses.
     */
    long countByStatus(User user, Collection<AgentRunStatus> statuses) throws Exception;

    /**
     * Changes the status of the run only if it still has the expected one, in one SQL update.
     *
     * @param ownerId    only a run of this user is changed; null is a run of anybody
     * @param finishedAt the new end of the run, null clears it
     * @return 1 when the status was changed, 0 when the run has another status (or does not exist)
     */
    int updateStatus(Long runId, Long ownerId, AgentRunStatus from, AgentRunStatus to, String reason, Date finishedAt) throws Exception;

    /**
     * The non-deleted runs of the user that wait to be looked at ({@link AgentRun#isAttention}), the oldest first.
     */
    List<AgentRun> findNeedingAttention(User user) throws Exception;

    /**
     * Clears the attention of the run of the user with one update.
     *
     * @return 1 when the run was waiting to be looked at, 0 otherwise
     */
    int clearAttention(Long runId, Long ownerId) throws Exception;

    /**
     * Adds to the counters of the run in one SQL update, and counts one round.
     *
     * @return 1 when the run exists, 0 otherwise
     */
    int addUsage(Long runId, long prefillTokens, long generatedTokens) throws Exception;

    /**
     * Sets the working copy of the run (null takes it away) with one update, the way the status is changed: a stale
     * copy of the run held by a caller can't write it back.
     *
     * @return 1 when the run exists, 0 otherwise
     */
    int updateWorkingCopy(Long runId, Manuscript workingCopy) throws Exception;

    /**
     * Sets the status of the run with one update whatever the status is now, for the trash of a run that is going on.
     *
     * @return 1 when the run exists, 0 otherwise
     */
    int forceStatus(Long runId, AgentRunStatus to, String reason, Date finishedAt) throws Exception;

}
