package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.OwnedService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class FolderCrudTest extends ExtendableCrudContract<Folder> {

    @Autowired
    private FolderService folderService;

    @Override
    protected OwnedService<Folder, ?> service() {
        return folderService;
    }

    @Override
    protected Folder newEntity() {
        Folder folder = new Folder();
        folder.setName("Sci-fi");
        folder.setDescription("Books in space");
        folder.setStyle("hard science fiction");
        folder.setLanguage(Language.ENGLISH);
        return folder;
    }

    @Override
    protected void assertCreated(Folder loaded) {
        assertThat(loaded.getName()).isEqualTo("Sci-fi");
        assertThat(loaded.getDescription()).isEqualTo("Books in space");
        assertThat(loaded.getStyle()).isEqualTo("hard science fiction");
        assertThat(loaded.getLanguage()).isEqualTo(Language.ENGLISH);
        assertThat(loaded.getParent()).isNull();
    }

    @Override
    protected void modify(Folder entity) {
        entity.setName("Fantasy");
        entity.setStyle("lyrical");
        entity.setPov(null);
    }

    @Override
    protected void assertModified(Folder loaded) {
        assertThat(loaded.getName()).isEqualTo("Fantasy");
        assertThat(loaded.getStyle()).isEqualTo("lyrical");
    }

    @Test
    void unsetSettingsStayNull() throws Exception {
        Folder saved = folderService.create("Plain", null);

        Folder loaded = reload(saved);

        assertThat(loaded.getStyle()).isNull();
        assertThat(loaded.getPov()).isNull();
        assertThat(loaded.getTemplate()).isNull();
        assertThat(loaded.getLanguage()).isNull();
    }

    @Test
    void settingsAreFound() throws Exception {
        Folder saved = create();

        assertThat(folderService.findForUser(saved.getUuid()).getName()).isEqualTo("Sci-fi");
    }
}
