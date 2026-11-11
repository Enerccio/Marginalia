package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.service.AIGate;
import com.github.enerccio.marginalia.domain.service.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntConsumer;

public class AIGateImpl implements AIGate {

    private static final Logger log = LoggerFactory.getLogger(AIGateImpl.class);

    /** A request in the queue, with whom to tell its place. */
    private record Waiter(CompletableFuture<Permit> future, IntConsumer position) {
    }

    /**
     * What to do after the lock: a place to give ({@code granted} for {@code future}) or a position to tell. Futures are completed
     * and listeners told outside of the lock, so what runs on them may take locks of its own and give places back.
     */
    private record Notice(IntConsumer position, int ahead, CompletableFuture<Permit> future, Held granted) {

        Notice(IntConsumer position, int ahead) {
            this(position, ahead, null, null);
        }

        static Notice grant(CompletableFuture<Permit> future, Held granted) {
            return new Notice(null, 0, future, granted);
        }
    }

    /**
     * The places of one AI; everything is guarded by the monitor of the object.
     */
    private static final class Slots {
        int cap = 1;
        int active;
        final Deque<Waiter> queue = new ArrayDeque<>();

        boolean hasRoom() {
            return active < cap;
        }
    }

    /** A permit for an AI that is not counted (not stored). */
    private static final Permit FREE = () -> {
    };

    @Autowired
    @Lazy
    private AIService aiService;

    // by the uuid of the AI, shared by every user of it
    private final Map<String, Slots> slots = new HashMap<>();

    @Override
    public CompletableFuture<Permit> acquire(AI ai, IntConsumer position) {
        if (ai == null || ai.getUuid() == null) {
            return CompletableFuture.completedFuture(FREE);
        }
        // the AI the caller holds may be older than the setting, or a proxy that can't read it now
        AI stored = ai;
        try {
            AI found = aiService.find(ai);
            if (found != null) {
                stored = found;
            }
        } catch (Exception e) {
            log.warn("The AI {} could not be read for its connections: {}", ai.getUuid(), e.getMessage());
        }
        int cap = stored.getConnections();
        Slots mine = slotsOf(ai);
        CompletableFuture<Permit> future = new CompletableFuture<>();
        List<Notice> notices = new ArrayList<>();
        synchronized (mine) {
            mine.cap = cap;
            // a lowered number is not a reason to wait for the ones already on their way, a raised one lets the queue in
            mine.queue.removeIf(w -> w.future().isDone());
            admit(mine, notices);
            if (mine.queue.isEmpty() && mine.hasRoom()) {
                mine.active++;
                future.complete(new Held(mine));
            } else {
                mine.queue.add(new Waiter(future, position));
                if (position != null) {
                    notices.add(new Notice(position, mine.queue.size() - 1 + mine.active));
                }
                log.debug("A request waits for a connection of the AI {} ({} active, {} waiting, {} connections)", ai.getUuid(),
                        mine.active, mine.queue.size(), cap);
                // a future canceled in the queue goes out of the way of the others
                future.whenComplete((permit, error) -> {
                    if (error != null) {
                        List<Notice> moved = new ArrayList<>();
                        synchronized (mine) {
                            mine.queue.removeIf(w -> w.future() == future);
                            admit(mine, moved);
                            positions(mine, moved);
                        }
                        tell(moved);
                    }
                });
            }
        }
        tell(notices);
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
            mine.queue.removeIf(w -> w.future().isDone());
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
    private static void admit(Slots mine, List<Notice> notices) {
        boolean moved = false;
        while (mine.hasRoom() && !mine.queue.isEmpty()) {
            Waiter next = mine.queue.poll();
            if (next.future().isDone()) {
                continue;
            }
            mine.active++;
            notices.add(Notice.grant(next.future(), new Held(mine)));
            moved = true;
        }
        if (moved) {
            positions(mine, notices);
        }
    }

    /** The places of the ones that wait, after the queue moved. Called with the monitor of {@code mine}. */
    private static void positions(Slots mine, List<Notice> notices) {
        int ahead = mine.active;
        for (Waiter waiter : mine.queue) {
            if (waiter.position() != null && !waiter.future().isDone()) {
                notices.add(new Notice(waiter.position(), ahead));
            }
            ahead++;
        }
    }

    private static void tell(List<Notice> notices) {
        for (Notice notice : notices) {
            if (notice.future() != null) {
                // false for a future that was canceled meanwhile: the place goes to the next one
                if (!notice.future().complete(notice.granted())) {
                    notice.granted().close();
                }
                continue;
            }
            try {
                notice.position().accept(notice.ahead());
            } catch (RuntimeException e) {
                log.debug("A listener of the place in the queue failed: {}", e.getMessage(), e);
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
                List<Notice> notices = new ArrayList<>();
                synchronized (mine) {
                    mine.active--;
                    admit(mine, notices);
                }
                tell(notices);
            }
        }
    }
}
