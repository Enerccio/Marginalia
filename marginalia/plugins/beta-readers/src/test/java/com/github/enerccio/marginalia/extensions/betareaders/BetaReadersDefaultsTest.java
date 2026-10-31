package com.github.enerccio.marginalia.extensions.betareaders;

import com.github.enerccio.marginalia.extensions.betareaders.model.BetaReadersDefaults;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaReadersSettings;
import com.github.enerccio.marginalia.extensions.betareaders.model.Medium;
import com.github.enerccio.marginalia.extensions.betareaders.model.Persona;
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
        List<Persona> personas = BetaReadersDefaults.starterPersonas();

        assertEquals(List.of("Athena", "Vesper", "Ember", "Quill", "Nox", "Sable"), personas.stream().map(Persona::getName).toList());
        assertTrue(personas.stream().allMatch(p -> !p.getDescription().isBlank()));
        assertEquals(personas.size(), personas.stream().map(Persona::getUuid).distinct().count());
        assertTrue(personas.stream().allMatch(p -> p.getIcon() == null));
        // another call makes other personas, a library never shares a uuid with another one
        Set<String> second = new HashSet<>(BetaReadersDefaults.starterPersonas().stream().map(Persona::getUuid).toList());
        assertTrue(personas.stream().noneMatch(p -> second.contains(p.getUuid())));
    }

    @Test
    void theBuiltInMediumsHaveFixedUniqueIdsAndTheDefaultIsAmongThem() {
        List<Medium> mediums = BetaReadersDefaults.builtInMediums();

        assertEquals(List.of("book", "movie", "series", "manga", "audio-drama", "stage-play", "game"),
                mediums.stream().map(Medium::getId).toList());
        assertTrue(mediums.stream().allMatch(m -> !m.getName().isBlank() && !m.getPrompt().isBlank()));
        assertTrue(mediums.stream().anyMatch(m -> m.getId().equals(BetaReadersDefaults.DEFAULT_MEDIUM)));
    }

    @Test
    void bothPromptsHaveTheMediumSection() {
        for (String prompt : List.of(BetaReadersDefaults.SINGLE_PROMPT, BetaReadersDefaults.GROUP_PROMPT)) {
            assertTrue(prompt.contains("{{#medium}}") && prompt.contains("{{.}}") && prompt.contains("{{/medium}}"));
        }
        assertNotEquals(BetaReadersDefaults.SINGLE_PROMPT, BetaReadersDefaults.GROUP_PROMPT);
    }

    @Test
    void newSettingsHaveTheLibrariesAndNothingToOverride() {
        BetaReadersSettings settings = BetaReadersDefaults.newSettings();

        assertEquals(6, settings.getPersonas().size());
        assertEquals(7, settings.getMediums().size());
        assertEquals("", settings.getSinglePrompt());
        assertEquals("", settings.getGroupPrompt());
        assertEquals("", settings.getMedium());
        assertEquals("", settings.getAi());
        assertEquals("", settings.getProtocol());
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
