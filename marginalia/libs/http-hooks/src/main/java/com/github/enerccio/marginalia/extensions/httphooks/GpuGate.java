package com.github.enerccio.marginalia.extensions.httphooks;

import com.github.enerccio.marginalia.extensions.SharedGate;

import java.util.Locale;
import java.util.function.BooleanSupplier;

/**
 * One GPU used by several people and several extensions. The hooks around a piece of work (load my model, free it) must not interleave: if the
 * "after" hook of one user's window unloads the model while another user's job runs, that job fails. So everyone who uses the same GPU goes
 * through one gate (see {@link #forBackend}):
 * <ul>
 *   <li>the first one to {@link #acquire} runs the "before" hook, the last one to {@link #release} runs the "after" hook, the ones
 *       in between run none (the hooks are those of the user who comes first / last);</li>
 *   <li>everybody who holds the gate has asked for the same <i>mode</i>, what the "before" hook loads (the speech model, an image workflow).
 *       Somebody who needs another mode waits until the holders are gone and then runs their own "before" hook;</li>
 *   <li>{@link #exclusive} lets one piece of work run at a time, the others wait their turn in the order they came.</li>
 * </ul>
 * The gates themselves are kept by the application ({@link SharedGate}), so two extensions that name the same GPU find the same gate even
 * though each embeds its own copy of this class.
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

    private final SharedGate gate;

    /** A gate of its own, shared with nobody. */
    public GpuGate() {
        this(new SharedGate());
    }

    private GpuGate(SharedGate gate) {
        this.gate = gate;
    }

    /** The gate of the server an address belongs to; all the settings that call that server get the same one. */
    public static GpuGate forAddress(String url) {
        return new GpuGate(SharedGate.forKey("address:" + UrlGuard.origin(url)));
    }

    /**
     * The gate of the GPU a backend uses: the one the administrator or the user named, so that backends of different servers (a speech server,
     * an image server) that live on one GPU wait for each other; without a name, the server of the address.
     */
    public static GpuGate forBackend(String gpuName, String url) {
        if (gpuName == null || gpuName.isBlank()) {
            return forAddress(url);
        }
        return new GpuGate(SharedGate.forKey("gpu:" + gpuName.strip().toLowerCase(Locale.ROOT)));
    }

    /**
     * Takes part in the use of the GPU. Runs {@code before} when nobody else holds it; others wait until it has run. When it fails
     * nothing is held.
     */
    public void acquire(Step before) throws BackendException {
        acquire("", null, before);
    }

    /**
     * Like {@link #acquire(Step)} for a mode: waits while the holders have another one.
     *
     * @param mode      what the before hook loads; backends with the same mode can share it
     * @param cancelled asked while waiting, the wait ends with an exception when it is true (may be null)
     */
    public void acquire(String mode, BooleanSupplier cancelled, Step before) throws BackendException {
        try {
            gate.acquire(mode, cancelled, before == null ? null : before::run);
        } catch (Exception e) {
            throw backendException(e);
        }
    }

    /**
     * Gives the GPU back; the last one runs {@code after}.
     *
     * @return the error of the hook, null when it went well or there was nothing to do
     */
    public String release(Step after) {
        return gate.release(after == null ? null : after::run);
    }

    public int holders() {
        return gate.holders();
    }

    /** The mode of the holders, null when there are none. */
    public String mode() {
        return gate.mode();
    }

    /**
     * Runs the work when it is its turn; the work of the others waits for it.
     *
     * @param cancelled asked while waiting, the wait ends with an exception when it is true (may be null)
     */
    public <T> T exclusive(BooleanSupplier cancelled, Work<T> work) throws BackendException {
        try {
            return gate.exclusive(cancelled, work::run);
        } catch (Exception e) {
            throw backendException(e);
        }
    }

    private static BackendException backendException(Exception e) {
        if (e instanceof BackendException backend) {
            return backend;
        }
        if (e instanceof RuntimeException runtime) {
            throw runtime;
        }
        if (e instanceof SharedGate.Cancelled) {
            return new BackendException(e.getMessage());
        }
        return new BackendException(e.getMessage(), e);
    }
}
