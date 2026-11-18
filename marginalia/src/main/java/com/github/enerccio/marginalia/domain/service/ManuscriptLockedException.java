package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.loc.M_L;
import com.github.enerccio.marginalia.loc.Localization;

/**
 * A change of a book that an agent works on: the book is locked while a run of its agent is going on, reading it is
 * allowed, changing it is not (see {@link AgentRunService#checkNotLocked}). The user pauses or stops the run to edit.
 * A run that holds the book ({@link com.github.enerccio.marginalia.domain.service.run.Runs.HeldException}) is a
 * {@link LockedException} too, but not this one.
 */
public class ManuscriptLockedException extends LockedException {

    public ManuscriptLockedException(String agentName) {
        this("The book is locked by the agent " + agentName + ", pause or stop its run to change the book", agentName);
    }

    protected ManuscriptLockedException(String message, String agentName) {
        super(message, agentName);
    }

    @Override
    public String describe(Localization loc) {
        return String.format(loc.getValue(M_L.ERROR_BOOK_LOCKED), getAgentName());
    }
}
