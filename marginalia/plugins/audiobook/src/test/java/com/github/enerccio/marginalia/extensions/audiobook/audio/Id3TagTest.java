package com.github.enerccio.marginalia.extensions.audiobook.audio;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Id3TagTest {

    /** The frames of a tag: id and body (a minimal reader of ID3v2.3, to see that what is written can be read). */
    private static List<Map.Entry<String, byte[]>> frames(byte[] tag) {
        assertEquals("ID3", new String(tag, 0, 3, StandardCharsets.ISO_8859_1));
        assertEquals(3, tag[3], "version 2.3");
        int size = (tag[6] & 0x7f) << 21 | (tag[7] & 0x7f) << 14 | (tag[8] & 0x7f) << 7 | (tag[9] & 0x7f);
        assertEquals(tag.length - 10, size, "the size in the header is the size of the frames");
        List<Map.Entry<String, byte[]>> out = new ArrayList<>();
        for (int pos = 10; pos < tag.length; ) {
            String id = new String(tag, pos, 4, StandardCharsets.ISO_8859_1);
            int length = (tag[pos + 4] & 0xff) << 24 | (tag[pos + 5] & 0xff) << 16 | (tag[pos + 6] & 0xff) << 8 | (tag[pos + 7] & 0xff);
            byte[] body = new byte[length];
            System.arraycopy(tag, pos + 10, body, 0, length);
            out.add(Map.entry(id, body));
            pos += 10 + length;
        }
        return out;
    }

    private static String text(byte[] body) {
        assertEquals(1, body[0], "UTF-16 with a byte order mark");
        assertEquals((byte) 0xFF, body[1]);
        assertEquals((byte) 0xFE, body[2]);
        return new String(body, 3, body.length - 3, StandardCharsets.UTF_16LE);
    }

    private static long number(byte[] b, int pos) {
        return ((long) (b[pos] & 0xff) << 24) | (b[pos + 1] & 0xff) << 16 | (b[pos + 2] & 0xff) << 8 | (b[pos + 3] & 0xff);
    }

    @Test
    void theTextFramesAreWrittenInUtf16AndBlankOnesAreLeftOut() {
        byte[] tag = Id3Tag.tag("Chapter one: Mizuki ミズキ", "An Author", "The Book", "3/12", "Audiobook", List.of());

        Map<String, String> texts = new LinkedHashMap<>();
        frames(tag).forEach(f -> texts.put(f.getKey(), text(f.getValue())));

        assertEquals("Chapter one: Mizuki ミズキ", texts.get("TIT2"));
        assertEquals("An Author", texts.get("TPE1"));
        assertEquals("The Book", texts.get("TALB"));
        assertEquals("3/12", texts.get("TRCK"));
        assertEquals("Audiobook", texts.get("TCON"));
        assertEquals(5, texts.size());
        assertEquals(4, frames(Id3Tag.tag("T", " ", null, "", "", List.of())).size() + 3, "only the title is there");
    }

    @Test
    void theChaptersAreChapFramesWithATableOfContents() {
        byte[] tag = Id3Tag.tag("Book", "A", "Book", "", "", List.of(new Id3Tag.Chapter("One", 0, 90_500), new Id3Tag.Chapter("Two: é", 90_500, 200_000)));

        var frames = frames(tag);
        var chapters = frames.stream().filter(f -> f.getKey().equals("CHAP")).toList();
        var toc = frames.stream().filter(f -> f.getKey().equals("CTOC")).toList();

        assertEquals(2, chapters.size());
        byte[] second = chapters.get(1).getValue();
        // element id "chp1" and a null, then start, end, and the two "no offset" values
        assertEquals("chp1", new String(second, 0, 4, StandardCharsets.ISO_8859_1));
        assertEquals(0, second[4]);
        assertEquals(90_500, number(second, 5));
        assertEquals(200_000, number(second, 9));
        assertEquals(0xFFFFFFFFL, number(second, 13));
        assertEquals(0xFFFFFFFFL, number(second, 17));
        // the title of the chapter is a TIT2 subframe
        assertEquals("TIT2", new String(second, 21, 4, StandardCharsets.ISO_8859_1));
        assertEquals("Two: é", text(java.util.Arrays.copyOfRange(second, 31, second.length)));
        // the table of contents names the chapters in order
        assertEquals(1, toc.size());
        byte[] contents = toc.getFirst().getValue();
        assertEquals("toc", new String(contents, 0, 3, StandardCharsets.ISO_8859_1));
        assertEquals(0x03, contents[4], "top level and ordered");
        assertEquals(2, contents[5]);
        assertEquals("chp0", new String(contents, 6, 4, StandardCharsets.ISO_8859_1));
    }

    @Test
    void theTagGoesInFrontOfTheAudioWhichStaysAsItIs() {
        byte[] tag = Id3Tag.tag("T", "", "", "", "", List.of());
        byte[] audio = {1, 2, 3};

        byte[] all = Id3Tag.withTag(tag, audio);

        assertEquals(tag.length + 3, all.length);
        assertArrayEquals(audio, java.util.Arrays.copyOfRange(all, tag.length, all.length));
    }
}
