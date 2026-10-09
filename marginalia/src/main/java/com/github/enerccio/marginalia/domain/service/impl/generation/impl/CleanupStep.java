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
                if (message == null || message.getId() == null) {
                    // ignore, did not even call new node
                    controller.getUIListener().onCancelled(null);
                } else {
                    message = chatMessageService.find(message);
                    if (controller.getRequest().getRequestType() == GenerationRequestType.REGENERATE) {
                        restoreRegenerated(controller, message);
                    } else {
                        removeEmptyPart(controller, message);
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

    /**
     * No text arrived - puts back the content the part had before it was cleared for regeneration.
     */
    private void restoreRegenerated(GenerationController controller, ChatMessage message) throws Exception {
        ChatMessage original = (ChatMessage) controller.getProperties().get(GenerationProperties.ORIGINAL_MESSAGE);
        if (message != null && original != null) {
            // replaces every value (attributes included), so nothing of the failed attempt stays in the part
            message.loadFrom(original);
            message = chatMessageService.save(message);
            controller.setMessage(message);
        }
        controller.getUIListener().onCancelled(message);
        controller.getUIListener().onMetricsUpdated(message);
    }

    /**
     * No text arrived - removes the new part (or swipe) and makes the previous part (or version) active again.
     */
    private void removeEmptyPart(GenerationController controller, ChatMessage message) throws Exception {
        if (message == null) {
            controller.getUIListener().onCancelled(null);
            return;
        }

        Manuscript manuscript = manuscriptService.find(controller.getManuscript());
        ChatMessage previousVersion = null;
        if (controller.getRequest().getRequestType() == GenerationRequestType.SWIPE) {
            previousVersion = chatMessageService.find(controller.getRequest().getNode());
            if (previousVersion == null) {
                List<ChatMessage> family = chatMessageService.getSwipesForMessage(message);
                Collections.reverse(family);
                for (ChatMessage m : family) {
                    if (!m.getId().equals(message.getId())) {
                        previousVersion = m;
                        break;
                    }
                }
            }
        }

        // active leaf must stop pointing at the part before it is deleted
        if (previousVersion != null) {
            chatMessageService.swipeTo(manuscript, previousVersion);
        } else {
            manuscript.setActiveLeaf(message.getParent());
        }
        controller.setManuscript(manuscriptService.save(manuscript));

        if (previousVersion != null) {
            chatMessageService.delete(message, true);
            controller.getUIListener().onCancelled(previousVersion);
            controller.getUIListener().onMetricsUpdated(previousVersion);
        } else {
            chatMessageService.deleteNodeAndMigrateChildren(message, controller.getManuscript(), true);
            controller.getUIListener().onCancelled(null);
        }
        controller.setMessage(null);
    }

    @Override
    public GenerationStepType getType() {
        return GenerationStepType.CLEANUP;
    }
}
