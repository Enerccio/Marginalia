package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.repository.SummaryRepository;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.InferenceService.ChunkType;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncCallback;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncController;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.templates.SummaryTemplateData;
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
    private ManuscriptService manuscriptService;

    @Autowired
    private TemplateService templateService;

    @Override
    @CommonTx
    public CancellationToken createSummary(Manuscript manuscript, ChatMessage from, AsyncCallback callback) throws Exception {
        if (manuscript == null || from == null || callback == null)
            return null;

        AI ai = aiService.find(manuscript.getAi());
        InferenceService inferenceService = inferenceServices.forAI(ai);
        CancellationToken cancellationToken = new CancellationToken();
        Summary newSummary = new Summary();

        List<LLMChatMessage> payload = createSummaryPayload(manuscript, from, ai, inferenceService, newSummary);
        if (payload == null)
            return null;

        newSummary = save(newSummary);
        Summary finalSummary = newSummary;
        inferenceService.stream(payload, new InferenceAsyncCallback() {

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
                }
            }

            @Override
            public void onCompletion() throws Exception {
                try (InRequestScope _ = new InRequestScope(attributes)) {
                    summary.setSummaryTokens(inferenceService.countTokens(summary.getSummary()));
                    summary.setReasoningTokens(inferenceService.countTokens(summary.getReasoning()));
                    summary = self.save(summary);
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
            public void onError(Exception exception) throws Exception {
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

    private List<LLMChatMessage> createSummaryPayload(Manuscript manuscript, ChatMessage from, AI ai, InferenceService inferenceService, Summary newSummary) throws Exception {
        String systemPrompt = manuscriptService.getSummaryPrompt(manuscript);
        List<ChatMessage> tree = chatMessageService.getBranchFromLeaf(from);
        tree = tree.reversed();

        int maxTokens = ai.getMaxCompletionTokens();
        SummaryTemplateData template = new SummaryTemplateData();

        if (StringUtils.isNotBlank(from.getBackgroundLore())) {
            template.setBackgroundLore(from.getBackgroundLore());
        } else {
            template.setBackgroundLore("");
        }

        String prompt = templateService.processTemplate(systemPrompt, "summaryPrompt", template);
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
        messagesToSummarize.reversed().stream().map(ChatMessage::getResponse).map(s -> s.getBytes(StandardCharsets.UTF_8)).forEach(digest::update);
        newSummary.setSummaryMessageHash(HexFormat.of().formatHex(digest.digest()));
        template.setText(messagesToSummarize.reversed().stream().map(ChatMessage::getResponse).collect(Collectors.joining("\n\n")));
        String fullPrompt = templateService.processTemplate(systemPrompt, "summaryPrompt", template);
        tokens = inferenceService.countTokens(prompt);

        if (tokens > maxTokens) {
            throw new SummaryContextInsufficient(tokens, maxTokens);
        }

        return List.of(LLMChatMessage.of(LLMRole.SYSTEM, fullPrompt));
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
