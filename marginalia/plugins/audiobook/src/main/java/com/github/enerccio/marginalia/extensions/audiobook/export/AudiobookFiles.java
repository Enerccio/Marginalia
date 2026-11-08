package com.github.enerccio.marginalia.extensions.audiobook.export;

import com.github.enerccio.marginalia.extensions.audiobook.audio.Id3Tag;
import com.github.enerccio.marginalia.extensions.audiobook.audio.Mp3Joiner;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/** The files of an audiobook, made from the audio of its chapters (mp3, as the plugin stores it): a zip with a file per chapter, or one file with chapter marks. */
public final class AudiobookFiles {

    /** The silence between two chapters in the one file. */
    public static final double PAUSE_BETWEEN_CHAPTERS = 1.5;

    private AudiobookFiles() {
    }

    /** One chapter: what it is called and its audio. */
    public record Chapter(String title, byte[] audio) {
    }

    /**
     * A zip with one mp3 per chapter ({@code 01 - Title.mp3}, tagged with the title, the track, the book and the author, as an audiobook) and a playlist.
     */
    public static byte[] zip(List<Chapter> chapters, String book, String author) throws BackendException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int width = Math.max(2, String.valueOf(chapters.size()).length());
        StringBuilder playlist = new StringBuilder("#EXTM3U\n");
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            // mp3 does not compress
            zip.setLevel(Deflater.NO_COMPRESSION);
            for (int i = 0; i < chapters.size(); i++) {
                Chapter chapter = chapters.get(i);
                String name = String.format("%0" + width + "d - %s.mp3", i + 1, fileSafe(chapter.title()));
                byte[] tag = Id3Tag.tag(chapter.title(), author, book, (i + 1) + "/" + chapters.size(), "Audiobook", List.of());
                zip.putNextEntry(new ZipEntry(name));
                zip.write(Id3Tag.withTag(tag, chapter.audio()));
                zip.closeEntry();
                playlist.append("#EXTINF:").append(Math.round(Mp3Joiner.seconds(chapter.audio()))).append(',').append(chapter.title().replace('\n', ' ')).append('\n')
                        .append(name).append('\n');
            }
            zip.putNextEntry(new ZipEntry("playlist.m3u8"));
            zip.write(playlist.toString().getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        } catch (IOException e) {
            throw new BackendException("The zip could not be made: " + e.getMessage(), e);
        }
        return out.toByteArray();
    }

    /** One mp3 of all the chapters, a pause between them, with the chapters marked in its tag (CHAP and CTOC frames). */
    public static byte[] oneFile(List<Chapter> chapters, String book, String author) throws BackendException {
        List<Double> pauses = new ArrayList<>();
        chapters.forEach(c -> pauses.add(PAUSE_BETWEEN_CHAPTERS));
        Mp3Joiner.Joined joined = Mp3Joiner.joinMarked(chapters.stream().map(Chapter::audio).toList(), pauses);
        long total = Math.round(Mp3Joiner.seconds(joined.audio()) * 1000);
        List<Id3Tag.Chapter> marks = new ArrayList<>();
        for (int i = 0; i < chapters.size(); i++) {
            long start = Math.round(joined.starts()[i] * 1000);
            long end = i + 1 < chapters.size() ? Math.round(joined.starts()[i + 1] * 1000) : total;
            marks.add(new Id3Tag.Chapter(chapters.get(i).title(), start, end));
        }
        return Id3Tag.withTag(Id3Tag.tag(book, author, book, "", "Audiobook", marks), joined.audio());
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
