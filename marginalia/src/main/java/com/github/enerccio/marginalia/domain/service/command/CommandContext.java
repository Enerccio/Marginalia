package com.github.enerccio.marginalia.domain.service.command;

import java.util.List;
import java.util.Map;

/**
 * What a {@link com.github.enerccio.marginalia.domain.traits.Command} gets when it is run: the arguments of the call,
 * the way to answer and whether the caller still wants the answer. The context belongs to one run of one command.
 */
public interface CommandContext {

    /**
     * The argument of the call.
     *
     * @return the value, null when the caller did not pass the argument
     */
    Object get(String name);

    /**
     * The argument of the call, as {@code type}.
     *
     * @return the value, null when the caller did not pass the argument
     * @throws IllegalArgumentException when the argument is not an instance of {@code type} (a number is converted to
     *                                  the number type asked for, when it fits)
     */
    <T> T get(String name, Class<T> type);

    /**
     * The arguments in the order of the call, as a list of objects: for a command with a declared signature the
     * converted arguments in the order of its parameters; for one that takes {@code any} arguments the objects the caller
     * passed (empty when the caller passed a map).
     */
    List<Object> getPositional();

    /** Whether the caller passed the argument (even as null). */
    boolean has(String name);

    /** All the arguments of the call, read only. */
    Map<String, Object> getArguments();

    /**
     * Whether the run is over for the caller: the command has finished (answered or failed), or the caller stopped it
     * (the future was cancelled, the stop button of the progress window). A command that runs for a while, an
     * {@code async} one above all, checks it between the steps of its work and returns at once when it is true; what it
     * sets afterwards is ignored. Never true while a command that is not {@code async} runs.
     */
    boolean isTerminated();

    /**
     * Sets the main line of status of the run, shown to the user in the progress window while an {@code async} command
     * runs (what the command is doing now, "Taking backup"). Replaces the previous one; {@code null} or blank clears it.
     * Can be called from any thread; ignored when the run is over.
     */
    void setStatus(String status);

    /**
     * Sets the second line of status, under the main one: the detail of the step ("Chapter 3 of 12"). Replaces the
     * previous one; {@code null} or blank clears it. Can be called from any thread; ignored when the run is over.
     */
    void setSubStatus(String subStatus);

    /**
     * Declares that the run can't be stopped now (or, with {@code false}, that it can be again): the Stop button of the
     * progress window is disabled and cancelling the run does nothing while it is set. For the part of an {@code async}
     * command that can't be abandoned half way, a change that is being written, which it has to finish. The command
     * should not stay uninterruptable longer than it needs. Can be called from any thread; ignored when the run is over.
     */
    void setUninterruptable(boolean uninterruptable);

    /**
     * Whether the run is uninterruptable now, because the command set it or because the caller started it so (which the
     * command can't undo). A command that starts another asynchronous one while it is uninterruptable passes this to
     * {@link com.github.enerccio.marginalia.domain.service.CommandService#execute(String, java.util.Map, boolean)}.
     */
    boolean isUninterruptable();

    /**
     * Sets what the command returns. A command that is not {@code async} must call this before its method returns, or
     * the caller gets {@code null}; later calls are ignored. An {@code async} command finishes with the first call, from
     * any thread, and the calls after it are ignored.
     */
    void setReturn(Object value);

    /**
     * Finishes the command with an error, the caller gets a {@link CommandException} with this cause. For an
     * {@code async} command that reports from another thread; a command that is not {@code async} can just throw.
     * Ignored when the command has finished already.
     */
    void fail(Throwable error);

}
