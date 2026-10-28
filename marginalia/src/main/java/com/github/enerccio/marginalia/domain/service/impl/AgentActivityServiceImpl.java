package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.service.AgentActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class AgentActivityServiceImpl implements AgentActivityService, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(AgentActivityServiceImpl.class);

    // the first change of a burst is heard this late, the rest of the burst with it
    static final long COALESCE_MILLIS = 150;

    private final Map<Long, List<Runnable>> listeners = new ConcurrentHashMap<>();
    // agents with a delivery scheduled; a change while one waits is part of it
    private final Set<Long> pending = ConcurrentHashMap.newKeySet();

    // One thread, so the consoles hear the changes in the order they were made. It is not the thread that changed the run: that one
    // is inside the commit of the change (or a worker of the model), and a console's first move is to read the database.
    private final ScheduledExecutorService delivery = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "agent-activity");
        thread.setDaemon(true);
        return thread;
    });

    @Override
    public Runnable subscribe(Long agentId, Runnable listener) {
        if (agentId == null || listener == null) {
            throw new IllegalArgumentException("The agent and the listener are needed");
        }
        listeners.computeIfAbsent(agentId, id -> new CopyOnWriteArrayList<>()).add(listener);
        return () -> listeners.computeIfPresent(agentId, (id, all) -> {
            all.remove(listener);
            return all.isEmpty() ? null : all;
        });
    }

    @Override
    public void changed(Long agentId) {
        if (agentId == null || !listeners.containsKey(agentId) || !pending.add(agentId)) {
            return;
        }
        try {
            delivery.schedule(() -> deliver(agentId), COALESCE_MILLIS, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
            // the server is going down
            pending.remove(agentId);
        }
    }

    private void deliver(Long agentId) {
        // before the listeners: a change while they run asks for another delivery
        pending.remove(agentId);
        List<Runnable> all = listeners.get(agentId);
        if (all == null) {
            return;
        }
        for (Runnable listener : all) {
            try {
                listener.run();
            } catch (RuntimeException e) {
                // a console that is gone, the others still hear
                log.debug("A listener of the activity of an agent failed: {}", e.getMessage());
            }
        }
    }

    /**
     * Waits until everything changed so far is delivered, for the tests.
     */
    public void drain() throws Exception {
        Thread.sleep(COALESCE_MILLIS);
        delivery.submit(() -> { }).get();
    }

    @Override
    public void destroy() {
        delivery.shutdownNow();
    }
}
