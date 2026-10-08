package com.github.enerccio.marginalia.domain.service.impl.generation;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.PrePromptData;
import com.github.enerccio.marginalia.domain.templates.TemplateContext;
import com.github.enerccio.marginalia.domain.templates.TemplateVariables;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class GenerationStepBase implements GenerationStep {
    private static final Logger log = LoggerFactory.getLogger(GenerationStepBase.class);

    @Autowired
    protected Localization loc;

    @Autowired
    protected ManuscriptService manuscriptService;

    @Autowired
    protected ChatMessageService chatMessageService;

    @Autowired
    protected TemplateService templateService;

    @Autowired
    protected InferenceServices inferenceServices;

    @Autowired
    protected AIService aiService;

    @Autowired
    protected ProtocolService protocolService;

    @Autowired
    protected LorebookService lorebookService;

    @Autowired
    protected LorebookEntryService lorebookEntryService;

    @Autowired
    protected TagRelationService tagRelationService;

    @Autowired
    protected SummaryService summaryService;

    protected abstract void onStep(GenerationController controller) throws Exception;

    @Override
    public void step(GenerationController controller) throws Exception {
        try (InRequestScope _ = new InRequestScope(controller.getRequestAttributes())) {
            if (Thread.interrupted()) {
                controller.getUIListener().onSimpleError(loc.getValue(L.MSG_INTERRUPTED));
                controller.jumpTo(GenerationStepType.CLEANUP);
                return;
            }
            if (controller.getCancellationToken().isCancelled() && getType() != GenerationStepType.CLEANUP) {
                controller.getUIListener().onCancelled(controller.getMessage());
                controller.jumpTo(GenerationStepType.CLEANUP);
                return;
            }

            try {
                onStep(controller);
            } catch (Exception e) {
                log.error(e.getMessage());
                log.debug(e.getMessage(), e);
                controller.getUIListener().onError(e);
                controller.jumpTo(GenerationStepType.CLEANUP);
            }
        }
    }

    /**
     * Returns template context shared by all templates processed in this generation (user prompt, lorebook entries,
     * master template), creating it on first use. Must be called after {@link PrePromptData} is prepared.
     */
    protected TemplateContext getTemplateContext(GenerationController controller) throws Exception {
        TemplateContext context = (TemplateContext) controller.getProperties().get(GenerationProperties.TEMPLATE_CONTEXT);
        if (context == null) {
            context = createTemplateContext(controller);
            controller.getProperties().put(GenerationProperties.TEMPLATE_CONTEXT, context);
        }
        return context;
    }

    private TemplateContext createTemplateContext(GenerationController controller) throws Exception {
        Manuscript manuscript = controller.getManuscript();
        TemplateContext context = new TemplateContext();

        TurnInput input = controller.getInput();
        if (input != null) {
            context.setPovCharacter(input.povCharacter());
            context.setPresentCharacters(input.presentCharacters());
            context.setSceneSetting(input.sceneSetting());
            context.setInstructions(input.instructions());
        }

        PrePromptData data = controller.getPrePromptData();
        if (data != null) {
            context.setNarrativePov(data.getPov());
            context.setNarrativeTense(data.getTense());
            context.setStyle(data.getStyle());
        }

        context.setManuscriptName(manuscript.getName());
        context.setManuscriptDescription(manuscript.getDescription());
        context.setPickSeed(String.valueOf(manuscript.getId()));

        AI ai = manuscript.getAi();
        if (ai != null) {
            context.setModelName(ai.getName());
            int limit = TokenLimits.contextTokens(ai, manuscript.getProtocol());
            // 0 means "not configured" - leave the macros empty instead of reporting nonsense
            if (limit > 0) {
                context.setMaxContextTokens(limit);
            }
            int responseTokens = TokenLimits.responseTokens(ai, manuscript.getProtocol());
            if (responseTokens > 0) {
                context.setMaxResponseTokens(responseTokens);
            }
        }

        context.setGenerationType(switch (controller.getRequest().getRequestType()) {
            case NEW_MESSAGE -> "normal";
            case REGENERATE -> "regenerate";
            case SWIPE -> "swipe";
        });

        // story so far, the same branch PrepareContentStep uses
        ChatMessage activeMessage = chatMessageService.find(manuscript.getActiveLeaf());
        if (activeMessage != null) {
            List<ChatMessage> branch = new ArrayList<>(chatMessageService.getBranchFromLeaf(activeMessage));
            if (!branch.isEmpty() && controller.getRequest().getRequestType() == GenerationRequestType.REGENERATE) {
                branch.removeLast();
            }

            List<String> story = new ArrayList<>();
            for (ChatMessage message : branch) {
                if (message.getResponse() != null && !message.getResponse().isBlank()) {
                    story.add(message.getResponse());
                }
            }
            context.setStoryMessages(story);

            if (!branch.isEmpty()) {
                ChatMessage last = branch.getLast();
                context.setLastInstructions(last.getInstructions());
                context.setLastMessageTime(last.getRequest());
                // local variables follow the story branch
                context.getVariables().loadFrom(TemplateVariables.Scope.LOCAL, last.getAttributes());
            }

            for (ChatMessage message : branch.reversed()) {
                if (message.getSummary() != null) {
                    Summary summary = summaryService.find(message.getSummary());
                    if (summary != null) {
                        context.setLatestSummary(summary.getSummary());
                    }
                    break;
                }
            }
        }

        context.getVariables().loadFrom(TemplateVariables.Scope.GLOBAL, manuscript.getAttributes());
        log.debug("Template context created, variables: {}", context.getVariables());
        return context;
    }

    protected <T> void forEachAsync(
            GenerationController controller,
            Iterable<T> items,
            AsyncItemProcessor<T> processor,
            GenerationController.FromEventCallback onComplete) throws Exception {

        if (items == null) {
            if (onComplete != null) {
                onComplete.returnFromEvent();
            }
            return;
        }

        Iterator<T> iterator = items.iterator();
        processNextAsync(controller, iterator, processor, onComplete);
    }

    private <T> void processNextAsync(
            GenerationController controller,
            Iterator<T> iterator,
            AsyncItemProcessor<T> processor,
            GenerationController.FromEventCallback onComplete) throws Exception {
        try (InRequestScope _ = new InRequestScope(controller.getRequestAttributes())) {
            if (Thread.interrupted()) {
                controller.getUIListener().onSimpleError(loc.getValue(L.MSG_INTERRUPTED));
                controller.jumpTo(GenerationStepType.CLEANUP);
                return;
            }

            if (!iterator.hasNext()) {
                if (onComplete != null) {
                    onComplete.returnFromEvent();
                }
                return;
            }

            T item = iterator.next();
            processor.process(item, () -> processNextAsync(controller, iterator, processor, onComplete));
        } catch (Exception e) {
            log.error("Error during async iteration: {}", e.getMessage(), e);
            controller.getUIListener().onError(e);
            controller.jumpTo(GenerationStepType.CLEANUP);
        }
    }

    @FunctionalInterface
    public interface AsyncItemProcessor<T> {
        void process(T item, GenerationController.FromEventCallback next) throws Exception;
    }

}

