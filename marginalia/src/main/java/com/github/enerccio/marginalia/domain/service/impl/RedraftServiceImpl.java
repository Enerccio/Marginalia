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
import com.github.enerccio.marginalia.domain.service.SummaryService.SummaryBlock;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RedraftServiceImpl implements RedraftService {

    private static final Logger log = LoggerFactory.getLogger(RedraftServiceImpl.class);

    /**
     * Tokens kept free for what the count does not see (the headers of the messages).
     */
    private static final long BUFFER_TOKENS = 100;

    /**
     * The share of the room for the story the part after the redrafted one may take, the rest is the story before it.
     */
    private static final int FOLLOWING_SHARE = 4;

    private static final String SEPARATOR = "\n\n";

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
    public CancellationToken redraft(Manuscript manuscript, ChatMessage part, String instructions, String prompt,
                                     GenerationOptions options, Callback callback) throws Exception {
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

        List<LLMChatMessage> payload = createPayload(book, stored, StringUtils.trimToEmpty(instructions), prompt, ai, protocol, inferenceService);

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
            log.error("Failed to start the redraft inference", e);
            inferenceCallback.onError(e);
        }
        return cancellationToken;
    }

    /**
     * The prompt of the redraft: the redraft template of the book with the part in it and as much story around the part
     * as the context takes.
     */
    private List<LLMChatMessage> createPayload(Manuscript book, ChatMessage part, String instructions, String prompt, AI ai, Protocol protocol,
                                               InferenceService inferenceService) throws Exception {
        List<ChatMessage> upToPart = new ArrayList<>(chatMessageService.getBranchFromLeaf(part));
        List<ChatMessage> before = upToPart.isEmpty() ? new ArrayList<>() : new ArrayList<>(upToPart.subList(0, upToPart.size() - 1));
        List<ChatMessage> after = partsAfter(book, part);

        String template = StringUtils.isNotBlank(prompt) ? prompt : manuscriptService.getRedraftPrompt(book);
        int maxTokens = TokenLimits.promptTokens(ai, protocol);

        RedraftTemplateData data = new RedraftTemplateData();
        data.setBackgroundLore(StringUtils.defaultString(part.getBackgroundLore()));
        data.setNarrativePov(manuscriptService.getPov(book));
        data.setNarrativeTense(manuscriptService.getTense(book));
        data.setStyle(manuscriptService.getStyle(book));
        data.setPovCharacter(StringUtils.defaultString(part.getPovCharacter()));
        data.setSceneSetting(StringUtils.defaultString(part.getSceneSetting()));
        data.setPresentCharacters(StringUtils.defaultString(part.getPresentCharacters()));
        data.setInstructions(StringUtils.defaultString(part.getInstructions()));
        data.setText(part.getResponse());
        data.setRedraftInstructions(instructions);
        data.setSummaries("");
        data.setPreviousStory("");
        data.setFollowingStory("");

        TemplateContext context = SummaryServiceImpl.createTemplateContext(book, part, ai, protocol, new ArrayList<>(before.reversed()));
        // estimation render must not change variables or the real render would apply them twice
        data.setTemplateContext(context.fork());

        // the jailbreak goes first, before everything else in the prompt
        String jailbreak = Boolean.TRUE.equals(ai.getNeedsJailbreak()) && StringUtils.isNotBlank(ai.getJailbreak())
                ? ai.getJailbreak() + SEPARATOR : "";

        long base = inferenceService.countTokens(jailbreak + templateService.processTemplate(template, "redraftPrompt", data)) + BUFFER_TOKENS;
        long room = maxTokens - base;
        if (room < 0) {
            throw new ContextInsufficient(base, maxTokens);
        }

        // the story after the part is only a hint of where it leads, so it gets a share of the room
        long followingRoom = room / FOLLOWING_SHARE;
        List<String> following = new ArrayList<>();
        long followingTokens = 0;
        for (ChatMessage next : after) {
            if (StringUtils.isBlank(next.getResponse())) {
                continue;
            }
            long tokens = tokensOf(inferenceService, next) + 2;
            if (followingTokens + tokens > followingRoom) {
                break;
            }
            following.add(next.getResponse());
            followingTokens += tokens;
        }

        long previousRoom = room - followingTokens;
        List<ChatMessage> newestFirst = new ArrayList<>(before.reversed());
        List<SummaryBlock> blocks = summaryService.collectBlocks(newestFirst);
        // summaries that do not describe the story anymore are not told to the model, the story is used as it is instead
        boolean summarized = !blocks.isEmpty() && blocks.stream().allMatch(SummaryBlock::isValid);

        List<String> summaries = new ArrayList<>();
        long summaryTokens = 0;
        if (summarized) {
            // the newest summaries are the ones worth keeping when they do not fit
            for (SummaryBlock block : blocks) {
                String text = StringUtils.defaultString(block.summary().getSummary());
                long tokens = inferenceService.countTokens(text) + 2;
                if (summaryTokens + tokens > previousRoom) {
                    break;
                }
                summaries.add(text);
                summaryTokens += tokens;
            }
            Collections.reverse(summaries);
        }

        List<String> previous = new ArrayList<>();
        long previousTokens = summaryTokens;
        ChatMessage stop = summarized ? blocks.getFirst().head() : null;
        for (ChatMessage earlier : newestFirst) {
            if (stop != null && Objects.equals(stop.getId(), earlier.getId())) {
                break;
            }
            if (StringUtils.isBlank(earlier.getResponse())) {
                continue;
            }
            long tokens = tokensOf(inferenceService, earlier) + 2;
            if (previousTokens + tokens > previousRoom) {
                break;
            }
            previous.add(earlier.getResponse());
            previousTokens += tokens;
        }
        Collections.reverse(previous);

        data.setSummaries(String.join(SEPARATOR, summaries));
        data.setPreviousStory(String.join(SEPARATOR, previous));
        data.setFollowingStory(String.join(SEPARATOR, following));
        data.setTemplateContext(context);
        String rendered = jailbreak + templateService.processTemplate(template, "redraftPrompt", data);
        long tokens = inferenceService.countTokens(rendered);
        if (tokens > maxTokens) {
            throw new ContextInsufficient(tokens, maxTokens);
        }
        return List.of(LLMChatMessage.of(LLMRole.SYSTEM, rendered));
    }

    /**
     * The parts after the given one on the branch the book is at, oldest first; none when the part is not on it.
     */
    private List<ChatMessage> partsAfter(Manuscript book, ChatMessage part) throws Exception {
        ChatMessage leaf = chatMessageService.find(book.getActiveLeaf());
        if (leaf == null) {
            return List.of();
        }
        List<ChatMessage> branch = chatMessageService.getBranchFromLeaf(leaf);
        for (int i = 0; i < branch.size(); i++) {
            if (Objects.equals(branch.get(i).getId(), part.getId())) {
                return new ArrayList<>(branch.subList(i + 1, branch.size()));
            }
        }
        return List.of();
    }

    private static long tokensOf(InferenceService inferenceService, ChatMessage message) throws Exception {
        return message.getTokenCount() > 0 ? message.getTokenCount() : inferenceService.countTokens(message.getResponse());
    }
}
