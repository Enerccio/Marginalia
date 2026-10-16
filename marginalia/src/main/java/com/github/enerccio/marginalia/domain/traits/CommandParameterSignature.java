package com.github.enerccio.marginalia.domain.traits;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The arguments one {@link Command} method takes. The methods of a suite that share a {@code commandName} are
 * overloads of one command, and the service dispatches a call to the one whose signature fits the arguments (the same
 * number, each convertible to the type of its parameter; the closest types win, see
 * {@link com.github.enerccio.marginalia.domain.service.CommandService}).
 * <pre>
 * &#64;Command(commandName = "hello-world", humanName = "Hello world", signature = &#64;CommandParameterSignature)
 * public void hello(CommandContext context) { ... }   // no arguments
 *
 * &#64;Command(commandName = "hello-world", humanName = "Hello world",
 *          signature = &#64;CommandParameterSignature(&#64;CommandParameter(name = "message")))
 * public void hello1(CommandContext context) { ... }  // one argument
 * </pre>
 * An empty signature is a command without arguments. A command that does not declare a signature at all takes
 * {@link #any() any} arguments.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface CommandParameterSignature {

    /** The parameters in the order the caller passes them. */
    CommandParameter[] value() default {};

    /**
     * Any arguments: any number of objects, which the command reads as a list with
     * {@code CommandContext.getPositional()} (or by name, when the caller passed a map). Then {@link #value()} must be
     * empty. This is the signature of a {@link Command} that declares none; it fits every call, but an overload with a
     * declared signature that fits is preferred.
     */
    boolean any() default false;

}
