package com.github.enerccio.marginalia.extensions.audiobook.audio;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * The samples of a clip as one channel, to draw it: a 16-bit wav is read here (its channels mixed), anything else is decoded by ffmpeg.
 *
 * @param samples    the mono samples
 * @param sampleRate their rate
 */
public record Waveform(short[] samples, int sampleRate) {

    /** The waveform of a clip; null when it can't be read here (an mp3 without ffmpeg). */
    public static Waveform of(byte[] audio) throws BackendException {
        if (audio == null || audio.length == 0) {
            return null;
        }
        byte[] wav = audio;
        if (AudioKind.of(audio) != AudioKind.WAV || !pcm16(audio)) {
            if (!Ffmpeg.available()) {
                return null;
            }
            wav = Ffmpeg.toMonoWav(audio);
        }
        WavJoiner.Format format = WavJoiner.formatOf(wav);
        ByteBuffer data = ByteBuffer.wrap(WavJoiner.data(wav)).order(ByteOrder.LITTLE_ENDIAN);
        int channels = Math.max(1, format.channels());
        int frames = data.remaining() / (2 * channels);
        short[] mono = new short[frames];
        for (int f = 0; f < frames; f++) {
            int sum = 0;
            for (int c = 0; c < channels; c++) {
                sum += data.getShort();
            }
            mono[f] = (short) (sum / channels);
        }
        return new Waveform(mono, format.sampleRate());
    }

    private static boolean pcm16(byte[] wav) {
        try {
            WavJoiner.Format format = WavJoiner.formatOf(wav);
            return format.tag() == 1 && format.bits() == 16;
        } catch (BackendException e) {
            return false;
        }
    }

    public double seconds() {
        return samples.length / (double) sampleRate;
    }

    /**
     * The lowest and the highest sample of each of {@code buckets} equal slices of the range, as fractions of full scale (-1 to 1):
     * {@code [min0, max0, min1, max1, ...]}.
     */
    public float[] peaks(double from, double to, int buckets) {
        float[] peaks = new float[buckets * 2];
        int start = Math.max(0, (int) Math.floor(from * sampleRate));
        int end = Math.min(samples.length, (int) Math.ceil(to * sampleRate));
        if (end <= start || buckets <= 0) {
            return peaks;
        }
        double per = (end - start) / (double) buckets;
        for (int b = 0; b < buckets; b++) {
            int a = start + (int) Math.floor(b * per);
            int z = Math.max(a + 1, Math.min(end, start + (int) Math.floor((b + 1) * per)));
            int min = 0;
            int max = 0;
            for (int i = a; i < z && i < samples.length; i++) {
                min = Math.min(min, samples[i]);
                max = Math.max(max, samples[i]);
            }
            peaks[b * 2] = min / 32768f;
            peaks[b * 2 + 1] = max / 32767f;
        }
        return peaks;
    }
}
