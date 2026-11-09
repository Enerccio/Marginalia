package com.github.enerccio.marginalia.extensions.audiobook.audio;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * An ID3v2.3 tag for an mp3 file: title, artist, album, track, genre, and the <b>chapters</b> ({@code CHAP} frames with a {@code CTOC} table of contents),
 * which audiobook players (VLC, Audiobookshelf, Podcast apps, Foobar...) show as chapters of one long file. Pure Java, nothing is decoded.
 */
public final class Id3Tag {

    /** One chapter of a file. */
    public record Chapter(String title, long startMillis, long endMillis) {
    }

    private Id3Tag() {
    }

    /**
     * @param title    the title of the file, blank for none
     * @param track    "3/12" or "3", blank for none
     * @param chapters the chapters of the file in order, empty for none
     * @return the tag, to be put in front of the frames of the file
     */
    public static byte[] tag(String title, String artist, String album, String track, String genre, List<Chapter> chapters) {
        ByteArrayOutputStream frames = new ByteArrayOutputStream();
        text(frames, "TIT2", title);
        text(frames, "TPE1", artist);
        text(frames, "TALB", album);
        text(frames, "TRCK", track);
        text(frames, "TCON", genre);
        if (!chapters.isEmpty()) {
            for (int i = 0; i < chapters.size(); i++) {
                Chapter chapter = chapters.get(i);
                ByteArrayOutputStream body = new ByteArrayOutputStream();
                latin(body, "chp" + i);
                body.write(0);
                number(body, chapter.startMillis());
                number(body, chapter.endMillis());
                number(body, 0xFFFFFFFFL);
                number(body, 0xFFFFFFFFL);
                text(body, "TIT2", chapter.title());
                frame(frames, "CHAP", body.toByteArray());
            }
            ByteArrayOutputStream toc = new ByteArrayOutputStream();
            latin(toc, "toc");
            toc.write(0);
            toc.write(0x03);                       // top level, ordered
            toc.write(Math.min(chapters.size(), 255));
            for (int i = 0; i < Math.min(chapters.size(), 255); i++) {
                latin(toc, "chp" + i);
                toc.write(0);
            }
            frame(frames, "CTOC", toc.toByteArray());
        }
        byte[] body = frames.toByteArray();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(new byte[]{'I', 'D', '3', 3, 0, 0});
        int size = body.length;
        out.write((size >> 21) & 0x7f);
        out.write((size >> 14) & 0x7f);
        out.write((size >> 7) & 0x7f);
        out.write(size & 0x7f);
        out.writeBytes(body);
        return out.toByteArray();
    }

    /** The tag in front of the audio. */
    public static byte[] withTag(byte[] tag, byte[] audio) {
        byte[] all = new byte[tag.length + audio.length];
        System.arraycopy(tag, 0, all, 0, tag.length);
        System.arraycopy(audio, 0, all, tag.length, audio.length);
        return all;
    }

    private static void text(ByteArrayOutputStream out, String id, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.write(1);                                              // UTF-16 with a byte order mark
        body.writeBytes(new byte[]{(byte) 0xFF, (byte) 0xFE});
        body.writeBytes(value.getBytes(StandardCharsets.UTF_16LE));
        frame(out, id, body.toByteArray());
    }

    private static void frame(ByteArrayOutputStream out, String id, byte[] body) {
        latin(out, id);
        number(out, body.length);
        out.write(0);
        out.write(0);
        out.writeBytes(body);
    }

    private static void latin(ByteArrayOutputStream out, String text) {
        out.writeBytes(text.getBytes(StandardCharsets.ISO_8859_1));
    }

    private static void number(ByteArrayOutputStream out, long value) {
        out.write((int) (value >> 24) & 0xff);
        out.write((int) (value >> 16) & 0xff);
        out.write((int) (value >> 8) & 0xff);
        out.write((int) value & 0xff);
    }
}
