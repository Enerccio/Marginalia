package com.github.enerccio.marginalia.extensions.conlangtranslator.service;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.EntryStatus;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.Language;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.LexiconEntry;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.NameCategory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * What the provider tells the Name generator: the categories of the library of the caller, with their words as they are now.
 */
class NameGeneratorBridgeTest {

    private static final class Library extends LanguageLibrary {
        List<Language> languages = new ArrayList<>();
        boolean fails;

        @Override
        public List<Language> languages() {
            if (fails) {
                throw new IllegalStateException("no session");
            }
            return languages;
        }
    }

    private final Library library = new Library();
    private final NameGeneratorBridge bridge = new NameGeneratorBridge(null, library, null);

    @Test
    void theProviderListsTheNameCategoriesOfTheLibraryWithTheirWordsAsTheyAreNow() {
        Language vrakht = new Language("Vrakht");
        LexiconEntry drak = vrakht.addEntry(LexiconEntry.ofAuthor("drak", "war"));
        LexiconEntry krom = vrakht.addEntry(LexiconEntry.ofAuthor("krom", "king"));
        NameCategory places = new NameCategory("Places");
        places.set(drak.getId(), true);
        places.set(krom.getId(), true);
        places.set("gone", true);
        vrakht.getNameCategories().add(places);
        vrakht.getNameCategories().add(new NameCategory("Nobody"));
        library.languages.add(vrakht);
        library.languages.add(new Language("Plain"));

        Map<String, Collection<String>> categories = bridge.categoriesOfCaller();

        assertEquals(List.of("Conlang - Vrakht - Places"), List.copyOf(categories.keySet()));
        assertEquals(List.of("drak", "krom"), List.copyOf(categories.get("Conlang - Vrakht - Places")));

        // the next call sees the library as it is then: a word retired, the category emptied
        krom.setStatus(EntryStatus.RETIRED);
        assertEquals(List.of("drak"), List.copyOf(bridge.categoriesOfCaller().get("Conlang - Vrakht - Places")));
        vrakht.deleteEntry(drak.getId());
        assertTrue(bridge.categoriesOfCaller().isEmpty());
    }

    @Test
    void aCallerWithoutALibraryGetsAnExceptionTheGeneratorTakesForNothingHere() {
        library.fails = true;

        assertThrows(IllegalStateException.class, bridge::categoriesOfCaller);
    }
}
