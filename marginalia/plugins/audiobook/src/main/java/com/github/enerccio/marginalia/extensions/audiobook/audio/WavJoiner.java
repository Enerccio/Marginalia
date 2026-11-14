package com.github.enerccio.marginalia.extensions.audiobook.audio;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Joins wav pieces (RIFF, uncompressed samples) into one wav, with silence between them: the lossless storage of the plugin. Pure Java. The pieces
 * must have the same format (sample rate, channels, bits), which the pieces of one server always have.
 */
public final class WavJoiner {

    private WavJoiner() {
    }

    /** The format of the samples of a wav. */
    public record Format(int tag, int channels, int sampleRate, int byteRate, int blockAlign, int bits) {

        /** Bytes of one second of audio. */
        public int bytesPerSecond() {
            return byteRate;
        }
    }

    /** Where the samples of a wav are: from {@code dataStart} to {@code dataEnd} (exclusive), in bytes from the start of the file. */
    private record Parsed(Format format, int dataStart, long dataEnd) {

        long dataLength() {
            return dataEnd - dataStart;
        }
    }

    /** The header of a wav file is read up to this much (the chunks before the samples are small). */
    private static final int HEAD_BYTES = 1 << 20;

    private static int u16(ByteBuffer b, int at) {
        return b.getShort(at) & 0xFFFF;
    }

    private static long u32(ByteBuffer b, int at) {
        return b.getInt(at) & 0xFFFFFFFFL;
    }

    private static Parsed parse(byte[] wav) throws BackendException {
        return parse(wav, wav == null ? 0 : wav.length);
    }

    /** The format and the samples of a wav file, from its first bytes (the samples themselves are not read). */
    private static Parsed parse(Path file) throws BackendException {
        try (InputStream in = Files.newInputStream(file)) {
            return parse(in.readNBytes(HEAD_BYTES), Files.size(file));
        } catch (IOException e) {
            throw new BackendException("The audio could not be read: " + e.getMessage(), e);
        }
    }

    /**
     * @param wav    the start of the file (or all of it)
     * @param length the length of the whole file
     */
    private static Parsed parse(byte[] wav, long length) throws BackendException {
        if (wav == null || wav.length < 44 || wav[0] != 'R' || wav[1] != 'I' || wav[2] != 'F' || wav[3] != 'F' || wav[8] != 'W' || wav[9] != 'A') {
            throw new BackendException("A piece is not a wav file");
        }
        ByteBuffer b = ByteBuffer.wrap(wav).order(ByteOrder.LITTLE_ENDIAN);
        Format format = null;
        int pos = 12;
        while (pos + 8 <= wav.length) {
            String id = new String(wav, pos, 4, StandardCharsets.ISO_8859_1);
            long size = u32(b, pos + 4);
            int body = pos + 8;
            if (id.equals("fmt ") && body + 16 <= wav.length) {
                int tag = u16(b, body);
                if (tag == 0xFFFE && size >= 26 && body + 26 <= wav.length) {
                    tag = u16(b, body + 24);       // extensible: the real tag is the first two bytes of the sub format
                }
                format = new Format(tag, u16(b, body + 2), (int) u32(b, body + 4), (int) u32(b, body + 8), u16(b, body + 12), u16(b, body + 14));
            } else if (id.equals("data")) {
                if (format == null) {
                    throw new BackendException("A wav piece has its data before its format");
                }
                // a stream that was cut short or written without the size says more than there is
                long end = Math.min(length, body + size);
                return new Parsed(format, body, end);
            }
            long next = body + size + (size & 1);
            if (next > wav.length) {
                break;
            }
            pos = (int) next;
        }
        throw new BackendException("A wav piece has no audio");
    }

    /** The format of the wav. */
    public static Format formatOf(byte[] wav) throws BackendException {
        return parse(wav).format();
    }

    /** The format of the wav file. */
    public static Format formatOf(Path wav) throws BackendException {
        return parse(wav).format();
    }

    /** The length of the wav in seconds. */
    public static double seconds(byte[] wav) throws BackendException {
        return seconds(parse(wav));
    }

    /** The length of the wav file in seconds (only its header is read). */
    public static double seconds(Path wav) throws BackendException {
        return seconds(parse(wav));
    }

    private static double seconds(Parsed p) {
        return p.format().byteRate() <= 0 ? 0 : (double) p.dataLength() / p.format().byteRate();
    }

    /**
     * Where the pieces start in the joined audio and how long it is, in seconds.
     */
    public record Marks(Format format, double[] starts, double seconds) {
    }

    /** The checks that the pieces can be joined: one format, and samples where zero is silence. */
    private static Format commonFormat(Parsed[] parsed) throws BackendException {
        for (Parsed p : parsed) {
            if (!p.format().equals(parsed[0].format())) {
                throw new BackendException("The pieces differ in sample rate, channels or bits");
            }
        }
        Format format = parsed[0].format();
        if (format.bits() == 8 || format.blockAlign() <= 0 || format.byteRate() <= 0) {
            // 8 bit samples are unsigned: a zero is not silence there
            throw new BackendException("This kind of wav can't be joined (" + format.bits() + " bits)");
        }
        return format;
    }

    /** The bytes of silence after a piece, a whole number of sample frames. */
    private static long silence(List<Double> pauses, int i, int count, Format format) {
        double seconds = pauses == null || i >= pauses.size() || i == count - 1 || pauses.get(i) == null ? 0 : pauses.get(i);
        long silence = Math.round(seconds * format.byteRate());
        return silence - silence % format.blockAlign();
    }

    /**
     * Joins wav files into the stream as bare samples (no header, any length: {@code -f s16le} and the like for ffmpeg, see
     * {@link Ffmpeg.Input#raw}), with silence between them. Only the headers of the files are held in memory, the samples are copied.
     *
     * @param pauses seconds of silence after each piece (the last one is not used); null or short means no pause
     */
    public static Marks joinRaw(List<Path> pieces, List<Double> pauses, OutputStream out) throws BackendException, IOException {
        if (pieces.isEmpty()) {
            throw new BackendException("There is nothing to join");
        }
        Parsed[] parsed = new Parsed[pieces.size()];
        for (int i = 0; i < parsed.length; i++) {
            parsed[i] = parse(pieces.get(i));
        }
        Format format = commonFormat(parsed);
        double[] starts = new double[parsed.length];
        byte[] buffer = new byte[64 * 1024];
        long written = 0;
        for (int i = 0; i < parsed.length; i++) {
            starts[i] = (double) written / format.byteRate();
            try (InputStream in = Files.newInputStream(pieces.get(i))) {
                in.skipNBytes(parsed[i].dataStart());
                long left = parsed[i].dataLength();
                while (left > 0) {
                    int read = in.read(buffer, 0, (int) Math.min(buffer.length, left));
                    if (read < 0) {
                        throw new BackendException("A wav piece is shorter than its header says");
                    }
                    out.write(buffer, 0, read);
                    left -= read;
                }
            }
            written += parsed[i].dataLength();
            long silence = silence(pauses, i, parsed.length, format);
            if (silence > 0) {
                java.util.Arrays.fill(buffer, (byte) 0);
                for (long left = silence; left > 0; left -= buffer.length) {
                    out.write(buffer, 0, (int) Math.min(buffer.length, left));
                }
                written += silence;
            }
        }
        return new Marks(format, starts, (double) written / format.byteRate());
    }

    /**
     * @param pauses seconds of silence after each piece (the last one is not used); null or short means no pause
     */
    public static Mp3Joiner.Joined joinMarked(List<byte[]> pieces, List<Double> pauses) throws BackendException {
        if (pieces.isEmpty()) {
            throw new BackendException("There is nothing to join");
        }
        Parsed[] parsed = new Parsed[pieces.size()];
        for (int i = 0; i < parsed.length; i++) {
            parsed[i] = parse(pieces.get(i));
        }
        Format format = commonFormat(parsed);
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        double[] starts = new double[parsed.length];
        for (int i = 0; i < parsed.length; i++) {
            starts[i] = (double) data.size() / format.byteRate();
            data.write(pieces.get(i), parsed[i].dataStart(), (int) parsed[i].dataLength());
            long silence = silence(pauses, i, parsed.length, format);
            if (silence > 0) {
                data.write(new byte[(int) silence], 0, (int) silence);
            }
        }
        if (data.size() > Integer.MAX_VALUE - 64) {
            throw new BackendException("The wav is too long for one file; store the audio as mp3");
        }
        return new Mp3Joiner.Joined(withHeader(format, data.toByteArray()), starts);
    }

    private static byte[] withHeader(Format f, byte[] data) {
        ByteBuffer b = ByteBuffer.allocate(44 + data.length).order(ByteOrder.LITTLE_ENDIAN);
        b.put(new byte[]{'R', 'I', 'F', 'F'}).putInt(36 + data.length).put(new byte[]{'W', 'A', 'V', 'E', 'f', 'm', 't', ' '}).putInt(16);
        b.putShort((short) f.tag()).putShort((short) f.channels()).putInt(f.sampleRate()).putInt(f.byteRate()).putShort((short) f.blockAlign())
                .putShort((short) f.bits());
        b.put(new byte[]{'d', 'a', 't', 'a'}).putInt(data.length).put(data);
        return b.array();
    }

    /** A wav file of the samples (for the tests). */
    public static byte[] of(Format f, byte[] data) {
        return withHeader(f, data);
    }
}
