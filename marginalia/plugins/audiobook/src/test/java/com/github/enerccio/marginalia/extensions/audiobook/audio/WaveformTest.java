package com.github.enerccio.marginalia.extensions.audiobook.audio;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WaveformTest {

    @Test
    void aStereoWavIsMixedAndItsPeaksFollowTheRange() throws Exception {
        WavJoiner.Format stereo = new WavJoiner.Format(1, 2, 100, 400, 4, 16);
        ByteBuffer data = ByteBuffer.allocate(200 * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < 200; i++) {
            // silence in the first second, a loud second after it; the right channel is silent
            data.putShort((short) (i < 100 ? 0 : (i % 2 == 0 ? 16000 : -16000))).putShort((short) 0);
        }
        Waveform waveform = Waveform.of(WavJoiner.of(stereo, data.array()));

        assertEquals(100, waveform.sampleRate());
        assertEquals(2.0, waveform.seconds(), 1e-9);
        float[] peaks = waveform.peaks(0, 2, 2);
        assertEquals(0f, peaks[0], 1e-6);
        assertEquals(0f, peaks[1], 1e-6);
        assertEquals(-8000 / 32768f, peaks[2], 1e-4, "the channels are averaged");
        assertEquals(8000 / 32767f, peaks[3], 1e-4);
        float[] zoomed = waveform.peaks(0.5, 0.9, 4);
        for (float peak : zoomed) {
            assertEquals(0f, peak, 1e-6, "a zoom into the silence is silent");
        }
    }
}
