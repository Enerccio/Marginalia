package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;

import com.github.enerccio.marginalia.domain.security.model.User;

import java.util.List;

public interface LorebookRepository extends ExtendableRepository<Lorebook> {

    /**
     * @param agentic true for the working copies of agents, false for the books of the user
     */
    List<Lorebook> findAllByAgentic(boolean agentic, User user) throws Exception;

    /**
     * The lorebooks that other users share with everybody: not the user's own, not deleted, not working copies of agents,
     * of an owner that is not deleted.
     */
    List<Lorebook> findAllShared(User user) throws Exception;

    /**
     * The lorebook with the uuid when it is one of {@link #findAllShared}, null otherwise.
     */
    Lorebook findShared(String uuid, User user) throws Exception;

}
