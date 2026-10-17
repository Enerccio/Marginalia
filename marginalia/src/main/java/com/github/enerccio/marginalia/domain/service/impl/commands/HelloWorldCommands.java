package com.github.enerccio.marginalia.domain.service.impl.commands;

import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.traits.Command;
import com.github.enerccio.marginalia.domain.traits.CommandDataSeverity;
import com.github.enerccio.marginalia.domain.traits.CommandParameter;
import com.github.enerccio.marginalia.domain.traits.CommandParameterSignature;
import com.github.enerccio.marginalia.domain.traits.CommandReturn;
import com.github.enerccio.marginalia.loc.L;

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

    @Command(commandName = "hello-world", humanName = L.LABEL_COMMAND_HELLO_WORLD, description = L.DESC_COMMAND_HELLO_WORLD,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            llmDescription = L.DESC_COMMAND_HELLO_WORLD_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "message", type = Object.class, description = L.DESC_COMMAND_HELLO_WORLD_GREETING_RETURN))
    public void greet(CommandContext context) {
        context.setReturn("Hello world");
    }

    @Command(commandName = "hello-world", humanName = L.LABEL_COMMAND_HELLO_WORLD, description = L.DESC_COMMAND_HELLO_WORLD,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            llmDescription = L.DESC_COMMAND_HELLO_WORLD_LLM,
            signature = @CommandParameterSignature(
                    @CommandParameter(name = "message", description = L.DESC_COMMAND_HELLO_WORLD_MESSAGE_PARAM)),
            returns = @CommandReturn(name = "message", type = Object.class, description = L.DESC_COMMAND_HELLO_WORLD_ECHO_RETURN))
    public void echo(CommandContext context) {
        context.setReturn(context.get("message"));
    }

    @Command(commandName = "hello-world-async", humanName = L.LABEL_COMMAND_HELLO_WORLD_ASYNC, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_HELLO_WORLD_ASYNC, llmDescription = L.DESC_COMMAND_HELLO_WORLD_ASYNC_LLM,
            signature = @CommandParameterSignature,
            returns = @CommandReturn(name = "message", type = Object.class, description = L.DESC_COMMAND_HELLO_WORLD_GREETING_RETURN))
    public void greetLater(CommandContext context) {
        if (waitUnlessStopped(context)) {
            context.setReturn("Hello world");
        }
    }

    @Command(commandName = "hello-world-async", humanName = L.LABEL_COMMAND_HELLO_WORLD_ASYNC, async = true,
            dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
            description = L.DESC_COMMAND_HELLO_WORLD_ASYNC, llmDescription = L.DESC_COMMAND_HELLO_WORLD_ASYNC_LLM,
            signature = @CommandParameterSignature(
                    @CommandParameter(name = "message", description = L.DESC_COMMAND_HELLO_WORLD_MESSAGE_PARAM)),
            returns = @CommandReturn(name = "message", type = Object.class, description = L.DESC_COMMAND_HELLO_WORLD_ECHO_RETURN))
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
