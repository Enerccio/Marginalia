package com.github.enerccio.marginalia.ui.components;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.service.TagService;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The list of the entries in the lorebook views: the search box that shows only some of them, the selection that the
 * button deleting many entries counts, and the scan depth of the lorebook on the phone.
 */
class LorebookEntryListTest extends AgentTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService entryService;

    @Autowired
    private Localization loc;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    // UI.setCurrent keeps a weak reference only
    private UI ui;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        loginAgentUser();
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private static Stream<Component> all(Component root) {
        return Stream.concat(Stream.of(root), root.getChildren().flatMap(LorebookEntryListTest::all));
    }

    private Lorebook book() throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName("World");
        lorebook.setSubbooks(new ArrayList<>());
        lorebook = lorebookService.save(lorebook);
        entry(lorebook, "Varro", "A captain of the watch.", "varro", "Rumours say he drinks.");
        entry(lorebook, "Ostra", "A harbor city.", "ostra", null);
        entry(lorebook, "Mages", "A guild. Varro distrusts it.", null, null);
        return lorebook;
    }

    private void entry(Lorebook lorebook, String name, String payload, String filter, String comment) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        entry.setPayload(payload);
        entry.setFiltering(filter);
        entry.setComment(comment);
        entryService.save(entry);
    }

    @SuppressWarnings("unchecked")
    private static Grid<LorebookEntry> grid(Component view) {
        return (Grid<LorebookEntry>) all(view).filter(Grid.class::isInstance).findFirst().orElseThrow();
    }

    private static List<String> shown(Grid<LorebookEntry> grid) {
        return grid.getGenericDataView().getItems().map(LorebookEntry::getName).sorted().toList();
    }

    private TextField searchBox(Component view) {
        return all(view).filter(TextField.class::isInstance).map(TextField.class::cast)
                .filter(field -> loc.getValue(L.LABEL_SEARCH).equals(field.getPlaceholder())).findFirst().orElseThrow();
    }

    @Test
    void theSearchBoxShowsTheEntriesWithTheTextInTheNameContentNoteOrFilter() throws Exception {
        LorebookView view = new LorebookView(book(), true);
        view.create();
        Grid<LorebookEntry> grid = grid(view);
        TextField search = searchBox(view);
        assertThat(shown(grid)).containsExactly("Mages", "Ostra", "Varro");

        search.setValue("VARRO");
        assertThat(shown(grid)).as("name, filter and content, any case").containsExactly("Mages", "Varro");

        search.setValue("harbor");
        assertThat(shown(grid)).as("content").containsExactly("Ostra");

        search.setValue("rumours");
        assertThat(shown(grid)).as("note").containsExactly("Varro");

        search.setValue("nothing like it");
        assertThat(shown(grid)).isEmpty();

        search.clear();
        assertThat(shown(grid)).containsExactly("Mages", "Ostra", "Varro");
    }

    @Test
    void theSearchSurvivesRefreshingTheEntries() throws Exception {
        LorebookView view = new LorebookView(book(), true);
        view.create();
        searchBox(view).setValue("ostra");

        view.refreshEntries();

        assertThat(shown(grid(view))).containsExactly("Ostra");
    }

    @Test
    void theDeleteButtonAppearsWithTheSelectionAndCountsIt() throws Exception {
        LorebookView view = new LorebookView(book(), true);
        view.create();
        Grid<LorebookEntry> grid = grid(view);
        Button delete = all(view).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(b -> b.getText() != null && b.getText().startsWith(loc.getValue(L.LABEL_DELETE_SELECTED)))
                .findFirst().orElseThrow();
        assertThat(delete.isVisible()).isFalse();

        grid.getGenericDataView().getItems().limit(2).forEach(grid::select);

        assertThat(delete.isVisible()).isTrue();
        assertThat(delete.getText()).endsWith("(2)");

        grid.deselectAll();

        assertThat(delete.isVisible()).isFalse();
    }

    @Test
    void thePhoneViewSearchesAndShowsTheScanDepth() throws Exception {
        Lorebook lorebook = book();
        lorebook.setScanDepth(2);
        lorebook = lorebookService.save(lorebook);

        PhoneLorebookView view = new PhoneLorebookView(lorebook);
        view.create();

        searchBox(view).setValue("guild");
        assertThat(shown(grid(view))).containsExactly("Mages");
        assertThat(all(view).filter(Span.class::isInstance).map(Span.class::cast).map(Span::getText))
                .contains(loc.getValue(L.LABEL_SCAN_DEPTH) + ": 2");
    }

    private void tag(Lorebook lorebook, String entryName, String value) throws Exception {
        Tag tag = tagService.getOrCreateForUser(value);
        LorebookEntry entry = entryService.getEntriesForLorebook(lorebook).stream().filter(e -> e.getName().equals(entryName))
                .findFirst().orElseThrow();
        tagRelationService.createRelation(tag, entry);
    }

    @SuppressWarnings("unchecked")
    private <T> ComboBox<T> comboBox(Component view, L placeholder) {
        return (ComboBox<T>) all(view).filter(ComboBox.class::isInstance).map(ComboBox.class::cast)
                .filter(box -> loc.getValue(placeholder).equals(box.getPlaceholder())).findFirst().orElseThrow();
    }

    private Lorebook bookWithTagsAndAStatus() throws Exception {
        Lorebook lorebook = book();
        tag(lorebook, "Varro", "people");
        tag(lorebook, "Mages", "people");
        tag(lorebook, "Mages", "guilds");
        LorebookEntry ostra = entryService.getEntriesForLorebook(lorebook).stream().filter(e -> e.getName().equals("Ostra"))
                .findFirst().orElseThrow();
        ostra.setEnabled(false);
        entryService.save(ostra);
        return lorebook;
    }

    @Test
    void theTagAndTheStatusLimitTheEntriesOfTheDesktopView() throws Exception {
        LorebookView view = new LorebookView(bookWithTagsAndAStatus(), true);
        view.create();
        Grid<LorebookEntry> grid = grid(view);
        ComboBox<String> tags = comboBox(view, L.LABEL_TAGS);
        ComboBox<LorebookEntryFilter.Status> status = comboBox(view, L.LABEL_STATUS);
        assertThat(tags.getListDataView().getItems()).as("the tags the entries have").containsExactly("guilds", "people");

        tags.setValue("people");
        assertThat(shown(grid)).containsExactly("Mages", "Varro");

        status.setValue(LorebookEntryFilter.Status.CONSTANT);
        assertThat(shown(grid)).as("tag and status together: enabled and without a filter").containsExactly("Mages");

        tags.clear();
        status.setValue(LorebookEntryFilter.Status.DISABLED);
        assertThat(shown(grid)).containsExactly("Ostra");

        status.setValue(LorebookEntryFilter.Status.ENABLED);
        searchBox(view).setValue("varro");
        assertThat(shown(grid)).as("with the search box too").containsExactly("Mages", "Varro");

        searchBox(view).clear();
        status.setValue(LorebookEntryFilter.Status.ALL);
        assertThat(shown(grid)).containsExactly("Mages", "Ostra", "Varro");
    }

    @Test
    void theTagAndTheStatusLimitTheEntriesOfThePhoneView() throws Exception {
        PhoneLorebookView view = new PhoneLorebookView(bookWithTagsAndAStatus());
        view.create();
        Grid<LorebookEntry> grid = grid(view);

        comboBox(view, L.LABEL_TAGS).setValue("guilds");
        assertThat(shown(grid)).containsExactly("Mages");

        comboBox(view, L.LABEL_TAGS).clear();
        comboBox(view, L.LABEL_STATUS).setValue(LorebookEntryFilter.Status.DISABLED);
        assertThat(shown(grid)).containsExactly("Ostra");
    }

    @Test
    void thePhoneViewDeletesTheCheckedEntries() throws Exception {
        Lorebook lorebook = book();
        PhoneLorebookView view = new PhoneLorebookView(lorebook);
        view.create();
        Grid<LorebookEntry> grid = grid(view);
        Button delete = all(view).filter(Button.class::isInstance).map(Button.class::cast)
                .filter(b -> b.getText() != null && b.getText().startsWith(loc.getValue(L.LABEL_DELETE_SELECTED)))
                .findFirst().orElseThrow();
        assertThat(delete.isVisible()).isFalse();

        // the menu item "Select entries" turns the check boxes on
        view.setSelecting(true);
        grid.getGenericDataView().getItems().limit(2).forEach(grid::select);

        assertThat(delete.isVisible()).isTrue();
        assertThat(delete.getText()).endsWith("(2)");

        view.setSelecting(false);
        assertThat(delete.isVisible()).as("the selection is gone with the check boxes").isFalse();
    }

    @Test
    void aSubLorebookHasAnOwnScanDepthField() throws Exception {
        Lorebook lorebook = book();
        lorebook.setOwnScanDepth(2);
        lorebook = lorebookService.save(lorebook);

        LorebookView view = new LorebookView(lorebook, true);
        view.create();

        assertThat(all(view).filter(com.vaadin.flow.component.textfield.IntegerField.class::isInstance)
                .map(com.vaadin.flow.component.textfield.IntegerField.class::cast)
                .filter(f -> loc.getValue(L.LABEL_OWN_SCAN_DEPTH).equals(f.getLabel()))
                .map(com.vaadin.flow.component.textfield.IntegerField::getValue)).containsExactly(2);
    }
}
