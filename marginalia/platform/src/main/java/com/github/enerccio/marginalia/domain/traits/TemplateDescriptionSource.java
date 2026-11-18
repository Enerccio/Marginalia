package com.github.enerccio.marginalia.domain.traits;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an annotation that gives the localized description of a template variable (a field of the data of a template,
 * or its getter), shown in the hints next to a prompt field. Each layer has its own annotation typed with its own enum
 * of keys ({@link LocalizedTemplateDescription} with the platform's {@code L}, {@code MarginaliaTemplateDescription}
 * with the application's {@code M_L}); the element {@code loc} is the key.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface TemplateDescriptionSource {
}
