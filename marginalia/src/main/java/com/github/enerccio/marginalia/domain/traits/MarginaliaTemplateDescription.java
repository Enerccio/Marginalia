package com.github.enerccio.marginalia.domain.traits;

import com.github.enerccio.marginalia.loc.M_L;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The description of a template variable of the application, an {@link M_L} key (see
 * {@link TemplateDescriptionSource}).
 */
@TemplateDescriptionSource
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MarginaliaTemplateDescription {

    M_L loc();

}
