package com.github.enerccio.marginalia.extensions.audiobook.audio;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Which of the two formats the plugin stores a piece of audio in: mp3 (joined without decoding, {@link Mp3Joiner}) or wav ({@link WavJoiner}, lossless,
 * needs ffmpeg for everything that is not a wav). Found by looking at the first bytes, so the audio that was stored before the setting changed is still
 * understood.
 */
public enum AudioKind {
    MP3("mp3", "audio/mpeg"), WAV("wav", "audio/wav");

    private final String extension;
    private final String mime;

    AudioKind(String extension, String mime) {
        this.extension = extension;
        this.mime = mime;
    }

    public String extension() {
        return extension;
    }

    public String mime() {
        return mime;
    }

    public static AudioKind of(byte[] audio) {
        return audio != null && audio.length > 12 && audio[0] == 'R' && audio[1] == 'I' && audio[2] == 'F' && audio[3] == 'F' && audio[8] == 'W' ? WAV : MP3;
    }

    /** The kind of the audio of a file, from its first bytes. */
    public static AudioKind of(Path file) throws BackendException {
        try (InputStream in = Files.newInputStream(file)) {
            byte[] start = in.readNBytes(13);
            return start.length > 12 ? of(start) : MP3;
        } catch (IOException e) {
            throw new BackendException("The audio could not be read: " + e.getMessage(), e);
        }
    }

    /** The kind of a format name ({@code "wav"}, anything else is mp3). */
    public static AudioKind ofName(String name) {
        return "wav".equalsIgnoreCase(name) ? WAV : MP3;
    }

    public static double seconds(byte[] audio) throws BackendException {
        return of(audio) == WAV ? WavJoiner.seconds(audio) : Mp3Joiner.seconds(audio);
    }

    /** The length of the audio of a file: a wav from its header, an mp3 from its frames (the file is read, it is one part). */
    public static double seconds(Path file) throws BackendException {
        if (of(file) == WAV) {
            return WavJoiner.seconds(file);
        }
        try {
            return Mp3Joiner.seconds(Files.readAllBytes(file));
        } catch (IOException e) {
            throw new BackendException("The audio could not be read: " + e.getMessage(), e);
        }
    }

    /**
     * Joins the pieces and tells where each starts. Pieces of both kinds (a locked piece of the studio that was made before the storage changed) are
     * joined as wav: the mp3 ones are decoded by ffmpeg to the format of the wav ones.
     */
    public static Mp3Joiner.Joined joinMarked(List<byte[]> pieces, List<Double> pauses) throws BackendException {
        byte[] firstWav = pieces.stream().filter(p -> of(p) == WAV).findFirst().orElse(null);
        if (firstWav == null) {
            return Mp3Joiner.joinMarked(pieces, pauses);
        }
        WavJoiner.Format format = WavJoiner.formatOf(firstWav);
        List<byte[]> same = new java.util.ArrayList<>();
        for (byte[] piece : pieces) {
            same.add(of(piece) == WAV ? piece : Ffmpeg.toWav(piece, format));
        }
        return WavJoiner.joinMarked(same, pauses);
    }

    public static byte[] join(List<byte[]> pieces, List<Double> pauses) throws BackendException {
        return joinMarked(pieces, pauses).audio();
    }
}
