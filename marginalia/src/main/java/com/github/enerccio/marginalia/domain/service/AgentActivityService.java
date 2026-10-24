package com.github.enerccio.marginalia.domain.service;

/**
 * Tells the consoles that are open that something happened to the runs of an agent: a message was stored, the run changed its
 * status, the answer of the model grew, the queue of the author changed. A console reads the run again when it hears
 * ({@code AgentConsoleDialog}), so it does not have to ask on a timer. Nothing travels with the event, it only says
 * "look again"; what the console shows is read from the services as always.
 * <p>
 * The events are in memory (a restart forgets them, the consoles are gone then too) and coalesced: a burst of changes, such as
 * the chunks of an answer that is streamed, is one call of each listener some milliseconds after the first change. The
 * listeners are not called on the thread that changed something (that thread may be inside a commit), see {@link #changed}.
 */
public interface AgentActivityService {

    /**
     * Calls the listener, on a thread of the service, some time after the runs of the agent changed.
     *
     * @param agentId the {@code id} of the agent
     * @return cancels the subscription
     */
    Runnable subscribe(Long agentId, Runnable listener);

    /**
     * Something happened to the runs of the agent. Cheap, can be called from any thread at any rate (an answer is streamed
     * with it), and does not wait for the listeners. Call it after the change is stored, a listener reads the database.
     */
    void changed(Long agentId);

}
