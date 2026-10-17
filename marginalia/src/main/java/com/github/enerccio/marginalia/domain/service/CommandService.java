package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.command.CommandDefinition;
import com.github.enerccio.marginalia.domain.service.command.CommandException;
import com.github.enerccio.marginalia.domain.service.command.CommandRun;
import com.github.enerccio.marginalia.domain.traits.Command;

import java.util.List;
import java.util.Map;

/**
 * The commands the application and its extensions offer, to be run by name by the UI, by other extensions or by agents.
 * <p>
 * Commands come in suites: an object whose class has methods annotated with {@link Command}. Such a method is public,
 * returns {@code void} and takes a {@link CommandContext} as its only parameter. The arguments of a run reach the
 * command through {@link CommandContext#get(String)}, its result leaves with {@link CommandContext#setReturn(Object)}.
 * Methods with the same command name are overloads of one command: each declares its parameters in a
 * {@link com.github.enerccio.marginalia.domain.traits.CommandParameterSignature} and a call goes to the one that fits.
 * The application's own commands are registered when the service starts.
 * <p>
 * An extension unregisters its suites in {@code onExtensionUnload}, the service does not know which bundle a command
 * came from. Commands are shared between all users and run in parallel, so a suite must be stateless: no fields that
 * hold what one run or one user did. What a command needs to know about the current state it reads when it runs, from the services
 * (which resolve the current user and check ownership) and from the session-scoped
 * {@link com.github.enerccio.marginalia.bound.SessionPoint} (language, and the
 * {@link com.github.enerccio.marginalia.bound.SessionPoint#getWorkspace() workspace} the user is looking at).
 * A command that is not {@code async} runs on the thread of the caller, so the session of the caller is the current one;
 * an {@code async} one runs on a worker thread that gets the request attributes of the caller, so the session scoped
 * beans resolve there too.
 */
public interface CommandService {

    /**
     * Registers all the commands of the suite, or none of them when one is not valid. Registering the same object again
     * does nothing.
     *
     * @return the definitions of the commands (every overload), ordered by name
     * @throws IllegalArgumentException when the class has no method annotated with {@link Command}, an annotated method
     *                                  is not {@code public void name(CommandContext)}, a name is not valid, a
     *                                  signature is not valid or two overloads have the same one, the overloads of a
     *                                  command differ in the human name, or another object is registered under one of
     *                                  the names
     */
    List<CommandDefinition> registerCommands(Object suite);

    /**
     * Removes all the commands of the suite; does nothing when the object is not registered. Runs in progress are not
     * interrupted.
     */
    void unregisterCommands(Object suite);

    /**
     * Removes the command of that name with all its overloads (the other commands of its suite stay); does nothing when
     * there is none.
     *
     * @return whether a command was removed
     */
    boolean unregisterCommand(String commandName);

    /**
     * The registered commands, one definition per overload, in the order of registration; a copy.
     */
    List<CommandDefinition> getCommands();

    /**
     * The overloads of the command of that name, ordered as they are tried: forms with a declared signature by the
     * number of parameters, the form that takes any arguments last.
     *
     * @return the definitions, empty when there is no such command
     */
    List<CommandDefinition> getCommands(String commandName);

    /**
     * Runs the command with the arguments by name. The overload whose parameter names are exactly the keys is called
     * (the values are converted to the types of its parameters); a command that takes any arguments gets the map as it
     * is. A command that is not {@code async} runs on the calling thread and the returned future is complete when this
     * method returns; an {@code async} one runs on a worker thread (with the session of the caller) and the future
     * completes when the command answers. Cancelling the future stops the run: the command sees
     * {@link CommandContext#isTerminated()} and its thread is interrupted; an {@code async} command that has not started
     * yet does not start. The future fails with a {@link CommandException} when the command throws or reports an
     * error, and with {@link IllegalArgumentException} when no such command is registered or no overload fits.
     *
     * @param arguments what {@link CommandContext#get} reads, may be null
     * @return the run, a future of the value the command set with {@link CommandContext#setReturn} (null if it set
     * none); it also holds the status the command reports, see {@link CommandRun}
     */
    CommandRun execute(String commandName, Map<String, Object> arguments);

    /**
     * Runs the command with the arguments by position (any objects, text from a command line included). The overload
     * with as many parameters as there are arguments, to whose types all the arguments convert, is called; when several
     * fit, the one with the closest types wins (an argument that already has the type, then a parameter of type
     * "any", then text converted to a number, boolean or enum), then the one with fewer parameters declared first. A
     * command that declares no signature takes any arguments, they are in {@link CommandContext#getPositional()}.
     * Otherwise like {@link #execute(String, Map)}.
     */
    CommandRun executePositional(String commandName, List<Object> arguments);

    /**
     * Runs a command typed as text, {@code /command-name argument argument}, see
     * {@link com.github.enerccio.marginalia.domain.service.command.CommandLine}; the arguments are text. Fails with
     * {@link IllegalArgumentException} when the text does not start with a slash.
     */
    CommandRun executeLine(String line);

}
