package com.github.enerccio.marginalia.extensions.knowledgetracker.scene;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * How the present characters field is read: leniently enough to find the names, strictly enough not to find a name
 * that is not there.
 */
class PresentCharactersTest {

    private static List<String> parse(String text) {
        return PresentCharacters.parse(text, null);
    }

    @Test
    void emptyAndBlankGiveNothing() {
        assertEquals(List.of(), parse(null));
        assertEquals(List.of(), parse(""));
        assertEquals(List.of(), parse("  \n ,; "));
    }

    @Test
    void splitsOnTheDefaultSeparators() {
        assertEquals(List.of("Mara", "Thomas", "Fiora", "Ilse", "Joss", "Kai", "Noor"),
                parse("Mara, Thomas; Fiora\nIlse/Joss|Kai、Noor"));
    }

    @Test
    void splitsOnFullwidthSeparators() {
        assertEquals(List.of("マラ", "トーマス", "フィオラ", "イルセ"), parse("マラ、トーマス，フィオラ；イルセ"));
    }

    @Test
    void handlesWindowsAndOldLineEndings() {
        assertEquals(List.of("Mara", "Thomas", "Fiora"), parse("Mara\r\nThomas\rFiora"));
    }

    @Test
    void dropsTextInBrackets() {
        assertEquals(List.of("Mara", "Thomas"), parse("Mara (injured, asleep), Thomas"));
        assertEquals(List.of("Mara", "Thomas"), parse("Mara [wounded], Thomas {hidden}"));
        assertEquals(List.of("Mara", "Thomas"), parse("Mara（負傷、眠っている）、Thomas"));
    }

    @Test
    void dropsNestedBrackets() {
        assertEquals(List.of("Mara", "Thomas"), parse("Mara (asleep (dreaming, of Fiora)), Thomas"));
    }

    @Test
    void aStrayClosingBracketIsIgnored() {
        assertEquals(List.of("Mara", "Thomas"), parse("Mara), Thomas"));
    }

    @Test
    void anUnclosedBracketDropsTheRestOfItsLine() {
        // Thomas could be taken from a note, so he is not taken; the next line is read again
        assertEquals(List.of("Mara", "Ilse"), parse("Mara, (injured, Thomas\nIlse"));
    }

    @Test
    void cutsANoteAfterADashOrAColon() {
        assertEquals(List.of("Mara", "Thomas", "Fiora"), parse("Mara - wounded, Thomas: asleep; Fiora — afraid"));
    }

    @Test
    void aHyphenatedNameSurvives() {
        assertEquals(List.of("Jean-Luc", "Anne-Marie"), parse("Jean-Luc, Anne-Marie"));
    }

    @Test
    void wordSeparatorsAreOnlyUsedWhenConfigured() {
        assertEquals(List.of("Jack and Jill"), parse("Jack and Jill"));
        assertEquals(List.of("Jack", "Jill", "Mara"),
                PresentCharacters.parse("Jack and Jill, Mara", List.of(",", "and")));
    }

    @Test
    void aWordSeparatorNeedsSpacesAroundIt() {
        assertEquals(List.of("Sandra", "Randall"), PresentCharacters.parse("Sandra AND Randall", List.of("and")));
        assertEquals(List.of("Anderson", "Brandt"), PresentCharacters.parse("Anderson and Brandt", List.of("and")));
        assertEquals(List.of("Candice"), PresentCharacters.parse("Candice", List.of("and")));
    }

    @Test
    void aCustomSeparatorReplacesTheDefaults() {
        assertEquals(List.of("Mara, Thomas", "Fiora"), PresentCharacters.parse("Mara, Thomas & Fiora", List.of("&")));
    }

    @Test
    void aNewlineSeparatorIsKeptWhenBlank() {
        assertEquals(List.of("Mara, Thomas", "Fiora"), PresentCharacters.parse("Mara, Thomas\nFiora", List.of("\n")));
    }

    @Test
    void unusableSeparatorsFallBackToTheDefaults() {
        assertEquals(List.of("Mara", "Thomas"), PresentCharacters.parse("Mara, Thomas", List.of(" ", "")));
    }

    @Test
    void normalizeIgnoresCaseAccentsSpacesAndTheArticle() {
        assertEquals("fiora", PresentCharacters.normalize("  FIORA "));
        assertEquals("zoe", PresentCharacters.normalize("Zoë"));
        assertEquals("jean luc", PresentCharacters.normalize("Jean  Luc"));
        assertEquals("duke", PresentCharacters.normalize("The Duke"));
        assertEquals("mara", PresentCharacters.normalize("\"Mara\"."));
        assertEquals("abc", PresentCharacters.normalize("ＡＢＣ"));
        assertEquals("", PresentCharacters.normalize(null));
    }

    @Test
    void normalizeKeepsJapaneseVoicing() {
        assertNotEquals(PresentCharacters.normalize("か"), PresentCharacters.normalize("が"));
    }

    @Test
    void isPresentMatchesTheWholeNameOrAnAlias() {
        Set<String> present = PresentCharacters.normalizedNames("mara (asleep), The Duke, Zoë", null);
        assertTrue(PresentCharacters.isPresent(present, "Mara", null));
        assertTrue(PresentCharacters.isPresent(present, "Duke", null));
        assertTrue(PresentCharacters.isPresent(present, "Zoe", null));
        assertTrue(PresentCharacters.isPresent(present, "Lady Mara", List.of("Lady M", "mara")));
        assertFalse(PresentCharacters.isPresent(present, "Thomas", List.of("Tom")));
    }

    @Test
    void aSubstringIsNotAMatch() {
        Set<String> present = PresentCharacters.normalizedNames("Mara's brother, Marathon, Thomas Fiora", null);
        assertFalse(PresentCharacters.isPresent(present, "Mara", null));
        assertFalse(PresentCharacters.isPresent(present, "Thomas", null));
        assertFalse(PresentCharacters.isPresent(present, "Fiora", null));
    }

    @Test
    void nothingIsPresentInAnEmptyField() {
        assertFalse(PresentCharacters.isPresent(PresentCharacters.normalizedNames("", null), "Mara", null));
        assertFalse(PresentCharacters.isPresent(null, "Mara", null));
        assertFalse(PresentCharacters.isPresent(Set.of("mara"), "", null));
    }
}
