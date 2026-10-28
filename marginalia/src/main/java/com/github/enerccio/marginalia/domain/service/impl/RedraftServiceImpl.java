package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.components.AnnotationTexts;
import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.InferenceService.ChunkType;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncCallback;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceAsyncController;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationOptions;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.templates.RedraftTemplateData;
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
import java.util.Objects;

public class RedraftServiceImpl implements RedraftService {

    private static final Logger log = LoggerFactory.getLogger(RedraftServiceImpl.class);

    /**
     * Tokens kept free for what the count does not see (the headers of the messages).
     */
    private static final long BUFFER_TOKENS = 100;

    private static final String SEPARATOR = StoryAround.SEPARATOR;

    @Autowired
    private ManuscriptService manuscriptService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private MessagePromptService messagePromptService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private AnnotationService annotationService;

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
    public CancellationToken redraft(Manuscript manuscript, ChatMessage part, String instructions, String prompt,
                                     GenerationOptions options, boolean withAnnotations, Passage passage, Callback callback) throws Exception {
        if (manuscript == null || part == null || callback == null) {
            throw new IllegalArgumentException("The part does not exist");
        }
        Manuscript book = (Manuscript) Hibernate.unproxy(manuscriptService.findForUser(manuscript.getUuid()));
        if (book == null) {
            throw new IllegalArgumentException("The manuscript does not exist");
        }
        agentRunService.checkNotLocked(book);
        ChatMessage stored = (ChatMessage) Hibernate.unproxy(chatMessageService.find(part));
        if (stored == null || stored.isDeleted() || stored.getParentScript() == null
                || !Objects.equals(stored.getParentScript().getId(), book.getId())) {
            throw new IllegalArgumentException("The part does not exist");
        }
        if (StringUtils.isBlank(stored.getResponse())) {
            throw new IllegalArgumentException("The part has no text");
        }
        if (passage != null && (passage.start() < 0 || passage.end() > stored.getResponse().length()
                || StringUtils.isBlank(stored.getResponse().substring(passage.start(), Math.max(passage.start(), passage.end()))))) {
            throw new IllegalArgumentException("The passage is not in the text of the part");
        }
        GenerationOptions chosen = options == null ? GenerationOptions.defaults() : options;
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

        List<LLMChatMessage> payload = createPayload(book, stored, StringUtils.trimToEmpty(instructions), prompt, withAnnotations, passage, ai, protocol, inferenceService);

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

        try (InferenceSession _ = InferenceSession.open(InferenceSession.REDRAFT)) {
            inferenceService.stream(payload, protocol, inferenceCallback);
        } catch (Exception e) {
            log.error("Failed to start the redraft inference", e);
            inferenceCallback.onError(e);
        }
        return cancellationToken;
    }

    /**
     * The annotations a redraft of the passage deals with: the ones whose passage overlaps it. All of them for the redraft of the whole part.
     */
    private static List<Annotation> annotationsIn(List<Annotation> annotations, Passage passage) {
        if (passage == null) {
            return annotations;
        }
        return annotations.stream().filter(passage::overlaps).toList();
    }

    /**
     * The prompt of the redraft: the redraft template of the book with the part in it and as much story around the part
     * as the context takes.
     */
    private List<LLMChatMessage> createPayload(Manuscript book, ChatMessage part, String instructions, String prompt, boolean withAnnotations, Passage passage, AI ai, Protocol protocol,
                                               InferenceService inferenceService) throws Exception {
        List<ChatMessage> upToPart = new ArrayList<>(chatMessageService.getBranchFromLeaf(part));
        List<ChatMessage> before = upToPart.isEmpty() ? new ArrayList<>() : new ArrayList<>(upToPart.subList(0, upToPart.size() - 1));
        List<ChatMessage> after = StoryAround.partsAfter(chatMessageService, book, part);

        String systemTemplate = manuscriptService.getRedraftSystemPrompt(book);
        // the prompt given for one run replaces the last message, the one that holds the part
        String template = StringUtils.isNotBlank(prompt) ? prompt : manuscriptService.getRedraftPrompt(book);
        int maxTokens = TokenLimits.promptTokens(ai, protocol);

        RedraftTemplateData data = new RedraftTemplateData();
        data.setBackgroundLore(StringUtils.defaultString(messagePromptService.getBackgroundLore(part)));
        data.setNarrativePov(manuscriptService.getPov(book));
        data.setNarrativeTense(manuscriptService.getTense(book));
        data.setStyle(manuscriptService.getStyle(book));
        data.setPovCharacter(StringUtils.defaultString(part.getPovCharacter()));
        data.setSceneSetting(StringUtils.defaultString(part.getSceneSetting()));
        data.setPresentCharacters(StringUtils.defaultString(part.getPresentCharacters()));
        data.setInstructions(StringUtils.defaultString(part.getInstructions()));
        data.setText(part.getResponse());
        if (passage != null) {
            data.setSelection(part.getResponse().substring(passage.start(), passage.end()));
            data.setSelectionBefore(part.getResponse().substring(0, passage.start()));
            data.setSelectionAfter(part.getResponse().substring(passage.end()));
        }
        data.setRedraftInstructions(instructions);
        data.setAnnotations(withAnnotations ? AnnotationTexts.openAnnotations(annotationsIn(annotationService.listForPart(book, part), passage)) : "");
        data.setSummaries("");
        data.setPreviousStory("");
        data.setFollowingStory("");

        TemplateContext context = SummaryServiceImpl.createTemplateContext(book, part, ai, protocol, new ArrayList<>(before.reversed()));
        // estimation render must not change variables or the real render would apply them twice
        data.setTemplateContext(context.fork());

        // the jailbreak goes first, before everything else in the prompt
        String jailbreak = Boolean.TRUE.equals(ai.getNeedsJailbreak()) && StringUtils.isNotBlank(ai.getJailbreak())
                ? ai.getJailbreak() + SEPARATOR : "";

        String estimation = jailbreak + templateService.processTemplate(systemTemplate, "redraftSystemPrompt", data)
                + templateService.processTemplate(template, "redraftPrompt", data);
        long base = inferenceService.countTokens(estimation) + BUFFER_TOKENS;
        long room = maxTokens - base;
        if (room < 0) {
            throw new ContextInsufficient(base, maxTokens);
        }
        // the same story for the same part, whatever the length of the part and of its notes is now; it goes in as messages
        StoryAround.Around around = StoryAround.collect(summaryService, inferenceService, before, after,
                StoryAround.stableRoom(room, maxTokens), true);
        data.setSummaries(around.summaries());
        data.setPreviousStory(around.previousParts().isEmpty() ? "" : StoryAround.STORY_IN_MESSAGES);
        data.setFollowingStory(around.following());
        data.setTemplateContext(context);
        String system = jailbreak + templateService.processTemplate(systemTemplate, "redraftSystemPrompt", data);
        String user = templateService.processTemplate(template, "redraftPrompt", data);
        long tokens = inferenceService.countTokens(system + user) + around.previousPartsTokens();
        if (tokens > maxTokens) {
            throw new ContextInsufficient(tokens, maxTokens);
        }
        return StoryAround.messages(system, around.previousParts(), user);
    }
}
