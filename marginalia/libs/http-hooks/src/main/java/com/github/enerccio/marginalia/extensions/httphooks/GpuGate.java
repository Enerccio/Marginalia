package com.github.enerccio.marginalia.extensions.httphooks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;

/**
 * One GPU used by several people. The hooks around a piece of work (load my model, free it) must not interleave: if the "after"
 * hook of one user's window unloads the model while another user's job runs, that job fails. So everyone who calls the same server
 * goes through one gate (see {@link #forAddress}):
 * <ul>
 *   <li>the first one to {@link #acquire} runs the "before" hook, the last one to {@link #release} runs the "after" hook, the ones
 *       in between run none (the hooks are those of the user who comes first / last);</li>
 *   <li>{@link #exclusive} lets one piece of work run at a time, the others wait their turn in the order they came.</li>
 * </ul>
 * The gates of a plugin live in the plugin (each plugin embeds its own copy of this class), so two plugins on one GPU do not see
 * each other's gate.
 */
public final class GpuGate {

    /** A hook; passed as a function so that the gate can be tested without a server. */
    @FunctionalInterface
    public interface Step {
        void run() throws BackendException;
    }

    /** A piece of work that holds the gate. */
    @FunctionalInterface
    public interface Work<T> {
        T run() throws BackendException;
    }

    private static final ConcurrentHashMap<String, GpuGate> GATES = new ConcurrentHashMap<>();

    /** The gate of the server an address belongs to; all the settings that call that server get the same one. */
    public static GpuGate forAddress(String url) {
        return GATES.computeIfAbsent(UrlGuard.origin(url), key -> new GpuGate());
    }

    private final Object lease = new Object();
    private final ReentrantLock turn = new ReentrantLock(true);
    private int holders;

    public GpuGate() {
    }

    /**
     * Takes part in the use of the GPU. Runs {@code before} when nobody else holds it; others wait until it has run. When it fails
     * nothing is held.
     */
    public void acquire(Step before) throws BackendException {
        synchronized (lease) {
            if (holders == 0 && before != null) {
                before.run();
            }
            holders++;
        }
    }

    /**
     * Gives the GPU back; the last one runs {@code after}.
     *
     * @return the error of the hook, null when it went well or there was nothing to do
     */
    public String release(Step after) {
        synchronized (lease) {
            if (holders == 0) {
                return null;
            }
            holders--;
            if (holders > 0 || after == null) {
                return null;
            }
            try {
                after.run();
                return null;
            } catch (BackendException e) {
                return e.getMessage();
            }
        }
    }

    public int holders() {
        synchronized (lease) {
            return holders;
        }
    }

    /**
     * Runs the work when it is its turn; the work of the others waits for it.
     *
     * @param cancelled asked while waiting, the wait ends with an exception when it is true (may be null)
     */
    public <T> T exclusive(BooleanSupplier cancelled, Work<T> work) throws BackendException {
        try {
            while (!turn.tryLock(200, TimeUnit.MILLISECONDS)) {
                if (cancelled != null && cancelled.getAsBoolean()) {
                    throw new BackendException("Cancelled");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BackendException("Interrupted", e);
        }
        try {
            return work.run();
        } finally {
            turn.unlock();
        }
    }
}
