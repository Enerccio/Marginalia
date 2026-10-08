package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.repository.SummaryRepository;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.InferenceService.ChunkType;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncCallback;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncController;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.templates.SummaryTemplateData;
import com.github.enerccio.marginalia.domain.templates.TemplateContext;
import com.github.enerccio.marginalia.domain.templates.TemplateVariables;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

public class SummaryServiceImpl extends ExtendableServiceImpl<Summary, SummaryRepository> implements SummaryService {

    @Autowired
    @Lazy
    private SummaryService self;

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

    @Override
    @CommonTx
    public CancellationToken createSummary(Manuscript manuscript, ChatMessage from, AsyncCallback callback) throws Exception {
        if (manuscript == null || from == null || callback == null || manuscript.getAi() == null)
            return null;

        AI ai = aiService.find(manuscript.getAi());
        Protocol protocol = protocolService.find(manuscript.getProtocol());
        InferenceService inferenceService = inferenceServices.forAI(ai);
        if (inferenceService == null)
            return null;
        
        CancellationToken cancellationToken = new CancellationToken();
        Summary newSummary = new Summary();
        newSummary.setReasoning("");
        newSummary.setSummary("");
        newSummary.setReasoningTokens(0L);
        newSummary.setSummaryTokens(0L);

        List<LLMChatMessage> payload = createSummaryPayload(manuscript, from, ai, protocol, inferenceService, newSummary);
        if (payload == null)
            return null;

        newSummary = save(newSummary);
        Summary finalSummary = newSummary;
        inferenceService.stream(payload, protocol, new InferenceAsyncCallback() {

            Summary summary = finalSummary;
            final ThreadCopyRequestAttributes attributes = ThreadCopyRequestAttributes.create();

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
                    summary = self.save(summary);
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
                    ChatMessage message = chatMessageService.find(from);
                    message.setSummary(summary);
                    chatMessageService.save(message);
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
        });

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
        return save(copy);
    }

    private List<LLMChatMessage> createSummaryPayload(Manuscript manuscript, ChatMessage from, AI ai, Protocol protocol, InferenceService inferenceService, Summary newSummary) throws Exception {
        String systemPrompt = manuscriptService.getSummaryPrompt(manuscript);
        List<ChatMessage> tree = chatMessageService.getBranchFromLeaf(from);
        tree = tree.reversed();

        // room for the response is reserved, the prompt gets the rest of the context
        int maxTokens = TokenLimits.promptTokens(ai, protocol);
        SummaryTemplateData template = new SummaryTemplateData();
        template.setTemplateContext(createTemplateContext(manuscript, from, ai, protocol, tree));

        // the jailbreak goes first, before everything else in the prompt
        String jailbreak = Boolean.TRUE.equals(ai.getNeedsJailbreak()) && StringUtils.isNotBlank(ai.getJailbreak())
                ? ai.getJailbreak() + "\n\n" : "";

        if (StringUtils.isNotBlank(from.getBackgroundLore())) {
            template.setBackgroundLore(from.getBackgroundLore());
        } else {
            template.setBackgroundLore("");
        }

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
        String fullPrompt = jailbreak + templateService.processTemplate(systemPrompt, "summaryPrompt", template);
        tokens = inferenceService.countTokens(fullPrompt);

        if (tokens > maxTokens) {
            throw new SummaryContextInsufficient(tokens, maxTokens);
        }

        return List.of(LLMChatMessage.of(LLMRole.SYSTEM, fullPrompt));
    }


    /**
     * Context for macros in the summary prompt. Variables are loaded from the summarized branch, but changes are not
     * persisted - summaries are side jobs, not story continuations.
     *
     * @param newestFirst branch from {@code from} back to the root
     */
    private TemplateContext createTemplateContext(Manuscript manuscript, ChatMessage from, AI ai, Protocol protocol, List<ChatMessage> newestFirst) {
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

