package com.github.enerccio.marginalia.domain.service.command;

/**
 * What a registered command returns, from a {@link com.github.enerccio.marginalia.domain.traits.CommandReturn}.
 *
 * @param name        the name of the value, empty when the command returns nothing
 * @param type        the type of the value, {@code void.class} when the command returns nothing, {@code Object.class}
 *                    for any
 * @param description what the value is, may be empty
 */
public record CommandReturnDefinition(String name, Class<?> type, CommandText description) {

    /** Whether the command declares that it returns nothing. */
    public boolean isVoid() {
        return type == void.class || type == Void.class;
    }

    /** The type for display: "any" for {@code Object}, otherwise the simple name of the class. */
    public String typeName() {
        return type == Object.class ? "any" : type.getSimpleName();
    }

}
