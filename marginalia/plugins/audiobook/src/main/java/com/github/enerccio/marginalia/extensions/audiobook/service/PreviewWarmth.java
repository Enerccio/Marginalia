package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.extensions.audiobook.AudiobookLog;
import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.extensions.httphooks.GpuGate;

import java.util.function.Function;
import java.util.function.LongSupplier;

/**
 * Keeps the speech server of one user up between the samples of a window ("Voices and clips", an actor, the cast): while such a window is open
 * the GPU gate is held for the backend of the last sample, so its requests before a batch ran once and the samples after the first are made at
 * once (each sample is a batch that joins the holder: it runs no requests of its own). The requests after the batch run when the last window
 * closes, when a sample needs another backend (that one is held instead), or when no sample was made for {@link #idleMillis} (a window left open
 * must not keep the GPU from the language model and the others for ever).
 * <p>
 * The methods that run requests ({@link #ensure}, {@link #close}, {@link #releaseIfIdle}) block while they run: call them from a thread of
 * their own, never from the UI.
 */
public final class PreviewWarmth {

    private final Function<BackendConfig, GpuGate.Step> before;
    private final Function<BackendConfig, GpuGate.Step> after;
    private final long idleMillis;
    private final LongSupplier clock;

    private int windows;
    private BackendConfig held;
    private GpuGate heldGate;
    private long lastUse;

    /**
     * @param before     the requests before a batch of a backend
     * @param after      the requests after a batch of a backend
     * @param idleMillis how long the server is kept without a sample
     */
    public PreviewWarmth(Function<BackendConfig, GpuGate.Step> before, Function<BackendConfig, GpuGate.Step> after, long idleMillis, LongSupplier clock) {
        this.before = before;
        this.after = after;
        this.idleMillis = idleMillis;
        this.clock = clock;
    }

    /** A window that makes samples was opened. */
    public synchronized void open() {
        windows++;
        lastUse = clock.getAsLong();
    }

    /** A window was closed; the last one gives the server back (its requests after a batch run). */
    public synchronized void close() {
        if (windows > 0) {
            windows--;
        }
        if (windows == 0) {
            release();
        }
    }

    /**
     * Before a sample with the backend: holds the gate for it when a window is open (its requests before a batch run when it was not held yet;
     * another backend held so far is given back first). Nothing happens when no window is open, the sample is then a batch of its own.
     */
    public synchronized void ensure(BackendConfig config) throws BackendException {
        if (windows == 0 || config == null) {
            return;
        }
        lastUse = clock.getAsLong();
        if (held != null && same(held, config)) {
            return;
        }
        release();
        GpuGate gate = config.gate();
        gate.acquire(config.gateMode(), null, before.apply(config));
        held = config;
        heldGate = gate;
        lastUse = clock.getAsLong();
        AudiobookLog.get().info("The speech server is kept up for the samples while the window is open");
    }

    /** Gives the server back when no sample was made for a while; true when it did. */
    public synchronized boolean releaseIfIdle() {
        if (held == null || clock.getAsLong() - lastUse < idleMillis) {
            return false;
        }
        AudiobookLog.get().info("No sample for {} s: the speech server is given back", idleMillis / 1000);
        release();
        return true;
    }

    /** Whether a server is held now. */
    public synchronized boolean holding() {
        return held != null;
    }

    /** Whether a window is open. */
    public synchronized boolean inUse() {
        return windows > 0;
    }

    private void release() {
        if (held == null) {
            return;
        }
        BackendConfig config = held;
        GpuGate gate = heldGate;
        held = null;
        heldGate = null;
        String error = gate.release(after.apply(config));
        if (error != null) {
            AudiobookLog.get().warn("The request after the samples failed: {}", error);
        }
    }

    /** The same server in the same mode: the same backend, unchanged where it matters for the gate. */
    private static boolean same(BackendConfig a, BackendConfig b) {
        return a.getId().equals(b.getId()) && a.gateMode().equals(b.gateMode()) && java.util.Objects.equals(a.getGpu(), b.getGpu());
    }
}
