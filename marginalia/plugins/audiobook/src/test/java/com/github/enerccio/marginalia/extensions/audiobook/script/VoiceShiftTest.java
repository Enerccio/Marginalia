package com.github.enerccio.marginalia.extensions.audiobook.script;

import com.github.enerccio.marginalia.extensions.audiobook.label.Age;
import com.github.enerccio.marginalia.extensions.audiobook.label.Gender;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class VoiceShiftTest {

    private static final String BOOK = "book-1";

    @Test
    void theTextFormComesBackAsItWent() {
        VoiceShift shift = new VoiceShift(-2, 1, 107, VoiceShift.Eq.BRIGHT);
        assertEquals("p-2.f+1.t107.eB", shift.spec());
        assertEquals(shift, VoiceShift.parse(shift.spec()));
        assertEquals(new VoiceShift(0, -1, 93, VoiceShift.Eq.DARK), VoiceShift.parse("p+0.f-1.t93.eD"));
        assertThrows(IllegalArgumentException.class, () -> VoiceShift.parse("p-2"));
        assertThrows(IllegalArgumentException.class, () -> VoiceShift.parse("p-2.f+1.t107.eX"));
        assertThrows(IllegalArgumentException.class, () -> VoiceShift.parse(null));
    }

    @Test
    void theSameSpeakerOfTheSameBookIsAlwaysTheSameVoiceAndCaseAndAccentsDoNotCount() {
        VoiceShift guard = VoiceShift.pick(BOOK, "the guard", 0, Gender.MALE, Age.ADULT, false);
        assertEquals(guard, VoiceShift.pick(BOOK, "The  Guard", 0, Gender.MALE, Age.ADULT, false));
        assertEquals(VoiceShift.pick(BOOK, "Tomáš", 0, Gender.MALE, Age.ADULT, false), VoiceShift.pick(BOOK, "tomas", 0, Gender.MALE, Age.ADULT, false));
    }

    @Test
    void speakersSpreadOverTheGridAndARerollOrAnotherBookIsAnotherVoice() {
        Set<String> seen = new HashSet<>();
        int differentInOtherBook = 0;
        int differentAfterReroll = 0;
        for (int i = 0; i < 200; i++) {
            String name = "speaker " + i;
            VoiceShift shift = VoiceShift.pick(BOOK, name, 0, Gender.MALE, Age.ADULT, false);
            seen.add(shift == null ? "none" : shift.spec());
            if (!java.util.Objects.equals(shift, VoiceShift.pick("book-2", name, 0, Gender.MALE, Age.ADULT, false))) {
                differentInOtherBook++;
            }
            if (!java.util.Objects.equals(shift, VoiceShift.pick(BOOK, name, 1, Gender.MALE, Age.ADULT, false))) {
                differentAfterReroll++;
            }
        }
        // 135 cells: 200 names fill most of them
        assertTrue(seen.size() > 80, "cells used: " + seen.size());
        assertTrue(differentInOtherBook > 180, "other book: " + differentInOtherBook);
        assertTrue(differentAfterReroll > 180, "reroll: " + differentAfterReroll);
    }

    @Test
    void theShiftsStayWhereTheyWereHeardToSoundRight() {
        for (int i = 0; i < 300; i++) {
            String name = "speaker " + i;
            for (Gender gender : Gender.values()) {
                for (Age age : Age.values()) {
                    for (boolean own : new boolean[]{false, true}) {
                        VoiceShift shift = VoiceShift.pick(BOOK, name, 0, gender, age, own);
                        if (shift == null) {
                            continue;
                        }
                        int max = age == Age.CHILD && !own ? 4 : age == Age.TEEN && gender != Gender.MALE ? 2 : 1;
                        assertTrue(shift.pitch() >= -4 && shift.pitch() <= max, gender + " " + age + " " + own + ": " + shift.spec());
                        assertTrue(shift.formant() >= -2 && shift.formant() <= 1, shift.spec());
                        assertTrue(shift.tempo() >= 85 && shift.tempo() <= 110, shift.spec());
                    }
                }
            }
        }
    }

    @Test
    void aChildIsRaisedFromTheWomensClipAndABoyIsLowerAndDarkerThanAGirl() {
        for (int i = 0; i < 100; i++) {
            String name = "child " + i;
            VoiceShift girl = VoiceShift.pick(BOOK, name, 0, Gender.FEMALE, Age.CHILD, false);
            VoiceShift boy = VoiceShift.pick(BOOK, name, 0, Gender.MALE, Age.CHILD, false);
            assertTrue(girl.pitch() >= 3 && girl.pitch() <= 4, girl.spec());
            assertTrue(boy.pitch() >= 2 && boy.pitch() <= 3, boy.spec());
            assertEquals(girl.pitch() - 1, boy.pitch());
            assertEquals(Math.max(-2, girl.formant() - 1), boy.formant());
            // the cast's own voice for children is used as it is (a girl not raised, a boy a little lower)
            VoiceShift ownGirl = VoiceShift.pick(BOOK, name, 0, Gender.FEMALE, Age.CHILD, true);
            assertTrue(ownGirl == null || ownGirl.pitch() <= 1, ownGirl == null ? "" : ownGirl.spec());
        }
    }

    @Test
    void anOldSpeakerIsLowerAndSlowerThanTheSameSpeakerAsAnAdult() {
        for (int i = 0; i < 100; i++) {
            String name = "old " + i;
            VoiceShift adult = VoiceShift.pick(BOOK, name, 0, Gender.MALE, Age.ADULT, false);
            VoiceShift old = VoiceShift.pick(BOOK, name, 0, Gender.MALE, Age.ELDERLY, false);
            int adultPitch = adult == null ? 0 : adult.pitch();
            int adultTempo = adult == null ? 100 : adult.tempo();
            // the cell +1 st, 107 % comes down to the base clip itself for an old speaker
            int oldPitch = old == null ? 0 : old.pitch();
            int oldTempo = old == null ? 100 : old.tempo();
            assertEquals(Math.max(-4, adultPitch - 1), oldPitch);
            assertTrue(oldTempo < adultTempo, (old == null ? "none" : old.spec()) + " vs " + (adult == null ? "none" : adult.spec()));
        }
    }

    @Test
    void theFilterIsRubberbandAndTheEqAndNothingForThePartsThatChangeNothing() {
        assertEquals("rubberband=pitch=0.890899:formant=shifted", new VoiceShift(-2, 0, 100, VoiceShift.Eq.FLAT).filter());
        // the formants alone: there with them, back without them
        assertEquals("rubberband=pitch=1.059463:formant=shifted,rubberband=pitch=0.943874:formant=preserved",
                new VoiceShift(0, 1, 100, VoiceShift.Eq.FLAT).filter());
        assertEquals("rubberband=tempo=0.93,treble=g=-4:f=3000,bass=g=2:f=150", new VoiceShift(0, 0, 93, VoiceShift.Eq.DARK).filter());
        assertTrue(new VoiceShift(0, 0, 100, VoiceShift.Eq.FLAT).isNone());
        assertEquals("-2 st, formants +1, tempo 107 %, brighter", new VoiceShift(-2, 1, 107, VoiceShift.Eq.BRIGHT).describe());
    }
}
