package com.github.enerccio.marginalia.extensions.audiobook.model;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutoModeTest {

    @Test
    void theIdsAreWhatTheSettingStores() {
        assertEquals("no", AutoMode.NO.id());
        assertEquals("plain", AutoMode.PLAIN.id());
        assertEquals("labelled", AutoMode.LABELLED.id());
        for (AutoMode mode : AutoMode.values()) {
            assertEquals(mode, AutoMode.of(mode.id()));
        }
    }

    @Test
    void anythingElseIsOffSoNothingHappensByAccident() {
        assertEquals(AutoMode.NO, AutoMode.of(null));
        assertEquals(AutoMode.NO, AutoMode.of(""));
        assertEquals(AutoMode.NO, AutoMode.of("yes"));
        assertEquals(AutoMode.NO, AutoMode.of("LABELLED"), "the ids are lower case, a different spelling is not a mode");
    }

    @Test
    void theUserLevelIsOffUntilSetAndSurvivesJson() {
        Gson gson = new Gson();
        assertEquals(AutoMode.NO, new AudiobookSettings().getAutoMode());
        assertEquals(AutoMode.NO, gson.fromJson("{}", AudiobookSettings.class).getAutoMode(), "settings saved before have none");

        AudiobookSettings settings = new AudiobookSettings();
        settings.setAutoMode(AutoMode.LABELLED);
        assertEquals(AutoMode.LABELLED, gson.fromJson(gson.toJson(settings), AudiobookSettings.class).getAutoMode());
        settings.setAutoMode(null);
        assertEquals(AutoMode.NO, settings.getAutoMode());
    }
}
