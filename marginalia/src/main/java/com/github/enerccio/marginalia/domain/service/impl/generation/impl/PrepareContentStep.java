package com.github.enerccio.marginalia.domain.service.impl.generation.impl;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationController;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationController.FromEventCallback;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationStepBase;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationStepType;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.PrePromptData;
import com.github.enerccio.marginalia.domain.templates.MasterTemplateData;
import com.github.enerccio.marginalia.loc.L;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class PrepareContentStep extends GenerationStepBase {
    private static final Logger log = LoggerFactory.getLogger(PrepareContentStep.class);

    public static final String MANUSCRIPT_CHRONICLE = "MANUSCRIPT_CHRONICLE";
    public static final String SUMMARIES = "SUMMARIES";

    @Autowired
    private SummaryService summaryService;

    @SuppressWarnings("unchecked")
    @Override
    protected void onStep(GenerationController controller) throws Exception {
        controller.emitEvent(Events.BEFORE_PREPARE_CONTENT, () -> {
            ChatMessage activeMessage = chatMessageService.find(controller.getManuscript().getActiveLeaf());

            InferenceService inferenceService = inferenceServices.forAI(controller.getManuscript().getAi());
            PrePromptData data = controller.getPrePromptData();
            List<ChatMessage> fromRoot = null;
            List<ChatMessage> invalidatedSummaries = new ArrayList<>();
            if (activeMessage != null) {
                fromRoot = chatMessageService.getBranchFromLeaf(activeMessage);
                List<String> currentSummaries = gatherSummaries(fromRoot, invalidatedSummaries);
                controller.getProperties().put(SUMMARIES, currentSummaries);
            } else {
                controller.getProperties().put(SUMMARIES, new ArrayList<>());
            }

            List<ChatMessage> finalFromRoot = fromRoot;
            FromEventCallback callback = () -> {
                controller.emitEvent(Events.BEFORE_SUMMARIES, () -> {
                    int limit = controller.getManuscript().getProtocol().getMaxTokens();
                    if (limit < 0) {
                        limit = controller.getManuscript().getAi().getMaxContext();
                    }

                    List<String> summaries = (List<String>) controller.getProperties().get(SUMMARIES);
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

                    String emptyTemplate = templateService.processTemplate(data.getGeneralTemplate(), "systemTemplate", templateData);
                    long baseTokens = inferenceService.countTokens(emptyTemplate) + data.getJailbreakTokens() + data.getUserPromptProcessedTokens() + 100; /* Buffer for HEADERS */

                    if (baseTokens >= limit) {
                        controller.getUIListener().onSimpleError(loc.getValue(L.ERROR_CONTEXT_INSUFFICIENT));
                    }

                    if (activeMessage != null) {
                        List<ChatMessage> chain = finalFromRoot;
                        if (!chain.isEmpty()) {
                            // remove last
                            chain = chain.subList(0, chain.size() - 1);
                        }
                        Collections.reverse(chain);
                        Iterator<ChatMessage> iterator = chain.iterator();

                        long tokens = baseTokens;
                        List<String> storyText = new ArrayList<>();

                        while (true) {
                            if (tokens > limit - 256) {
                                break;
                            }
                            if (!iterator.hasNext())
                                break;
                            ChatMessage message = iterator.next();
                            if (StringUtils.isNotBlank(message.getResponse())) {
                                storyText.add(message.getResponse());
                                tokens += message.getTokenCount() + 100; /* Buffer for dummy messages */
                            }
                        }

                        Collections.reverse(storyText);
                        controller.getProperties().put(MANUSCRIPT_CHRONICLE, storyText);

                        controller.emitEvent(Events.AFTER_MANUSCRIPT_CONCATENATION, () -> {
                            String systemPrompt = templateService.processTemplate(data.getGeneralTemplate(), "systemTemplate", templateData);
                            data.setSystemPrompt(systemPrompt);
                            data.setSystemPromptTokens(inferenceService.countTokens(systemPrompt));
                            controller.emitEvent(Events.AFTER_PREPARE_CONTENT, controller::next);
                        });
                    } else {
                        data.setSystemPrompt(emptyTemplate);
                        data.setSystemPromptTokens(baseTokens);
                        controller.emitEvent(Events.AFTER_PREPARE_CONTENT, controller::next);
                    }
                });
            };

            if (!invalidatedSummaries.isEmpty()) {
                controller.getUIListener().askQuestion("Invalidated summaries for messages with IDs: []. Continue generation?", callback, () -> controller.jumpTo(GenerationStepType.CLEANUP));
            } else {
                callback.returnFromEvent();
            }
        });
    }

    private List<String> gatherSummaries(List<ChatMessage> fromRoot, List<ChatMessage> invalidatedSummaries) throws Exception {
        List<ChatMessage> toCheck = fromRoot.reversed();

        ChatMessage current = null;
        for (ChatMessage chatMessage : toCheck) {
            if (chatMessage.getSummary() != null) {
                current = chatMessage;
                break;
            }
        }

        if (current == null) {
            return new ArrayList<>();
        }

        List<String> summaries = new ArrayList<>();
        MessageDigest digest = null;
        String lastHash = "";
        List<ChatMessage> slice = toCheck.subList(toCheck.indexOf(current), toCheck.size());

        if (slice.isEmpty()) {
            return new ArrayList<>();
        }

        for (ChatMessage message : slice) {
            if (message.getSummary() != null) {
                if (digest != null) {
                    String hash = HexFormat.of().formatHex(digest.digest());
                    if (!lastHash.equals(hash)) {
                        Summary summary = summaryService.find(current.getSummary());
                        current.setSummary(null);
                        summaryService.delete(summary, true);
                        current = chatMessageService.save(current);
                        summaries.removeLast(); // remove invalid
                        log.info("Summary invalidated");
                        invalidatedSummaries.add(current);
                    }
                }
                digest = MessageDigest.getInstance("SHA512");
                current = message;
                Summary summary = summaryService.find(current.getSummary());
                lastHash = summary.getSummaryMessageHash();
                summaries.add(summary.getSummary());
            }
            //noinspection DataFlowIssue
            digest.update(message.getResponse().getBytes(StandardCharsets.UTF_8));
        }

        String hash = HexFormat.of().formatHex(digest.digest());
        if (!lastHash.equals(hash)) {
            Summary summary = summaryService.find(current.getSummary());
            current.setSummary(null);
            summaryService.delete(summary, true);
            current = chatMessageService.save(current);
            summaries.removeLast(); // remove invalid
            log.info("Summary invalidated");
            invalidatedSummaries.add(current);
        }

        summaries = summaries.reversed();
        return summaries;
    }

    @Override
    public GenerationStepType getType() {
        return GenerationStepType.PREPARE_CONTENT;
    }
}
