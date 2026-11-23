package com.github.enerccio.marginalia.extensions.audiobook.audio;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;

import java.util.ArrayList;
import java.util.List;

/**
 * Joins reference clips into one, one after the other, with no pause between them (a longer reference from short takes of one voice). Wav clips of
 * one format are joined here; anything else (an mp3, wavs of different rates) is first made a 16-bit wav in the format of the first clip by ffmpeg.
 */
public final class ClipMerge {

    /** The format of the result when the first clip is not a wav: what the measuring of clips uses. */
    static final WavJoiner.Format DEFAULT = pcm16(1, 24000);

    private ClipMerge() {
    }

    /** @return a wav of the clips in their order */
    public static byte[] merge(List<byte[]> clips) throws BackendException {
        if (clips == null || clips.size() < 2) {
            throw new BackendException("Pick two clips or more to merge");
        }
        for (byte[] clip : clips) {
            if (clip == null || clip.length == 0) {
                throw new BackendException("A clip is empty");
            }
        }
        boolean allWav = clips.stream().allMatch(c -> AudioKind.of(c) == AudioKind.WAV);
        BackendException direct = null;
        if (allWav) {
            try {
                return WavJoiner.joinMarked(clips, null).audio();
            } catch (BackendException e) {
                direct = e;
            }
        }
        if (!Ffmpeg.available()) {
            throw new BackendException(direct != null ? "The clips differ in sample rate, channels or bits; merging them needs ffmpeg"
                    : "Only wav clips can be merged without ffmpeg");
        }
        WavJoiner.Format target = DEFAULT;
        if (AudioKind.of(clips.getFirst()) == AudioKind.WAV) {
            WavJoiner.Format first = WavJoiner.formatOf(clips.getFirst());
            target = pcm16(first.channels(), first.sampleRate());
        }
        List<byte[]> wavs = new ArrayList<>();
        for (byte[] clip : clips) {
            wavs.add(Ffmpeg.toWav(clip, target));
        }
        return WavJoiner.joinMarked(wavs, null).audio();
    }

    static WavJoiner.Format pcm16(int channels, int sampleRate) {
        return new WavJoiner.Format(1, channels, sampleRate, sampleRate * channels * 2, channels * 2, 16);
    }
}
