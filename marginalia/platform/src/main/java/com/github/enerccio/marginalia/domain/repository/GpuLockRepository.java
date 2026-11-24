package com.github.enerccio.marginalia.domain.repository;

import com.github.enerccio.marginalia.domain.model.impl.Gpu;
import com.github.enerccio.marginalia.domain.model.impl.GpuLock;
import com.github.enerccio.marginalia.domain.model.impl.GpuStep;

import java.util.List;

public interface GpuLockRepository extends BaseRepository<GpuLock> {

    /** The locks of the GPU. */
    List<GpuLock> findByGpu(Gpu gpu) throws Exception;

    /** The locks that run the step, as an acquire or a release step. */
    List<GpuLock> findUsingStep(GpuStep step) throws Exception;
}
