package com.github.enerccio.marginalia.domain.model.impl;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.traits.CleanupReference;
import com.github.enerccio.marginalia.domain.traits.CleanupReference.Policy;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * One way to use a {@link Gpu}: what its acquire steps leave loaded (the language model, an image workflow, the speech model). An AI or a
 * backend of an extension names the lock it needs by its uuid; every request or job takes the lock for its own length (see
 * {@code GpuService}). The GPU stays loaded with the lock after the job; the release steps run only when a job of another lock of the
 * same GPU comes, before that lock's acquire steps.
 */
@Entity
@Table(name = "gpu_locks")
public class GpuLock extends BaseEntity {

    @Column(nullable = false)
    private String name = "";

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "gpu_id", nullable = false)
    @CleanupReference(Policy.OWNED_BY)
    private Gpu gpu;

    /** How many jobs of the lock run at once, 0 for no limit (a server that makes one image at a time: 1). */
    private int maxConcurrent;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "gpu_lock_acquire_steps",
            joinColumns = @JoinColumn(name = "lock_id"),
            inverseJoinColumns = @JoinColumn(name = "step_id"))
    @OrderColumn(name = "ordinal")
    @CleanupReference(Policy.WEAK)
    private List<GpuStep> acquireSteps = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "gpu_lock_release_steps",
            joinColumns = @JoinColumn(name = "lock_id"),
            inverseJoinColumns = @JoinColumn(name = "step_id"))
    @OrderColumn(name = "ordinal")
    @CleanupReference(Policy.WEAK)
    private List<GpuStep> releaseSteps = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Gpu getGpu() {
        return gpu;
    }

    public void setGpu(Gpu gpu) {
        this.gpu = gpu;
    }

    public int getMaxConcurrent() {
        return maxConcurrent;
    }

    public void setMaxConcurrent(int maxConcurrent) {
        this.maxConcurrent = Math.max(0, maxConcurrent);
    }

    public List<GpuStep> getAcquireSteps() {
        return acquireSteps;
    }

    public void setAcquireSteps(List<GpuStep> acquireSteps) {
        this.acquireSteps = acquireSteps == null ? new ArrayList<>() : acquireSteps;
    }

    public List<GpuStep> getReleaseSteps() {
        return releaseSteps;
    }

    public void setReleaseSteps(List<GpuStep> releaseSteps) {
        this.releaseSteps = releaseSteps == null ? new ArrayList<>() : releaseSteps;
    }
}
