package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.AgentMessage;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;

import java.util.List;

public interface AgentMessageRepository extends ExtendableRepository<AgentMessage> {

    /**
     * Non-deleted messages of the conversation of the agent (no thread) in the order of the transcript.
     */
    List<AgentMessage> findAllByRun(AgentRun run) throws Exception;

    /**
     * The last non-deleted messages of the conversation of the agent, the latest first.
     */
    List<AgentMessage> findLatestByRun(AgentRun run, int limit) throws Exception;

    /**
     * Number of the non-deleted messages of the conversation of the agent, the ordinal the next one gets.
     */
    int countByRun(AgentRun run) throws Exception;

    /**
     * Non-deleted messages of the conversation of one sub-agent in order.
     */
    List<AgentMessage> findAllByRunAndThread(AgentRun run, String thread) throws Exception;

    /**
     * Non-deleted messages of every sub-agent of the run, the conversations one after another in the order they were started.
     */
    List<AgentMessage> findAllThreadRowsByRun(AgentRun run) throws Exception;

    /**
     * Number of the non-deleted messages of one sub-agent, the ordinal the next one gets.
     */
    int countByRunAndThread(AgentRun run, String thread) throws Exception;

    /**
     * Non-deleted messages of the whole run, the agent's and the sub-agents'.
     */
    List<AgentMessage> findEveryByRun(AgentRun run) throws Exception;

}
