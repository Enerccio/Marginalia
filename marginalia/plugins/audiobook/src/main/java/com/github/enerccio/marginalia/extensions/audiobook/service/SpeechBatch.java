package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.extensions.audiobook.AudiobookLog;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.extensions.httphooks.GpuGate;

import java.util.function.BooleanSupplier;

/**
 * The time a speech server is held for a batch of pieces: the requests before the batch run ahead of the first piece, all the pieces are
 * made with the server up, and the requests after the batch run when the batch is over, whatever became of it. One swap per batch, not
 * one per piece (loading the language model again takes about a minute).
 * <p>
 * Batches of several users on one GPU share a {@link GpuGate}: the before requests run for the first of them and the after requests for the
 * last, and the batches are made one at a time, so the end of one does not stop the server under the other.
 */
public final class SpeechBatch {

    /** The pieces of the batch, made with the backend that is ready. */
    @FunctionalInterface
    public interface Work<T> {
        T run(SpeechBackend backend) throws BackendException;
    }

    /**
     * @param value      what the work returned
     * @param afterError the message of the first failed request after the batch, null when all went well (the work is done anyway)
     */
    public record Outcome<T>(T value, String afterError) {
    }

    private SpeechBatch() {
    }

    /**
     * @param before    the requests before the batch (in order, the first failure stops the batch: the next must not load a model on a full GPU)
     * @param after     the requests after the batch (all of them are made, the first failure is reported)
     * @param cancelled asked while the batch waits for its turn (may be null)
     */
    public static <T> Outcome<T> run(SpeechBackend backend, GpuGate gate, GpuGate.Step before, GpuGate.Step after, BooleanSupplier cancelled,
                                     Work<T> work) throws BackendException {
        gate.acquire(before);
        T value;
        try {
            value = gate.exclusive(cancelled, () -> {
                backend.prepare();
                AudiobookLog.get().info("The batch starts: the pieces are spoken");
                T made = work.run(backend);
                AudiobookLog.get().info("The batch is over: the requests after it follow");
                return made;
            });
        } catch (BackendException | RuntimeException e) {
            String error = gate.release(after);
            if (error != null) {
                AudiobookLog.get().warn("The request after the batch failed: {}", error);
            }
            throw e;
        }
        String error = gate.release(after);
        if (error != null) {
            AudiobookLog.get().warn("The request after the batch failed: {}", error);
        }
        return new Outcome<>(value, error);
    }
}
