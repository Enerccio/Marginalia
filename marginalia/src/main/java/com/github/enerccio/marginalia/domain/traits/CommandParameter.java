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
     * What the parameter means, shown to the user while they type it, the key of its text; may be left out (see
     * {@link Command} for the pairs of texts).
     */
    L[] description() default {};

    /** {@link #description()} as a plain text, for extensions without translations. */
    String[] descriptionText() default {};

}
