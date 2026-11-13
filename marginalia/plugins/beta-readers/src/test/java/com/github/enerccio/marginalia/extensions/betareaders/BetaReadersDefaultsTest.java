package com.github.enerccio.marginalia.extensions.betareaders;

import com.github.enerccio.marginalia.extensions.betareaders.model.*;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What the plugin starts with is complete: every persona and medium is usable, the ids and uuids are unique and the prompts have the
 * medium section.
 */
class BetaReadersDefaultsTest {

    @Test
    void theStarterPersonasHaveANameAndADescriptionAndAUuidOfTheirOwn() {
        List<Persona> personas = new BetaReadersDefaults().starterPersonas();

        assertEquals(List.of("Athena", "Vesper", "Ember", "Quill", "Nox", "Sable"), personas.stream().map(Persona::getName).toList());
        assertTrue(personas.stream().allMatch(p -> !p.getDescription().isBlank()));
        assertEquals(personas.size(), personas.stream().map(Persona::getUuid).distinct().count());
        assertTrue(personas.stream().allMatch(p -> p.getIcon() == null));
        // another call makes other personas, a library never shares a uuid with another one
        Set<String> second = new HashSet<>(new BetaReadersDefaults().starterPersonas().stream().map(Persona::getUuid).toList());
        assertTrue(personas.stream().noneMatch(p -> second.contains(p.getUuid())));
    }

    @Test
    void theBuiltInMediumsHaveFixedUniqueIdsAndTheDefaultIsAmongThem() {
        List<Medium> mediums = new BetaReadersDefaults().builtInMediums();

        assertEquals(List.of("book", "movie", "series", "manga", "audio-drama", "stage-play", "game"),
                mediums.stream().map(Medium::getId).toList());
        assertTrue(mediums.stream().allMatch(m -> !m.getName().isBlank() && !m.getPrompt().isBlank()));
        assertTrue(mediums.stream().anyMatch(m -> m.getId().equals(BetaReadersDefaults.DEFAULT_MEDIUM)));
    }

    @Test
    void bothPromptsHaveTheMediumSection() {
        for (String prompt : List.of(new BetaReadersDefaults().singlePrompt(), new BetaReadersDefaults().groupPrompt())) {
            assertTrue(prompt.contains("{{#medium}}") && prompt.contains("{{.}}") && prompt.contains("{{/medium}}"));
        }
        assertNotEquals(new BetaReadersDefaults().singlePrompt(), new BetaReadersDefaults().groupPrompt());
    }

    @Test
    void newSettingsHaveTheLibrariesAndNothingToOverride() {
        BetaReadersSettings settings = new BetaReadersDefaults().newSettings();

        assertEquals(6, settings.getPersonas().size());
        assertEquals(7, settings.getMediums().size());
        assertEquals("", settings.getSinglePrompt());
        assertEquals("", settings.getGroupPrompt());
        assertEquals("", settings.getMedium());
        assertEquals("", settings.getAi());
        assertEquals("", settings.getProtocol());
        assertEquals(0, settings.getMaxMessages());
    }

    @Test
    void theMostMessagesOfATurnComeFromTheChatThenTheUserThenTheBuiltInRule() {
        // nothing set: the built-in rule follows the kind and the size of the group
        assertEquals(3, BetaReadersDefaults.maxMessages(ChatKind.SINGLE, 1, 0, 0));
        assertEquals(8, BetaReadersDefaults.maxMessages(ChatKind.GROUP, 6, 0, 0));
        // the user's limit replaces the rule, whatever the kind
        assertEquals(12, BetaReadersDefaults.maxMessages(ChatKind.SINGLE, 1, 0, 12));
        assertEquals(12, BetaReadersDefaults.maxMessages(ChatKind.GROUP, 6, 0, 12));
        // the chat's own limit wins over the user's
        assertEquals(5, BetaReadersDefaults.maxMessages(ChatKind.GROUP, 6, 5, 12));
        // a negative limit is not set, a huge one is capped
        assertEquals(3, BetaReadersDefaults.maxMessages(ChatKind.SINGLE, 1, -1, -4));
        assertEquals(BetaReadersDefaults.MAX_MESSAGES_LIMIT, BetaReadersDefaults.maxMessages(ChatKind.GROUP, 2, 5000, 0));
        assertEquals(BetaReadersDefaults.MAX_MESSAGES_LIMIT, BetaReadersDefaults.maxMessages(ChatKind.GROUP, 2, 0, 5000));
    }

    @Test
    void aCopyKeepsTheUuidAndADuplicateGetsANewOne() {
        Persona persona = new Persona("Mara", "text");
        persona.setIcon("aWNvbg==");

        Persona copy = persona.copy();
        Persona duplicate = persona.duplicate();

        assertNotSame(persona, copy);
        assertEquals(persona.getUuid(), copy.getUuid());
        assertEquals("aWNvbg==", copy.getIcon());
        assertNotEquals(persona.getUuid(), duplicate.getUuid());
        assertEquals("Mara", duplicate.getName());
        copy.setName("changed");
        assertEquals("Mara", persona.getName());
    }
}
