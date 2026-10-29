package com.github.enerccio.marginalia.ui.workspace.phone;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.ComponentTree;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.widgets.FolderBookRow;
import com.github.enerccio.marginalia.ui.workspace.phone.parts.PhoneManuscriptPart;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The phone list of books with folders: the folders above the books, going into one and back, the books of everything
 * below, the filters in the folder, the favorites, the quick move button and the folder kept in the workspace state.
 */
class PhoneManuscriptPartFoldersTest extends MarginaliaTestBase {

    @Autowired
    private FolderService folderService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private Localization loc;

    // UI.setCurrent keeps only a weak reference
    private UI ui;
    private PhoneManuscriptPart part;
    private Component view;
    private Folder scifi;
    private Folder expanse;
    private Manuscript loose;
    private Manuscript inScifi;
    private Manuscript inExpanse;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        scifi = folderService.create("Sci-fi", null);
        expanse = folderService.create("Expanse", scifi);
        loose = book("Loose", null);
        inScifi = book("Leviathan", scifi);
        inExpanse = book("Caliban", expanse);

        part = new PhoneManuscriptPart(null);
        view = part.create();
    }

    @AfterEach
    void tearDown() {
        UI.setCurrent(null);
    }

    private Manuscript book(String name, Folder folder) throws Exception {
        Manuscript book = new Manuscript();
        book.setName(name);
        book.setFolder(folder);
        return manuscriptService.save(book);
    }

    @SuppressWarnings("unchecked")
    private Grid<FolderBookRow> grid() {
        return (Grid<FolderBookRow>) ReflectionTestUtils.getField(part, "grid");
    }

    private List<FolderBookRow> rows() {
        return grid().getDataProvider().fetch(new Query<>()).toList();
    }

    private List<String> names() {
        return rows().stream().map(row -> (row.isFolder() ? "[" : "") + row.getName() + (row.isFolder() ? "]" : "")).toList();
    }

    private FolderBookRow row(String name) {
        return rows().stream().filter(row -> row.getName().equals(name)).findFirst().orElseThrow();
    }

    @SuppressWarnings("unchecked")
    private Component cell(FolderBookRow row) {
        return ((ComponentRenderer<Component, FolderBookRow>) grid().getColumns().getFirst().getRenderer()).createComponent(row);
    }

    // a folder row is tapped as a whole
    private void tap(FolderBookRow row) {
        Component item = cell(row);
        ComponentUtil.fireEvent(item, new ClickEvent<>(item));
    }

    private void click(String text) {
        Button button = ComponentTree.button(view, text);
        ComponentUtil.fireEvent(button, new ClickEvent<>(button));
    }

    private static Button withTooltip(Component root, String tooltip) {
        return ComponentTree.all(root, Button.class)
                .filter(button -> button.getTooltip() != null && tooltip.equals(button.getTooltip().getText()))
                .findFirst().orElse(null);
    }

    private static JsonObject json(String text) {
        return JsonParser.parseString(text).getAsJsonObject();
    }

    @Test
    void theTopLevelListsTheFoldersAboveTheBooks() {
        assertThat(names()).containsExactly("[Sci-fi]", "Loose");
        assertThat(row("Sci-fi").getBooks()).isEqualTo(2);
    }

    @Test
    void aFolderIsOpenedByTappingItAndLeftFromTheBreadcrumb() {
        tap(row("Sci-fi"));
        assertThat(names()).containsExactly("[Expanse]", "Leviathan");
        assertThat(ComponentTree.all(view, Button.class).map(Button::getText)).contains(loc.getValue(L.LABEL_BOOKS));

        tap(row("Expanse"));
        assertThat(names()).containsExactly("Caliban");

        click("Sci-fi");
        assertThat(names()).containsExactly("[Expanse]", "Leviathan");

        click(loc.getValue(L.LABEL_BOOKS));
        assertThat(names()).containsExactly("[Sci-fi]", "Loose");
    }

    @Test
    void includingSubfoldersListsEverythingBelowWithItsFolder() {
        ComponentTree.all(view, Checkbox.class).findFirst().orElseThrow().setValue(true);

        assertThat(names()).containsExactlyInAnyOrder("[Sci-fi]", "[Expanse]", "Loose", "Leviathan", "Caliban");
        assertThat(row("Caliban").getPath()).isEqualTo("Sci-fi/Expanse");
        assertThat(row("Loose").getPath()).isEmpty();
    }

    @Test
    void theNameFilterSearchesTheCurrentFolderOnly() {
        part.restoreState(json("{\"filters\":{\"name\":\"Cal\"}}"));
        assertThat(names()).isEmpty();

        part.restoreState(json("{\"filters\":{\"name\":\"Cal\"},\"subfolders\":true}"));
        assertThat(names()).containsExactly("Caliban");
    }

    @Test
    void theFolderIsKeptInTheWorkspaceState() {
        part.restoreState(json("{\"folder\":\"" + scifi.getUuid() + "\",\"subfolders\":true}"));

        assertThat(names()).containsExactlyInAnyOrder("[Expanse]", "Leviathan", "Caliban");
        JsonObject state = part.storeState();
        assertThat(state.get("folder").getAsString()).isEqualTo(scifi.getUuid());
        assertThat(state.get("subfolders").getAsBoolean()).isTrue();
    }

    @Test
    void aFolderThatIsGoneIsTheTopLevel() throws Exception {
        Folder gone = folderService.create("Gone", null);
        folderService.delete(gone, false);

        part.restoreState(json("{\"folder\":\"" + gone.getUuid() + "\"}"));

        assertThat(names()).containsExactly("[Sci-fi]", "Loose");
        assertThat(part.storeState().has("folder")).isFalse();
    }

    @Test
    void aFolderDeletedWhileOpenSendsTheListToTheTopLevel() throws Exception {
        tap(row("Sci-fi"));
        folderService.deleteWithContent(folderService.find(scifi.getId()));

        part.refresh();

        assertThat(names()).containsExactly("Loose");
        assertThat(part.storeState().has("folder")).isFalse();
    }

    @Test
    void aBookRowHasTheStarAndTheQuickMoveButtons() {
        Component item = cell(row("Loose"));

        assertThat(withTooltip(item, loc.getValue(L.LABEL_FAVORITES))).isNotNull();
        assertThat(withTooltip(item, loc.getValue(L.LABEL_MOVE_TO_FOLDER))).isNotNull();
    }

    @Test
    void aFolderRowHasNoButtonsToEditIt() {
        assertThat(ComponentTree.all(cell(row("Sci-fi")), Button.class)).isEmpty();
    }

    @Test
    void theStarMakesAFavoriteThatTheListOffersAsAFolder() throws Exception {
        assertThat(names()).as("no favorite, no folder").containsExactly("[Sci-fi]", "Loose");

        ComponentUtil.fireEvent(withTooltip(cell(row("Loose")), loc.getValue(L.LABEL_FAVORITES)),
                new ClickEvent<>(withTooltip(cell(row("Loose")), loc.getValue(L.LABEL_FAVORITES))));
        assertThat(manuscriptService.getFavorites()).containsExactly(loose.getUuid());

        manuscriptService.setFavorite(inExpanse, true);
        part.refresh();
        assertThat(rows().getFirst().isFavorites()).isTrue();
        assertThat(rows().getFirst().getBooks()).isEqualTo(2);

        tap(rows().getFirst());
        assertThat(names()).containsExactlyInAnyOrder("Caliban", "Loose");
        assertThat(row("Caliban").getPath()).isEqualTo("Sci-fi/Expanse");
        assertThat(part.storeState().get("favorites").getAsBoolean()).isTrue();
        assertThat(ComponentTree.button(view, loc.getValue(L.LABEL_ADD_BOOK)).isEnabled()).as("no book is added to the favorites").isFalse();

        click(loc.getValue(L.LABEL_BOOKS));
        assertThat(names()).contains("Loose");
        assertThat(part.storeState().has("favorites")).isFalse();
        assertThat(ComponentTree.button(view, loc.getValue(L.LABEL_ADD_BOOK)).isEnabled()).isTrue();
    }

    @Test
    void theFavoritesViewEndsWhenTheLastFavoriteGoes() throws Exception {
        manuscriptService.setFavorite(loose, true);
        part.restoreState(json("{\"favorites\":true}"));
        assertThat(names()).containsExactly("Loose");

        manuscriptService.setFavorite(loose, false);
        part.refresh();

        assertThat(names()).containsExactly("[Sci-fi]", "Loose");
        assertThat(part.storeState().has("favorites")).isFalse();
    }

    @Test
    void aMovedBookShowsInItsNewFolder() throws Exception {
        manuscriptService.moveToFolder(List.of(loose), expanse);
        part.restoreState(json("{\"folder\":\"" + expanse.getUuid() + "\"}"));

        assertThat(names()).containsExactlyInAnyOrder("Caliban", "Loose");
    }
}
