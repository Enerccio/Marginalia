package com.github.enerccio.marginalia.command;

import com.github.enerccio.marginalia.domain.collections.DestructiveCommandMode;
import com.github.enerccio.marginalia.domain.service.agent.CommandFunctions;
import com.github.enerccio.marginalia.domain.service.command.CommandContext;
import com.github.enerccio.marginalia.domain.service.command.CommandLinks;
import com.github.enerccio.marginalia.domain.service.function.FunctionDefinition;
import com.github.enerccio.marginalia.domain.service.impl.CommandServiceImpl;
import com.github.enerccio.marginalia.domain.traits.Command;
import com.github.enerccio.marginalia.domain.traits.CommandDataSeverity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Commands that an agent gets only together: {@link Command#linkedCommand()}, any number of them, and how the allowlist of
 * an agent is completed and cut by the links.
 */
class LinkedCommandTest {

    private final CommandServiceImpl commands = new CommandServiceImpl();

    @AfterEach
    void tearDown() {
        commands.destroy();
    }

    public static class Trio {
        // an array, and a text with names separated by commas: the same thing
        @Command(commandName = "a", humanNameText = "A", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, linkedCommand = {"b", "c"})
        public void a(CommandContext context) {
            context.setReturn("a");
        }

        @Command(commandName = "b", humanNameText = "B", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, linkedCommand = "a, c")
        public void b(CommandContext context) {
            context.setReturn("b");
        }

        @Command(commandName = "c", humanNameText = "C", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, linkedCommand = "a,b")
        public void c(CommandContext context) {
            context.setReturn("c");
        }

        @Command(commandName = "free", humanNameText = "Free", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE)
        public void free(CommandContext context) {
            context.setReturn("free");
        }
    }

    public static class Needy {
        @Command(commandName = "needy", humanNameText = "Needy", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, linkedCommand = "missing")
        public void needy(CommandContext context) {
            context.setReturn("needy");
        }

        @Command(commandName = "user-only", humanNameText = "User only", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                availableForAgent = false, linkedCommand = "free")
        public void userOnly(CommandContext context) {
            context.setReturn("user");
        }

        @Command(commandName = "wants-user-only", humanNameText = "Wants", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE,
                linkedCommand = "user-only")
        public void wants(CommandContext context) {
            context.setReturn("wants");
        }
    }

    @Test
    void definitionsListEveryLinkedCommand() {
        commands.registerCommands(new Trio());
        assertThat(commands.getCommands("a").getFirst().linkedCommands()).containsExactly("b", "c");
        assertThat(commands.getCommands("b").getFirst().linkedCommands()).containsExactly("a", "c");
        assertThat(commands.getCommands("free").getFirst().linkedCommands()).isEmpty();
    }

    @Test
    void closeAddsAllLinkedCommandsAndKeepsUnknownNames() {
        commands.registerCommands(new Trio());
        assertThat(CommandLinks.close(commands.getCommands(), List.of("b", "gone"))).containsExactly("b", "gone", "a", "c");
        assertThat(CommandLinks.close(commands.getCommands(), List.of("free"))).containsExactly("free");
    }

    @Test
    void resolveGivesTheCommandsTogetherForAnOlderAllowlist() {
        commands.registerCommands(new Trio());
        // an agent saved before the link existed has just one of them
        assertThat(CommandLinks.resolve(commands.getCommands(), List.of("a", "free"))).containsExactlyInAnyOrder("a", "b", "c", "free");
    }

    @Test
    void resolveDropsACommandWhoseLinkedCommandIsNotThere() {
        commands.registerCommands(new Trio());
        commands.registerCommands(new Needy());
        assertThat(CommandLinks.resolve(commands.getCommands(), List.of("needy", "wants-user-only", "user-only", "free", "unknown")))
                .containsExactly("free");
    }

    @Test
    void dependentsAreTheCommandsThatNeedThisOne() {
        commands.registerCommands(new Trio());
        assertThat(CommandLinks.dependents(commands.getCommands(), "a")).containsExactlyInAnyOrder("b", "c");
        assertThat(CommandLinks.dependents(commands.getCommands(), "free")).isEmpty();
    }

    @Test
    void anAgentIsOfferedAllOfThemOrNone() {
        commands.registerCommands(new Trio());
        Set<String> offered = names(CommandFunctions.create(commands, List.of("c"), DestructiveCommandMode.AUTO, l -> "", null));
        assertThat(offered).containsExactlyInAnyOrder("a", "b", "c");

        commands.registerCommands(new Needy());
        assertThat(names(CommandFunctions.create(commands, List.of("needy", "free"), DestructiveCommandMode.AUTO, l -> "", null)))
                .containsExactly("free");
    }

    @Test
    void aCommandCanNotBeLinkedToItself() {
        class Self {
            @Command(commandName = "me", humanNameText = "Me", dataSeverity = CommandDataSeverity.NON_DESTRUCTIVE, linkedCommand = "me")
            public void me(CommandContext context) {
            }
        }
        assertThatThrownBy(() -> commands.registerCommands(new Self())).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("linked command");
    }

    private static Set<String> names(com.github.enerccio.marginalia.domain.service.function.FunctionSet set) {
        return set.getDefinitions().stream().map(FunctionDefinition::getName).collect(java.util.stream.Collectors.toSet());
    }
}
