package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.components.LorebookEntryEditor;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.provider.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The lorebook editor: the list of the entries, the entry tabs (each entry once, Open reuses the last tab), closing them.
 */
class LorebookEditorDialogTest extends MarginaliaTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    // UI.setCurrent keeps a weak reference only
    private UI ui;
    private Lorebook lorebook;

    @BeforeEach
    void currentUI() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        login();
        lorebook = new Lorebook();
        lorebook.setName("World");
        lorebook.setSubbooks(new ArrayList<>());
        lorebook = lorebookService.save(lorebook);
    }

    @AfterEach
    void noCurrentUI() {
        UI.setCurrent(null);
        ui = null;
    }

    private LorebookEntry entry(String name, String payload, boolean enabled) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        entry.setPayload(payload);
        entry.setEnabled(enabled);
        return lorebookEntryService.save(entry);
    }

    private LorebookEditorDialog dialog() {
        LorebookEditorDialog dialog = new LorebookEditorDialog(lorebook, null).create();
        dialog.open();
        return dialog;
    }

    private static List<LorebookEntry> listed(LorebookEditorDialog dialog) {
        return dialog.getGrid().getDataProvider().fetch(new Query<>()).toList();
    }

    private static LorebookEntry listed(LorebookEditorDialog dialog, String name) {
        return listed(dialog).stream().filter(entry -> name.equals(entry.getName())).findFirst().orElseThrow();
    }

    private static List<String> openNames(LorebookEditorDialog dialog) {
        return dialog.getTabs().values().stream().map(editor -> editor.getEntry().getName()).toList();
    }

    @Test
    void theListShowsTheEntriesAndTheTotalOfTheEnabledOnes() throws Exception {
        entry("Varro", "A captain of the watch.", true);
        entry("Lia", "A thief of the lower city, very long description that has many tokens in it.", false);

        LorebookEditorDialog dialog = dialog();

        assertThat(listed(dialog)).extracting(LorebookEntry::getName).containsExactlyInAnyOrder("Varro", "Lia");
        assertThat(dialog.getTotalSpan().getText()).startsWith("~");
        assertThat(dialog.getTabs()).isEmpty();
        assertThat(dialog.getDock().isClosable(LorebookEditorDialog.LIST_TAB)).isFalse();
        assertThat(dialog.getDock().minimize(LorebookEditorDialog.LIST_TAB)).isFalse();
    }

    @Test
    void openReusesTheLastEntryTabAndOpenInNewTabAddsOne() throws Exception {
        entry("Varro", "A captain.", true);
        entry("Lia", "A thief.", true);
        entry("Odo", "A priest.", true);
        LorebookEditorDialog dialog = dialog();

        dialog.openEntry(listed(dialog, "Varro"), false);
        assertThat(openNames(dialog)).containsExactly("Varro");

        // the same tab shows the other entry
        String tab = dialog.getTabs().keySet().iterator().next();
        dialog.openEntry(listed(dialog, "Lia"), false);
        assertThat(openNames(dialog)).containsExactly("Lia");
        assertThat(dialog.getTabs().keySet()).containsExactly(tab);
        assertThat(dialog.getDock().isClosable(tab)).isTrue();

        dialog.openEntry(listed(dialog, "Varro"), true);
        assertThat(openNames(dialog)).containsExactlyInAnyOrder("Lia", "Varro");

        // an entry that has a tab is shown there, not opened again
        dialog.openEntry(listed(dialog, "Lia"), true);
        assertThat(openNames(dialog)).containsExactlyInAnyOrder("Lia", "Varro");

        // Open goes to the tab the user had last: Lia's, shown just now
        dialog.openEntry(listed(dialog, "Odo"), false);
        assertThat(openNames(dialog)).containsExactlyInAnyOrder("Odo", "Varro");
    }

    @Test
    void aClosedTabIsForgotten() throws Exception {
        entry("Varro", "A captain.", true);
        entry("Lia", "A thief.", true);
        LorebookEditorDialog dialog = dialog();
        dialog.openEntry(listed(dialog, "Varro"), false);
        String tab = dialog.getTabs().keySet().iterator().next();

        assertThat(dialog.getDock().close(tab)).isTrue();

        assertThat(dialog.getTabs()).isEmpty();
        dialog.openEntry(listed(dialog, "Lia"), false);
        assertThat(openNames(dialog)).containsExactly("Lia");
        assertThat(dialog.getTabs().keySet()).doesNotContain(tab);
    }

    @Test
    void theTabAndTheListShareTheEntry() throws Exception {
        entry("Varro", "A captain.", true);
        LorebookEditorDialog dialog = dialog();
        LorebookEntry varro = listed(dialog, "Varro");

        dialog.openEntry(varro, false);

        LorebookEntryEditor editor = dialog.getTabs().values().iterator().next();
        assertThat(editor.getEntry()).isSameAs(varro);
    }

    @Test
    void theOpenEntriesAndTheLayoutAreKeptInTheLorebook() throws Exception {
        entry("Varro", "A captain.", true);
        entry("Lia", "A thief.", true);
        entry("Odo", "A priest.", true);
        LorebookEditorDialog dialog = dialog();
        dialog.openEntry(listed(dialog, "Varro"), false);
        dialog.openEntry(listed(dialog, "Lia"), true);
        String liaTab = dialog.getTabs().entrySet().stream()
                .filter(tab -> tab.getValue().getEntry().getName().equals("Lia")).findFirst().orElseThrow().getKey();
        // Lia beside the list, in a column of its own
        dialog.getDock().setLayout(com.google.gson.JsonParser.parseString("{\"columns\":[{\"groups\":[{\"tabs\":[\""
                + LorebookEditorDialog.LIST_TAB + "\",\"entry-1\"]}]},{\"groups\":[{\"tabs\":[\"" + liaTab + "\"]}]}]}")
                .getAsJsonObject());
        dialog.close();

        assertThat(lorebookService.find(lorebook.getId()).getAttributes().has(LorebookEditorDialog.CONFIG_ATTRIBUTE)).isTrue();

        LorebookEditorDialog again = dialog();
        assertThat(openNames(again)).containsExactlyInAnyOrder("Varro", "Lia");
        assertThat(again.getDock().getLayout().getAsJsonArray("columns")).hasSize(2);
        // Lia was used last: Open shows the next entry in its tab
        again.openEntry(listed(again, "Odo"), false);
        assertThat(openNames(again)).containsExactlyInAnyOrder("Varro", "Odo");
        // a new tab gets a number of its own
        again.openEntry(listed(again, "Lia"), true);
        assertThat(again.getTabs()).hasSize(3);
    }

    @Test
    void anEntryThatIsGoneIsNotOpenedAgain() throws Exception {
        LorebookEntry varro = entry("Varro", "A captain.", true);
        entry("Lia", "A thief.", true);
        LorebookEditorDialog dialog = dialog();
        dialog.openEntry(listed(dialog, "Varro"), false);
        dialog.openEntry(listed(dialog, "Lia"), true);
        dialog.close();

        lorebookEntryService.delete(lorebookEntryService.find(varro.getId()), false);

        assertThat(openNames(dialog())).containsExactly("Lia");
    }

    @Test
    void savingTheArrangementKeepsWhatWasChangedElsewhere() throws Exception {
        entry("Varro", "A captain.", true);
        LorebookEditorDialog dialog = dialog();
        Lorebook elsewhere = lorebookService.find(lorebook.getId());
        elsewhere.setName("The World");
        lorebookService.save(elsewhere);

        dialog.openEntry(listed(dialog, "Varro"), false);

        Lorebook saved = lorebookService.find(lorebook.getId());
        assertThat(saved.getName()).isEqualTo("The World");
        assertThat(saved.getAttributes().has(LorebookEditorDialog.CONFIG_ATTRIBUTE)).isTrue();
    }

    @Test
    void anEntryChangedElsewhereIsShownAgainInItsTabAndInTheList() throws Exception {
        LorebookEntry varro = entry("Varro", "A captain.", true);
        entry("Lia", "A thief.", true);
        LorebookEditorDialog dialog = dialog();
        dialog.openEntry(listed(dialog, "Varro"), false);
        // a generator or a revision saves the entries
        for (String name : List.of("Varro", "Lia")) {
            LorebookEntry other = lorebookEntryService.find(listed(dialog, name).getId());
            other.setPayload("Rewritten " + name + ".");
            lorebookEntryService.save(other);
        }

        dialog.reloadEntry(listed(dialog, "Varro"));
        dialog.reloadEntry(listed(dialog, "Lia"));

        LorebookEntryEditor editor = dialog.getTabs().values().iterator().next();
        assertThat(editor.getEntry().getPayload()).isEqualTo("Rewritten Varro.");
        // the list and the tab still share the entry
        assertThat(listed(dialog, "Varro")).isSameAs(editor.getEntry());
        assertThat(listed(dialog, "Lia").getPayload()).isEqualTo("Rewritten Lia.");
        assertThat(editor.getLorebook().getId()).isEqualTo(lorebook.getId());
        assertThat(varro.getId()).isEqualTo(editor.getEntry().getId());
    }

    @Test
    void theTabReloadingItselfKeepsTheListOnTheSameEntry() throws Exception {
        entry("Varro", "A captain.", true);
        LorebookEditorDialog dialog = dialog();
        dialog.openEntry(listed(dialog, "Varro"), false);
        LorebookEntryEditor editor = dialog.getTabs().values().iterator().next();
        LorebookEntry other = lorebookEntryService.find(editor.getEntry().getId());
        other.setName("Captain Varro");
        lorebookEntryService.save(other);

        editor.reloadFromDatabase();

        assertThat(listed(dialog)).extracting(LorebookEntry::getName).containsExactly("Captain Varro");
        assertThat(listed(dialog, "Captain Varro")).isSameAs(editor.getEntry());
    }

    @Test
    void theEditorKnowsTheViewItWasOpenedFrom() throws Exception {
        com.github.enerccio.marginalia.ui.components.LorebookView view =
                new com.github.enerccio.marginalia.ui.components.LorebookView(true);

        LorebookEditorDialog dialog = new LorebookEditorDialog(lorebook, view, null).create();

        assertThat(dialog.getOrigin()).isSameAs(view);
        assertThat(dialog.getLorebook().getId()).isEqualTo(lorebook.getId());
    }

    @Test
    void theEditorCanBeOpenedOnAnEntry() throws Exception {
        LorebookEntry lia = entry("Lia", "A thief.", true);
        entry("Varro", "A captain.", true);
        LorebookEditorDialog dialog = dialog();

        // the row of the lorebook view has its own object of the entry
        dialog.showEntry(lorebookEntryService.find(lia.getId()));

        assertThat(openNames(dialog)).containsExactly("Lia");
        assertThat(dialog.getTabs().values().iterator().next().getEntry()).isSameAs(listed(dialog, "Lia"));
    }

    @Test
    void closingTellsTheViewItCameFrom() throws Exception {
        AtomicInteger closed = new AtomicInteger();
        LorebookEditorDialog dialog = new LorebookEditorDialog(lorebook, closed::incrementAndGet).create();
        dialog.open();

        dialog.close();

        assertThat(closed).hasValue(1);
    }

    @Test
    void entriesWrittenByAnotherWindowAreShownInTheirTabsAndInTheList() throws Exception {
        entry("Varro", "A captain.", true);
        entry("Lia", "A thief.", true);
        LorebookEditorDialog dialog = dialog();
        dialog.openEntry(listed(dialog, "Varro"), false);
        LorebookEntryEditor editor = dialog.getTabs().values().iterator().next();
        java.util.Set<String> uuids = new java.util.HashSet<>();
        for (String name : List.of("Varro", "Lia")) {
            LorebookEntry other = lorebookEntryService.find(listed(dialog, name).getId());
            other.setPayload("Rewritten " + name + ".");
            uuids.add(lorebookEntryService.save(other).getUuid());
        }
        uuids.add(entry("Cassia", "A spy.", true).getUuid());

        dialog.loreChanged(java.util.Set.of(lorebook.getId()), uuids, false);

        assertThat(editor.getEntry().getPayload()).isEqualTo("Rewritten Varro.");
        assertThat(listed(dialog, "Varro")).isSameAs(editor.getEntry());
        assertThat(listed(dialog, "Lia").getPayload()).isEqualTo("Rewritten Lia.");
        assertThat(listed(dialog)).extracting(LorebookEntry::getName).contains("Cassia");
    }

    @Test
    void theChangesOfAnotherLorebookAreLeftAlone() throws Exception {
        entry("Varro", "A captain.", true);
        LorebookEditorDialog dialog = dialog();
        entry("Lia", "A thief.", true);

        dialog.loreChanged(java.util.Set.of(lorebook.getId() + 1000), java.util.Set.of("another"), false);

        assertThat(listed(dialog)).extracting(LorebookEntry::getName).containsExactly("Varro");
    }

    @Test
    void theTabOfAnEntryDeletedByAnotherWindowCloses() throws Exception {
        entry("Varro", "A captain.", true);
        LorebookEditorDialog dialog = dialog();
        dialog.openEntry(listed(dialog, "Varro"), false);
        LorebookEntry varro = lorebookEntryService.find(listed(dialog, "Varro").getId());
        lorebookEntryService.delete(varro, false);

        dialog.loreChanged(java.util.Set.of(lorebook.getId()), java.util.Set.of(varro.getUuid()), false);

        assertThat(dialog.getTabs()).isEmpty();
        assertThat(listed(dialog)).isEmpty();
    }

    private static com.github.enerccio.marginalia.ui.widgets.MarkdownEditor markdownOf(com.vaadin.flow.component.Component root) {
        if (root instanceof com.github.enerccio.marginalia.ui.widgets.MarkdownEditor editor) {
            return editor;
        }
        return root.getChildren().map(LorebookEditorDialogTest::markdownOf).filter(java.util.Objects::nonNull).findFirst()
                .orElse(null);
    }

    @Test
    void everyTabKeepsTheModeOfItsMarkdownEditor() throws Exception {
        entry("Varro", "A captain.", true);
        entry("Lia", "A thief.", true);
        LorebookEditorDialog dialog = dialog();
        dialog.openEntry(listed(dialog, "Varro"), false);
        LorebookEntryEditor editor = dialog.getTabs().values().iterator().next();
        // the user picks the preview with the button of the editor
        markdownOf(editor).getElement().setPropertyJson("preview",
                (tools.jackson.databind.node.BaseJsonNode) tools.jackson.databind.json.JsonMapper.builder().build()
                        .valueToTree("preview"));
        assertThat(editor.getEditorMode()).isEqualTo(com.github.enerccio.marginalia.ui.widgets.MarkdownEditor.Mode.PREVIEW);

        // the tab shows another entry in the same mode
        dialog.openEntry(listed(dialog, "Lia"), false);
        LorebookEntryEditor lia = dialog.getTabs().values().iterator().next();
        assertThat(lia.getEditorMode()).isEqualTo(com.github.enerccio.marginalia.ui.widgets.MarkdownEditor.Mode.PREVIEW);
        dialog.close();

        LorebookEditorDialog again = dialog();
        assertThat(again.getTabs().values().iterator().next().getEditorMode())
                .isEqualTo(com.github.enerccio.marginalia.ui.widgets.MarkdownEditor.Mode.PREVIEW);
        assertThat(markdownOf(again.getTabs().values().iterator().next()).getMode())
                .isEqualTo(com.github.enerccio.marginalia.ui.widgets.MarkdownEditor.Mode.PREVIEW);
    }

    @Test
    void theFirstEntryTabOpensInAColumnRightOfTheListAndTheNextOnesBesideIt() throws Exception {
        entry("Varro", "A captain.", true);
        entry("Lia", "A thief.", true);
        LorebookEditorDialog dialog = dialog();

        dialog.openEntry(listed(dialog, "Varro"), false);
        com.google.gson.JsonArray columns = dialog.getDock().getLayout().getAsJsonArray("columns");
        assertThat(columns).hasSize(2);
        assertThat(columns.get(0).toString()).contains(LorebookEditorDialog.LIST_TAB).doesNotContain("entry-");
        assertThat(columns.get(1).toString()).contains("entry-1");

        dialog.openEntry(listed(dialog, "Lia"), true);
        columns = dialog.getDock().getLayout().getAsJsonArray("columns");
        assertThat(columns).hasSize(2);
        assertThat(columns.get(1).getAsJsonObject().getAsJsonArray("groups")).hasSize(1);
        assertThat(columns.get(1).toString()).contains("entry-1", "entry-2");
    }

    private Lorebook withSubbook(String name) throws Exception {
        Lorebook sub = new Lorebook();
        sub.setName(name);
        sub.setSubbooks(new ArrayList<>());
        sub = lorebookService.save(sub);
        Lorebook main = lorebookService.find(lorebook.getId());
        main.setSubbooks(new ArrayList<>(List.of(sub)));
        lorebook = lorebookService.save(main);
        return sub;
    }

    private LorebookEntry entryIn(Lorebook book, String name) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(book);
        entry.setName(name);
        entry.setPayload("About " + name + ".");
        entry.setEnabled(true);
        return lorebookEntryService.save(entry);
    }

    @Test
    void theEntriesOfTheSubLorebooksAreListedOnRequest() throws Exception {
        entry("Varro", "A captain.", true);
        Lorebook city = withSubbook("City");
        entryIn(city, "Harbour");
        LorebookEditorDialog dialog = dialog();
        assertThat(listed(dialog)).extracting(LorebookEntry::getName).containsExactly("Varro");

        dialog.getIncludeSubbooks().setValue(true);
        com.vaadin.flow.component.ComponentUtil.fireEvent(dialog.getIncludeSubbooks(),
                new com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent<>(dialog.getIncludeSubbooks(),
                        dialog.getIncludeSubbooks(), false, true));

        assertThat(listed(dialog)).extracting(LorebookEntry::getName).containsExactlyInAnyOrder("Varro", "Harbour");
        LorebookEntry harbour = listed(dialog, "Harbour");
        assertThat(dialog.lorebookOf(harbour).getId()).isEqualTo(city.getId());
        dialog.openEntry(harbour, false);
        LorebookEntryEditor editor = dialog.getTabs().values().iterator().next();
        // the tab works on the entry's own lorebook
        assertThat(editor.getLorebook().getId()).isEqualTo(city.getId());

        // a new entry goes to the lorebook picked for it
        dialog.getTargetBook().setValue(dialog.getTargetBook().getListDataView().getItems()
                .filter(book -> book.getId().equals(city.getId())).findFirst().orElseThrow());
        dialog.createEntry();
        assertThat(lorebookEntryService.getEntriesForLorebook(city.getId())).hasSize(2);

        // the choice is kept with the layout
        dialog.close();
        LorebookEditorDialog again = dialog();
        assertThat(again.getIncludeSubbooks().getValue()).isTrue();
        assertThat(listed(again)).hasSize(3);
    }
}
