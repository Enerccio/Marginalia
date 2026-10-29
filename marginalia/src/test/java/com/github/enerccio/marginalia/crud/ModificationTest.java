package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code modification} follows the changes by itself: a save sets it, and a change of a part of a book is a change of the
 * book, whoever made it. {@code saveWithoutModification} is the way not to.
 */
class ModificationTest extends MarginaliaTestBase {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private FolderService folderService;

    private Manuscript book;
    private ChatMessage root;

    @BeforeEach
    void createBook() throws Exception {
        login();
        Manuscript m = new Manuscript();
        m.setName("book");
        book = manuscriptService.save(m);
        root = part(null, "root");
        pause();
    }

    private ChatMessage part(ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        return parent == null ? chatMessageService.createRoot(book, message) : chatMessageService.addChild(parent, message);
    }

    private static void pause() throws InterruptedException {
        Thread.sleep(5);
    }

    private Date bookModification() throws Exception {
        return manuscriptService.find(book.getId()).getModification();
    }

    @Test
    void savingAPartChangesTheBook() throws Exception {
        Date before = bookModification();
        pause();

        ChatMessage loaded = chatMessageService.find(root.getId());
        loaded.setResponse("edited");
        chatMessageService.save(loaded);

        assertThat(bookModification()).isAfter(before);
    }

    @Test
    void addingAPartChangesTheBook() throws Exception {
        Date before = bookModification();
        pause();

        part(root, "child");

        assertThat(bookModification()).isAfter(before);
    }

    @Test
    void deletingAPartChangesTheBook() throws Exception {
        ChatMessage child = part(root, "child");
        Date before = bookModification();
        pause();

        chatMessageService.delete(child, false);

        assertThat(bookModification()).isAfter(before);
    }

    @Test
    void removingAPartKeepingItsChildrenChangesTheBook() throws Exception {
        ChatMessage child = part(root, "child");
        part(child, "grandchild");
        Date before = bookModification();
        pause();

        chatMessageService.deleteNodeAndMigrateChildren(child, book, false);

        assertThat(bookModification()).isAfter(before);
    }

    @Test
    void savingAPartWithoutModificationLeavesBothAlone() throws Exception {
        ChatMessage loaded = chatMessageService.find(root.getId());
        Date partBefore = loaded.getModification();
        Date bookBefore = bookModification();
        pause();

        loaded.setResponse("edited quietly");
        chatMessageService.saveWithoutModification(loaded);

        ChatMessage reloaded = chatMessageService.find(root.getId());
        assertThat(reloaded.getResponse()).isEqualTo("edited quietly");
        assertThat(reloaded.getModification()).isEqualTo(partBefore);
        assertThat(bookModification()).isEqualTo(bookBefore);
    }

    @Test
    void savingABookWithoutModificationKeepsItsExtendedAttributes() throws Exception {
        Manuscript loaded = manuscriptService.find(book.getId());
        Date before = loaded.getModification();
        pause();

        loaded.setStyle("terse");
        manuscriptService.saveWithoutModification(loaded);

        Manuscript reloaded = manuscriptService.find(book.getId());
        assertThat(reloaded.getStyle()).isEqualTo("terse");
        assertThat(reloaded.getModification()).isEqualTo(before);
    }

    @Test
    void savingABookStillChangesIt() throws Exception {
        Manuscript loaded = manuscriptService.find(book.getId());
        Date before = loaded.getModification();
        pause();

        manuscriptService.save(loaded);

        assertThat(bookModification()).isAfter(before);
    }

    @Test
    void aNewEntitySavedWithoutModificationStillGetsOne() throws Exception {
        Manuscript m = new Manuscript();
        m.setName("other");

        Manuscript saved = manuscriptService.saveWithoutModification(m);

        assertThat(saved.getModification()).isNotNull();
    }

    @Test
    void openingABookIsNotAChange() throws Exception {
        Date before = bookModification();
        pause();

        manuscriptService.markOpened(manuscriptService.find(book.getId()));

        assertThat(bookModification()).isEqualTo(before);
    }

    /**
     * A book in "outer/inner" and a book at the top level, the folders and the book older than what the test does.
     */
    private Folder inner;
    private Folder outer;
    private Manuscript filed;

    private void fileABook() throws Exception {
        outer = folderService.create("outer", null);
        inner = folderService.create("inner", outer);
        Manuscript m = new Manuscript();
        m.setName("filed");
        m.setFolder(inner);
        filed = manuscriptService.save(m);
        pause();
    }

    private Date modification(Folder folder) throws Exception {
        return folderService.find(folder.getId()).getModification();
    }

    @Test
    void savingABookChangesItsFoldersAndTheOnesAbove() throws Exception {
        fileABook();
        Date innerBefore = modification(inner);
        Date outerBefore = modification(outer);
        pause();

        manuscriptService.save(manuscriptService.find(filed.getId()));

        assertThat(modification(inner)).isAfter(innerBefore);
        assertThat(modification(outer)).isAfter(outerBefore);
    }

    @Test
    void changingAPartOfABookChangesItsFolders() throws Exception {
        fileABook();
        ChatMessage start = new ChatMessage();
        start.setResponse("start");
        book = filed;
        Date innerBefore = modification(inner);
        Date outerBefore = modification(outer);
        pause();

        chatMessageService.createRoot(filed, start);

        assertThat(modification(inner)).isAfter(innerBefore);
        assertThat(modification(outer)).isAfter(outerBefore);
    }

    @Test
    void aBookInAnotherFolderLeavesTheFoldersAlone() throws Exception {
        fileABook();
        Folder other = folderService.create("other", null);
        Date otherBefore = modification(other);
        Date innerBefore = modification(inner);
        pause();

        // the book of the fixture is at the top level
        manuscriptService.save(manuscriptService.find(book.getId()));

        assertThat(modification(other)).isEqualTo(otherBefore);
        assertThat(modification(inner)).isEqualTo(innerBefore);
    }

    @Test
    void savingABookWithoutModificationLeavesItsFoldersAlone() throws Exception {
        fileABook();
        Date innerBefore = modification(inner);
        Date outerBefore = modification(outer);
        pause();

        Manuscript loaded = manuscriptService.find(filed.getId());
        loaded.setStyle("terse");
        manuscriptService.saveWithoutModification(loaded);

        assertThat(modification(inner)).isEqualTo(innerBefore);
        assertThat(modification(outer)).isEqualTo(outerBefore);
    }

    @Test
    void openingABookLeavesItsFoldersAlone() throws Exception {
        fileABook();
        Date innerBefore = modification(inner);
        pause();

        manuscriptService.markOpened(manuscriptService.find(filed.getId()));

        assertThat(modification(inner)).isEqualTo(innerBefore);
    }

    @Test
    void switchingTheBranchOfABookChangesItsFolders() throws Exception {
        fileABook();
        ChatMessage start = new ChatMessage();
        start.setResponse("start");
        ChatMessage first = chatMessageService.createRoot(filed, start);
        ChatMessage child = new ChatMessage();
        child.setResponse("child");
        chatMessageService.addChild(first, child);
        Date outerBefore = modification(outer);
        pause();

        chatMessageService.swipeAndSwitch(manuscriptService.find(filed.getId()), first);

        assertThat(modification(outer)).isAfter(outerBefore);
    }
}
