package com.github.enerccio.marginalia.domain.traits;

import com.github.enerccio.marginalia.loc.L;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The description of one parameter of a command's signature, by its {@link CommandParameter#name()}; usable only in
 * the {@code parameters} of {@link CommandTexts}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface CommandParameterText {

    String name();

    L[] description();
}
