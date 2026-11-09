package com.github.enerccio.marginalia.extensions;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;

/**
 * One GPU (or any machine that can hold one thing at a time) used by several extensions and users, see {@code GpuGate} of the library
 * {@code http-hooks}, which is how the extensions use it. The gates live here, in the application, so that two extensions on one GPU
 * find the same gate: a class that each extension embeds would be a different class, with a different registry, in each bundle.
 * <ul>
 *   <li>Everybody who holds the gate has asked for the same <i>mode</i> (what the "before" step loads, for example the speech model). The first
 *       to {@link #acquire} runs its "before" step, the last to {@link #release} runs the "after" step, the ones in between run none.</li>
 *   <li>Somebody who wants another mode waits until the holders are gone, then runs its own "before" step. The waiting is in the order
 *       people came: once somebody waits for another mode nobody else joins the holders, so the others cannot starve them.</li>
 *   <li>{@link #exclusive} lets one piece of work run at a time, the others wait their turn in the order they came.</li>
 * </ul>
 */
public final class SharedGate {

    /** A step before or after the work; it may throw anything, the caller gets it back as it was. */
    @FunctionalInterface
    public interface Step {
        void run() throws Exception;
    }

    /** A piece of work that holds the gate. */
    @FunctionalInterface
    public interface Work<T> {
        T run() throws Exception;
    }

    /** The wait for the gate was cancelled or the thread was interrupted. */
    public static class Cancelled extends Exception {
        public Cancelled(String message) {
            super(message);
        }
    }

    private static final ConcurrentHashMap<String, SharedGate> GATES = new ConcurrentHashMap<>();
    private static final long POLL_MILLIS = 200;

    /**
     * The gate of the key, the same one for everybody who asks. The key names the GPU: its name as the administrator or the user wrote it, or
     * the address of the server; case and the space around it do not matter.
     */
    public static SharedGate forKey(String key) {
        return GATES.computeIfAbsent(key == null ? "" : key.strip().toLowerCase(Locale.ROOT), k -> new SharedGate());
    }

    private final Object lease = new Object();
    private final Deque<Object> waiting = new ArrayDeque<>();
    private final ReentrantLock turn = new ReentrantLock(true);
    private String mode;
    private int holders;

    public SharedGate() {
    }

    /**
     * Takes part in the use of the GPU in the mode. Runs {@code before} when nobody holds the gate; waits when the holders have another mode,
     * or somebody who came earlier is waiting. When it fails nothing is held.
     *
     * @param cancelled asked while waiting, the wait ends with {@link Cancelled} when it is true (may be null)
     * @throws Exception what {@code before} threw, or {@link Cancelled}
     */
    public void acquire(String mode, BooleanSupplier cancelled, Step before) throws Exception {
        String wanted = mode == null ? "" : mode;
        Object ticket = new Object();
        synchronized (lease) {
            waiting.addLast(ticket);
            try {
                while (waiting.peekFirst() != ticket || (holders > 0 && !wanted.equals(this.mode))) {
                    if (cancelled != null && cancelled.getAsBoolean()) {
                        throw new Cancelled("Cancelled");
                    }
                    try {
                        lease.wait(POLL_MILLIS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new Cancelled("Interrupted");
                    }
                }
                if (holders == 0 && before != null) {
                    before.run();
                }
                this.mode = wanted;
                holders++;
            } finally {
                waiting.remove(ticket);
                lease.notifyAll();
            }
        }
    }

    /**
     * Gives the GPU back; the last one runs {@code after}.
     *
     * @return the error of the step, null when it went well or there was nothing to do
     */
    public String release(Step after) {
        synchronized (lease) {
            try {
                if (holders == 0) {
                    return null;
                }
                holders--;
                if (holders > 0) {
                    return null;
                }
                mode = null;
                if (after == null) {
                    return null;
                }
                try {
                    after.run();
                    return null;
                } catch (Exception e) {
                    return e.getMessage();
                }
            } finally {
                lease.notifyAll();
            }
        }
    }

    public int holders() {
        synchronized (lease) {
            return holders;
        }
    }

    /** The mode of the holders, null when there are none. */
    public String mode() {
        synchronized (lease) {
            return holders == 0 ? null : mode;
        }
    }

    /**
     * Runs the work when it is its turn; the work of the others waits for it.
     *
     * @param cancelled asked while waiting, the wait ends with {@link Cancelled} when it is true (may be null)
     */
    public <T> T exclusive(BooleanSupplier cancelled, Work<T> work) throws Exception {
        try {
            while (!turn.tryLock(POLL_MILLIS, TimeUnit.MILLISECONDS)) {
                if (cancelled != null && cancelled.getAsBoolean()) {
                    throw new Cancelled("Cancelled");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Cancelled("Interrupted");
        }
        try {
            return work.run();
        } finally {
            turn.unlock();
        }
    }
}
