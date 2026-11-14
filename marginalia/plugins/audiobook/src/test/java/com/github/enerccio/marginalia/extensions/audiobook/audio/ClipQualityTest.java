package com.github.enerccio.marginalia.extensions.audiobook.audio;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClipQualityTest {

    /** A reading: phrases of 1.5 s with pauses of 0.4 s between them, over the given background (silence or hiss at a level). */
    private static byte[] reading(double seconds, Double backgroundDb) {
        BreathTrimTest.Audio audio = new BreathTrimTest.Audio();
        for (double t = 0; t < seconds; t += 1.9) {
            audio.voice(1.5, -18);
            if (backgroundDb == null) {
                audio.silence(0.4);
            } else {
                audio.hiss(0.4, backgroundDb);
            }
        }
        return audio.data();
    }

    @Test
    void aCleanReadingWithPausesIsClean() {
        ClipQuality.Result q = ClipQuality.measure(BreathTrimTest.MONO, reading(15, -75.0));

        assertEquals(List.of(), q.problems());
        assertEquals(ClipQuality.Grade.CLEAN, q.grade());
        assertTrue(q.marginDb() > 45, "margin " + q.marginDb());
        assertEquals(15.2, q.seconds(), 0.1);
    }

    @Test
    void aClipWithABackgroundLikeAFilmIsPoor() {
        // the author's film clip: its quietest parts 29 dB under the speech
        ClipQuality.Result q = ClipQuality.measure(BreathTrimTest.MONO, reading(15, -47.0));

        assertEquals(List.of(ClipQuality.Problem.BACKGROUND), q.problems());
        assertEquals(ClipQuality.Grade.POOR, q.grade());
        // -18 dB of voice over hiss at nominally -47 dB (uniform noise is 1.8 dB quieter than a sine of the same peak): about 31 dB
        assertEquals(31, q.marginDb(), 1.5);
    }

    @Test
    void someBackgroundIsFair() {
        ClipQuality.Result q = ClipQuality.measure(BreathTrimTest.MONO, reading(15, -58.0));

        assertEquals(List.of(ClipQuality.Problem.SOME_BACKGROUND), q.problems());
        assertEquals(ClipQuality.Grade.FAIR, q.grade());
    }

    @Test
    void aShortOrALongClipIsTold() {
        assertEquals(List.of(ClipQuality.Problem.TOO_SHORT), ClipQuality.measure(BreathTrimTest.MONO, reading(4, -75.0)).problems());
        ClipQuality.Result longOne = ClipQuality.measure(BreathTrimTest.MONO, reading(40, -75.0));
        assertEquals(List.of(ClipQuality.Problem.TOO_LONG), longOne.problems());
        assertEquals(ClipQuality.Grade.FAIR, longOne.grade(), "a long clip only loses its end");
    }

    @Test
    void aClippedClipIsPoor() {
        BreathTrimTest.Audio audio = new BreathTrimTest.Audio();
        for (int i = 0; i < 8; i++) {
            audio.voice(1.5, 3).silence(0.4);
        }
        ClipQuality.Result q = ClipQuality.measure(BreathTrimTest.MONO, audio.data());

        assertTrue(q.problems().contains(ClipQuality.Problem.CLIPPED), q.problems().toString());
        assertEquals(ClipQuality.Grade.POOR, q.grade());
    }

    @Test
    void aWavFileIsMeasuredWholeAndTheCleanupLowersASteadyHiss() throws Exception {
        byte[] wav = WavJoiner.of(BreathTrimTest.MONO, reading(15, -50.0));
        ClipQuality.Result before = ClipQuality.measure(wav);
        assertNotNull(before);
        assertEquals(ClipQuality.Grade.POOR, before.grade());
        org.junit.jupiter.api.Assumptions.assumeTrue(Ffmpeg.available(), "ffmpeg is not installed");

        ClipQuality.Result after = ClipQuality.measure(Ffmpeg.cleanVoice(wav, before.floorDb()));

        assertTrue(after.floorDb() < before.floorDb() - 6, "a steady hiss is lowered: " + before.floorDb() + " -> " + after.floorDb());
        assertEquals(before.peakDb(), after.peakDb(), 1.5, "the voice stays");
    }
}
