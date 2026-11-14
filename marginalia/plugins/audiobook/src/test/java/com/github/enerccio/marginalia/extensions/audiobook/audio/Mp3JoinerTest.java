package com.github.enerccio.marginalia.extensions.audiobook.audio;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** The sample is 0.4 s of a tone made by ffmpeg: 24 kHz mono 32 kbps, an ID3v2 tag in front and an Info frame as the first frame, as the server's pieces are. */
class Mp3JoinerTest {

    private static byte[] tone() throws IOException {
        try (var in = Mp3JoinerTest.class.getResourceAsStream("/tone.mp3")) {
            return in.readAllBytes();
        }
    }

    private static int count(byte[] haystack, String needle) {
        int found = 0;
        byte[] n = needle.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        for (int i = 0; i + n.length <= haystack.length; i++) {
            if (Arrays.equals(haystack, i, i + n.length, n, 0, n.length)) {
                found++;
            }
        }
        return found;
    }

    @Test
    void filesAreJoinedIntoTheStreamAsTheJoinInMemoryDoes() throws Exception {
        byte[] tone = tone();
        Mp3Joiner.Joined inMemory = Mp3Joiner.joinMarked(List.of(tone, tone, tone), List.of(1.0, 0.5));
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        java.nio.file.Path file = com.github.enerccio.marginalia.extensions.audiobook.export.AudiobookFilesFixture.file(tone);

        Mp3Joiner.Marks marks = Mp3Joiner.joinTo(List.of(file, file, file), List.of(1.0, 0.5), out);

        assertArrayEquals(inMemory.audio(), out.toByteArray());
        assertArrayEquals(inMemory.starts(), marks.starts(), 0.0001);
        assertEquals(Mp3Joiner.seconds(inMemory.audio()), marks.seconds(), 0.0001);
        assertEquals(Mp3Joiner.seconds(tone), AudioKind.seconds(file), 0.0001);
    }

    @Test
    void theTagsAndTheInfoFrameAreCutOff() throws Exception {
        byte[] tone = tone();
        assertEquals(1, count(tone, "ID3"));
        assertEquals(1, count(tone, "Info"));

        byte[] one = Mp3Joiner.join(List.of(tone), null);

        assertEquals(0, count(one, "ID3"));
        assertEquals(0, count(one, "Info"));
        assertEquals(0xff, one[0] & 0xff, "starts with a frame");
        assertEquals(Mp3Joiner.seconds(tone), Mp3Joiner.seconds(one), 0.001);
    }

    @Test
    void piecesAreJoinedWithSilentFramesForThePause() throws Exception {
        byte[] tone = tone();
        double audio = Mp3Joiner.seconds(tone);

        byte[] joined = Mp3Joiner.join(List.of(tone, tone, tone), List.of(0.5, 1.0, 9.0));

        // 0.5 s and 1.0 s after the first two, nothing after the last one whatever the list says; a frame is 24 ms
        assertEquals(3 * audio + 1.5, Mp3Joiner.seconds(joined), 0.03);
        assertEquals(0, count(joined, "Info"));
    }

    @Test
    void noPauseMeansNoSilence() throws Exception {
        byte[] tone = tone();

        assertEquals(2 * Mp3Joiner.seconds(tone), Mp3Joiner.seconds(Mp3Joiner.join(List.of(tone, tone), List.of(0.0))), 0.001);
        assertEquals(2 * Mp3Joiner.seconds(tone), Mp3Joiner.seconds(Mp3Joiner.join(List.of(tone, tone), null)), 0.001);
    }

    @Test
    void aSilentFrameHasNothingButItsHeader() throws Exception {
        byte[] tone = tone();
        byte[] withPause = Mp3Joiner.join(List.of(tone, tone), List.of(0.024));
        byte[] without = Mp3Joiner.join(List.of(tone, tone), null);

        assertEquals(96, withPause.length - without.length, "one frame of 24 kHz 32 kbps is 96 bytes");
        // the frame sits after the first piece
        int at = Mp3Joiner.join(List.of(tone), null).length;
        assertEquals(0xff, withPause[at] & 0xff);
        assertEquals(0xf3, withPause[at + 1] & 0xff, "MPEG 2, Layer III, no CRC");
        for (int i = at + 4; i < at + 96; i++) {
            assertEquals(0, withPause[i], "byte " + (i - at));
        }
    }

    @Test
    void aPieceInAnotherSampleRateIsRefused() throws Exception {
        byte[] tone = tone();
        byte[] other;
        try (var in = Mp3JoinerTest.class.getResourceAsStream("/tone16.mp3")) {
            other = in.readAllBytes();      // 16 kHz mono
        }

        var e = assertThrows(BackendException.class, () -> Mp3Joiner.join(List.of(tone, other), null));
        assertTrue(e.getMessage().contains("Piece 2"), e.getMessage());
    }

    @Test
    void somethingThatIsNotMp3IsRefused() {
        assertThrows(BackendException.class, () -> Mp3Joiner.join(List.of("not audio at all, just text".getBytes()), null));
        assertThrows(BackendException.class, () -> Mp3Joiner.join(List.of(), null));
        assertThrows(BackendException.class, () -> Mp3Joiner.join(List.of(new byte[0]), null));
    }

    @Test
    void anId3v1TagAtTheEndIsCutOff() throws Exception {
        byte[] tone = tone();
        byte[] tagged = Arrays.copyOf(tone, tone.length + 128);
        tagged[tone.length] = 'T';
        tagged[tone.length + 1] = 'A';
        tagged[tone.length + 2] = 'G';

        assertEquals(0, count(Mp3Joiner.join(List.of(tagged, tone), null), "TAG"));
        assertEquals(2 * Mp3Joiner.seconds(tone), Mp3Joiner.seconds(Mp3Joiner.join(List.of(tagged, tone), null)), 0.001);
    }

    @Test
    void joinMarkedTellsWhereEveryPieceStarts() throws Exception {
        byte[] tone = tone();
        double length = Mp3Joiner.seconds(tone);

        Mp3Joiner.Joined joined = Mp3Joiner.joinMarked(List.of(tone, tone, tone), List.of(1.0, 0.5, 9.0));

        assertEquals(3, joined.starts().length);
        assertEquals(0, joined.starts()[0]);
        // a pause is a whole number of frames, so the second piece starts about a second after the first one ends
        assertEquals(length + 1.0, joined.starts()[1], 0.03);
        assertEquals(2 * length + 1.5, joined.starts()[2], 0.06);
        // the last pause is not made; the audio is as long as the pieces and the two pauses
        assertEquals(3 * length + 1.5, Mp3Joiner.seconds(joined.audio()), 0.06);
        assertArrayEquals(Mp3Joiner.join(List.of(tone, tone, tone), List.of(1.0, 0.5, 9.0)), joined.audio());
    }
}
