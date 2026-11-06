package com.github.enerccio.marginalia.workset;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.BackupService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The book of the dataset ("Unregistered", built by {@link WorksetBuilder}, see {@code TODO.DATASET.MD}) restored the way a user restores a backup, to have
 * something the size of a real book to look at: 100 parts on the main line and a branch of 3, 31 summaries, 1,697 annotations and passages, 360 versions of the parts,
 * the prompts the parts were generated from, the data of four plugins, a lorebook of 31 entries and a picture. The files are in {@code src/test/resources/workset}
 * (made by the harness, {@code work/harness/export}; to replace them copy {@code book.zip} and {@code lorebook.json} from there).
 * <p>
 * The book is restored <b>once per class</b> (about 5 seconds), the tests only read it.
 */
class LargeWorksetTest extends MarginaliaTestBase {

    private static final Gson GSON = new Gson();
    private static final String BOOK = "/workset/book.zip";
    private static final String LOREBOOK = "/workset/lorebook.json";

    /** What the first test of the class restored; the database is shared by the classes of a JVM, so it is made once. */
    private static String login;
    private static String bookUuid;
    private static long restoreMillis;
    private static JsonObject original;
    private static JsonObject backup;

    @Autowired
    private BackupService backupService;
    @Autowired
    private ManuscriptService manuscriptService;
    @Autowired
    private LorebookService lorebookService;
    @Autowired
    private ResourceService resourceService;

    @BeforeEach
    void restoreOnce() throws Exception {
        if (login == null) {
            User user = createUser(uniqueName("large"), DEFAULT_PASSWORD, false);
            loginAs(user);
            Path file = Files.createTempFile("workset-book", ".zip");
            try (InputStream in = LargeWorksetTest.class.getResourceAsStream(BOOK)) {
                assertNotNull(in, "the book of the dataset is not in the test resources: " + BOOK);
                Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
            }
            try (InputStream in = LargeWorksetTest.class.getResourceAsStream(LOREBOOK)) {
                assertNotNull(in, "the lorebook of the dataset is not in the test resources: " + LOREBOOK);
                // a backup only links its lorebook, so the lorebook comes first (its uuid is fixed, the book finds it)
                lorebookService.importLorebook(new String(in.readAllBytes(), StandardCharsets.UTF_8), "Unregistered");
            }
            long start = System.nanoTime();
            Manuscript restored = backupService.restoreAsNewManuscript(file.toFile(), uniqueName("Unregistered"), Map.of());
            restoreMillis = (System.nanoTime() - start) / 1_000_000;
            Files.deleteIfExists(file);
            backup = manuscriptService.createBackup(restored, true);
            try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(extract())) {
                JsonObject stored = GSON.fromJson(new String(zip.getInputStream(zip.getEntry("backup.json")).readAllBytes(), StandardCharsets.UTF_8), JsonObject.class);
                // the file is the stored backup (the book is in its "backup") or the book itself
                original = stored.has("backup") && stored.get("backup").isJsonObject() ? stored.getAsJsonObject("backup") : stored;
            }
            bookUuid = restored.getUuid();
            login = user.getLogin();
            System.out.println("[large workset] restored in " + restoreMillis + " ms");
        } else {
            loginAs(userService.findByName(login));
        }
    }

    private static java.io.File extract() throws Exception {
        java.io.File file = java.io.File.createTempFile("workset-book", ".zip");
        file.deleteOnExit();
        try (InputStream in = LargeWorksetTest.class.getResourceAsStream(BOOK)) {
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return file;
    }

    private static JsonArray messages(JsonObject book) {
        return book.getAsJsonArray("messages");
    }

    @Test
    void mainLineHasHundredPartsAndTheBranchHangsOffIt() {
        Map<String, JsonObject> byUuid = new HashMap<>();
        for (JsonElement e : messages(backup)) {
            byUuid.put(e.getAsJsonObject().get("uuid").getAsString(), e.getAsJsonObject());
        }
        assertEquals(103, byUuid.size(), "100 parts on the main line and 3 on the branch");
        Set<String> mainLine = new HashSet<>();
        String uuid = backup.get("activeLeafUuid").getAsString();
        while (uuid != null) {
            assertTrue(mainLine.add(uuid), "a cycle in the parents");
            JsonObject m = byUuid.get(uuid);
            assertNotNull(m, "a parent that is not in the book");
            uuid = m.has("parentUuid") && !m.get("parentUuid").isJsonNull() ? m.get("parentUuid").getAsString() : null;
        }
        assertEquals(100, mainLine.size(), "parts from the active leaf to the root");
        long roots = byUuid.values().stream().filter(m -> !m.has("parentUuid") || m.get("parentUuid").isJsonNull()).count();
        assertEquals(1, roots, "exactly one root");
        long off = byUuid.keySet().stream().filter(u -> !mainLine.contains(u)).count();
        assertEquals(3, off, "the branch");
    }

    @Test
    void restoreIsNotSlow() {
        // not a benchmark (the machines differ), a guard against a restore that got ten times worse: 5 seconds on a laptop, 60 is far above any machine we run on
        assertTrue(restoreMillis < 60_000, "restoring the book took " + restoreMillis + " ms");
    }

    @Test
    void theBookComesBackAsItWasExported() {
        int words = 0, annotations = 0, revisions = 0, summaries = 0;
        for (JsonElement e : messages(backup)) {
            JsonObject m = e.getAsJsonObject();
            words += m.get("wordCount").getAsInt();
            annotations += m.getAsJsonArray("annotations").size();
            revisions += m.has("revisions") ? m.getAsJsonArray("revisions").size() : 0;
            summaries += m.has("summary") && !m.get("summary").isJsonNull() ? 1 : 0;
        }
        assertEquals(98965, words);
        assertEquals(1697, annotations, "notes and the passages of the translations");
        assertEquals(360, revisions, "versions of the parts");
        assertEquals(31, summaries, "summaries and meta summaries");
        assertEquals(messages(original).size(), messages(backup).size());
    }

    @Test
    void thePromptsAndTheLoreTheyUsedAreKept() {
        int withPrompt = 0, withLore = 0;
        for (JsonElement e : messages(backup)) {
            JsonObject extended = e.getAsJsonObject().getAsJsonObject("extendedContent");
            withPrompt += extended != null && extended.has("builtPrompt") ? 1 : 0;
            withLore += extended != null && extended.has("loreUsage") ? 1 : 0;
        }
        assertTrue(withPrompt >= 100, "prompts kept: " + withPrompt);
        assertTrue(withLore >= 100, "lore usage kept: " + withLore);
    }

    @Test
    void theLorebookIsLinkedAndWhole() {
        JsonArray lorebooks = backup.getAsJsonArray("lorebooks");
        assertNotNull(lorebooks, "the book has no lorebook");
        assertEquals(1, lorebooks.size());
        assertEquals(31, lorebooks.get(0).getAsJsonObject().getAsJsonArray("entries").size());
    }

    @Test
    void thePluginsKeepTheirData() {
        JsonObject extended = backup.getAsJsonObject("extendedContent");
        for (String key : new String[]{"knowledgetracker", "conlangtranslator.book", "reallangtranslator.book", "arcoutline"}) {
            assertTrue(extended.keySet().stream().anyMatch(k -> k.startsWith("attributes_") && k.endsWith(key)), "data of the plugin lost: " + key);
        }
    }

    @Test
    void thePictureOfPartThirtyOneIsThereWithItsFile() throws Exception {
        int withPicture = 0;
        for (JsonElement e : messages(backup)) {
            JsonObject extended = e.getAsJsonObject().getAsJsonObject("extendedContent");
            if (extended != null && extended.has("imageAttachments")) {
                withPicture++;
                JsonArray images = GSON.fromJson(extended.get("imageAttachments").getAsString(), JsonArray.class);
                String resource = images.get(0).getAsJsonObject().get("resource").getAsString();
                assertNotNull(resourceService.findImage(resource), "the file of the picture was not restored");
            }
        }
        assertEquals(1, withPicture);
    }
}
