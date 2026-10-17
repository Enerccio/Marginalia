package com.github.enerccio.marginalia.command;

import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.command.CommandDefinition;
import com.github.enerccio.marginalia.domain.service.command.CommandParameterDefinition;
import com.github.enerccio.marginalia.domain.traits.CommandDataSeverity;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The commands the application registers use localization keys, and the texts they point to exist.
 */
class BuiltInCommandTextsTest extends MarginaliaTestBase {

    @Autowired
    private CommandService commandService;

    @Autowired
    private Localization loc;

    @Test
    void everyBuiltInCommandShowsLocalizedTexts() {
        assertThat(commandService.getCommands()).isNotEmpty();
        for (CommandDefinition definition : commandService.getCommands()) {
            String name = definition.commandName();
            assertThat(definition.humanName().key()).as("human name of %s", name).isNotNull();
            assertThat(definition.humanName().get(loc)).as("human name of %s", name).isNotBlank();
            assertThat(definition.description().key()).as("description of %s", name).isNotNull();
            assertThat(definition.description().get(loc)).as("description of %s", name).isNotBlank();
            assertThat(definition.llmDescription().key()).as("llm description of %s", name).isNotNull();
            assertThat(definition.llmDescription().get(loc)).as("llm description of %s", name).isNotBlank();
            assertThat(definition.returns().description().get(loc)).as("return of %s", name).isNotBlank();
            for (CommandParameterDefinition parameter : definition.parameters()) {
                assertThat(parameter.description().get(loc)).as("parameter %s of %s", parameter.name(), name).isNotBlank();
            }
        }
    }

    @Test
    void theBuiltInCommandsSayWhatTheyDoToTheData() {
        Map<String, CommandDataSeverity> expected = Map.of(
                "hello-world", CommandDataSeverity.NON_DESTRUCTIVE,
                "hello-world-async", CommandDataSeverity.NON_DESTRUCTIVE,
                "take-backup", CommandDataSeverity.NON_DESTRUCTIVE,
                "search-replace-parts", CommandDataSeverity.DESTRUCTIVE,
                "search-replace-lore-entries", CommandDataSeverity.DESTRUCTIVE);
        for (CommandDefinition definition : commandService.getCommands()) {
            assertThat(expected).as("a built-in command with a declared severity").containsKey(definition.commandName());
            assertThat(definition.dataSeverity()).as("severity of %s", definition.commandName())
                    .isEqualTo(expected.get(definition.commandName()));
            assertThat(definition.dataSeverity().getLabel()).isNotNull();
            assertThat(loc.getValue(definition.dataSeverity().getDescription())).isNotBlank();
        }
    }

}
