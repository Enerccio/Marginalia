package com.github.enerccio.marginalia.workset;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Checks that the book the harness exports ({@code export/book.json}) can be imported: the app's own import ({@code ManuscriptService.cloneFromBackup}) into an
 * empty database, then a new backup of the clone is compared with the file (parts, summaries, annotations, versions, the data of the plugins).
 * A check, not a test of the suite: runs only with {@code -Dworkset.roundtrip=true} (the file is {@code -Dworkset.export}, default {@code ../work/harness/export/book.json}).
 */
@EnabledIfSystemProperty(named = "workset.roundtrip", matches = "true")
class ExportRoundTripCheck extends MarginaliaTestBase {

    @Autowired
    private ManuscriptService manuscriptService;
    @Autowired
    private LorebookService lorebookService;
    @Autowired
    private com.github.enerccio.marginalia.domain.service.BackupService backupService;
    @Autowired
    private com.github.enerccio.marginalia.domain.service.ChatMessageService chatMessageService;

    private record Counts(int messages, int summaries, int annotations, int revisions, int words) {
    }

    private static Counts count(JsonObject backup) {
        int messages = 0, summaries = 0, annotations = 0, revisions = 0, words = 0;
        for (JsonElement element : backup.getAsJsonArray("messages")) {
            JsonObject m = element.getAsJsonObject();
            messages++;
            summaries += m.has("summary") && !m.get("summary").isJsonNull() ? 1 : 0;
            annotations += m.has("annotations") ? m.getAsJsonArray("annotations").size() : 0;
            revisions += m.has("revisions") ? m.getAsJsonArray("revisions").size() : 0;
            words += m.has("wordCount") ? m.get("wordCount").getAsInt() : 0;
        }
        return new Counts(messages, summaries, annotations, revisions, words);
    }

    private static String ms(long since) {
        return (System.nanoTime() - since) / 1_000_000 + " ms";
    }

    @Test
    void exportImportsAndComesBackTheSame() throws Exception {
        Path file = Path.of(System.getProperty("workset.export", "../work/harness/export/book.json")).toAbsolutePath().normalize();
        long t0 = System.nanoTime();
        JsonObject backup = new Gson().fromJson(Files.readString(file, StandardCharsets.UTF_8), JsonObject.class);
        System.out.println("[roundtrip] time: parse of the file " + ms(t0));
        User user = createUser("roundtrip", DEFAULT_PASSWORD, false);
        loginAs(user);

        // the lorebook file first (a book backup only links its lorebook), then the book
        Path lorebookFile = file.resolveSibling("lorebook.json");
        if (Files.exists(lorebookFile)) {
            var lorebook = lorebookService.importLorebook(Files.readString(lorebookFile, StandardCharsets.UTF_8), "imported");
            System.out.println("[roundtrip] lorebook file imported: " + lorebook.getName() + ", uuid " + lorebook.getUuid()
                    + " (the book links " + backup.getAsJsonObject("lorebook").get("uuid").getAsString() + ")");
        }
        t0 = System.nanoTime();
        Manuscript clone = manuscriptService.cloneFromBackup(backup);
        System.out.println("[roundtrip] time: cloneFromBackup " + ms(t0));
        assertNotNull(clone, "the import returned no book");
        t0 = System.nanoTime();
        JsonObject again = manuscriptService.createBackup(clone, true);
        System.out.println("[roundtrip] time: createBackup with history " + ms(t0));

        Counts before = count(backup);
        Counts after = count(again);
        System.out.println("[roundtrip] file:  " + before);
        System.out.println("[roundtrip] clone: " + after);
        JsonObject a = backup.getAsJsonObject("extendedContent");
        JsonObject b = again.getAsJsonObject("extendedContent");
        for (String key : a.keySet()) {
            System.out.println("[roundtrip] book data " + key + ": " + (b.has(key) ? "present" : "MISSING"));
        }
        assertEquals(before, after);
        for (String key : a.keySet()) {
            if (key.startsWith("attributes_")) {
                assertEquals(true, b.has(key), "book data lost: " + key);
            }
        }
        // the archive with the pictures (book.zip), restored the way the import of the app does it
        Path archive = file.resolveSibling("book.zip");
        if (Files.exists(archive)) {
            t0 = System.nanoTime();
            Manuscript restored = backupService.restoreAsNewManuscript(archive.toFile(), "from the archive", java.util.Map.of());
            System.out.println("[roundtrip] time: restoreAsNewManuscript of the archive " + ms(t0));
            JsonObject fromArchive = manuscriptService.createBackup(restored, true);
            Counts viaArchive = count(fromArchive);
            long pictures = 0;
            for (JsonElement element : fromArchive.getAsJsonArray("messages")) {
                JsonObject extended = element.getAsJsonObject().getAsJsonObject("extendedContent");
                pictures += extended != null && extended.has("imageAttachments") ? 1 : 0;
            }
            System.out.println("[roundtrip] archive: " + viaArchive + ", parts with a picture: " + pictures);
            assertEquals(before, viaArchive);
            assertEquals(1, pictures, "the picture of the archive");
        }
        JsonArray lorebooks = again.getAsJsonArray("lorebooks");
        System.out.println("[roundtrip] lorebooks linked to the clone: " + (lorebooks == null ? 0 : lorebooks.size()));
    }
}
