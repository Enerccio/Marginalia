package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.github.enerccio.marginalia.ui.shortcuts.ShortcutActions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The questions the Organise window asks while it works (a clash of folder names, the go-on of a copy, the name of a folder,
 * the confirmation of a delete) and what a book an agent works on does to it. The dialogs are found in the UI and their buttons
 * pressed, as the user would.
 */
class OrganiseDialogFlowTest extends AgentTestBase {

    @Autowired
    private FolderService folderService;

    // UI.setCurrent keeps only a weak reference
    private UI ui;
    private Folder scifi;
    private Folder fantasy;
    private Folder target;

    @BeforeEach
    void setUp() throws Exception {
        ui = new UI();
        UI.setCurrent(ui);
        loginAgentUser();
        scifi = folderService.create("Sci-fi", null);
        fantasy = folderService.create("Fantasy", null);
        target = folderService.create("Target", null);
        book("Alpha", null);
        book("Leviathan", scifi);
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

    private OrganiseDialog dialog(Folder left, Folder right) {
        OrganiseDialog dialog = new OrganiseDialog(left, right);
        dialog.create();
        return dialog;
    }

    private static Stream<Component> descendants(Component component) {
        return Stream.concat(Stream.of(component), component.getChildren().flatMap(OrganiseDialogFlowTest::descendants));
    }

    private static Stream<Component> inside(Dialog dialog) {
        Stream<Component> footer = dialog.getFooter().getElement().getChildren().map(e -> e.getComponent().orElseThrow()).flatMap(OrganiseDialogFlowTest::descendants);
        return Stream.concat(footer, descendants(dialog));
    }

    private static void click(Dialog dialog, String text) {
        inside(dialog).filter(Button.class::isInstance).map(Button.class::cast).filter(b -> text.equals(b.getText())).findFirst()
                .orElseThrow().click();
    }

    private static void tickApplyToAll(Dialog dialog) {
        inside(dialog).filter(Checkbox.class::isInstance).map(Checkbox.class::cast).filter(box -> "Apply to all".equals(box.getLabel()))
                .findFirst().orElseThrow().setValue(true);
    }

    /** Both Sci-fi and Fantasy exist in the target, and both are marked in the left pane. */
    private OrganiseDialog twoClashes() throws Exception {
        folderService.create("Sci-fi", target);
        folderService.create("Fantasy", target);
        OrganiseDialog dialog = dialog(null, target);
        dialog.cursorOn(true, "Fantasy");
        dialog.press("Space");
        dialog.cursorOn(true, "Sci-fi");
        dialog.press("Space");
        return dialog;
    }

    @Test
    void aClashAsksAndARenameGivesTheFolderAFreeName() throws Exception {
        folderService.create("Sci-fi", target);
        OrganiseDialog dialog = dialog(null, target);
        dialog.cursorOn(true, "Sci-fi");

        dialog.press(ShortcutActions.ORGANISE_MOVE);
        assertThat(dialog.lastQuestion()).isInstanceOf(FolderClashDialog.class);
        click(dialog.lastQuestion(), "Rename");

        assertThat(dialog.names(true)).doesNotContain("Sci-fi");
        assertThat(dialog.names(false)).containsExactly("..", "Sci-fi", "Sci-fi (2)");
    }

    @Test
    void applyToAllDecidesTheOtherClashesWithoutAskingAgain() throws Exception {
        OrganiseDialog dialog = twoClashes();

        dialog.press(ShortcutActions.ORGANISE_MOVE);
        Dialog first = dialog.lastQuestion();
        tickApplyToAll(first);
        click(first, "Merge");

        assertThat(dialog.lastQuestion()).as("no second question").isSameAs(first);
        assertThat(dialog.names(true)).doesNotContain("Sci-fi", "Fantasy");
        assertThat(dialog.names(false)).containsExactly("..", "Fantasy", "Sci-fi");
        dialog.open(false, "Sci-fi");
        assertThat(dialog.names(false)).as("merged into the one there").contains("Leviathan");
    }

    @Test
    void applyToAllWithARenameGivesEachFolderANameOfItsOwn() throws Exception {
        OrganiseDialog dialog = twoClashes();

        dialog.press(ShortcutActions.ORGANISE_MOVE);
        tickApplyToAll(dialog.lastQuestion());
        click(dialog.lastQuestion(), "Rename");

        assertThat(dialog.names(false)).containsExactly("..", "Fantasy", "Fantasy (2)", "Sci-fi", "Sci-fi (2)");
    }

    @Test
    void withoutApplyToAllEveryClashAsksAndSkipLeavesTheFolderWhereItIs() throws Exception {
        OrganiseDialog dialog = twoClashes();

        dialog.press(ShortcutActions.ORGANISE_MOVE);
        Dialog first = dialog.lastQuestion();
        click(first, "Skip");
        assertThat(dialog.lastQuestion()).as("the second clash asks").isNotSameAs(first);
        click(dialog.lastQuestion(), "Skip");

        assertThat(dialog.names(true)).contains("Sci-fi", "Fantasy");
        assertThat(dialog.names(false)).containsExactly("..", "Fantasy", "Sci-fi");
    }

    @Test
    void cancellingAClashQuestionTransfersNothing() throws Exception {
        OrganiseDialog dialog = twoClashes();

        dialog.press(ShortcutActions.ORGANISE_MOVE);
        click(dialog.lastQuestion(), "Cancel");

        assertThat(dialog.names(true)).contains("Sci-fi", "Fantasy");
        assertThat(dialog.names(false)).containsExactly("..", "Fantasy", "Sci-fi");
        dialog.open(false, "Sci-fi");
        assertThat(dialog.names(false)).as("the folder there is untouched").containsExactly("..");
    }

    @Test
    void aCopyAsksToGoOnAndOffersTheCurrentBranchOnlyWhenBooksAreCopied() throws Exception {
        OrganiseDialog dialog = dialog(null, target);
        dialog.cursorOn(true, "Alpha");

        dialog.press(ShortcutActions.ORGANISE_COPY);
        Checkbox branch = inside(dialog.lastQuestion()).filter(Checkbox.class::isInstance).map(Checkbox.class::cast).findFirst().orElseThrow();
        assertThat(branch.isVisible()).isTrue();
        assertThat(branch.getValue()).as("the default").isTrue();
        click(dialog.lastQuestion(), "Cancel");
        assertThat(dialog.names(false)).as("nothing is copied").containsExactly("..");

        dialog.cursorOn(true, "Fantasy");
        dialog.press(ShortcutActions.ORGANISE_COPY);
        Checkbox none = inside(dialog.lastQuestion()).filter(Checkbox.class::isInstance).map(Checkbox.class::cast).findFirst().orElseThrow();
        assertThat(none.isVisible()).as("an empty folder has no books").isFalse();
    }

    @Test
    void aBookAnAgentWorksOnIsNotTransferred() throws Exception {
        Manuscript locked = book("Locked", null);
        agentRunService.start(agentService.save(newAgent(locked)));
        OrganiseDialog dialog = dialog(null, target);
        dialog.cursorOn(true, "Alpha");
        dialog.press("Space");
        dialog.cursorOn(true, "Locked");
        dialog.press("Space");

        dialog.press(ShortcutActions.ORGANISE_MOVE);

        assertThat(dialog.names(true)).as("not even the free book is moved").contains("Alpha", "Locked");
        assertThat(dialog.names(false)).containsExactly("..");
        assertThat(dialog.marked(true)).as("the marks stay").containsExactlyInAnyOrder("Alpha", "Locked");
    }
}
