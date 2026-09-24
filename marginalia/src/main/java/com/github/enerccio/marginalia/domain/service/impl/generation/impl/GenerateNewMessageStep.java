package com.github.enerccio.marginalia.domain.service.impl.generation.impl;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.TurnInput;
import com.github.enerccio.marginalia.domain.service.impl.generation.Events;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationController;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationStepBase;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationStepType;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

public class GenerateNewMessageStep extends GenerationStepBase {
    private static final Logger log = LoggerFactory.getLogger(GenerateNewMessageStep.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    protected void onStep(GenerationController controller) throws Exception {
        controller.emitEvent(Events.BEFORE_GENERATE_NEW_MESSAGE, () -> {
            Manuscript manuscript = controller.getManuscript();
            InferenceService inferenceService = inferenceServices.forAI(manuscript.getAi());
            TurnInput input = controller.getInput();
            List<LLMChatMessage> payload = controller.getPayload();

            ChatMessage parentLeaf = chatMessageService.find(manuscript.getActiveLeaf());

            ChatMessage initialNode;

            switch (controller.getRequest().getRequestType()) {
                case NEW_MESSAGE, SWIPE -> {
                    initialNode = new ChatMessage();
                }
                case REGENERATE -> {
                    initialNode = chatMessageService.find(controller.getRequest().getNode());
                }
                default -> throw new RuntimeException("FIX STATES!");
            }

            initialNode.setSceneSetting(input.sceneSetting());
            initialNode.setPovCharacter(input.povCharacter());
            initialNode.setPresentCharacters(input.presentCharacters());
            initialNode.setInstructions(input.instructions());
            initialNode.setResponseReasoning("");
            initialNode.setResponse("");
            initialNode.setBuiltPromptTokens(controller.getPrePromptData().getSystemPromptTokens() + controller.getPrePromptData().getUserPromptProcessedTokens());
            initialNode.setModelUsed(manuscript.getAi().getName());
            initialNode.setProtocolUsed(manuscript.getProtocol().getName());
            initialNode.setBuiltPrompt(gson.toJson(payload));
            initialNode.setBuiltPromptTokens(inferenceService.countTokens(initialNode.getBuiltPrompt()));
            initialNode.setRequest(new Date());

            ChatMessage node;

            switch (controller.getRequest().getRequestType()) {
                case NEW_MESSAGE -> {
                    if (parentLeaf == null) {
                        node = chatMessageService.createRoot(manuscript, initialNode);
                    } else {
                        initialNode.setParentScript(manuscript);
                        initialNode.setParent(parentLeaf);
                        node = chatMessageService.addChild(parentLeaf, initialNode);
                    }
                }
                case REGENERATE -> {
                    node = initialNode;
                }
                case SWIPE -> {
                    ChatMessage parent = chatMessageService.getParent(controller.getRequest().getNode());
                    if (parent == null) {
                        node = chatMessageService.createRoot(manuscript, initialNode);
                    } else {
                        initialNode.setParentScript(manuscript);
                        initialNode.setParent(parent);
                        node = chatMessageService.addChild(parent, initialNode);
                    }
                }
                default -> throw new RuntimeException("FIX STATES!");
            }

            log.debug("ChatMessage created: {}", node.getId());

            manuscript.setActiveLeaf(node);
            controller.setManuscript(manuscriptService.save(manuscript));
            controller.setMessage(node);
            controller.emitEvent(Events.AFTER_GENERATE_NEW_MESSAGE, () -> {
                controller.getUIListener().onNodeCreated(controller.getMessage());
                controller.next();
            });
        });
    }

    @Override
    public GenerationStepType getType() {
        return GenerationStepType.GENERATE_NEW_MESSAGE;
    }
}
