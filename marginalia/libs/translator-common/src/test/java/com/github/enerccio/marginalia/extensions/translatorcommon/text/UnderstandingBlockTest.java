package com.github.enerccio.marginalia.extensions.translatorcommon.text;

import com.github.enerccio.marginalia.extensions.translatorcommon.model.CharacterNote;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Speakers;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.UnderstandingLevel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The text that tells the models who follows which language: only a level that says something is written.
 */
class UnderstandingBlockTest {

    private static CharacterNote note(String name, UnderstandingLevel level, String... aliases) {
        CharacterNote note = new CharacterNote(name);
        note.setLevel(level);
        note.getAliases().addAll(List.of(aliases));
        return note;
    }

    private static UnderstandingBlock.Entry entry(String language, CharacterNote... notes) {
        Speakers speakers = new Speakers();
        speakers.getCharacters().addAll(List.of(notes));
        return new UnderstandingBlock.Entry(language, speakers);
    }

    @Test
    void writesTheLevelsThatSaySomething() {
        String block = UnderstandingBlock.build(List.of(entry("Vrakht",
                note("Tomáš", UnderstandingLevel.NONE),
                note("Mara", UnderstandingLevel.FEW_WORDS),
                note("Other villagers", UnderstandingLevel.FOLLOWS_MOST))), null);

        assertTrue(block.startsWith(UnderstandingBlock.HEADER + "\n"));
        assertTrue(block.contains("- Tomáš, Vrakht: understands nothing."));
        assertTrue(block.contains("- Mara, Vrakht: knows only a few words, cannot follow it."));
        assertTrue(block.contains("- Other villagers, Vrakht: follows most of it, misses details."));
    }

    @Test
    void notSaidAndFluentTellTheModelNothing() {
        String block = UnderstandingBlock.build(List.of(entry("Vrakht",
                note("Brog", null), note("Hana", UnderstandingLevel.FLUENT))), null);

        assertEquals("", block);
    }

    @Test
    void noLanguagesNoBlock() {
        assertEquals("", UnderstandingBlock.build(List.of(), null));
        assertEquals("", UnderstandingBlock.build(null, "Mara"));
    }

    @Test
    void severalLanguagesKeepTheirOwnLines() {
        String block = UnderstandingBlock.build(List.of(
                entry("Vrakht", note("Tomáš", UnderstandingLevel.NONE)),
                entry("Japanese", note("Tomáš", UnderstandingLevel.FEW_WORDS))), "");

        assertTrue(block.contains("- Tomáš, Vrakht: understands nothing."));
        assertTrue(block.contains("- Tomáš, Japanese: knows only a few words, cannot follow it."));
    }

    @Test
    void theCharactersFilterMatchesNamesAndAliases() {
        List<UnderstandingBlock.Entry> entries = List.of(entry("Vrakht",
                note("Tomáš", UnderstandingLevel.NONE, "Thomas"),
                note("Mara", UnderstandingLevel.NONE)));

        String block = UnderstandingBlock.build(entries, "thomas, Somebody");

        assertTrue(block.contains("Tomáš"));
        assertFalse(block.contains("Mara"));
        assertEquals("", UnderstandingBlock.build(entries, "Nobody"));
    }

    @Test
    void levelNamesOfTheCommands() {
        assertNull(UnderstandingLevel.parse(null));
        assertNull(UnderstandingLevel.parse("  "));
        assertEquals(UnderstandingLevel.FEW_WORDS, UnderstandingLevel.parse("few-words"));
        assertEquals(UnderstandingLevel.FOLLOWS_MOST, UnderstandingLevel.parse(" Follows_Most "));
        for (UnderstandingLevel level : UnderstandingLevel.values()) {
            assertEquals(level, UnderstandingLevel.parse(level.key()));
        }
        assertThrows(IllegalArgumentException.class, () -> UnderstandingLevel.parse("a lot"));
    }
}
