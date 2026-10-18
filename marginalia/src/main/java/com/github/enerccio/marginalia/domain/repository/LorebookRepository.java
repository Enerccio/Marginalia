package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;

import com.github.enerccio.marginalia.domain.security.model.User;

import java.util.List;

public interface LorebookRepository extends ExtendableRepository<Lorebook> {

    /**
     * @param agentic true for the working copies of agents, false for the books of the user
     */
    List<Lorebook> findAllByAgentic(boolean agentic, User user) throws Exception;

}