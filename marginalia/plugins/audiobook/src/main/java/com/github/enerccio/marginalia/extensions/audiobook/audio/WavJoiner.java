package com.github.enerccio.marginalia.extensions.audiobook.audio;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
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

    private record Parsed(Format format, int dataStart, int dataEnd) {
    }

    private static int u16(ByteBuffer b, int at) {
        return b.getShort(at) & 0xFFFF;
    }

    private static long u32(ByteBuffer b, int at) {
        return b.getInt(at) & 0xFFFFFFFFL;
    }

    private static Parsed parse(byte[] wav) throws BackendException {
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
                long end = Math.min(wav.length, body + size);
                return new Parsed(format, body, (int) end);
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

    /** The length of the wav in seconds. */
    public static double seconds(byte[] wav) throws BackendException {
        Parsed p = parse(wav);
        return p.format().byteRate() <= 0 ? 0 : (double) (p.dataEnd() - p.dataStart()) / p.format().byteRate();
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
            Format f = parsed[i].format();
            if (!f.equals(parsed[0].format())) {
                throw new BackendException("The pieces differ in sample rate, channels or bits");
            }
        }
        Format format = parsed[0].format();
        if (format.bits() == 8 || format.blockAlign() <= 0 || format.byteRate() <= 0) {
            // 8 bit samples are unsigned: a zero is not silence there
            throw new BackendException("This kind of wav can't be joined (" + format.bits() + " bits)");
        }
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        double[] starts = new double[parsed.length];
        for (int i = 0; i < parsed.length; i++) {
            starts[i] = (double) data.size() / format.byteRate();
            data.write(pieces.get(i), parsed[i].dataStart(), parsed[i].dataEnd() - parsed[i].dataStart());
            double seconds = pauses == null || i >= pauses.size() || i == parsed.length - 1 || pauses.get(i) == null ? 0 : pauses.get(i);
            long silence = Math.round(seconds * format.byteRate());
            silence -= silence % format.blockAlign();
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
