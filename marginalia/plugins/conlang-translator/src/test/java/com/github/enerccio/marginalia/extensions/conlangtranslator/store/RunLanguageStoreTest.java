package com.github.enerccio.marginalia.extensions.conlangtranslator.store;

import com.github.enerccio.marginalia.extensions.conlangtranslator.diff.ChangeSet;
import com.github.enerccio.marginalia.extensions.conlangtranslator.diff.LanguageDiff;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.*;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What an agent run leaves of a language in the attributes of its book, and the three-way take-over of it into a library the user changed meanwhile.
 */
class RunLanguageStoreTest {

    private static Language vrakht() {
        Language language = new Language("Vrakht");
        language.getRules().add(new RuleSection("Grammar", "Mood particle first."));
        language.addEntry(LexiconEntry.ofAuthor("drak", "war"));
        return language;
    }

    @Test
    void theRunLanguagesRoundTripInTheAttributesOfTheBook() {
        Language base = vrakht();
        Language ours = ConlangStore.copy(base);
        ours.addEntry(LexiconEntry.ofModel("gild", "house", "house root", Origin.model(null, "agent", 1L)));
        Map<String, RunLanguage> run = new LinkedHashMap<>();
        run.put(base.getId(), new RunLanguage(base, ours, 7L));

        JsonObject attributes = new JsonObject();
        ConlangStore.writeRun(attributes, run);
        Map<String, RunLanguage> read = ConlangStore.readRun(attributes);

        assertEquals(1, read.size());
        RunLanguage entry = read.get(base.getId());
        assertEquals(7L, entry.getSince());
        assertEquals(1, entry.getBase().getLexicon().size());
        assertEquals(2, entry.getOurs().getLexicon().size());
        assertEquals("gild", entry.getOurs().getLexicon().get(1).getWord());
    }

    @Test
    void aLanguageTheRunMadeHasNoBase() {
        Language made = new Language("Skarn");
        JsonObject attributes = new JsonObject();
        ConlangStore.writeRun(attributes, Map.of(made.getId(), new RunLanguage(null, made, 1L)));
        RunLanguage entry = ConlangStore.readRun(attributes).get(made.getId());
        assertNull(entry.getBase());
        assertEquals("Skarn", entry.getOurs().getName());
    }

    @Test
    void nothingLeftRemovesTheAttributeAndADamagedEntryIsLeftOut() {
        JsonObject attributes = new JsonObject();
        attributes.add(RunLanguage.KEY, new JsonObject());
        ConlangStore.writeRun(attributes, Map.of());
        assertFalse(attributes.has(RunLanguage.KEY));
        assertTrue(ConlangStore.readRun(attributes).isEmpty());

        JsonObject bad = new JsonObject();
        JsonObject languages = new JsonObject();
        languages.addProperty("x", "not an object");
        bad.add(RunLanguage.KEY, languages);
        assertTrue(ConlangStore.readRun(bad).isEmpty());
        assertTrue(ConlangStore.readRun(null).isEmpty());
    }

    @Test
    void theTakeOverKeepsWhatTheUserChangedMeanwhile() {
        Language base = vrakht();
        Language ours = ConlangStore.copy(base);
        ours.addEntry(LexiconEntry.ofModel("gild", "house", "house root", Origin.model(null, "agent", 1L)));
        // the user added a word to the library while the run worked
        Language library = ConlangStore.copy(base);
        library.addEntry(LexiconEntry.ofAuthor("mun", "boy"));

        ChangeSet set = LanguageDiff.diff(base, ours, library);
        assertEquals(1, set.changes().size(), "only what the run did is a change");
        set.pickClean();
        LanguageDiff.Result result = LanguageDiff.apply(library, set);

        assertEquals(1, result.applied().size());
        assertEquals(3, library.getLexicon().size(), "drak, the user's mun and the run's gild");
    }

    @Test
    void aFieldBothSidesChangedIsAConflictThatKeepsTheLibrarysValue() {
        Language base = vrakht();
        Language ours = ConlangStore.copy(base);
        ours.getLexicon().getFirst().setRationale("the run's reason");
        Language library = ConlangStore.copy(base);
        library.getLexicon().getFirst().setRationale("the user's reason");

        ChangeSet set = LanguageDiff.diff(base, ours, library);
        assertEquals(1, set.conflicts());
        set.pickClean();
        LanguageDiff.apply(library, set);
        assertEquals("the user's reason", library.getLexicon().getFirst().getRationale(), "the user's words win until they take the run's");
    }
}
