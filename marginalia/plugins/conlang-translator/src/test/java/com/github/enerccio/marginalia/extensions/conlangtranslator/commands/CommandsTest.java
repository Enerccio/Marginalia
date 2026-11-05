package com.github.enerccio.marginalia.extensions.conlangtranslator.commands;

import com.github.enerccio.marginalia.domain.service.command.CommandDefinition;
import com.github.enerccio.marginalia.domain.service.impl.CommandServiceImpl;
import com.github.enerccio.marginalia.domain.traits.CommandDataSeverity;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.*;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeRuns;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The commands of the plugin: they register (the core checks the annotations, the overloads and the texts when a suite is registered), they follow
 * the conventions of the others, and every operation of a service that the dialogs call has a command (or is listed here as one that is not an action
 * of the user).
 */
class CommandsTest {

    private static final List<Object> SUITES = new ArrayList<>();
    private static final Map<String, CommandDefinition> COMMANDS = new TreeMap<>();

    @BeforeAll
    static void register() {
        CommandServiceImpl service = new CommandServiceImpl();
        TranslationService translation = new TranslationService();
        SUITES.addAll(List.of(new LibraryCommands(), new LexiconCommands(), new TranslationCommands(translation, new BookConlang(translation)),
                new DesignerCommands(new DesignerService())));
        for (Object suite : SUITES) {
            for (CommandDefinition definition : service.registerCommands(suite)) {
                COMMANDS.put(definition.commandName(), definition);
            }
        }
    }

    @Test
    void everyCommandIsPrefixedAndDescribed() {
        assertTrue(COMMANDS.size() > 60, "the plugin has " + COMMANDS.size() + " commands");
        for (CommandDefinition command : COMMANDS.values()) {
            assertTrue(command.commandName().startsWith("conlang-"), command.commandName());
            assertFalse(command.description().isEmpty(), command.commandName() + " has no description");
            if (!command.availableInMacros()) {
                assertFalse(command.llmDescription().isEmpty(), command.commandName() + " has no text for an agent");
            }
        }
    }

    @Test
    void theLinkedCommandsExist() {
        for (CommandDefinition command : COMMANDS.values()) {
            for (String linked : command.linkedCommands()) {
                assertTrue(COMMANDS.containsKey(linked), command.commandName() + " is linked to " + linked + ", which does not exist");
            }
        }
    }

    @Test
    void aCommandThatAsksTheUserIsAsyncAndTheOnesThatCallTheModelToo() {
        // a command that calls context.confirm waits for the answer on a worker thread; the names are the ones that confirm or ask a model
        List<String> mustBeAsync = List.of("conlang-delete", "conlang-delete-rules", "conlang-delete-category", "conlang-delete-register", "conlang-delete-word",
                "conlang-translate-part", "conlang-translate-range", "conlang-revert-to-source", "conlang-translate-text", "conlang-translate-sentence",
                "conlang-design-words", "conlang-design-rules", "conlang-rewrite-rules", "conlang-design-stage", "conlang-ask-designer", "conlang-review",
                "conlang-try-sentences", "conlang-take-pending-changes", "conlang-discard-pending-changes", "conlang-remove-speaker");
        for (String name : mustBeAsync) {
            assertTrue(COMMANDS.get(name).async(), name + " must be async");
        }
    }

    @Test
    void theMacrosOnlyReadAndTheDeletesAreDestructive() {
        for (CommandDefinition command : COMMANDS.values()) {
            if (command.availableInMacros()) {
                assertTrue(command.readOnly(), command.commandName() + " is in macros, it must be read only");
            }
            if (command.readOnly()) {
                assertEquals(CommandDataSeverity.NON_DESTRUCTIVE, command.dataSeverity(), command.commandName() + " reads only");
            }
            if (command.commandName().contains("-delete-") || command.commandName().equals("conlang-delete")) {
                assertEquals(CommandDataSeverity.DESTRUCTIVE, command.dataSeverity(), command.commandName());
            }
        }
        assertEquals(Set.of("conlang-lexicon", "conlang-rules"),
                COMMANDS.values().stream().filter(CommandDefinition::availableInMacros).map(CommandDefinition::commandName).collect(Collectors.toSet()));
    }

    @Test
    void everyConstantOfASuiteIsACommand() throws Exception {
        for (Object suite : SUITES) {
            for (var field : suite.getClass().getFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class && field.getName().matches("[A-Z_]+")) {
                    String name = (String) field.get(null);
                    assertTrue(COMMANDS.containsKey(name), suite.getClass().getSimpleName() + "." + field.getName() + " = " + name + " is not a command");
                }
            }
        }
    }

    /**
     * The operation of a service -> the command that does it, or why there is none. A new public method of one of these services fails the test
     * until it is put here: an action of the user has a command (decided: nothing is UI only).
     */
    private static final Map<String, String> OPERATIONS = Map.ofEntries(
            // LanguageAccess: what the commands of the library read and write
            Map.entry("LanguageAccess.inRun", "internal"),
            Map.entry("LanguageAccess.list", "conlang-list"),
            Map.entry("LanguageAccess.find", "conlang-get"),
            Map.entry("LanguageAccess.resolve", "internal"),
            Map.entry("LanguageAccess.update", "conlang-update"),
            Map.entry("LanguageAccess.add", "conlang-create"),
            Map.entry("LanguageAccess.delete", "conlang-delete"),
            Map.entry("LanguageAccess.run", "conlang-pending-changes"),
            Map.entry("LanguageAccess.changes", "conlang-pending-changes"),
            Map.entry("LanguageAccess.takeOver", "conlang-take-pending-changes"),
            Map.entry("LanguageAccess.forget", "conlang-discard-pending-changes"),
            // TranslationService
            Map.entry("TranslationService.find", "internal"),
            Map.entry("TranslationService.settings", "conlang-status"),
            Map.entry("TranslationService.languageOf", "conlang-status"),
            Map.entry("TranslationService.languagesOf", "conlang-status"),
            Map.entry("TranslationService.translate", "conlang-translate-part"),
            Map.entry("TranslationService.revert", "conlang-revert-to-source"),
            Map.entry("TranslationService.translateSelection", "conlang-translate-sentence"),
            // BookConlang
            Map.entry("BookConlang.languageOf", "conlang-status"),
            Map.entry("BookConlang.languages", "conlang-status"),
            Map.entry("BookConlang.libraryLanguages", "conlang-list"),
            Map.entry("BookConlang.primaryLanguageId", "conlang-status"),
            Map.entry("BookConlang.otherLanguages", "conlang-add-book-language"),
            Map.entry("BookConlang.saveOtherLanguages", "conlang-add-book-language"),
            Map.entry("BookConlang.speakers", "conlang-speakers"),
            Map.entry("BookConlang.saveSpeakers", "conlang-set-speaker"),
            Map.entry("BookConlang.saveWords", "conlang-update-word"),
            Map.entry("BookConlang.runChanges", "conlang-pending-changes"),
            Map.entry("BookConlang.runChangeSet", "conlang-pending-changes"),
            Map.entry("BookConlang.takeOverRunChanges", "conlang-take-pending-changes"),
            Map.entry("BookConlang.discardRunChanges", "conlang-discard-pending-changes"),
            Map.entry("BookConlang.quickTranslate", "conlang-translate-text"),
            Map.entry("BookConlang.importWords", "conlang-translate-text"),
            // DesignerService
            Map.entry("DesignerService.lore", "internal"),
            Map.entry("DesignerService.model", "internal"),
            Map.entry("DesignerService.answer", "conlang-ask-designer"),
            Map.entry("DesignerService.propose", "conlang-design-words"),
            Map.entry("DesignerService.proposal", "conlang-ask-designer"),
            Map.entry("DesignerService.modelName", "internal"),
            Map.entry("DesignerService.samples", "conlang-try-sentences"),
            // the library of the settings screen: the screen's own load / store with the merge of the edits it made
            Map.entry("LanguageLibrary.languages", "conlang-list"),
            Map.entry("LanguageLibrary.defaults", "ui only: the user level of the inherited settings (the panel of the Settings screen)"),
            Map.entry("LanguageLibrary.put", "ui only: the Settings screen stores what it shows"),
            Map.entry("LanguageLibrary.languageChoices", "ui only: the choices of a field"),
            Map.entry("LanguageLibrary.aiChoices", "ui only: the choices of a field"),
            Map.entry("LanguageLibrary.protocolChoices", "ui only: the choices of a field"),
            Map.entry("LanguageLibrary.bookChoices", "ui only: the choices of a field"),
            Map.entry("LanguageLibrary.lorebookChoices", "ui only: the choices of a field"),
            Map.entry("LanguageLibrary.availableAis", "ui only: the choices of a field"),
            Map.entry("LanguageLibrary.availableProtocols", "ui only: the choices of a field"),
            Map.entry("LanguageLibrary.defaultsOrBlank", "ui only: the choices of a field"),
            // the runs that keep a dialog informed
            Map.entry("RangeRuns.isRunning", "ui only: the progress of a dialog"),
            Map.entry("RangeRuns.start", "conlang-translate-range"),
            Map.entry("TranslationRuns.isRunning", "ui only: the progress of a dialog"),
            Map.entry("TranslationRuns.start", "conlang-translate-part"),
            Map.entry("TranslationRuns.startSelection", "conlang-translate-sentence"));

    @Test
    void everyOperationOfAServiceHasACommand() {
        Set<String> seen = new TreeSet<>();
        for (Class<?> service : List.of(LanguageAccess.class, TranslationService.class, BookConlang.class, DesignerService.class, LanguageLibrary.class,
                RangeRuns.class, TranslationRuns.class)) {
            for (Method method : service.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers()) && !method.isSynthetic() && !method.getName().startsWith("lambda$")) {
                    seen.add(service.getSimpleName() + "." + method.getName());
                }
            }
        }
        Set<String> missing = new TreeSet<>(seen);
        missing.removeAll(OPERATIONS.keySet());
        assertTrue(missing.isEmpty(), "These operations have no command and are not listed as one that needs none: " + missing);
        Set<String> gone = new TreeSet<>(OPERATIONS.keySet());
        gone.removeAll(seen);
        assertTrue(gone.isEmpty(), "These operations are listed here and do not exist any more: " + gone);
        OPERATIONS.forEach((operation, command) -> {
            if (command.startsWith("conlang-")) {
                assertTrue(COMMANDS.containsKey(command), operation + " names " + command + ", which does not exist");
            }
        });
    }
}
