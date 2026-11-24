package com.github.enerccio.marginalia.domain.traits;

import com.github.enerccio.marginalia.domain.collections.AIType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SupportedAI {
    AIType value();
}