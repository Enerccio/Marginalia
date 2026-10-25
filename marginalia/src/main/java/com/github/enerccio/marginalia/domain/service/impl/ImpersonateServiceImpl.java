package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.InferenceService.ChunkType;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncCallback;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncController;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationOptions;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.templates.ImpersonateTemplateData;
import com.github.enerccio.marginalia.domain.templates.TemplateContext;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.context.request.RequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ImpersonateServiceImpl implements ImpersonateService {

    private static final Logger log = LoggerFactory.getLogger(ImpersonateServiceImpl.class);

    /**
     * Tokens kept free for what the count does not see (the headers of the messages).
     */
    private static final long BUFFER_TOKENS = 100;

    /**
     * How many of the last instructions of the author the model sees as examples of how they write them.
     */
    private static final int EXAMPLES = 3;

    private static final String SEPARATOR = StoryAround.SEPARATOR;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private InferenceServices inferenceServices;

    @Autowired
    private AIService aiService;

    @Autowired
    private ProtocolService protocolService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    @Lazy
    private AgentRunService agentRunService;

    @Override
    @CommonTxReadOnly
    public CancellationToken impersonate(Manuscript manuscript, Request request, Callback callback) throws Exception {
        if (manuscript == null || request == null || callback == null) {
            throw new IllegalArgumentException("The manuscript does not exist");
        }
        Manuscript book = (Manuscript) Hibernate.unproxy(manuscriptService.findForUser(manuscript.getUuid()));
        if (book == null) {
            throw new IllegalArgumentException("The manuscript does not exist");
        }
        agentRunService.checkNotLocked(book);
        GenerationOptions chosen = request.options() == null ? GenerationOptions.defaults() : request.options();
        // the model of the options, else the one of the book
        AI wanted = chosen.ai() != null ? chosen.ai() : book.getAi();
        Protocol wantedProtocol = chosen.protocol() != null ? chosen.protocol() : book.getProtocol();
        if (wanted == null || wantedProtocol == null) {
            return null;
        }

        AI ai = aiService.find(wanted);
        Protocol protocol = protocolService.find(wantedProtocol);
        if (ai == null || protocol == null) {
            return null;
        }
        InferenceService inferenceService = inferenceServices.forAIOrThrow(ai);

        List<LLMChatMessage> payload = createPayload(book, request, ai, protocol, inferenceService);

        CancellationToken cancellationToken = new CancellationToken();
        InferenceAsyncCallback inferenceCallback = new InferenceAsyncCallback() {

            private final StringBuilder reasoning = new StringBuilder();
            private final StringBuilder text = new StringBuilder();
            private final RequestAttributes attributes = ThreadCopyRequestAttributes.create();

            @Override
            public void onChunk(InferenceAsyncController controller, ChunkType chunkType, String chunk) throws Exception {
                try (InRequestScope _ = new InRequestScope(attributes)) {
                    if (chunkType == ChunkType.REASONING) {
                        reasoning.append(chunk);
                    } else {
                        text.append(chunk);
                    }
                    callback.onProgress(reasoning.toString(), text.toString());
                    controller.continueInference();
                }
            }

            @Override
            public void onCompletion() throws Exception {
                try (InRequestScope _ = new InRequestScope(attributes)) {
                    callback.onFinished(reasoning.toString(), text.toString().strip());
                }
            }

            @Override
            public void onCancel() throws Exception {
                try (InRequestScope _ = new InRequestScope(attributes)) {
                    callback.onTerminated(reasoning.toString(), text.toString().strip());
                }
            }

            @Override
            public void onError(Throwable exception) throws Exception {
                try (InRequestScope _ = new InRequestScope(attributes)) {
                    callback.onError(exception);
                }
            }

            @Override
            public boolean isDead() {
                return cancellationToken.isCancelled();
            }
        };

        try {
            inferenceService.stream(payload, protocol, inferenceCallback);
        } catch (Exception e) {
            log.error("Failed to start the impersonate inference", e);
            inferenceCallback.onError(e);
        }
        return cancellationToken;
    }

    /**
     * The prompt: the impersonate template of the book with the story so far in it, as much of it as the context takes.
     */
    private List<LLMChatMessage> createPayload(Manuscript book, Request request, AI ai, Protocol protocol,
                                               InferenceService inferenceService) throws Exception {
        ChatMessage leaf = book.getActiveLeaf() == null ? null : chatMessageService.find(book.getActiveLeaf());
        // oldest first
        List<ChatMessage> branch = leaf == null ? new ArrayList<>() : new ArrayList<>(chatMessageService.getBranchFromLeaf(leaf));
        ChatMessage last = branch.isEmpty() ? null : branch.getLast();

        String template = StringUtils.isNotBlank(request.prompt()) ? request.prompt() : manuscriptService.getImpersonatePrompt(book);
        int maxTokens = TokenLimits.promptTokens(ai, protocol);

        ImpersonateTemplateData data = new ImpersonateTemplateData();
        data.setBackgroundLore(last == null ? "" : StringUtils.defaultString(last.getBackgroundLore()));
        data.setNarrativePov(manuscriptService.getPov(book));
        data.setNarrativeTense(manuscriptService.getTense(book));
        data.setStyle(manuscriptService.getStyle(book));
        data.setSceneSetting(request.sceneSetting() != null ? request.sceneSetting() : last == null ? "" : StringUtils.defaultString(last.getSceneSetting()));
        data.setPovCharacter(request.povCharacter() != null ? request.povCharacter() : last == null ? "" : StringUtils.defaultString(last.getPovCharacter()));
        data.setPresentCharacters(request.presentCharacters() != null ? request.presentCharacters()
                : last == null ? "" : StringUtils.defaultString(last.getPresentCharacters()));
        data.setInstructionExamples(examples(branch));
        data.setOutline(StringUtils.trimToEmpty(request.outline()));
        data.setImpersonateInstructions(StringUtils.trimToEmpty(request.instructions()));
        data.setSummaries("");
        data.setPreviousStory("");

        TemplateContext context = last == null ? bookContext(book)
                : SummaryServiceImpl.createTemplateContext(book, last, ai, protocol, new ArrayList<>(branch.reversed()));
        // estimation render must not change variables or the real render would apply them twice
        data.setTemplateContext(context.fork());

        // the jailbreak goes first, before everything else in the prompt
        String jailbreak = Boolean.TRUE.equals(ai.getNeedsJailbreak()) && StringUtils.isNotBlank(ai.getJailbreak())
                ? ai.getJailbreak() + SEPARATOR : "";

        long base = inferenceService.countTokens(jailbreak + templateService.processTemplate(template, "impersonatePrompt", data)) + BUFFER_TOKENS;
        long room = maxTokens - base;
        if (room < 0) {
            throw new ContextInsufficient(base, maxTokens);
        }

        StoryAround.Around around = StoryAround.collect(summaryService, inferenceService, branch, List.of(), room);
        data.setSummaries(around.summaries());
        data.setPreviousStory(around.previous());
        data.setTemplateContext(context);
        String rendered = jailbreak + templateService.processTemplate(template, "impersonatePrompt", data);
        long tokens = inferenceService.countTokens(rendered);
        if (tokens > maxTokens) {
            throw new ContextInsufficient(tokens, maxTokens);
        }
        return List.of(LLMChatMessage.of(LLMRole.SYSTEM, rendered));
    }

    /**
     * The instructions of the last parts of the branch, oldest first, so that the model sees how the author writes them.
     */
    private static String examples(List<ChatMessage> branch) {
        List<String> found = branch.reversed().stream()
                .map(ChatMessage::getInstructions)
                .filter(StringUtils::isNotBlank)
                .limit(EXAMPLES)
                .map(String::strip)
                .collect(Collectors.toCollection(ArrayList::new));
        return String.join(SEPARATOR, found.reversed());
    }

    /**
     * The context of a book that has no part yet.
     */
    private static TemplateContext bookContext(Manuscript book) {
        TemplateContext context = new TemplateContext();
        context.setManuscriptName(book.getName());
        context.setManuscriptDescription(book.getDescription());
        context.setPickSeed(String.valueOf(book.getId()));
        return context;
    }
}
