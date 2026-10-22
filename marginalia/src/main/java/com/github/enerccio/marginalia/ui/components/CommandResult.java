package com.github.enerccio.marginalia.ui.components;

/**
 * What a command returned, as the {@link CommandBar} hands it to its {@link CommandResultTarget}.
 *
 * @param commandName the name of the command that was run
 * @param humanName   the name of the command for the user
 * @param value       what the command set with {@code setReturn}, null when it set nothing
 */
public record CommandResult(String commandName, String humanName, Object value) {

    /**
     * The value as text, empty when there is none.
     */
    public String text() {
        return value == null ? "" : String.valueOf(value);
    }

}
