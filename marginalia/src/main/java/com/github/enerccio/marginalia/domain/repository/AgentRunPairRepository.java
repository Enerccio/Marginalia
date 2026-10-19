package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.AgentRunPair;

import java.util.Collection;
import java.util.List;

/**
 * The rows of {@link AgentRunPair}. Not a {@code BaseRepository}: the rows are no {@code BaseEntity}, and every query
 * is keyed by the id of the run.
 */
public interface AgentRunPairRepository {

    /**
     * Stores the rows, in one batch; the pair is stored as it is, the caller has made the uuids unique.
     */
    void insertAll(Collection<AgentRunPair> pairs) throws Exception;

    List<AgentRunPair> findAllByRun(Long runId) throws Exception;

    /**
     * The row of the item of the copy; null when there is none.
     */
    AgentRunPair findByCopy(Long runId, String copyUuid) throws Exception;

    /**
     * The row of the item that was copied from the original item; null when nothing was.
     */
    AgentRunPair findByOriginal(Long runId, String originalUuid) throws Exception;

    long countByRun(Long runId) throws Exception;

    /**
     * Deletes the rows of the run.
     *
     * @return how many there were
     */
    int deleteByRun(Long runId) throws Exception;

}
