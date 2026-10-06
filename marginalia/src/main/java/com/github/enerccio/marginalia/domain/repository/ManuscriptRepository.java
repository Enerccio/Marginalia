package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;
import com.github.enerccio.marginalia.domain.service.search.Sorter;

import java.util.List;

public interface ManuscriptRepository extends ExtendableRepository<Manuscript> {

    List<Long> searchManuscripts(ManuscriptFilterValues filterValues, List<Sorter> sorters, User user) throws Exception;

}
