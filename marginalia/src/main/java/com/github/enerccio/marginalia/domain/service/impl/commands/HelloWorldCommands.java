package com.github.enerccio.marginalia.domain.service.impl.commands;

import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.Command;
import com.github.enerccio.marginalia.domain.traits.CommandParameter;
import com.github.enerccio.marginalia.domain.traits.CommandParameterSignature;
import com.github.enerccio.marginalia.domain.traits.CommandReturn;

/**
 * The first commands of the application, and the example of an overloaded one: {@code /hello-world} answers a greeting,
 * {@code /hello-world something} answers {@code something}. {@code /hello-world-async} does the same after
 * {@value #ASYNC_DELAY_MILLIS} ms, on a worker thread: the example of an asynchronous command that waits in a loop and
 * leaves it at once when it is stopped.
 */
public class HelloWorldCommands {

    /** How long the asynchronous variant waits before it answers. */
    public static final long ASYNC_DELAY_MILLIS = 10_000;

    /** How often the asynchronous variant looks whether it was stopped. */
    private static final long ASYNC_POLL_MILLIS = 50;

    @Command(commandName = "hello-world", humanName = "Hello world",
            description = "Returns what it is given, or a greeting when it is given nothing.",
            llmDescription = "Echoes the argument back. Without an argument answers 'Hello world'. Has no side effects.",
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "message", type = Object.class, description = "'Hello world'"))
    public void greet(CommandContext context) {
        context.setReturn("Hello world");
    }

    @Command(commandName = "hello-world", humanName = "Hello world",
            description = "Returns what it is given, or a greeting when it is given nothing.",
            llmDescription = "Echoes the argument back. Without an argument answers 'Hello world'. Has no side effects.",
            signature = @CommandParameterSignature(
                    @CommandParameter(name = "message", description = "Anything, it is returned as it is")),
            returns = @CommandReturn(name = "message", type = Object.class, description = "The argument"))
    public void echo(CommandContext context) {
        context.setReturn(context.get("message"));
    }

    @Command(commandName = "hello-world-async", humanName = "Hello world (async)", async = true,
            description = "Like hello-world, but answers after 10 seconds, on a worker thread. Can be stopped.",
            llmDescription = "Echoes the argument back after 10 seconds, without an argument answers 'Hello world'. "
                    + "Asynchronous, no side effects.",
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "message", type = Object.class, description = "'Hello world'"))
    public void greetLater(CommandContext context) {
        if (waitUnlessStopped(context)) {
            context.setReturn("Hello world");
        }
    }

    @Command(commandName = "hello-world-async", humanName = "Hello world (async)", async = true,
            description = "Like hello-world, but answers after 10 seconds, on a worker thread. Can be stopped.",
            llmDescription = "Echoes the argument back after 10 seconds, without an argument answers 'Hello world'. "
                    + "Asynchronous, no side effects.",
            signature = @CommandParameterSignature(
                    @CommandParameter(name = "message", description = "Anything, it is returned as it is")),
            returns = @CommandReturn(name = "message", type = Object.class, description = "The argument"))
    public void echoLater(CommandContext context) {
        if (waitUnlessStopped(context)) {
            context.setReturn(context.get("message"));
        }
    }

    /**
     * Waits for {@link #ASYNC_DELAY_MILLIS}, looking every {@link #ASYNC_POLL_MILLIS} ms whether the run was stopped
     * ({@code isTerminated}) or the thread interrupted (the service interrupts it when the caller cancels).
     *
     * @return true when the time passed, false when the command was stopped and has to return at once
     */
    private static boolean waitUnlessStopped(CommandContext context) {
        long end = System.nanoTime() + ASYNC_DELAY_MILLIS * 1_000_000;
        while (System.nanoTime() < end) {
            if (context.isTerminated() || Thread.currentThread().isInterrupted()) {
                return false;
            }
            try {
                Thread.sleep(ASYNC_POLL_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return !context.isTerminated();
    }

}
