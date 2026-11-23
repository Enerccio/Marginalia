package com.github.enerccio.marginalia.extensions.audiobook.audio;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * How fit an audio file is to be the reference clip of a voice. A voice model copies the recording, not only the voice: a clip cut from a film (the
 * author's first test clip: its quietest parts at -47 dB, 29 dB under the speech, no pause in 12.5 s) gave Higgs lines a soft "whoosh" no filter of the
 * output removed, a clip from a voice actor's database (pauses at -61.5 dB, 55 dB under the speech) gave none (TODO.md). Measured in windows of 50 ms
 * on a 16-bit mono version of the clip:
 * <ul>
 *     <li>the <b>floor</b> is the mean level of the quietest 10 % of the windows, the <b>margin</b> the loudest window minus the floor: under 35 dB the clip
 *     has background sound, 35 to 45 some, from 45 it is clean;</li>
 *     <li>shorter than 8 s is too short to clone well, longer than 30 s is cut by Chatterbox to its first 30 s;</li>
 *     <li>more than 0.1 % of the samples at full scale is clipped (distorted).</li>
 * </ul>
 */
public final class ClipQuality {

    static final double WINDOW_SECONDS = 0.05;
    static final double BACKGROUND_MARGIN = 35;
    static final double CLEAN_MARGIN = 45;
    static final double MIN_SECONDS = 8;
    static final double MAX_SECONDS = 30;
    static final double MAX_CLIPPED_SHARE = 0.001;

    /** The verdict, from the best. */
    public enum Grade {
        CLEAN, FAIR, POOR
    }

    /** What can be wrong with a clip, in the order it matters. */
    public enum Problem {
        BACKGROUND, SOME_BACKGROUND, CLIPPED, TOO_SHORT, TOO_LONG
    }

    /**
     * @param seconds  the length
     * @param peakDb   the loudest window
     * @param floorDb  the mean of the quietest 10 % of the windows
     * @param clipped  the share of samples at full scale
     * @param problems what is wrong, empty for a clean clip
     */
    public record Result(double seconds, double peakDb, double floorDb, double clipped, List<Problem> problems) {

        public double marginDb() {
            return peakDb - floorDb;
        }

        public Grade grade() {
            if (problems.contains(Problem.BACKGROUND) || problems.contains(Problem.CLIPPED) || problems.contains(Problem.TOO_SHORT)) {
                return Grade.POOR;
            }
            return problems.isEmpty() ? Grade.CLEAN : Grade.FAIR;
        }
    }

    private ClipQuality() {
    }

    /**
     * Measures a wav or mp3 clip: a 16-bit wav is read here, anything else needs ffmpeg.
     *
     * @return null when the clip can't be read here (an mp3 without ffmpeg)
     */
    public static Result measure(byte[] audio) throws BackendException {
        if (audio == null || audio.length == 0) {
            throw new BackendException("The clip is empty");
        }
        byte[] wav;
        if (Ffmpeg.available()) {
            wav = Ffmpeg.toMonoWav(audio);
        } else if (AudioKind.of(audio) == AudioKind.WAV && WavJoiner.formatOf(audio).bits() == 16 && WavJoiner.formatOf(audio).tag() == 1) {
            wav = audio;
        } else {
            return null;
        }
        WavJoiner.Format format = WavJoiner.formatOf(wav);
        return measure(format, WavJoiner.data(wav));
    }

    /** Measures 16-bit PCM of that format. */
    static Result measure(WavJoiner.Format format, byte[] data) {
        int frames = data.length / format.blockAlign();
        int window = Math.max(1, (int) Math.round(WINDOW_SECONDS * format.sampleRate()));
        int count = Math.max(1, (frames + window - 1) / window);
        double[] db = new double[count];
        long clipped = 0;
        for (int w = 0; w < count; w++) {
            double energy = 0;
            int from = w * window;
            int to = Math.min(frames, from + window);
            for (int i = from; i < to; i++) {
                double sum = 0;
                for (int c = 0; c < format.channels(); c++) {
                    int at = i * format.blockAlign() + c * 2;
                    short s = (short) ((data[at] & 0xFF) | (data[at + 1] << 8));
                    if (s >= 32766 || s <= -32767) {
                        clipped++;
                    }
                    sum += s / 32768.0;
                }
                double v = sum / format.channels();
                energy += v * v;
            }
            db[w] = 20 * Math.log10(Math.sqrt(energy / Math.max(1, to - from)) + 1e-9);
        }
        double[] sorted = db.clone();
        Arrays.sort(sorted);
        int tenth = Math.max(1, count / 10);
        double floor = 0;
        for (int i = 0; i < tenth; i++) {
            floor += sorted[i];
        }
        floor /= tenth;
        double peak = sorted[count - 1];
        double seconds = (double) frames / format.sampleRate();
        double clippedShare = frames == 0 ? 0 : (double) clipped / ((long) frames * format.channels());

        List<Problem> problems = new ArrayList<>();
        double margin = peak - floor;
        if (margin < BACKGROUND_MARGIN) {
            problems.add(Problem.BACKGROUND);
        } else if (margin < CLEAN_MARGIN) {
            problems.add(Problem.SOME_BACKGROUND);
        }
        if (clippedShare > MAX_CLIPPED_SHARE) {
            problems.add(Problem.CLIPPED);
        }
        if (seconds < MIN_SECONDS) {
            problems.add(Problem.TOO_SHORT);
        } else if (seconds > MAX_SECONDS) {
            problems.add(Problem.TOO_LONG);
        }
        return new Result(seconds, peak, floor, clippedShare, List.copyOf(problems));
    }
}
