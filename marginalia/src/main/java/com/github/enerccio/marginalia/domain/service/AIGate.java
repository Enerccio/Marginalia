package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.AI;

import java.util.concurrent.CompletableFuture;
import java.util.function.IntConsumer;

/**
 * The connections of an AI ({@link AI#getConnections()}): how many requests may be on the way to it at once. Every request to a
 * model takes a place here, the wrapper that {@code InferenceServices.forAI} puts around the service of any AI (the core's and an
 * extension's) does it, so callers do not; the requests above the number wait in the order they came, one plain queue per AI
 * shared by all its users. A place is one request in flight, not a conversation: it is given back while a function the model
 * called runs, and taken again for the next round. The state is in memory; the number is read from the AI at every request, so a
 * change takes effect at once.
 */
public interface AIGate {

    /**
     * A place at the AI, to be closed when the request is over.
     */
    interface Permit extends AutoCloseable {

        /**
         * Gives the place back, the first request that waits gets it. Closing twice does nothing.
         */
        @Override
        void close();
    }

    /**
     * The place at the AI: completed at once when the AI has a free place, otherwise when it is the turn of the caller.
     * Canceling the future takes the caller out of the queue (a place that was given is not lost: a canceled future never
     * gets one). An AI that is not stored (no uuid) is not counted.
     */
    default CompletableFuture<Permit> acquire(AI ai) {
        return acquire(ai, null);
    }

    /**
     * As {@link #acquire(AI)}; {@code position} is told how many requests are before the caller when it has to wait, and again
     * whenever that number changes while it waits (not when the place is given). Called outside of the lock of the gate, on
     * the thread that changed the queue; what it throws is ignored.
     */
    CompletableFuture<Permit> acquire(AI ai, IntConsumer position);

    /**
     * How many requests hold a place at the AI now.
     */
    int active(AI ai);

    /**
     * How many requests wait for a place at the AI now.
     */
    int waiting(AI ai);
}
