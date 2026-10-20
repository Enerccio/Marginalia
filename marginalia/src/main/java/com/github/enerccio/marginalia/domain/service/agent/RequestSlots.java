package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.AIGate.Permit;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The places for the requests to the model that one run can have in the air at once, the agent and its sub-agents
 * together ({@code AgentGrant#maxConcurrency}). With one place the agent is parked while its sub-agent asks the model, and
 * the other way round. A request {@link #acquire}s a place and closes it when it is over; the ones above the cap wait in the
 * order they came. In memory, one object per working run; the cap of the AI ({@code AIGate}) is a separate, second queue.
 */
public final class RequestSlots {

    private final int cap;
    private final Deque<CompletableFuture<Permit>> queue = new ArrayDeque<>();
    private int active;

    /**
     * @param cap how many requests can be in the air, at least one
     */
    public RequestSlots(int cap) {
        this.cap = Math.max(1, cap);
    }

    public int cap() {
        return cap;
    }

    /**
     * The place: completed at once when one is free, otherwise when it is the turn of the caller. Canceling the future takes the
     * caller out of the queue; a canceled future never gets a place.
     */
    public synchronized CompletableFuture<Permit> acquire() {
        CompletableFuture<Permit> future = new CompletableFuture<>();
        queue.removeIf(CompletableFuture::isDone);
        if (queue.isEmpty() && active < cap) {
            active++;
            future.complete(new Held());
        } else {
            queue.add(future);
            future.whenComplete((permit, error) -> {
                if (error != null) {
                    synchronized (this) {
                        queue.remove(future);
                        admit();
                    }
                }
            });
        }
        return future;
    }

    public synchronized int active() {
        return active;
    }

    public synchronized int waiting() {
        queue.removeIf(CompletableFuture::isDone);
        return queue.size();
    }

    private void admit() {
        while (active < cap && !queue.isEmpty()) {
            CompletableFuture<Permit> next = queue.poll();
            active++;
            // false for a future that was canceled meanwhile: it gets no place
            if (!next.complete(new Held())) {
                active--;
            }
        }
    }

    private final class Held implements Permit {

        private final AtomicBoolean closed = new AtomicBoolean();

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                synchronized (RequestSlots.this) {
                    active--;
                    admit();
                }
            }
        }
    }
}
