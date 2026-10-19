package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.service.AgentRunner;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.command.CommandDefinition;
import com.github.enerccio.marginalia.domain.service.command.CommandReturnDefinition;
import com.github.enerccio.marginalia.domain.service.command.CommandText;
import com.github.enerccio.marginalia.domain.traits.CommandDataSeverity;
import com.github.enerccio.marginalia.loc.L;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The commands the author can run on an agent from its console ({@link AgentRunner#consoleCommands}) as the definitions
 * the command bar shows its list and help from: the registered commands among them, and the commands of the runner
 * itself, which no suite registers because they need a run.
 */
public final class AgentConsoleCommands {

    /**
     * {@code /compact [what to stress]}: takes the rest of the line as its text.
     */
    public static final CommandDefinition COMPACT = new CommandDefinition(AgentRunner.COMPACT,
            CommandText.of(L.LABEL_COMMAND_COMPACT), CommandText.of(L.DESC_AGENT_COMMAND_COMPACT), CommandText.NONE, false,
            CommandDataSeverity.NON_DESTRUCTIVE, true, false, null, true, List.of(),
            new CommandReturnDefinition("", void.class, CommandText.NONE));

    /**
     * {@code /clear-workspace [paths and patterns to keep]}: deletes the rest of the workspace.
     */
    public static final CommandDefinition CLEAR_WORKSPACE = new CommandDefinition(AgentRunner.CLEAR_WORKSPACE,
            CommandText.of(L.LABEL_COMMAND_CLEAR_WORKSPACE), CommandText.of(L.DESC_AGENT_COMMAND_CLEAR_WORKSPACE), CommandText.NONE,
            false, CommandDataSeverity.DESTRUCTIVE, true, false, null, true, List.of(),
            new CommandReturnDefinition("", void.class, CommandText.NONE));

    private AgentConsoleCommands() {
    }

    /**
     * The definitions of the named commands: {@link #COMPACT}, {@link #CLEAR_WORKSPACE} and the forms of the registered ones.
     */
    public static List<CommandDefinition> of(CommandService commandService, List<String> names) {
        Set<String> wanted = Set.copyOf(names);
        List<CommandDefinition> definitions = new ArrayList<>();
        if (wanted.contains(COMPACT.commandName())) {
            definitions.add(COMPACT);
        }
        if (wanted.contains(CLEAR_WORKSPACE.commandName())) {
            definitions.add(CLEAR_WORKSPACE);
        }
        commandService.getCommands().stream().filter(definition -> wanted.contains(definition.commandName()))
                .forEach(definitions::add);
        return definitions;
    }
}
