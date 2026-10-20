package com.github.enerccio.marginalia.domain.service.agent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * The sub-agents of one working run: threads of the run with a conversation of their own, started by the agent with
 * {@code spawn_agent}. One object per worker, in memory; what a sub-agent said and was told is stored with the transcript of
 * the run, so a pause does not lose it: the sub-agents stop at a stable point ({@link State#SUSPENDED}), and the worker of
 * the resumed run makes the pool again from the stored conversations ({@link #restore}). The object knows the handles, the
 * state and the result of each; the loop of a sub-agent is the runner's.
 */
public final class SubAgents {

    public enum State {
        /**
         * Works.
         */
        RUNNING,
        DONE, FAILED, CANCELLED,
        /**
         * Stopped because the run was paused. Over for this worker; the conversation goes on when the run does.
         */
        SUSPENDED;

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
        private final String callId;
        private volatile State state = State.RUNNING;
        private volatile String result;
        private volatile int rounds;
        private volatile long tokens;
        private volatile String lastAction;
        private volatile boolean cancelRequested;
        private final CompletableFuture<Sub> finished = new CompletableFuture<>();

        private final SubAgents owner;

        private Sub(String handle, String task, String callId, SubAgents owner) {
            this.handle = handle;
            this.task = task;
            this.callId = callId;
            this.owner = owner;
        }

        /**
         * Whether the sub-agent is asked to stop at its next stable point to go on with the run.
         */
        public boolean isSuspendRequested() {
            return owner.suspending;
        }

        public String handle() {
            return handle;
        }

        public String task() {
            return task;
        }

        /**
         * The id of the {@code spawn_agent} call that started it, null when it is not known.
         */
        public String callId() {
            return callId;
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
    private volatile boolean suspending;

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
     * Makes a sub-agent that no call is known to have started, see {@link #create(String, String)}.
     */
    public Sub create(String task) {
        return create(task, null);
    }

    /**
     * Makes a sub-agent; the caller starts its loop.
     *
     * @param callId the id of the {@code spawn_agent} call that asked for it, null when there is none
     *
     * @throws IllegalStateException when {@code max} sub-agents work already
     */
    public synchronized Sub create(String task, String callId) {
        if (live() >= max) {
            throw new IllegalStateException("The limit of " + max + " sub-agents working at once is reached");
        }
        Sub sub = new Sub("sub" + (++counter), task, callId, this);
        subs.put(sub.handle, sub);
        return sub;
    }

    /**
     * Brings back a sub-agent of a run that was paused (or of one the server was stopped under). Not limited by {@code max}.
     *
     * @param state  how it ended, null when it did not end: the caller continues its conversation
     * @param result its answer or the reason it did not give one, for a state that is not null
     */
    public synchronized Sub restore(String handle, String task, String callId, State state, String result, int rounds, long tokens) {
        Sub sub = new Sub(handle, task, callId, this);
        sub.rounds = rounds;
        sub.tokens = tokens;
        subs.put(handle, sub);
        try {
            counter = Math.max(counter, Integer.parseInt(handle.substring("sub".length())));
        } catch (RuntimeException e) {
            // not a handle this class made: it only keeps its own
        }
        if (state != null) {
            sub.state = state;
            sub.result = result;
            sub.finished.complete(sub);
        }
        return sub;
    }

    /**
     * Makes the sub-agents that work stop at their next stable point as {@link State#SUSPENDED}, to go on with the run.
     */
    public void suspend() {
        suspending = true;
    }

    public boolean isSuspending() {
        return suspending;
    }

    public synchronized List<Sub> all() {
        return new ArrayList<>(subs.values());
    }

    /**
     * The sub-agent a {@code spawn_agent} call started, null when that call started none.
     */
    public synchronized Sub findByCall(String callId) {
        return callId == null ? null : subs.values().stream().filter(sub -> callId.equals(sub.callId)).findFirst().orElse(null);
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
