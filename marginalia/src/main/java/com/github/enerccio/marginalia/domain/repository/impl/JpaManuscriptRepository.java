package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.ManuscriptRepository;

public class JpaManuscriptRepository extends JpaExtendableRepository<Manuscript> implements ManuscriptRepository {

    @Override
    protected Class<Manuscript> getEntityClass() {
        return Manuscript.class;
    }
}
