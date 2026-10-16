package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
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

}
