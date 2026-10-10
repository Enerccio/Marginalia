package com.github.enerccio.marginalia.domain.service.impl.generation.impl;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.service.TokenLimits;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class PrepareContentStep extends GenerationStepBase {
    private static final Logger log = LoggerFactory.getLogger(PrepareContentStep.class);


    @SuppressWarnings("unchecked")
    @Override
    protected void onStep(GenerationController controller) throws Exception {
        controller.emitEvent(Events.BEFORE_PREPARE_CONTENT, () -> {
            ChatMessage activeMessage = chatMessageService.find(controller.getManuscript().getActiveLeaf());

            InferenceService inferenceService = inferenceServices.forAI(controller.getManuscript().getAi());
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
                    int limit = TokenLimits.promptTokens(controller.getManuscript().getAi(), controller.getManuscript().getProtocol());

                    List<String> summaries = (List<String>) controller.getProperties().get(GenerationProperties.SUMMARIES);
                    String sumText = "";
                    if (summaries != null) {
                        sumText = String.join("\n", summaries);
                    }

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

                    if (activeMessage != null) {
                        List<ChatMessage> chain = finalFromRoot;
                        Collections.reverse(chain);
                        Iterator<ChatMessage> iterator = chain.iterator();

                        long tokens = baseTokens + reservedTokens;
                        List<String> storyText = new ArrayList<>();

                        while (true) {
                            if (tokens > limit - 256) {
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
                                if (tokens + message.getTokenCount() + 100 > limit - 256)
                                    break;
                                storyText.add(message.getResponse());
                                tokens += message.getTokenCount() + 100; /* Buffer for dummy messages */
                            }
                        }

                        Collections.reverse(storyText);
                        controller.getProperties().put(GenerationProperties.MANUSCRIPT_CHRONICLE, storyText);

                        controller.emitEvent(Events.AFTER_MANUSCRIPT_CONCATENATION, () -> {
                            String systemPrompt = templateService.processTemplate(data.getGeneralTemplate(), "systemTemplate", templateData);
                            data.setSystemPrompt(systemPrompt);
                            data.setSystemPromptTokens(inferenceService.countTokens(systemPrompt));
                            controller.emitEvent(Events.AFTER_PREPARE_CONTENT, controller::next);
                        });
                    } else {
                        data.setSystemPrompt(templateService.processTemplate(data.getGeneralTemplate(), "systemTemplate", templateData));
                        data.setSystemPromptTokens(baseTokens);
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

