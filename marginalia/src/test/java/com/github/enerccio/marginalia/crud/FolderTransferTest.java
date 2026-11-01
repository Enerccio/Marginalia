package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.FolderService.Choice;
import com.github.enerccio.marginalia.domain.service.FolderService.Outcome;
import com.github.enerccio.marginalia.domain.service.FolderService.Resolution;
import com.github.enerccio.marginalia.domain.service.FolderService.Transfer;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;
import com.github.enerccio.marginalia.domain.service.search.Sorter;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Copying and moving books and folders into a folder, the way the two-pane dialog does: nothing changes when a rule is
 * broken, a clash of folder names needs a choice, a copy is a deep copy.
 */
class FolderTransferTest extends AgentTestBase {

    @Autowired
    private FolderService folderService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    private User me;
    private Folder scifi;
    private Folder fantasy;

    @BeforeEach
    void setUp() throws Exception {
        me = loginAgentUser();
        scifi = folderService.create("Sci-fi", null);
        fantasy = folderService.create("Fantasy", null);
    }

    private Manuscript book(String name, Folder folder) throws Exception {
        Manuscript book = new Manuscript();
        book.setName(name);
        book.setFolder(folder);
        return manuscriptService.save(book);
    }

    private Transfer move(List<Manuscript> books, List<Folder> folders, Folder target, Map<Long, Choice> choices) {
        return new Transfer(books, folders, target, false, false, choices);
    }

    private Transfer copy(List<Manuscript> books, List<Folder> folders, Folder target, boolean onlyCurrentBranch, Map<Long, Choice> choices) {
        return new Transfer(books, folders, target, true, onlyCurrentBranch, choices);
    }

    private Folder folderOf(Manuscript book) throws Exception {
        return manuscriptService.find(book.getId()).getFolder();
    }

    private List<String> bookNames(Folder folder) throws Exception {
        return manuscriptService.findRegularInFolder(folder, false).stream().map(Manuscript::getName).sorted().toList();
    }

    private List<String> folderNames(Folder parent) throws Exception {
        return folderService.findChildren(parent).stream().map(Folder::getName).toList();
    }

    @Test
    void booksAreMovedToAFolder() throws Exception {
        Manuscript a = book("A", null);
        Manuscript b = book("B", scifi);

        Outcome outcome = folderService.transfer(move(List.of(a, b), List.of(), fantasy, Map.of()));

        assertThat(outcome).isEqualTo(new Outcome(2, 0, 0));
        assertThat(bookNames(fantasy)).containsExactly("A", "B");
        assertThat(bookNames(scifi)).isEmpty();
    }

    @Test
    void booksAreMovedToTheTopLevel() throws Exception {
        Manuscript a = book("A", scifi);

        folderService.transfer(move(List.of(a), List.of(), null, Map.of()));

        assertThat(folderOf(a)).isNull();
    }

    @Test
    void booksAreCopiedWithTheirStoryAndTheOriginalStays() throws Exception {
        Manuscript a = book("A", scifi);
        ChatMessage root = chatMessageService.createRoot(a, part("root"));
        chatMessageService.addChild(root, part("child"));

        Outcome outcome = folderService.transfer(copy(List.of(a), List.of(), fantasy, false, Map.of()));

        assertThat(outcome.books()).isEqualTo(1);
        assertThat(bookNames(scifi)).containsExactly("A");
        assertThat(bookNames(fantasy)).containsExactly("A");
        Manuscript clone = manuscriptService.findRegularInFolder(fantasy, false).getFirst();
        assertThat(clone.getId()).isNotEqualTo(a.getId());
        assertThat(chatMessageService.getAllMessages(clone)).hasSize(2);
    }

    @Test
    void aCopyCanKeepOnlyTheCurrentBranch() throws Exception {
        Manuscript a = book("A", null);
        ChatMessage root = chatMessageService.createRoot(a, part("root"));
        ChatMessage kept = chatMessageService.addChild(root, part("kept"));
        chatMessageService.addChild(root, part("other"));
        Manuscript stored = manuscriptService.find(a.getId());
        stored.setActiveLeaf(kept);
        manuscriptService.save(stored);

        folderService.transfer(copy(List.of(a), List.of(), fantasy, true, Map.of()));

        Manuscript clone = manuscriptService.findRegularInFolder(fantasy, false).getFirst();
        assertThat(chatMessageService.getAllMessages(clone)).hasSize(2);
    }

    private ChatMessage part(String text) {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        return message;
    }

    @Test
    void aFolderIsMovedWithEverythingInIt() throws Exception {
        Folder expanse = folderService.create("Expanse", scifi);
        Manuscript caliban = book("Caliban", expanse);

        Outcome outcome = folderService.transfer(move(List.of(), List.of(scifi), fantasy, Map.of()));

        assertThat(outcome).isEqualTo(new Outcome(1, 1, 0));
        assertThat(folderNames(fantasy)).containsExactly("Sci-fi");
        assertThat(folderService.getPath(folderService.find(expanse.getId()))).isEqualTo("Fantasy/Sci-fi/Expanse");
        assertThat(folderOf(caliban).getId()).isEqualTo(expanse.getId());
    }

    @Test
    void aFolderGoesToTheTopLevel() throws Exception {
        Folder expanse = folderService.create("Expanse", scifi);

        folderService.transfer(move(List.of(), List.of(expanse), null, Map.of()));

        assertThat(folderNames(null)).containsExactly("Expanse", "Fantasy", "Sci-fi");
    }

    @Test
    void aFolderCanNotGoIntoItselfOrBelowItAndNothingChanges() throws Exception {
        Folder expanse = folderService.create("Expanse", scifi);
        Manuscript a = book("A", null);

        assertThatThrownBy(() -> folderService.transfer(move(List.of(a), List.of(scifi), expanse, Map.of())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> folderService.transfer(copy(List.of(), List.of(scifi), scifi, false, Map.of())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(folderOf(a)).isNull();
        assertThat(folderNames(null)).containsExactly("Fantasy", "Sci-fi");
    }

    @Test
    void aClashNeedsAChoiceAndNothingChangesWithoutOne() throws Exception {
        Folder other = folderService.create("Sci-fi", fantasy);
        Manuscript a = book("A", null);

        assertThat(folderService.findClashes(List.of(scifi), fantasy, false)).extracting(Folder::getId).containsExactly(scifi.getId());
        assertThat(folderService.findClashes(List.of(scifi), null, false)).as("a move to where it is changes nothing").isEmpty();
        assertThat(folderService.findClashes(List.of(scifi), null, true)).as("a copy there would clash").hasSize(1);

        assertThatThrownBy(() -> folderService.transfer(move(List.of(a), List.of(scifi), fantasy, Map.of())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(folderOf(a)).isNull();
        assertThat(folderNames(fantasy)).containsExactly("Sci-fi");
        assertThat(folderService.find(other.getId()).getParent().getId()).isEqualTo(fantasy.getId());
    }

    @Test
    void aClashedFolderCanBeRenamedOnTheWay() throws Exception {
        folderService.create("Sci-fi", fantasy);

        folderService.transfer(move(List.of(), List.of(scifi), fantasy, Map.of(scifi.getId(), new Choice(Resolution.RENAME, "Sci-fi 2"))));

        assertThat(folderNames(fantasy)).containsExactly("Sci-fi", "Sci-fi 2");
        assertThat(folderNames(null)).containsExactly("Fantasy");
    }

    @Test
    void aRenameToAnotherTakenNameIsRefused() throws Exception {
        folderService.create("Sci-fi", fantasy);
        folderService.create("Taken", fantasy);

        assertThatThrownBy(() -> folderService.transfer(move(List.of(), List.of(scifi), fantasy,
                Map.of(scifi.getId(), new Choice(Resolution.RENAME, "taken")))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(folderNames(null)).containsExactly("Fantasy", "Sci-fi");
    }

    @Test
    void aClashedFolderCanBeSkipped() throws Exception {
        folderService.create("Sci-fi", fantasy);
        Folder expanse = folderService.create("Expanse", null);

        Outcome outcome = folderService.transfer(move(List.of(), List.of(scifi, expanse), fantasy,
                Map.of(scifi.getId(), new Choice(Resolution.SKIP, null))));

        assertThat(outcome).isEqualTo(new Outcome(0, 1, 1));
        assertThat(folderNames(null)).containsExactly("Fantasy", "Sci-fi");
        assertThat(folderNames(fantasy)).containsExactly("Expanse", "Sci-fi");
    }

    @Test
    void movingIntoAFolderOfTheSameNameMergesRecursively() throws Exception {
        // Fantasy/Sci-fi has Expanse (with Caliban) and a book; the Sci-fi that moves has Expanse (with Abaddon), Dune and a book
        Folder targetScifi = folderService.create("Sci-fi", fantasy);
        Folder targetExpanse = folderService.create("Expanse", targetScifi);
        book("Caliban", targetExpanse);
        book("Existing", targetScifi);
        Folder expanse = folderService.create("Expanse", scifi);
        Folder dune = folderService.create("Dune", scifi);
        book("Abaddon", expanse);
        book("Leviathan", scifi);

        folderService.transfer(move(List.of(), List.of(scifi), fantasy, Map.of(scifi.getId(), new Choice(Resolution.MERGE, null))));

        assertThat(folderNames(null)).as("the source is gone").containsExactly("Fantasy");
        assertThat(folderNames(fantasy)).containsExactly("Sci-fi");
        assertThat(folderNames(targetScifi)).containsExactly("Dune", "Expanse");
        assertThat(bookNames(targetScifi)).containsExactly("Existing", "Leviathan");
        assertThat(bookNames(targetExpanse)).containsExactly("Abaddon", "Caliban");
        assertThat(folderService.find(dune.getId()).getParent().getId()).isEqualTo(targetScifi.getId());
        assertThat(folderService.find(expanse.getId()).isDeleted()).as("the merged folder is deleted").isTrue();
        assertThat(folderService.find(scifi.getId()).isDeleted()).isTrue();
    }

    @Test
    void aFolderIsCopiedDeeplyWithItsSettingsAndTags() throws Exception {
        Folder expanse = folderService.create("Expanse", scifi);
        scifi = folderService.find(scifi.getId());
        scifi.setStyle("terse");
        scifi.setLanguage(Language.ENGLISH);
        scifi.setDescription("space opera");
        scifi = folderService.save(scifi);
        Tag tag = tagService.getOrCreateForUser("space");
        tagRelationService.createRelation(tag, scifi);
        Manuscript leviathan = book("Leviathan", scifi);
        book("Caliban", expanse);
        chatMessageService.createRoot(leviathan, part("root"));

        Outcome outcome = folderService.transfer(copy(List.of(), List.of(scifi), fantasy, false, Map.of()));

        assertThat(outcome).isEqualTo(new Outcome(2, 2, 0));
        Folder copy = folderService.findChild(fantasy, "Sci-fi");
        assertThat(copy.getId()).isNotEqualTo(scifi.getId());
        assertThat(copy.getStyle()).isEqualTo("terse");
        assertThat(copy.getDescription()).isEqualTo("space opera");
        assertThat(tagRelationService.getTagsForObject(copy)).extracting(Tag::getValue).containsExactly("space");
        assertThat(bookNames(copy)).containsExactly("Leviathan");
        Folder copiedExpanse = folderService.findChild(copy, "Expanse");
        assertThat(bookNames(copiedExpanse)).containsExactly("Caliban");
        Manuscript copiedBook = manuscriptService.findRegularInFolder(copy, false).getFirst();
        assertThat(chatMessageService.getAllMessages(copiedBook)).hasSize(1);

        // the original is untouched
        assertThat(folderNames(null)).containsExactly("Fantasy", "Sci-fi");
        assertThat(bookNames(scifi)).containsExactly("Leviathan");
        assertThat(manuscriptService.findRegularInFolder(null, true)).hasSize(4);
    }

    @Test
    void aFolderCopiedIntoWhereItIsNeedsAChoice() throws Exception {
        assertThatThrownBy(() -> folderService.transfer(copy(List.of(), List.of(scifi), null, false, Map.of())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> folderService.transfer(copy(List.of(), List.of(scifi), null, false,
                Map.of(scifi.getId(), new Choice(Resolution.MERGE, null)))))
                .as("merging a folder with itself").isInstanceOf(IllegalStateException.class);

        folderService.transfer(copy(List.of(), List.of(scifi), null, false, Map.of(scifi.getId(), new Choice(Resolution.RENAME, "Sci-fi copy"))));

        assertThat(folderNames(null)).containsExactly("Fantasy", "Sci-fi", "Sci-fi copy");
    }

    @Test
    void aCopyMergesIntoTheFolderOfTheSameNameAndKeepsTheSource() throws Exception {
        Folder targetScifi = folderService.create("Sci-fi", fantasy);
        book("Existing", targetScifi);
        book("Leviathan", scifi);
        Folder expanse = folderService.create("Expanse", scifi);
        book("Caliban", expanse);

        folderService.transfer(copy(List.of(), List.of(scifi), fantasy, false, Map.of(scifi.getId(), new Choice(Resolution.MERGE, null))));

        assertThat(bookNames(targetScifi)).containsExactly("Existing", "Leviathan");
        assertThat(folderNames(targetScifi)).containsExactly("Expanse");
        assertThat(bookNames(folderService.findChild(targetScifi, "Expanse"))).containsExactly("Caliban");
        assertThat(bookNames(scifi)).as("the source stays").containsExactly("Leviathan");
        assertThat(bookNames(expanse)).containsExactly("Caliban");
    }

    @Test
    void anotherUsersFoldersAndBooksAreRefused() throws Exception {
        Manuscript mine = book("Mine", null);
        Folder foreign = foreignFolder();

        assertThatThrownBy(() -> folderService.transfer(move(List.of(mine), List.of(), foreign, Map.of())))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> folderService.transfer(move(List.of(), List.of(foreign), fantasy, Map.of())))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(folderOf(mine)).isNull();
    }

    private Folder foreignFolder() throws Exception {
        loginAs(createUser());
        Folder foreign = folderService.create("Foreign", null);
        loginAs(me);
        return foreign;
    }

    @Test
    void aBookAnAgentWorksOnBlocksTheTransfer() throws Exception {
        Manuscript locked = book("Locked", scifi);
        Manuscript free = book("Free", null);
        com.github.enerccio.marginalia.domain.model.impl.Agent agent = agentService.save(newAgent(locked));
        agentRunService.start(agent);

        assertThatThrownBy(() -> folderService.transfer(move(List.of(free, locked), List.of(), fantasy, Map.of())))
                .isInstanceOf(ManuscriptLockedException.class);
        assertThatThrownBy(() -> folderService.transfer(copy(List.of(), List.of(scifi), fantasy, false, Map.of())))
                .isInstanceOf(ManuscriptLockedException.class);
        assertThatThrownBy(() -> folderService.transfer(move(List.of(), List.of(scifi), fantasy, Map.of())))
                .isInstanceOf(ManuscriptLockedException.class);

        assertThat(folderOf(free)).as("the free book did not move either").isNull();
        assertThat(manuscriptService.findRegularInFolder(null, true)).hasSize(2);
        assertThat(folderNames(fantasy)).isEmpty();
        assertThat(manuscriptService.searchRegularManuscripts(new ManuscriptFilterValues().inFolder(scifi, false), Sorter.sorter("name")))
                .hasSize(1);
    }

    @Test
    void aCopyReportsEveryBookAndEveryFolderItMakes() throws Exception {
        book("Leviathan", scifi);
        Folder expanse = folderService.create("Expanse", scifi);
        book("Caliban", expanse);
        book("Abaddon", expanse);
        Manuscript loose = book("Loose", null);
        int[] steps = {0};

        folderService.transfer(copy(List.of(loose), List.of(scifi), fantasy, false, Map.of()), () -> steps[0]++);

        assertThat(steps[0]).as("1 loose book, 2 folders, 3 books in them").isEqualTo(6);
    }
}
