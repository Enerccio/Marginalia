package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.AI;

import java.util.concurrent.CompletableFuture;

/**
 * The cap of requests that may be on the way to one AI at once ({@link AI#getMaxConcurrentRequests()}), for the runs of
 * agents: a server with one slot (a single GPU) would otherwise get a request from every running agent and answer them
 * one by one with timeouts. A request {@link #acquire}s a place, the ones above the cap wait in the order they came. The
 * state is in memory; the cap is read from the AI at every request, so a change takes effect at once.
 * <p>
 * It does not see the requests the user makes by hand (the generation of the story window): those are not queued, but they
 * take the place of nobody either.
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
     * The place at the AI: completed at once when the AI has no cap or a free place, otherwise when it is the turn of the
     * caller. Canceling the future takes the caller out of the queue (a place that was given is not lost: a canceled future
     * never gets one).
     */
    CompletableFuture<Permit> acquire(AI ai);

    /**
     * How many requests hold a place at the AI now.
     */
    int active(AI ai);

    /**
     * How many requests wait for a place at the AI now.
     */
    int waiting(AI ai);
}
