package com.github.enerccio.marginalia.extensions.audiobook.audio;

import com.github.enerccio.marginalia.extensions.audiobook.export.AudiobookFiles;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        byte[] m4b = AudiobookFiles.m4b(List.of(new AudiobookFiles.Chapter("The Arch", tone), new AudiobookFiles.Chapter("Who", tone)), "The Book", "An Author");

        String head = new String(m4b, 0, 32, StandardCharsets.ISO_8859_1);
        assertEquals("ftyp", head.substring(4, 8));
        assertEquals("M4B ", head.substring(8, 12));
        String all = new String(m4b, StandardCharsets.ISO_8859_1);
        assertTrue(all.contains("chpl"), "the chapter list is there");
        assertTrue(all.contains("The Arch") && all.contains("Who"), "with the titles");
        assertTrue(all.contains("An Author"));
    }
}
