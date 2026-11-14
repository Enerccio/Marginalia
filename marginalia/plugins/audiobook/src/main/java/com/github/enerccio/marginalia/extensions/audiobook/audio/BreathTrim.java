package com.github.enerccio.marginalia.extensions.audiobook.audio;

/**
 * Cuts what a speech model makes after the last word of a piece and fades the end out. Higgs Audio v3 goes on after the text: a breath or the start of
 * the next sound, cut off by the end of the clip, which is heard as a "whoosh" before the pause (on the author's server most pieces of a test scene ended at
 * -30 to -40 dB instead of fading out, 0.2 to 1 s after the last word). Measured in windows of 50 ms:
 * <ul>
 *     <li>a gap of at least 150 ms 25 dB under the loudest window, followed by a tail of at most 0.6 s that stays 8 dB under it (or of at most 0.25 s after
 *     a gap of 250 ms, whatever its level): the tail is cut at the start of the gap;</li>
 *     <li>else everything 250 ms after the last voiced window (loud, few zero crossings) goes: a word that ends in a hiss ("much", "yes") keeps it (cutting
 *     100 ms after it clipped the "ch" of "much").</li>
 * </ul>
 * 16-bit PCM only (what ffmpeg makes of the flac of ComfyUI); other audio is returned as it is.
 */
public final class BreathTrim {

    static final double WINDOW_SECONDS = 0.05;
    static final double FADE_SECONDS = 0.04;
    static final double KEEP_AFTER_VOICE_SECONDS = 0.25;

    private BreathTrim() {
    }

    /** The samples ({@code data} of a wav of that format) without the tail, faded out. */
    public static byte[] trim(WavJoiner.Format format, byte[] data) {
        if (format.bits() != 16 || format.channels() < 1 || data.length < format.blockAlign()) {
            return data;
        }
        int frames = data.length / format.blockAlign();
        double[] mono = new double[frames];
        for (int i = 0; i < frames; i++) {
            double sum = 0;
            for (int c = 0; c < format.channels(); c++) {
                int at = i * format.blockAlign() + c * 2;
                sum += (short) ((data[at] & 0xFF) | (data[at + 1] << 8)) / 32768.0;
            }
            mono[i] = sum / format.channels();
        }
        int window = Math.max(1, (int) Math.round(WINDOW_SECONDS * format.sampleRate()));
        int cut = end(mono, window, format.sampleRate());
        byte[] out = java.util.Arrays.copyOf(data, cut * format.blockAlign());
        int fade = Math.min(cut, (int) Math.round(FADE_SECONDS * format.sampleRate()));
        for (int i = 0; i < fade; i++) {
            int frame = cut - 1 - i;
            double gain = (double) i / fade;
            for (int c = 0; c < format.channels(); c++) {
                int at = frame * format.blockAlign() + c * 2;
                short s = (short) ((out[at] & 0xFF) | (out[at + 1] << 8));
                short faded = (short) Math.round(s * gain);
                out[at] = (byte) faded;
                out[at + 1] = (byte) (faded >> 8);
            }
        }
        return out;
    }

    /** The frame where the piece ends. */
    static int end(double[] mono, int window, int sampleRate) {
        int count = (mono.length + window - 1) / window;
        double[] db = new double[count];
        double[] zcr = new double[count];
        double peak = -200;
        for (int w = 0; w < count; w++) {
            int from = w * window;
            int to = Math.min(mono.length, from + window);
            double energy = 0;
            int crossings = 0;
            for (int i = from; i < to; i++) {
                energy += mono[i] * mono[i];
                if (i > from && (mono[i - 1] < 0) != (mono[i] < 0)) {
                    crossings++;
                }
            }
            db[w] = 20 * Math.log10(Math.sqrt(energy / Math.max(1, to - from)) + 1e-9);
            zcr[w] = (double) crossings / Math.max(1, to - from);
            peak = Math.max(peak, db[w]);
        }
        int last = 0;
        for (int w = 0; w < count; w++) {
            if (db[w] > peak - 20 && zcr[w] < 0.2) {
                last = w;
            }
        }
        double quiet = peak - 25;
        int w = 0;
        while (w < last) {
            if (db[w] < quiet) {
                int g = w;
                while (g < count && db[g] < quiet) {
                    g++;
                }
                double tailSeconds = (last + 1 - g) * WINDOW_SECONDS;
                double tailMax = -200;
                for (int t = g; t <= last && t < count; t++) {
                    tailMax = Math.max(tailMax, db[t]);
                }
                boolean quietTail = g - w >= 3 && tailMax <= peak - 8;
                boolean shortTail = g - w >= 5 && tailSeconds <= 0.25;
                if (w > 0 && tailSeconds <= 0.6 && (quietTail || shortTail)) {
                    return Math.min(mono.length, (w + 1) * window);
                }
                w = g;
            } else {
                w++;
            }
        }
        return Math.min(mono.length, (last + 1) * window + (int) Math.round(KEEP_AFTER_VOICE_SECONDS * sampleRate));
    }
}
