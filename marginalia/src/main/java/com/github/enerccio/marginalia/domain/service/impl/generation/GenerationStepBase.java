package com.github.enerccio.marginalia.domain.service.impl.generation;

import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Iterator;

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

    protected abstract void onStep(GenerationController controller) throws Exception;

    @Override
    public void step(GenerationController controller) throws Exception {
        ThreadCopyRequestAttributes attributes = controller.getRequestAttributes();
        RequestContextHolder.setRequestAttributes(attributes);
        try {
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
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
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

        ThreadCopyRequestAttributes attributes = controller.getRequestAttributes();
        RequestContextHolder.setRequestAttributes(attributes);

        try {
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
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @FunctionalInterface
    public interface AsyncItemProcessor<T> {
        void process(T item, GenerationController.FromEventCallback next) throws Exception;
    }

}
