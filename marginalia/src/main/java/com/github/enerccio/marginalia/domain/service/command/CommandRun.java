package com.github.enerccio.marginalia.domain.service.command;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * One run of a command: the future of its result, which also carries the two lines of status the command reports with
 * {@link CommandContext#setStatus(String)} and {@link CommandContext#setSubStatus(String)}. Cancelling it stops the run.
 * <p>
 * The status is for whoever shows the progress of the run (the progress window of the command bar); the command itself
 * only sets it. Nothing changes after the run is over.
 * <p>
 * A command can declare its run uninterruptable ({@link CommandContext#setUninterruptable(boolean)}) for the part of its
 * work that can't be stopped half way; while it is, {@link #cancel(boolean)} does nothing and the progress window
 * disables its Stop button.
 */
public class CommandRun extends CompletableFuture<Object> {

    private final List<BiConsumer<String, String>> listeners = new CopyOnWriteArrayList<>();

    private final List<Consumer<Boolean>> uninterruptableListeners = new CopyOnWriteArrayList<>();

    private volatile String status;
    private volatile String subStatus;
    private volatile boolean uninterruptable;

    /**
     * The main line of status, null when the command set none.
     */
    public String getStatus() {
        return status;
    }

    /**
     * The second, more detailed line of status, null when the command set none.
     */
    public String getSubStatus() {
        return subStatus;
    }

    /**
     * Whether the command has declared that it can't be stopped now. A cancel is then refused.
     */
    public boolean isUninterruptable() {
        return uninterruptable;
    }

    /**
     * Calls the listener with the new value each time {@link #isUninterruptable()} changes, on the thread of the command
     * (not the UI thread). It does not get the value set before it was added: read {@link #isUninterruptable()} after
     * adding it.
     */
    public void addUninterruptableListener(Consumer<Boolean> listener) {
        uninterruptableListeners.add(listener);
    }

    /**
     * Stops the run, unless the command has declared it uninterruptable: then nothing happens and it returns false.
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (uninterruptable && !isDone()) {
            return false;
        }
        return super.cancel(mayInterruptIfRunning);
    }

    /**
     * Calls the listener with {@code (status, subStatus)} each time either of them changes, on the thread of the
     * command (not the UI thread). It does not get the status set before it was added: read {@link #getStatus()} and
     * {@link #getSubStatus()} after adding it.
     */
    public void addStatusListener(BiConsumer<String, String> listener) {
        listeners.add(listener);
    }

    /**
     * Stores the status and tells the listeners; a listener that throws does not stop the others or the command.
     * For the service that runs the command, not for the command.
     */
    protected void publishStatus(String status, String subStatus) {
        this.status = status;
        this.subStatus = subStatus;
        for (BiConsumer<String, String> listener : listeners) {
            try {
                listener.accept(status, subStatus);
            } catch (RuntimeException ignored) {
                // the view of the status is gone, the command goes on
            }
        }
    }

    /**
     * Stores the flag and tells the listeners; a listener that throws does not stop the others or the command.
     * For the service that runs the command, not for the command.
     */
    protected void publishUninterruptable(boolean uninterruptable) {
        this.uninterruptable = uninterruptable;
        for (Consumer<Boolean> listener : uninterruptableListeners) {
            try {
                listener.accept(uninterruptable);
            } catch (RuntimeException ignored) {
                // the view is gone, the command goes on
            }
        }
    }

}
