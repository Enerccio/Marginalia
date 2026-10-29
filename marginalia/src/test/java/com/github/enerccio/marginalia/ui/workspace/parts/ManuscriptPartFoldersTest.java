package com.github.enerccio.marginalia.ui.workspace.parts;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.ComponentTree;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.widgets.FolderBookRow;
import com.google.gson.JsonObject;
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
 * The list of books with folders: what a folder shows, going into one, the way back, the books of everything below, and
 * the folder kept in the workspace state.
 */
class ManuscriptPartFoldersTest extends MarginaliaTestBase {

    @Autowired
    private FolderService folderService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private Localization loc;

    // UI.setCurrent keeps only a weak reference
    private UI ui;
    private ManuscriptPart part;
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

        part = new ManuscriptPart(null);
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

    @SuppressWarnings("unchecked")
    private void clickName(FolderBookRow row) {
        Component cell = ((ComponentRenderer<Component, FolderBookRow>) grid().getColumns().getFirst().getRenderer()).createComponent(row);
        Button name = (Button) cell;
        ComponentUtil.fireEvent(name, new ClickEvent<>(name));
    }

    private FolderBookRow row(String name) {
        return rows().stream().filter(row -> row.getName().equals(name)).findFirst().orElseThrow();
    }

    private void click(String text) {
        Button button = ComponentTree.button(view, text);
        ComponentUtil.fireEvent(button, new ClickEvent<>(button));
    }

    @Test
    void theTopLevelListsTheFoldersAboveTheBooks() {
        assertThat(names()).containsExactly("[Sci-fi]", "Loose");
    }

    @Test
    void aFolderShowsHowManyBooksAreBeneathIt() {
        assertThat(row("Sci-fi").getBooks()).isEqualTo(2);
    }

    @Test
    void aFolderIsOpenedFromItsNameAndLeftFromTheBreadcrumb() {
        clickName(row("Sci-fi"));
        assertThat(names()).containsExactly("[Expanse]", "Leviathan");
        assertThat(ComponentTree.all(view, Button.class).map(Button::getText)).contains(loc.getValue(L.LABEL_BOOKS));

        clickName(row("Expanse"));
        assertThat(names()).containsExactly("Caliban");

        click("Sci-fi");
        assertThat(names()).containsExactly("[Expanse]", "Leviathan");

        click(loc.getValue(L.LABEL_BOOKS));
        assertThat(names()).containsExactly("[Sci-fi]", "Loose");
    }

    @Test
    void includingSubfoldersListsEverythingBelowWithItsFolder() {
        Checkbox include = ComponentTree.all(view, Checkbox.class).findFirst().orElseThrow();
        include.setValue(true);

        assertThat(names()).containsExactlyInAnyOrder("[Sci-fi]", "[Expanse]", "Loose", "Leviathan", "Caliban");
        assertThat(row("Caliban").getPath()).isEqualTo("Sci-fi/Expanse");
        assertThat(row("Leviathan").getPath()).isEqualTo("Sci-fi");
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
    void aBookShowsTheSizeOfItsActiveBranch() throws Exception {
        ChatMessage root = new ChatMessage();
        root.setResponse("one two three");
        root.setWordCount(3);
        root.setTokenCount(5);
        root = chatMessageService.createRoot(loose, root);
        Manuscript stored = manuscriptService.find(loose.getId());
        stored.setActiveLeaf(root);
        manuscriptService.save(stored);

        part.refresh();

        assertThat(row("Loose").getSize().words()).isEqualTo(3);
        assertThat(row("Loose").getSize().tokens()).isEqualTo(5);
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
        clickName(row("Sci-fi"));
        folderService.deleteWithContent(folderService.find(scifi.getId()));

        part.refresh();

        assertThat(names()).containsExactly("Loose");
        assertThat(part.storeState().has("folder")).isFalse();
    }

    @Test
    @SuppressWarnings("unchecked")
    void theFavoritesAreAVirtualFolderOfTheTopLevel() throws Exception {
        assertThat(names()).as("no favorite, no folder").containsExactly("[Sci-fi]", "Loose");

        manuscriptService.setFavorite(inExpanse, true);
        manuscriptService.setFavorite(loose, true);
        part.refresh();

        List<FolderBookRow> top = rows();
        assertThat(top.getFirst().isFavorites()).isTrue();
        assertThat(top.getFirst().getBooks()).isEqualTo(2);
        assertThat(top.stream().filter(row -> row.isFavorite()).map(FolderBookRow::getName)).containsExactly("Loose");

        // opened, it lists the favorite books of every folder, with their folder
        Component cell = ((ComponentRenderer<Component, FolderBookRow>) grid().getColumns().getFirst().getRenderer()).createComponent(top.getFirst());
        ComponentUtil.fireEvent((Button) cell, new ClickEvent<>((Button) cell));
        assertThat(names()).containsExactlyInAnyOrder("Caliban", "Loose");
        assertThat(row("Caliban").getPath()).isEqualTo("Sci-fi/Expanse");
        assertThat(part.storeState().get("favorites").getAsBoolean()).isTrue();
        assertThat(((Button) ReflectionTestUtils.getField(part, "newFolderButton")).isEnabled()).isFalse();

        // the filters work in it
        part.restoreState(json("{\"favorites\":true,\"filters\":{\"name\":\"Cal\"}}"));
        assertThat(names()).containsExactly("Caliban");

        // back at the top level the filter is still set: the name starts with Cal, no book there does
        click(loc.getValue(L.LABEL_BOOKS));
        assertThat(names()).isEmpty();
        assertThat(part.storeState().has("favorites")).isFalse();
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

    private static JsonObject json(String text) {
        return com.google.gson.JsonParser.parseString(text).getAsJsonObject();
    }
}
