package com.github.enerccio.marginalia.domain.service;

/**
 * A change of a book that an agent works on: the book is locked while a run of its agent is going on, reading it is
 * allowed, changing it is not (see {@link AgentRunService#checkNotLocked}). The user pauses or stops the run to edit.
 */
public class ManuscriptLockedException extends IllegalStateException {

    private final String agentName;

    public ManuscriptLockedException(String agentName) {
        super("The book is locked by the agent " + agentName + ", pause or stop its run to change the book");
        this.agentName = agentName;
    }

    /**
     * Name of the agent that holds the lock, for the message shown to the user.
     */
    public String getAgentName() {
        return agentName;
    }
}
