package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.Gpu;
import com.github.enerccio.marginalia.domain.repository.GpuRepository;

public class JpaGpuRepository extends JpaBaseRepository<Gpu> implements GpuRepository {

    @Override
    protected Class<Gpu> getEntityClass() {
        return Gpu.class;
    }
}
