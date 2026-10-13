package com.github.enerccio.marginalia.cleanup;

import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.CleanupService.EntityKey;
import com.github.enerccio.marginalia.domain.service.TrashService.RestoreResult;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Soft delete takes the objects that depend on the deleted one with it (manuscript - messages - summaries, lorebook -
 * entries, AI - usages, tags of all of them), and nothing else.
 */
class SoftDeleteCascadeTest extends MarginaliaTestBase {

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

    @Autowired
    private UsageService usageService;

    @Autowired
    private TrashService trashService;

    @Autowired
    private CleanupService cleanupService;

    @BeforeEach
    void loginUser() throws Exception {
        login();
    }

    private Manuscript manuscript() throws Exception {
        Manuscript manuscript = new Manuscript();
        manuscript.setName(uniqueName("book"));
        return manuscriptService.save(manuscript);
    }

    private ChatMessage message(Manuscript manuscript, ChatMessage parent, String text) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(text);
        return parent == null ? chatMessageService.createRoot(manuscript, message) : chatMessageService.addChild(parent, message);
    }

    private Summary summaryOf(ChatMessage message) throws Exception {
        Summary summary = new Summary();
        summary.setSummary("summary of " + message.getResponse());
        summary = summaryService.save(summary);
        message = chatMessageService.find(message.getId());
        message.setSummary(summary);
        chatMessageService.save(message);
        return summary;
    }

    private Tag tag() throws Exception {
        Tag tag = new Tag();
        tag.setValue(uniqueName("tag"));
        return tagService.save(tag);
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

    private boolean deleted(ChatMessage message) throws Exception {
        return chatMessageService.find(message.getId()).isDeleted();
    }

    private boolean deleted(Summary summary) throws Exception {
        return summaryService.find(summary.getId()).isDeleted();
    }

    private boolean deleted(TagRelation relation) throws Exception {
        return tagRelationService.find(relation.getId()).isDeleted();
    }

    @Test
    void manuscriptTakesMessagesSummariesAndTagsWithIt() throws Exception {
        Manuscript book = manuscript();
        Manuscript other = manuscript();
        ChatMessage root = message(book, null, "root");
        ChatMessage child = message(book, root, "child");
        ChatMessage sibling = message(book, root, "sibling");
        Summary rootSummary = summaryOf(root);
        Summary childSummary = summaryOf(child);
        ChatMessage otherRoot = message(other, null, "other root");
        Summary otherSummary = summaryOf(otherRoot);
        Tag tag = tag();
        TagRelation relation = tagRelationService.createRelation(tag, book);
        TagRelation otherRelation = tagRelationService.createRelation(tag, other);

        manuscriptService.delete(manuscriptService.find(book.getId()), false);

        assertThat(manuscriptService.find(book.getId()).isDeleted()).isTrue();
        assertThat(List.of(root, child, sibling)).allMatch(m -> isDeleted(m));
        assertThat(List.of(rootSummary, childSummary)).allMatch(s -> isDeleted(s));
        assertThat(deleted(relation)).isTrue();
        // the extended content of a deleted message is intact (restore needs it)
        assertThat(chatMessageService.find(root.getId()).getResponse()).isEqualTo("root");
        assertThat(summaryService.find(rootSummary.getId()).getSummary()).isEqualTo("summary of root");

        assertThat(manuscriptService.find(other.getId()).isDeleted()).isFalse();
        assertThat(deleted(otherRoot)).isFalse();
        assertThat(isDeleted(otherSummary)).isFalse();
        assertThat(deleted(otherRelation)).isFalse();
        assertThat(tag.isDeleted()).isFalse();
    }

    @Test
    void messageTakesItsSummaryAndEverythingBelowIt() throws Exception {
        Manuscript book = manuscript();
        ChatMessage root = message(book, null, "root");
        ChatMessage middle = message(book, root, "middle");
        ChatMessage leaf = message(book, middle, "leaf");
        ChatMessage sibling = message(book, root, "sibling");
        Summary middleSummary = summaryOf(middle);
        Summary leafSummary = summaryOf(leaf);
        Summary rootSummary = summaryOf(root);

        chatMessageService.delete(chatMessageService.find(middle.getId()), false);

        assertThat(deleted(middle)).isTrue();
        assertThat(deleted(leaf)).isTrue();
        assertThat(isDeleted(middleSummary)).isTrue();
        assertThat(isDeleted(leafSummary)).isTrue();
        assertThat(deleted(root)).isFalse();
        assertThat(deleted(sibling)).isFalse();
        assertThat(isDeleted(rootSummary)).isFalse();
        assertThat(chatMessageService.getAllMessages(book)).extracting(ChatMessage::getId)
                .containsExactlyInAnyOrder(root.getId(), sibling.getId());
    }

    @Test
    void deletingNodeAndMigratingChildrenKeepsTheChildren() throws Exception {
        Manuscript book = manuscript();
        ChatMessage root = message(book, null, "root");
        ChatMessage middle = message(book, root, "middle");
        ChatMessage leaf = message(book, middle, "leaf");
        Summary middleSummary = summaryOf(middle);

        chatMessageService.deleteNodeAndMigrateChildren(chatMessageService.find(middle.getId()), null, false);

        assertThat(deleted(middle)).isTrue();
        assertThat(isDeleted(middleSummary)).isTrue();
        assertThat(deleted(leaf)).isFalse();
        assertThat(chatMessageService.find(leaf.getId()).getParent().getId()).isEqualTo(root.getId());
    }

    @Test
    void hundredsOfMessagesAreDeletedWithTheManuscript() throws Exception {
        Manuscript book = manuscript();
        ChatMessage parent = null;
        List<ChatMessage> all = new ArrayList<>();
        for (int i = 0; i < 1200; i++) {
            parent = message(book, parent, "part " + i);
            all.add(parent);
        }

        manuscriptService.delete(manuscriptService.find(book.getId()), false);

        assertThat(chatMessageService.getAllMessages(book)).isEmpty();
        assertThat(all).allMatch(m -> isDeleted(m));
    }

    @Test
    void lorebookTakesEntriesAndTagsWithIt() throws Exception {
        Lorebook book = lorebook();
        Lorebook other = lorebook();
        LorebookEntry first = entry(book, "first");
        LorebookEntry second = entry(book, "second");
        LorebookEntry otherEntry = entry(other, "other");
        Tag tag = tag();
        TagRelation bookRelation = tagRelationService.createRelation(tag, book);
        TagRelation entryRelation = tagRelationService.createRelation(tag, first);
        TagRelation negativeRelation = tagRelationService.createRelation(tag, second, true);
        TagRelation otherRelation = tagRelationService.createRelation(tag, otherEntry);

        lorebookService.delete(lorebookService.find(book.getId()), false);

        assertThat(lorebookService.find(book.getId()).isDeleted()).isTrue();
        assertThat(lorebookEntryService.find(first.getId()).isDeleted()).isTrue();
        assertThat(lorebookEntryService.find(second.getId()).isDeleted()).isTrue();
        assertThat(deleted(bookRelation)).isTrue();
        assertThat(deleted(entryRelation)).isTrue();
        assertThat(deleted(negativeRelation)).isTrue();
        assertThat(lorebookEntryService.getEntriesForLorebook(book)).isEmpty();

        assertThat(lorebookService.find(other.getId()).isDeleted()).isFalse();
        assertThat(lorebookEntryService.find(otherEntry.getId()).isDeleted()).isFalse();
        assertThat(deleted(otherRelation)).isFalse();
    }

    @Test
    void entryTakesItsTagsWithIt() throws Exception {
        Lorebook book = lorebook();
        LorebookEntry entry = entry(book, "entry");
        LorebookEntry kept = entry(book, "kept");
        Tag tag = tag();
        TagRelation relation = tagRelationService.createRelation(tag, entry);
        TagRelation keptRelation = tagRelationService.createRelation(tag, kept);

        lorebookEntryService.delete(lorebookEntryService.find(entry.getId()), false);

        assertThat(deleted(relation)).isTrue();
        assertThat(deleted(keptRelation)).isFalse();
        assertThat(lorebookService.find(book.getId()).isDeleted()).isFalse();
    }

    @Test
    void aiTakesItsUsagesWithIt() throws Exception {
        AI ai = createAI();
        AI other = createAI();
        usageService.record(ai, 10, 1);
        usageService.record(other, 10, 1);
        Usage usage = usageService.findForUser(ai);
        Usage otherUsage = usageService.findForUser(other);

        aiService.delete(aiService.find(ai.getId()), false);

        assertThat(usageService.find(usage.getId()).isDeleted()).isTrue();
        assertThat(usageService.find(otherUsage.getId()).isDeleted()).isFalse();
        assertThat(aiService.find(other.getId()).isDeleted()).isFalse();

        // the tests share a database and a purge in one of them takes every deleted object: leave nothing behind
        usageService.delete(usageService.find(usage.getId()), true);
        aiService.delete(aiService.find(ai.getId()), true);
    }

    @Test
    void restoringManuscriptBringsBackItsMessagesSummariesAndTags() throws Exception {
        Manuscript book = manuscript();
        ChatMessage root = message(book, null, "root");
        ChatMessage child = message(book, root, "child");
        Summary summary = summaryOf(child);
        TagRelation relation = tagRelationService.createRelation(tag(), book);
        manuscriptService.delete(manuscriptService.find(book.getId()), false);

        RestoreResult result = trashService.restore(List.of(new EntityKey(Manuscript.class, book.getId())));

        assertThat(result.isBlocked()).isFalse();
        assertThat(result.getRestored()).contains(new EntityKey(Manuscript.class, book.getId()),
                new EntityKey(ChatMessage.class, root.getId()), new EntityKey(ChatMessage.class, child.getId()),
                new EntityKey(Summary.class, summary.getId()), new EntityKey(TagRelation.class, relation.getId()));
        assertThat(chatMessageService.getAllMessages(book)).extracting(ChatMessage::getId)
                .containsExactlyInAnyOrder(root.getId(), child.getId());
        assertThat(isDeleted(summary)).isFalse();
        assertThat(deleted(relation)).isFalse();

        // nothing is left for the cleanup to purge
        loginAdmin();
        cleanupService.purge();
        assertThat(chatMessageService.find(child.getId())).isNotNull();
        assertThat(summaryService.find(summary.getId())).isNotNull();
    }

    @Test
    void messageOfDeletedManuscriptIsNotRestoredAlone() throws Exception {
        Manuscript book = manuscript();
        ChatMessage root = message(book, null, "root");
        manuscriptService.delete(manuscriptService.find(book.getId()), false);

        RestoreResult result = trashService.restore(List.of(new EntityKey(ChatMessage.class, root.getId())));

        assertThat(result.isBlocked()).isTrue();
        assertThat(deleted(root)).isTrue();
    }

    @Test
    void cleanupPurgesTheCascadedObjectsWithTheirOwner() throws Exception {
        Manuscript book = manuscript();
        ChatMessage root = message(book, null, "root");
        ChatMessage child = message(book, root, "child");
        Summary summary = summaryOf(child);
        manuscriptService.delete(manuscriptService.find(book.getId()), false);

        loginAdmin();
        cleanupService.purge();

        assertThat(manuscriptService.find(book.getId())).isNull();
        assertThat(chatMessageService.find(root.getId())).isNull();
        assertThat(chatMessageService.find(child.getId())).isNull();
        assertThat(summaryService.find(summary.getId())).isNull();
    }

    @Test
    void hardDeleteDoesNotCascade() throws Exception {
        Lorebook book = lorebook();
        LorebookEntry entry = entry(book, "entry");

        lorebookEntryService.delete(lorebookEntryService.find(entry.getId()), true);

        assertThat(lorebookEntryService.find(entry.getId())).isNull();
        assertThat(lorebookService.find(book.getId()).isDeleted()).isFalse();
    }

    private boolean isDeleted(ChatMessage message) {
        try {
            return deleted(message);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean isDeleted(Summary summary) {
        try {
            return deleted(summary);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
