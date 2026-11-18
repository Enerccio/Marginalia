package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;

/**
 * A change refused because somebody else holds the object: an agent that works on a book, a run that writes it. Reading
 * is allowed, changing is not until the holder lets go. The application has its own kinds (a book, a lorebook, a folder);
 * this is what code that only needs to know "it is locked, by whom" catches.
 */
public class LockedException extends IllegalStateException {

    private final String agentName;

    public LockedException(String message, String agentName) {
        super(message);
        this.agentName = agentName;
    }

    /**
     * Name of what holds the lock (an agent, a run), for the message shown to the user.
     */
    public String getAgentName() {
        return agentName;
    }

    /**
     * The warning shown to the user who tried the change; each kind says what is locked.
     */
    public String describe(Localization loc) {
        return String.format(loc.getValue(L.ERROR_HELD_BY_RUN), agentName);
    }
}
