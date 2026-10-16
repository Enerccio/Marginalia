package com.github.enerccio.marginalia.command;

import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.command.CommandDefinition;
import com.github.enerccio.marginalia.domain.service.command.CommandException;
import com.github.enerccio.marginalia.domain.service.impl.CommandServiceImpl;
import com.github.enerccio.marginalia.domain.traits.Command;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link CommandService}: registration of command suites ({@link Command} methods) and running the commands.
 */
class CommandServiceTest {

    private final CommandServiceImpl commands = new CommandServiceImpl();

    @AfterEach
    void tearDown() {
        commands.destroy();
    }

    public static class Arithmetic {
        @Command(commandName = "add", humanName = "Add", description = "Adds", llmDescription = "Adds two numbers")
        public void add(CommandContext context) {
            context.setReturn(context.get("a", Integer.class) + context.get("b", Integer.class));
        }

        @Command(commandName = "negate", humanName = "Negate")
        public void negate(CommandContext context) {
            context.setReturn(-context.get("a", Integer.class));
        }

        public void notACommand(CommandContext context) {
            context.setReturn("should never be reachable");
        }
    }

    public static class Silent {
        @Command(commandName = "silent", humanName = "Silent")
        public void run(CommandContext context) {
            // sets nothing
        }
    }

    public static class Late {
        CommandContext kept;

        @Command(commandName = "late", humanName = "Late")
        public void run(CommandContext context) {
            kept = context;
            context.setReturn("first");
            context.setReturn("second");
        }
    }

    public static class Boom {
        @Command(commandName = "boom", humanName = "Boom")
        public void run(CommandContext context) {
            throw new IllegalStateException("bang");
        }
    }

    public static class Later {
        public final CompletableFuture<Void> release = new CompletableFuture<>();
        volatile String thread;

        @Command(commandName = "later", humanName = "Later", async = true)
        public void run(CommandContext context) {
            thread = Thread.currentThread().getName();
            new Thread(() -> {
                release.join();
                context.setReturn("done " + context.get("who"));
            }).start();
        }

        @Command(commandName = "later-fails", humanName = "Later fails", async = true)
        public void fails(CommandContext context) {
            new Thread(() -> context.fail(new IllegalStateException("async bang"))).start();
        }
    }

    public static class Inherited extends Arithmetic {
    }

    public static class NoCommands {
        public void run(CommandContext context) {
        }
    }

    public static class WrongSignature {
        @Command(commandName = "ok", humanName = "Ok")
        public void ok(CommandContext context) {
        }

        @Command(commandName = "wrong", humanName = "Wrong")
        public String wrong(CommandContext context) {
            return "x";
        }
    }

    public static class NotPublic {
        @Command(commandName = "hidden", humanName = "Hidden")
        void hidden(CommandContext context) {
        }
    }

    public static class BadName {
        @Command(commandName = "bad name!", humanName = "Bad")
        public void run(CommandContext context) {
        }
    }

    public static class Twice {
        @Command(commandName = "same", humanName = "One")
        public void one(CommandContext context) {
        }

        @Command(commandName = "same", humanName = "Two")
        public void two(CommandContext context) {
        }
    }

    public static class AddAgain {
        @Command(commandName = "fresh", humanName = "Fresh")
        public void fresh(CommandContext context) {
        }

        @Command(commandName = "add", humanName = "Another add")
        public void add(CommandContext context) {
        }
    }

    private CommandDefinition firstOf(String name) {
        List<CommandDefinition> overloads = commands.getCommands(name);
        return overloads.isEmpty() ? null : overloads.getFirst();
    }

    private static Object result(CompletableFuture<Object> future) throws Exception {
        return future.get(5, TimeUnit.SECONDS);
    }

    @Test
    void registeringASuiteRegistersAllItsCommands() {
        Arithmetic suite = new Arithmetic();
        List<CommandDefinition> definitions = commands.registerCommands(suite);

        assertThat(definitions).extracting(CommandDefinition::commandName).containsExactly("add", "negate");
        CommandDefinition add = firstOf("add");
        assertThat(add.humanName()).isEqualTo("Add");
        assertThat(add.description()).isEqualTo("Adds");
        assertThat(add.llmDescription()).isEqualTo("Adds two numbers");
        assertThat(add.async()).isFalse();
        assertThat(add.instance()).isSameAs(suite);
        assertThat(commands.getCommands()).containsExactlyInAnyOrderElementsOf(definitions);
        assertThat(firstOf("notACommand")).isNull();
    }

    @Test
    void inheritedCommandsAreRegistered() throws Exception {
        commands.registerCommands(new Inherited());
        assertThat(result(commands.execute("negate", Map.of("a", 4)))).isEqualTo(-4);
    }

    @Test
    void registeringTheSameSuiteAgainChangesNothing() {
        Arithmetic suite = new Arithmetic();
        commands.registerCommands(suite);
        commands.registerCommands(suite);
        assertThat(commands.getCommands()).hasSize(2);
    }

    @Test
    void invalidSuitesAreRefusedWholeAndLeaveNothingBehind() {
        assertThatThrownBy(() -> commands.registerCommands(new NoCommands())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> commands.registerCommands(new WrongSignature())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> commands.registerCommands(new NotPublic())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> commands.registerCommands(new BadName())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> commands.registerCommands(new Twice())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> commands.registerCommands(null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(commands.getCommands()).isEmpty();
    }

    @Test
    void anotherSuiteCannotTakeANameThatIsInUseAndRegistersNothing() {
        commands.registerCommands(new Arithmetic());
        assertThatThrownBy(() -> commands.registerCommands(new AddAgain())).isInstanceOf(IllegalArgumentException.class);
        assertThat(firstOf("fresh")).isNull();
        assertThat(commands.getCommands()).hasSize(2);
    }

    @Test
    void unregisterBySuiteAndByName() {
        Arithmetic suite = new Arithmetic();
        commands.registerCommands(suite);
        commands.registerCommands(new Silent());

        assertThat(commands.unregisterCommand("negate")).isTrue();
        assertThat(commands.unregisterCommand("negate")).isFalse();
        assertThat(firstOf("add")).isNotNull();

        commands.unregisterCommands(suite);
        assertThat(firstOf("add")).isNull();
        assertThat(commands.getCommands()).extracting(CommandDefinition::commandName).containsExactly("silent");
    }

    @Test
    void commandsAreListedInTheOrderOfRegistration() {
        commands.registerCommands(new Silent());
        commands.registerCommands(new Arithmetic());
        assertThat(commands.getCommands()).extracting(CommandDefinition::commandName)
                .containsExactly("silent", "add", "negate");
    }

    @Test
    void synchronousCommandIsFinishedWhenExecuteReturns() throws Exception {
        commands.registerCommands(new Arithmetic());
        CompletableFuture<Object> future = commands.execute("add", Map.of("a", 2, "b", 3));
        assertThat(future).isCompleted();
        assertThat(result(future)).isEqualTo(5);
    }

    @Test
    void numbersAreConvertedToTheTypeAsked() throws Exception {
        commands.registerCommands(new Arithmetic());
        assertThat(result(commands.execute("add", Map.of("a", 2L, "b", 3.0)))).isEqualTo(5);
        assertThat(commands.execute("add", Map.of("a", 2.5, "b", 3)))
                .failsWithin(5, TimeUnit.SECONDS).withThrowableOfType(ExecutionException.class)
                .withCauseInstanceOf(CommandException.class);
    }

    @Test
    void synchronousCommandThatSetsNothingReturnsNull() throws Exception {
        commands.registerCommands(new Silent());
        assertThat(result(commands.execute("silent", null))).isNull();
    }

    @Test
    void firstReturnWinsAndTheContextIsDeadAfterwards() throws Exception {
        Late late = new Late();
        commands.registerCommands(late);
        CompletableFuture<Object> future = commands.execute("late", null);
        assertThat(result(future)).isEqualTo("second");
        late.kept.setReturn("after");
        assertThat(result(future)).isEqualTo("second");
    }

    @Test
    void thrownExceptionFailsTheFutureWithTheCause() {
        commands.registerCommands(new Boom());
        assertThat(commands.execute("boom", null))
                .failsWithin(5, TimeUnit.SECONDS).withThrowableOfType(ExecutionException.class)
                .havingCause().isInstanceOf(CommandException.class)
                .withCauseInstanceOf(IllegalStateException.class)
                .withMessageContaining("bang");
    }

    @Test
    void unknownCommandFails() {
        assertThat(commands.execute("nothing", null))
                .failsWithin(5, TimeUnit.SECONDS).withThrowableOfType(ExecutionException.class)
                .withCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void asynchronousCommandAnswersWhenItLikesOnAnotherThread() throws Exception {
        Later later = new Later();
        commands.registerCommands(later);

        CompletableFuture<Object> future = commands.execute("later", Map.of("who", "me"));
        assertThat(future).isNotCompleted();
        later.release.complete(null);

        assertThat(result(future)).isEqualTo("done me");
        assertThat(later.thread).startsWith("command-");
        assertThat(later.thread).isNotEqualTo(Thread.currentThread().getName());
    }

    @Test
    void asynchronousCommandCanFailFromAnotherThread() {
        commands.registerCommands(new Later());
        assertThat(commands.execute("later-fails", null))
                .failsWithin(5, TimeUnit.SECONDS).withThrowableOfType(ExecutionException.class)
                .withCauseInstanceOf(CommandException.class);
    }

    @Test
    void argumentsAreReadOnlyAndMayBeMissing() throws Exception {
        CommandContext[] seen = new CommandContext[1];
        class Peek {
            @Command(commandName = "peek", humanName = "Peek")
            public void run(CommandContext context) {
                seen[0] = context;
            }
        }
        commands.registerCommands(new Peek());
        result(commands.execute("peek", Map.of("x", "y")));

        assertThat(seen[0].get("x")).isEqualTo("y");
        assertThat(seen[0].has("x")).isTrue();
        assertThat(seen[0].get("missing")).isNull();
        assertThat(seen[0].has("missing")).isFalse();
        assertThat(seen[0].get("missing", Integer.class)).isNull();
        assertThatThrownBy(() -> seen[0].getArguments().put("z", 1)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> seen[0].get("x", Integer.class)).isInstanceOf(IllegalArgumentException.class);
    }
}
