package com.github.enerccio.marginalia.extensions.audiobook.audio;

import com.github.enerccio.marginalia.extensions.audiobook.export.AudiobookFiles;
import com.github.enerccio.marginalia.extensions.audiobook.export.AudiobookFilesFixture;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class FfmpegTest {

    private static byte[] tone() throws IOException {
        try (var in = FfmpegTest.class.getResourceAsStream("/tone.mp3")) {
            return in.readAllBytes();
        }
    }

    @Test
    void theMetadataFileNamesTheChaptersInMilliseconds() {
        String text = Ffmpeg.metadata("The Book", "An Author", "The Book", List.of(new Id3Tag.Chapter("One", 0, 1500), new Id3Tag.Chapter("Two", 1500, 4000)));

        assertTrue(text.startsWith(";FFMETADATA1\n"));
        assertTrue(text.contains("title=The Book\nartist=An Author\nalbum=The Book\ngenre=Audiobook\n"));
        assertTrue(text.contains("[CHAPTER]\nTIMEBASE=1/1000\nSTART=0\nEND=1500\ntitle=One\n"));
        assertTrue(text.contains("START=1500\nEND=4000\ntitle=Two\n"));
    }

    @Test
    void theCharactersThatMeanSomethingInTheFileAreEscaped() {
        assertEquals("a\\=b\\;c\\#d\\\\e", Ffmpeg.escape("a=b;c#d\\e"));
        assertEquals("two\\\nlines", Ffmpeg.escape("two\nlines"));
    }

    @Test
    void anM4bHasTheChaptersAndTheTags() throws Exception {
        assumeTrue(Ffmpeg.available(), "ffmpeg is not installed");
        byte[] tone = tone();

        byte[] m4b = AudiobookFilesFixture.m4b(List.of(AudiobookFilesFixture.chapter("The Arch", tone), AudiobookFilesFixture.chapter("Who", tone)), "The Book", "An Author");

        String head = new String(m4b, 0, 32, StandardCharsets.ISO_8859_1);
        assertEquals("ftyp", head.substring(4, 8));
        assertEquals("M4B ", head.substring(8, 12));
        String all = new String(m4b, StandardCharsets.ISO_8859_1);
        assertTrue(all.contains("chpl"), "the chapter list is there");
        assertTrue(all.contains("The Arch") && all.contains("Who"), "with the titles");
        assertTrue(all.contains("An Author"));
    }

    /** A mono 24 kHz 16 bit wav of a sine at the given peak amplitude (0 to 1). */
    private static byte[] sine(double seconds, double amplitude) {
        int rate = 24000;
        int samples = (int) (seconds * rate);
        java.nio.ByteBuffer wav = java.nio.ByteBuffer.allocate(44 + samples * 2).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        wav.put("RIFF".getBytes(StandardCharsets.US_ASCII)).putInt(36 + samples * 2).put("WAVEfmt ".getBytes(StandardCharsets.US_ASCII)).putInt(16)
                .putShort((short) 1).putShort((short) 1).putInt(rate).putInt(rate * 2).putShort((short) 2).putShort((short) 16)
                .put("data".getBytes(StandardCharsets.US_ASCII)).putInt(samples * 2);
        for (int i = 0; i < samples; i++) {
            // a tone with a slow wobble in the level, like speech has
            double envelope = 0.6 + 0.4 * Math.sin(2 * Math.PI * 0.7 * i / rate);
            wav.putShort((short) (Math.sin(2 * Math.PI * 220 * i / rate) * amplitude * envelope * 32767));
        }
        return wav.array();
    }

    @Test
    void aQuietPartIsLevelledToTheTargetInTheKindItWasAndAtItsRate() throws Exception {
        assumeTrue(Ffmpeg.available(), "ffmpeg is not installed");
        byte[] quiet = sine(8, 0.02);
        Ffmpeg.Loudness before = Ffmpeg.measure(quiet, -18);
        assertTrue(before.integrated() < -30, "the test tone is quiet: " + before.integrated());

        byte[] levelled = Ffmpeg.normalizeLoudness(quiet, -18);

        assertEquals(AudioKind.WAV, AudioKind.of(levelled), "a wav stays a wav");
        Ffmpeg.Loudness after = Ffmpeg.measure(levelled, -18);
        assertEquals(-18, after.integrated(), 1.0, "the loudness of the result: " + after.integrated());
        assertEquals(24000, after.sampleRate());
        assertTrue(after.truePeak() <= -1.0, "the peak stays under the limit: " + after.truePeak());
        assertEquals(AudioKind.seconds(quiet), AudioKind.seconds(levelled), 0.2, "the length is the same");
    }

    @Test
    void anMp3IsLevelledAsAnMp3() throws Exception {
        assumeTrue(Ffmpeg.available(), "ffmpeg is not installed");
        byte[] mp3 = Ffmpeg.toMp3(sine(8, 0.02));

        byte[] levelled = Ffmpeg.normalizeLoudness(mp3, -18);

        assertEquals(AudioKind.MP3, AudioKind.of(levelled));
        assertEquals(-18, Ffmpeg.measure(levelled, -18).integrated(), 1.0);
    }

    @Test
    void audioThatIsAlreadyAtTheTargetIsNotEncodedAgain() throws Exception {
        assumeTrue(Ffmpeg.available(), "ffmpeg is not installed");
        byte[] levelled = Ffmpeg.normalizeLoudness(sine(8, 0.02), -18);

        assertSame(levelled, Ffmpeg.normalizeLoudness(levelled, -18), "within the tolerance: the same bytes");
    }

    @Test
    void silenceHasNoLoudnessAndIsLeftAlone() throws Exception {
        assumeTrue(Ffmpeg.available(), "ffmpeg is not installed");
        byte[] silence = sine(3, 0);

        assertNull(Ffmpeg.measure(silence, -18));
        assertSame(silence, Ffmpeg.normalizeLoudness(silence, -18));
    }
}
