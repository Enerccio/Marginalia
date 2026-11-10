package com.github.enerccio.marginalia.instruct;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Put on an {@code @Extendable} class by {@link BuildTimeWeaver} when it rewrote its methods, never written in the
 * sources of the application. The hooks of such a class are already in its class file, so neither the weaver nor the
 * agent ({@link RuntimeInstrumentationInitializer}) touches it again.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Woven {

}
