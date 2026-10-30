package com.github.enerccio.marginalia.command;

import com.github.enerccio.marginalia.domain.service.CommandService;
import com.github.enerccio.marginalia.domain.service.command.CommandDefinition;
import com.github.enerccio.marginalia.domain.service.command.CommandParameterDefinition;
import com.github.enerccio.marginalia.domain.traits.CommandDataSeverity;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
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
            if (definition.availableForAgent()) {
                assertThat(definition.llmDescription().key()).as("llm description of %s", name).isNotNull();
                assertThat(definition.llmDescription().get(loc)).as("llm description of %s", name).isNotBlank();
            }
            assertThat(definition.returns().description().get(loc)).as("return of %s", name).isNotBlank();
            for (CommandParameterDefinition parameter : definition.parameters()) {
                assertThat(parameter.description().get(loc)).as("parameter %s of %s", parameter.name(), name).isNotBlank();
            }
        }
    }

    @Test
    void theBuiltInCommandsSayWhatTheyDoToTheData() {
        Map<String, CommandDataSeverity> expected = new HashMap<>(Map.of(
                "take-backup", CommandDataSeverity.NON_DESTRUCTIVE,
                "search-replace-parts", CommandDataSeverity.DESTRUCTIVE,
                "search-replace-lore-entries", CommandDataSeverity.DESTRUCTIVE));
        // the commands that read the book change nothing
        for (String name : List.of("get-book-info", "list-parts", "get-part", "list-swipes", "list-branches",
                "get-part-metrics", "show-prompt", "search-parts", "find-occurrences", "compare-parts", "count-tokens",
                "list-summaries", "get-summary", "check-summaries", "get-template-variables",
                "list-lorebooks", "list-lore-entries", "get-lore-entry", "search-lore", "preview-lore-activation",
                "list-annotations", "get-annotation")) {
            expected.put(name, CommandDataSeverity.NON_DESTRUCTIVE);
        }
        // the commands that change the book: what they do to the text that exists decides (adding is not destructive)
        for (String name : List.of("edit-part", "append-to-part", "set-part-fields", "create-part", "insert-part", "branch-part", "switch-branch",
                "set-summary", "revalidate-summaries", "regenerate-summary", "create-meta-summary", "next", "swipe",
                "set-book-info", "set-book-tags", "set-book-history-limit", "create-lore-entry", "update-lore-entry", "move-lore-entry",
                "set-lore-entry-tags", "create-lorebook", "set-lorebook", "add-subbook", "remove-subbook",
                "create-annotation", "update-annotation", "resolve-annotation", "reattach-annotation", "generate-annotation", "impersonate")) {
            expected.put(name, CommandDataSeverity.NON_DESTRUCTIVE);
        }
        for (String name : List.of("replace-part-text", "delete-part", "remove-summary", "regenerate", "redraft-part", "set-book-prompts",
                "delete-lore-entry", "delete-lorebook", "delete-annotation")) {
            expected.put(name, CommandDataSeverity.DESTRUCTIVE);
        }
        for (CommandDefinition definition : commandService.getCommands()) {
            assertThat(expected).as("a built-in command with a declared severity").containsKey(definition.commandName());
            assertThat(definition.dataSeverity()).as("severity of %s", definition.commandName())
                    .isEqualTo(expected.get(definition.commandName()));
            assertThat(definition.dataSeverity().getLabel()).isNotNull();
            assertThat(loc.getValue(definition.dataSeverity().getDescription())).isNotBlank();
        }
    }

}
