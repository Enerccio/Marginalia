package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.extensions.audiobook.AudiobookLog;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;

import java.util.function.BooleanSupplier;

/**
 * A batch of pieces on a speech server: the GPU lock of the backend is taken for the batch (the lock's steps unload the language model or
 * another server and load the speech server when the GPU holds something else), the requests before the batch run, all the pieces are made,
 * the requests after the batch run whatever became of it, and the lock is given back. The GPU stays loaded with the speech server after the
 * batch, so the next batch (or sample) of the same lock starts at once; nothing is held between batches.
 */
public final class SpeechBatch {

    /** The pieces of the batch, made with the backend that is ready. */
    @FunctionalInterface
    public interface Work<T> {
        T run(SpeechBackend backend) throws BackendException;
    }

    /** Requests made around the work (before or after a batch, or a restart of the server). */
    @FunctionalInterface
    public interface Hooks {
        void run() throws BackendException;
    }

    /** The hold of the GPU lock; closing it twice does nothing. */
    @FunctionalInterface
    public interface Lease extends AutoCloseable {
        @Override
        void close();

        /** Restarts what the GPU lock loaded (its release and then its acquire steps) while the batch holds it. */
        default void reload() throws BackendException {
        }
    }

    /** Takes the GPU lock of the backend, blocking; a backend without a lock needs nothing. */
    @FunctionalInterface
    public interface Gpu {
        Lease take(BooleanSupplier cancelled) throws BackendException;
    }

    /** No GPU lock. */
    public static final Gpu NO_GPU = cancelled -> () -> {
    };

    /**
     * @param value      what the work returned
     * @param afterError the message of the first failed request after the batch, null when all went well (the work is done anyway)
     */
    public record Outcome<T>(T value, String afterError) {
    }

    private SpeechBatch() {
    }

    /**
     * @param before    the requests before the batch (in order, the first failure stops the batch), may be null
     * @param after     the requests after the batch (all of them are made, the first failure is reported), may be null
     * @param cancelled asked while the batch waits for the GPU (may be null)
     */
    public static <T> Outcome<T> run(SpeechBackend backend, Gpu gpu, Hooks before, Hooks after, BooleanSupplier cancelled, Work<T> work)
            throws BackendException {
        // a batch that was stopped before it began must not touch the GPU: the steps of the lock unload the language model
        if (cancelled != null && cancelled.getAsBoolean()) {
            throw new BackendException("Cancelled");
        }
        try (Lease _ = gpu.take(cancelled)) {
            if (cancelled != null && cancelled.getAsBoolean()) {
                throw new BackendException("Cancelled");
            }
            if (before != null) {
                before.run();
            }
            T value;
            try {
                backend.prepare();
                AudiobookLog.get().info("The batch starts: the pieces are spoken");
                value = work.run(backend);
                AudiobookLog.get().info("The batch is over: the requests after it follow");
            } catch (BackendException | RuntimeException e) {
                runAfter(after);
                throw e;
            }
            return new Outcome<>(value, runAfter(after));
        }
    }

    private static String runAfter(Hooks after) {
        if (after == null) {
            return null;
        }
        try {
            after.run();
            return null;
        } catch (BackendException | RuntimeException e) {
            AudiobookLog.get().warn("The request after the batch failed: {}", e.getMessage());
            return e.getMessage();
        }
    }
}
