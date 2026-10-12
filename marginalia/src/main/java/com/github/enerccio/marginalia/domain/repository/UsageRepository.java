package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Usage;
import com.github.enerccio.marginalia.domain.security.model.User;

import java.util.List;

public interface UsageRepository extends OwnedRepository<Usage> {

    /**
     * @return the usage of the AI by the user, null if the user never used it
     */
    Usage findByAI(AI ai, User user) throws Exception;

    /**
     * @return usages of the AI by all users
     */
    List<Usage> findAllByAI(AI ai) throws Exception;

    /**
     * Adds to the counters of the usage of the AI by the user (one request), in SQL so parallel additions don't lose
     * counts.
     *
     * @return number of updated usages, 0 when there is none yet
     */
    int add(AI ai, User user, long prefillTokens, long generatedTokens) throws Exception;

}
