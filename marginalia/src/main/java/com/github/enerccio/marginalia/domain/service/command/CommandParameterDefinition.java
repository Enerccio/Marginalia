package com.github.enerccio.marginalia.domain.service.command;

/**
 * A parameter of a registered command signature, from a
 * {@link com.github.enerccio.marginalia.domain.traits.CommandParameter}.
 *
 * @param name        the name the command reads the argument by
 * @param type        the type of the argument, {@code Object.class} for any
 * @param description what the parameter means, may be empty
 */
public record CommandParameterDefinition(String name, Class<?> type, CommandText description) {

    /** The type for display: "any" for {@code Object}, otherwise the simple name of the class. */
    public String typeName() {
        return type == Object.class ? "any" : type.getSimpleName();
    }

}
