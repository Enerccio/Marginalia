package com.github.enerccio.marginalia.extensions.audiobook.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VoiceUsesTest {

    private static AudiobookSettings settings() {
        AudiobookSettings settings = new AudiobookSettings();
        Actor mira = new Actor();
        mira.setName("Mira");
        mira.setVoice("clip:film");
        mira.getVariants().put(Role.moodKey("WHISPER"), "clip:film");
        mira.getVariants().put(Role.languageKey("ja"), "clip:japanese");
        Actor tomas = new Actor();
        tomas.setName("Tomas");
        tomas.setVoice("predefined:Henry.wav");
        BackendConfig higgs = new BackendConfig();
        higgs.setName("Higgs");
        higgs.setNarratorVoice("clip:film");
        BackendConfig turbo = new BackendConfig();
        turbo.setName("Turbo");
        turbo.setNarratorVoice("predefined:Henry.wav");
        settings.setActors(new java.util.ArrayList<>(List.of(mira, tomas)));
        settings.setBackends(new java.util.ArrayList<>(List.of(higgs, turbo)));
        return settings;
    }

    @Test
    void theUsesOfAClipAreTheActorsVoicesAndTheBackendsNarrators() {
        assertEquals(List.of(new AudiobookSettings.VoiceUse(false, "Mira", null), new AudiobookSettings.VoiceUse(false, "Mira", "mood:WHISPER"),
                new AudiobookSettings.VoiceUse(true, "Higgs", null)), settings().usesOf("clip:film"));
        assertEquals(List.of(), settings().usesOf("clip:nobody"));
    }

    @Test
    void aReplacedClipIsReplacedEverywhereAndNothingElse() {
        AudiobookSettings settings = settings();

        assertEquals(3, settings.replaceVoice("clip:film", "clip:clean"));

        Actor mira = settings.getActors().getFirst();
        assertEquals("clip:clean", mira.getVoice());
        assertEquals("clip:clean", mira.getVariants().get(Role.moodKey("WHISPER")));
        assertEquals("clip:japanese", mira.getVariants().get(Role.languageKey("ja")));
        assertEquals("predefined:Henry.wav", settings.getActors().get(1).getVoice());
        assertEquals("clip:clean", settings.getBackends().getFirst().getNarratorVoice());
        assertEquals("predefined:Henry.wav", settings.getBackends().get(1).getNarratorVoice());
        assertEquals(List.of(), settings.usesOf("clip:film"));
        assertEquals(0, settings.replaceVoice("clip:film", "clip:clean"), "nothing left to replace");
    }
}
