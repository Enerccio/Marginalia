package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.FolderService;
import com.github.enerccio.marginalia.domain.service.FolderService.Content;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.TagRelationService;
import com.github.enerccio.marginalia.domain.service.TagService;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The rules of the folders: names, parents, moves, deletes and the books in them.
 */
class FolderServiceTest extends MarginaliaTestBase {

    @Autowired
    private FolderService folderService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    @BeforeEach
    void loginUser() throws Exception {
        login();
    }

    private Folder folder(String name, Folder parent) throws Exception {
        return folderService.create(name, parent);
    }

    private Manuscript book(String name, Folder folder) throws Exception {
        Manuscript book = new Manuscript();
        book.setName(name);
        book.setFolder(folder);
        return manuscriptService.save(book);
    }

    private Manuscript reload(Manuscript book) throws Exception {
        return manuscriptService.find(book.getId());
    }

    @Test
    void foldersNestAndHaveAPath() throws Exception {
        Folder scifi = folder("Sci-fi", null);
        Folder expanse = folder("Expanse", scifi);
        Folder book1 = folder("Book 1", expanse);

        assertThat(folderService.getPath(book1)).isEqualTo("Sci-fi/Expanse/Book 1");
        assertThat(folderService.getAncestors(book1)).extracting(Folder::getName).containsExactly("Sci-fi", "Expanse");
        assertThat(folderService.getAncestors(scifi)).isEmpty();
        assertThat(folderService.getPath(null)).isEmpty();
    }

    @Test
    void childrenAreListedByNameInTheirParent() throws Exception {
        Folder scifi = folder("Sci-fi", null);
        folder("beta", scifi);
        folder("Alpha", scifi);
        folder("Fantasy", null);

        assertThat(folderService.findChildren(scifi)).extracting(Folder::getName).containsExactly("Alpha", "beta");
        assertThat(folderService.findChildren(null)).extracting(Folder::getName).containsExactly("Fantasy", "Sci-fi");
    }

    @Test
    void nameIsTrimmedAndChecked() throws Exception {
        assertThat(folder("  Padded  ", null).getName()).isEqualTo("Padded");

        assertThatThrownBy(() -> folder("   ", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> folder(null, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> folder("a/b", null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> folder("x".repeat(FolderService.MAX_NAME_LENGTH + 1), null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(folder("x".repeat(FolderService.MAX_NAME_LENGTH), null)).isNotNull();
    }

    @Test
    void namesAreUniqueInAParentIgnoringCase() throws Exception {
        Folder scifi = folder("Sci-fi", null);
        folder("Expanse", scifi);

        assertThatThrownBy(() -> folder("EXPANSE", scifi)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> folder("sci-fi", null)).isInstanceOf(IllegalStateException.class);
        // the same name in another parent, or a deleted folder's name, is free
        assertThat(folder("Expanse", null)).isNotNull();
        folderService.delete(folderService.findChild(scifi, "Expanse"), false);
        assertThat(folder("Expanse", scifi)).isNotNull();
    }

    @Test
    void renameKeepsTheRules() throws Exception {
        Folder scifi = folder("Sci-fi", null);
        Folder other = folder("Other", null);

        assertThat(folderService.rename(scifi, "Science fiction").getName()).isEqualTo("Science fiction");
        assertThatThrownBy(() -> folderService.rename(other, "science FICTION")).isInstanceOf(IllegalStateException.class);
        // its own name in another case is fine
        assertThat(folderService.rename(other, "OTHER").getName()).isEqualTo("OTHER");
        folderService.delete(other, false);
        assertThat(folderService.rename(other, "Gone")).isNull();
    }

    @Test
    void moveChangesTheParentAndRejectsCycles() throws Exception {
        Folder a = folder("A", null);
        Folder b = folder("B", a);
        Folder c = folder("C", b);

        assertThatThrownBy(() -> folderService.move(a, a)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> folderService.move(a, c)).isInstanceOf(IllegalStateException.class);

        folderService.move(c, null);
        assertThat(folderService.getPath(c)).isEqualTo("C");
        folderService.move(b, c);
        assertThat(folderService.getPath(b)).isEqualTo("C/B");
        assertThat(folderService.findChildren(a)).isEmpty();
    }

    @Test
    void moveRejectsANameThatIsTaken() throws Exception {
        Folder a = folder("A", null);
        Folder inA = folder("Same", a);
        folder("Same", null);

        assertThatThrownBy(() -> folderService.move(inA, null)).isInstanceOf(IllegalStateException.class);
        assertThat(folderService.getPath(inA)).isEqualTo("A/Same");
    }

    @Test
    void aFolderOfAnotherUserIsNotFound() throws Exception {
        Folder mine = folder("Mine", null);

        loginAs(createUser());

        assertThat(folderService.findChildren(null)).isEmpty();
        assertThat(folderService.findForUser(mine.getUuid())).isNull();
        assertThatThrownBy(() -> folder("Child", mine)).isInstanceOf(IllegalArgumentException.class);
        assertThat(folderService.rename(mine, "Stolen")).isNull();
        assertThat(folderService.move(mine, null)).isNull();
        assertThat(folder("Mine", null)).isNotNull();
    }

    @Test
    void aBookIsInAFolderOfItsUserOnly() throws Exception {
        Folder mine = folder("Mine", null);
        Manuscript inside = book("Inside", mine);

        assertThat(reload(inside).getFolder().getId()).isEqualTo(mine.getId());

        loginAs(createUser());
        assertThatThrownBy(() -> book("Intruder", mine)).isInstanceOf(IllegalArgumentException.class);
        Manuscript own = book("Own", null);
        assertThatThrownBy(() -> manuscriptService.moveToFolder(List.of(own), mine)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void moveToFolderPutsBooksInAndOutWithoutTouchingThem() throws Exception {
        Folder scifi = folder("Sci-fi", null);
        Manuscript one = book("One", null);
        Manuscript two = book("Two", null);
        Date modified = reload(one).getModification();
        Thread.sleep(5);

        manuscriptService.moveToFolder(List.of(one, two), scifi);

        assertThat(reload(one).getFolder().getId()).isEqualTo(scifi.getId());
        assertThat(reload(two).getFolder().getId()).isEqualTo(scifi.getId());
        assertThat(reload(one).getModification()).isEqualTo(modified);

        manuscriptService.moveToFolder(List.of(one), null);
        assertThat(reload(one).getFolder()).isNull();
        assertThat(reload(two).getFolder()).isNotNull();
    }

    @Test
    void deleteMovesTheContentUp() throws Exception {
        Folder a = folder("A", null);
        Folder b = folder("B", a);
        Folder c = folder("C", b);
        Manuscript inB = book("In B", b);
        Manuscript inC = book("In C", c);

        folderService.delete(b, false);

        assertThat(folderService.findForUser(b.getUuid())).isNull();
        assertThat(folderService.getPath(c)).isEqualTo("A/C");
        assertThat(reload(inB).getFolder().getId()).isEqualTo(a.getId());
        assertThat(reload(inC).getFolder().getId()).isEqualTo(c.getId());
        assertThat(reload(inB).isDeleted()).isFalse();
    }

    @Test
    void deleteFromTheTopLevelPutsTheContentOnTheTopLevel() throws Exception {
        Folder a = folder("A", null);
        folder("B", a);
        Manuscript inA = book("In A", a);

        folderService.delete(a, false);

        assertThat(reload(inA).getFolder()).isNull();
        assertThat(folderService.findChildren(null)).extracting(Folder::getName).containsExactly("B");
    }

    @Test
    void deleteFailsWhenAFolderInItHasTheNameOfOneAbove() throws Exception {
        Folder a = folder("A", null);
        Folder b = folder("B", a);
        folder("Same", b);
        folder("Same", a);
        Manuscript inB = book("In B", b);

        assertThatThrownBy(() -> folderService.delete(b, false)).isInstanceOf(IllegalStateException.class);

        assertThat(folderService.findForUser(b.getUuid())).isNotNull();
        assertThat(reload(inB).getFolder().getId()).isEqualTo(b.getId());
    }

    @Test
    void deleteTakesTheTagsOfTheFolder() throws Exception {
        Folder a = folder("A", null);
        Tag tag = tagService.getOrCreateForUser("series");
        tagRelationService.createRelation(tag, a);
        assertThat(tagRelationService.getTagsForObject(a)).hasSize(1);

        folderService.delete(a, false);

        assertThat(tagRelationService.getTagsForObject(a)).isEmpty();
    }

    @Test
    void deleteWithContentDeletesEverythingBelow() throws Exception {
        Folder a = folder("A", null);
        Folder b = folder("B", a);
        Manuscript inA = book("In A", a);
        Manuscript inB = book("In B", b);
        Manuscript outside = book("Outside", null);
        assertThat(folderService.describeContent(a)).isEqualTo(new Content(1, 2));

        folderService.deleteWithContent(a);

        assertThat(folderService.findForUser(a.getUuid())).isNull();
        assertThat(folderService.findForUser(b.getUuid())).isNull();
        assertThat(manuscriptService.findForUser(inA.getUuid())).isNull();
        assertThat(manuscriptService.findForUser(inB.getUuid())).isNull();
        assertThat(manuscriptService.findForUser(outside.getUuid())).isNotNull();
    }

    @Test
    void booksAreCountedWithTheFoldersBelow() throws Exception {
        Folder a = folder("A", null);
        Folder b = folder("B", a);
        Folder empty = folder("Empty", null);
        book("1", a);
        book("2", b);
        book("3", b);
        book("4", null);
        Manuscript deleted = book("5", b);
        manuscriptService.delete(deleted, false);

        Map<Long, Long> counts = folderService.countBooksByFolder();

        assertThat(counts).containsEntry(a.getId(), 3L).containsEntry(b.getId(), 2L).containsEntry(empty.getId(), 0L);
        assertThat(folderService.describeContent(empty)).isEqualTo(new Content(0, 0));
        assertThat(folderService.describeContent(a)).isEqualTo(new Content(1, 3));
    }
}
