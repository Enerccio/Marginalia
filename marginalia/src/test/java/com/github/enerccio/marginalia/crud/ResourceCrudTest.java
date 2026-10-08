package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.service.OwnedService;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceCrudTest extends OwnedCrudContract<Resource> {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private Configuration configuration;

    @Override
    protected OwnedService<Resource, ?> service() {
        return resourceService;
    }

    @Override
    protected Resource newEntity() {
        Resource resource = new Resource();
        resource.setMimeType("text/plain");
        resource.setOriginalName("notes.txt");
        resource.setPath("stored.txt");
        resource.setHash("hash");
        resource.setSize(42);
        return resource;
    }

    @Override
    protected void assertCreated(Resource loaded) {
        assertThat(loaded.getMimeType()).isEqualTo("text/plain");
        assertThat(loaded.getOriginalName()).isEqualTo("notes.txt");
        assertThat(loaded.getPath()).isEqualTo("stored.txt");
        assertThat(loaded.getHash()).isEqualTo("hash");
        assertThat(loaded.getSize()).isEqualTo(42);
    }

    @Override
    protected void modify(Resource entity) {
        entity.setOriginalName("renamed.txt");
    }

    @Override
    protected void assertModified(Resource loaded) {
        assertThat(loaded.getOriginalName()).isEqualTo("renamed.txt");
        assertThat(loaded.getPath()).isEqualTo("stored.txt");
    }

    @Test
    void uploadStoresFileInUserFolder() throws Exception {
        byte[] content = "hello resource".getBytes(StandardCharsets.UTF_8);

        Resource resource = resourceService.upload("hello.txt", content, "text/plain");

        assertThat(resource.getSize()).isEqualTo(content.length);
        assertThat(resource.getOriginalName()).isEqualTo("hello.txt");
        assertThat(resource.getPath()).endsWith(".txt");
        assertThat(new File(configuration.getResourcesFolder(owner), resource.getPath())).exists();
        assertThat(resourceService.getResourceData(reload(resource))).isEqualTo(content);
        assertThat(resourceService.findByHash(resource.getHash(), owner).getId()).isNotNull();
    }

    @Test
    void imagesGoToImagesFolder() throws Exception {
        Resource image = resourceService.upload("pic.png", new byte[]{1, 2, 3}, "image/png");

        assertThat(new File(configuration.getImagesFolder(owner), image.getPath())).exists();
        assertThat(new File(configuration.getResourcesFolder(owner), image.getPath())).doesNotExist();
    }

    @Test
    void identicalUploadReusesStoredFile() throws Exception {
        byte[] content = uniqueName("same").getBytes(StandardCharsets.UTF_8);

        Resource first = resourceService.upload("a.txt", content, "text/plain");
        Resource second = resourceService.upload("b.txt", content, "text/plain");

        assertThat(second.getId()).isNotEqualTo(first.getId());
        assertThat(second.getPath()).isEqualTo(first.getPath());
        assertThat(second.getHash()).isEqualTo(first.getHash());
    }

    @Test
    void sameContentOfOtherUserIsStoredSeparately() throws Exception {
        byte[] content = uniqueName("shared").getBytes(StandardCharsets.UTF_8);
        Resource mine = resourceService.upload("a.txt", content, "text/plain");

        loginAs(createUser());
        Resource theirs = resourceService.upload("a.txt", content, "text/plain");

        assertThat(theirs.getPath()).isNotEqualTo(mine.getPath());
        assertThat(resourceService.getResourceData(theirs)).isEqualTo(content);
    }
}
