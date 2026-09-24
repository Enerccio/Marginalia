package com.github.enerccio.marginalia.domain.service.impl.generation.impl;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.impl.generation.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationController.State;

import java.util.Collections;
import java.util.List;

public class CleanupStep extends GenerationStepBase {

    @Override
    protected void onStep(GenerationController controller) throws Exception {
        controller.emitEvent(Events.BEFORE_CLEANUP, () -> {

            if (controller.getState() == State.NOT_SUCCESSFUL) {
                ChatMessage message = controller.getMessage();
                if (message.getId() == null) {
                    // ignore, did not even call new node
                } else {
                    message = chatMessageService.find(message);
                    if (controller.getRequest().getRequestType() == GenerationRequestType.REGENERATE) {
                        // treat it as partial success because it was modified
                        controller.getUIListener().onCancelled(message);
                        controller.getUIListener().onMetricsUpdated(controller.getMessage());
                    } else {
                        Manuscript manuscript = manuscriptService.find(controller.getManuscript());
                        if (controller.getRequest().getRequestType() == GenerationRequestType.NEW_MESSAGE) {
                            chatMessageService.deleteNodeAndMigrateChildren(message, manuscript, true);
                            controller.getUIListener().onCancelled(null);
                        } else {
                            List<ChatMessage> family = chatMessageService.getSwipesForMessage(message);
                            Collections.reverse(family);
                            for (ChatMessage m : family) {
                                if (!m.getId().equals(message.getId())) {
                                    chatMessageService.swipeTo(manuscript, m);
                                    controller.getUIListener().onCancelled(m);
                                    controller.getUIListener().onMetricsUpdated(m);
                                    break;
                                }
                            }
                            chatMessageService.delete(message, true);
                        }
                    }
                }
            } else if (controller.getState() == State.PARTIAL_SUCCESS) {
                controller.getUIListener().onCancelled(controller.getMessage());
                controller.getUIListener().onMetricsUpdated(controller.getMessage());
            } else {
                controller.getUIListener().onComplete(controller.getMessage());
                controller.getUIListener().onMetricsUpdated(controller.getMessage());
            }

            controller.next();
        });
    }

    @Override
    public GenerationStepType getType() {
        return GenerationStepType.CLEANUP;
    }
}
