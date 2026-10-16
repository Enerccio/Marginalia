package com.github.enerccio.marginalia.domain.traits;

import java.lang.annotation.*;

/**
 * Marks a method of a command suite as a command that can be run through
 * {@link com.github.enerccio.marginalia.domain.service.CommandService}. A suite is any object whose class has such
 * methods; registering the object registers all of them. Methods that share a {@link #commandName()} are overloads of
 * one command, told apart by their {@link #signature()}. A command method is public, returns {@code void} and takes a
 * {@link com.github.enerccio.marginalia.domain.service.command.CommandContext} as its only parameter. It reads its
 * arguments with {@code CommandContext.get(...)} and answers with {@code CommandContext.setReturn(...)}.
 * <pre>
 * public class DiceCommands {
 *     &#64;Command(commandName = "roll", humanName = "Roll a die", description = "Rolls a die with the given sides")
 *     public void roll(CommandContext context) {
 *         int sides = context.get("sides", Integer.class);
 *         context.setReturn(ThreadLocalRandom.current().nextInt(sides) + 1);
 *     }
 *
 *     &#64;Command(commandName = "coin", humanName = "Flip a coin")
 *     public void coin(CommandContext context) {
 *         context.setReturn(ThreadLocalRandom.current().nextBoolean() ? "heads" : "tails");
 *     }
 * }
 * </pre>
 * The suite is shared by all users and runs in parallel, so it must be stateless: keep no fields with the state of a run
 * or a user, read the current state when the command runs from the services and from the session scoped
 * {@link com.github.enerccio.marginalia.bound.SessionPoint} (which also gives the user's
 * {@link com.github.enerccio.marginalia.bound.SessionPoint#getWorkspace() workspace}). Anything a command needs is in
 * the {@code CommandContext} arguments.
 * A command with {@link #async()} {@code false} must call {@code setReturn} before the method returns (otherwise it
 * returned {@code null}); an asynchronous one runs on a worker thread and may answer later, from any thread.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

    /** Unique name the command is run by. The overloads of a command share it. Letters, digits, '_', '-' and '.' only. */
    String commandName();

    /** Name shown to the user; the same for all the overloads of a command. */
    String humanName();

    /** What the command does, shown to the user; may be empty. */
    String description() default "";

    /** What the command does and when to use it, for an agent (LLM) that decides on this text; may be empty. */
    String llmDescription() default "";

    /**
     * The arguments this method takes, see {@link CommandParameterSignature}. Without a declaration the method takes
     * any arguments.
     */
    CommandParameterSignature signature() default @CommandParameterSignature(any = true);

    /**
     * What the command returns: the name and the type of the value. {@code void.class} (the default) when it returns
     * nothing.
     */
    CommandReturn returns() default @CommandReturn(name = "", type = void.class);

    /**
     * false: the command is run on the thread of the caller and has answered when its method returns, so it must call
     * {@code setReturn} before that. true: the command is run on a worker thread and answers whenever it calls
     * {@code setReturn} (or {@code fail}), from any thread; it may hand the work to something else.
     */
    boolean async() default false;

}
