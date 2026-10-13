package com.github.enerccio.marginalia.cleanup;

import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.CleanupService.CleanupContributor;
import com.github.enerccio.marginalia.domain.service.CleanupService.EntityKey;
import com.github.enerccio.marginalia.domain.service.TrashService.DeleteResult;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link TrashService#deletePermanently}: story parts and lorebook entries can be removed from the trash for good,
 * the parts below a removed part move to its parent like they do when it is deleted in the editor.
 */
class PermanentDeleteTest extends MarginaliaTestBase {

    @Autowired
    private TrashService trashService;

    @Autowired
    private CleanupService cleanupService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private LorebookService lorebookService;

    @Autowired
    private LorebookEntryService lorebookEntryService;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagRelationService tagRelationService;

    private final List<CleanupContributor> contributors = new ArrayList<>();
    private Manuscript book;

    @BeforeEach
    void setUp() throws Exception {
        login();
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        book = manuscriptService.save(manuscript);
    }

    @AfterEach
    void unregisterContributors() {
        contributors.forEach(cleanupService::unregisterContributor);
    }

    private ChatMessage message(ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        return parent == null ? chatMessageService.createRoot(book, message) : chatMessageService.addChild(parent, message);
    }

    private void softDelete(ChatMessage message) throws Exception {
        chatMessageService.delete(chatMessageService.find(message.getId()), false);
    }

    private static EntityKey key(ChatMessage message) {
        return new EntityKey(ChatMessage.class, message.getId());
    }

    private Long parentId(ChatMessage message) throws Exception {
        ChatMessage parent = chatMessageService.find(message.getId()).getParent();
        return parent != null ? parent.getId() : null;
    }

    private Lorebook lorebook() throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName(uniqueName("lore"));
        lorebook.setSubbooks(new ArrayList<>());
        return lorebookService.save(lorebook);
    }

    private LorebookEntry entry(Lorebook lorebook, String name) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        return lorebookEntryService.save(entry);
    }

    @Test
    void summaryGoesAndTheActiveLeafMovesToTheParent() throws Exception {
        ChatMessage root = message(null, "root");
        ChatMessage middle = message(root, "middle");
        Summary summary = new Summary();
        summary.setSummary("middle summary");
        summary = summaryService.save(summary);
        ChatMessage stored = chatMessageService.find(middle.getId());
        stored.setSummary(summary);
        chatMessageService.save(stored);
        softDelete(middle);
        // the active leaf of a book on a deleted part, as a book that was deleted and restored may have
        Manuscript manuscript = manuscriptService.find(book.getId());
        manuscript.setActiveLeaf(chatMessageService.find(middle.getId()));
        manuscriptService.save(manuscript);

        DeleteResult result = trashService.deletePermanently(List.of(key(middle)));

        assertThat(result.isBlocked()).isFalse();
        assertThat(result.getDeleted()).containsExactlyInAnyOrder(key(middle), new EntityKey(Summary.class, summary.getId()));
        assertThat(chatMessageService.find(middle.getId())).isNull();
        assertThat(summaryService.find(summary.getId())).isNull();
        assertThat(chatMessageService.find(root.getId())).isNotNull();
        assertThat(manuscriptService.find(book.getId()).getActiveLeaf().getId()).isEqualTo(root.getId());
    }

    @Test
    void childrenOfADeletedPartMoveToItsParentWhateverTheirState() throws Exception {
        ChatMessage root = message(null, "root");
        ChatMessage middle = message(root, "middle");
        ChatMessage liveChild = message(middle, "live child");
        ChatMessage deletedChild = message(middle, "deleted child");
        softDelete(deletedChild);
        // the soft delete of a part takes the parts below it with it, and none of them is selected here
        softDelete(middle);
        assertThat(chatMessageService.find(liveChild.getId()).isDeleted()).isTrue();

        DeleteResult result = trashService.deletePermanently(List.of(key(middle)));

        assertThat(result.isBlocked()).isFalse();
        assertThat(chatMessageService.find(middle.getId())).isNull();
        // the children are not deleted with the part, they hang under its parent now
        assertThat(parentId(liveChild)).isEqualTo(root.getId());
        assertThat(parentId(deletedChild)).isEqualTo(root.getId());
        assertThat(chatMessageService.find(liveChild.getId())).isNotNull();
        assertThat(chatMessageService.find(deletedChild.getId())).isNotNull();
    }

    @Test
    void chainOfSelectedPartsMovesChildrenToTheNearestPartThatStays() throws Exception {
        ChatMessage root = message(null, "root");
        ChatMessage a = message(root, "a");
        ChatMessage b = message(a, "b");
        ChatMessage leaf = message(b, "leaf");
        softDelete(a);
        softDelete(b);
        // the leaf was deleted with b but is not selected: it stays
        reviveLeaf(leaf);

        DeleteResult result = trashService.deletePermanently(List.of(key(a), key(b)));

        assertThat(result.isBlocked()).isFalse();
        assertThat(chatMessageService.find(a.getId())).isNull();
        assertThat(chatMessageService.find(b.getId())).isNull();
        assertThat(parentId(leaf)).isEqualTo(root.getId());
    }

    private void reviveLeaf(ChatMessage leaf) throws Exception {
        ChatMessage stored = chatMessageService.find(leaf.getId());
        stored.setDeleted(false);
        chatMessageService.save(stored);
    }

    @Test
    void messagesThatAreNotDeletedAreSkipped() throws Exception {
        ChatMessage root = message(null, "root");
        ChatMessage child = message(root, "child");

        DeleteResult result = trashService.deletePermanently(List.of(key(root)));

        assertThat(result.getDeleted()).isEmpty();
        assertThat(chatMessageService.find(root.getId())).isNotNull();
        assertThat(parentId(child)).isEqualTo(root.getId());
    }

    @Test
    void onlyPartsAndEntriesCanBeDeletedPermanently() throws Exception {
        Lorebook lorebook = lorebook();
        lorebookService.delete(lorebookService.find(lorebook.getId()), false);

        assertThat(trashService.getPermanentlyDeletableTypes()).containsExactlyInAnyOrder(ChatMessage.class, LorebookEntry.class);
        assertThatThrownBy(() -> trashService.deletePermanently(List.of(new EntityKey(Lorebook.class, lorebook.getId()))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> trashService.deletePermanently(List.of(new EntityKey(Manuscript.class, book.getId()))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(lorebookService.find(lorebook.getId())).isNotNull();
    }

    @Test
    void entryIsRemovedWithItsTagLinks() throws Exception {
        Lorebook lorebook = lorebook();
        LorebookEntry entry = entry(lorebook, "entry");
        LorebookEntry kept = entry(lorebook, "kept");
        Tag tag = new Tag();
        tag.setValue(uniqueName("tag"));
        tag = tagService.save(tag);
        TagRelation relation = tagRelationService.createRelation(tag, entry);
        TagRelation keptRelation = tagRelationService.createRelation(tag, kept);
        lorebookEntryService.delete(lorebookEntryService.find(entry.getId()), false);

        DeleteResult result = trashService.deletePermanently(List.of(new EntityKey(LorebookEntry.class, entry.getId())));

        assertThat(result.isBlocked()).isFalse();
        assertThat(lorebookEntryService.find(entry.getId())).isNull();
        assertThat(tagRelationService.find(relation.getId())).isNull();
        assertThat(lorebookEntryService.find(kept.getId()).isDeleted()).isFalse();
        assertThat(tagRelationService.find(keptRelation.getId()).isDeleted()).isFalse();
        assertThat(lorebookService.find(lorebook.getId()).isDeleted()).isFalse();
    }

    @Test
    void userDeletesOnlyOwnObjects() throws Exception {
        ChatMessage root = message(null, "root");
        softDelete(root);
        User other = createUser();
        loginAs(other);

        assertThatThrownBy(() -> trashService.deletePermanently(List.of(key(root)))).isInstanceOf(SecurityException.class);
        assertThat(chatMessageService.find(root.getId())).isNotNull();
    }

    @Test
    void blockedDeleteChangesNothing() throws Exception {
        ChatMessage root = message(null, "root");
        ChatMessage middle = message(root, "middle");
        ChatMessage child = message(middle, "child");
        chatMessageService.deleteNodeAndMigrateChildren(chatMessageService.find(middle.getId()), null, false);
        // the child moved to root, move it back below the deleted part to see that the failed delete moves nothing
        ChatMessage stored = chatMessageService.find(child.getId());
        stored.setParent(chatMessageService.find(middle.getId()));
        chatMessageService.save(stored);
        EntityKey middleKey = key(middle);
        CleanupContributor contributor = new CleanupContributor() {
            @Override
            public void collectReferences(Set<EntityKey> candidates, CleanupService.BlockSink sink) {
                sink.block(middleKey, "kept by extension");
            }
        };
        cleanupService.registerContributor(contributor);
        contributors.add(contributor);

        DeleteResult result = trashService.deletePermanently(List.of(middleKey));

        assertThat(result.isBlocked()).isTrue();
        assertThat(result.getBlocked().get(middleKey)).contains("kept by extension");
        assertThat(result.getDeleted()).isEmpty();
        assertThat(chatMessageService.find(middle.getId())).isNotNull();
        assertThat(parentId(child)).isEqualTo(middle.getId());
    }
}
