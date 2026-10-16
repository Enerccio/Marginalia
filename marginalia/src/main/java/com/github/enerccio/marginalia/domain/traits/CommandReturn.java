package com.github.enerccio.marginalia.domain.traits;

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

    /** What the value is. */
    String description() default "";

}
