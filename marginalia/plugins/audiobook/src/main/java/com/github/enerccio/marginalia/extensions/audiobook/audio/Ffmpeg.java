package com.github.enerccio.marginalia.extensions.audiobook.audio;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The optional program <b>ffmpeg</b>, used for what Java can't do here: the audiobook as an {@code .m4b} (AAC audio in an MP4 container with chapters,
 * which Apple Books and most phone audiobook apps read, unlike the chapter frames of an mp3). Without the program the format is not offered; nothing
 * else of the plugin needs it. It is found by the environment variable {@code MARGINALIA_FFMPEG} (a path), else on the {@code PATH}, else in the
 * usual folders (a desktop application started from the Finder or a menu does not get the {@code PATH} of the shell).
 */
public final class Ffmpeg {

    /** AAC at this rate is transparent for speech and a tenth of the size of the wav. */
    static final String BITRATE = "64k";

    /** The mp3 made of a wav for the export: speech does not need more. */
    static final String MP3_BITRATE = "96k";

    /** The longest the conversion of a whole book may take. */
    static final long TIMEOUT_MINUTES = 30;

    private static final List<String> USUAL_FOLDERS = List.of("/opt/homebrew/bin", "/usr/local/bin", "/usr/bin", "/snap/bin", "C:\\ffmpeg\\bin");

    private static volatile Path found;

    private Ffmpeg() {
    }

    /** Whether the program is there; asked again until it is (it may be installed while the application runs). */
    public static boolean available() {
        return locate() != null;
    }

    private static Path locate() {
        Path known = found;
        if (known != null && Files.isExecutable(known)) {
            return known;
        }
        List<Path> candidates = new ArrayList<>();
        String custom = System.getenv("MARGINALIA_FFMPEG");
        if (custom != null && !custom.isBlank()) {
            candidates.add(Path.of(custom));
        }
        List<String> folders = new ArrayList<>();
        String path = System.getenv("PATH");
        if (path != null) {
            folders.addAll(List.of(path.split(java.io.File.pathSeparator)));
        }
        folders.addAll(USUAL_FOLDERS);
        for (String folder : folders) {
            if (!folder.isBlank()) {
                for (String name : List.of("ffmpeg", "ffmpeg.exe")) {
                    try {
                        candidates.add(Path.of(folder).resolve(name));
                    } catch (RuntimeException e) {
                        // not a path on this system
                    }
                }
            }
        }
        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate) && Files.isExecutable(candidate)) {
                found = candidate;
                return candidate;
            }
        }
        return null;
    }

    /**
     * The file for ffmpeg that names the chapters and the tags ({@code ;FFMETADATA1}): the times are in milliseconds.
     *
     * @param title the title of the book, blank for none
     */
    static String metadata(String title, String author, String album, List<Id3Tag.Chapter> chapters) {
        StringBuilder text = new StringBuilder(";FFMETADATA1\n");
        tag(text, "title", title);
        tag(text, "artist", author);
        tag(text, "album", album);
        tag(text, "genre", "Audiobook");
        for (Id3Tag.Chapter chapter : chapters) {
            text.append("[CHAPTER]\nTIMEBASE=1/1000\nSTART=").append(chapter.startMillis()).append("\nEND=").append(chapter.endMillis()).append('\n');
            tag(text, "title", chapter.title());
        }
        return text.toString();
    }

    private static void tag(StringBuilder text, String key, String value) {
        if (value != null && !value.isBlank()) {
            text.append(key).append('=').append(escape(value)).append('\n');
        }
    }

    /** The characters that mean something in the file are written after a backslash. */
    static String escape(String value) {
        StringBuilder out = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\\', '=', ';', '#' -> out.append('\\').append(c);
                case '\n', '\r' -> out.append("\\\n");
                default -> out.append(c);
            }
        }
        return out.toString();
    }

    /**
     * Makes an m4b of the audio (one file, mp3 or wav, without a tag). An mp3 is encoded a second time (AAC); a wav is encoded once.
     *
     * @param chapters where the chapters are in the audio
     * @return the bytes of the m4b
     * @throws BackendException when ffmpeg is not there or fails
     */
    public static byte[] m4b(byte[] audio, String title, String author, String album, List<Id3Tag.Chapter> chapters) throws BackendException {
        return run(audio, AudioKind.of(audio).extension(), "m4b", metadata(title, author, album, chapters),
                List.of("-map", "0:a", "-map_metadata", "1", "-map_chapters", "1", "-c:a", "aac", "-b:a", BITRATE, "-movflags", "+faststart",
                        "-brand", "M4B ", "-f", "ipod"));
    }

    /** An mp3 of a wav (for the files of the export that are mp3, when the audio is stored as wav). */
    public static byte[] toMp3(byte[] wav) throws BackendException {
        return run(wav, "wav", "mp3", null, List.of("-map", "0:a", "-map_metadata", "-1", "-c:a", "libmp3lame", "-b:a", MP3_BITRATE));
    }

    /** A wav of the audio in the given format (for the audio that was stored as mp3 and has to be joined with wav). */
    public static byte[] toWav(byte[] audio, WavJoiner.Format format) throws BackendException {
        return run(audio, AudioKind.of(audio).extension(), "wav", null,
                List.of("-map", "0:a", "-map_metadata", "-1", "-c:a", "pcm_s16le", "-ar", String.valueOf(format.sampleRate()), "-ac", String.valueOf(format.channels())));
    }

    /** Runs ffmpeg on one input (and a metadata file when there is text for it) and reads the output file. */
    private static byte[] run(byte[] input, String inputExtension, String outputExtension, String metadata, List<String> options) throws BackendException {
        Path program = locate();
        if (program == null) {
            throw new BackendException("ffmpeg is not installed");
        }
        Path folder = null;
        try {
            folder = Files.createTempDirectory("audiobook-ffmpeg");
            Path in = folder.resolve("in." + inputExtension);
            Path out = folder.resolve("out." + outputExtension);
            Path log = folder.resolve("ffmpeg.log");
            Files.write(in, input);
            List<String> command = new ArrayList<>(List.of(program.toString(), "-nostdin", "-hide_banner", "-y", "-i", in.toString()));
            if (metadata != null) {
                Path meta = folder.resolve("chapters.txt");
                Files.writeString(meta, metadata, StandardCharsets.UTF_8);
                command.addAll(List.of("-i", meta.toString()));
            }
            command.addAll(options);
            command.add(out.toString());
            Process process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
            if (!process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
                process.destroyForcibly();
                throw new BackendException("ffmpeg did not finish in " + TIMEOUT_MINUTES + " minutes");
            }
            if (process.exitValue() != 0 || !Files.isRegularFile(out)) {
                throw new BackendException("ffmpeg failed: " + tail(log));
            }
            return Files.readAllBytes(out);
        } catch (IOException e) {
            throw new BackendException("ffmpeg could not be run: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BackendException("Interrupted", e);
        } finally {
            if (folder != null) {
                deleteAll(folder);
            }
        }
    }

    /** The last lines of the log: the reason is at the end. */
    private static String tail(Path log) {
        try {
            List<String> lines = Files.readAllLines(log, StandardCharsets.UTF_8);
            return String.join(" | ", lines.subList(Math.max(0, lines.size() - 4), lines.size()));
        } catch (IOException | RuntimeException e) {
            return "no log";
        }
    }

    private static void deleteAll(Path folder) {
        try (var files = Files.list(folder)) {
            files.forEach(f -> f.toFile().delete());
        } catch (IOException | RuntimeException e) {
            // a temporary folder that stays is not worth an error
        }
        folder.toFile().delete();
    }
}
