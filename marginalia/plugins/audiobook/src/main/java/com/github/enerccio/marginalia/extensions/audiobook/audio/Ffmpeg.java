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

    /** The loudness target of a part is a level a listener does not notice being missed by this much: such audio is left as it is (no second encoding). */
    static final double TOLERANCE_LU = 0.5;

    /** The mp3 of a part that was levelled: speech does not need more, and the pieces were 32 kbps. */
    static final String LEVELLED_MP3_BITRATE = "64k";

    /**
     * What ffmpeg measured of a piece of audio (EBU R128, the first pass of {@code loudnorm}).
     *
     * @param integrated the loudness of the whole in LUFS
     * @param truePeak   the highest peak in dBTP
     * @param range      the loudness range in LU
     * @param threshold  the gate of the measurement in LUFS
     * @param offset     the gain {@code loudnorm} suggests for the target it was asked
     * @param sampleRate the sample rate of the audio in Hz
     */
    public record Loudness(double integrated, double truePeak, double range, double threshold, double offset, int sampleRate) {
    }

    private static final java.util.regex.Pattern SAMPLE_RATE = java.util.regex.Pattern.compile("Audio: [^,]+, (\\d+) Hz");

    /**
     * Measures the loudness of the audio.
     *
     * @param targetLufs the target the suggested offset is worked out for
     * @return the measurement, null when there is nothing to measure (silence, or audio too short to have a loudness)
     * @throws BackendException when ffmpeg is not there or fails
     */
    public static Loudness measure(byte[] audio, double targetLufs) throws BackendException {
        String log = execute(audio, AudioKind.of(audio).extension(), null, null,
                List.of("-map", "0:a", "-af", "loudnorm=I=" + targetLufs + ":TP=-1.5:LRA=11:print_format=json", "-f", "null")).log();
        Double integrated = number(log, "input_i");
        Double peak = number(log, "input_tp");
        Double range = number(log, "input_lra");
        Double threshold = number(log, "input_thresh");
        Double offset = number(log, "target_offset");
        java.util.regex.Matcher rate = SAMPLE_RATE.matcher(log);
        if (integrated == null || peak == null || range == null || threshold == null || offset == null || !rate.find()) {
            return null;
        }
        return new Loudness(integrated, peak, range, threshold, offset, Integer.parseInt(rate.group(1)));
    }

    /** A number of the JSON that {@code loudnorm} prints, null when it is missing or is not a finite number ({@code -inf} for silence). */
    private static Double number(String log, String name) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"" + name + "\"\\s*:\\s*\"([^\"]+)\"").matcher(log);
        if (!m.find()) {
            return null;
        }
        try {
            double value = Double.parseDouble(m.group(1));
            return Double.isFinite(value) ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Levels the audio to the target loudness (EBU R128 {@code loudnorm} in two passes, linear: the dynamics are kept, only the level changes; true peak
     * -1.5 dB), in the kind it is (an mp3 is encoded again, a wav stays PCM) and at its sample rate. Audio that is within {@link #TOLERANCE_LU} of the target
     * and silence are returned as they are.
     *
     * @param targetLufs the loudness to reach, -30 to -10
     * @throws BackendException when ffmpeg is not there or fails
     */
    public static byte[] normalizeLoudness(byte[] audio, double targetLufs) throws BackendException {
        Loudness measured = measure(audio, targetLufs);
        if (measured == null || Math.abs(measured.integrated() - targetLufs) < TOLERANCE_LU) {
            return audio;
        }
        String filter = "loudnorm=I=" + targetLufs + ":TP=-1.5:LRA=11:measured_I=" + measured.integrated() + ":measured_TP=" + measured.truePeak()
                + ":measured_LRA=" + measured.range() + ":measured_thresh=" + measured.threshold() + ":offset=" + measured.offset() + ":linear=true";
        AudioKind kind = AudioKind.of(audio);
        List<String> encoder = kind == AudioKind.WAV ? List.of("-c:a", "pcm_s16le") : List.of("-c:a", "libmp3lame", "-b:a", LEVELLED_MP3_BITRATE);
        List<String> options = new ArrayList<>(List.of("-map", "0:a", "-map_metadata", "-1", "-af", filter, "-ar", String.valueOf(measured.sampleRate())));
        options.addAll(encoder);
        return run(audio, kind.extension(), kind.extension(), null, options);
    }

    /** What an ffmpeg run gave: the output file (null when the output was discarded) and its log, which has the measurements. */
    private record Executed(byte[] output, String log) {
    }

    private static byte[] run(byte[] input, String inputExtension, String outputExtension, String metadata, List<String> options) throws BackendException {
        return execute(input, inputExtension, outputExtension, metadata, options).output();
    }

    /**
     * Runs ffmpeg on one input (and a metadata file when there is text for it) and reads the output file; with no output extension the output is
     * discarded ({@code -f null}, the options must say the format) and only the log is read.
     */
    private static Executed execute(byte[] input, String inputExtension, String outputExtension, String metadata, List<String> options) throws BackendException {
        Path program = locate();
        if (program == null) {
            throw new BackendException("ffmpeg is not installed");
        }
        Path folder = null;
        try {
            folder = Files.createTempDirectory("audiobook-ffmpeg");
            Path in = folder.resolve("in." + inputExtension);
            Path out = outputExtension == null ? null : folder.resolve("out." + outputExtension);
            Path log = folder.resolve("ffmpeg.log");
            Files.write(in, input);
            List<String> command = new ArrayList<>(List.of(program.toString(), "-nostdin", "-hide_banner", "-y", "-i", in.toString()));
            if (metadata != null) {
                Path meta = folder.resolve("chapters.txt");
                Files.writeString(meta, metadata, StandardCharsets.UTF_8);
                command.addAll(List.of("-i", meta.toString()));
            }
            command.addAll(options);
            command.add(out == null ? "-" : out.toString());
            Process process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
            if (!process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
                process.destroyForcibly();
                throw new BackendException("ffmpeg did not finish in " + TIMEOUT_MINUTES + " minutes");
            }
            if (process.exitValue() != 0 || (out != null && !Files.isRegularFile(out))) {
                throw new BackendException("ffmpeg failed: " + tail(log));
            }
            return new Executed(out == null ? null : Files.readAllBytes(out), Files.readString(log, StandardCharsets.UTF_8));
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
