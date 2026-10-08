package com.github.enerccio.marginalia.backup;

import com.github.enerccio.marginalia.domain.collections.AIType;
import com.github.enerccio.marginalia.domain.collections.FilteringMode;
import com.github.enerccio.marginalia.domain.collections.InsertionMode;
import com.github.enerccio.marginalia.domain.collections.ProtocolType;
import com.github.enerccio.marginalia.domain.model.BaseEntity;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.BackupService.BackupStrategy;
import com.github.enerccio.marginalia.test.MarginaliaTestBase;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.function.Consumer;

/**
 * Fixture builders shared by the backup tests: a fully populated manuscript (extended fields, plugin attributes, tags,
 * AI, protocol, lorebook tree, branching story with summaries) and helpers to read a manuscript back.
 */
abstract class BackupTestBase extends MarginaliaTestBase {

    static final Date REQUEST = new Date(1_700_000_000_000L);

    @Autowired
    protected ManuscriptService manuscriptService;

    @Autowired
    protected BackupService backupService;

    @Autowired
    protected ProtocolService protocolService;

    @Autowired
    protected LorebookService lorebookService;

    @Autowired
    protected LorebookEntryService lorebookEntryService;

    @Autowired
    protected ChatMessageService chatMessageService;

    @Autowired
    protected SummaryService summaryService;

    @Autowired
    protected TagService tagService;

    @Autowired
    protected TagRelationService tagRelationService;

    // ------------------------------------------------------------------------------------------------------------
    // builders
    // ------------------------------------------------------------------------------------------------------------

    protected AI ai(String name) throws Exception {
        OpenAICompatible ai = new OpenAICompatible();
        ai.setAiType(AIType.OPEN_AI_COMPATIBLE);
        ai.setName(name);
        ai.setUri(llm.baseUrl());
        ai.setModel("model");
        ai.setEnabledReasoning(false);
        return aiService.save(ai);
    }

    protected Protocol protocol(String name) throws Exception {
        ChatCompletionProtocol protocol = new ChatCompletionProtocol();
        protocol.setName(name);
        protocol.setProtocolType(ProtocolType.CHAT_COMPLETION);
        protocol.setMaxTokens(8000);
        protocol.setReplyTokens(500);
        return protocolService.save(protocol);
    }

    protected Tag tag(String value) throws Exception {
        List<Tag> existing = tagService.searchTagsForUser(value, 0, 50).stream().filter(t -> t.getValue().equals(value)).toList();
        if (!existing.isEmpty()) {
            return existing.getFirst();
        }
        Tag tag = new Tag();
        tag.setValue(value);
        return tagService.save(tag);
    }

    protected Lorebook lorebook(String name, Lorebook... subbooks) throws Exception {
        Lorebook lorebook = new Lorebook();
        lorebook.setName(name);
        lorebook.setSubbooks(new ArrayList<>(List.of(subbooks)));
        return lorebookService.save(lorebook);
    }

    protected LorebookEntry entry(Lorebook lorebook, String name, Consumer<LorebookEntry> setup) throws Exception {
        LorebookEntry entry = new LorebookEntry();
        entry.setLorebook(lorebook);
        entry.setName(name);
        entry.setPayload("payload of " + name);
        setup.accept(entry);
        return lorebookEntryService.save(entry);
    }

    protected Manuscript manuscript(String name, AI ai, Protocol protocol, Lorebook lorebook) throws Exception {
        Manuscript manuscript = new Manuscript();
        manuscript.setName(name);
        manuscript.setDescription("description of " + name);
        manuscript.setAi(ai);
        manuscript.setProtocol(protocol);
        manuscript.setLorebook(lorebook);
        manuscript.setPov("first person");
        manuscript.setTense("present");
        manuscript.setStyle("terse");
        manuscript.setTemplate("{{backgroundLore}}");
        manuscript.setUserPrompt("{{instructions}}");
        manuscript.setSummaryPrompt("summarize");
        manuscript.setShowBookStyles(true);
        manuscript.setBackupStrategy(BackupStrategy.AFTER_N_MESSAGES);
        manuscript.setBackupStrategyValue("5");
        manuscript.getAttributes().addProperty("plugin.flag", "on");
        return manuscriptService.save(manuscript);
    }

    protected ChatMessage message(Manuscript manuscript, ChatMessage parent, String response) throws Exception {
        ChatMessage message = new ChatMessage();
        message.setResponse(response);
        message.setInstructions("instructions for " + response);
        message.setRequest(REQUEST);
        message.setWordCount(response.length());
        message.setTokenCount(response.length() * 2L);
        message.getAttributes().addProperty("chapter", response);
        message = parent == null ? chatMessageService.createRoot(manuscript, message) : chatMessageService.addChild(parent, message);
        Thread.sleep(2);
        return message;
    }

    protected ChatMessage summarize(ChatMessage message, String text) throws Exception {
        Summary summary = new Summary();
        summary.setSummary(text);
        summary.setSummaryTokens(3L);
        summary = summaryService.save(summary);
        ChatMessage loaded = chatMessageService.find(message.getId());
        loaded.setSummary(summary);
        return chatMessageService.save(loaded);
    }

    protected Manuscript setActiveLeaf(Manuscript manuscript, ChatMessage leaf) throws Exception {
        Manuscript loaded = manuscriptService.find(manuscript.getId());
        loaded.setActiveLeaf(leaf);
        return manuscriptService.save(loaded);
    }

    /**
     * root -> chapter 1 -> chapter 2a (active leaf, summarized)
     * \-> chapter 2b (alternative branch)
     */
    protected Manuscript story(Manuscript manuscript) throws Exception {
        ChatMessage root = message(manuscript, null, "prologue");
        ChatMessage one = message(manuscript, root, "chapter 1");
        ChatMessage twoA = message(manuscript, one, "chapter 2a");
        message(manuscript, one, "chapter 2b");
        summarize(twoA, "summary up to 2a");
        ChatMessage edited = chatMessageService.find(one.getId());
        edited.setEdited(true);
        chatMessageService.save(edited);
        return setActiveLeaf(manuscript, twoA);
    }

    // ------------------------------------------------------------------------------------------------------------
    // readers
    // ------------------------------------------------------------------------------------------------------------

    protected Manuscript reload(Manuscript manuscript) throws Exception {
        return manuscriptService.find(manuscript.getId());
    }

    /**
     * Story tree as "response -> parent response" ("" for roots).
     */
    protected Map<String, String> tree(Manuscript manuscript) throws Exception {
        Map<String, String> tree = new TreeMap<>();
        for (ChatMessage message : chatMessageService.getAllMessages(reload(manuscript))) {
            ChatMessage parent = chatMessageService.getParent(message);
            tree.put(message.getResponse(), parent == null ? "" : parent.getResponse());
        }
        return tree;
    }

    protected static Map<String, String> storyTree() {
        return new TreeMap<>(Map.of(
                "prologue", "",
                "chapter 1", "prologue",
                "chapter 2a", "chapter 1",
                "chapter 2b", "chapter 1"));
    }

    protected ChatMessage messageByResponse(Manuscript manuscript, String response) throws Exception {
        return chatMessageService.getAllMessages(reload(manuscript)).stream()
                .filter(m -> response.equals(m.getResponse())).findFirst()
                .orElseThrow(() -> new AssertionError("No message " + response));
    }

    protected String activeLeafResponse(Manuscript manuscript) throws Exception {
        Manuscript loaded = reload(manuscript);
        if (loaded.getActiveLeaf() == null) {
            return null;
        }
        return chatMessageService.find(loaded.getActiveLeaf().getId()).getResponse();
    }

    protected List<String> tagValues(BaseEntity entity) throws Exception {
        return tagRelationService.getTagsForObject(entity).stream().map(Tag::getValue).sorted().toList();
    }

    protected Long idOf(BaseEntity entity) {
        return entity == null ? null : entity.getId();
    }

    protected List<String> lorebookNames() throws Exception {
        return lorebookService.findAllForUser().stream().map(Lorebook::getName).sorted().toList();
    }

    protected static void fillEntry(LorebookEntry entry) {
        entry.setComment("comment");
        entry.setOrder(7);
        entry.setFiltering("dragon");
        entry.setFilteringMode(FilteringMode.REGEX);
        entry.setInsertionMode(InsertionMode.BEFORE_USER_PROMPT);
        entry.setEnabled(false);
    }
}
