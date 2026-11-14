package com.github.enerccio.marginalia.extensions.audiobook.export;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.ExporterService;
import com.github.enerccio.marginalia.extensions.audiobook.AudiobookLog;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.ui.dialogs.ProgressBarDialog;
import com.github.enerccio.marginalia.ui.widgets.Notification;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.github.enerccio.marginalia.extensions.audiobook.AudiobookLocalization.*;

/**
 * The audiobook as an export format of the book (the export dialog gives the title, the author, the file name and the range of parts): the audio of the
 * parts that were converted, one file per chapter in a zip, one mp3 with chapter marks, or (when ffmpeg is installed) one m4b. A part without audio, or with audio older than its text or its
 * script, is left out and the author is told; nothing is converted by the export. It runs in the request context of the user (the audio is among
 * their resources) and keeps no state between exports.
 */
public final class AudiobookExporter implements ExporterService.Exporter {

    public enum Format {
        ZIP("audiobook-zip", "zip", "application/zip"), MP3("audiobook-mp3", "mp3", "audio/mpeg"), M4B("audiobook-m4b", "m4b", "audio/mp4");

        private final String id;
        private final String extension;
        private final String mime;

        Format(String id, String extension, String mime) {
            this.id = id;
            this.extension = extension;
            this.mime = mime;
        }
    }

    /** The audio of a part for the export: the file it is stored in (only read), or why there is none. */
    public record PartAudio(Path audio, String problem) {

        public static PartAudio of(Path audio) {
            return new PartAudio(audio, null);
        }

        public static PartAudio missing(String problem) {
            return new PartAudio(null, problem);
        }
    }

    private final Format format;
    private final Function<ChatMessage, PartAudio> source;

    /** @param source the up to date audio of a part (read for the current user), or the reason there is none */
    public AudiobookExporter(Format format, Function<ChatMessage, PartAudio> source) {
        this.format = format;
        this.source = source;
    }

    @Override
    public String getName() {
        return switch (format) {
            case ZIP -> T.localized(AUDIOBOOK_ZIP);
            case MP3 -> T.localized(AUDIOBOOK_ONE_MP3);
            case M4B -> T.localized(AUDIOBOOK_M4B);
        };
    }

    @Override
    public String getId() {
        return format.id;
    }

    @Override
    public String getFileExtension() {
        return format.extension;
    }

    @Override
    public String getMimeType() {
        return format.mime;
    }

    /** The chapters of the parts that have audio, and the parts that were left out with the reason. */
    record Gathered(List<AudiobookFiles.Chapter> chapters, List<String> left) {
    }

    /**
     * The whole audiobook, one mp3 with the chapters marked.
     *
     * @param audio a temporary file the caller serves and deletes; null when no part has audio
     * @param left  which parts were left out and why
     */
    public record Whole(Path audio, List<String> left) {
    }

    /** Joins the audio of the parts into one temporary file (the parts without up to date audio are left out and named). */
    public static Whole wholeBook(List<ChatMessage> parts, Function<ChatMessage, PartAudio> source, String book, String author) throws BackendException {
        Gathered gathered = gather(parts, source, () -> {
        });
        if (gathered.chapters().isEmpty()) {
            return new Whole(null, gathered.left());
        }
        Path file;
        try {
            file = Files.createTempFile("audiobook-", ".mp3");
        } catch (IOException e) {
            throw new BackendException("The audiobook could not be made: " + e.getMessage(), e);
        }
        try {
            AudiobookFiles.oneFile(gathered.chapters(), book, author, file);
        } catch (BackendException | RuntimeException e) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
                // a temporary file that stays is not worth hiding the error
            }
            throw e;
        }
        return new Whole(file, gathered.left());
    }

    static Gathered gather(List<ChatMessage> parts, Function<ChatMessage, PartAudio> source, Runnable tick) {
        List<AudiobookFiles.Chapter> chapters = new ArrayList<>();
        List<String> left = new ArrayList<>();
        int number = 0;
        for (ChatMessage part : parts) {
            number++;
            PartAudio found = source.apply(part);
            if (found.audio() == null) {
                left.add(number + " (" + found.problem() + ")");
            } else {
                chapters.add(new AudiobookFiles.Chapter(AudiobookFiles.titleOf(part.getResponse(), number, T.localized(PART_NUMBER)), found.audio()));
            }
            tick.run();
        }
        return new Gathered(chapters, left);
    }

    /**
     * Has the export dialog end the progress window without its result window (what the dialog does for a failed export: the field is protected, a plugin
     * reaches it by reflection). False when that is not possible, the export then fails with the message.
     */
    private static boolean stopWithoutResult(ProgressBarDialog progress) {
        try {
            java.lang.reflect.Field field = ProgressBarDialog.class.getDeclaredField("manualCancel");
            field.setAccessible(true);
            field.setBoolean(progress, true);
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            AudiobookLog.get().warn("The export could not be stopped quietly: {}", e.getMessage());
            return false;
        }
    }

    /** Writes the audiobook straight into the file of the export dialog, from the files of the parts. */
    @Override
    public void exportTo(ExporterService.ExportOptions options, List<ChatMessage> parts, ProgressBarDialog progress, Path target) throws Exception {
        Gathered gathered = gather(parts, source, progress::updateProgress);
        if (gathered.chapters().isEmpty()) {
            String message = gathered.left().isEmpty() ? T.localized(NO_PART_TO_EXPORT)
                    : T.localized(NO_PART_HAS_UP_TO_DATE_AUDIO).formatted(String.join(", ", gathered.left()));
            if (stopWithoutResult(progress)) {
                // nothing to download: the progress window closes and the author is told why, instead of an error page
                progress.vaadinLocked(() -> {
                    progress.close();
                    Notification.warning(message, Notification.DURATION_LONGER);
                });
                return;
            }
            throw new IllegalStateException(message);
        }
        if (!gathered.left().isEmpty()) {
            String message = T.localized(PARTS_LEFT_OUT_OF_THE_AUDIOBOOK).formatted(String.join(", ", gathered.left()));
            AudiobookLog.get().info("Audiobook export: {}", message);
            progress.vaadinLocked(() -> Notification.warning(message));
        }
        switch (format) {
            case ZIP -> AudiobookFiles.zip(gathered.chapters(), options.bookName(), options.author(), target);
            case MP3 -> AudiobookFiles.oneFile(gathered.chapters(), options.bookName(), options.author(), target);
            case M4B -> AudiobookFiles.m4b(gathered.chapters(), options.bookName(), options.author(), target);
        }
    }
}
