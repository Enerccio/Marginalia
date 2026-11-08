package com.github.enerccio.marginalia.extensions.imagegenerator.service;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.extensions.httphooks.GpuGate;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * The time a backend is held by one window: the "before" hook runs ahead of the first image, the backend stays up while the user
 * looks at the image and rerolls, and the "after" hook runs when the window is done with it - or when no image was asked for
 * during the idle time, so a forgotten window doesn't keep the GPU.
 * <p>
 * Windows of several users can be on one GPU: they share a {@link GpuGate}. The before hook runs for the first of them and the after
 * hook for the last, so the end of one window does not unload the model under the image of another, and the images are made one at a time.
 */
public class ImageSession {

    private final ImageBackend backend;
    private final GpuGate.Step before;
    private final GpuGate.Step after;
    private final int idleSeconds;
    private final ScheduledExecutorService scheduler;
    private final Consumer<String> onIdleEnd;
    private final GpuGate gate;

    private boolean leased;
    private int running;
    private boolean endRequested;
    private ScheduledFuture<?> idle;

    /** A session on a GPU of its own (nobody to share it with). */
    public ImageSession(ImageBackend backend, GpuGate.Step before, GpuGate.Step after, int idleSeconds, ScheduledExecutorService scheduler,
                        Consumer<String> onIdleEnd) {
        this(backend, before, after, idleSeconds, scheduler, onIdleEnd, new GpuGate());
    }

    /**
     * @param idleSeconds 0 for no idle end
     * @param onIdleEnd   told (with the error of the after hook, or null) when the session ended because of the idle time
     * @param gate        shared by the sessions of everyone who uses the same GPU
     */
    public ImageSession(ImageBackend backend, GpuGate.Step before, GpuGate.Step after, int idleSeconds, ScheduledExecutorService scheduler,
                        Consumer<String> onIdleEnd, GpuGate gate) {
        this.gate = gate;
        this.backend = backend;
        this.before = before;
        this.after = after;
        this.idleSeconds = idleSeconds;
        this.scheduler = scheduler;
        this.onIdleEnd = onIdleEnd;
    }

    /**
     * Makes one image; the first call of a session runs the before hook. Called from a worker thread.
     */
    public byte[] generate(ImageRequest request, ImageBackend.Cancel cancel) throws BackendException {
        synchronized (this) {
            cancelIdle();
            endRequested = false;
            running++;
            if (!leased) {
                try {
                    gate.acquire(before);
                } catch (BackendException e) {
                    running--;
                    throw e;
                }
                leased = true;
            }
        }
        try {
            return gate.exclusive(cancel == null ? null : cancel::isCancelled, () -> backend.generate(request, cancel));
        } finally {
            boolean finish;
            synchronized (this) {
                running--;
                finish = endRequested && running == 0;
                if (!finish) {
                    scheduleIdle();
                }
            }
            if (finish) {
                end();
            }
        }
    }

    /**
     * Ends the session: runs the after hook if the backend is held. While an image is being made it ends when that image is done.
     *
     * @return the error of the hook, null when it went well or there was nothing to do
     */
    public String end() {
        synchronized (this) {
            cancelIdle();
            if (running > 0) {
                endRequested = true;
                return null;
            }
            if (!leased) {
                return null;
            }
            leased = false;
        }
        String error = gate.release(after);
        if (error != null) {
            ImageGeneratorLogs.warn("The after hook failed: {}", error);
        }
        return error;
    }

    public synchronized boolean isLeased() {
        return leased;
    }

    private void scheduleIdle() {
        if (idleSeconds > 0 && leased) {
            idle = scheduler.schedule(() -> {
                synchronized (this) {
                    if (running > 0 || !leased) {
                        return;
                    }
                }
                String error = end();
                if (onIdleEnd != null) {
                    onIdleEnd.accept(error);
                }
            }, idleSeconds, TimeUnit.SECONDS);
        }
    }

    private void cancelIdle() {
        if (idle != null) {
            idle.cancel(false);
            idle = null;
        }
    }
}
