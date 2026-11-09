package com.github.enerccio.marginalia.extensions.reallangtranslator.model;

import com.github.enerccio.marginalia.extensions.reallangtranslator.model.Glossary.Effective;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.Glossary.Layer;
import com.github.enerccio.marginalia.extensions.reallangtranslator.model.Glossary.Level;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The three levels of the glossary: the union, the nearest level for the same word, a word switched off, and the whole-word search.
 */
class GlossaryTest {

    private static GlossaryEntry written(String source, String written) {
        return new GlossaryEntry(source, GlossaryKind.WRITTEN, written, "", "");
    }

    private static Layer layer(Level level, String label, GlossaryEntry... entries) {
        return new Layer(level, level == Level.FOLDER ? "uuid-" + label : null, label, List.of(entries));
    }

    @Test
    void theLevelsAreAUnionInTheOrderTheWordsAppear() {
        List<Effective> merged = Glossary.merge(List.of(
                layer(Level.LANGUAGE, "Japanese", written("witch", "魔女")),
                layer(Level.FOLDER, "Series", written("Saellen", "サエレン")),
                layer(Level.BOOK, "Book", written("Silvakhor", "シルヴァコール"))));

        assertEquals(List.of("witch", "Saellen", "Silvakhor"), merged.stream().map(item -> item.entry().getSource()).toList());
        assertEquals(List.of(Level.LANGUAGE, Level.FOLDER, Level.BOOK), merged.stream().map(Effective::level).toList());
        assertEquals("uuid-Series", merged.get(1).id());
    }

    @Test
    void theNearestLevelWinsForTheSameWordWhateverTheCase() {
        List<Effective> merged = Glossary.merge(List.of(
                layer(Level.LANGUAGE, "Japanese", written("Saellen", "セーレン")),
                layer(Level.FOLDER, "Outer", written("saellen", "サエレン")),
                layer(Level.FOLDER, "Inner", written("SAELLEN", "サエレン様")),
                layer(Level.BOOK, "Book")));

        assertEquals(1, merged.size());
        assertEquals("サエレン様", merged.getFirst().entry().getWritten());
        assertEquals("Inner", merged.getFirst().label());
    }

    @Test
    void aWordSwitchedOffNearerTakesItOutOfWhatTheModelGets() {
        GlossaryEntry off = written("witch", "魔女");
        off.setEnabled(false);
        List<Effective> merged = Glossary.merge(List.of(
                layer(Level.LANGUAGE, "Japanese", written("witch", "魔女"), written("sword", "剣")),
                layer(Level.BOOK, "Book", off)));

        assertEquals(2, merged.size());
        assertEquals(List.of("sword"), Glossary.active(merged).stream().map(GlossaryEntry::getSource).toList());
        assertFalse(merged.stream().filter(item -> item.entry().getSource().equals("witch")).findFirst().orElseThrow().active());
    }

    @Test
    void anEntryWithNothingToWriteIsNotUsedUnlessItIsKept() {
        GlossaryEntry empty = written("ghost", "");
        GlossaryEntry kept = new GlossaryEntry("Tomáš", GlossaryKind.KEEP, "", "", "");

        assertFalse(empty.isUsable());
        assertTrue(kept.isUsable());
        assertEquals("Tomáš", kept.expected());
        assertEquals(List.of(kept), Glossary.active(Glossary.merge(List.of(layer(Level.BOOK, "Book", empty, kept)))));
    }

    @Test
    void aWordIsFoundOnlyWhole() {
        assertTrue(Glossary.contains("Ann said hello.", "ann"));
        assertFalse(Glossary.contains("Anna said hello.", "Ann"));
        assertTrue(Glossary.contains("\"Saellen!\" she cried.", "Saellen"));
        assertTrue(Glossary.contains("Tomáš waited.", "tomáš"));
        assertFalse(Glossary.contains("", "Ann"));
        assertFalse(Glossary.contains("text", " "));
    }

    @Test
    void onlyTheEntriesThePartHasAreRelevant() {
        List<GlossaryEntry> all = List.of(written("Saellen", "サエレン"), written("Silvakhor", "シルヴァコール"));

        assertEquals(List.of("Saellen"), Glossary.relevant(all, "Saellen came in.").stream().map(GlossaryEntry::getSource).toList());
        assertTrue(Glossary.relevant(all, "Nobody came in.").isEmpty());
    }

    @Test
    void puttingReplacesTheSameWordAndKeepsItsPlaceAndId() {
        GlossaryEntry first = written("Saellen", "セーレン");
        GlossaryEntry other = written("Silvakhor", "シルヴァコール");
        List<GlossaryEntry> list = Glossary.put(Glossary.put(List.of(), first), other);

        GlossaryEntry changed = written("saellen", "サエレン");
        List<GlossaryEntry> replaced = Glossary.put(list, changed);

        assertEquals(2, replaced.size());
        assertEquals("サエレン", replaced.getFirst().getWritten());
        assertEquals(first.getId(), replaced.getFirst().getId());
    }
}
