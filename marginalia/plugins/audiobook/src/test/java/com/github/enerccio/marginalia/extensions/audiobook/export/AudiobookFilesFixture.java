package com.github.enerccio.marginalia.extensions.audiobook.export;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * The files of an audiobook in memory, for the tests: the chapters are written into temporary files (the exports read files) and the export is
 * read back from its file.
 */
public final class AudiobookFilesFixture {

    private AudiobookFilesFixture() {
    }

    /** A temporary file with the audio, deleted when the tests end. */
    public static Path file(byte[] audio) throws IOException {
        Path file = Files.createTempFile("audiobook-test-", ".audio");
        file.toFile().deleteOnExit();
        Files.write(file, audio);
        return file;
    }

    public static AudiobookFiles.Chapter chapter(String title, byte[] audio) throws IOException {
        return new AudiobookFiles.Chapter(title, file(audio));
    }

    private interface Export {
        void into(Path target) throws Exception;
    }

    private static byte[] read(Export export) throws Exception {
        Path target = Files.createTempFile("audiobook-test-", ".out");
        try {
            export.into(target);
            return Files.readAllBytes(target);
        } finally {
            Files.deleteIfExists(target);
        }
    }

    public static byte[] zip(List<AudiobookFiles.Chapter> chapters, String book, String author) throws Exception {
        return read(target -> AudiobookFiles.zip(chapters, book, author, target));
    }

    public static byte[] oneFile(List<AudiobookFiles.Chapter> chapters, String book, String author) throws Exception {
        return read(target -> AudiobookFiles.oneFile(chapters, book, author, target));
    }

    public static byte[] m4b(List<AudiobookFiles.Chapter> chapters, String book, String author) throws Exception {
        return read(target -> AudiobookFiles.m4b(chapters, book, author, target));
    }
}
