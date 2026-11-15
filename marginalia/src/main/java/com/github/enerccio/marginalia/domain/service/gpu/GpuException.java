package com.github.enerccio.marginalia.domain.service.gpu;

/** The GPU could not be taken: a step failed, or the wait was cancelled. */
public class GpuException extends Exception {

    private final boolean cancelled;

    public GpuException(String message) {
        this(message, null, false);
    }

    public GpuException(String message, Throwable cause) {
        this(message, cause, false);
    }

    private GpuException(String message, Throwable cause, boolean cancelled) {
        super(message, cause);
        this.cancelled = cancelled;
    }

    /** The wait for the GPU was cancelled by the caller or its thread was interrupted. */
    public static GpuException cancelled() {
        return new GpuException("Cancelled", null, true);
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
