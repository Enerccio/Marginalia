package com.github.enerccio.marginalia.domain.traits;

import com.github.enerccio.marginalia.loc.L;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * What a {@link Command} returns with {@code CommandContext.setReturn}: a name for the value and its Java type, for the
 * user and for agents. Usable only inside {@link Command#returns()}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface CommandReturn {

    /** Name of the returned value. */
    String name();

    /** Type of the returned value, a direct class reference such as {@code String.class}; {@code Object.class} is "any". */
    Class<?> type();

    /** What the value is, the key of its text; may be left out (see {@link Command} for the pairs of texts). */
    L[] description() default {};

    /** {@link #description()} as a plain text, for extensions without translations. */
    String[] descriptionText() default {};

}
