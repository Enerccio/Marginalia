package com.github.enerccio.marginalia.domain.service.agent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * The sub-agents of one working run: threads of the run with a conversation of their own, started by the agent with
 * {@code spawn_agent}. In memory, one object per worker: a sub-agent does not outlive the worker that started it (a paused
 * run starts none again, the agent hears that its call was cut off). The object knows the handles, the state and the
 * result of each; the loop of a sub-agent is the runner's.
 */
public final class SubAgents {

    public enum State {
        RUNNING, DONE, FAILED, CANCELLED;

        public boolean isFinished() {
            return this != RUNNING;
        }
    }

    /**
     * One sub-agent. The runner's loop reports its progress, the tools read it.
     */
    public static final class Sub {
        private final String handle;
        private final String task;
        private volatile State state = State.RUNNING;
        private volatile String result;
        private volatile int rounds;
        private volatile long tokens;
        private volatile String lastAction;
        private volatile boolean cancelRequested;
        private final CompletableFuture<Sub> finished = new CompletableFuture<>();

        private Sub(String handle, String task) {
            this.handle = handle;
            this.task = task;
        }

        public String handle() {
            return handle;
        }

        public String task() {
            return task;
        }

        public State state() {
            return state;
        }

        /**
         * The answer of a sub-agent that is done, the reason for one that failed or was canceled, null while it works.
         */
        public String result() {
            return result;
        }

        public int rounds() {
            return rounds;
        }

        public long tokens() {
            return tokens;
        }

        public String lastAction() {
            return lastAction;
        }

        public boolean isCancelRequested() {
            return cancelRequested;
        }

        /**
         * Asks the sub-agent to stop at its next step.
         */
        public void cancel() {
            cancelRequested = true;
        }

        /**
         * Completes when the sub-agent is over, whatever the state.
         */
        public CompletableFuture<Sub> finished() {
            return finished;
        }

        public void progress(int rounds, long tokens, String lastAction) {
            this.rounds = rounds;
            this.tokens = tokens;
            if (lastAction != null) {
                this.lastAction = lastAction;
            }
        }
    }

    private final int max;
    private final Map<String, Sub> subs = new LinkedHashMap<>();
    private final List<Runnable> slotWaiters = new ArrayList<>();
    private int counter;

    /**
     * @param max how many sub-agents can work at once
     */
    public SubAgents(int max) {
        this.max = Math.max(0, max);
    }

    public int max() {
        return max;
    }

    /**
     * How many sub-agents work now.
     */
    public synchronized int live() {
        return (int) subs.values().stream().filter(sub -> !sub.state.isFinished()).count();
    }

    /**
     * Makes a sub-agent; the caller starts its loop.
     *
     * @throws IllegalStateException when {@code max} sub-agents work already
     */
    public synchronized Sub create(String task) {
        if (live() >= max) {
            throw new IllegalStateException("The limit of " + max + " sub-agents working at once is reached");
        }
        Sub sub = new Sub("sub" + (++counter), task);
        subs.put(sub.handle, sub);
        return sub;
    }

    public synchronized List<Sub> all() {
        return new ArrayList<>(subs.values());
    }

    public synchronized Sub find(String handle) {
        return handle == null ? null : subs.get(handle.strip().toLowerCase());
    }

    /**
     * Ends a sub-agent that works. The first end wins.
     *
     * @param result the answer when it is done, the reason otherwise
     */
    public void finish(Sub sub, State state, String result) {
        synchronized (this) {
            if (sub.state.isFinished()) {
                return;
            }
            sub.result = result;
            sub.state = state;
        }
        sub.finished.complete(sub);
        List<Runnable> due = new ArrayList<>();
        synchronized (this) {
            while (!slotWaiters.isEmpty() && live() < max) {
                due.add(slotWaiters.removeFirst());
            }
        }
        due.forEach(Runnable::run);
    }

    /**
     * Keeps the action for the moment when fewer than {@code max} sub-agents work, unless that is so now.
     *
     * @return true when a place is free now and the action was not kept (the caller goes on), false when it runs when a sub-agent ends
     */
    public boolean whenSlotFree(Runnable action) {
        synchronized (this) {
            if (live() >= max) {
                slotWaiters.add(action);
                return false;
            }
        }
        return true;
    }

    /**
     * Asks every sub-agent that works to stop.
     */
    public void cancelAll() {
        all().forEach(Sub::cancel);
    }

    /**
     * Completes when every sub-agent made so far is over.
     */
    public CompletableFuture<Void> allFinished() {
        return CompletableFuture.allOf(all().stream().map(Sub::finished).toArray(CompletableFuture[]::new));
    }
}
