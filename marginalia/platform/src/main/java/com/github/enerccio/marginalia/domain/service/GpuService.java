package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Gpu;
import com.github.enerccio.marginalia.domain.model.impl.GpuLock;
import com.github.enerccio.marginalia.domain.model.impl.GpuStep;
import com.github.enerccio.marginalia.domain.service.gpu.GpuException;
import com.github.enerccio.marginalia.domain.service.gpu.GpuLease;
import com.github.enerccio.marginalia.domain.service.gpu.GpuLockChoice;
import com.github.enerccio.marginalia.domain.service.gpu.GpuStatus;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;

/**
 * GPU cooperation: the AIs and the backends of the extensions that run on one GPU take turns on it.
 * <p>
 * The administrator sets up the {@link Gpu}s, the {@link GpuLock}s (a way to use a GPU: what its acquire steps load) and the
 * {@link GpuStep}s (HTTP requests, shared by locks). An AI ({@code AI.gpuLock}) or a backend of an extension names the lock it needs;
 * every request or job takes the lock for its own length with {@link #acquire} / {@link #acquireAsync}:
 * <ul>
 *   <li>a GPU loaded with the lock lets the job in at once (up to {@link GpuLock#getMaxConcurrent()} jobs), no step runs;</li>
 *   <li>a GPU loaded with another lock waits until that lock's jobs are done, runs its release steps (all of them, whatever one
 *       answers; when one failed the GPU may still be full: the job fails and the GPU stays with the lock it had), then the acquire
 *       steps of the new lock (in order, the first failure fails the job and leaves the GPU unknown);</li>
 *   <li>closing the lease runs no step: the GPU stays loaded for the next job (sticky), no timeout frees it;
 *       {@link #unload} frees it by hand;</li>
 *   <li>jobs wait in the order they came, per GPU: once a job waits for a switch, new jobs of the loaded lock wait behind it.</li>
 * </ul>
 * The state is in memory: after a start nothing is known to be loaded, the first job runs only its acquire steps.
 * <p>
 * Extensions use the same service (inject it with {@code @Autowired}) and store the uuid of the lock their backend needs.
 */
public interface GpuService {

    // --- for everybody ---

    /** The locks for a picker (every user may pick one), sorted by GPU and name. */
    List<GpuLockChoice> lockChoices() throws Exception;

    /** The choice of a lock, null when the uuid is blank or names no lock. */
    GpuLockChoice lockChoice(String lockUuid) throws Exception;

    /**
     * Takes the lock for a job; blocks until it is the job's turn and the steps ran. Call it from a worker thread, never from the UI.
     *
     * @param lockUuid  the lock; blank or unknown: {@link GpuLease#NONE} at once
     * @param cancelled asked while waiting (may be null)
     * @throws GpuException a step failed, or the wait was cancelled ({@link GpuException#isCancelled()})
     */
    GpuLease acquire(String lockUuid, BooleanSupplier cancelled) throws GpuException;

    /**
     * Takes the lock for a job without blocking: the future completes with the lease when it is the job's turn and the steps ran, or
     * with a {@link GpuException}. Canceling the future takes the job out of the queue (a lease given meanwhile is closed).
     *
     * @param position told how many jobs are before this one while it waits (may be null); called outside of the lock of the GPU
     */
    CompletableFuture<GpuLease> acquireAsync(String lockUuid, IntConsumer position);

    // --- for the administrator ---

    List<Gpu> findGpus() throws Exception;

    List<GpuLock> findLocks() throws Exception;

    List<GpuStep> findSteps() throws Exception;

    Gpu saveGpu(Gpu gpu) throws Exception;

    /** Deletes the GPU and its locks. */
    void deleteGpu(Gpu gpu) throws Exception;

    GpuLock saveLock(GpuLock lock) throws Exception;

    void deleteLock(GpuLock lock) throws Exception;

    GpuStep saveStep(GpuStep step) throws Exception;

    /** Deletes the step and takes it out of the locks that run it. */
    void deleteStep(GpuStep step) throws Exception;

    /** What the GPUs do now, one status per GPU. */
    List<GpuStatus> statuses() throws Exception;

    /**
     * Frees the GPU: waits for its jobs (in the queue like a job), runs the release steps of the loaded lock and leaves it unknown, also when a
     * step failed (the administrator decides; a dead server must not block the GPU for ever).
     *
     * @return completes with the error of a release step, null when they went well or nothing was loaded
     */
    CompletableFuture<String> unload(Gpu gpu) throws Exception;

    /**
     * Runs the step now, outside of any lock (the Test button of its dialog).
     *
     * @return the error, null when it went well
     */
    String testStep(GpuStep step) throws Exception;
}
