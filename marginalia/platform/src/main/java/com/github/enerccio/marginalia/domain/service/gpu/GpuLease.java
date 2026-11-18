package com.github.enerccio.marginalia.domain.service.gpu;

/**
 * A job's hold of a GPU lock (see {@code GpuService}): the GPU is loaded with the lock and the job may use it until the lease is closed.
 * Closing runs no steps, the GPU stays loaded for the next job of the same lock. Closing twice does nothing.
 */
public interface GpuLease extends AutoCloseable {

    /** The lease of a job that needs no GPU (no lock, or a lock that does not exist any more). */
    GpuLease NONE = () -> {
    };

    @Override
    void close();

    /**
     * Restarts what the lock loaded while the job holds it (a server that died in the middle of a long job): the release steps of the lock (all
     * of them), then its acquire steps (the first failure throws). The other jobs of the lock, if any run at once, see the restart too.
     */
    default void reload() throws GpuException {
    }
}
