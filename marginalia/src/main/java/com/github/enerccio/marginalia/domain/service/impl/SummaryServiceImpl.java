package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Constants;
import com.github.enerccio.marginalia.domain.collections.SummaryType;
import com.github.enerccio.marginalia.domain.components.SummaryNode;
import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.listener.ExtendableEntityListener;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.repository.SummaryRepository;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.InferenceService.ChunkType;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncCallback;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncController;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.templates.MetaSummaryTemplateData;
import com.github.enerccio.marginalia.domain.templates.SummaryTemplateData;
import com.github.enerccio.marginalia.domain.templates.TemplateContext;
import com.github.enerccio.marginalia.domain.templates.TemplateVariables;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

public class SummaryServiceImpl extends ExtendableServiceImpl<Summary, SummaryRepository> implements SummaryService {

    private static final Logger log = LoggerFactory.getLogger(SummaryServiceImpl.class);

    private final ExtendableEntityListener extendableListener = new ExtendableEntityListener();

    @Autowired
    @Lazy
    private SummaryService self;

    @Autowired
    private MessagePromptService messagePromptService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    @Lazy
    private AgentRunService agentRunService;

    @Override
    @CommonTx
    public CancellationToken createSummary(Manuscript manuscript, ChatMessage from, AsyncCallback callback) throws Exception {
        return createSummary(manuscript, from, null, callback);
    }

    @Override
    @CommonTx
    public CancellationToken createMetaSummary(Manuscript manuscript, ChatMessage from, ChatMessage to, AsyncCallback callback) throws Exception {
        if (to == null)
            return null;
        return createSummary(manuscript, from, to, callback);
    }

    /**
     * @param to null for summary of the messages, otherwise the oldest message of the range the meta summary merges
     */
    private CancellationToken createSummary(Manuscript manuscript, ChatMessage from, ChatMessage to, AsyncCallback callback) throws Exception {
        if (manuscript == null || from == null || callback == null || manuscript.getAi() == null)
            return null;
        agentRunService.checkNotLocked(manuscript);

        AI ai = aiService.find(manuscript.getAi());
        Protocol protocol = protocolService.find(manuscript.getProtocol());
        InferenceService inferenceService = inferenceServices.forAIOrThrow(ai);
        if (inferenceService == null)
            return null;
        
        CancellationToken cancellationToken = new CancellationToken();
        Summary newSummary = new Summary();
        newSummary.setReasoning("");
        newSummary.setSummary("");
        newSummary.setReasoningTokens(0L);
        newSummary.setSummaryTokens(0L);

        List<LLMChatMessage> payload = to == null
                ? createSummaryPayload(manuscript, from, ai, protocol, inferenceService, newSummary)
                : createMetaSummaryPayload(manuscript, from, to, ai, protocol, inferenceService, newSummary);
        if (payload == null)
            return null;

        newSummary = save(newSummary);
        Summary finalSummary = newSummary;
        InferenceAsyncCallback inferenceCallback = new InferenceAsyncCallback() {

            Summary summary = finalSummary;
            long lastSave = System.currentTimeMillis();
            final RequestAttributes attributes = ThreadCopyRequestAttributes.create();

            @Override
            public void onChunk(InferenceAsyncController controller, ChunkType chunkType, String text) throws Exception {
                try (InRequestScope _ = new InRequestScope(attributes)) {
                    if (chunkType == ChunkType.REASONING) {
                        summary.setReasoning(summary.getReasoning() + text);
                        summary.setReasoningTokens(inferenceService.countTokensApprox(summary.getReasoning()));
                    } else {
                        summary.setSummary(summary.getSummary() + text);
                        summary.setSummaryTokens(inferenceService.countTokensApprox(summary.getSummary()));
                    }
                    long now = System.currentTimeMillis();
                    if (now - Constants.WRITE_TIMEOUT > lastSave) {
                        lastSave = now;
                        summary = self.save(summary);
                    }
                    callback.onSummaryProgress(summary.getReasoning(), summary.getSummary());
                    controller.continueInference();
                }
            }

            @Override
            public void onCompletion() throws Exception {
                try (InRequestScope _ = new InRequestScope(attributes)) {
                    summary.setSummaryTokens(inferenceService.countTokens(summary.getSummary()));
                    summary.setReasoningTokens(inferenceService.countTokens(summary.getReasoning()));
                    summary = self.save(summary);
                    if (to == null) {
                        ChatMessage message = chatMessageService.find(from);
                        message.setSummary(summary);
                        chatMessageService.save(message);
                    } else {
                        summary = self.attachMetaSummary(summary, from);
                    }
                    callback.onSummaryFinished(summary);
                }
            }

            @Override
            public void onCancel() throws Exception {
                try (InRequestScope _ = new InRequestScope(attributes)) {
                    self.delete(summary, true);
                    callback.onSummaryTerminated();
                }
            }

            @Override
            public void onError(Throwable exception) throws Exception {
                try (InRequestScope _ = new InRequestScope(attributes)) {
                    self.delete(summary, true);
                    callback.onError(exception);
                }
            }

            @Override
            public boolean isDead() {
                return cancellationToken.isCancelled();
            }
        };

        // the summary row is inserted by this transaction: the inference runs on another thread, which saves the summary
        // as soon as the model answers, so it must not start before the commit makes the row visible to it
        Runnable start = () -> {
            try (InferenceSession _ = InferenceSession.open(InferenceSession.SUMMARY); UsageContext _ = UsageContext.open(manuscript)) {
                inferenceService.stream(payload, protocol, inferenceCallback);
            } catch (Exception e) {
                log.error("Failed to start the summary inference", e);
                try {
                    inferenceCallback.onError(e);
                } catch (Exception ex) {
                    log.error("Failed to report the summary error", ex);
                }
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    start.run();
                }
            });
        } else {
            start.run();
        }

        return cancellationToken;
    }

    @Override
    @CommonTx
    public Summary copySummary(Summary summary) throws Exception {
        if (summary == null) return null;
        Summary copy = new Summary();
        copy.setReasoning(summary.getReasoning());
        copy.setReasoningTokens(summary.getReasoningTokens());
        copy.setSummary(summary.getSummary());
        copy.setSummaryTokens(summary.getSummaryTokens());
        copy.setSummaryMessageHash(summary.getSummaryMessageHash());
        copy.setSummaryType(summary.getSummaryType());
        copy.setNextSummaryUuid(summary.getNextSummaryUuid());
        copy.setReplacedSummary(summary.getReplacedSummary());
        return save(copy);
    }

    @Override
    @CommonTx
    public Summary attachMetaSummary(Summary metaSummary, ChatMessage message) throws Exception {
        message = chatMessageService.find(message);
        agentRunService.checkNotLocked(message.getParentScript());
        Summary replaced = message.getSummary() == null ? null : find(message.getSummary());
        if (replaced != null) {
            extendableListener.serialize(replaced);
            metaSummary.setReplacedSummary(new String(replaced.getExtendedContent(), StandardCharsets.UTF_8));
            swapUuid(replaced, metaSummary);
            replaced = save(replaced);
        }
        metaSummary = save(metaSummary);
        message.setSummary(metaSummary);
        chatMessageService.save(message);
        if (replaced != null) {
            delete(replaced, true);
        }
        return metaSummary;
    }

    @Override
    @CommonTx
    public void softDeleteForManuscript(Manuscript manuscript) throws Exception {
        getRepository().softDeleteByManuscript(manuscript);
    }

    @Override
    @CommonTx
    public void softDeleteForMessages(Collection<Long> messageIds) throws Exception {
        getRepository().softDeleteByMessageIds(messageIds);
    }

    @Override
    @CommonTx
    public ChatMessage removeSummary(ChatMessage message, boolean unwind) throws Exception {
        message = chatMessageService.find(message);
        if (message == null || message.getSummary() == null) {
            return message;
        }
        agentRunService.checkNotLocked(message.getParentScript());
        Summary summary = find(message.getSummary());
        Summary restored = null;
        if (unwind && summary != null && summary.getSummaryType() == SummaryType.META_SUMMARY
                && StringUtils.isNotBlank(summary.getReplacedSummary())) {
            restored = new Summary();
            restored.setExtendedContent(summary.getReplacedSummary().getBytes(StandardCharsets.UTF_8));
            extendableListener.deserialize(restored);
        }

        message.setSummary(null);
        message = chatMessageService.save(message);
        if (summary == null) {
            return message;
        }
        if (restored != null) {
            swapUuid(summary, restored);
            summary = save(summary);
            restored = save(restored);
            message.setSummary(restored);
            message = chatMessageService.save(message);
        }
        delete(summary, true);
        return message;
    }

    /**
     * Gives uuid of the summary that goes away to the one that takes its place (summaries point to each other by uuid).
     * The outgoing one gets a new uuid, it must be saved before the incoming one.
     */
    private static void swapUuid(Summary outgoing, Summary incoming) {
        incoming.setUuid(outgoing.getUuid());
        outgoing.setUuid(UUID.randomUUID().toString());
    }

    @Override
    @CommonTx
    public Summary updateSummaryText(Manuscript manuscript, Summary summary, String text) throws Exception {
        agentRunService.checkNotLocked(manuscript);
        summary = find(summary);
        summary.setSummary(text);
        InferenceService inferenceService = inferenceServices.forAI(aiService.find(manuscript.getAi()));
        summary.setSummaryTokens(inferenceService == null ? inferenceTokensFallback(text) : inferenceService.countTokens(text));
        return save(summary);
    }

    @Override
    @CommonTx
    public List<SummaryBlock> revalidate(Manuscript manuscript, List<ChatMessage> newestFirst, ChatMessage only) throws Exception {
        agentRunService.checkNotLocked(manuscript);
        List<SummaryBlock> revalidated = new ArrayList<>();
        for (SummaryBlock block : collectBlocks(newestFirst)) {
            if (block.isValid() || only != null && !block.head().getId().equals(only.getId())) {
                continue;
            }
            Summary summary = find(block.summary());
            summary.setSummaryMessageHash(block.hash());
            save(summary);
            revalidated.add(block);
        }
        Collections.reverse(revalidated);
        return revalidated;
    }

    @Override
    @CommonTx
    public void revalidate(Manuscript manuscript, Collection<SummaryBlock> blocks) throws Exception {
        agentRunService.checkNotLocked(manuscript);
        for (SummaryBlock block : blocks) {
            Summary summary = find(block.summary());
            summary.setSummaryMessageHash(block.hash());
            save(summary);
        }
    }

    private static long inferenceTokensFallback(String text) {
        return StringUtils.length(text) / 4;
    }

    @Override
    @CommonTxReadOnly
    public List<SummaryNode> collectTree(List<ChatMessage> newestFirst) throws Exception {
        List<SummaryNode> nodes = new ArrayList<>();
        for (SummaryBlock block : collectBlocks(newestFirst)) {
            nodes.add(createNode(newestFirst, newestFirst.indexOf(block.head()), block.summary(), false));
        }
        return nodes.reversed();
    }

    @Override
    @CommonTxReadOnly
    public List<SummaryNode> collectTree(Manuscript manuscript) throws Exception {
        if (manuscript == null || manuscript.getActiveLeaf() == null) {
            return new ArrayList<>();
        }
        return collectTree(chatMessageService.getBranchFromLeaf(manuscript.getActiveLeaf()).reversed());
    }

    private SummaryNode createNode(List<ChatMessage> newestFirst, int index, Summary summary, boolean replaced) throws Exception {
        List<SummaryNode> children = summary.getSummaryType() == SummaryType.META_SUMMARY
                ? createChildren(newestFirst, index, summary) : List.of();
        return new SummaryNode(newestFirst.get(index), newestFirst.size() - index, summary, replaced, children);
    }

    /**
     * Same walk as {@link #collectBlocks}, from the part of the meta summary to the summary it points to.
     */
    private List<SummaryNode> createChildren(List<ChatMessage> newestFirst, int index, Summary meta) throws Exception {
        List<SummaryNode> children = new ArrayList<>();
        boolean ignoring = false;
        String nextUuid = null;

        if (StringUtils.isNotBlank(meta.getReplacedSummary())) {
            Summary replaced = new Summary();
            replaced.setExtendedContent(meta.getReplacedSummary().getBytes(StandardCharsets.UTF_8));
            extendableListener.deserialize(replaced);
            children.add(createNode(newestFirst, index, replaced, true));
            ignoring = replaced.getSummaryType() == SummaryType.META_SUMMARY;
            nextUuid = replaced.getNextSummaryUuid();
        }

        for (int i = index + 1; i < newestFirst.size(); i++) {
            ChatMessage message = newestFirst.get(i);
            Summary summary = message.getSummary() == null ? null : find(message.getSummary());
            if (summary == null) {
                continue;
            }
            if (meta.getNextSummaryUuid() != null && Objects.equals(summary.getUuid(), meta.getNextSummaryUuid())) {
                break;
            }
            if (!ignoring || Objects.equals(summary.getUuid(), nextUuid)) {
                children.add(createNode(newestFirst, i, summary, false));
                ignoring = summary.getSummaryType() == SummaryType.META_SUMMARY;
                nextUuid = summary.getNextSummaryUuid();
            }
        }
        return children.reversed();
    }

    @Override
    @CommonTxReadOnly
    public List<SummaryBlock> collectBlocks(List<ChatMessage> newestFirst) throws Exception {
        return walkBlocks(newestFirst.iterator(), BlockTexts.CURRENT, false);
    }

    @Override
    @CommonTxReadOnly
    public SummaryBlock collectFirstBlock(Iterator<ChatMessage> newestFirst, BlockTexts texts) throws Exception {
        List<SummaryBlock> blocks = walkBlocks(newestFirst, texts, true);
        return blocks.isEmpty() ? null : blocks.getFirst();
    }

    /**
     * @param firstOnly stop at the end of the first block, the messages after it are not read
     */
    private List<SummaryBlock> walkBlocks(Iterator<ChatMessage> newestFirst, BlockTexts texts, boolean firstOnly) throws Exception {
        List<SummaryBlock> blocks = new ArrayList<>();
        MessageDigest digest = null;
        ChatMessage head = null;
        Summary headSummary = null;
        // meta summary stands in for the summaries until the one with this uuid (none: until the root)
        boolean ignoring = false;
        String nextUuid = null;

        while (newestFirst.hasNext()) {
            ChatMessage message = newestFirst.next();
            Summary summary = message.getSummary() == null ? null : find(message.getSummary());
            if (summary != null && (!ignoring || Objects.equals(summary.getUuid(), nextUuid))) {
                if (head != null) {
                    blocks.add(new SummaryBlock(head, headSummary, HexFormat.of().formatHex(digest.digest())));
                    if (firstOnly) {
                        return blocks;
                    }
                }
                head = message;
                headSummary = summary;
                digest = MessageDigest.getInstance("SHA512");
                ignoring = summary.getSummaryType() == SummaryType.META_SUMMARY;
                nextUuid = summary.getNextSummaryUuid();
                // summary of the head is what is being checked
                updateHash(digest, message, null, texts);
            } else if (head != null) {
                updateHash(digest, message, summary, texts);
            } else if (firstOnly) {
                // the branch has to start with the message of the summary
                return blocks;
            }
        }
        if (head != null) {
            blocks.add(new SummaryBlock(head, headSummary, HexFormat.of().formatHex(digest.digest())));
        }
        return blocks;
    }

    /**
     * What a summary block hash is made of: the story text and, for the summaries a meta summary stands in for, their
     * text. Head of the block puts in no summary, that is the one being checked.
     */
    private static void updateHash(MessageDigest digest, ChatMessage message, Summary summary, BlockTexts texts) {
        digest.update(StringUtils.defaultString(texts.response(message)).getBytes(StandardCharsets.UTF_8));
        if (summary != null) {
            digest.update(StringUtils.defaultString(texts.summary(summary)).getBytes(StandardCharsets.UTF_8));
        }
    }

    private List<LLMChatMessage> createSummaryPayload(Manuscript manuscript, ChatMessage from, AI ai, Protocol protocol, InferenceService inferenceService, Summary newSummary) throws Exception {
        String systemPrompt = manuscriptService.getSummaryPrompt(manuscript);
        List<ChatMessage> tree = chatMessageService.getBranchFromLeaf(from);
        tree = tree.reversed();

        // room for the response is reserved, the prompt gets the rest of the context
        int maxTokens = TokenLimits.promptTokens(ai, protocol);
        SummaryTemplateData template = new SummaryTemplateData();
        TemplateContext templateContext = createTemplateContext(manuscript, from, ai, protocol, tree);
        // estimation render must not change variables or the real render would apply them twice
        template.setTemplateContext(templateContext.fork());

        // the jailbreak goes first, before everything else in the prompt
        String jailbreak = Boolean.TRUE.equals(ai.getNeedsJailbreak()) && StringUtils.isNotBlank(ai.getJailbreak())
                ? ai.getJailbreak() + "\n\n" : "";

        template.setBackgroundLore(StringUtils.defaultString(messagePromptService.getBackgroundLore(from)));

        String prompt = jailbreak + templateService.processTemplate(systemPrompt, "summaryPrompt", template);
        long tokens = inferenceService.countTokens(prompt);

        if (tokens > maxTokens) {
            throw new SummaryContextInsufficient(tokens, maxTokens);
        }

        List<ChatMessage> messagesToSummarize = new ArrayList<>();
        for (ChatMessage chatMessage : tree) {
            if (chatMessage.getSummary() != null) {
                break;
            }
            messagesToSummarize.add(chatMessage);
        }

        if (messagesToSummarize.isEmpty()) {
            // should not happen
            return null;
        }

        tokens += messagesToSummarize.stream().map(ChatMessage::getTokenCount).reduce(0L, (a, b) -> a + b + 1);
        if (tokens > maxTokens) {
            throw new SummaryContextInsufficient(tokens, maxTokens);
        }

        MessageDigest digest = MessageDigest.getInstance("SHA512");
        messagesToSummarize.stream().map(ChatMessage::getResponse).map(s -> s.getBytes(StandardCharsets.UTF_8)).forEach(digest::update);
        newSummary.setSummaryMessageHash(HexFormat.of().formatHex(digest.digest()));
        template.setText(messagesToSummarize.reversed().stream().map(ChatMessage::getResponse).collect(Collectors.joining("\n\n")));
        template.setTemplateContext(templateContext);
        String fullPrompt = jailbreak + templateService.processTemplate(systemPrompt, "summaryPrompt", template);
        tokens = inferenceService.countTokens(fullPrompt);

        if (tokens > maxTokens) {
            throw new SummaryContextInsufficient(tokens, maxTokens);
        }

        return List.of(LLMChatMessage.of(LLMRole.SYSTEM, fullPrompt));
    }

    private List<LLMChatMessage> createMetaSummaryPayload(Manuscript manuscript, ChatMessage from, ChatMessage to, AI ai, Protocol protocol, InferenceService inferenceService, Summary newSummary) throws Exception {
        if (from.getSummary() == null || to.getSummary() == null) {
            return null;
        }

        List<ChatMessage> tree = chatMessageService.getBranchFromLeaf(from).reversed();
        int toIndex = indexOf(tree, to);
        if (toIndex <= 0) {
            // not on the branch of from, or from itself
            return null;
        }

        // the blocks the story uses are merged, the summaries inside of them are not the story summaries anymore
        List<SummaryBlock> blocks = collectBlocks(tree);
        List<SummaryBlock> merged = new ArrayList<>();
        SummaryBlock next = null;
        for (SummaryBlock block : blocks) {
            if (indexOf(tree, block.head()) <= toIndex) {
                merged.add(block);
            } else {
                next = block;
                break;
            }
        }
        if (merged.size() < 2) {
            return null;
        }

        newSummary.setSummaryType(SummaryType.META_SUMMARY);
        newSummary.setNextSummaryUuid(next == null ? null : next.summary().getUuid());

        MessageDigest digest = MessageDigest.getInstance("SHA512");
        int end = next == null ? tree.size() : indexOf(tree, next.head());
        for (int i = 0; i < end; i++) {
            ChatMessage message = tree.get(i);
            updateHash(digest, message, i == 0 || message.getSummary() == null ? null : find(message.getSummary()), BlockTexts.CURRENT);
        }
        newSummary.setSummaryMessageHash(HexFormat.of().formatHex(digest.digest()));

        List<String> texts = merged.reversed().stream()
                .map(block -> StringUtils.defaultString(block.summary().getSummary()))
                .collect(Collectors.toList());

        String systemPrompt = manuscriptService.getMetaSummaryPrompt(manuscript);
        // room for the response is reserved, the prompt gets the rest of the context
        int maxTokens = TokenLimits.promptTokens(ai, protocol);
        MetaSummaryTemplateData template = new MetaSummaryTemplateData();
        TemplateContext templateContext = createTemplateContext(manuscript, from, ai, protocol, tree);
        // estimation render must not change variables or the real render would apply them twice
        template.setTemplateContext(templateContext.fork());

        // the jailbreak goes first, before everything else in the prompt
        String jailbreak = Boolean.TRUE.equals(ai.getNeedsJailbreak()) && StringUtils.isNotBlank(ai.getJailbreak())
                ? ai.getJailbreak() + "\n\n" : "";

        template.setBackgroundLore(StringUtils.defaultString(messagePromptService.getBackgroundLore(from)));
        template.setSummaryBlocks("");

        long summaryTokens = 0;
        for (String text : texts) {
            summaryTokens += inferenceService.countTokens(text);
        }

        String prompt = jailbreak + templateService.processTemplate(systemPrompt, "metaSummaryPrompt", template);
        long tokens = inferenceService.countTokens(prompt);
        if (tokens > maxTokens) {
            throw new SummaryContextInsufficient(tokens, maxTokens);
        }

        tokens += summaryTokens + texts.size();
        if (tokens > maxTokens) {
            throw new SummaryContextInsufficient(tokens, maxTokens);
        }

        template.setSummaryBlocks(String.join("\n\n", texts));
        template.setTemplateContext(templateContext);
        String fullPrompt = jailbreak + templateService.processTemplate(systemPrompt, "metaSummaryPrompt", template);
        tokens = inferenceService.countTokens(fullPrompt);
        if (tokens > maxTokens) {
            throw new SummaryContextInsufficient(tokens, maxTokens);
        }

        return List.of(LLMChatMessage.of(LLMRole.SYSTEM, fullPrompt));
    }

    private static int indexOf(List<ChatMessage> messages, ChatMessage message) {
        for (int i = 0; i < messages.size(); i++) {
            if (Objects.equals(messages.get(i).getId(), message.getId())) {
                return i;
            }
        }
        return -1;
    }


    /**
     * Context for macros in the summary prompt. Variables are loaded from the summarized branch, but changes are not
     * persisted - summaries are side jobs, not story continuations.
     *
     * @param newestFirst branch from {@code from} back to the root
     */
    static TemplateContext createTemplateContext(Manuscript manuscript, ChatMessage from, AI ai, Protocol protocol, List<ChatMessage> newestFirst) {
        TemplateContext context = new TemplateContext();
        context.setPovCharacter(from.getPovCharacter());
        context.setPresentCharacters(from.getPresentCharacters());
        context.setSceneSetting(from.getSceneSetting());
        context.setInstructions(from.getInstructions());
        context.setLastInstructions(from.getInstructions());
        context.setLastMessageTime(from.getRequest());
        context.setManuscriptName(manuscript.getName());
        context.setManuscriptDescription(manuscript.getDescription());
        context.setPickSeed(String.valueOf(manuscript.getId()));
        context.setModelName(ai.getName());
        int contextTokens = TokenLimits.contextTokens(ai, protocol);
        if (contextTokens > 0) {
            context.setMaxContextTokens(contextTokens);
        }
        int responseTokens = TokenLimits.responseTokens(ai, protocol);
        if (responseTokens > 0) {
            context.setMaxResponseTokens(responseTokens);
        }
        context.setGenerationType("quiet");
        context.setStoryMessages(newestFirst.reversed().stream()
                .map(ChatMessage::getResponse)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(ArrayList::new)));
        context.getVariables().loadFrom(TemplateVariables.Scope.LOCAL, from.getAttributes());
        context.getVariables().loadFrom(TemplateVariables.Scope.GLOBAL, manuscript.getAttributes());
        return context;
    }

    public static class SummaryContextInsufficient extends Exception {
        private final long contextRequired;
        private final long contextMax;

        public SummaryContextInsufficient(long contextRequired, long contextMax) {
            this.contextRequired = contextRequired;
            this.contextMax = contextMax;
        }

        public long getContextRequired() {
            return contextRequired;
        }

        public long getContextMax() {
            return contextMax;
        }
    }
}

