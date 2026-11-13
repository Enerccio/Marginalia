package com.github.enerccio.marginalia.backup;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupExport;
import com.github.enerccio.marginalia.domain.service.LoreImages;
import com.github.enerccio.marginalia.domain.service.LorebookService.LorebookDecision;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The images of lorebook entries ({@code ![caption](resource:<uuid>)} in the content) travel with the lorebook: a clone, a lorebook
 * file, a copy of a shared lorebook and a book backup give the new entries images of their own with the same content.
 */
class LoreImagesTravelTest extends BackupTestBase {

    @Autowired
    private ResourceService resourceService;

    private final List<File> temp = new ArrayList<>();

    private User author;
    private Lorebook lorebook;
    private Resource image;
    private byte[] imageData;

    @BeforeEach
    void aLorebookWithAnImage() throws Exception {
        author = createUser();
        loginAs(author);
        BufferedImage picture = new BufferedImage(5, 2, BufferedImage.TYPE_INT_RGB);
        picture.setRGB(1, 1, 0xff0000);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(picture, "png", png);
        image = resourceService.uploadImage("map.png", png.toByteArray());
        imageData = resourceService.getResourceData(image);
        lorebook = lorebook(uniqueName("World"));
        entry(lorebook, "Port", entry -> entry.setPayload("The port.\n\n" + LoreImages.markup("Map", image.getUuid())));
    }

    @AfterEach
    void deleteTemp() {
        temp.forEach(FileUtils::deleteQuietly);
    }

    private String imageOf(Lorebook book) throws Exception {
        List<LorebookEntry> entries = lorebookEntryService.getEntriesForLorebook(book);
        assertThat(entries).hasSize(1);
        assertThat(entries.getFirst().getPayload()).startsWith("The port.");
        return LoreImages.uuids(entries.getFirst().getPayload()).iterator().next();
    }

    /** The image is the user's, another resource than the original one, with the same content. */
    private void ownCopy(String uuid) throws Exception {
        assertThat(uuid).isNotEqualTo(image.getUuid());
        Resource copy = resourceService.findImage(uuid);
        assertThat(copy).isNotNull();
        assertThat(resourceService.getResourceData(copy)).isEqualTo(imageData);
    }

    @Test
    void aClonedLorebookHasItsOwnImages() throws Exception {
        Lorebook clone = lorebookService.cloneLorebook(lorebook, "Copy");

        ownCopy(imageOf(clone));
        assertThat(imageOf(lorebook)).isEqualTo(image.getUuid());
    }

    @Test
    void aLorebookFileCarriesItsImagesToAnotherUser() throws Exception {
        String json = lorebookService.exportLorebook(lorebook);
        assertThat(JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("images").has(image.getUuid())).isTrue();

        loginAs(createUser());
        Lorebook imported = lorebookService.importLorebook(json, null);

        ownCopy(imageOf(imported));
    }

    @Test
    void aCopyOfASharedLorebookGetsCopiesOfItsImages() throws Exception {
        lorebookService.share(lorebook, false);

        loginAs(createUser());
        Lorebook copy = lorebookService.importShared(lorebook, null);

        ownCopy(imageOf(copy));
        assertThat(resourceService.findImage(image.getUuid())).as("the author's image is not the reader's").isNull();
    }

    @Test
    void aBookBackupPacksTheImagesOfItsLorebookAndARestoredBookHasItsOwn() throws Exception {
        Manuscript book = manuscript(uniqueName("Book"), null, null, lorebook);
        BackupExport export = backupService.exportBackup(backupService.takeBackup(book), true, null);
        temp.add(export.file());
        try (ZipFile zip = new ZipFile(export.file())) {
            assertThat(zip.stream().map(ZipEntry::getName)).contains("resources.json");
            JsonObject manifest = JsonParser.parseReader(new java.io.InputStreamReader(zip.getInputStream(zip.getEntry("resources.json")))).getAsJsonObject();
            assertThat(manifest.getAsJsonArray("resources").get(0).getAsJsonObject().get("uuid").getAsString()).isEqualTo(image.getUuid());
        }

        Manuscript restored = backupService.restoreAsNewManuscript(export.file(), "Restored", Map.of(lorebook.getUuid(), LorebookDecision.CREATE));

        Lorebook restoredBook = lorebookService.find(reload(restored).getLorebook());
        assertThat(restoredBook.getId()).isNotEqualTo(lorebook.getId());
        ownCopy(imageOf(restoredBook));
    }
}
