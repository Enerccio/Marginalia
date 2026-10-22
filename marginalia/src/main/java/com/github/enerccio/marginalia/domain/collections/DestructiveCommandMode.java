package com.github.enerccio.marginalia.domain.collections;

/**
 * What an agent does with a command that is {@code DESTRUCTIVE} (see {@code DataSeverity}). Destructive on the working
 * copy is cheap to undo, but the user still decides.
 */
public enum DestructiveCommandMode {

    /**
     * The command runs.
     */
    AUTO,

    /**
     * The run waits for the user to approve the command.
     */
    ASK,

    /**
     * The command is refused, the agent gets an error.
     */
    DENY

}
