package com.github.enerccio.marginalia.domain.traits;

import com.github.enerccio.marginalia.loc.M_L;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The description of one parameter of a command's signature, by its {@link CommandParameter#name()}; usable only in
 * the {@code parameters} of {@link MarginaliaCommandTexts}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface MarginaliaParameterText {

    String name();

    M_L[] description();
}
