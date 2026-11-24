package com.github.enerccio.marginalia.extensions.audiobook.audio;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;

import java.io.ByteArrayOutputStream;

/**
 * Keeps a range of a clip, or cuts it out (a sound under the voice that no cleanup removes: a hoot, a cough, a door). A 16-bit wav is cut here, with a
 * short fade at each join so that it does not click; anything else needs ffmpeg ({@link Ffmpeg#trim}).
 */
public final class ClipTrim {

    /** The fade at a join, in seconds. */
    static final double FADE_SECONDS = 0.01;

    private ClipTrim() {
    }

    /**
     * @param from the start of the range in seconds
     * @param to   the end of the range in seconds
     * @param keep true: only the range stays; false: the range is cut out
     * @return a wav
     */
    public static byte[] trim(byte[] audio, double from, double to, boolean keep) throws BackendException {
        if (audio == null || audio.length == 0) {
            throw new BackendException("The clip is empty");
        }
        if (!(from >= 0) || !(to > from)) {
            throw new BackendException("The range must start before it ends");
        }
        if (AudioKind.of(audio) == AudioKind.WAV) {
            WavJoiner.Format format = WavJoiner.formatOf(audio);
            if (format.tag() == 1 && format.bits() == 16) {
                return trimPcm16(format, WavJoiner.data(audio), from, to, keep);
            }
        }
        if (!Ffmpeg.available()) {
            throw new BackendException("Only a 16-bit wav clip can be trimmed without ffmpeg");
        }
        return Ffmpeg.trim(audio, from, to, keep);
    }

    static byte[] trimPcm16(WavJoiner.Format format, byte[] data, double from, double to, boolean keep) throws BackendException {
        int block = format.blockAlign();
        int frames = data.length / block;
        int start = Math.min(frames, (int) Math.round(from * format.sampleRate()));
        int end = Math.min(frames, (int) Math.round(to * format.sampleRate()));
        if (end <= start) {
            throw new BackendException("The range is outside the clip (it is %.1f s long)".formatted(frames / (double) format.sampleRate()));
        }
        int fade = Math.max(1, (int) Math.round(FADE_SECONDS * format.sampleRate()));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (keep) {
            byte[] part = java.util.Arrays.copyOfRange(data, start * block, end * block);
            fadeIn(part, block, fade);
            fadeOut(part, block, fade);
            out.writeBytes(part);
        } else {
            if (start == 0 && end == frames) {
                throw new BackendException("Cutting the whole clip leaves nothing");
            }
            byte[] before = java.util.Arrays.copyOfRange(data, 0, start * block);
            byte[] after = java.util.Arrays.copyOfRange(data, end * block, frames * block);
            fadeOut(before, block, fade);
            fadeIn(after, block, fade);
            out.writeBytes(before);
            out.writeBytes(after);
        }
        return WavJoiner.of(format, out.toByteArray());
    }

    private static void fadeIn(byte[] pcm, int block, int fade) {
        int frames = pcm.length / block;
        int n = Math.min(fade, frames);
        for (int f = 0; f < n; f++) {
            scale(pcm, f * block, block, f / (double) n);
        }
    }

    private static void fadeOut(byte[] pcm, int block, int fade) {
        int frames = pcm.length / block;
        int n = Math.min(fade, frames);
        for (int i = 0; i < n; i++) {
            scale(pcm, (frames - 1 - i) * block, block, i / (double) n);
        }
    }

    /** Scales the 16-bit samples of one frame. */
    private static void scale(byte[] pcm, int at, int block, double gain) {
        for (int b = at; b + 1 < at + block; b += 2) {
            int sample = (short) ((pcm[b] & 0xff) | (pcm[b + 1] << 8));
            int scaled = (int) Math.round(sample * gain);
            pcm[b] = (byte) scaled;
            pcm[b + 1] = (byte) (scaled >> 8);
        }
    }
}
