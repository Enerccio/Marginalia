package com.github.enerccio.marginalia.domain.service.impl.generation.impl;

import com.github.enerccio.marginalia.domain.components.PromptTokenBreakdown;
import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.MessagePrompt;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.impl.generation.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.templates.TemplateVariables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GenerateNewMessageStep extends GenerationStepBase {
    private static final Logger log = LoggerFactory.getLogger(GenerateNewMessageStep.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    protected void onStep(GenerationController controller) throws Exception {
        controller.emitEvent(Events.BEFORE_GENERATE_NEW_MESSAGE, () -> {
            Manuscript manuscript = controller.getManuscript();
            InferenceService inferenceService = inferenceServices.forAIOrThrow(controller.getAi());
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
                    // the text that is about to be cleared is in the history
                    messageRevisionService.ensureCurrent(initialNode);
                    // the part is cleared below, keep a full copy so cleanup can restore it if no text arrives
                    controller.getProperties().put(GenerationProperties.ORIGINAL_MESSAGE, ChatMessage.copyOf(initialNode));
                    MessagePrompt originalPrompt = messagePromptService.findFor(initialNode);
                    if (originalPrompt != null) {
                        controller.getProperties().put(GenerationProperties.ORIGINAL_PROMPT, originalPrompt);
                    }
                }
                default -> throw new RuntimeException("FIX STATES!");
            }

            initialNode.setSceneSetting(input.sceneSetting());
            initialNode.setPovCharacter(input.povCharacter());
            initialNode.setPresentCharacters(input.presentCharacters());
            initialNode.setInstructions(input.instructions());
            initialNode.setResponseReasoning("");
            initialNode.setResponse("");
            initialNode.setOriginalResponse("");
            initialNode.setTokenCount(0);
            initialNode.setReasoningEnd(null);
            initialNode.setTokenReasoningCount(null);
            initialNode.setBuiltPromptTokens(controller.getPrePromptData().getSystemPromptTokens() + controller.getPrePromptData().getUserPromptProcessedTokens());
            initialNode.setModelUsed(controller.getAi().getName());
            initialNode.setProtocolUsed(controller.getProtocol().getName());
            String builtPrompt = gson.toJson(payload);
            initialNode.setBuiltPromptTokens(inferenceService.countTokens(builtPrompt));
            initialNode.setTokenBreakdown(createTokenBreakdown(controller, inferenceService, payload));
            initialNode.setRequest(new Date());

            // persist template variables - local ones follow the branch, global ones belong to the manuscript
            TemplateVariables variables = getTemplateContext(controller).getVariables();
            variables.storeTo(TemplateVariables.Scope.LOCAL, initialNode.getAttributes());

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
                    node = chatMessageService.save(initialNode);
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
            // set right away, so cleanup removes (or restores) the part if anything below fails
            controller.setMessage(node);
            messagePromptService.store(node, builtPrompt, controller.getPrePromptData().getBackgroundLore());

            // the book is loaded again: only the global variables and the active leaf are written, not what the generation
            // started with (the user may have changed the settings or moved the book since)
            ChatMessage created = node;
            controller.setManuscript(manuscriptService.update(manuscript, book -> {
                variables.storeTo(TemplateVariables.Scope.GLOBAL, book.getAttributes());
                book.setActiveLeaf(created);
            }));
            // state stays NOT_SUCCESSFUL until the first text arrives (InferenceStep), so a failed request doesn't
            // leave an empty part behind
            controller.emitEvent(Events.AFTER_GENERATE_NEW_MESSAGE, () -> {
                controller.getUIListener().onNodeCreated(controller.getMessage());
                controller.next();
            });
        });
    }

    /**
     * Breakdown filled by {@link PrepareContentStep} with the tokens of each message of the final payload (listeners
     * may have changed it). Null when the content step did not run.
     */
    private PromptTokenBreakdown createTokenBreakdown(GenerationController controller, InferenceService inferenceService,
                                                      List<LLMChatMessage> payload) throws Exception {
        PromptTokenBreakdown breakdown = (PromptTokenBreakdown) controller.getProperties().get(GenerationProperties.TOKEN_BREAKDOWN);
        if (breakdown == null || payload == null) {
            return null;
        }
        // the generated "[ Generate more story. ]" messages repeat
        Map<String, Long> counted = new HashMap<>();
        List<PromptTokenBreakdown.MessageTokens> messages = new ArrayList<>();
        for (LLMChatMessage message : payload) {
            String content = message.getContent();
            Long tokens = counted.get(content);
            if (tokens == null) {
                tokens = content == null ? 0 : inferenceService.countTokens(content);
                if (content != null) {
                    counted.put(content, tokens);
                }
            }
            messages.add(new PromptTokenBreakdown.MessageTokens(message.getRole() == null ? null : message.getRole().name(), tokens));
        }
        breakdown.setMessages(messages);
        return breakdown;
    }

    @Override
    public GenerationStepType getType() {
        return GenerationStepType.GENERATE_NEW_MESSAGE;
    }
}

