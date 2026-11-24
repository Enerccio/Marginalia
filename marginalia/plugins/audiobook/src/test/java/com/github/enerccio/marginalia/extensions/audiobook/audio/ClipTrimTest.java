package com.github.enerccio.marginalia.extensions.audiobook.audio;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

class ClipTrimTest {

    private static final WavJoiner.Format MONO = new WavJoiner.Format(1, 1, 1000, 2000, 2, 16);

    /** A mono 16-bit wav of 1000 frames a second whose sample is its second (1000 for the first, 2000 for the second...). */
    private static byte[] wav(int seconds) {
        ByteBuffer data = ByteBuffer.allocate(seconds * 1000 * 2).order(ByteOrder.LITTLE_ENDIAN);
        for (int s = 0; s < seconds; s++) {
            for (int i = 0; i < 1000; i++) {
                data.putShort((short) ((s + 1) * 1000));
            }
        }
        return WavJoiner.of(MONO, data.array());
    }

    private static short sample(byte[] wav, int frame) throws BackendException {
        byte[] data = WavJoiner.data(wav);
        return ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getShort(frame * 2);
    }

    @Test
    void keepingARangeLeavesOnlyIt() throws Exception {
        byte[] out = ClipTrim.trim(wav(5), 1.0, 3.0, true);
        assertEquals(2.0, WavJoiner.seconds(out), 0.001);
        assertEquals(2000, sample(out, 500), "the second second comes first");
        assertEquals(3000, sample(out, 1500));
        assertTrue(Math.abs(sample(out, 0)) < 2000, "it fades in");
    }

    @Test
    void cuttingARangeJoinsTheSides() throws Exception {
        byte[] out = ClipTrim.trim(wav(5), 1.0, 3.0, false);
        assertEquals(3.0, WavJoiner.seconds(out), 0.001);
        assertEquals(1000, sample(out, 500));
        assertEquals(4000, sample(out, 1500), "after the cut comes the fourth second");
        assertEquals(MONO, WavJoiner.formatOf(out));
    }

    @Test
    void aWrongRangeIsRefused() {
        assertThrows(BackendException.class, () -> ClipTrim.trim(wav(2), 1.5, 1.0, true));
        assertThrows(BackendException.class, () -> ClipTrim.trim(wav(2), 5, 6, true), "outside the clip");
        assertThrows(BackendException.class, () -> ClipTrim.trim(wav(2), 0, 2, false), "nothing would be left");
    }
}
