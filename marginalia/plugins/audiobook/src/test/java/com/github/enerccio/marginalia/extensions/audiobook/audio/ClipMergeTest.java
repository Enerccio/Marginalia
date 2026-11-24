package com.github.enerccio.marginalia.extensions.audiobook.audio;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClipMergeTest {

    private static final WavJoiner.Format MONO = ClipMerge.pcm16(1, 1000);

    /** A mono 16-bit wav of 1000 frames a second, every sample {@code value}. */
    private static byte[] wav(double seconds, short value) {
        int frames = (int) Math.round(seconds * 1000);
        ByteBuffer data = ByteBuffer.allocate(frames * 2).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < frames; i++) {
            data.putShort(value);
        }
        return WavJoiner.of(MONO, data.array());
    }

    private static short sample(byte[] wav, int frame) throws BackendException {
        return ByteBuffer.wrap(WavJoiner.data(wav)).order(ByteOrder.LITTLE_ENDIAN).getShort(frame * 2);
    }

    @Test
    void clipsOfOneFormatAreJoinedInOrder() throws Exception {
        byte[] out = ClipMerge.merge(List.of(wav(1, (short) 100), wav(0.5, (short) 200), wav(2, (short) 300)));
        assertEquals(3.5, WavJoiner.seconds(out), 0.001);
        assertEquals(MONO, WavJoiner.formatOf(out));
        assertEquals(100, sample(out, 999));
        assertEquals(200, sample(out, 1000), "no pause between the clips");
        assertEquals(300, sample(out, 1500));
    }

    @Test
    void oneClipIsNothingToMerge() {
        assertThrows(BackendException.class, () -> ClipMerge.merge(List.of(wav(1, (short) 1))));
        assertThrows(BackendException.class, () -> ClipMerge.merge(List.of()));
    }

    @Test
    void anEmptyClipIsRefused() {
        assertThrows(BackendException.class, () -> ClipMerge.merge(List.of(wav(1, (short) 1), new byte[0])));
    }
}
