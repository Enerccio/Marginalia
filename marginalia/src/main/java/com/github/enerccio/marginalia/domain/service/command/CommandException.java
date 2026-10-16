package com.github.enerccio.marginalia.domain.service.command;

/**
 * A command failed: its method threw, or it reported an error with {@link CommandContext#fail}. The cause is what the
 * command threw.
 */
public class CommandException extends RuntimeException {

    private final String commandName;

    public CommandException(String commandName, Throwable cause) {
        super("Command " + commandName + " failed: " + cause, cause);
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }

}
