package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.settings.UserSetting;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.FolderService.Choice;
import com.github.enerccio.marginalia.domain.service.FolderService.Resolution;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.SettingService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import com.github.enerccio.marginalia.ui.shortcuts.KeyBindings;
import com.github.enerccio.marginalia.ui.shortcuts.ShortcutActions;
import com.vaadin.flow.component.UI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The structure of the two-pane dialog: what the panes load and how they move about.
 */
class OrganiseDialogTest extends MarginaliaTestBase {

    @Autowired
    private FolderService folderService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private SettingService settingService;

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

    @Test
    void leftAndRightChangeThePane() {
        OrganiseDialog dialog = dialog(null, scifi);
        assertThat(dialog.leftActive()).isTrue();

        dialog.press("ArrowRight");
        assertThat(dialog.leftActive()).isFalse();

        dialog.press("ArrowLeft");
        assertThat(dialog.leftActive()).isTrue();

        dialog.cursorOn(false, "Leviathan");
        assertThat(dialog.leftActive()).as("a cursor moved in a pane makes it the active one").isFalse();
    }

    @Test
    void enterOpensTheFolderUnderTheCursorLikeADoubleClick() {
        OrganiseDialog dialog = dialog(null, null);

        dialog.cursorOn(true, "Sci-fi");
        dialog.press("Enter");

        assertThat(dialog.path(true)).isEqualTo("Sci-fi");
        assertThat(dialog.names(true)).containsExactly("..", "Expanse", "Leviathan");
    }

    @Test
    void enterOnTheEntryAboveGoesUpAndOnABookDoesNothing() {
        OrganiseDialog dialog = dialog(null, expanse);

        dialog.cursorOn(false, "..");
        dialog.press("Enter");
        assertThat(dialog.path(false)).isEqualTo("Sci-fi");

        dialog.cursorOn(false, "Leviathan");
        dialog.press("Enter");
        assertThat(dialog.path(false)).isEqualTo("Sci-fi");
    }

    @Test
    void backspaceGoesUpOneLevel() {
        OrganiseDialog dialog = dialog(expanse, null);

        dialog.press("Backspace");

        assertThat(dialog.path(true)).isEqualTo("Sci-fi");
    }

    @Test
    void thePaneKeepsItsCursorWhenTheOtherOneHasTheKeyboard() {
        OrganiseDialog dialog = dialog(null, scifi);
        dialog.cursorOn(false, "Leviathan");
        String right = dialog.cursor(false);

        dialog.press("ArrowLeft");
        dialog.press("ArrowRight");

        assertThat(right).isEqualTo("Leviathan");
        assertThat(dialog.cursor(false)).isEqualTo(right);
    }

    @Test
    void goingUpPutsTheCursorOnTheFolderWeCameFrom() {
        OrganiseDialog dialog = dialog(expanse, null);

        dialog.press("Backspace");
        assertThat(dialog.cursor(true)).isEqualTo(expanse.getName());

        dialog.open(true, expanse.getName());
        dialog.cursorOn(true, "..");
        dialog.press("Enter");
        assertThat(dialog.cursor(true)).isEqualTo(expanse.getName());
    }

    @Test
    void spaceAndInsertMarkAndUnmarkTheEntryUnderTheCursor() {
        OrganiseDialog dialog = dialog(null, null);

        dialog.cursorOn(true, "Sci-fi");
        dialog.press("Space");
        dialog.cursorOn(true, "alpha");
        dialog.press("Insert");
        assertThat(dialog.marked(true)).containsExactlyInAnyOrder("Sci-fi", "alpha");

        dialog.press("Space");
        assertThat(dialog.marked(true)).containsExactly("Sci-fi");
    }

    @Test
    void clickingABoxWithShiftMarksTheRangeFromTheBoxClickedBefore() {
        OrganiseDialog dialog = dialog(null, null);
        List<String> names = dialog.names(true);

        dialog.clickBox(true, names.get(0), true, false);
        dialog.clickBox(true, names.get(2), true, true);
        assertThat(dialog.marked(true)).containsExactlyInAnyOrderElementsOf(names.subList(0, 3));

        // a click with Shift clears the range like the box it was made on
        dialog.clickBox(true, names.get(1), false, true);
        assertThat(dialog.marked(true)).containsExactly(names.get(0));
    }

    @Test
    void theEntryAboveCanNotBeMarked() {
        OrganiseDialog dialog = dialog(null, scifi);

        dialog.cursorOn(false, "..");
        dialog.press("Space");
        dialog.press("Shift+ArrowDown");

        assertThat(dialog.marked(false)).isEmpty();
    }

    @Test
    void shiftWithUpAndDownMarksTheEntryTheCursorLeaves() {
        OrganiseDialog dialog = dialog(null, null);

        dialog.cursorOn(true, "Fantasy");
        dialog.press("Shift+ArrowDown");
        dialog.cursorOn(true, "Sci-fi");
        dialog.press("Shift+ArrowDown");
        // marking an entry that is marked keeps it marked
        dialog.press("Shift+ArrowDown");

        assertThat(dialog.marked(true)).containsExactlyInAnyOrder("Fantasy", "Sci-fi");
    }

    @Test
    void openingAFolderClearsTheMarks() {
        OrganiseDialog dialog = dialog(null, null);

        dialog.cursorOn(true, "Zeta");
        dialog.press("Space");
        dialog.cursorOn(true, "Sci-fi");
        dialog.press("Enter");

        assertThat(dialog.marked(true)).isEmpty();
    }

    // copy and move

    private Folder fantasy() throws Exception {
        return folderService.findChildren(null).stream().filter(folder -> folder.getName().equals("Fantasy")).findFirst().orElseThrow();
    }

    @Test
    void f6MovesTheEntryUnderTheCursorIntoTheOtherPane() throws Exception {
        OrganiseDialog dialog = dialog(null, fantasy());
        dialog.cursorOn(true, "alpha");

        dialog.press("organise.move");

        assertThat(dialog.names(true)).containsExactly("Fantasy", "Sci-fi", "Zeta");
        assertThat(dialog.names(false)).containsExactly("..", "alpha");
        assertThat(dialog.leftActive()).isTrue();
    }

    @Test
    void theMarkedEntriesAreMovedAndTheMarksGo() throws Exception {
        OrganiseDialog dialog = dialog(null, fantasy());
        dialog.cursorOn(true, "alpha");
        dialog.press("Space");
        dialog.cursorOn(true, "Sci-fi");
        dialog.press("Space");
        dialog.cursorOn(true, "Zeta");

        dialog.press("organise.move");

        assertThat(dialog.names(true)).containsExactly("Fantasy", "Zeta");
        assertThat(dialog.names(false)).containsExactly("..", "Sci-fi", "alpha");
        assertThat(dialog.marked(true)).isEmpty();
    }

    @Test
    void aCopyOfABookLeavesTheOriginal() throws Exception {
        OrganiseDialog dialog = dialog(null, fantasy());
        dialog.cursorOn(true, "alpha");

        dialog.transferNow(true, true, Map.of());

        assertThat(dialog.names(true)).contains("alpha");
        assertThat(dialog.names(false)).hasSize(2).contains("..");
    }

    @Test
    void theEntryAboveIsNeverTransferred() throws Exception {
        OrganiseDialog dialog = dialog(null, scifi);
        dialog.cursorOn(false, "..");
        dialog.press("organise.move");

        assertThat(dialog.names(false)).containsExactly("..", "Expanse", "Leviathan");
        assertThat(dialog.names(true)).containsExactly("Fantasy", "Sci-fi", "alpha", "Zeta");
    }

    @Test
    void nothingIsTransferredBetweenTwoPanesOnTheSameFolder() {
        OrganiseDialog dialog = dialog(scifi, scifi);
        dialog.cursorOn(true, "Leviathan");

        dialog.press("organise.move");

        assertThat(dialog.names(true)).containsExactly("..", "Expanse", "Leviathan");
    }

    @Test
    void aFolderWithTheNameOfOneInTheTargetClashes() throws Exception {
        folderService.create("Expanse", fantasy());
        OrganiseDialog dialog = dialog(scifi, fantasy());
        dialog.cursorOn(true, "Expanse");

        assertThat(dialog.clashes(false)).containsExactly("Expanse");
        assertThat(dialog.clashes(true)).containsExactly("Expanse");
    }

    @Test
    void aClashIsResolvedByRenameSkipOrMerge() throws Exception {
        folderService.create("Expanse", fantasy());
        OrganiseDialog dialog = dialog(scifi, fantasy());
        dialog.cursorOn(true, "Expanse");

        dialog.transferNow(true, true, Map.of("Expanse", new Choice(Resolution.SKIP, null)));
        assertThat(dialog.names(false)).containsExactly("..", "Expanse");

        dialog.transferNow(true, true, Map.of("Expanse", new Choice(Resolution.RENAME, "Expanse (2)")));
        assertThat(dialog.names(false)).containsExactly("..", "Expanse", "Expanse (2)");

        dialog.transferNow(false, true, Map.of("Expanse", new Choice(Resolution.MERGE, null)));
        assertThat(dialog.names(true)).as("the merged folder is emptied into the one in the target").doesNotContain("Caliban");
        dialog.open(false, "Expanse");
        assertThat(dialog.names(false)).contains("Caliban");
    }

    @Test
    void theButtonsShowTheKeysAndTheUserCanChangeThem() throws Exception {
        assertThat(dialog(null, null).buttons()).containsExactly("Copy (c)", "Move (m)", "New folder (n)", "Rename (Shift+r)", "Delete (Delete)",
                "Folder settings (i)", "Mass Change Model/Protocol (p)", "Mass Change Language (l)", "Tag books (t)", "Advanced search (f)", "Show folder in the list (o)");

        UserSetting setting = settingService.getOrCreate(UserSetting.class);
        setting.setKeyBindings(KeyBindings.toStored(setting.getKeyBindings(), Map.of(ShortcutActions.ORGANISE_COPY, "F5", ShortcutActions.ORGANISE_MOVE, "")));
        settingService.save(setting);

        assertThat(dialog(null, null).buttons()).startsWith("Copy (F5)", "Move");
    }

    private Manuscript topLevelBook(String name) throws Exception {
        return manuscriptService.findRegularInFolder(null, false).stream().filter(book -> name.equals(book.getName())).findFirst().orElseThrow();
    }

    @Test
    void aFolderMadeIsShownInBothPanesOnTheSameFolderWithTheCursorOnIt() throws Exception {
        OrganiseDialog dialog = dialog(null, null);

        dialog.folderChanged(folderService.create("Horror", null));

        assertThat(dialog.names(true)).contains("Horror");
        assertThat(dialog.names(false)).contains("Horror");
        assertThat(dialog.cursor(true)).isEqualTo("Horror");
    }

    @Test
    void aRenamedFolderKeepsTheCursor() throws Exception {
        OrganiseDialog dialog = dialog(null, null);
        dialog.cursorOn(true, "Fantasy");

        dialog.folderChanged(folderService.rename(fantasy(), "Zzz"));

        assertThat(dialog.names(true)).containsExactly("Sci-fi", "Zzz", "alpha", "Zeta");
        assertThat(dialog.cursor(true)).isEqualTo("Zzz");
    }

    @Test
    void aRenamedBookKeepsTheCursor() throws Exception {
        OrganiseDialog dialog = dialog(null, null);
        dialog.cursorOn(true, "alpha");

        dialog.renameBook(topLevelBook("alpha"), " omega ");

        assertThat(dialog.names(true)).containsExactly("Fantasy", "Sci-fi", "omega", "Zeta");
        assertThat(dialog.names(false)).contains("omega");
        assertThat(dialog.cursor(true)).isEqualTo("omega");
    }

    @Test
    void aDeletedBookLeavesTheCursorOnItsRow() throws Exception {
        OrganiseDialog dialog = dialog(null, null);
        dialog.cursorOn(true, "alpha");

        dialog.deleteBook(topLevelBook("alpha"));

        assertThat(dialog.names(true)).containsExactly("Fantasy", "Sci-fi", "Zeta");
        assertThat(dialog.names(false)).doesNotContain("alpha");
        assertThat(dialog.cursor(true)).isEqualTo("Zeta");
    }

    @Test
    void renameAndDeleteDoNothingOnTheEntryAbove() {
        OrganiseDialog dialog = dialog(scifi, null);
        assertThat(dialog.cursor(true)).isEqualTo("..");

        dialog.press(ShortcutActions.ORGANISE_RENAME);
        dialog.press(ShortcutActions.ORGANISE_DELETE);

        assertThat(dialog.names(true)).containsExactly("..", "Expanse", "Leviathan");
    }

    @Test
    void aResultOfTheSearchIsShownInItsFolderAndMarked() throws Exception {
        OrganiseDialog dialog = dialog(null, null);
        Manuscript caliban = manuscriptService.findRegularInFolder(expanse, false).getFirst();

        dialog.showInFolder(false, caliban);

        assertThat(dialog.leftActive()).isFalse();
        assertThat(dialog.path(false)).isEqualTo("Sci-fi/Expanse");
        assertThat(dialog.names(true)).contains("Zeta");
        assertThat(dialog.marked(false)).containsExactly("Caliban");
        assertThat(dialog.cursor(false)).isEqualTo("Caliban");
    }

    @Test
    void theMassChangesAndTheSearchOpenForTheMarkedBooksOrTheFolder() {
        OrganiseDialog dialog = dialog(null, scifi);
        dialog.cursorOn(true, "alpha");
        dialog.press("Space");

        dialog.press(ShortcutActions.ORGANISE_MODEL_PROTOCOL);
        dialog.press(ShortcutActions.ORGANISE_LANGUAGE);
        dialog.press(ShortcutActions.ORGANISE_TAG_BOOKS);
        dialog.press(ShortcutActions.ORGANISE_SEARCH);

        dialog.press("Space");
        dialog.press(ShortcutActions.ORGANISE_MODEL_PROTOCOL);
        dialog.press(ShortcutActions.ORGANISE_LANGUAGE);
        dialog.press(ShortcutActions.ORGANISE_TAG_BOOKS);
    }

    @Test
    void theFolderOfTheActivePaneIsShownInTheList() {
        OrganiseDialog dialog = dialog(null, scifi);
        List<Folder> shown = new ArrayList<>();
        dialog.setOnShowInList(shown::add);

        dialog.press(ShortcutActions.ORGANISE_SHOW_IN_LIST);
        dialog.press(ShortcutActions.ORGANISE_SWITCH_PANE);
        dialog.press(ShortcutActions.ORGANISE_SHOW_IN_LIST);

        assertThat(shown).hasSize(2);
        assertThat(shown.get(0)).as("the top level").isNull();
        assertThat(shown.get(1).getId()).isEqualTo(scifi.getId());
    }

    @Test
    void theFolderOfTheRightPaneIsToldWhenTheWindowIsClosed() {
        OrganiseDialog dialog = dialog(null, scifi);
        List<Folder> targets = new ArrayList<>();
        dialog.setOnTarget(targets::add);
        dialog.open();
        dialog.open(false, "Expanse");

        dialog.close();

        assertThat(targets).hasSize(1);
        assertThat(targets.getFirst().getId()).isEqualTo(expanse.getId());
    }

    @Test
    void tabSwitchesToTheOtherPaneAndBack() {
        OrganiseDialog dialog = dialog(null, scifi);
        assertThat(dialog.leftActive()).isTrue();

        dialog.press(ShortcutActions.ORGANISE_SWITCH_PANE);
        assertThat(dialog.leftActive()).isFalse();

        dialog.press(ShortcutActions.ORGANISE_SWITCH_PANE);
        assertThat(dialog.leftActive()).isTrue();
        assertThat(dialog.buttons()).as("a key only, no button").hasSize(11);
    }
}
