package com.github.enerccio.marginalia.extensions.reallangtranslator.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class PresetsTest {

    @Test
    void theJapanesePresetsDifferInTheScript() {
        assertEquals(Script.KANA_KANJI, Presets.make("ja-kana-kanji").getScript());
        assertEquals(Script.KANA, Presets.make("ja-kana").getScript());
        assertEquals(Script.ROMAJI, Presets.make("ja-romaji").getScript());
        assertEquals("Japanese", Presets.make("ja-kana").getTongue());
    }

    @Test
    void aReadingIsAskedForOnlyWhereTheTextIsNotRomajiAlready() {
        assertTrue(Presets.make("ja-kana-kanji").isReading());
        assertTrue(Presets.make("ja-kana").isReading());
        assertFalse(Presets.make("ja-romaji").isReading());
    }

    @Test
    void theJapanesePresetsCarryTheRulesOfPolitenessAndNames() {
        RealLanguage language = Presets.make("ja-kana-kanji");

        assertTrue(language.getPoliteness().contains("keigo"));
        assertTrue(language.getNames().contains("katakana"));
    }

    @Test
    void anotherLanguageStartsBlankAndEveryNewLanguageHasItsOwnId() {
        RealLanguage other = Presets.make("other");

        assertEquals("", other.getName());
        assertEquals(Script.NATIVE, other.getScript());
        assertEquals("", other.getPoliteness());
        assertEquals(4, new HashSet<>(Presets.all().stream().map(Presets.Preset::id).toList()).size());
        assertNotEquals(Presets.make("other").getId(), Presets.make("other").getId());
    }

    @Test
    void anUnknownPresetIsABlankLanguage() {
        assertEquals("", Presets.make("nope").getName());
    }

    @Test
    void theTongueIsTheNameUntilTheUserSaysOtherwise() {
        RealLanguage language = new RealLanguage();
        language.setName("Korean");

        assertEquals("Korean", language.getTongue());
        language.setTongue("Korean (Seoul)");
        assertEquals("Korean (Seoul)", language.getTongue());
    }
}
