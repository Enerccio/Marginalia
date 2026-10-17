package com.github.enerccio.marginalia.domain.traits;

import com.github.enerccio.marginalia.loc.L;

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
 *     &#64;Command(commandName = "roll", humanNameText = "Roll a die", descriptionText = "Rolls a die with the given sides")
 *     public void roll(CommandContext context) {
 *         int sides = context.get("sides", Integer.class);
 *         context.setReturn(ThreadLocalRandom.current().nextInt(sides) + 1);
 *     }
 *
 *     &#64;Command(commandName = "coin", humanNameText = "Flip a coin")
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
 * The texts the user (or an agent) reads come in pairs, an {@link L} key for the application (shown in the language of
 * the user) and a {@code ...Text} variant with a plain string for extensions that bring no translations. Both are
 * arrays only so that "not given" can be told from "given" (the default is the empty array); give one element, or
 * write the value alone, as for any annotation array: {@code humanName = L.LABEL_X} or
 * {@code humanNameText = "X"}. Giving both of a pair, or more than one element, is an error when the suite is
 * registered, and so is giving neither for the human name. The other texts are optional.
 * A command with {@link #async()} {@code false} must call {@code setReturn} before the method returns (otherwise it
 * returned {@code null}); an asynchronous one runs on a worker thread and may answer later, from any thread.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

    /** Unique name the command is run by. The overloads of a command share it. Letters, digits, '_', '-' and '.' only. */
    String commandName();

    /**
     * Name shown to the user, the key of its text; the same for all the overloads of a command. One of this and
     * {@link #humanNameText()} is required.
     */
    L[] humanName() default {};

    /** {@link #humanName()} as a plain text, for extensions without translations. */
    String[] humanNameText() default {};

    /** What the command does, shown to the user, the key of its text; may be left out. */
    L[] description() default {};

    /** {@link #description()} as a plain text, for extensions without translations. */
    String[] descriptionText() default {};

    /**
     * What the command does and when to use it, for an agent (LLM) that decides on this text, the key of its text; may
     * be left out.
     */
    L[] llmDescription() default {};

    /** {@link #llmDescription()} as a plain text, for extensions without translations. */
    String[] llmDescriptionText() default {};

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
