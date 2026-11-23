package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.test.PlatformTestBase;
import com.github.enerccio.marginalia.utils.AppClock;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Storing a file that is already on the disk ({@link ResourceService#saveFile}), the upload file and the sweep of unused uploads.
 */
class ResourceServiceTest extends PlatformTestBase {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private Configuration configuration;

    @BeforeEach
    void setUp() throws Exception {
        login();
    }

    private File upload(String content) throws Exception {
        File file = resourceService.newUploadFile();
        Files.writeString(file.toPath(), content);
        return file;
    }

    private static Date later() {
        return new Date(AppClock.now().getTime() + 60_000);
    }

    @Test
    void uploadFileIsANewPartFileInTheResourcesFolder() throws Exception {
        File one = resourceService.newUploadFile();
        File two = resourceService.newUploadFile();

        assertThat(one.getParentFile()).isEqualTo(configuration.getResourcesFolder(currentUser));
        assertThat(one.getName()).endsWith(".part");
        assertThat(one).doesNotExist();
        assertThat(one).isNotEqualTo(two);
    }

    @Test
    void fileIsMovedAndDescribed() throws Exception {
        String content = uniqueName("clip");
        File file = upload(content);

        Resource resource = resourceService.saveFile(file, "clip.wav", "audio/wav");

        assertThat(file).doesNotExist();
        assertThat(resource.getId()).isNotNull();
        assertThat(resource.getOriginalName()).isEqualTo("clip.wav");
        assertThat(resource.getMimeType()).isEqualTo("audio/wav");
        assertThat(resource.getSize()).isEqualTo(content.length());
        assertThat(resource.getHash()).isEqualTo(DigestUtils.sha256Hex(content));
        assertThat(resource.getPath()).endsWith(".wav");
        assertThat(resource.getClazz()).isNull();
        File stored = resourceService.getResourceFile(resource);
        assertThat(stored.getParentFile()).isEqualTo(configuration.getResourcesFolder(currentUser));
        assertThat(stored).hasContent(content);
    }

    @Test
    void imageGoesToTheImagesFolderAndIsLinked() throws Exception {
        File file = upload(uniqueName("picture"));

        Resource resource = resourceService.saveFile(file, "picture.png", "image/png", User.class, 42L);

        assertThat(resourceService.getResourceFile(resource).getParentFile()).isEqualTo(configuration.getImagesFolder(currentUser));
        assertThat(resource.getClazz()).isEqualTo(User.class.getName());
        assertThat(resource.getObjectId()).isEqualTo(42L);
    }

    @Test
    void blankTypeIsOctetStream() throws Exception {
        Resource resource = resourceService.saveFile(upload(uniqueName("data")), "data.bin", " ");

        assertThat(resource.getMimeType()).isEqualTo("application/octet-stream");
    }

    @Test
    void sameContentSharesTheFile() throws Exception {
        String content = uniqueName("same");
        Resource first = resourceService.saveFile(upload(content), "a.txt", "text/plain");
        File second = upload(content);

        Resource other = resourceService.saveFile(second, "b.txt", "text/plain");

        assertThat(second).doesNotExist();
        assertThat(other.getId()).isNotEqualTo(first.getId());
        assertThat(other.getPath()).isEqualTo(first.getPath());
        assertThat(other.getOriginalName()).isEqualTo("b.txt");
    }

    @Test
    void sameContentOfAnUploadedArraySharesTheFile() throws Exception {
        String content = uniqueName("array");
        Resource first = resourceService.upload("a.txt", content.getBytes(StandardCharsets.UTF_8), "text/plain");

        Resource other = resourceService.saveFile(upload(content), "b.txt", "text/plain");

        assertThat(other.getPath()).isEqualTo(first.getPath());
    }

    @Test
    void sameContentInTheOtherFolderIsNotShared() throws Exception {
        String content = uniqueName("folders");
        Resource image = resourceService.saveFile(upload(content), "a.png", "image/png");

        Resource text = resourceService.saveFile(upload(content), "a.txt", "text/plain");

        // the file of the text is looked up in the resources folder, the image's is in the images folder
        assertThat(text.getPath()).isNotEqualTo(image.getPath());
        assertThat(resourceService.getResourceFile(text)).hasContent(content);
        assertThat(resourceService.getResourceFile(image)).hasContent(content);
    }

    @Test
    void deletedResourceLeavesTheFileOfTheOther() throws Exception {
        String content = uniqueName("shared");
        Resource first = resourceService.saveFile(upload(content), "a.txt", "text/plain");
        Resource other = resourceService.saveFile(upload(content), "b.txt", "text/plain");

        resourceService.softDelete(List.of(first.getUuid()));

        assertThat(resourceService.getResourceFile(other)).hasContent(content);
    }

    @Test
    void fileOutsideTheUsersFolderIsRefused() throws Exception {
        File outside = new File(configuration.getDataFolder(), uniqueName("outside") + ".txt");
        Files.writeString(outside.toPath(), "outside");
        File nested = new File(configuration.getResourcesFolder(currentUser), uniqueName("nested"));
        assertThat(nested.mkdirs()).isTrue();
        File inside = new File(nested, "x.txt");
        Files.writeString(inside.toPath(), "nested");
        File ofOther = upload("other user");
        login();

        assertThatThrownBy(() -> resourceService.saveFile(outside, "x.txt", "text/plain")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> resourceService.saveFile(inside, "x.txt", "text/plain")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> resourceService.saveFile(ofOther, "x.txt", "text/plain")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> resourceService.saveFile(resourceService.newUploadFile(), "x.txt", "text/plain"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(outside).exists();
        assertThat(ofOther).exists();
    }

    @Test
    void deleteUnlinkedSweepsOnlyOldUnusedUploadsOfTheName() throws Exception {
        Resource unused = resourceService.saveFile(upload(uniqueName("unused")), "chat-image", "image/png");
        Resource taken = resourceService.saveFile(upload(uniqueName("taken")), "chat-image", "image/png", User.class, 7L);
        Resource otherName = resourceService.saveFile(upload(uniqueName("other")), "avatar", "image/png");

        assertThat(resourceService.deleteUnlinked("chat-image", new Date(unused.getCreation().getTime() - 60_000))).isZero();

        assertThat(resourceService.deleteUnlinked("chat-image", later())).isEqualTo(1);

        assertThat(resourceService.findForUser(unused.getUuid())).isNull();
        assertThat(resourceService.findForUser(taken.getUuid())).isNotNull();
        assertThat(resourceService.findForUser(otherName.getUuid())).isNotNull();
        assertThat(resourceService.getResourceFile(unused)).exists();
    }

    @Test
    void deleteUnlinkedLeavesTheResourcesOfOtherUsers() throws Exception {
        Resource ofOther = resourceService.saveFile(upload(uniqueName("mine")), "chat-image", "image/png");
        User owner = userService.find(currentUser.getId());
        login();

        assertThat(resourceService.deleteUnlinked("chat-image", later())).isZero();

        loginAs(owner);
        assertThat(resourceService.findForUser(ofOther.getUuid())).isNotNull();
    }
}
