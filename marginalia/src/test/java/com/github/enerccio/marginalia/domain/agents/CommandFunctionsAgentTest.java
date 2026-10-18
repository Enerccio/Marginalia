package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.bound.AgentRequestAttributes;
import com.github.enerccio.marginalia.domain.collections.DestructiveCommandMode;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.agent.CommandFunctions;
import com.github.enerccio.marginalia.domain.service.command.CommandDefinition;
import com.github.enerccio.marginalia.domain.service.function.FunctionDefinition;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The commands of the application as the functions of an agent: every one of them has a usable function, and a call
 * made on a thread of the run reaches the book of the run.
 */
class CommandFunctionsAgentTest extends AgentTestBase {

    @Autowired
    private CommandService commandService;

    @Autowired
    private Localization localization;

    @Test
    void everyRegisteredCommandIsAFunction() {
        List<String> all = commandService.getCommands().stream().map(CommandDefinition::commandName).distinct().toList();

        FunctionSet set = CommandFunctions.create(commandService, all, DestructiveCommandMode.AUTO, localization::getValue, null);

        assertThat(set.getDefinitions()).hasSameSizeAs(all);
        assertThat(set.getDefinitions()).allSatisfy(definition -> {
            assertThat(definition.getName()).matches("[a-zA-Z0-9_-]{1,64}");
            assertThat(definition.getDescription()).as(definition.getName()).isNotBlank();
            assertThat(definition.getParametersSchema().get("type").asText()).isEqualTo("object");
        });
    }

    @Test
    void aCallOnAThreadOfTheRunReadsTheBookOfTheRun() throws Exception {
        loginAgentUser();
        Agent agent = agent();
        AgentRun run = agentRunService.start(agent);
        FunctionSet set = CommandFunctions.create(commandService, List.of("get-book-info"), DestructiveCommandMode.DENY,
                localization::getValue, null);
        assertThat(set.getDefinitions()).extracting(FunctionDefinition::getName).containsExactly("get-book-info");

        try (AgentRequestAttributes attributes = agentRunService.openSession(run)) {
            // what the inference service does: a thread of its own, in the context of the request
            Executor executor = task -> CompletableFuture.runAsync(() -> {
                try (InRequestScope _ = new InRequestScope(attributes)) {
                    task.run();
                }
            });
            String result = set.call("get-book-info", "{}", executor, () -> false).get(10, TimeUnit.SECONDS);

            assertThat(result).contains(agent.getManuscript().getName());
        }
    }
}
