package com.github.enerccio.marginalia.domain.traits;

import com.github.enerccio.marginalia.loc.L;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * One parameter of a {@link CommandParameterSignature}: what the caller passes at its position. Usable only inside
 * {@link CommandParameterSignature#value()}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface CommandParameter {

    /** Name the command reads the argument by, {@code CommandContext.get(name)}; unique in its signature. */
    String name();

    /**
     * What the argument is. {@code Object.class} is "any": the argument is passed as it is, a list of objects
     * included. What the caller types in the command line (text) is converted to numbers, booleans, characters and
     * enums when the type is one of them.
     */
    Class<?> type() default Object.class;

    /**
     * Whether the caller may leave the argument out (it is then null, or {@link #defaultValue()}). Without it a
     * parameter is required for an agent when every overload of the command has it: the form with the fewest parameters
     * says what is needed, the longer ones (the ladder of overloads) only add options.
     */
    boolean optional() default false;

    /**
     * The value used when the caller leaves the argument out, as text (converted to the type of the parameter); empty is
     * none. Shown to an agent, the command still gets null from the forms that do not take the parameter.
     */
    String defaultValue() default "";

    /**
     * The values the argument may have, as text; empty is no restriction. An enum type needs none, its constants are
     * the values. For an agent only, the command line does not check it.
     */
    String[] allowed() default {};

    /**
     * The smallest value of a number, as text; empty is no lower limit. For an agent only: it is in the schema of the function and a
     * smaller value is refused with a message that names the limit, the command line does not check it.
     */
    String min() default "";

    /**
     * The largest value of a number, as text; empty is no upper limit. Checked and shown like {@link #min()}.
     */
    String max() default "";

    /**
     * What the parameter means, shown to the user while they type it, the key of its text; may be left out (see
     * {@link Command} for the pairs of texts).
     */
    L[] description() default {};

    /** {@link #description()} as a plain text, for extensions without translations. */
    String[] descriptionText() default {};

}
