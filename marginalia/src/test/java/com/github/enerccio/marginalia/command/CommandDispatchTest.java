package com.github.enerccio.marginalia.command;

import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.command.CommandDefinition;
import com.github.enerccio.marginalia.domain.service.impl.CommandServiceImpl;
import com.github.enerccio.marginalia.domain.traits.Command;
import com.github.enerccio.marginalia.domain.traits.CommandParameter;
import com.github.enerccio.marginalia.domain.traits.CommandParameterSignature;
import com.github.enerccio.marginalia.domain.traits.CommandReturn;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link CommandServiceImpl}: overloads told apart by their signature, positional and typed commands, the commands of the
 * application, and stopping a run.
 */
class CommandDispatchTest {

    private final CommandServiceImpl commands = new CommandServiceImpl();

    @AfterEach
    void tearDown() {
        commands.destroy();
    }

    private static Object result(CompletableFuture<Object> future) throws Exception {
        return future.get(5, TimeUnit.SECONDS);
    }

    public static class Overloaded {
        @Command(commandName = "show", humanNameText = "Show", signature = @CommandParameterSignature,
                returns = @CommandReturn(name = "text", type = String.class))
        public void none(CommandContext context) {
            context.setReturn("none");
        }

        @Command(commandName = "show", humanNameText = "Show",
                signature = @CommandParameterSignature(@CommandParameter(name = "text", type = String.class)))
        public void text(CommandContext context) {
            context.setReturn("text " + context.get("text"));
        }

        @Command(commandName = "show", humanNameText = "Show",
                signature = @CommandParameterSignature(@CommandParameter(name = "count", type = Integer.class)))
        public void count(CommandContext context) {
            context.setReturn("count " + context.get("count"));
        }

        @Command(commandName = "show", humanNameText = "Show",
                signature = @CommandParameterSignature({@CommandParameter(name = "a", type = Integer.class),
                        @CommandParameter(name = "b", type = Boolean.class)}))
        public void two(CommandContext context) {
            context.setReturn("two " + context.get("a") + " " + context.get("b"));
        }

        @Command(commandName = "show", humanNameText = "Show")
        public void anything(CommandContext context) {
            context.setReturn("any " + context.getPositional());
        }
    }

    public static class Echo {
        @Command(commandName = "echo", humanNameText = "Echo",
                signature = @CommandParameterSignature(@CommandParameter(name = "value")))
        public void echo(CommandContext context) {
            context.setReturn(context.get("value"));
        }
    }

    @Test
    void theOverloadThatFitsTheNumberOfArgumentsIsCalled() throws Exception {
        commands.registerCommands(new Overloaded());
        assertThat(result(commands.executePositional("show", List.of()))).isEqualTo("none");
        assertThat(result(commands.executePositional("show", List.of(true, 3)))).isEqualTo("any [true, 3]");
        assertThat(result(commands.executePositional("show", List.of("5", "true")))).isEqualTo("two 5 true");
    }

    @Test
    void anArgumentThatAlreadyHasTheTypeBeatsAConvertedOne() throws Exception {
        commands.registerCommands(new Overloaded());
        assertThat(result(commands.executePositional("show", List.of(7)))).isEqualTo("count 7");
        assertThat(result(commands.executePositional("show", List.of("7")))).isEqualTo("text 7");
    }

    @Test
    void whenNoDeclaredFormFitsTheOneThatTakesAnythingIsCalled() throws Exception {
        commands.registerCommands(new Overloaded());
        assertThat(result(commands.executePositional("show", List.of("a", "b", "c")))).isEqualTo("any [a, b, c]");
        assertThat(result(commands.executePositional("show", List.of("x", "notABoolean")))).isEqualTo("any [x, notABoolean]");
    }

    @Test
    void noFormFitsFailsTheCall() {
        commands.registerCommands(new Echo());
        assertThat(commands.executePositional("echo", List.of()))
                .failsWithin(5, TimeUnit.SECONDS).withThrowableOfType(ExecutionException.class)
                .withCauseInstanceOf(IllegalArgumentException.class);
        assertThat(commands.executePositional("echo", List.of("a", "b")))
                .failsWithin(5, TimeUnit.SECONDS).withThrowableOfType(ExecutionException.class)
                .withCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anyTypeAcceptsAListOfObjectsAsItIs() throws Exception {
        commands.registerCommands(new Echo());
        List<Object> list = List.of(1, "two", List.of(3));
        assertThat(result(commands.executePositional("echo", List.of(list)))).isSameAs(list);
    }

    @Test
    void argumentsByNameGoToTheFormWithThoseNames() throws Exception {
        commands.registerCommands(new Overloaded());
        assertThat(result(commands.execute("show", Map.of("count", "12")))).isEqualTo("count 12");
        assertThat(result(commands.execute("show", Map.of("a", 1, "b", false)))).isEqualTo("two 1 false");
        assertThat(result(commands.execute("show", Map.of("other", 1)))).isEqualTo("any []");
    }

    @Test
    void positionalArgumentsAreAlsoReadByName() throws Exception {
        commands.registerCommands(new Echo());
        assertThat(result(commands.executePositional("echo", List.of("x")))).isEqualTo("x");
    }

    @Test
    void theOverloadsAreListedInTheOrderTheyAreTried() {
        commands.registerCommands(new Overloaded());
        List<CommandDefinition> overloads = commands.getCommands("show");
        assertThat(overloads).extracting(d -> d.anyArguments() ? -1 : d.parameters().size())
                .containsExactly(0, 1, 1, 2, -1);
        assertThat(overloads.getFirst().returns().type()).isEqualTo(String.class);
        assertThat(overloads.get(1).parameters().getFirst().name()).isEqualTo("count");
    }

    public static class SameSignatureTwice {
        @Command(commandName = "dup", humanNameText = "Dup", signature = @CommandParameterSignature)
        public void one(CommandContext context) {
        }

        @Command(commandName = "dup", humanNameText = "Dup", signature = @CommandParameterSignature)
        public void two(CommandContext context) {
        }
    }

    public static class DifferentHumanNames {
        @Command(commandName = "names", humanNameText = "One", signature = @CommandParameterSignature)
        public void one(CommandContext context) {
        }

        @Command(commandName = "names", humanNameText = "Two",
                signature = @CommandParameterSignature(@CommandParameter(name = "x")))
        public void two(CommandContext context) {
        }
    }

    public static class ParametersAndAny {
        @Command(commandName = "both", humanNameText = "Both",
                signature = @CommandParameterSignature(value = @CommandParameter(name = "x"), any = true))
        public void run(CommandContext context) {
        }
    }

    public static class ParameterNameTwice {
        @Command(commandName = "twice", humanNameText = "Twice",
                signature = @CommandParameterSignature({@CommandParameter(name = "x"), @CommandParameter(name = "x")}))
        public void run(CommandContext context) {
        }
    }

    @Test
    void invalidSignaturesAreRefused() {
        assertThatThrownBy(() -> commands.registerCommands(new SameSignatureTwice())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> commands.registerCommands(new DifferentHumanNames())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> commands.registerCommands(new ParametersAndAny())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> commands.registerCommands(new ParameterNameTwice())).isInstanceOf(IllegalArgumentException.class);
        assertThat(commands.getCommands()).isEmpty();
    }

    @Test
    void aCommandTypedAsTextIsRunWithTextArguments() throws Exception {
        commands.registerCommands(new Overloaded());
        assertThat(result(commands.executeLine("/show"))).isEqualTo("none");
        assertThat(result(commands.executeLine("/show 4 true"))).isEqualTo("two 4 true");
        assertThat(result(commands.executeLine("/show hello\\ world"))).isEqualTo("text hello world");
        assertThat(commands.executeLine("show"))
                .failsWithin(5, TimeUnit.SECONDS).withThrowableOfType(ExecutionException.class)
                .withCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theCommandsOfTheApplicationAreRegisteredAtStartup() throws Exception {
        commands.afterPropertiesSet();

        assertThat(commands.getCommands("hello-world")).hasSize(2);
        assertThat(result(commands.executeLine("/hello-world"))).isEqualTo("Hello world");
        assertThat(result(commands.executeLine("/hello-world something"))).isEqualTo("something");
        assertThat(result(commands.executeLine("/hello-world a\\ b"))).isEqualTo("a b");
        assertThat(commands.executeLine("/hello-world a b"))
                .failsWithin(5, TimeUnit.SECONDS).withThrowableOfType(ExecutionException.class)
                .withCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theAsynchronousHelloWorldIsStoppedByCancellingTheFuture() throws Exception {
        commands.afterPropertiesSet();

        assertThat(commands.getCommands("hello-world-async")).hasSize(2).allMatch(CommandDefinition::async);
        CompletableFuture<Object> future = commands.executeLine("/hello-world-async hi");
        Thread.sleep(200);
        assertThat(future).isNotDone();

        long start = System.nanoTime();
        future.cancel(true);

        assertThat(future).isCancelled();
        // nothing is waiting for the 10 seconds: the worker was interrupted and is free for the next command
        assertThat(result(commands.executeLine("/hello-world ok"))).isEqualTo("ok");
        assertThat(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)).isLessThan(5_000);
    }

    // ---- stopping a run

    public static class Slow {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch ended = new CountDownLatch(1);
        final AtomicBoolean sawTerminated = new AtomicBoolean();
        final AtomicBoolean ran = new AtomicBoolean();

        @Command(commandName = "slow", humanNameText = "Slow", async = true, signature = @CommandParameterSignature)
        public void run(CommandContext context) {
            ran.set(true);
            started.countDown();
            while (!context.isTerminated()) {
                Thread.onSpinWait();
            }
            sawTerminated.set(true);
            context.setReturn("too late");
            ended.countDown();
        }
    }

    @Test
    void cancellingTheFutureTerminatesTheRunOfAnAsynchronousCommand() throws Exception {
        Slow slow = new Slow();
        commands.registerCommands(slow);

        CompletableFuture<Object> future = commands.execute("slow", null);
        assertThat(slow.started.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(future).isNotDone();

        future.cancel(true);

        assertThat(slow.ended.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(slow.sawTerminated).isTrue();
        assertThat(future).isCancelled();
    }

    public static class Sync {
        boolean terminatedInside;

        @Command(commandName = "sync", humanNameText = "Sync", signature = @CommandParameterSignature)
        public void run(CommandContext context) {
            terminatedInside = context.isTerminated();
            context.setReturn("done");
        }
    }

    @Test
    void aSynchronousCommandIsNeverTerminatedWhileItRuns() throws Exception {
        Sync sync = new Sync();
        commands.registerCommands(sync);
        assertThat(result(commands.execute("sync", null))).isEqualTo("done");
        assertThat(sync.terminatedInside).isFalse();
    }
}
