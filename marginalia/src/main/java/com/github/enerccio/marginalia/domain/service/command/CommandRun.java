package com.github.enerccio.marginalia.domain.service.command;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

/**
 * One run of a command: the future of its result, which also carries the two lines of status the command reports with
 * {@link CommandContext#setStatus(String)} and {@link CommandContext#setSubStatus(String)}. Cancelling it stops the run.
 * <p>
 * The status is for whoever shows the progress of the run (the progress window of the command bar); the command itself
 * only sets it. Nothing changes after the run is over.
 */
public class CommandRun extends CompletableFuture<Object> {

    private final List<BiConsumer<String, String>> listeners = new CopyOnWriteArrayList<>();

    private volatile String status;
    private volatile String subStatus;

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

}
