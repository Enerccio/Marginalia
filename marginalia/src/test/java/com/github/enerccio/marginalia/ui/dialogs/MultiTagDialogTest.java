package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tagging many books, lorebooks or entries at once: the list with the picked tag first, the buttons of a row and the ones
 * for the checked items, and the items an agent works on, which nothing can be done with.
 */
class MultiTagDialogTest extends AgentTestBase {

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    @Autowired
    private FolderService folderService;

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

    private Manuscript book(String name) throws Exception {
        Manuscript book = new Manuscript();
        book.setName(name);
        return manuscriptService.save(book);
    }

    private Lorebook lorebook(String name) throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName(name);
        lorebook.setSubbooks(new ArrayList<>());
        return lorebookService.save(lorebook);
    }

    private LorebookEntry entry(Lorebook lorebook, String name) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        return entryService.save(entry);
    }

    private MultiTagDialog open(MultiTagDialog dialog) {
        dialog.create();
        return dialog;
    }

    private static List<String> names(MultiTagDialog dialog) {
        return dialog.getRows().stream().map(MultiTagDialog.Row::name).toList();
    }

    private MultiTagDialog.Row row(MultiTagDialog dialog, String name) {
        return dialog.getRows().stream().filter(row -> row.name().equals(name)).findFirst().orElseThrow();
    }

    private List<String> tagsOf(com.github.enerccio.marginalia.domain.model.BaseEntity entity) throws Exception {
        return tagRelationService.getTagsForObject(entity).stream().map(Tag::getValue).toList();
    }

    @Test
    void theBooksAreListedByNameWithTheirTags() throws Exception {
        Manuscript bravo = book("Bravo");
        book("Alpha");
        tagRelationService.createRelation(tagService.getOrCreateForUser("noir"), bravo);

        MultiTagDialog dialog = open(new MultiTagDialog(MultiTagDialog.Kind.BOOKS, null));

        assertThat(names(dialog)).containsExactly("Alpha", "Bravo");
        assertThat(row(dialog, "Bravo").tags()).extracting(Tag::getValue).containsExactly("noir");
        assertThat(row(dialog, "Alpha").tags()).isEmpty();
        assertThat(dialog.getApplyChecked().isEnabled()).as("no tag and nothing checked").isFalse();
    }

    @Test
    void theItemsThatHaveThePickedTagComeFirstAndShowItMarked() throws Exception {
        Manuscript alpha = book("Alpha");
        Manuscript bravo = book("Bravo");
        Manuscript charlie = book("Charlie");
        Tag noir = tagService.getOrCreateForUser("noir");
        Tag cozy = tagService.getOrCreateForUser("cozy");
        tagRelationService.createRelation(noir, charlie);
        tagRelationService.createRelation(cozy, charlie);
        tagRelationService.createRelation(cozy, bravo);
        MultiTagDialog dialog = open(new MultiTagDialog(MultiTagDialog.Kind.BOOKS, null));

        dialog.getTagBox().setValue(noir);

        assertThat(names(dialog)).containsExactly("Charlie", "Alpha", "Bravo");
        List<Span> chips = dialog.tagsCell(row(dialog, "Charlie")).getChildren().map(Span.class::cast).toList();
        assertThat(chips).extracting(chip -> chip.getText()).containsExactly("cozy", "noir");
        assertThat(chips.get(0).getClassNames()).doesNotContain(CssStyles.MULTI_TAG_CHIP_MARKED);
        assertThat(chips.get(1).getClassNames()).contains(CssStyles.MULTI_TAG_CHIP, CssStyles.MULTI_TAG_CHIP_MARKED);

        dialog.getTagBox().setValue(cozy);

        assertThat(names(dialog)).containsExactly("Bravo", "Charlie", "Alpha");
        dialog.getTagBox().clear();
        assertThat(names(dialog)).containsExactly("Alpha", "Bravo", "Charlie");
        assertThat(alpha).isNotNull();
    }

    @Test
    void aRowAppliesAndRemovesTheTagForItsItem() throws Exception {
        Manuscript alpha = book("Alpha");
        Manuscript bravo = book("Bravo");
        AtomicInteger changes = new AtomicInteger();
        MultiTagDialog dialog = open(new MultiTagDialog(MultiTagDialog.Kind.BOOKS, changes::incrementAndGet));
        dialog.createTag("noir");
        assertThat(dialog.getTagBox().getValue().getValue()).isEqualTo("noir");

        dialog.applyTo(row(dialog, "Bravo"), true);

        assertThat(tagsOf(bravo)).containsExactly("noir");
        assertThat(tagsOf(alpha)).isEmpty();
        assertThat(names(dialog)).as("the tagged one moved to the top").containsExactly("Bravo", "Alpha");
        assertThat(changes.get()).isEqualTo(1);

        dialog.applyTo(row(dialog, "Bravo"), false);

        assertThat(tagsOf(bravo)).isEmpty();
        assertThat(names(dialog)).containsExactly("Alpha", "Bravo");
        assertThat(changes.get()).isEqualTo(2);
    }

    @Test
    void aTypedTagThatExistsIsUsedNotMadeTwice() throws Exception {
        Tag noir = tagService.getOrCreateForUser("noir");
        MultiTagDialog dialog = open(new MultiTagDialog(MultiTagDialog.Kind.BOOKS, null));

        dialog.createTag("  NOIR ");

        assertThat(dialog.getTagBox().getValue().getId()).isEqualTo(noir.getId());
    }

    @Test
    void theButtonsAtTheBottomActOnTheCheckedItems() throws Exception {
        Manuscript alpha = book("Alpha");
        Manuscript bravo = book("Bravo");
        Manuscript charlie = book("Charlie");
        MultiTagDialog dialog = open(new MultiTagDialog(MultiTagDialog.Kind.BOOKS, null));
        dialog.createTag("noir");
        assertThat(dialog.getApplyChecked().isEnabled()).as("nothing is checked").isFalse();

        dialog.check(row(dialog, "Alpha"), true);
        dialog.check(row(dialog, "Charlie"), true);
        assertThat(dialog.getApplyChecked().isEnabled()).isTrue();
        dialog.getApplyChecked().click();

        assertThat(tagsOf(alpha)).containsExactly("noir");
        assertThat(tagsOf(bravo)).isEmpty();
        assertThat(tagsOf(charlie)).containsExactly("noir");
        assertThat(row(dialog, "Alpha").checked()).as("the checks stay for the next step").isTrue();

        dialog.check(row(dialog, "Charlie"), false);
        dialog.getRemoveChecked().click();

        assertThat(tagsOf(alpha)).isEmpty();
        assertThat(tagsOf(charlie)).containsExactly("noir");
    }

    @Test
    void theBooksAnAgentWorksOnAreListedButCanNotBeChanged() throws Exception {
        Manuscript free = book("Free");
        Manuscript locked = book("Locked");
        agentRunService.start(agentService.save(newAgent(locked)));
        MultiTagDialog dialog = open(new MultiTagDialog(MultiTagDialog.Kind.BOOKS, null));
        dialog.createTag("noir");

        assertThat(names(dialog)).as("the working copy of the run is not listed").containsExactly("Free", "Locked");
        assertThat(row(dialog, "Locked").locked()).isTrue();
        assertThat(row(dialog, "Free").locked()).isFalse();

        dialog.applyTo(row(dialog, "Locked"), true);
        assertThat(tagsOf(locked)).isEmpty();
        // a check that was forced anyway does nothing either
        dialog.check(row(dialog, "Locked"), true);
        dialog.check(row(dialog, "Free"), true);
        dialog.getApplyChecked().click();

        assertThat(tagsOf(locked)).isEmpty();
        assertThat(tagsOf(free)).containsExactly("noir");
    }

    @Test
    void theLorebooksAreTaggedLikeTheBooks() throws Exception {
        Lorebook alpha = lorebook("Alpha");
        Lorebook bravo = lorebook("Bravo");
        MultiTagDialog dialog = open(new MultiTagDialog(MultiTagDialog.Kind.LOREBOOKS, null));
        dialog.createTag("noir");

        dialog.applyTo(row(dialog, "Bravo"), true);

        assertThat(tagsOf(bravo)).containsExactly("noir");
        assertThat(tagsOf(alpha)).isEmpty();
        assertThat(names(dialog)).containsExactly("Bravo", "Alpha");
    }

    @Test
    void theLorebooksOfABookAnAgentWorksOnAreLocked() throws Exception {
        Lorebook used = lorebook("Used");
        lorebook("Free");
        Manuscript locked = book("Book");
        locked.setLorebook(used);
        locked = manuscriptService.save(locked);
        agentRunService.start(agentService.save(newAgent(locked)));

        MultiTagDialog dialog = open(new MultiTagDialog(MultiTagDialog.Kind.LOREBOOKS, null));

        assertThat(row(dialog, "Used").locked()).isTrue();
        assertThat(row(dialog, "Free").locked()).isFalse();
    }

    @Test
    void theEntriesOfTheLorebookAreTagged() throws Exception {
        Lorebook lorebook = lorebook("World");
        LorebookEntry harry = entry(lorebook, "Harry");
        LorebookEntry ron = entry(lorebook, "Ron");
        Lorebook other = lorebook("Other");
        entry(other, "Elsewhere");

        MultiTagDialog dialog = open(new MultiTagDialog(lorebook, null));
        dialog.createTag("hero");

        assertThat(names(dialog)).as("only the entries of this lorebook").containsExactly("Harry", "Ron");
        dialog.check(row(dialog, "Ron"), true);
        dialog.getApplyChecked().click();

        assertThat(tagsOf(ron)).containsExactly("hero");
        assertThat(tagsOf(harry)).isEmpty();
        assertThat(names(dialog)).containsExactly("Ron", "Harry");
    }

    @Test
    void theEntriesOfALockedLorebookAreAllLocked() throws Exception {
        Lorebook lorebook = lorebook("World");
        LorebookEntry harry = entry(lorebook, "Harry");
        Manuscript locked = book("Book");
        locked.setLorebook(lorebook);
        locked = manuscriptService.save(locked);
        agentRunService.start(agentService.save(newAgent(locked)));
        MultiTagDialog dialog = open(new MultiTagDialog(lorebook, null));
        dialog.createTag("hero");

        assertThat(row(dialog, "Harry").locked()).isTrue();
        dialog.applyTo(row(dialog, "Harry"), true);

        assertThat(tagsOf(harry)).isEmpty();
    }

    @Test
    void withoutAPickedTagNothingIsChanged() throws Exception {
        Manuscript alpha = book("Alpha");
        MultiTagDialog dialog = open(new MultiTagDialog(MultiTagDialog.Kind.BOOKS, null));

        dialog.applyTo(row(dialog, "Alpha"), true);

        assertThat(tagsOf(alpha)).isEmpty();
    }

    @Test
    void theBooksOfAFolderAreListedAndTheOnesBelowItOnRequest() throws Exception {
        Folder scifi = folderService.create("Sci-fi", null);
        Folder expanse = folderService.create("Expanse", scifi);
        book("Loose");
        Manuscript leviathan = book("Leviathan");
        manuscriptService.moveToFolder(List.of(leviathan), scifi);
        Manuscript caliban = book("Caliban");
        manuscriptService.moveToFolder(List.of(caliban), expanse);

        MultiTagDialog top = open(new MultiTagDialog(MultiTagDialog.Kind.BOOKS, null, null));
        assertThat(names(top)).containsExactly("Loose");

        MultiTagDialog inside = open(new MultiTagDialog(MultiTagDialog.Kind.BOOKS, null, scifi));
        assertThat(names(inside)).containsExactly("Leviathan");

        inside.getSubfolders().setValue(true);
        assertThat(names(inside)).containsExactly("Sci-fi / Expanse / Caliban", "Sci-fi / Leviathan");
    }

    @Test
    void givenBooksAreTaggedAndNoOthers() throws Exception {
        Manuscript alpha = book("Alpha");
        book("Bravo");

        MultiTagDialog dialog = open(new MultiTagDialog(null, List.of(alpha)));
        assertThat(names(dialog)).containsExactly("Alpha");

        dialog.createTag("space");
        dialog.applyTo(row(dialog, "Alpha"), true);
        assertThat(tagsOf(alpha)).containsExactly("space");
    }

    @Test
    void foldersAreTaggedLikeBooks() throws Exception {
        Folder scifi = folderService.create("Sci-fi", null);
        Folder expanse = folderService.create("Expanse", scifi);
        folderService.create("Fantasy", null);

        MultiTagDialog top = open(new MultiTagDialog(MultiTagDialog.Kind.FOLDERS, null, null));
        assertThat(names(top)).containsExactly("Fantasy", "Sci-fi");

        MultiTagDialog inside = open(new MultiTagDialog(MultiTagDialog.Kind.FOLDERS, null, scifi));
        assertThat(names(inside)).containsExactly("Expanse");
        inside.createTag("space");
        inside.applyTo(row(inside, "Expanse"), true);

        assertThat(tagsOf(expanse)).containsExactly("space");
        assertThat(tagsOf(scifi)).isEmpty();
    }
}
