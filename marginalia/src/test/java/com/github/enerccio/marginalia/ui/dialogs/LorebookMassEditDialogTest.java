package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.collections.ActivationContext;
import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.model.impl.LorebookEntry;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.LorebookEntryService;
import com.github.enerccio.marginalia.domain.service.LorebookLockedException;
import com.github.enerccio.marginalia.domain.service.LorebookService;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.vaadin.flow.component.*;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The mass edit of a lorebook: the entries of the lorebook and of its subbooks in one list by their order, the move of
 * entries between them, and the lorebooks an agent works on, which nothing can be done with.
 */
class LorebookMassEditDialogTest extends AgentTestBase {

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService entryService;

    // UI.setCurrent keeps only a weak reference, see WorkspaceStateTest
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

    private Lorebook lorebook(String name) throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName(name);
        lorebook.setSubbooks(new ArrayList<>());
        return lorebookService.save(lorebook);
    }

    private LorebookEntry entry(Lorebook lorebook, String name, int order) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        entry.setOrder(order);
        return entryService.save(entry);
    }

    private LorebookMassEditDialog open(Lorebook root, Runnable onChange) {
        LorebookMassEditDialog dialog = new LorebookMassEditDialog(root, onChange);
        dialog.create();
        return dialog;
    }

    private static List<String> names(LorebookMassEditDialog dialog) {
        return dialog.getRows().stream().map(row -> row.entry().getName()).toList();
    }

    private static LorebookMassEditDialog.Row row(LorebookMassEditDialog dialog, String name) {
        return dialog.getRows().stream().filter(row -> row.entry().getName().equals(name)).findFirst().orElseThrow();
    }

    private List<String> namesIn(Lorebook lorebook) throws Exception {
        return entryService.getEntriesForLorebook(lorebook).stream().map(LorebookEntry::getName).toList();
    }

    private void lock(Lorebook lorebook) throws Exception {
        Manuscript book = new Manuscript();
        book.setName("Locking book");
        book.setLorebook(lorebook);
        book = manuscriptService.save(book);
        agentRunService.start(agentService.save(newAgent(book)));
    }

    @Test
    void theEntriesOfTheLorebookAndItsSubbooksAreListedByTheirOrder() throws Exception {
        Lorebook root = lorebook("Root");
        Lorebook sub = lorebook("Sub");
        Lorebook deep = lorebook("Deep");
        Lorebook other = lorebook("Other");
        lorebookService.addSubbook(root, sub);
        lorebookService.addSubbook(sub, deep);
        entry(root, "Late", 300);
        entry(sub, "Middle", 200);
        entry(deep, "Early", 100);
        entry(other, "Elsewhere", 1);

        LorebookMassEditDialog dialog = open(root, null);

        assertThat(dialog.getBooks()).extracting(Lorebook::getName).containsExactly("Root", "Sub", "Deep");
        assertThat(names(dialog)).as("by order, not by lorebook, and not the lorebooks that are not subbooks")
                .containsExactly("Early", "Middle", "Late");
        assertThat(row(dialog, "Early").book().getName()).isEqualTo("Deep");
    }

    @Test
    void aChangedOrderPutsTheEntryWhereItBelongs() throws Exception {
        Lorebook root = lorebook("Root");
        entry(root, "A", 100);
        entry(root, "B", 200);
        LorebookMassEditDialog dialog = open(root, null);

        dialog.setOrder(row(dialog, "B"), 50);

        assertThat(names(dialog)).containsExactly("B", "A");
        assertThat(entryService.getEntriesForLorebook(root)).extracting(LorebookEntry::getName).containsExactly("B", "A");
    }

    @Test
    void anEntryIsMovedBetweenTheMainBookAndASubbook() throws Exception {
        Lorebook root = lorebook("Root");
        Lorebook sub = lorebook("Sub");
        lorebookService.addSubbook(root, sub);
        entry(root, "InRoot", 100);
        entry(sub, "InSub", 200);
        AtomicInteger changes = new AtomicInteger();
        LorebookMassEditDialog dialog = open(root, changes::incrementAndGet);

        dialog.moveTo(row(dialog, "InRoot"), sub);
        assertThat(namesIn(sub)).containsExactlyInAnyOrder("InRoot", "InSub");
        assertThat(namesIn(root)).isEmpty();
        assertThat(row(dialog, "InRoot").book().getName()).isEqualTo("Sub");

        dialog.moveTo(row(dialog, "InSub"), root);
        assertThat(namesIn(root)).containsExactly("InSub");
        assertThat(namesIn(sub)).containsExactly("InRoot");
    }

    @Test
    void theCheckedEntriesAreMovedTogether() throws Exception {
        Lorebook root = lorebook("Root");
        Lorebook sub = lorebook("Sub");
        lorebookService.addSubbook(root, sub);
        entry(root, "A", 100);
        entry(root, "B", 200);
        entry(root, "C", 300);
        LorebookMassEditDialog dialog = open(root, null);
        assertThat(dialog.getMoveChecked().isEnabled()).as("nothing is checked, no lorebook is picked").isFalse();

        dialog.check(row(dialog, "A"), true);
        dialog.check(row(dialog, "C"), true);
        assertThat(dialog.getMoveChecked().isEnabled()).as("no lorebook is picked").isFalse();
        dialog.getTargetBox().setValue(dialog.getBooks().get(1));
        assertThat(dialog.getMoveChecked().isEnabled()).isTrue();
        dialog.getMoveChecked().click();

        assertThat(namesIn(sub)).containsExactlyInAnyOrder("A", "C");
        assertThat(namesIn(root)).containsExactly("B");
        assertThat(names(dialog)).as("the order does not change").containsExactly("A", "B", "C");
    }

    @Test
    void theEntriesOfALockedLorebookAreLockedAndNothingIsMovedIntoIt() throws Exception {
        Lorebook root = lorebook("Root");
        Lorebook free = lorebook("Free");
        Lorebook used = lorebook("Used");
        lorebookService.addSubbook(root, free);
        lorebookService.addSubbook(root, used);
        entry(root, "InRoot", 100);
        entry(used, "InUsed", 200);
        lock(used);

        LorebookMassEditDialog dialog = open(root, null);

        assertThat(row(dialog, "InUsed").locked()).isTrue();
        assertThat(row(dialog, "InRoot").locked()).isFalse();
        assertThat(dialog.getTargetBox().getListDataView().getItems()).extracting(Lorebook::getName)
                .as("a locked lorebook is not a target").containsExactly("Root", "Free");

        dialog.moveTo(row(dialog, "InRoot"), used);
        dialog.moveTo(row(dialog, "InUsed"), free);
        dialog.check(row(dialog, "InUsed"), true);
        dialog.getTargetBox().setValue(free);
        dialog.getMoveChecked().click();

        assertThat(namesIn(used)).containsExactly("InUsed");
        assertThat(namesIn(root)).containsExactly("InRoot");
        assertThat(namesIn(free)).isEmpty();
    }

    @Test
    void theServiceRefusesAMoveIntoALockedLorebook() throws Exception {
        Lorebook free = lorebook("Free");
        Lorebook used = lorebook("Used");
        LorebookEntry entry = entry(free, "Entry", 100);
        lock(used);

        assertThatThrownBy(() -> entryService.move(entry, used)).isInstanceOf(LorebookLockedException.class);

        assertThat(namesIn(free)).containsExactly("Entry");
    }

    @Test
    void whenTheRootIsLockedEverythingIs() throws Exception {
        Lorebook root = lorebook("Root");
        Lorebook sub = lorebook("Sub");
        lorebookService.addSubbook(root, sub);
        entry(root, "InRoot", 100);
        entry(sub, "InSub", 200);
        lock(root);

        LorebookMassEditDialog dialog = open(root, null);

        assertThat(dialog.getRows()).allMatch(LorebookMassEditDialog.Row::locked);
        assertThat(dialog.getTargetBox().getListDataView().getItems()).isEmpty();
    }

    @Test
    void theGridHasTheColumnsOfTheEntryDetailsToo() throws Exception {
        Lorebook root = lorebook("Root");
        entry(root, "A", 100);

        LorebookMassEditDialog dialog = open(root, null);

        assertThat(dialog.getGrid().getColumns()).as("check, enabled, name, order, lorebook, tags, filtering mode, filtering, "
                + "insertion mode, negative tags, activation context, parts, from, to, note, content").hasSize(16);
    }

    /**
     * Changes the cell of the column the way the user does: sets the value of the field and fires the change as from the browser.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void edit(LorebookMassEditDialog dialog, int column, LorebookMassEditDialog.Row row, Object value) {
        Component field = ((ComponentRenderer<Component, LorebookMassEditDialog.Row>) dialog.getGrid().getColumns().get(column)
                .getRenderer()).createComponent(row);
        ((HasValue) field).setValue(value);
        ComponentUtil.fireEvent(field, new AbstractField.ComponentValueChangeEvent(field, (HasValue) field, null, true));
    }

    @Test
    void theActivationContextAndTheRangeOfPartsAreEditedInTheGrid() throws Exception {
        Lorebook root = lorebook("Root");
        LorebookEntry a = entry(root, "A", 100);
        LorebookMassEditDialog dialog = open(root, null);
        LorebookMassEditDialog.Row row = row(dialog, "A");
        assertThat(a.getActivationContext()).isEqualTo(ActivationContext.INSTRUCTIONS);

        edit(dialog, 10, row, ActivationContext.INCLUDE_PARTS);
        edit(dialog, 11, row, 4);
        edit(dialog, 12, row, 2);
        edit(dialog, 13, row, 9);

        LorebookEntry saved = entryService.find(a.getId());
        assertThat(saved.getActivationContext()).isEqualTo(ActivationContext.INCLUDE_PARTS);
        assertThat(saved.getActivationParts()).isEqualTo(4);
        assertThat(saved.getFromMessage()).isEqualTo(2);
        assertThat(saved.getToMessage()).isEqualTo(9);

        edit(dialog, 12, row, null);
        assertThat(entryService.find(a.getId()).getFromMessage()).as("empty = no limit").isNull();
        assertThat(entryService.find(a.getId()).getToMessage()).isEqualTo(9);
    }

    @Test
    void theCheckedEntriesAreDeletedButNotTheOnesOfALockedLorebook() throws Exception {
        Lorebook root = lorebook("Root");
        Lorebook used = lorebook("Used");
        lorebookService.addSubbook(root, used);
        entry(root, "A", 100);
        entry(root, "B", 200);
        entry(root, "C", 300);
        entry(used, "InUsed", 400);
        lock(used);
        LorebookMassEditDialog dialog = open(root, null);
        assertThat(dialog.getDeleteChecked().isEnabled()).as("nothing is checked").isFalse();

        dialog.check(row(dialog, "A"), true);
        dialog.check(row(dialog, "C"), true);
        assertThat(dialog.getDeleteChecked().isEnabled()).isTrue();
        dialog.check(row(dialog, "InUsed"), true);
        dialog.deleteCheckedNow();

        assertThat(namesIn(root)).containsExactly("B");
        assertThat(namesIn(used)).as("an agent works on it").containsExactly("InUsed");
    }
}
