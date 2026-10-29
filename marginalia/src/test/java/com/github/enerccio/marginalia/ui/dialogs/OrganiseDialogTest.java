package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The structure of the two-pane dialog: what the panes load and how they move about.
 */
class OrganiseDialogTest extends MarginaliaTestBase {

    @Autowired
    private FolderService folderService;

    @Autowired
    private ManuscriptService manuscriptService;

    // UI.setCurrent keeps only a weak reference
    private UI ui;
    private Folder scifi;
    private Folder expanse;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        scifi = folderService.create("Sci-fi", null);
        expanse = folderService.create("Expanse", scifi);
        folderService.create("Fantasy", null);
        book("Zeta", null);
        book("alpha", null);
        book("Leviathan", scifi);
        book("Caliban", expanse);
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    private void book(String name, Folder folder) throws Exception {
        Manuscript book = new Manuscript();
        book.setName(name);
        book.setFolder(folder);
        manuscriptService.save(book);
    }

    private OrganiseDialog dialog(Folder left, Folder right) {
        OrganiseDialog dialog = new OrganiseDialog(left, right);
        dialog.create();
        return dialog;
    }

    @Test
    void thePanesListTheFoldersThenTheBooksByName() {
        OrganiseDialog dialog = dialog(null, scifi);

        assertThat(dialog.names(true)).containsExactly("Fantasy", "Sci-fi", "alpha", "Zeta");
        assertThat(dialog.names(false)).containsExactly("..", "Expanse", "Leviathan");
    }

    @Test
    void aPaneInAFolderShowsItsPath() {
        OrganiseDialog dialog = dialog(null, expanse);

        assertThat(dialog.path(false)).isEqualTo("Sci-fi/Expanse");
        assertThat(dialog.names(false)).containsExactly("..", "Caliban");
    }

    @Test
    void aFolderIsOpenedAndLeftThroughTheEntryAbove() {
        OrganiseDialog dialog = dialog(null, null);

        dialog.open(true, "Sci-fi");
        assertThat(dialog.names(true)).containsExactly("..", "Expanse", "Leviathan");
        assertThat(dialog.names(false)).as("the other pane stays").contains("Fantasy");

        dialog.open(true, "Expanse");
        assertThat(dialog.path(true)).isEqualTo("Sci-fi/Expanse");

        dialog.open(true, "..");
        assertThat(dialog.path(true)).isEqualTo("Sci-fi");
        dialog.up(true);
        assertThat(dialog.names(true)).contains("Fantasy");
    }

    @Test
    void aBookIsNotOpened() {
        OrganiseDialog dialog = dialog(null, null);

        dialog.open(true, "alpha");

        assertThat(dialog.names(true)).contains("alpha", "Fantasy");
    }

    @Test
    void goingUpFromTheTopLevelDoesNothing() {
        OrganiseDialog dialog = dialog(null, null);

        dialog.up(true);

        assertThat(dialog.names(true)).containsExactly("Fantasy", "Sci-fi", "alpha", "Zeta");
    }

    @Test
    void aPaneInAFolderThatIsGoneStartsAtTheTopLevel() throws Exception {
        Folder gone = folderService.create("Gone", null);
        folderService.delete(gone, false);

        OrganiseDialog dialog = dialog(gone, null);

        assertThat(dialog.names(true)).contains("Fantasy");
    }
}
