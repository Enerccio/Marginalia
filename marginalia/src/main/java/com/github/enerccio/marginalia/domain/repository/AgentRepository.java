package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface AgentRepository extends ExtendableRepository<Agent> {

    /**
     * Non-deleted agent of the manuscript, the oldest one if there are several; null when there is none.
     */
    Agent findByManuscript(Manuscript manuscript) throws Exception;

    /**
     * Non-deleted agents of the manuscript.
     */
    List<Agent> findAllByManuscript(Manuscript manuscript) throws Exception;

    /**
     * The names of the manuscripts of the agents of the owner, by the uuid of the agent, without loading the manuscripts.
     */
    Map<String, String> findBookNames(Collection<String> agentUuids, Long ownerId) throws Exception;

}
