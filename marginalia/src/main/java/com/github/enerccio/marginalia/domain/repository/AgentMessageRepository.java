package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.AgentMessage;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;

import java.util.List;

public interface AgentMessageRepository extends ExtendableRepository<AgentMessage> {

    /**
     * Non-deleted messages of the run in the order of the transcript.
     */
    List<AgentMessage> findAllByRun(AgentRun run) throws Exception;

    /**
     * The last non-deleted messages of the run, the latest first.
     */
    List<AgentMessage> findLatestByRun(AgentRun run, int limit) throws Exception;

    /**
     * Number of the non-deleted messages of the run, the ordinal the next one gets.
     */
    int countByRun(AgentRun run) throws Exception;

}
