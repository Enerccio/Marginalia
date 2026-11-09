package com.github.enerccio.marginalia.extensions.audiobook.audio;

import com.github.enerccio.marginalia.extensions.audiobook.export.AudiobookFiles;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class WavJoinerTest {

    /** 16 bit mono at 8000 Hz: 16000 bytes a second. */
    private static final WavJoiner.Format FORMAT = new WavJoiner.Format(1, 1, 8000, 16000, 2, 16);

    private static byte[] wav(double seconds, int sample) {
        byte[] data = new byte[(int) Math.round(seconds * FORMAT.byteRate())];
        for (int i = 0; i + 1 < data.length; i += 2) {
            data[i] = (byte) sample;
        }
        return WavJoiner.of(FORMAT, data);
    }

    @Test
    void piecesAreJoinedWithTheSilenceOfThePauses() throws Exception {
        Mp3Joiner.Joined joined = WavJoiner.joinMarked(List.of(wav(1, 7), wav(2, 7), wav(1, 7)), List.of(0.5, 0.25));

        // 1 + 0.5 + 2 + 0.25 + 1
        assertEquals(4.75, WavJoiner.seconds(joined.audio()), 0.0001);
        assertArrayEquals(new double[]{0, 1.5, 3.75}, joined.starts(), 0.0001);
        assertEquals(AudioKind.WAV, AudioKind.of(joined.audio()));
        assertEquals(FORMAT, WavJoiner.formatOf(joined.audio()));
    }

    @Test
    void thePauseIsSilence() throws Exception {
        byte[] joined = WavJoiner.joinMarked(List.of(wav(0.01, 5), wav(0.01, 5)), List.of(0.01)).audio();

        // after the first piece (160 bytes of 5s) come 160 bytes of zeros
        assertEquals(5, joined[44]);
        assertEquals(0, joined[44 + 160]);
        assertEquals(0, joined[44 + 160 + 159]);
        assertEquals(5, joined[44 + 320]);
    }

    @Test
    void piecesOfAnotherFormatAreRefused() {
        byte[] other = WavJoiner.of(new WavJoiner.Format(1, 1, 16000, 32000, 2, 16), new byte[320]);

        assertThrows(BackendException.class, () -> WavJoiner.joinMarked(List.of(wav(0.1, 1), other), null));
    }

    @Test
    void somethingThatIsNotAWavIsRefused() {
        assertThrows(BackendException.class, () -> WavJoiner.seconds("not a wav at all, really not a wav".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void aDataChunkThatSaysMoreThanThereIsIsCutAtTheEnd() throws Exception {
        byte[] wav = wav(1, 1);
        // a streamed file says 0xFFFFFFFF
        wav[40] = wav[41] = wav[42] = wav[43] = (byte) 0xFF;

        assertEquals(1.0, WavJoiner.seconds(wav), 0.0001);
    }

    @Test
    void theKindIsFoundByLookingAtTheBytes() throws IOException {
        assertEquals(AudioKind.WAV, AudioKind.of(wav(0.1, 1)));
        try (var in = WavJoinerTest.class.getResourceAsStream("/tone.mp3")) {
            assertEquals(AudioKind.MP3, AudioKind.of(in.readAllBytes()));
        }
    }

    @Test
    void anAudiobookOfWavPartsIsAnMp3WithChaptersAndAnM4bEncodedOnce() throws Exception {
        assumeTrue(Ffmpeg.available(), "ffmpeg is not installed");
        List<AudiobookFiles.Chapter> chapters = List.of(new AudiobookFiles.Chapter("One", wav(2, 3)), new AudiobookFiles.Chapter("Two", wav(2, 3)));

        byte[] mp3 = AudiobookFiles.oneFile(chapters, "Book", "Author");
        byte[] m4b = AudiobookFiles.m4b(chapters, "Book", "Author");

        assertEquals("ID3", new String(mp3, 0, 3, StandardCharsets.ISO_8859_1));
        assertTrue(new String(mp3, StandardCharsets.ISO_8859_1).contains("CHAP"));
        assertEquals("ftyp", new String(m4b, 4, 4, StandardCharsets.ISO_8859_1));
        assertTrue(new String(m4b, StandardCharsets.ISO_8859_1).contains("chpl"));
    }

    @Test
    void aBookOfBothKindsIsJoinedAsWav() throws Exception {
        assumeTrue(Ffmpeg.available(), "ffmpeg is not installed");
        byte[] mp3;
        try (var in = WavJoinerTest.class.getResourceAsStream("/tone.mp3")) {
            mp3 = in.readAllBytes();
        }

        Mp3Joiner.Joined joined = AudioKind.joinMarked(List.of(wav(1, 3), mp3), List.of(0.0));

        assertEquals(AudioKind.WAV, AudioKind.of(joined.audio()));
        assertEquals(1 + Mp3Joiner.seconds(mp3), WavJoiner.seconds(joined.audio()), 0.1);
    }

    @Test
    void anExportOfPartsOfBothKindsWorksInEveryFormat() throws Exception {
        assumeTrue(Ffmpeg.available(), "ffmpeg is not installed");
        byte[] mp3;
        try (var in = WavJoinerTest.class.getResourceAsStream("/tone.mp3")) {
            mp3 = in.readAllBytes();
        }
        // one part was made when the storage was mp3, the next two after it was changed to wav, and the wav is not at the format of the mp3
        List<AudiobookFiles.Chapter> chapters = List.of(new AudiobookFiles.Chapter("Old", mp3), new AudiobookFiles.Chapter("New", wav(2, 3)),
                new AudiobookFiles.Chapter("Newer", wav(1, 3)));

        byte[] one = AudiobookFiles.oneFile(chapters, "Book", "Author");
        byte[] m4b = AudiobookFiles.m4b(chapters, "Book", "Author");
        byte[] zip = AudiobookFiles.zip(chapters, "Book", "Author");

        assertEquals("ID3", new String(one, 0, 3, StandardCharsets.ISO_8859_1));
        // three chapter frames (the titles are UTF-16 in the tag)
        assertEquals(3, new String(one, StandardCharsets.ISO_8859_1).split("CHAP", -1).length - 1);
        assertEquals("ftyp", new String(m4b, 4, 4, StandardCharsets.ISO_8859_1));
        assertTrue(zip.length > 1000);
        // the mp3 file is as long as the three chapters and the two pauses
        double expected = Mp3Joiner.seconds(mp3) + 2 + 1 + 2 * AudiobookFiles.PAUSE_BETWEEN_CHAPTERS;
        assertEquals(expected, Mp3Joiner.seconds(java.util.Arrays.copyOfRange(one, 10 + ((one[6] & 0x7F) << 21 | (one[7] & 0x7F) << 14 | (one[8] & 0x7F) << 7 | (one[9] & 0x7F)), one.length)), 0.5);
    }
}
