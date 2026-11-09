package com.github.enerccio.marginalia.backup;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupExport;
import com.github.enerccio.marginalia.domain.service.BackupService.ManuscriptBackup;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The resources a book points to besides the images of its parts (the audio an extension made for a part): its data names them by the uuid of the resource,
 * the backup carries that and the export packs the files, so a restored or cloned book has its own copies, noted for its own parts.
 */
class BackupResourcesTest extends BackupTestBase {

    @Autowired
    private ResourceService resourceService;

    private final List<File> temp = new ArrayList<>();

    private Manuscript original;
    private Resource audio;
    private Resource piece;
    private byte[] audioData;
    private byte[] pieceData;

    @BeforeEach
    void createBookWithAudio() throws Exception {
        login();
        original = story(manuscript("Spoken", ai(uniqueName("ai")), protocol(uniqueName("protocol")), null));
        audioData = ("joined audio " + uniqueName("take")).getBytes(StandardCharsets.UTF_8);
        pieceData = ("one piece " + uniqueName("take")).getBytes(StandardCharsets.UTF_8);

        // what an extension keeps with a part: the files are resources linked to the part, the part's attributes say which is which
        ChatMessage part = messageByResponse(original, "chapter 1");
        audio = resourceService.link(resourceService.upload("audiobook-part.mp3", audioData, "audio/mpeg"), ChatMessage.class, part.getId());
        piece = resourceService.link(resourceService.upload("audiobook-1.mp3", pieceData, "audio/mpeg"), ChatMessage.class, part.getId());
        JsonObject index = new JsonObject();
        JsonObject joined = new JsonObject();
        joined.addProperty("resource", audio.getUuid());
        index.add("part", joined);
        JsonObject segments = new JsonObject();
        segments.addProperty("key-1", piece.getUuid());
        index.add("segments", segments);
        part.getAttributes().add("audiobook", index);
        chatMessageService.save(part);
    }

    @AfterEach
    void deleteTemp() {
        temp.forEach(FileUtils::deleteQuietly);
    }

    private BackupExport export(boolean withResources, Runnable progress) throws Exception {
        BackupExport export = backupService.exportBackup(backupService.takeBackup(original), withResources, progress);
        temp.add(export.file());
        return export;
    }

    private JsonObject indexOf(Manuscript book) throws Exception {
        return messageByResponse(book, "chapter 1").getAttributes().getAsJsonObject("audiobook");
    }

    private String joinedOf(Manuscript book) throws Exception {
        return indexOf(book).getAsJsonObject("part").get("resource").getAsString();
    }

    private String pieceOf(Manuscript book) throws Exception {
        return indexOf(book).getAsJsonObject("segments").get("key-1").getAsString();
    }

    @Test
    void theBackupCountsTheResourcesTheDataOfThePartsPointsTo() throws Exception {
        ManuscriptBackup backup = backupService.takeBackup(original);

        assertThat(backup.getImageCount()).as("the joined audio and the piece").isEqualTo(2);
        // the uuids of the parts and of the book are not resources
        assertThat(backupService.takeBackup(manuscript("Plain", null, null, null)).getImageCount()).isZero();
    }

    @Test
    void theArchiveHasEveryResourceTheDataPointsTo() throws Exception {
        AtomicInteger progress = new AtomicInteger();

        BackupExport export = export(true, progress::incrementAndGet);

        assertThat(export.mimeType()).isEqualTo("application/zip");
        assertThat(export.missingImages()).isZero();
        assertThat(progress.get()).isEqualTo(2);
        try (ZipFile zip = new ZipFile(export.file())) {
            assertThat(zip.stream().map(ZipEntry::getName)).containsExactlyInAnyOrder("backup.json", "resources.json", "resources/1.mp3", "resources/2.mp3");
            JsonObject manifest = JsonParser.parseReader(new java.io.InputStreamReader(zip.getInputStream(zip.getEntry("resources.json")))).getAsJsonObject();
            assertThat(manifest.getAsJsonArray("resources")).hasSize(2);
            assertThat(manifest.getAsJsonArray("resources").get(0).getAsJsonObject().get("uuid").getAsString()).isIn(audio.getUuid(), piece.getUuid());
        }
    }

    @Test
    void withoutResourcesItIsJustTheJsonThatNamesThem() throws Exception {
        BackupExport export = export(false, null);

        assertThat(export.mimeType()).isEqualTo("application/json");
        String json = FileUtils.readFileToString(export.file(), StandardCharsets.UTF_8);
        assertThat(json).contains(audio.getUuid()).contains(piece.getUuid());
    }

    @Test
    void anArchiveRestoredAsANewBookGivesItsOwnResourcesNotedForItsOwnParts() throws Exception {
        Manuscript restored = backupService.restoreAsNewManuscript(export(true, null).file(), "Restored", Map.of());

        String joined = joinedOf(restored);
        String single = pieceOf(restored);
        assertThat(joined).isNotEqualTo(audio.getUuid());
        assertThat(single).isNotEqualTo(piece.getUuid());
        Resource restoredAudio = resourceService.findForUser(joined);
        assertThat(resourceService.getResourceData(restoredAudio)).isEqualTo(audioData);
        assertThat(resourceService.getResourceData(resourceService.findForUser(single))).isEqualTo(pieceData);
        // linked to the part they belong to now, the original ones stay with the original part
        assertThat(restoredAudio.getObjectId()).isEqualTo(messageByResponse(restored, "chapter 1").getId());
        assertThat(resourceService.findForUser(audio.getUuid()).getObjectId()).isEqualTo(messageByResponse(original, "chapter 1").getId());
        assertThat(joinedOf(original)).isEqualTo(audio.getUuid());
    }

    @Test
    void aJsonBackupRestoredAsANewBookKeepsTheReferencesAndTheOwnResourcesAreCopied() throws Exception {
        Manuscript restored = backupService.restoreAsNewManuscript(export(false, null).file(), "Restored", Map.of());

        // the user has the files, so the book gets copies of them
        assertThat(joinedOf(restored)).isNotEqualTo(audio.getUuid());
        assertThat(resourceService.getResourceData(resourceService.findForUser(joinedOf(restored)))).isEqualTo(audioData);
    }

    @Test
    void aClonedBookHasItsOwnCopiesOfTheResources() throws Exception {
        Manuscript copy = backupService.cloneManuscript(original, "Copy", false);

        assertThat(joinedOf(copy)).isNotEqualTo(audio.getUuid());
        assertThat(pieceOf(copy)).isNotEqualTo(piece.getUuid());
        assertThat(resourceService.getResourceData(resourceService.findForUser(joinedOf(copy)))).isEqualTo(audioData);
        assertThat(resourceService.findForUser(joinedOf(copy)).getObjectId()).isEqualTo(messageByResponse(copy, "chapter 1").getId());
        // deleting what the copy has does not touch the original
        resourceService.softDelete(List.of(joinedOf(copy)));
        assertThat(resourceService.getResourceData(resourceService.findForUser(audio.getUuid()))).isEqualTo(audioData);
    }
}
