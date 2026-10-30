package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Renaming many books at once: the list with the names the books would get, the four ways to rename, the cases where nothing
 * is done, and the books an agent works on, which nothing can be done with.
 */
class MassRenameDialogTest extends AgentTestBase {

    @Autowired
    private FolderService folderService;

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

    private Manuscript book(String name) throws Exception {
        Manuscript book = new Manuscript();
        book.setName(name);
        return manuscriptService.save(book);
    }

    private MassRenameDialog open(Runnable onChange) {
        MassRenameDialog dialog = new MassRenameDialog(onChange);
        dialog.create();
        return dialog;
    }

    private static MassRenameDialog.Row row(MassRenameDialog dialog, String name) {
        return dialog.getRows().stream().filter(row -> row.name().equals(name)).findFirst().orElseThrow();
    }

    private static void checkAll(MassRenameDialog dialog) {
        dialog.getShown().forEach(row -> dialog.check(row, true));
    }

    private String nameOf(Manuscript book) throws Exception {
        return manuscriptService.findForUser(book.getUuid()).getName();
    }

    @Test
    void theBooksAreListedByNameWithoutAnyNewNameUntilTheyAreChecked() throws Exception {
        book("charlie");
        book("Alpha");
        MassRenameDialog dialog = open(null);
        dialog.getTextBox().setValue("The ");

        assertThat(dialog.getRows()).extracting(MassRenameDialog.Row::name).containsExactly("Alpha", "charlie");
        assertThat(dialog.getRows()).extracting(MassRenameDialog.Row::preview).containsOnlyNulls();

        dialog.check(row(dialog, "charlie"), true);

        assertThat(row(dialog, "charlie").preview()).isEqualTo("The charlie");
        assertThat(row(dialog, "Alpha").preview()).isNull();
    }

    @Test
    void thePrefixAndThePostfixAreAddedToTheCheckedBooks() throws Exception {
        Manuscript alpha = book("Alpha");
        Manuscript bravo = book("Bravo");
        Manuscript charlie = book("Charlie");
        AtomicInteger changes = new AtomicInteger();
        MassRenameDialog dialog = open(changes::incrementAndGet);
        dialog.check(row(dialog, "Alpha"), true);
        dialog.check(row(dialog, "Bravo"), true);

        dialog.getTextBox().setValue("Vol. ");
        assertThat(dialog.rename()).isTrue();
        assertThat(nameOf(alpha)).isEqualTo("Vol. Alpha");
        assertThat(nameOf(bravo)).isEqualTo("Vol. Bravo");
        assertThat(nameOf(charlie)).isEqualTo("Charlie");
        assertThat(changes.get()).isEqualTo(1);

        MassRenameDialog second = open(null);
        second.getModeBox().setValue(MassRenameDialog.Mode.POSTFIX);
        second.getTextBox().setValue(" (old)");
        second.check(row(second, "Charlie"), true);
        assertThat(second.rename()).isTrue();
        assertThat(nameOf(charlie)).isEqualTo("Charlie (old)");
    }

    @Test
    void findAndReplaceChangesTheTextInTheNames() throws Exception {
        Manuscript alpha = book("Saga - FDM");
        Manuscript bravo = book("Other saga - FDM - fdm");
        MassRenameDialog dialog = open(null);
        dialog.getModeBox().setValue(MassRenameDialog.Mode.REPLACE);
        dialog.getFindBox().setValue(" - fdm");
        checkAll(dialog);

        assertThat(row(dialog, "Saga - FDM").preview()).isEqualTo("Saga");
        assertThat(row(dialog, "Other saga - FDM - fdm").preview()).isEqualTo("Other saga");

        dialog.getCaseBox().setValue(true);
        assertThat(row(dialog, "Saga - FDM").preview()).isEqualTo("Saga - FDM");
        assertThat(dialog.rename()).isTrue();

        assertThat(nameOf(alpha)).isEqualTo("Saga - FDM");
        assertThat(nameOf(bravo)).isEqualTo("Other saga - FDM");
    }

    @Test
    void theNumberingCountsTheCheckedBooksInTheOrderOfTheList() throws Exception {
        Manuscript alpha = book("Alpha");
        Manuscript bravo = book("Bravo");
        Manuscript charlie = book("Charlie");
        MassRenameDialog dialog = open(null);
        dialog.getModeBox().setValue(MassRenameDialog.Mode.NUMBER);
        dialog.getPatternBox().setValue("{n} - {name}");
        dialog.getStartBox().setValue(9);
        dialog.getDigitsBox().setValue(2);
        dialog.check(row(dialog, "Alpha"), true);
        dialog.check(row(dialog, "Charlie"), true);

        assertThat(row(dialog, "Alpha").preview()).isEqualTo("09 - Alpha");
        assertThat(row(dialog, "Bravo").preview()).isNull();
        assertThat(row(dialog, "Charlie").preview()).isEqualTo("10 - Charlie");

        assertThat(dialog.rename()).isTrue();

        assertThat(nameOf(alpha)).isEqualTo("09 - Alpha");
        assertThat(nameOf(bravo)).isEqualTo("Bravo");
        assertThat(nameOf(charlie)).isEqualTo("10 - Charlie");
    }

    @Test
    void nothingIsDoneWithoutTextOrWithoutCheckedBooksOrWhenNothingChanges() throws Exception {
        Manuscript alpha = book("Alpha");
        AtomicInteger changes = new AtomicInteger();
        MassRenameDialog dialog = open(changes::incrementAndGet);

        dialog.check(row(dialog, "Alpha"), true);
        assertThat(dialog.rename()).as("no text").isFalse();
        assertThat(row(dialog, "Alpha").preview()).isNull();

        dialog.getTextBox().setValue("x");
        dialog.check(row(dialog, "Alpha"), false);
        assertThat(dialog.rename()).as("no book is checked").isFalse();

        dialog.getModeBox().setValue(MassRenameDialog.Mode.REPLACE);
        dialog.getFindBox().setValue("zzz");
        dialog.check(row(dialog, "Alpha"), true);
        assertThat(dialog.rename()).as("the text is not in the name").isFalse();

        dialog.getModeBox().setValue(MassRenameDialog.Mode.NUMBER);
        dialog.getPatternBox().setValue("{name}");
        assertThat(dialog.rename()).as("the pattern has no number").isFalse();

        assertThat(nameOf(alpha)).isEqualTo("Alpha");
        assertThat(changes.get()).isZero();
    }

    @Test
    void aNameThatWouldBeEmptyStopsTheWholeRename() throws Exception {
        Manuscript alpha = book("Alpha");
        Manuscript bravo = book("x");
        MassRenameDialog dialog = open(null);
        dialog.getModeBox().setValue(MassRenameDialog.Mode.REPLACE);
        dialog.getFindBox().setValue("x");
        dialog.getWithBox().setValue("");
        checkAll(dialog);

        assertThat(dialog.rename()).isFalse();

        assertThat(nameOf(bravo)).isEqualTo("x");
        assertThat(nameOf(alpha)).isEqualTo("Alpha");
    }

    @Test
    void theNameFilterNarrowsTheListAndUnchecksWhatItHides() throws Exception {
        book("Alpha");
        book("Bravo");
        book("alpine");
        MassRenameDialog dialog = open(null);
        dialog.getTextBox().setValue("x ");
        dialog.check(row(dialog, "Bravo"), true);

        dialog.getNameFilter().setValue("ALP");

        assertThat(dialog.getShown()).extracting(MassRenameDialog.Row::name).containsExactly("Alpha", "alpine");
        assertThat(row(dialog, "Bravo").checked()).as("a hidden book is not renamed").isFalse();
        assertThat(row(dialog, "Bravo").preview()).isNull();
    }

    @Test
    void givenBooksAreListedAndNoOthers() throws Exception {
        Manuscript alpha = book("Alpha");
        Manuscript gone = book("Gone");
        book("Charlie");
        manuscriptService.delete(gone, false);

        MassRenameDialog dialog = new MassRenameDialog(null, List.of(alpha, gone));
        dialog.create();

        assertThat(dialog.getRows()).extracting(MassRenameDialog.Row::name).containsExactly("Alpha");
        assertThat(dialog.getHeaderTitle()).endsWith("Books (2)");
    }

    @Test
    void theBooksOfTheFoldersBelowAreListedOnRequestAndKeepTheirNameForTheRename() throws Exception {
        Folder scifi = folderService.create("Sci-fi", null);
        Folder expanse = folderService.create("Expanse", scifi);
        Manuscript caliban = book("Caliban");
        caliban.setFolder(expanse);
        manuscriptService.save(caliban);
        book("Top");

        MassRenameDialog dialog = new MassRenameDialog(null, scifi);
        dialog.create();
        assertThat(dialog.getRows()).isEmpty();

        dialog.getSubfolders().setValue(true);
        assertThat(dialog.getRows()).hasSize(1);
        MassRenameDialog.Row row = dialog.getRows().getFirst();
        assertThat(row.label()).contains("Expanse").endsWith("Caliban");

        dialog.getTextBox().setValue("The ");
        dialog.check(row, true);
        assertThat(row.preview()).isEqualTo("The Caliban");
        assertThat(dialog.rename()).isTrue();
        assertThat(nameOf(caliban)).isEqualTo("The Caliban");
    }

    @Test
    void theBooksAnAgentWorksOnAreListedButCanNotBeRenamed() throws Exception {
        Manuscript free = book("Free");
        Manuscript locked = book("Locked");
        agentRunService.start(agentService.save(newAgent(locked)));
        MassRenameDialog dialog = open(null);

        assertThat(row(dialog, "Locked").locked()).isTrue();

        dialog.getTextBox().setValue("New ");
        dialog.check(row(dialog, "Locked"), true);
        dialog.check(row(dialog, "Free"), true);
        assertThat(row(dialog, "Locked").preview()).isNull();
        assertThat(dialog.rename()).isTrue();

        assertThat(nameOf(free)).isEqualTo("New Free");
        assertThat(nameOf(locked)).isEqualTo("Locked");
    }
}
