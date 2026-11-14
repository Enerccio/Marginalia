package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The read only lorebook preview of the story: which rows it has with and without the sub lorebooks, and what a
 * refresh keeps.
 */
class LorebookPreviewTest extends MarginaliaTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    // UI.setCurrent keeps a weak reference only
    private UI ui;

    @BeforeEach
    void currentUI() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private Lorebook lorebook(String name, Lorebook... subbooks) throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName(name);
        lorebook.setSubbooks(new ArrayList<>(List.of(subbooks)));
        return lorebookService.save(lorebook);
    }

    private LorebookEntry entry(Lorebook lorebook, String name, int order) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        entry.setOrder(order);
        entry.setPayload("payload of " + name);
        return lorebookEntryService.save(entry);
    }

    private static <T extends Component> T find(Component root, Class<T> type) {
        if (type.isInstance(root)) {
            return type.cast(root);
        }
        for (Component child : (Iterable<Component>) root.getChildren()::iterator) {
            T found = find(child, type);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static TreeData<LorebookPreview.Row> data(LorebookPreview preview) {
        TreeGrid<LorebookPreview.Row> grid = find(preview, TreeGrid.class);
        return ((TreeDataProvider<LorebookPreview.Row>) grid.getDataProvider()).getTreeData();
    }

    private static String label(LorebookPreview.Row row) {
        return row.entry() == null ? "[" + row.book().getName() + "]" : row.entry().getName();
    }

    private static List<String> roots(LorebookPreview preview) {
        return data(preview).getRootItems().stream().map(LorebookPreviewTest::label).toList();
    }

    private static List<String> children(LorebookPreview preview, String book) {
        TreeData<LorebookPreview.Row> data = data(preview);
        Optional<LorebookPreview.Row> row = data.getRootItems().stream().filter(r -> label(r).equals("[" + book + "]")).findFirst();
        return data.getChildren(row.orElseThrow()).stream().map(LorebookPreviewTest::label).toList();
    }

    private static void includeSubbooks(LorebookPreview preview, boolean include) {
        find(preview, Checkbox.class).setValue(include);
    }

    @Test
    void withoutSubbooksTheEntriesOfTheLorebookAreTheTopLevel() throws Exception {
        Lorebook sub = lorebook("sub");
        entry(sub, "Hidden", 1);
        Lorebook main = lorebook("main", sub);
        entry(main, "Second", 20);
        entry(main, "First", 10);

        LorebookPreview preview = new LorebookPreview().create();
        preview.refresh(main);

        assertThat(roots(preview)).containsExactly("First", "Second");
    }

    @Test
    void withSubbooksEveryLorebookIsATopLevelRowWithItsEntries() throws Exception {
        Lorebook deep = lorebook("deep");
        entry(deep, "Deep entry", 1);
        Lorebook sub = lorebook("sub", deep);
        entry(sub, "Sub entry", 1);
        Lorebook other = lorebook("other");
        Lorebook main = lorebook("main", sub, other);
        entry(main, "Main entry", 1);

        LorebookPreview preview = new LorebookPreview().create();
        preview.refresh(main);
        includeSubbooks(preview, true);
        preview.refresh(main);

        assertThat(roots(preview)).containsExactly("[main]", "[sub]", "[other]", "[deep]");
        assertThat(children(preview, "main")).containsExactly("Main entry");
        assertThat(children(preview, "sub")).containsExactly("Sub entry");
        assertThat(children(preview, "other")).isEmpty();
        assertThat(children(preview, "deep")).containsExactly("Deep entry");
    }

    @Test
    void aLorebookThatIsItsOwnSubbookIsShownOnce() throws Exception {
        Lorebook a = lorebook("a");
        Lorebook b = lorebook("b", a);
        a.setSubbooks(new ArrayList<>(List.of(b)));
        a = lorebookService.save(a);
        entry(a, "In a", 1);

        LorebookPreview preview = new LorebookPreview().create();
        includeSubbooks(preview, true);
        preview.refresh(a);

        assertThat(roots(preview)).containsExactly("[a]", "[b]");
    }

    @Test
    void withoutLorebookNothingIsShownButTheHint() {
        LorebookPreview preview = new LorebookPreview().create();
        preview.refresh(null);

        TreeGrid<?> grid = find(preview, TreeGrid.class);
        assertThat(grid.isVisible()).isFalse();
        assertThat(find(preview, Checkbox.class).isVisible()).isFalse();
    }

    @Test
    void refreshShowsChangesAndKeepsOpenedEntries() throws Exception {
        Lorebook main = lorebook("main");
        entry(main, "One", 1);

        LorebookPreview preview = new LorebookPreview().create();
        preview.refresh(main);
        TreeGrid<LorebookPreview.Row> grid = find(preview, TreeGrid.class);
        LorebookPreview.Row row = data(preview).getRootItems().getFirst();
        assertThat(preview.toggleDetails(row)).isTrue();

        entry(main, "Two", 2);
        preview.refresh(main);

        assertThat(roots(preview)).containsExactly("One", "Two");
        assertThat(grid.isDetailsVisible(data(preview).getRootItems().getFirst())).isTrue();
        assertThat(grid.isDetailsVisible(data(preview).getRootItems().getLast())).isFalse();
    }

    @Test
    void theSearchBoxLimitsTheEntries() throws Exception {
        Lorebook main = lorebook("main");
        LorebookEntry varro = entry(main, "Varro", 10);
        varro.setFiltering("captain");
        lorebookEntryService.save(varro);
        entry(main, "Ostra", 20);

        LorebookPreview preview = new LorebookPreview().create();
        preview.refresh(main);
        assertThat(roots(preview)).containsExactly("Varro", "Ostra");

        // typed in the browser: the listener acts on changes from the client only
        TextField search = preview.getSearchField();
        search.setValue("CAPTAIN");
        ComponentUtil.fireEvent(search, new AbstractField.ComponentValueChangeEvent<>(search, search, "", true));
        assertThat(roots(preview)).as("name, content, note or filter, any case").containsExactly("Varro");

        search.setValue("payload of ostra");
        ComponentUtil.fireEvent(search, new AbstractField.ComponentValueChangeEvent<>(search, search, "", true));
        assertThat(roots(preview)).containsExactly("Ostra");
    }

    @SuppressWarnings("unchecked")
    private static long buttonsOfTheFirstRow(LorebookPreview preview) {
        TreeGrid<LorebookPreview.Row> grid = find(preview, TreeGrid.class);
        ComponentRenderer<Component, LorebookPreview.Row> renderer =
                (ComponentRenderer<Component, LorebookPreview.Row>) grid.getColumns().getFirst().getRenderer();
        Component row = renderer.createComponent(data(preview).getRootItems().getFirst());
        return java.util.stream.Stream.of(row).flatMap(LorebookPreviewTest::all).filter(Button.class::isInstance).count();
    }

    private static java.util.stream.Stream<Component> all(Component root) {
        return java.util.stream.Stream.concat(java.util.stream.Stream.of(root), root.getChildren().flatMap(LorebookPreviewTest::all));
    }

    @Test
    void anEntryHasAnEditButtonOnlyWhenThePreviewIsEditable() throws Exception {
        Lorebook main = lorebook("main");
        entry(main, "One", 1);

        LorebookPreview preview = new LorebookPreview().create();
        preview.refresh(main);
        assertThat(buttonsOfTheFirstRow(preview)).as("only the one that opens the details").isEqualTo(1);

        preview.setEditable(true);
        assertThat(buttonsOfTheFirstRow(preview)).as("and the edit button").isEqualTo(2);
    }

    @Test
    void anEditablePreviewOnADesktopOpensTheFullScreenEditor() throws Exception {
        Lorebook main = lorebook("main");
        entry(main, "One", 1);
        LorebookEntry two = entry(main, "Two", 2);

        LorebookPreview preview = new LorebookPreview().create();
        preview.setEditable(true);
        preview.refresh(main);
        assertThat(preview.getEditorButton().isVisible()).as("not without the editor allowed").isFalse();

        preview.setFullScreenEditor(true);
        preview.refresh(main);
        assertThat(buttonsOfTheFirstRow(preview)).as("open in the editor, edit and the details").isEqualTo(3);
        assertThat(preview.getEditorButton().isVisible()).isTrue();

        java.util.concurrent.Callable<com.github.enerccio.marginalia.domain.model.impl.Manuscript> book = () -> null;
        preview.setBook(book);
        com.github.enerccio.marginalia.ui.dialogs.LorebookEditorDialog dialog = preview.openEditor(main, two);
        assertThat(dialog.getLorebook().getId()).isEqualTo(main.getId());
        // the book of the story goes along (the Lore Generator bills it)
        assertThat(dialog.getBook()).isSameAs(book);
        // which entry it shows is the editor's showEntry, see LorebookEditorDialogTest
        assertThat(dialog.isOpened()).isTrue();
    }

    @Test
    void aPreviewThatIsNotEditableHasNoEditorButtons() throws Exception {
        Lorebook main = lorebook("main");
        entry(main, "One", 1);

        LorebookPreview preview = new LorebookPreview().create();
        preview.setFullScreenEditor(true);
        preview.refresh(main);

        assertThat(buttonsOfTheFirstRow(preview)).isEqualTo(1);
        assertThat(preview.getEditorButton().isVisible()).isFalse();
    }
}
