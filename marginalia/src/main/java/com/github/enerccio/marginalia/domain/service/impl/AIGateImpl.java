package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.service.AIGate;
import com.github.enerccio.marginalia.domain.service.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class AIGateImpl implements AIGate {

    private static final Logger log = LoggerFactory.getLogger(AIGateImpl.class);

    /**
     * The places of one AI; everything is guarded by the monitor of the object.
     */
    private static final class Slots {
        int cap;
        int active;
        final Deque<CompletableFuture<Permit>> queue = new ArrayDeque<>();

        boolean hasRoom() {
            return cap <= 0 || active < cap;
        }
    }

    @Autowired
    @Lazy
    private AIService aiService;

    // by the uuid of the AI, shared by every user of it
    private final Map<String, Slots> slots = new HashMap<>();

    @Override
    public CompletableFuture<Permit> acquire(AI ai) {
        // the AI the caller holds may be a proxy that can't read its settings now
        AI stored = ai;
        try {
            stored = aiService.find(ai);
        } catch (Exception e) {
            log.warn("The AI {} could not be read for its cap of requests: {}", ai.getUuid(), e.getMessage());
        }
        int cap = stored == null || stored.getMaxConcurrentRequests() == null ? 0 : Math.max(0, stored.getMaxConcurrentRequests());
        Slots mine = slotsOf(ai);
        CompletableFuture<Permit> future = new CompletableFuture<>();
        synchronized (mine) {
            mine.cap = cap;
            // a lowered cap is not a reason to wait for the ones already on their way, a raised one lets the queue in
            mine.queue.removeIf(CompletableFuture::isDone);
            if (mine.queue.isEmpty() && mine.hasRoom()) {
                mine.active++;
                future.complete(new Held(mine));
            } else {
                mine.queue.add(future);
                log.debug("A request waits for a place at the AI {} ({} active, {} waiting, cap {})", ai.getUuid(), mine.active,
                        mine.queue.size(), cap);
                // a future canceled in the queue goes out of the way of the others
                future.whenComplete((permit, error) -> {
                    if (error != null) {
                        synchronized (mine) {
                            mine.queue.remove(future);
                            admit(mine);
                        }
                    }
                });
            }
        }
        return future;
    }

    @Override
    public int active(AI ai) {
        Slots mine = slotsOf(ai);
        synchronized (mine) {
            return mine.active;
        }
    }

    @Override
    public int waiting(AI ai) {
        Slots mine = slotsOf(ai);
        synchronized (mine) {
            mine.queue.removeIf(CompletableFuture::isDone);
            return mine.queue.size();
        }
    }

    private Slots slotsOf(AI ai) {
        synchronized (slots) {
            return slots.computeIfAbsent(ai.getUuid(), uuid -> new Slots());
        }
    }

    /**
     * Gives the free places to the ones that wait, the oldest first. Called with the monitor of {@code mine}.
     */
    private static void admit(Slots mine) {
        while (mine.hasRoom() && !mine.queue.isEmpty()) {
            CompletableFuture<Permit> next = mine.queue.poll();
            // false for a future that was canceled meanwhile: it gets no place
            mine.active++;
            if (!next.complete(new Held(mine))) {
                mine.active--;
            }
        }
    }

    private static final class Held implements Permit {

        private final Slots mine;
        private final AtomicBoolean closed = new AtomicBoolean();

        Held(Slots mine) {
            this.mine = mine;
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                synchronized (mine) {
                    mine.active--;
                    admit(mine);
                }
            }
        }
    }
}
