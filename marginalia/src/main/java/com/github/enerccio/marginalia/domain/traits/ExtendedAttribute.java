package com.github.enerccio.marginalia.domain.traits;

import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Transactional(CommonTx.QUALIFIER)
public @interface ExtendedAttribute {

    boolean inject() default false;
    String injectPrefix() default "";

}
