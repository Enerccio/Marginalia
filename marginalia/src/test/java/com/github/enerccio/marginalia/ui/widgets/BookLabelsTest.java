package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.combobox.ComboBox;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * How the combo boxes of books name a book.
 */
class BookLabelsTest extends MarginaliaTestBase {

    @Autowired
    private FolderService folderService;

    @Autowired
    private ManuscriptService manuscriptService;

    private Manuscript book(String name, Folder folder) throws Exception {
        Manuscript book = new Manuscript();
        book.setName(name);
        book.setFolder(folder);
        return manuscriptService.save(book);
    }

    @Test
    void aBookIsNamedWithTheFoldersItIsIn() throws Exception {
        login();
        Folder scifi = folderService.create("Sci-fi", null);
        Folder expanse = folderService.create("Expanse", scifi);
        Manuscript loose = book("Loose", null);
        Manuscript deep = book("Caliban", expanse);

        ComboBox<Manuscript> combo = new ComboBox<>();
        new BookLabels().apply(combo);

        assertThat(combo.getItemLabelGenerator().apply(loose)).isEqualTo("Loose");
        assertThat(combo.getItemLabelGenerator().apply(deep)).isEqualTo("Sci-fi / Expanse / Caliban");
    }
}
