package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.repository.SummaryRepository;

public class JpaSummaryRepository extends JpaExtendableRepository<Summary> implements SummaryRepository {

    @Override
    protected Class<Summary> getEntityClass() {
        return Summary.class;
    }

}
