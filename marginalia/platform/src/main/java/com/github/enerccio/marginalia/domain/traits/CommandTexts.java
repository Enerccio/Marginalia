package com.github.enerccio.marginalia.domain.traits;

import com.github.enerccio.marginalia.loc.L;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The localized texts of a {@link Command} of the platform, {@link L} keys (see {@link CommandTextSource}). The
 * application's commands use {@code MarginaliaCommandTexts}.
 */
@Documented
@CommandTextSource
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandTexts {

    L[] humanName() default {};

    L[] description() default {};

    L[] llmDescription() default {};

    /** What the returned value is. */
    L[] returns() default {};

    CommandParameterText[] parameters() default {};

}
