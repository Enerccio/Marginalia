package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.repository.LorebookRepository;

public class JpaLorebookRepository extends JpaExtendableRepository<Lorebook> implements LorebookRepository {

    @Override
    protected Class<Lorebook> getEntityClass() {
        return Lorebook.class;
    }
}