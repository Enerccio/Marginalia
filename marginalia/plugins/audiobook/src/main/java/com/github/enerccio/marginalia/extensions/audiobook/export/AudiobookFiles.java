package com.github.enerccio.marginalia.extensions.audiobook.export;

import com.github.enerccio.marginalia.extensions.audiobook.audio.*;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The files of an audiobook, made from the audio of its chapters (the files of the parts, mp3 or wav as the plugin stores them): a zip with a
 * file per chapter, or one file with chapter marks. Everything goes from file to file: the audio of a whole book is never in memory (an mp3
 * chapter is read when it is joined, a wav one is copied), the joined audio is a temporary file, and ffmpeg reads and writes files.
 */
public final class AudiobookFiles {

    /** The silence between two chapters in the one file. */
    public static final double PAUSE_BETWEEN_CHAPTERS = 1.5;

    private AudiobookFiles() {
    }

    /** One chapter: what it is called and the file of its audio (it is only read). */
    public record Chapter(String title, Path audio) {
    }

    /**
     * A zip with one mp3 per chapter ({@code 01 - Title.mp3}, tagged with the title, the track, the book and the author, as an audiobook) and a playlist,
     * written into the file.
     */
    public static void zip(List<Chapter> chapters, String book, String author, Path target) throws BackendException {
        int width = Math.max(2, String.valueOf(chapters.size()).length());
        StringBuilder playlist = new StringBuilder("#EXTM3U\n");
        try (ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(target)))) {
            // mp3 does not compress
            zip.setLevel(Deflater.NO_COMPRESSION);
            for (int i = 0; i < chapters.size(); i++) {
                Chapter chapter = chapters.get(i);
                String name = String.format("%0" + width + "d - %s.mp3", i + 1, fileSafe(chapter.title()));
                byte[] tag = Id3Tag.tag(chapter.title(), author, book, (i + 1) + "/" + chapters.size(), "Audiobook", List.of());
                // audio that is stored as wav is encoded to mp3 for the file of the chapter
                Path converted = null;
                try {
                    Path mp3 = chapter.audio();
                    if (AudioKind.of(chapter.audio()) == AudioKind.WAV) {
                        converted = Files.createTempFile("audiobook-chapter-", ".mp3");
                        Ffmpeg.toMp3(Ffmpeg.Input.of(chapter.audio()), converted);
                        mp3 = converted;
                    }
                    zip.putNextEntry(new ZipEntry(name));
                    zip.write(tag);
                    Files.copy(mp3, zip);
                    zip.closeEntry();
                } finally {
                    deleteQuietly(converted);
                }
                playlist.append("#EXTINF:").append(Math.round(AudioKind.seconds(chapter.audio()))).append(',').append(chapter.title().replace('\n', ' ')).append('\n')
                        .append(name).append('\n');
            }
            zip.putNextEntry(new ZipEntry("playlist.m3u8"));
            zip.write(playlist.toString().getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        } catch (IOException e) {
            throw new BackendException("The zip could not be made: " + e.getMessage(), e);
        }
    }

    /**
     * The chapters joined into one temporary file, without a tag, and where each starts.
     *
     * @param audio what ffmpeg reads: the mp3, or the bare samples when the audio is wav
     * @param mp3   whether the file is mp3 (it can be used as it is), else it is bare samples
     */
    private record Joined(Ffmpeg.Input audio, boolean mp3, List<Id3Tag.Chapter> marks) {
    }

    /**
     * Joins the chapters in the folder: all mp3 are joined as they are; when any is wav the book is joined as wav samples (the mp3 of a mixed book
     * are decoded to the wav format of the others, by ffmpeg, into the folder).
     */
    private static Joined join(List<Chapter> chapters, Path folder) throws BackendException, IOException {
        WavJoiner.Format format = null;
        for (Chapter chapter : chapters) {
            if (AudioKind.of(chapter.audio()) == AudioKind.WAV) {
                format = WavJoiner.formatOf(chapter.audio());
                break;
            }
        }
        List<Double> pauses = new ArrayList<>();
        chapters.forEach(c -> pauses.add(PAUSE_BETWEEN_CHAPTERS));
        double[] starts;
        double total;
        Ffmpeg.Input audio;
        if (format == null) {
            Path joined = folder.resolve("joined.mp3");
            Mp3Joiner.Marks marks;
            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(joined))) {
                marks = Mp3Joiner.joinTo(chapters.stream().map(Chapter::audio).toList(), pauses, out);
            }
            starts = marks.starts();
            total = marks.seconds();
            audio = Ffmpeg.Input.of(joined);
        } else {
            List<Path> same = new ArrayList<>();
            for (int i = 0; i < chapters.size(); i++) {
                Path file = chapters.get(i).audio();
                if (AudioKind.of(file) == AudioKind.WAV) {
                    same.add(file);
                } else {
                    Path decoded = folder.resolve("chapter-" + (i + 1) + ".wav");
                    Ffmpeg.toWav(file, format, decoded);
                    same.add(decoded);
                }
            }
            Path joined = folder.resolve("joined.pcm");
            WavJoiner.Marks marks;
            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(joined))) {
                marks = WavJoiner.joinRaw(same, pauses, out);
            }
            starts = marks.starts();
            total = marks.seconds();
            audio = Ffmpeg.Input.raw(joined, marks.format());
        }
        long totalMillis = Math.round(total * 1000);
        List<Id3Tag.Chapter> marks = new ArrayList<>();
        for (int i = 0; i < chapters.size(); i++) {
            long start = Math.round(starts[i] * 1000);
            long end = i + 1 < chapters.size() ? Math.round(starts[i + 1] * 1000) : totalMillis;
            marks.add(new Id3Tag.Chapter(chapters.get(i).title(), start, end));
        }
        return new Joined(audio, format == null, marks);
    }

    /**
     * One mp3 of all the chapters, a pause between them, with the chapters marked in its tag (CHAP and CTOC frames), written into the file. Audio
     * stored as wav needs ffmpeg (it is encoded to mp3).
     */
    public static void oneFile(List<Chapter> chapters, String book, String author, Path target) throws BackendException {
        Path folder = null;
        try {
            folder = Files.createTempDirectory("audiobook-export");
            Joined joined = join(chapters, folder);
            Path mp3 = joined.audio().file();
            if (!joined.mp3()) {
                mp3 = folder.resolve("joined.mp3");
                Ffmpeg.toMp3(joined.audio(), mp3);
            }
            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(target))) {
                out.write(Id3Tag.tag(book, author, book, "", "Audiobook", joined.marks()));
                Files.copy(mp3, out);
            }
        } catch (IOException e) {
            throw new BackendException("The audiobook could not be made: " + e.getMessage(), e);
        } finally {
            deleteAll(folder);
        }
    }

    /**
     * One m4b (AAC in an MP4 container) of all the chapters, with the chapters marked, written into the file by ffmpeg; needs ffmpeg, see
     * {@link Ffmpeg#available()}. Audio stored as mp3 is encoded a second time, audio stored as wav only once.
     */
    public static void m4b(List<Chapter> chapters, String book, String author, Path target) throws BackendException {
        Path folder = null;
        try {
            folder = Files.createTempDirectory("audiobook-export");
            Joined joined = join(chapters, folder);
            Ffmpeg.m4b(joined.audio(), book, author, book, joined.marks(), target);
        } catch (IOException e) {
            throw new BackendException("The audiobook could not be made: " + e.getMessage(), e);
        } finally {
            deleteAll(folder);
        }
    }

    private static void deleteQuietly(Path file) {
        if (file != null) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException | RuntimeException e) {
                // a temporary file that stays is not worth an error
            }
        }
    }

    /** Deletes the folder and what is in it (the temporary files of an export: one level). */
    private static void deleteAll(Path folder) {
        if (folder == null) {
            return;
        }
        try (var files = Files.list(folder)) {
            files.forEach(AudiobookFiles::deleteQuietly);
        } catch (IOException | RuntimeException e) {
            // a temporary folder that stays is not worth an error
        }
        deleteQuietly(folder);
    }

    /** The title of a part: its heading when its text starts with one, else "Part n". */
    public static String titleOf(String text, int number, String fallback) {
        if (text != null) {
            for (String line : text.strip().split("\\R", 2)) {
                String stripped = line.strip();
                if (stripped.startsWith("#")) {
                    String title = stripped.replaceFirst("^#+\\s*", "").strip();
                    if (!title.isEmpty()) {
                        return title.length() > 120 ? title.substring(0, 120) : title;
                    }
                }
                break;
            }
        }
        return String.format(fallback, number);
    }

    public static String fileSafe(String title) {
        String safe = title.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").strip();
        if (safe.length() > 80) {
            safe = safe.substring(0, 80).strip();
        }
        return safe.isEmpty() ? "chapter" : safe;
    }
}
