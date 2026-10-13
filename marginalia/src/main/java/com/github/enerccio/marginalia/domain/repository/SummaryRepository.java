package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Summary;

import java.util.Collection;

public interface SummaryRepository extends ExtendableRepository<Summary> {

    /**
     * Soft deletes the summaries of all messages of the manuscript with one update (the summaries are not loaded and
     * no listener runs, their extended content stays as it is).
     *
     * @return number of the deleted summaries
     */
    int softDeleteByManuscript(Manuscript manuscript) throws Exception;

    /**
     * Soft deletes the summaries of the messages with the ids, see {@link #softDeleteByManuscript}.
     *
     * @return number of the deleted summaries
     */
    int softDeleteByMessageIds(Collection<Long> messageIds) throws Exception;

}
