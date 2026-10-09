package com.github.enerccio.marginalia.crud;

import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ManuscriptService;
import com.github.enerccio.marginalia.domain.service.OwnedService;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class ChatMessageCrudTest extends ExtendableCrudContract<ChatMessage> {

    private static final Date REQUEST = new Date(1_700_000_000_000L);
    private static final Date TTFT = new Date(1_700_000_001_500L);

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private SummaryService summaryService;

    private Manuscript manuscript;

    @BeforeEach
    void createManuscript() throws Exception {
        Manuscript m = new Manuscript();
        m.setName("book");
        manuscript = manuscriptService.save(m);
    }

    @Override
    protected OwnedService<ChatMessage, ?> service() {
        return chatMessageService;
    }

    @Override
    protected ChatMessage newEntity() {
        ChatMessage message = new ChatMessage();
        message.setParentScript(manuscript);
        message.setInstructions("Continue the story");
        message.setSceneSetting("A tavern");
        message.setPovCharacter("Alice");
        message.setPresentCharacters("Alice, Bob");
        message.setResponse("It was a dark night.");
        message.setResponseReasoning("thinking");
        message.setTokenCount(7);
        message.setWordCount(5);
        message.setPromptTokens(1200L);
        message.setTokenReasoningCount(30L);
        message.setRequest(REQUEST);
        message.setTtft(TTFT);
        message.setModelUsed("model-a");
        message.setProtocolUsed("protocol-a");
        message.setBuiltPrompt("[system] ...");
        message.setBuiltPromptTokens(1100L);
        message.setScrollPosition(250);
        message.setBackgroundLore("lore");
        return message;
    }

    @Override
    protected void assertCreated(ChatMessage loaded) {
        assertThat(idOf(loaded.getParentScript())).isEqualTo(manuscript.getId());
        assertThat(loaded.getParent()).isNull();
        assertThat(loaded.getInstructions()).isEqualTo("Continue the story");
        assertThat(loaded.getSceneSetting()).isEqualTo("A tavern");
        assertThat(loaded.getPovCharacter()).isEqualTo("Alice");
        assertThat(loaded.getPresentCharacters()).isEqualTo("Alice, Bob");
        assertThat(loaded.getResponse()).isEqualTo("It was a dark night.");
        assertThat(loaded.getResponseReasoning()).isEqualTo("thinking");
        assertThat(loaded.getTokenCount()).isEqualTo(7);
        assertThat(loaded.getWordCount()).isEqualTo(5);
        assertThat(loaded.getPromptTokens()).isEqualTo(1200L);
        assertThat(loaded.getTokenReasoningCount()).isEqualTo(30L);
        assertThat(loaded.getRequest()).isEqualTo(REQUEST);
        assertThat(loaded.getTtft()).isEqualTo(TTFT);
        assertThat(loaded.getReasoningEnd()).isNull();
        assertThat(loaded.getModelUsed()).isEqualTo("model-a");
        assertThat(loaded.getProtocolUsed()).isEqualTo("protocol-a");
        assertThat(loaded.getBuiltPrompt()).isEqualTo("[system] ...");
        assertThat(loaded.getBuiltPromptTokens()).isEqualTo(1100L);
        assertThat(loaded.getScrollPosition()).isEqualTo(250);
        assertThat(loaded.getBackgroundLore()).isEqualTo("lore");
        assertThat(loaded.isEdited()).isFalse();
    }

    @Override
    protected void modify(ChatMessage entity) {
        entity.setResponse("It was a bright morning.");
        entity.setEdited(true);
        entity.setWordCount(4);
        entity.setScrollPosition(null);
    }

    @Override
    protected void assertModified(ChatMessage loaded) {
        assertThat(loaded.getResponse()).isEqualTo("It was a bright morning.");
        assertThat(loaded.isEdited()).isTrue();
        assertThat(loaded.getWordCount()).isEqualTo(4);
        assertThat(loaded.getScrollPosition()).isNull();
        assertThat(loaded.getInstructions()).isEqualTo("Continue the story");
    }

    private ChatMessage message(String response, int words) {
        ChatMessage message = new ChatMessage();
        message.setResponse(response);
        message.setWordCount(words);
        message.setTokenCount(words * 2L);
        return message;
    }

    @Test
    void buildsTreeAndBranch() throws Exception {
        ChatMessage root = chatMessageService.createRoot(manuscript, message("root", 1));
        ChatMessage child = chatMessageService.addChild(root, message("child", 2));
        ChatMessage leaf = chatMessageService.addChild(child, message("leaf", 3));

        assertThat(idOf(chatMessageService.getParent(leaf))).isEqualTo(child.getId());
        assertThat(chatMessageService.getParent(root)).isNull();
        assertThat(idOf(reload(leaf).getParentScript())).isEqualTo(manuscript.getId());
        assertThat(chatMessageService.getBranchFromLeaf(leaf)).extracting(BaseEntity::getId)
                .containsExactly(root.getId(), child.getId(), leaf.getId());
        assertThat(chatMessageService.getBranchWordCount(leaf)).isEqualTo(6);
        assertThat(chatMessageService.getBranchTokenCount(leaf)).isEqualTo(12);
        assertThat(chatMessageService.getTotalWordCount(manuscript)).isEqualTo(6);
        assertThat(chatMessageService.getTotalTokenCount(manuscript)).isEqualTo(12);
        assertThat(chatMessageService.hasAnyMessages(manuscript)).isTrue();
        assertThat(chatMessageService.getAllMessages(manuscript)).hasSize(3);
    }

    @Test
    void emptyManuscriptHasNoMessages() throws Exception {
        assertThat(chatMessageService.hasAnyMessages(manuscript)).isFalse();
        assertThat(chatMessageService.getAllMessages(manuscript)).isEmpty();
        assertThat(chatMessageService.getTotalWordCount(manuscript)).isZero();
        assertThat(chatMessageService.getBranchFromLeaf(null)).isEmpty();
    }

    @Test
    void swipesAreSiblings() throws Exception {
        ChatMessage root = chatMessageService.createRoot(manuscript, message("root", 1));
        ChatMessage first = chatMessageService.addChild(root, message("first", 1));
        Thread.sleep(5);
        ChatMessage second = chatMessageService.addChild(root, message("second", 1));
        ChatMessage deep = chatMessageService.addChild(first, message("deep", 1));

        assertThat(chatMessageService.getSwipesForMessage(reload(first))).extracting(BaseEntity::getId)
                .containsExactly(first.getId(), second.getId());
        assertThat(chatMessageService.getSwipesForMessage(reload(root))).extracting(BaseEntity::getId)
                .containsExactly(root.getId());

        Manuscript m = manuscriptService.find(manuscript.getId());
        assertThat(idOf(chatMessageService.swipeTo(m, reload(first)))).isEqualTo(deep.getId());
        assertThat(idOf(chatMessageService.swipeTo(m, reload(second)))).isEqualTo(second.getId());
        assertThat(idOf(m.getActiveLeaf())).isEqualTo(second.getId());
    }

    @Test
    void activeLeafIsPersistedOnManuscript() throws Exception {
        ChatMessage root = chatMessageService.createRoot(manuscript, message("root", 1));
        Manuscript m = manuscriptService.find(manuscript.getId());
        m.setActiveLeaf(root);
        manuscriptService.save(m);

        assertThat(idOf(manuscriptService.find(manuscript.getId()).getActiveLeaf())).isEqualTo(root.getId());
    }

    @Test
    void branchCopiesMessageAndSummary() throws Exception {
        ChatMessage root = chatMessageService.createRoot(manuscript, message("root", 1));
        Summary summary = new Summary();
        summary.setSummary("so far");
        summary.setSummaryTokens(3L);
        summary = summaryService.save(summary);
        ChatMessage original = newEntity();
        original.setSummary(summary);
        original = chatMessageService.addChild(root, original);

        ChatMessage clone = chatMessageService.branch(manuscriptService.find(manuscript.getId()), reload(original));

        assertThat(clone.getId()).isNotEqualTo(original.getId());
        ChatMessage loaded = reload(clone);
        assertThat(idOf(loaded.getParent())).isEqualTo(root.getId());
        assertThat(loaded.getResponse()).isEqualTo("It was a dark night.");
        assertThat(loaded.getRequest()).isEqualTo(REQUEST);
        assertThat(loaded.getSummary()).isNotNull();
        assertThat(loaded.getSummary().getId()).isNotEqualTo(summary.getId());
        assertThat(loaded.getSummary().getSummary()).isEqualTo("so far");
        assertThat(idOf(reload(original).getSummary())).isEqualTo(summary.getId());
    }

    @Test
    void branchWithoutSummary() throws Exception {
        ChatMessage root = chatMessageService.createRoot(manuscript, message("root", 1));

        ChatMessage clone = chatMessageService.branch(manuscript, reload(root));

        assertThat(reload(clone).getSummary()).isNull();
        assertThat(reload(clone).getParent()).isNull();
    }

    @Test
    void deleteNodeMovesChildrenToParent() throws Exception {
        ChatMessage root = chatMessageService.createRoot(manuscript, message("root", 1));
        ChatMessage middle = chatMessageService.addChild(root, message("middle", 1));
        ChatMessage leaf1 = chatMessageService.addChild(middle, message("leaf1", 1));
        ChatMessage leaf2 = chatMessageService.addChild(middle, message("leaf2", 1));
        Manuscript m = manuscriptService.find(manuscript.getId());
        m.setActiveLeaf(middle);

        chatMessageService.deleteNodeAndMigrateChildren(reload(middle), m, false);

        assertThat(reload(middle).isDeleted()).isTrue();
        assertThat(idOf(reload(leaf1).getParent())).isEqualTo(root.getId());
        assertThat(idOf(reload(leaf2).getParent())).isEqualTo(root.getId());
        assertThat(idOf(m.getActiveLeaf())).isEqualTo(root.getId());
        assertThat(chatMessageService.getBranchFromLeaf(leaf1)).extracting(BaseEntity::getId)
                .containsExactly(root.getId(), leaf1.getId());
    }

    @Test
    void deletingActiveLeafSavesTheBook() throws Exception {
        ChatMessage a = chatMessageService.createRoot(manuscript, message("A", 1));
        ChatMessage b = chatMessageService.addChild(a, message("B", 1));
        Manuscript m = manuscriptService.find(manuscript.getId());
        m.setActiveLeaf(b);
        m = manuscriptService.save(m);

        // the way the story editor deletes a part: on its own copy of the book, then reloads the book
        chatMessageService.deleteNodeAndMigrateChildren(reload(b), m, false);

        Manuscript reloaded = manuscriptService.find(manuscript.getId());
        assertThat(idOf(reloaded.getActiveLeaf())).isEqualTo(a.getId());
        assertThat(chatMessageService.getBranchFromLeaf(reloaded.getActiveLeaf())).extracting(BaseEntity::getId)
                .containsExactly(a.getId());
    }

    @Test
    void hardDeleteNodeRemovesIt() throws Exception {
        ChatMessage root = chatMessageService.createRoot(manuscript, message("root", 1));
        ChatMessage leaf = chatMessageService.addChild(root, message("leaf", 1));

        chatMessageService.deleteNodeAndMigrateChildren(reload(root), null, true);

        assertThat(reload(root)).isNull();
        assertThat(reload(leaf).getParent()).isNull();
        assertThat(chatMessageService.getAllMessages(manuscript)).extracting(BaseEntity::getId).containsExactly(leaf.getId());
    }

    @Test
    void deletedMessagesAreNotCounted() throws Exception {
        ChatMessage root = chatMessageService.createRoot(manuscript, message("root", 10));
        ChatMessage child = chatMessageService.addChild(root, message("child", 5));

        chatMessageService.delete(reload(child), false);

        assertThat(chatMessageService.getTotalWordCount(manuscript)).isEqualTo(10);
        assertThat(chatMessageService.getAllMessages(manuscript)).extracting(BaseEntity::getId).containsExactly(root.getId());
        assertThat(chatMessageService.getSwipesForMessage(reload(root))).extracting(BaseEntity::getId).containsExactly(root.getId());
    }

    @Test
    void countsWords() {
        assertThat(chatMessageService.countWords("Hello, world! It's 2 o'clock")).isEqualTo(7);
        assertThat(chatMessageService.countWords("Příliš žluťoučký kůň")).isEqualTo(3);
        assertThat(chatMessageService.countWords("  ")).isZero();
    }
}
