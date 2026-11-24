package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.GpuStep;
import com.github.enerccio.marginalia.domain.repository.GpuStepRepository;

public class JpaGpuStepRepository extends JpaBaseRepository<GpuStep> implements GpuStepRepository {

    @Override
    protected Class<GpuStep> getEntityClass() {
        return GpuStep.class;
    }
}
