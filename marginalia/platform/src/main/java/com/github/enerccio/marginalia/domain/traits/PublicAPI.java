package com.github.enerccio.marginalia.domain.traits;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares what a plugin sees of an {@link Extendable} class through {@code ExtendableMethodContext}.
 *
 * <p><b>On a method</b> - the signature: the name and type of every argument, the type of the result and the name
 * and type of every named object local variable. Every instrumented method of an {@code @Extendable} class (not a
 * constructor, not static, not synthetic) carries it.</p>
 *
 * <p><b>On a field</b> - the name and the {@link #type() type} of a field a plugin reads or writes with
 * {@code getReflectiveFieldValue} / {@code setReflectiveFieldValue}. Fields are opt-in (the classes have thousands,
 * most of them private details): a field a plugin uses has to be declared, and a declared field is a promise like a
 * method. Only instance fields of {@code @Extendable} classes; the outer instance {@code this$0} of an inner class
 * is synthetic and cannot be annotated, it stays as long as the class is not static.</p>
 *
 * <p>{@code PublicAPISignatureTest} compares the annotation with the compiled class and fails when they differ, so
 * renaming an argument, a local variable or a field, changing a type or adding a method cannot go unnoticed - each of
 * them can break a plugin.</p>
 *
 * <p>When something changes in a way a plugin can notice, update the annotation, set {@link #from()} to the version
 * that contains the change and check the plugins in {@code marginalia/plugins}.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface PublicAPI {

    /**
     * The first version of the application in which the method or field has exactly this signature, for example
     * {@code "1.2.0"}.
     */
    String from();

    /**
     * Methods: the declared return type ({@code void.class} for {@code void}, which is also the default).
     */
    APIReturn returns() default @APIReturn(type = void.class);

    /**
     * Fields: the erased declared type of the field. Not used on a method.
     */
    Class<?> type() default void.class;

    /**
     * All arguments, in declaration order.
     */
    APIParameter[] parameters() default {};

    /**
     * The named local variables holding an object (primitive locals are not visible to decorators) that are not
     * arguments, in the order of the local variable table. A name used for more than one type appears once per type.
     */
    APILocals[] locals() default {};
}
