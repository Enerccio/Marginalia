package com.github.enerccio.marginalia.backup;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.domain.model.ImageAttachment;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.security.model.User;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The images of the parts and the book backups: a backup remembers which images a part has but doesn't carry the files,
 * the files travel in an archive made on export, and every restored or cloned book gets resources of its own.
 */
class BackupImagesTest extends BackupTestBase {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private Configuration configuration;

    private final List<File> temp = new ArrayList<>();

    private User author;
    private Manuscript original;
    private Resource image;
    private byte[] imageData;

    @BeforeEach
    void createBookWithImage() throws Exception {
        author = login();
        original = story(manuscript("Illustrated", ai(uniqueName("ai")), protocol(uniqueName("protocol")), null));

        BufferedImage picture = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        picture.setRGB(1, 1, uniqueName("pixel").hashCode());
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(picture, "png", png);
        imageData = png.toByteArray();

        ChatMessage part = messageByResponse(original, "chapter 1");
        image = resourceService.uploadImage("map.png", imageData, ChatMessage.class, part.getId());
        part.setImages(List.of(new ImageAttachment(image.getUuid(), "The map")));
        chatMessageService.save(part);
    }

    @AfterEach
    void deleteTemp() {
        temp.forEach(FileUtils::deleteQuietly);
    }

    private File temp(String suffix) throws IOException {
        File file = File.createTempFile("backup-images-test", suffix);
        temp.add(file);
        return file;
    }

    private File export(boolean withImages) throws Exception {
        BackupExport export = backupService.exportBackup(backupService.takeBackup(original), withImages, null);
        temp.add(export.file());
        return export.file();
    }

    private List<ImageAttachment> imagesOf(Manuscript manuscript, String response) throws Exception {
        return messageByResponse(manuscript, response).getImages();
    }

    private Resource resourceOf(Manuscript manuscript, String response) throws Exception {
        List<ImageAttachment> images = imagesOf(manuscript, response);
        assertThat(images).hasSize(1);
        return resourceService.findImage(images.getFirst().resource());
    }

    // ------------------------------------------------------------------------------------------------------------
    // backup
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void backupCountsTheImagesInItsHeaderAndDoesNotCarryThem() throws Exception {
        ManuscriptBackup backup = backupService.takeBackup(original);

        assertThat(backup.getImageCount()).isEqualTo(1);
        ManuscriptBackup listed = backupService.getBackups(original).stream()
                .filter(b -> b.getFile().equals(backup.getFile())).findFirst().orElseThrow();
        assertThat(listed.isLoaded()).isFalse();
        assertThat(listed.getImageCount()).isEqualTo(1);

        String stored = FileUtils.readFileToString(new File(backup.getFile()), StandardCharsets.UTF_8);
        assertThat(stored).contains(image.getUuid());
        // no image data in it, whatever the encoding (PNG signature as base64)
        assertThat(stored).doesNotContain("iVBORw0KGgo");
        assertThat(backupService.takeBackup(manuscript("Plain", null, null, null)).getImageCount()).isZero();
    }

    @Test
    void exportWithoutImagesIsTheStoredJson() throws Exception {
        ManuscriptBackup backup = backupService.takeBackup(original);

        BackupExport export = backupService.exportBackup(backup, false, null);
        temp.add(export.file());

        assertThat(export.mimeType()).isEqualTo("application/json");
        assertThat(export.fileName()).isEqualTo("Illustrated_backup.json");
        assertThat(export.missingImages()).isZero();
        assertThat(FileUtils.readFileToString(export.file(), StandardCharsets.UTF_8))
                .isEqualTo(FileUtils.readFileToString(new File(backup.getFile()), StandardCharsets.UTF_8));
    }

    @Test
    void exportWithImagesIsAnArchive() throws Exception {
        ManuscriptBackup backup = backupService.takeBackup(original);
        AtomicInteger progress = new AtomicInteger();

        BackupExport export = backupService.exportBackup(backup, true, progress::incrementAndGet);
        temp.add(export.file());

        assertThat(export.mimeType()).isEqualTo("application/zip");
        assertThat(export.fileName()).isEqualTo("Illustrated_backup.zip");
        assertThat(export.missingImages()).isZero();
        assertThat(progress.get()).isEqualTo(backup.getImageCount());
        try (ZipFile zip = new ZipFile(export.file())) {
            assertThat(zip.stream().map(ZipEntry::getName)).containsExactlyInAnyOrder("backup.json", "resources.json", "resources/1.png");
            assertThat(zip.getInputStream(zip.getEntry("resources/1.png")).readAllBytes()).isEqualTo(imageData);
            JsonObject manifest = JsonParser.parseReader(new java.io.InputStreamReader(zip.getInputStream(zip.getEntry("resources.json")))).getAsJsonObject();
            assertThat(manifest.getAsJsonArray("resources").get(0).getAsJsonObject().get("uuid").getAsString()).isEqualTo(image.getUuid());
        }
    }

    @Test
    void exportSaysWhichImagesCouldNotBePacked() throws Exception {
        ManuscriptBackup backup = backupService.takeBackup(original);
        resourceService.softDelete(List.of(image.getUuid()));
        AtomicInteger progress = new AtomicInteger();

        BackupExport export = backupService.exportBackup(backup, true, progress::incrementAndGet);
        temp.add(export.file());

        assertThat(export.missingImages()).isEqualTo(1);
        assertThat(progress.get()).isEqualTo(1);
        try (ZipFile zip = new ZipFile(export.file())) {
            assertThat(zip.stream().map(ZipEntry::getName)).containsExactlyInAnyOrder("backup.json", "resources.json");
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // restore as a new book
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void archiveRestoredAsNewBookGivesItsOwnResources() throws Exception {
        File archive = export(true);

        Manuscript restored = backupService.restoreAsNewManuscript(archive, "Restored", Map.of());

        Resource copy = resourceOf(restored, "chapter 1");
        assertThat(copy.getUuid()).isNotEqualTo(image.getUuid());
        // the file of the original is found and used, it is not stored twice
        assertThat(copy.getPath()).isEqualTo(image.getPath());
        assertThat(resourceService.getResourceData(copy)).isEqualTo(imageData);
        assertThat(imagesOf(restored, "chapter 1").getFirst().caption()).isEqualTo("The map");
        // each book has its own resource, noted for its own part
        assertThat(copy.getObjectId()).isEqualTo(messageByResponse(restored, "chapter 1").getId());
        assertThat(resourceService.describeLink(copy).present()).isTrue();
        assertThat(resourceService.findImage(image.getUuid()).getObjectId()).isEqualTo(messageByResponse(original, "chapter 1").getId());
        assertThat(imagesOf(original, "chapter 1").getFirst().resource()).isEqualTo(image.getUuid());
    }

    private int imageFiles() {
        String[] files = configuration.getImagesFolder(author).list();
        return files == null ? 0 : files.length;
    }

    @Test
    void restoringAsNewBookNeverStoresTheSameFileTwice() throws Exception {
        File archive = export(true);
        File json = export(false);
        int files = imageFiles();
        long resources = resourceService.countForUser();

        Manuscript fromArchive = backupService.restoreAsNewManuscript(archive, "From archive", Map.of());
        Manuscript fromJson = backupService.restoreAsNewManuscript(json, "From json", Map.of());
        Manuscript cloned = backupService.cloneBackup(backupService.takeBackup(original), "Cloned", Map.of(), true);

        // new resources for every book, all of them using the one file
        assertThat(resourceService.countForUser()).isEqualTo(resources + 3);
        assertThat(imageFiles()).isEqualTo(files);
        List<Resource> copies = List.of(resourceOf(fromArchive, "chapter 1"), resourceOf(fromJson, "chapter 1"), resourceOf(cloned, "chapter 1"));
        assertThat(copies).extracting(Resource::getPath).containsOnly(image.getPath());
        assertThat(copies).extracting(Resource::getUuid).doesNotContain(image.getUuid()).doesNotHaveDuplicates();
    }

    @Test
    void theFileIsFoundEvenWhenTheOriginalResourceWasDeleted() throws Exception {
        File archive = export(true);
        resourceService.softDelete(List.of(image.getUuid()));
        int files = imageFiles();

        Manuscript restored = backupService.restoreAsNewManuscript(archive, "From archive", Map.of());

        Resource copy = resourceOf(restored, "chapter 1");
        assertThat(copy.getUuid()).isNotEqualTo(image.getUuid());
        assertThat(copy.getPath()).isEqualTo(image.getPath());
        assertThat(imageFiles()).isEqualTo(files);
        assertThat(resourceService.getResourceData(copy)).isEqualTo(imageData);
    }

    @Test
    void archiveRestoredByAnotherUserStoresTheFiles() throws Exception {
        File archive = export(true);
        User other = loginAs(createUser());

        Manuscript restored = backupService.restoreAsNewManuscript(archive, "Restored", Map.of());

        Resource copy = resourceOf(restored, "chapter 1");
        assertThat(copy.getOwner().getId()).isEqualTo(other.getId());
        assertThat(resourceService.getResourceData(copy)).isEqualTo(imageData);
        assertThat(new File(configuration.getImagesFolder(other), copy.getPath())).exists();
    }

    @Test
    void jsonRestoredAsNewBookCopiesTheImagesTheUserStillHas() throws Exception {
        File json = export(false);

        Manuscript restored = backupService.restoreAsNewManuscript(json, "Restored", Map.of());

        Resource copy = resourceOf(restored, "chapter 1");
        assertThat(copy.getUuid()).isNotEqualTo(image.getUuid());
        assertThat(copy.getPath()).isEqualTo(image.getPath());
        assertThat(resourceService.describeLink(copy).present()).isTrue();
    }

    @Test
    void jsonRestoredByAnotherUserHasNoImages() throws Exception {
        File json = export(false);
        loginAs(createUser());

        Manuscript restored = backupService.restoreAsNewManuscript(json, "Restored", Map.of());

        assertThat(imagesOf(restored, "chapter 1")).isEmpty();
        assertThat(imagesOf(restored, "prologue")).isEmpty();
    }

    @Test
    void oldBackupWithoutImagesStillRestores() throws Exception {
        Manuscript plain = story(manuscript("Plain", null, null, null));
        BackupExport export = backupService.exportBackup(backupService.takeBackup(plain), true, null);
        temp.add(export.file());

        Manuscript restored = backupService.restoreAsNewManuscript(export.file(), null, Map.of());

        assertThat(tree(restored)).isEqualTo(storyTree());
        assertThat(export.missingImages()).isZero();
    }

    // ------------------------------------------------------------------------------------------------------------
    // clone
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void cloneWithImagesGivesTheCloneItsOwnCopies() throws Exception {
        ManuscriptBackup backup = backupService.takeBackup(original);

        Manuscript clone = backupService.cloneBackup(backup, "Clone", Map.of(), true);

        Resource copy = resourceOf(clone, "chapter 1");
        assertThat(copy.getUuid()).isNotEqualTo(image.getUuid());
        assertThat(copy.getPath()).isEqualTo(image.getPath());
        assertThat(copy.getObjectId()).isEqualTo(messageByResponse(clone, "chapter 1").getId());
        // the loaded backup is not changed by the clone
        assertThat(backup.getImageCount()).isEqualTo(1);
        assertThat(BackupImagesProbe.uuids(backup)).containsExactly(image.getUuid());
    }

    @Test
    void cloneWithoutImagesHasNone() throws Exception {
        ManuscriptBackup backup = backupService.takeBackup(original);

        Manuscript clone = backupService.cloneBackup(backup, "Clone", Map.of(), false);
        Manuscript defaultClone = backupService.cloneBackup(backup, "Clone 2", Map.of());

        assertThat(imagesOf(clone, "chapter 1")).isEmpty();
        assertThat(imagesOf(defaultClone, "chapter 1")).isEmpty();
        assertThat(imagesOf(original, "chapter 1")).hasSize(1);
    }

    @Test
    void cloneLeavesOutAnImageTheUserDeleted() throws Exception {
        ManuscriptBackup backup = backupService.takeBackup(original);
        resourceService.softDelete(List.of(image.getUuid()));

        Manuscript clone = backupService.cloneBackup(backup, "Clone", Map.of(), true);

        assertThat(imagesOf(clone, "chapter 1")).isEmpty();
    }

    // ------------------------------------------------------------------------------------------------------------
    // import into the backups of a book, restore in place
    // ------------------------------------------------------------------------------------------------------------

    @Test
    void importedArchiveOfTheSameUserUsesTheResourcesThatAreThere() throws Exception {
        File archive = export(true);
        long before = resourceService.countForUser();

        ManuscriptBackup imported = backupService.importBackup(original, archive);

        assertThat(resourceService.countForUser()).isEqualTo(before);
        assertThat(imported.getImageCount()).isEqualTo(1);
        assertThat(FileUtils.readFileToString(new File(imported.getFile()), StandardCharsets.UTF_8)).contains(image.getUuid());
    }

    @Test
    void importedArchiveOfAnotherUserAddsTheImages() throws Exception {
        File archive = export(true);
        User other = loginAs(createUser());
        Manuscript target = manuscript("Target", null, null, null);

        ManuscriptBackup imported = backupService.importBackup(target, archive);

        assertThat(imported.getImageCount()).isEqualTo(1);
        List<Resource> added = resourceService.findPageForUser(0, 10, "creation", false);
        assertThat(added).hasSize(1);
        assertThat(added.getFirst().getOwner().getId()).isEqualTo(other.getId());
        String stored = FileUtils.readFileToString(new File(imported.getFile()), StandardCharsets.UTF_8);
        assertThat(stored).contains(added.getFirst().getUuid()).doesNotContain(image.getUuid());

        // and the book can be restored from it, the images are there
        Manuscript restored = backupService.applyBackup(target, imported, false, Map.of());
        assertThat(resourceOf(restored, "chapter 1").getUuid()).isEqualTo(added.getFirst().getUuid());
    }

    @Test
    void restoreInPlaceNotesTheNewPartsInTheResources() throws Exception {
        ManuscriptBackup backup = backupService.takeBackup(original);

        Manuscript restored = backupService.applyBackup(original, backup, false, Map.of());

        ChatMessage part = messageByResponse(restored, "chapter 1");
        assertThat(part.getImages()).hasSize(1);
        Resource resource = resourceService.findImage(image.getUuid());
        // the parts are made again, the resource points to the part that exists
        assertThat(resource.getObjectId()).isEqualTo(part.getId());
        assertThat(resourceService.describeLink(resource).present()).isTrue();
    }

    // ------------------------------------------------------------------------------------------------------------
    // what is in the file is not trusted
    // ------------------------------------------------------------------------------------------------------------

    private File archiveWith(String entryName, String manifestEntry, byte[] data) throws Exception {
        File stored = new File(backupService.takeBackup(original).getFile());
        File archive = temp(".zip");
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(archive))) {
            zip.putNextEntry(new ZipEntry("backup.json"));
            zip.write(FileUtils.readFileToByteArray(stored));
            zip.closeEntry();
            if (entryName != null) {
                zip.putNextEntry(new ZipEntry(entryName));
                zip.write(data);
                zip.closeEntry();
            }
            JsonObject item = new JsonObject();
            item.addProperty("uuid", image.getUuid());
            item.addProperty("entry", manifestEntry);
            item.addProperty("name", "map.png");
            JsonObject manifest = new JsonObject();
            manifest.add("resources", new com.google.gson.JsonArray());
            manifest.getAsJsonArray("resources").add(item);
            zip.putNextEntry(new ZipEntry("resources.json"));
            zip.write(manifest.toString().getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return archive;
    }

    @Test
    void anEntryThatIsNotAnImageIsLeftOut() throws Exception {
        // another user, so the image can't be found among the resources either
        File archive = archiveWith("resources/1.png", "resources/1.png", "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8));
        loginAs(createUser());

        Manuscript restored = backupService.restoreAsNewManuscript(archive, null, Map.of());

        assertThat(imagesOf(restored, "chapter 1")).isEmpty();
        assertThat(resourceService.countForUser()).isZero();
    }

    @Test
    void manifestCanNotPointOutsideOfTheImages() throws Exception {
        File archive = archiveWith("../evil.png", "../evil.png", imageData);
        loginAs(createUser());

        Manuscript restored = backupService.restoreAsNewManuscript(archive, null, Map.of());

        assertThat(imagesOf(restored, "chapter 1")).isEmpty();
    }

    @Test
    void archiveWithoutBackupIsRefused() throws Exception {
        File archive = temp(".zip");
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(archive))) {
            zip.putNextEntry(new ZipEntry("something.txt"));
            zip.write(1);
            zip.closeEntry();
        }

        assertThatThrownBy(() -> backupService.restoreAsNewManuscript(archive, null, Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> backupService.importBackup(original, archive))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> backupService.analyzeLorebooks(archive))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void emptyAndBrokenFilesAreRefused() throws Exception {
        File empty = temp(".json");
        File broken = temp(".json");
        FileUtils.writeStringToFile(broken, "{ not json", StandardCharsets.UTF_8);

        assertThatThrownBy(() -> backupService.importBackup(original, empty)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> backupService.importBackup(original, broken)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> backupService.restoreAsNewManuscript(broken, null, Map.of())).isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Reads the images of a loaded backup the way a restore would.
     */
    private static final class BackupImagesProbe {
        static List<String> uuids(ManuscriptBackup backup) {
            List<String> uuids = new ArrayList<>();
            backup.getBackup().getAsJsonArray("messages").forEach(m -> {
                JsonObject extended = m.getAsJsonObject().getAsJsonObject("extendedContent");
                if (extended != null && extended.has("imageAttachments")) {
                    com.google.gson.JsonArray array = JsonParser.parseString(extended.get("imageAttachments").getAsString()).getAsJsonArray();
                    array.forEach(i -> uuids.add(i.getAsJsonObject().get("resource").getAsString()));
                }
            });
            return uuids;
        }
    }
}
