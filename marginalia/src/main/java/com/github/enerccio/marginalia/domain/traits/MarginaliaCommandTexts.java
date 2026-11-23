package com.github.enerccio.marginalia.domain.traits;

import com.github.enerccio.marginalia.loc.M_L;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The localized texts of a {@link Command} of the application, {@link M_L} keys (see {@link CommandTextSource}):
 * <pre>
 * &#64;Command(commandName = "take-backup", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
 *         returns = &#64;CommandReturn(name = "message", type = String.class))
 * &#64;MarginaliaCommandTexts(humanName = M_L.LABEL_COMMAND_TAKE_BACKUP, description = M_L.DESC_COMMAND_TAKE_BACKUP,
 *         returns = M_L.DESC_COMMAND_TAKE_BACKUP_RETURN)
 * </pre>
 * A parameter's text is a {@link MarginaliaParameterText} in {@link #parameters()}, by the name of the parameter:
 * <pre>
 *         parameters = &#64;MarginaliaParameterText(name = "from", description = M_L.DESC_COMMAND_SCAN_PHRASES_FROM_PARAM)
 * </pre>
 */
@Documented
@CommandTextSource
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MarginaliaCommandTexts {

    M_L[] humanName() default {};

    M_L[] description() default {};

    M_L[] llmDescription() default {};

    /** What the returned value is. */
    M_L[] returns() default {};

    MarginaliaParameterText[] parameters() default {};

}
