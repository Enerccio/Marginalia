package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.repository.AIRepository;

public class JpaAIRepository extends JpaExtendableRepository<AI> implements AIRepository {

    @Override
    protected Class<AI> getEntityClass() {
        return AI.class;
    }
}
