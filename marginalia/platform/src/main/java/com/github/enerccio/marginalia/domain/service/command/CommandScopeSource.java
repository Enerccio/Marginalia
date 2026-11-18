package com.github.enerccio.marginalia.domain.service.command;

/**
 * Gives the scope of the user at the command bar, the one a command runs in when the caller names none. The application's
 * {@code CommandScopes} is one.
 */
public interface CommandScopeSource {

    /**
     * The scope of the session of the calling thread.
     */
    BaseCommandScope session();
}
