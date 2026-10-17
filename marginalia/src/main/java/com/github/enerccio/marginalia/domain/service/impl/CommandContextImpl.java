package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.command.CommandException;

import java.util.*;

/**
 * Context of one run of a command. A command that is not async only stores its value, which the service hands over when
 * the method returns ({@link #finish()}); an async one completes the future with the first {@code setReturn}. The run is
 * over for the command ({@link #isTerminated()}) as soon as the future is done, whoever completed or cancelled it.
 */
class CommandContextImpl implements CommandContext {

    private final String commandName;
    private final Map<String, Object> arguments;
    private final List<Object> positional;
    private final boolean async;
    private final CommandRunImpl result;
    /** The caller started the run as uninterruptable, the command can't undo it. */
    private final boolean forcedUninterruptable;

    private Object value;
    private boolean closed;
    private String status;
    private String subStatus;

    CommandContextImpl(String commandName, Map<String, Object> arguments, List<Object> positional, boolean async,
                       CommandRunImpl result, boolean forcedUninterruptable) {
        this.commandName = commandName;
        // LinkedHashMap and ArrayList, not Map.copyOf: arguments may be null
        this.arguments = arguments == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(arguments));
        this.positional = positional == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(positional));
        this.async = async;
        this.result = result;
        this.forcedUninterruptable = forcedUninterruptable;
        if (forcedUninterruptable) {
            result.uninterruptable(true);
        }
    }

    @Override
    public Object get(String name) {
        return arguments.get(name);
    }

    @Override
    public <T> T get(String name, Class<T> type) {
        Object argument = arguments.get(name);
        if (argument == null) {
            return null;
        }
        CommandValues.Converted converted = CommandValues.convert(argument, type);
        if (converted == null) {
            throw new IllegalArgumentException("Argument " + name + " of command " + commandName + " is "
                    + argument.getClass().getName() + ", not " + type.getName());
        }
        return cast(converted.value());
    }

    @Override
    public List<Object> getPositional() {
        return positional;
    }

    @Override
    public boolean has(String name) {
        return arguments.containsKey(name);
    }

    @Override
    public Map<String, Object> getArguments() {
        return arguments;
    }

    @Override
    public boolean isTerminated() {
        return result.isDone();
    }

    @Override
    public void setStatus(String status) {
        update(blankToNull(status), null, true);
    }

    @Override
    public void setSubStatus(String subStatus) {
        update(null, blankToNull(subStatus), false);
    }

    @Override
    public synchronized void setUninterruptable(boolean uninterruptable) {
        if (closed || result.isDone()) {
            return;
        }
        result.uninterruptable(uninterruptable || forcedUninterruptable);
    }

    @Override
    public boolean isUninterruptable() {
        return result.isUninterruptable();
    }

    /**
     * Stores the line that was set and tells the run. Under the lock, so the listeners see the changes in the order they
     * were made. The two lines are independent: setting one leaves the other as it was.
     */
    private synchronized void update(String status, String subStatus, boolean main) {
        if (closed || result.isDone()) {
            return;
        }
        if (main) {
            this.status = status;
        } else {
            this.subStatus = subStatus;
        }
        result.status(this.status, this.subStatus);
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text;
    }

    @Override
    public synchronized void setReturn(Object value) {
        if (closed) {
            return;
        }
        if (async) {
            closed = true;
            result.complete(value);
        } else {
            this.value = value;
        }
    }

    @Override
    public synchronized void fail(Throwable error) {
        if (closed) {
            return;
        }
        closed = true;
        result.completeExceptionally(error instanceof CommandException ? error : new CommandException(commandName, error));
    }

    /**
     * The method of a command that is not async returned: its value (null if it set none) is the result and what comes
     * later is ignored. Nothing to do when it failed, and the async ones answer by themselves.
     */
    synchronized void finish() {
        if (closed) {
            return;
        }
        closed = true;
        result.complete(value);
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object value) {
        return (T) value;
    }

}
