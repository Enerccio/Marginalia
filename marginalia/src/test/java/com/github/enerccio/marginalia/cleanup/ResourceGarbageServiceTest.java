package com.github.enerccio.marginalia.cleanup;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.service.ResourceGarbageService;
import com.github.enerccio.marginalia.domain.service.ResourceGarbageService.DeleteResult;
import com.github.enerccio.marginalia.domain.service.ResourceGarbageService.StoredFile;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Garbage collection of the files of resources ({@link ResourceGarbageService}): a file is unused when no resource, not
 * even a soft deleted one, points to it.
 * <p>
 * The test data folder is shared, so tests only assert on the files of the user they created.
 */
class ResourceGarbageServiceTest extends MarginaliaTestBase {

    @Autowired
    private ResourceGarbageService garbageService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private Configuration configuration;

    @BeforeEach
    void setUp() throws Exception {
        loginAdmin();
    }

    @Test
    void onlyAdministratorsCollectGarbage() throws Exception {
        login();
        StoredFile file = new StoredFile("x", "images", "y.png", 1, 0);

        assertThatThrownBy(() -> garbageService.userFolders()).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> garbageService.listFiles("x")).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> garbageService.findUnused(List.of(file))).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> garbageService.getFile(file)).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> garbageService.writeZip(List.of(file), new ByteArrayOutputStream())).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> garbageService.delete(List.of(file))).isInstanceOf(SecurityException.class);
    }

    @Test
    void filesOfResourcesAreUsedAndTheOthersAreNot() throws Exception {
        Resource live = resourceService.upload("live.txt", uniqueName("live").getBytes(StandardCharsets.UTF_8), "text/plain");
        Resource trashed = resourceService.upload("trashed.txt", uniqueName("trashed").getBytes(StandardCharsets.UTF_8), "text/plain");
        resourceService.softDelete(List.of(trashed.getUuid()));
        File strayResource = stray(configuration.getResourcesFolder(currentUser), "stray.txt", "stray");
        File strayImage = stray(configuration.getImagesFolder(currentUser), "stray.png", "image");

        List<StoredFile> unused = garbageService.findUnused(garbageService.listFiles(currentUser.getLogin()));

        // the deleted resource may be restored from the trash, so its file is in use
        assertThat(unused).extracting(StoredFile::name)
                .containsExactlyInAnyOrder(strayResource.getName(), strayImage.getName())
                .doesNotContain(live.getPath(), trashed.getPath());
        assertThat(unused).extracting(StoredFile::path).contains(
                currentUser.getLogin() + "/resources/" + strayResource.getName(),
                currentUser.getLogin() + "/images/" + strayImage.getName());
        assertThat(unused).filteredOn(file -> file.name().equals(strayResource.getName())).singleElement()
                .satisfies(file -> assertThat(file.size()).isEqualTo("stray".length()));
    }

    @Test
    void replacedContentLeavesAnUnusedFile() throws Exception {
        Resource resource = resourceService.upload("note.txt", uniqueName("old").getBytes(StandardCharsets.UTF_8), "text/plain");
        String oldPath = resource.getPath();

        Resource replaced = resourceService.replace(resource, "note.txt", uniqueName("new").getBytes(StandardCharsets.UTF_8), "text/plain");

        assertThat(replaced.getPath()).isNotEqualTo(oldPath);
        assertThat(garbageService.findUnused(garbageService.listFiles(currentUser.getLogin())))
                .extracting(StoredFile::name).contains(oldPath).doesNotContain(replaced.getPath());
    }

    @Test
    void fileOfADeletedUserIsMatchedByNameInTheRetiredFolder() throws Exception {
        File retired = new File(configuration.getDataFolder(), uniqueName("gone") + "-deleted");
        File resources = new File(retired, "resources");
        assertThat(resources.mkdirs()).isTrue();
        Resource resource = resourceService.upload("kept.txt", uniqueName("kept").getBytes(StandardCharsets.UTF_8), "text/plain");
        Files.copy(resourceService.getResourceFile(resource).toPath(), new File(resources, resource.getPath()).toPath());
        stray(resources, "lost.txt", "lost");

        assertThat(garbageService.userFolders()).contains(retired.getName());
        assertThat(garbageService.findUnused(garbageService.listFiles(retired.getName())))
                .extracting(StoredFile::name).containsExactly("lost.txt");
    }

    @Test
    void foldersAndLinksAreNotListed() throws Exception {
        File resources = configuration.getResourcesFolder(currentUser);
        File folder = new File(resources, uniqueName("folder"));
        assertThat(folder.mkdirs()).isTrue();
        stray(folder, "inside.txt", "inside");
        File target = stray(resources, "target.txt", "target");
        Files.createSymbolicLink(new File(resources, "link.txt").toPath(), target.toPath());

        assertThat(garbageService.listFiles(currentUser.getLogin())).extracting(StoredFile::name)
                .contains("target.txt").doesNotContain("inside.txt", "link.txt", folder.getName());
    }

    @Test
    void deleteRemovesTheFilesAndKeepsTheOnesInUseNow() throws Exception {
        File gone = stray(configuration.getResourcesFolder(currentUser), "gone.txt", "gone");
        File adopted = stray(configuration.getResourcesFolder(currentUser), "adopted.txt", "adopted");
        List<StoredFile> unused = garbageService.findUnused(garbageService.listFiles(currentUser.getLogin()));
        assertThat(unused).extracting(StoredFile::name).contains("gone.txt", "adopted.txt");

        // a resource points to the file after the scan
        Resource resource = new Resource();
        resource.setMimeType("text/plain");
        resource.setOriginalName("adopted.txt");
        resource.setPath("adopted.txt");
        resource.setHash("hash");
        resourceService.save(resource);

        DeleteResult result = garbageService.delete(unused.stream().filter(f -> f.name().equals("gone.txt") || f.name().equals("adopted.txt")).toList());

        assertThat(result.deleted()).isEqualTo(1);
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.bytes()).isEqualTo("gone".length());
        assertThat(gone).doesNotExist();
        assertThat(adopted).exists();
    }

    @Test
    void zipHoldsTheFilesUnderTheirPaths() throws Exception {
        stray(configuration.getResourcesFolder(currentUser), "zipped.txt", "zipped content");
        stray(configuration.getImagesFolder(currentUser), "zipped.png", "png");
        List<StoredFile> files = garbageService.listFiles(currentUser.getLogin()).stream()
                .filter(f -> f.name().startsWith("zipped.")).toList();
        assertThat(files).hasSize(2);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        garbageService.writeZip(files, out);

        Map<String, String> entries = new TreeMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()))) {
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                entries.put(entry.getName(), new String(zip.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        assertThat(entries).containsOnly(
                Map.entry(currentUser.getLogin() + "/images/zipped.png", "png"),
                Map.entry(currentUser.getLogin() + "/resources/zipped.txt", "zipped content"));
    }

    @Test
    void filesOutsideTheResourceFoldersAreRefused() throws Exception {
        File outside = new File(configuration.getUserDataFolder(currentUser), "note.txt");
        Files.writeString(outside.toPath(), "private");
        List<StoredFile> attempts = new ArrayList<>(List.of(
                new StoredFile(currentUser.getLogin(), "images", "../note.txt", 0, 0),
                new StoredFile(currentUser.getLogin(), "..", "note.txt", 0, 0),
                new StoredFile("..", "images", "note.txt", 0, 0),
                new StoredFile(currentUser.getLogin(), "other", "note.txt", 0, 0),
                new StoredFile(currentUser.getLogin() + "/images", "resources", "note.txt", 0, 0)));

        for (StoredFile attempt : attempts) {
            assertThatThrownBy(() -> garbageService.getFile(attempt)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> garbageService.delete(List.of(attempt))).isInstanceOf(IllegalArgumentException.class);
        }
        assertThat(outside).exists();
    }

    private static File stray(File folder, String name, String content) throws Exception {
        File file = new File(folder, name);
        Files.writeString(file.toPath(), content);
        return file;
    }
}
