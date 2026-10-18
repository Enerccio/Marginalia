package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentAttentionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class AgentAttentionServiceImpl implements AgentAttentionService, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(AgentAttentionServiceImpl.class);

    private final Map<Long, List<Consumer<Notice>>> listeners = new ConcurrentHashMap<>();

    // The listeners are not called on the thread that changed the run. That thread is inside the commit of the change
    // (the service hands the run over after the commit, with the transaction still bound to the thread, its connection
    // closed), and a screen's first move is to read the database; Vaadin also runs the access to a UI at once on the calling
    // thread when it can. One thread, so the screens hear the changes in the order they were made.
    private final ExecutorService delivery = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "agent-attention");
        thread.setDaemon(true);
        return thread;
    });

    @Override
    public Runnable subscribe(Long userId, Consumer<Notice> listener) {
        if (userId == null || listener == null) {
            throw new IllegalArgumentException("The user and the listener are needed");
        }
        listeners.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>()).add(listener);
        return () -> listeners.computeIfPresent(userId, (id, all) -> {
            all.remove(listener);
            return all.isEmpty() ? null : all;
        });
    }

    @Override
    public void publish(AgentRun run) {
        if (run == null || run.getOwner() == null || run.getAgent() == null) {
            return;
        }
        Long userId = run.getOwner().getId();
        List<Consumer<Notice>> all = listeners.get(userId);
        if (all == null) {
            return;
        }
        Notice notice = new Notice(userId, run.getUuid(), run.getAgent().getUuid(), run.getAgent().getName(),
                run.getStatus(), run.getStatusReason(), run.isAttention());
        try {
            delivery.execute(() -> {
                for (Consumer<Notice> listener : all) {
                    try {
                        listener.accept(notice);
                    } catch (RuntimeException e) {
                        // a screen that is gone, the others still hear
                        log.debug("A listener of the attention of agents failed: {}", e.getMessage());
                    }
                }
            });
        } catch (RuntimeException e) {
            // the server is going down
            log.debug("The attention of agents was not delivered: {}", e.getMessage());
        }
    }

    /**
     * Waits until everything published so far is delivered, for the tests.
     */
    public void drain() throws Exception {
        delivery.submit(() -> { }).get();
    }

    @Override
    public void destroy() {
        delivery.shutdownNow();
    }
}
