package com.github.enerccio.marginalia.domain.repository.impl;

import com.github.enerccio.marginalia.domain.model.impl.Gpu;
import com.github.enerccio.marginalia.domain.model.impl.GpuLock;
import com.github.enerccio.marginalia.domain.model.impl.GpuStep;
import com.github.enerccio.marginalia.domain.repository.GpuLockRepository;

import java.util.List;

public class JpaGpuLockRepository extends JpaBaseRepository<GpuLock> implements GpuLockRepository {

    @Override
    protected Class<GpuLock> getEntityClass() {
        return GpuLock.class;
    }

    @Override
    public List<GpuLock> findByGpu(Gpu gpu) {
        return getEntityManager()
                .createQuery("SELECT l FROM GpuLock l WHERE l.gpu.id = :gpu AND l.deleted = false", GpuLock.class)
                .setParameter("gpu", gpu.getId())
                .getResultList();
    }

    @Override
    public List<GpuLock> findUsingStep(GpuStep step) {
        return getEntityManager()
                .createQuery("SELECT DISTINCT l FROM GpuLock l LEFT JOIN l.acquireSteps a LEFT JOIN l.releaseSteps r "
                        + "WHERE l.deleted = false AND (a.id = :step OR r.id = :step)", GpuLock.class)
                .setParameter("step", step.getId())
                .getResultList();
    }
}
