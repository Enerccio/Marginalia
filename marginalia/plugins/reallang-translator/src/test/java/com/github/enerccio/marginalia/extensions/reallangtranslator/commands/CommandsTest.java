package com.github.enerccio.marginalia.extensions.reallangtranslator.commands;

import com.github.enerccio.marginalia.domain.service.command.CommandDefinition;
import com.github.enerccio.marginalia.domain.service.impl.CommandServiceImpl;
import com.github.enerccio.marginalia.domain.traits.CommandDataSeverity;
import com.github.enerccio.marginalia.extensions.reallangtranslator.service.*;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.RangeRuns;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The commands of the plugin: they register (the core checks the annotations, the overloads and the texts when a suite is registered), they follow the
 * conventions of the others, and every operation of a service that the dialogs call has a command (or is listed here as one that is not an action of
 * the user).
 */
class CommandsTest {

    private static final Object SUITE;
    private static final Map<String, CommandDefinition> COMMANDS = new TreeMap<>();

    static {
        LanguageLibrary library = new LanguageLibrary();
        GlossaryService glossary = new GlossaryService(library);
        TranslationService translation = new TranslationService(glossary);
        SUITE = new ReallangCommands(translation, new BookReal(translation, glossary), library);
    }

    @BeforeAll
    static void register() {
        for (CommandDefinition definition : new CommandServiceImpl().registerCommands(SUITE)) {
            COMMANDS.put(definition.commandName(), definition);
        }
    }

    @Test
    void everyCommandIsPrefixedAndDescribed() {
        assertTrue(COMMANDS.size() >= 15, "the plugin has " + COMMANDS.size() + " commands");
        for (CommandDefinition command : COMMANDS.values()) {
            assertTrue(command.commandName().startsWith("reallang-"), command.commandName());
            assertFalse(command.description().isEmpty(), command.commandName() + " has no description");
            assertFalse(command.llmDescription().isEmpty(), command.commandName() + " has no text for an agent");
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
    void aCommandThatAsksTheUserOrCallsTheModelIsAsync() {
        for (String name : List.of("reallang-delete", "reallang-translate-part", "reallang-translate-range", "reallang-revert-to-source", "reallang-translate-text",
                "reallang-translate-sentence", "reallang-remove-speaker")) {
            assertTrue(COMMANDS.get(name).async(), name + " must be async");
        }
    }

    @Test
    void theReadsReadOnlyAndTheDestructiveOnesSayIt() {
        for (CommandDefinition command : COMMANDS.values()) {
            if (command.readOnly()) {
                assertEquals(CommandDataSeverity.NON_DESTRUCTIVE, command.dataSeverity(), command.commandName() + " reads only");
            }
        }
        for (String name : List.of("reallang-delete", "reallang-remove-speaker", "reallang-revert-to-source", "reallang-translate-range")) {
            assertEquals(CommandDataSeverity.DESTRUCTIVE, COMMANDS.get(name).dataSeverity(), name);
        }
        for (String name : List.of("reallang-list", "reallang-status", "reallang-speakers", "reallang-get-source", "reallang-list-passages")) {
            assertTrue(COMMANDS.get(name).readOnly(), name + " only reads");
        }
    }

    @Test
    void everyConstantOfTheSuiteIsACommand() throws Exception {
        for (var field : SUITE.getClass().getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class && field.getName().matches("[A-Z_]+")) {
                String name = (String) field.get(null);
                assertTrue(COMMANDS.containsKey(name), field.getName() + " = " + name + " is not a command");
            }
        }
    }

    /**
     * The operation of a service -> the command that does it, or why there is none. A new public method of one of these services fails the test until
     * it is put here: an action of the user has a command (decided: nothing is UI only).
     */
    private static final Map<String, String> OPERATIONS = Map.ofEntries(
            Map.entry("TranslationService.find", "internal"),
            Map.entry("TranslationService.settings", "reallang-status"),
            Map.entry("TranslationService.languageOf", "reallang-status"),
            Map.entry("TranslationService.languagesOf", "reallang-status"),
            Map.entry("TranslationService.library", "reallang-list"),
            Map.entry("TranslationService.translate", "reallang-translate-part"),
            Map.entry("TranslationService.revert", "reallang-revert-to-source"),
            Map.entry("TranslationService.translateSelection", "reallang-translate-sentence"),
            Map.entry("TranslationService.quickTranslate", "reallang-translate-text"),
            Map.entry("BookReal.languageOf", "reallang-status"),
            Map.entry("BookReal.languages", "reallang-status"),
            Map.entry("BookReal.libraryLanguages", "reallang-list"),
            Map.entry("BookReal.primaryLanguageId", "reallang-status"),
            Map.entry("BookReal.otherLanguages", "reallang-add-book-language"),
            Map.entry("BookReal.saveOtherLanguages", "reallang-add-book-language"),
            Map.entry("BookReal.speakers", "reallang-speakers"),
            Map.entry("BookReal.saveSpeakers", "reallang-set-speaker"),
            Map.entry("BookReal.quickTranslate", "reallang-translate-text"),
            Map.entry("BookReal.glossary", "reallang-glossary"),
            Map.entry("BookReal.glossaryFolders", "reallang-glossary"),
            Map.entry("BookReal.putGlossaryEntry", "reallang-glossary-set"),
            Map.entry("BookReal.removeGlossaryEntry", "reallang-glossary-remove"),
            Map.entry("BookReal.acceptGlossaryEntry", "reallang-glossary-accept"),
            Map.entry("BookReal.moveGlossaryEntry", "reallang-glossary-move"),
            Map.entry("BookReal.switchGlossaryEntry", "reallang-glossary-switch"),
            Map.entry("GlossaryService.layers", "reallang-glossary"),
            Map.entry("GlossaryService.folders", "reallang-glossary"),
            Map.entry("GlossaryService.effective", "reallang-glossary"),
            Map.entry("GlossaryService.active", "reallang-glossary"),
            Map.entry("GlossaryService.entries", "reallang-glossary"),
            Map.entry("GlossaryService.put", "reallang-glossary-set"),
            Map.entry("GlossaryService.remove", "reallang-glossary-remove"),
            Map.entry("GlossaryService.replace", "ui only: the language editor stores its own list"),
            Map.entry("GlossaryService.moveFromBook", "reallang-glossary-move"),
            Map.entry("GlossaryService.accept", "reallang-glossary-accept"),
            Map.entry("GlossaryService.propose", "internal: the translation of a part proposes the names the model chose"),
            Map.entry("LanguageLibrary.languages", "reallang-list"),
            Map.entry("LanguageLibrary.resolve", "internal"),
            Map.entry("LanguageLibrary.store", "reallang-create"),
            Map.entry("LanguageLibrary.remove", "reallang-delete"),
            Map.entry("LanguageLibrary.defaults", "ui only: the user level of the inherited settings (the panel of the Settings screen)"),
            Map.entry("LanguageLibrary.put", "ui only: the Settings screen stores what it shows"),
            Map.entry("LanguageLibrary.languageChoices", "ui only: the choices of a field"),
            Map.entry("LanguageLibrary.aiChoices", "ui only: the choices of a field"),
            Map.entry("LanguageLibrary.protocolChoices", "ui only: the choices of a field"),
            Map.entry("LanguageLibrary.availableAis", "ui only: the choices of a field"),
            Map.entry("LanguageLibrary.availableProtocols", "ui only: the choices of a field"),
            Map.entry("LanguageLibrary.defaultsOrBlank", "ui only: the choices of a field"),
            Map.entry("RangeRuns.isRunning", "ui only: the progress of a dialog"),
            Map.entry("RangeRuns.start", "reallang-translate-range"),
            Map.entry("TranslationRuns.isRunning", "ui only: the progress of a dialog"),
            Map.entry("TranslationRuns.start", "reallang-translate-part"),
            Map.entry("TranslationRuns.startSelection", "reallang-translate-sentence"));

    @Test
    void everyOperationOfAServiceHasACommand() {
        Set<String> seen = new TreeSet<>();
        for (Class<?> service : List.of(TranslationService.class, BookReal.class, GlossaryService.class, LanguageLibrary.class, RangeRuns.class, TranslationRuns.class)) {
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
            if (command.startsWith("reallang-")) {
                assertTrue(COMMANDS.containsKey(command), operation + " names " + command + ", which does not exist");
            }
        });
    }
}
