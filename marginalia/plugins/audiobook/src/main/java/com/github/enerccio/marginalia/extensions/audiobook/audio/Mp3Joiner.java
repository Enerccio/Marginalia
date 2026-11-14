package com.github.enerccio.marginalia.extensions.audiobook.audio;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Joins mp3 pieces into one mp3 without decoding or encoding them (the application's image has no ffmpeg, and Java can't read or write mp3):
 * the tags in front of and behind each piece and the Xing / Info frame of the encoder are cut off, the frames are put one after the other and
 * a pause is a number of silent frames, made from the header of the first frame of the first piece (so they have its sample rate and mode).
 * A silent frame is a header followed by zeros: no side information and no data, which decodes to silence.
 * <p>
 * What this does not do: change the loudness of a piece (that needs a decoder; the voices of a book differ in level) and cross-fade. The
 * encoder delay of a piece (about 50 ms at 24 kHz) is left in, so the gap between pieces is a little longer than the pause.
 * The pieces must be Layer III with one sample rate and channel mode, which the server's output is.
 */
public final class Mp3Joiner {

    private static final int[][] BITRATES = {
            {0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320},   // MPEG 1
            {0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160}};      // MPEG 2 and 2.5
    private static final int[][] SAMPLE_RATES = {{44100, 48000, 32000}, {22050, 24000, 16000}, {11025, 12000, 8000}};

    private Mp3Joiner() {
    }

    /** The format of the frames of a piece. */
    private record Header(int versionBits, int bitrateIndex, int sampleRateIndex, int channelMode, int bitrate, int sampleRate, int samplesPerFrame,
                          boolean mpeg1, int frameBytes) {

        /** Pieces can be joined when they have the same of these. */
        String format() {
            return versionBits + "/" + sampleRateIndex + "/" + channelMode;
        }
    }

    /** The audio of a piece: where its first frame starts and ends the data, and the format. */
    private record Audio(byte[] bytes, int start, int end, Header header) {
    }

    /**
     * @param pauses seconds of silence after each piece (the last one is not used); null or short means no pause
     * @throws BackendException when a piece is not mp3 (Layer III), or the pieces differ in sample rate or channel mode
     */
    public static byte[] join(List<byte[]> pieces, List<Double> pauses) throws BackendException {
        return joinMarked(pieces, pauses).audio();
    }

    /**
     * The joined audio and where each piece starts in it.
     *
     * @param starts seconds from the start of the audio to the first frame of each piece (the silence of a pause belongs to the piece before it)
     */
    public record Joined(byte[] audio, double[] starts) {
    }

    /** Like {@link #join}, and tells where every piece starts (for the chapter marks of one file made of several parts). */
    public static Joined joinMarked(List<byte[]> pieces, List<Double> pauses) throws BackendException {
        if (pieces.isEmpty()) {
            throw new BackendException("There is nothing to join");
        }
        // all of them are checked before anything is joined
        Audio[] parsed = new Audio[pieces.size()];
        for (int i = 0; i < parsed.length; i++) {
            parsed[i] = parse(pieces.get(i), i);
            if (!parsed[i].header().format().equals(parsed[0].header().format())) {
                throw new BackendException("Piece " + (i + 1) + " has another sample rate or channel mode than the first");
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            Marks marks = joinInto(parsed.length, i -> parsed[i], pauses, out);
            return new Joined(out.toByteArray(), marks.starts());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Where the pieces start in the joined audio and how long it is, in seconds.
     */
    public record Marks(double[] starts, double seconds) {
    }

    /**
     * Joins mp3 files into the stream: one piece is read into memory at a time (a part of a book, a few megabytes), the joined audio is
     * never held.
     *
     * @param pauses seconds of silence after each piece (the last one is not used); null or short means no pause
     * @throws BackendException when a piece is not mp3 (Layer III), or the pieces differ in sample rate or channel mode
     */
    public static Marks joinTo(List<Path> pieces, List<Double> pauses, OutputStream out) throws BackendException, IOException {
        if (pieces.isEmpty()) {
            throw new BackendException("There is nothing to join");
        }
        return joinInto(pieces.size(), i -> {
            try {
                return parse(Files.readAllBytes(pieces.get(i)), i);
            } catch (IOException e) {
                throw new BackendException("The audio could not be read: " + e.getMessage(), e);
            }
        }, pauses, out);
    }

    private interface Pieces {
        Audio get(int index) throws BackendException;
    }

    private static Marks joinInto(int count, Pieces pieces, List<Double> pauses, OutputStream out) throws BackendException, IOException {
        Header first = null;
        double[] starts = new double[count];
        long framesSoFar = 0;
        for (int i = 0; i < count; i++) {
            Audio audio = pieces.get(i);
            if (first == null) {
                first = audio.header();
            } else if (!audio.header().format().equals(first.format())) {
                throw new BackendException("Piece " + (i + 1) + " has another sample rate or channel mode than the first");
            }
            starts[i] = framesSoFar * (double) first.samplesPerFrame() / first.sampleRate();
            framesSoFar += frames(audio);
            out.write(audio.bytes(), audio.start(), audio.end() - audio.start());
            double seconds = pauses == null || i >= pauses.size() || i == count - 1 || pauses.get(i) == null ? 0 : pauses.get(i);
            int frames = (int) Math.round(seconds * first.sampleRate() / first.samplesPerFrame());
            framesSoFar += Math.max(frames, 0);
            if (frames > 0) {
                byte[] silence = silentFrame(first);
                for (int k = 0; k < frames; k++) {
                    out.write(silence, 0, silence.length);
                }
            }
        }
        return new Marks(starts, framesSoFar * (double) first.samplesPerFrame() / first.sampleRate());
    }

    private static int frames(Audio audio) {
        int frames = 0;
        for (int pos = audio.start(); pos + 4 <= audio.end(); ) {
            Header h = header(audio.bytes(), pos);
            if (h == null) {
                break;
            }
            frames++;
            pos += h.frameBytes();
        }
        return frames;
    }

    /** The length of the audio in seconds, from the frames (the pieces of the server are constant bitrate; a variable bitrate file gives an estimate). */
    public static double seconds(byte[] mp3) throws BackendException {
        Audio audio = parse(mp3, 0);
        int frames = 0;
        for (int pos = audio.start(); pos + 4 <= audio.end(); ) {
            Header h = header(audio.bytes(), pos);
            if (h == null) {
                break;
            }
            frames++;
            pos += h.frameBytes();
        }
        return frames * (double) audio.header().samplesPerFrame() / audio.header().sampleRate();
    }

    private static Audio parse(byte[] mp3, int index) throws BackendException {
        int start = 0;
        int end = mp3.length;
        if (end > 10 && mp3[0] == 'I' && mp3[1] == 'D' && mp3[2] == '3') {
            int size = (mp3[6] & 0x7f) << 21 | (mp3[7] & 0x7f) << 14 | (mp3[8] & 0x7f) << 7 | (mp3[9] & 0x7f);
            start = 10 + size + ((mp3[5] & 0x10) != 0 ? 10 : 0);
        }
        if (end - start > 128 && mp3[end - 128] == 'T' && mp3[end - 127] == 'A' && mp3[end - 126] == 'G') {
            end -= 128;
        }
        // the first real frame: a header whose next frame has a header too (a sync pattern in the middle of a tag or of data is not enough)
        for (int pos = start; pos + 4 <= end; pos++) {
            Header h = header(mp3, pos);
            if (h == null) {
                continue;
            }
            int next = pos + h.frameBytes();
            if (next != end && (next + 4 > end || header(mp3, next) == null)) {
                continue;
            }
            if (isInfoFrame(mp3, pos, h) && next < end) {
                pos = next;
                h = header(mp3, pos);
                if (h == null) {
                    break;
                }
            }
            return new Audio(mp3, pos, end, h);
        }
        throw new BackendException("Piece " + (index + 1) + " is not an mp3 (Layer III)");
    }

    /** The header of the frame at the position, null when there is none (or it is not Layer III or uses a value that is not allowed). */
    private static Header header(byte[] b, int pos) {
        if (pos + 4 > b.length || (b[pos] & 0xff) != 0xff || (b[pos + 1] & 0xe0) != 0xe0) {
            return null;
        }
        int versionBits = (b[pos + 1] >> 3) & 3;      // 3 = MPEG 1, 2 = MPEG 2, 0 = MPEG 2.5
        int layerBits = (b[pos + 1] >> 1) & 3;        // 1 = Layer III
        int bitrateIndex = (b[pos + 2] >> 4) & 15;
        int sampleRateIndex = (b[pos + 2] >> 2) & 3;
        int padding = (b[pos + 2] >> 1) & 1;
        if (versionBits == 1 || layerBits != 1 || bitrateIndex == 0 || bitrateIndex == 15 || sampleRateIndex == 3) {
            return null;
        }
        boolean mpeg1 = versionBits == 3;
        int bitrate = BITRATES[mpeg1 ? 0 : 1][bitrateIndex] * 1000;
        int sampleRate = SAMPLE_RATES[mpeg1 ? 0 : versionBits == 2 ? 1 : 2][sampleRateIndex];
        int frameBytes = (mpeg1 ? 144 : 72) * bitrate / sampleRate + padding;
        return new Header(versionBits, bitrateIndex, sampleRateIndex, (b[pos + 3] >> 6) & 3, bitrate, sampleRate, mpeg1 ? 1152 : 576, mpeg1, frameBytes);
    }

    /** The first frame of a file made by LAME or ffmpeg holds a "Xing" / "Info" header (and no audio). */
    private static boolean isInfoFrame(byte[] b, int pos, Header h) {
        boolean mono = h.channelMode() == 3;
        int sideInfo = h.mpeg1() ? (mono ? 17 : 32) : (mono ? 9 : 17);
        boolean crc = (b[pos + 1] & 1) == 0;
        int at = pos + 4 + (crc ? 2 : 0) + sideInfo;
        if (at + 4 > b.length) {
            return false;
        }
        String tag = new String(b, at, 4, java.nio.charset.StandardCharsets.ISO_8859_1);
        return tag.equals("Xing") || tag.equals("Info");
    }

    /** A frame with the format of the header and nothing in it: no CRC, no padding, zeros after the header. */
    private static byte[] silentFrame(Header h) {
        int length = (h.mpeg1() ? 144 : 72) * h.bitrate() / h.sampleRate();
        byte[] frame = new byte[length];
        frame[0] = (byte) 0xff;
        frame[1] = (byte) (0xe0 | (h.versionBits() << 3) | (1 << 1) | 1);     // Layer III, no CRC
        frame[2] = (byte) ((h.bitrateIndex() << 4) | (h.sampleRateIndex() << 2));
        frame[3] = (byte) (h.channelMode() << 6);
        return frame;
    }
}
