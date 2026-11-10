package com.github.enerccio.marginalia.extensions.translatorcommon.service;

import com.github.enerccio.marginalia.domain.service.CancellationToken;

import java.util.concurrent.CancellationException;

/**
 * What a run that asks a model shows and how it is stopped. The run binds one to its worker thread ({@link #bind}); {@link ModelClient} finds it
 * there, so the code between the run and the request (the translation service) needs no extra argument. A run with none behaves as before: the
 * request goes on and nothing is shown.
 * <p>
 * Stopping cancels the request that is going now (the model server stops generating) and refuses the next ones of the same run.
 */
public final class RunProgress {

    /**
     * Who shows what the model sends. Called on the worker thread, so an implementation that touches the page hands the work over to it.
     */
    public interface Listener {

        /**
         * A request goes to the model.
         */
        void requestStarted();

        /**
         * A piece of what the model sends.
         *
         * @param reasoning true for its thinking, false for the answer
         */
        void chunk(boolean reasoning, String text);
    }

    private static final ThreadLocal<RunProgress> CURRENT = new ThreadLocal<>();

    private final Listener listener;
    private volatile boolean stopped;
    private volatile CancellationToken token;

    public RunProgress(Listener listener) {
        this.listener = listener;
    }

    /**
     * The progress of the run on this thread, null when there is none.
     */
    public static RunProgress current() {
        return CURRENT.get();
    }

    /**
     * Makes this the progress of the run on the current thread until the result is closed.
     */
    public Binding bind() {
        RunProgress before = CURRENT.get();
        CURRENT.set(this);
        return () -> {
            if (before == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(before);
            }
        };
    }

    /**
     * Ends {@link #bind} without a checked exception.
     */
    public interface Binding extends AutoCloseable {
        @Override
        void close();
    }

    /**
     * Stops the run: the request that is going is cancelled and no other one starts. May be called on any thread, more than once.
     */
    public void stop() {
        stopped = true;
        CancellationToken current = token;
        if (current != null) {
            current.cancel();
        }
    }

    public boolean isStopped() {
        return stopped;
    }

    /**
     * Throws when the run was stopped: a loop over languages or parts calls it between requests.
     *
     * @throws CancellationException when {@link #stop} was called
     */
    public void checkNotStopped() {
        if (stopped) {
            throw new CancellationException("The run was stopped");
        }
    }

    /**
     * A request is about to go to the model.
     *
     * @throws CancellationException when the run was stopped already
     */
    void requestStarted() {
        checkNotStopped();
        token = null;
        listener.requestStarted();
    }

    /**
     * The request that goes now: a stop from here on cancels it (at once when it came before).
     */
    void attach(CancellationToken requestToken) {
        token = requestToken;
        if (stopped) {
            requestToken.cancel();
        }
    }

    void chunk(boolean reasoning, String text) {
        if (text != null && !text.isEmpty()) {
            listener.chunk(reasoning, text);
        }
    }
}
