package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;

import java.util.List;

public interface AgentRepository extends ExtendableRepository<Agent> {

    /**
     * Non-deleted agent of the manuscript, the oldest one if there are several; null when there is none.
     */
    Agent findByManuscript(Manuscript manuscript) throws Exception;

    /**
     * Non-deleted agents of the manuscript.
     */
    List<Agent> findAllByManuscript(Manuscript manuscript) throws Exception;

}
