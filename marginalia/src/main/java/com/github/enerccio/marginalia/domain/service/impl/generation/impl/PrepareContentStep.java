package com.github.enerccio.marginalia.domain.service.impl.generation.impl;

import com.github.enerccio.marginalia.domain.collections.AnnotationStatus;
import com.github.enerccio.marginalia.domain.components.AnnotationTexts;
import com.github.enerccio.marginalia.domain.components.PromptTokenBreakdown;
import com.github.enerccio.marginalia.domain.components.TokenLimits;
import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.service.impl.generation.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationController.FromEventCallback;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.PrePromptData;
import com.github.enerccio.marginalia.domain.templates.MasterTemplateData;
import com.github.enerccio.marginalia.domain.templates.TemplateContext;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.tools.Pointer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class PrepareContentStep extends GenerationStepBase {
    private static final Logger log = LoggerFactory.getLogger(PrepareContentStep.class);


    @SuppressWarnings("unchecked")
    @Override
    protected void onStep(GenerationController controller) throws Exception {
        controller.emitEvent(Events.BEFORE_PREPARE_CONTENT, () -> {
            ChatMessage activeMessage = chatMessageService.find(controller.getManuscript().getActiveLeaf());

            InferenceService inferenceService = inferenceServices.forAIOrThrow(controller.getAi());
            PrePromptData data = controller.getPrePromptData();
            List<ChatMessage> fromRoot = null;
            List<ChatMessage> invalidatedSummaries = new ArrayList<>();
            Pointer<ChatMessage> stopMessage = new Pointer<>();
            if (activeMessage != null) {
                fromRoot = new ArrayList<>(chatMessageService.getBranchFromLeaf(activeMessage));
                GenerationRequestType requestType = controller.getRequest().getRequestType();
                if (!fromRoot.isEmpty() && (requestType == GenerationRequestType.SWIPE || requestType == GenerationRequestType.REGENERATE)) {
                    // the active leaf is the part being replaced - it is neither story so far nor a valid summary point
                    fromRoot.removeLast();
                }
                List<String> currentSummaries = gatherSummaries(fromRoot, invalidatedSummaries, stopMessage);
                controller.getProperties().put(GenerationProperties.SUMMARIES, currentSummaries);
            } else {
                controller.getProperties().put(GenerationProperties.SUMMARIES, new ArrayList<>());
            }

            List<ChatMessage> finalFromRoot = fromRoot;
            FromEventCallback callback = () -> {
                controller.emitEvent(Events.BEFORE_SUMMARIES, () -> {
                    // room for the response is reserved, the prompt gets the rest of the context
                    int limit = TokenLimits.promptTokens(controller.getAi(), controller.getProtocol());
                    PromptTokenBreakdown breakdown = createBreakdown(controller, data);
                    controller.getProperties().put(GenerationProperties.TOKEN_BREAKDOWN, breakdown);

                    List<String> summaries = (List<String>) controller.getProperties().get(GenerationProperties.SUMMARIES);
                    String sumText = "";
                    if (summaries != null) {
                        sumText = String.join("\n", summaries);
                    }
                    breakdown.setSummaryTokens(inferenceService.countTokens(sumText));

                    MasterTemplateData templateData = new MasterTemplateData();
                    templateData.setBackgroundLore(data.getBackgroundLore());
                    templateData.setNarrativePov(data.getPov());
                    templateData.setStyle(data.getStyle());
                    templateData.setNarrativeTense(data.getTense());
                    templateData.setSummaries(sumText);

                    // estimation render must not change variables or the real render would apply them twice
                    TemplateContext templateContext = getTemplateContext(controller);
                    templateData.setTemplateContext(templateContext.fork());
                    String emptyTemplate = templateService.processTemplate(data.getGeneralTemplate(), "systemTemplate", templateData);
                    templateData.setTemplateContext(templateContext);
                    long baseTokens = inferenceService.countTokens(emptyTemplate) + data.getJailbreakTokens() + data.getUserPromptProcessedTokens() + 100; /* Buffer for HEADERS */
                    // room extensions reserved for what they add to the prompt later
                    long reservedTokens = Math.max(0, data.getReservedTokens());

                    if (baseTokens + reservedTokens >= limit) {
                        controller.getUIListener().onSimpleError(loc.getValue(L.ERROR_CONTEXT_INSUFFICIENT));
                        controller.jumpTo(GenerationStepType.CLEANUP);
                        return;
                    }

                    // the notes of the author marked to be given to the model, by the part they are about
                    Map<Long, List<Annotation>> flagged = flaggedAnnotations(controller);

                    if (activeMessage != null) {
                        List<ChatMessage> chain = finalFromRoot;
                        Collections.reverse(chain);
                        Iterator<ChatMessage> iterator = chain.iterator();

                        long tokens = baseTokens + reservedTokens;
                        List<String> storyText = new ArrayList<>();
                        // the notes of the parts that are in the prompt, the newest part first like the story
                        List<String> storyNotes = new ArrayList<>();
                        long historyTokens = 0;
                        // set when the room ended before the story did (the rest is cut, not replaced by a summary)
                        boolean outOfRoom = false;

                        while (true) {
                            if (tokens > limit - 256) {
                                outOfRoom = true;
                                break;
                            }
                            if (!iterator.hasNext())
                                break;
                            ChatMessage message = iterator.next();
                            if (stopMessage.isPresent()) {
                                if (stopMessage.fastGet().getId().equals(message.getId()))
                                    break;
                            }
                            if (StringUtils.isNotBlank(message.getResponse())) {
                                if (tokens + message.getTokenCount() + 100 > limit - 256) {
                                    outOfRoom = true;
                                    // the message that did not fit is cut as well
                                    breakdown.setCutMessages(breakdown.getCutMessages() + 1);
                                    breakdown.setCutTokens(breakdown.getCutTokens() + message.getTokenCount());
                                    break;
                                }
                                String notes = AnnotationTexts.promptNotes(flagged.getOrDefault(message.getId(), List.of()), message);
                                if (!notes.isEmpty()) {
                                    // the notes take room in the system prompt
                                    long noteTokens = inferenceService.countTokens(notes) + 1;
                                    if (tokens + message.getTokenCount() + noteTokens + 100 > limit - 256) {
                                        outOfRoom = true;
                                        breakdown.setCutMessages(breakdown.getCutMessages() + 1);
                                        breakdown.setCutTokens(breakdown.getCutTokens() + message.getTokenCount());
                                        break;
                                    }
                                    storyNotes.add(notes);
                                    tokens += noteTokens;
                                }
                                storyText.add(message.getResponse());
                                historyTokens += message.getTokenCount();
                                tokens += message.getTokenCount() + 100; /* Buffer for dummy messages */
                            }
                        }

                        if (outOfRoom) {
                            // the rest of the story until the newest summary
                            while (iterator.hasNext()) {
                                ChatMessage message = iterator.next();
                                if (stopMessage.isPresent() && stopMessage.fastGet().getId().equals(message.getId())) {
                                    break;
                                }
                                if (StringUtils.isNotBlank(message.getResponse())) {
                                    breakdown.setCutMessages(breakdown.getCutMessages() + 1);
                                    breakdown.setCutTokens(breakdown.getCutTokens() + message.getTokenCount());
                                }
                            }
                        }
                        breakdown.setHistoryMessages(storyText.size());
                        breakdown.setHistoryTokens(historyTokens);

                        Collections.reverse(storyText);
                        controller.getProperties().put(GenerationProperties.MANUSCRIPT_CHRONICLE, storyText);

                        Collections.reverse(storyNotes);
                        templateData.setAnnotations(String.join("\n", storyNotes));

                        controller.emitEvent(Events.AFTER_MANUSCRIPT_CONCATENATION, () -> {
                            String systemPrompt = templateService.processTemplate(data.getGeneralTemplate(), "systemTemplate", templateData);
                            data.setSystemPrompt(systemPrompt);
                            data.setSystemPromptTokens(inferenceService.countTokens(systemPrompt));
                            breakdown.setTemplateTokens(templateTokens(breakdown, data.getSystemPromptTokens()));
                            controller.emitEvent(Events.AFTER_PREPARE_CONTENT, controller::next);
                        });
                    } else {
                        data.setSystemPrompt(templateService.processTemplate(data.getGeneralTemplate(), "systemTemplate", templateData));
                        data.setSystemPromptTokens(baseTokens);
                        breakdown.setTemplateTokens(templateTokens(breakdown,
                                inferenceService.countTokens(data.getSystemPrompt())));
                        controller.emitEvent(Events.AFTER_PREPARE_CONTENT, controller::next);
                    }
                });
            };

            if (!invalidatedSummaries.isEmpty()) {
                controller.getUIListener().askQuestion(String.format(loc.getValue(L.MSG_SUMMARY_FAILURE), invalidatedSummaries.stream().map(ChatMessage::getId).map(x -> "#" + x.toString()).collect(Collectors.joining(", "))),
                        controller.wrapCallback(callback), controller.wrapCallback(() -> controller.jumpTo(GenerationStepType.CLEANUP)));
            } else {
                callback.returnFromEvent();
            }
        });
    }

    /**
     * The open annotations of the book that are marked to be given to the model, by the id of the part.
     */
    private Map<Long, List<Annotation>> flaggedAnnotations(GenerationController controller) throws Exception {
        Map<Long, List<Annotation>> flagged = new HashMap<>();
        for (Annotation annotation : annotationService.listForManuscript(controller.getManuscript())) {
            if (annotation.isSendToPrompt() && annotation.getStatus() == AnnotationStatus.OPEN) {
                flagged.computeIfAbsent(annotation.getPartId(), id -> new ArrayList<>()).add(annotation);
            }
        }
        return flagged;
    }

    /**
     * Breakdown with the budget and the sections that are known before the story is chosen.
     */
    private PromptTokenBreakdown createBreakdown(GenerationController controller, PrePromptData data) {
        PromptTokenBreakdown breakdown = new PromptTokenBreakdown();
        breakdown.setContextTokens(TokenLimits.contextTokens(controller.getAi(), controller.getProtocol()));
        breakdown.setResponseTokens(TokenLimits.responseTokens(controller.getAi(), controller.getProtocol()));
        breakdown.setPromptLimitTokens(TokenLimits.promptTokens(controller.getAi(), controller.getProtocol()));
        breakdown.setJailbreakTokens(data.getJailbreakTokens());
        breakdown.setLoreTokens(data.getBackgroundLoreTokens());
        breakdown.setUserLoreTokens(data.getBackgroundUserLoreTokens());
        // the lore in front of the user prompt is already added to its tokens
        long userPrompt = data.getUserPromptProcessedTokens();
        if (StringUtils.isNotBlank(data.getBackgroundUserLore())) {
            userPrompt -= data.getBackgroundUserLoreTokens();
        }
        breakdown.setUserPromptTokens(Math.max(0, userPrompt));
        breakdown.setReservedTokens(Math.max(0, data.getReservedTokens()));
        return breakdown;
    }

    /**
     * Tokens of the system prompt that are neither lore nor summaries.
     */
    private long templateTokens(PromptTokenBreakdown breakdown, long systemPromptTokens) {
        return Math.max(0, systemPromptTokens - breakdown.getLoreTokens() - breakdown.getSummaryTokens());
    }

    /**
     * Summaries to put into the prompt, oldest first. A summary (block) whose story changed since it was made is removed
     * (a meta summary is unwound to the summary it replaced) and reported in invalidatedSummaries.
     *
     * @param firstSummaryFound set to the message of the newest summary, the story text goes from the end to it
     */
    private List<String> gatherSummaries(List<ChatMessage> fromRoot, List<ChatMessage> invalidatedSummaries, Pointer<ChatMessage> firstSummaryFound) throws Exception {
        List<ChatMessage> newestFirst = new ArrayList<>(fromRoot.reversed());

        while (true) {
            List<SummaryService.SummaryBlock> blocks = summaryService.collectBlocks(newestFirst);
            SummaryService.SummaryBlock invalid = blocks.stream().filter(block -> !block.isValid()).findFirst().orElse(null);

            if (invalid == null) {
                if (blocks.isEmpty()) {
                    return new ArrayList<>();
                }
                firstSummaryFound.set(blocks.getFirst().head());
                return blocks.reversed().stream()
                        .map(block -> StringUtils.defaultString(block.summary().getSummary()))
                        .collect(Collectors.toCollection(ArrayList::new));
            }

            ChatMessage updated = summaryService.removeSummary(invalid.head(), true);
            log.info("Summary invalidated");
            invalidatedSummaries.add(updated);
            // restored summary has to be checked as well
            newestFirst.replaceAll(message -> message.getId().equals(updated.getId()) ? updated : message);
        }
    }

    @Override
    public GenerationStepType getType() {
        return GenerationStepType.PREPARE_CONTENT;
    }
}

