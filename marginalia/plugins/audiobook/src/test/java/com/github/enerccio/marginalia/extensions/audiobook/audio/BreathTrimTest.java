package com.github.enerccio.marginalia.extensions.audiobook.audio;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BreathTrimTest {

    public static final int RATE = 24000;
    public static final WavJoiner.Format MONO = new WavJoiner.Format(1, 1, RATE, RATE * 2, 2, 16);

    /** Builds 16-bit mono audio from parts: a voiced tone (few zero crossings), noise (many) or silence, each with its level in dB and length. */
    public static final class Audio {
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();
        private final Random random = new Random(7);

        public Audio voice(double seconds, double db) {
            return samples(seconds, db, i -> Math.sin(2 * Math.PI * 180 * i / RATE));
        }

        public Audio hiss(double seconds, double db) {
            return samples(seconds, db, i -> random.nextDouble() * 2 - 1);
        }

        public Audio silence(double seconds) {
            return samples(seconds, -200, i -> 0);
        }

        private Audio samples(double seconds, double db, java.util.function.IntToDoubleFunction wave) {
            double amplitude = Math.pow(10, db / 20) * Math.sqrt(2);
            for (int i = 0; i < (int) (seconds * RATE); i++) {
                short s = (short) Math.max(-32768, Math.min(32767, Math.round(wave.applyAsDouble(i) * amplitude * 32767)));
                out.write(s & 0xFF);
                out.write(s >> 8);
            }
            return this;
        }

        public byte[] data() {
            return out.toByteArray();
        }
    }

    private static double seconds(byte[] data) {
        return data.length / 2.0 / RATE;
    }

    @Test
    void aSoundAfterAGapAtTheEndIsCut() {
        // "The Kitchen" and then a breath swelling up to -30 dB after a quiet gap, cut off by the end of the clip
        byte[] data = new Audio().voice(0.8, -20).silence(0.35).voice(0.15, -30).data();

        byte[] trimmed = BreathTrim.trim(MONO, data);

        assertEquals(0.85, seconds(trimmed), 0.051, "cut at the start of the gap");
    }

    @Test
    void aShortLoudSoundAfterALongGapIsCutToo() {
        byte[] data = new Audio().voice(1.0, -20).silence(0.3).voice(0.1, -22).data();

        assertEquals(1.05, seconds(BreathTrim.trim(MONO, data)), 0.051);
    }

    @Test
    void aLastWordAfterAShortPauseStays() {
        // "Give me... a second.": the words after the pause are as loud as the rest
        byte[] data = new Audio().voice(1.0, -20).silence(0.2).voice(0.7, -21).data();

        assertEquals(seconds(data), seconds(BreathTrim.trim(MONO, data)), 0.001);
    }

    @Test
    void aWordThatEndsInAHissKeepsIt() {
        // "much": the vowel, a short closure, then the hiss of the "ch"
        byte[] data = new Audio().voice(1.0, -20).silence(0.05).hiss(0.2, -32).data();

        assertEquals(seconds(data), seconds(BreathTrim.trim(MONO, data)), 0.001);
    }

    @Test
    void aLongBreathyTailAfterTheLastWordIsCut() {
        byte[] data = new Audio().voice(1.0, -20).hiss(0.9, -38).data();

        assertEquals(1.25, seconds(BreathTrim.trim(MONO, data)), 0.051, "250 ms after the last voiced window stay");
    }

    @Test
    void theEndIsFadedOut() {
        byte[] trimmed = BreathTrim.trim(MONO, new Audio().voice(1.0, -20).data());

        short last = (short) ((trimmed[trimmed.length - 2] & 0xFF) | (trimmed[trimmed.length - 1] << 8));
        assertTrue(Math.abs(last) < 50, "the last sample is near zero: " + last);
    }
}
