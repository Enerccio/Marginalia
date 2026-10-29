package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Folder;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Tag;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.ChatMessageService.BranchSize;
import com.github.enerccio.marginalia.domain.service.search.ManuscriptFilterValues;
import com.github.enerccio.marginalia.domain.service.search.Sorter;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the list of books asks of the services: the books and the folders of one folder (or of everything below it), the
 * filters in it, the favorites, and the size of the active branch of a page of books.
 */
class FolderListTest extends MarginaliaTestBase {

    @Autowired
    private FolderService folderService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    private Folder scifi;
    private Folder expanse;
    private Folder fantasy;
    private Manuscript loose;
    private Manuscript inScifi;
    private Manuscript inExpanse;

    @BeforeEach
    void createTree() throws Exception {
        login();
        scifi = folderService.create("Sci-fi", null);
        expanse = folderService.create("Expanse", scifi);
        fantasy = folderService.create("Fantasy", null);
        loose = book("Loose", null);
        inScifi = book("Leviathan", scifi);
        inExpanse = book("Caliban", expanse);
    }

    private Manuscript book(String name, Folder folder) throws Exception {
        Manuscript book = new Manuscript();
        book.setName(name);
        book.setFolder(folder);
        return manuscriptService.save(book);
    }

    private List<Long> books(ManuscriptFilterValues filter) throws Exception {
        return manuscriptService.searchRegularManuscripts(filter, Sorter.sorter("name"));
    }

    private List<String> folders(ManuscriptFilterValues filter) throws Exception {
        return folderService.search(filter, Sorter.sorter("name")).stream().map(Folder::getName).toList();
    }

    @Test
    void theTopLevelListsItsOwnBooksAndFolders() throws Exception {
        ManuscriptFilterValues filter = new ManuscriptFilterValues().inFolder(null, false);

        assertThat(books(filter)).containsExactly(loose.getId());
        assertThat(folders(filter)).containsExactly("Fantasy", "Sci-fi");
    }

    @Test
    void aFolderListsItsOwnContentOnly() throws Exception {
        ManuscriptFilterValues filter = new ManuscriptFilterValues().inFolder(scifi, false);

        assertThat(books(filter)).containsExactly(inScifi.getId());
        assertThat(folders(filter)).containsExactly("Expanse");
    }

    @Test
    void includingSubfoldersListsEverythingBelow() throws Exception {
        ManuscriptFilterValues filter = new ManuscriptFilterValues().inFolder(scifi, true);

        assertThat(books(filter)).containsExactlyInAnyOrder(inScifi.getId(), inExpanse.getId());
        assertThat(folders(filter)).containsExactly("Expanse");

        ManuscriptFilterValues top = new ManuscriptFilterValues().inFolder(null, true);
        assertThat(books(top)).containsExactlyInAnyOrder(loose.getId(), inScifi.getId(), inExpanse.getId());
        assertThat(folders(top)).containsExactly("Expanse", "Fantasy", "Sci-fi");
    }

    @Test
    void withoutAScopeEverythingMatches() throws Exception {
        assertThat(books(new ManuscriptFilterValues())).hasSize(3);
        assertThat(folders(new ManuscriptFilterValues())).hasSize(3);
        assertThat(books(null)).hasSize(3);
    }

    @Test
    void theNameFilterWorksInTheScope() throws Exception {
        ManuscriptFilterValues filter = new ManuscriptFilterValues().inFolder(scifi, true);
        filter.setName("Cal");
        assertThat(books(filter)).containsExactly(inExpanse.getId());

        filter.setName("Exp");
        assertThat(folders(filter)).containsExactly("Expanse");
        filter.setName("Fan");
        assertThat(folders(filter)).isEmpty();
    }

    @Test
    void foldersAreFilteredByTheirTags() throws Exception {
        Tag tag = tagService.getOrCreateForUser("space");
        tagRelationService.createRelation(tag, expanse);

        ManuscriptFilterValues filter = new ManuscriptFilterValues().inFolder(null, true);
        filter.setTags(List.of("space"));
        assertThat(folders(filter)).containsExactly("Expanse");

        filter.setTags(List.of("other"));
        assertThat(folders(filter)).isEmpty();
    }

    @Test
    void foldersAreSortedByTheSorters() throws Exception {
        List<String> descending = folderService.search(new ManuscriptFilterValues().inFolder(null, false),
                        Sorter.sorter("name", Sorter.Ordering.DESC)).stream().map(Folder::getName).toList();
        assertThat(descending).containsExactly("Sci-fi", "Fantasy");
    }

    @Test
    void aFolderThatIsGoneMatchesNothing() throws Exception {
        Folder gone = folderService.create("Gone", null);
        folderService.delete(gone, false);

        ManuscriptFilterValues filter = new ManuscriptFilterValues().inFolder(gone, false);
        assertThat(books(filter)).isEmpty();
        assertThat(folders(filter)).isEmpty();
    }

    @Test
    void theFavoritesAreFilteredByUuid() throws Exception {
        ManuscriptFilterValues filter = new ManuscriptFilterValues();
        filter.setUuids(List.of(inExpanse.getUuid(), loose.getUuid()));
        assertThat(books(filter)).containsExactlyInAnyOrder(inExpanse.getId(), loose.getId());

        filter.setName("Cal");
        assertThat(books(filter)).containsExactly(inExpanse.getId());

        filter.setUuids(List.of());
        assertThat(books(filter)).isEmpty();
    }

    @Test
    void anotherUsersFoldersAreNotListed() throws Exception {
        loginAs(createUser());
        assertThat(folders(new ManuscriptFilterValues().inFolder(null, true))).isEmpty();
        assertThat(books(new ManuscriptFilterValues().inFolder(null, true))).isEmpty();
    }

    @Test
    void favoritesAreMarkedPerUserAndKeepTheOrderTheyWereMarkedIn() throws Exception {
        manuscriptService.setFavorite(inExpanse, true);
        manuscriptService.setFavorite(loose, true);
        manuscriptService.setFavorite(loose, true);

        assertThat(manuscriptService.getFavorites()).containsExactly(inExpanse.getUuid(), loose.getUuid());

        manuscriptService.setFavorite(inExpanse, false);
        assertThat(manuscriptService.getFavorites()).containsExactly(loose.getUuid());

        loginAs(createUser());
        assertThat(manuscriptService.getFavorites()).isEmpty();
    }

    @Test
    void aFavoriteThatIsGoneIsForgotten() throws Exception {
        manuscriptService.setFavorite(inExpanse, true);
        manuscriptService.setFavorite(loose, true);

        manuscriptService.delete(manuscriptService.find(loose.getId()), false);

        assertThat(manuscriptService.getFavorites()).containsExactly(inExpanse.getUuid());
    }

    @Test
    void markingAFavoriteDoesNotChangeTheBook() throws Exception {
        java.util.Date before = manuscriptService.find(loose.getId()).getModification();

        manuscriptService.setFavorite(loose, true);

        assertThat(manuscriptService.find(loose.getId()).getModification()).isEqualTo(before);
    }

    private ChatMessage part(Manuscript book, ChatMessage parent, String text, int words, long tokens) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        message.setWordCount(words);
        message.setTokenCount(tokens);
        return parent == null ? chatMessageService.createRoot(book, message) : chatMessageService.addChild(parent, message);
    }

    @Test
    void theBranchSizeSumsTheActiveBranchOnly() throws Exception {
        ChatMessage root = part(inScifi, null, "root", 10, 100);
        ChatMessage child = part(inScifi, root, "child", 20, 200);
        ChatMessage sibling = part(inScifi, root, "sibling", 5, 50);
        Manuscript stored = manuscriptService.find(inScifi.getId());
        stored.setActiveLeaf(child);
        manuscriptService.save(stored);

        Map<Long, BranchSize> sizes = chatMessageService.getActiveBranchSizes(List.of(inScifi.getId(), loose.getId()));

        assertThat(sizes.get(inScifi.getId())).isEqualTo(new BranchSize(30, 300));
        assertThat(sizes.get(loose.getId())).isEqualTo(new BranchSize(0, 0));

        stored = manuscriptService.find(inScifi.getId());
        stored.setActiveLeaf(sibling);
        manuscriptService.save(stored);
        assertThat(chatMessageService.getActiveBranchSizes(List.of(inScifi.getId())).get(inScifi.getId()))
                .isEqualTo(new BranchSize(15, 150));
    }

    @Test
    void deletedPartsAreNotCountedInTheBranchSize() throws Exception {
        ChatMessage root = part(inScifi, null, "root", 10, 100);
        ChatMessage child = part(inScifi, root, "child", 20, 200);
        Manuscript stored = manuscriptService.find(inScifi.getId());
        stored.setActiveLeaf(child);
        manuscriptService.save(stored);

        chatMessageService.delete(chatMessageService.find(child.getId()), false);

        // the leaf itself is gone, so nothing of the branch is read
        assertThat(chatMessageService.getActiveBranchSizes(List.of(inScifi.getId())).get(inScifi.getId()).words()).isLessThanOrEqualTo(10);
    }
}
