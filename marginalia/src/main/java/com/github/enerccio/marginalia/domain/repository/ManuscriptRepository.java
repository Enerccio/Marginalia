package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;
import com.github.enerccio.marginalia.domain.service.search.Sorter;

import java.util.List;

public interface ManuscriptRepository extends ExtendableRepository<Manuscript> {

    List<Long> searchManuscripts(ManuscriptFilterValues filterValues, List<Sorter> sorters, User user) throws Exception;

    /**
     * @param agentic true for the working copies of agents, false for the books of the user, null for both
     */
    List<Long> searchManuscripts(ManuscriptFilterValues filterValues, List<Sorter> sorters, User user, Boolean agentic) throws Exception;

    /**
     * @param agentic true for the working copies of agents, false for the books of the user
     */
    List<Manuscript> findAllByAgentic(boolean agentic, User user) throws Exception;

    Manuscript findViewable(String uuid, User user) throws Exception;

    void markOpened(Long id, User user) throws Exception;

}
