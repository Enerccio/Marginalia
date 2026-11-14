package com.github.enerccio.marginalia.extensions.audiobook.export;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.extensions.audiobook.audio.Mp3Joiner;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

class AudiobookExportTest {

    private static byte[] tone() throws IOException {
        try (var in = AudiobookExportTest.class.getResourceAsStream("/tone.mp3")) {
            return in.readAllBytes();
        }
    }

    private static Map<String, byte[]> unzip(byte[] zip) throws IOException {
        Map<String, byte[]> files = new LinkedHashMap<>();
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
            for (var entry = in.getNextEntry(); entry != null; entry = in.getNextEntry()) {
                files.put(entry.getName(), in.readAllBytes());
            }
        }
        return files;
    }

    private static String latin(byte[] b, int from, int length) {
        return new String(b, from, length, StandardCharsets.ISO_8859_1);
    }

    private static ChatMessage part(String text) {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        return message;
    }

    @Test
    void theZipHasATaggedFileForEveryChapterAndAPlaylist() throws Exception {
        byte[] tone = tone();

        Map<String, byte[]> files = unzip(AudiobookFilesFixture.zip(
                List.of(AudiobookFilesFixture.chapter("The Arch", tone), AudiobookFilesFixture.chapter("Who/What?", tone)), "The Book", "An Author"));

        assertEquals(List.of("01 - The Arch.mp3", "02 - Who_What_.mp3", "playlist.m3u8"), new ArrayList<>(files.keySet()));
        byte[] first = files.get("01 - The Arch.mp3");
        assertEquals("ID3", latin(first, 0, 3));
        // the audio is behind the tag, as it was: the file is as long as the tone
        int tagSize = (first[6] & 0x7f) << 21 | (first[7] & 0x7f) << 14 | (first[8] & 0x7f) << 7 | (first[9] & 0x7f);
        assertEquals(Mp3Joiner.seconds(tone), Mp3Joiner.seconds(first), 0.001);
        assertEquals(tone.length, first.length - 10 - tagSize);
        String playlist = new String(files.get("playlist.m3u8"), StandardCharsets.UTF_8);
        assertTrue(playlist.startsWith("#EXTM3U\n"));
        assertTrue(playlist.contains(",The Arch\n01 - The Arch.mp3\n"), playlist);
    }

    @Test
    void theNumbersOfTheFilesAreAsWideAsTheBookNeeds() throws Exception {
        byte[] tone = tone();
        List<AudiobookFiles.Chapter> many = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            many.add(AudiobookFilesFixture.chapter("C" + i, tone));
        }

        Map<String, byte[]> files = unzip(AudiobookFilesFixture.zip(many, "B", "A"));

        assertTrue(files.containsKey("001 - C0.mp3"));
        assertTrue(files.containsKey("100 - C99.mp3"));
    }

    @Test
    void theOneFileMarksTheChaptersWhereTheyStart() throws Exception {
        byte[] tone = tone();
        double length = Mp3Joiner.seconds(tone);

        byte[] file = AudiobookFilesFixture.oneFile(List.of(AudiobookFilesFixture.chapter("One", tone), AudiobookFilesFixture.chapter("Two", tone)), "The Book", "A");

        int tagSize = (file[6] & 0x7f) << 21 | (file[7] & 0x7f) << 14 | (file[8] & 0x7f) << 7 | (file[9] & 0x7f);
        byte[] tag = java.util.Arrays.copyOf(file, 10 + tagSize);
        String frames = latin(tag, 10, tag.length - 10);
        assertEquals(2, frames.split("CHAP", -1).length - 1, "two chapters");
        assertTrue(frames.contains("CTOC"));
        // the second chapter starts after the first and the pause between them
        int chap = frames.indexOf("CHAP", frames.indexOf("CHAP") + 1) + 10;
        long start = ((long) (tag[10 + chap + 5] & 0xff) << 24) | (tag[10 + chap + 6] & 0xff) << 16 | (tag[10 + chap + 7] & 0xff) << 8 | (tag[10 + chap + 8] & 0xff);
        assertEquals((length + AudiobookFiles.PAUSE_BETWEEN_CHAPTERS) * 1000, start, 40);
        // the audio is both chapters and the pause
        byte[] audio = java.util.Arrays.copyOfRange(file, 10 + tagSize, file.length);
        assertEquals(2 * length + AudiobookFiles.PAUSE_BETWEEN_CHAPTERS, Mp3Joiner.seconds(audio), 0.1);
    }

    @Test
    void aPartIsTitledByItsHeadingElseByItsNumber() {
        assertEquals("The Bricked Arch", AudiobookFiles.titleOf("# The Bricked Arch\n\nText.", 3, "Part %s"));
        assertEquals("I. The Arch", AudiobookFiles.titleOf("## I. The Arch\nText", 1, "Part %s"));
        assertEquals("Part 3", AudiobookFiles.titleOf("No heading here.\n\n# Later heading", 3, "Part %s"));
        assertEquals("Part 2", AudiobookFiles.titleOf("#\n\nText", 2, "Part %s"));
        assertEquals("Part 1", AudiobookFiles.titleOf(null, 1, "Part %s"));
        assertEquals(120, AudiobookFiles.titleOf("# " + "x".repeat(300), 1, "Part %s").length());
    }

    @Test
    void aPartWithoutUpToDateAudioIsLeftOutAndSaidSo() throws Exception {
        java.nio.file.Path tone = AudiobookFilesFixture.file(tone());
        List<ChatMessage> parts = List.of(part("# One\n\ntext"), part("# Two\n\ntext"), part("three"));
        List<Integer> ticks = new ArrayList<>();

        var gathered = AudiobookExporter.gather(parts, p -> p.getResponse().startsWith("# One") ? AudiobookExporter.PartAudio.of(tone)
                : p.getResponse().startsWith("# Two") ? AudiobookExporter.PartAudio.missing("no audio") : AudiobookExporter.PartAudio.missing("audio older than the text"),
                () -> ticks.add(1));

        assertEquals(List.of("One"), gathered.chapters().stream().map(AudiobookFiles.Chapter::title).toList());
        assertEquals(List.of("2 (no audio)", "3 (audio older than the text)"), gathered.left());
        assertEquals(3, ticks.size(), "the progress moves for every part");
    }

    @Test
    void theTwoFormatsHaveTheirOwnIdsAndFileTypes() {
        var zip = new AudiobookExporter(AudiobookExporter.Format.ZIP, p -> null);
        var mp3 = new AudiobookExporter(AudiobookExporter.Format.MP3, p -> null);

        assertEquals("audiobook-zip", zip.getId());
        assertEquals("zip", zip.getFileExtension());
        assertEquals("application/zip", zip.getMimeType());
        assertEquals("audiobook-mp3", mp3.getId());
        assertEquals("mp3", mp3.getFileExtension());
        assertEquals("audio/mpeg", mp3.getMimeType());
        assertFalse(zip.supportsStyles());
    }
}
