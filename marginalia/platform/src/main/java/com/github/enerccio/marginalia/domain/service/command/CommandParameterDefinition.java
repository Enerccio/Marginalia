package com.github.enerccio.marginalia.domain.service.command;

import java.util.List;

/**
 * A parameter of a registered command signature, from a
 * {@link com.github.enerccio.marginalia.domain.traits.CommandParameter}.
 *
 * @param name         the name the command reads the argument by
 * @param type         the type of the argument, {@code Object.class} for any
 * @param description  what the parameter means, may be empty
 * @param optional     whether the caller may leave the argument out
 * @param defaultValue the value used when it is left out, as text, empty for none
 * @param allowed      the values the argument may have, empty for no restriction
 * @param min          the smallest value of a number, as text, empty for no limit
 * @param max          the largest value of a number, as text, empty for no limit
 */
public record CommandParameterDefinition(String name, Class<?> type, CommandText description, boolean optional,
                                         String defaultValue, List<String> allowed, String min, String max) {

    public CommandParameterDefinition {
        defaultValue = defaultValue == null ? "" : defaultValue;
        allowed = allowed == null ? List.of() : List.copyOf(allowed);
        min = min == null ? "" : min;
        max = max == null ? "" : max;
    }

    public CommandParameterDefinition(String name, Class<?> type, CommandText description, boolean optional,
                                      String defaultValue, List<String> allowed) {
        this(name, type, description, optional, defaultValue, allowed, "", "");
    }

    public CommandParameterDefinition(String name, Class<?> type, CommandText description) {
        this(name, type, description, false, "", List.of());
    }

    /** The type for display: "any" for {@code Object}, otherwise the simple name of the class. */
    public String typeName() {
        return type == Object.class ? "any" : type.getSimpleName();
    }

}
