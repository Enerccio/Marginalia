package com.github.enerccio.marginalia.domain.service.impl.inference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.enerccio.marginalia.domain.collections.PromptPlacement;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.service.function.FunctionDefinition;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Puts parts of a request where the AI wants them ({@link PromptPlacement}): a text into the system message or at the start or
 * end of the latest user message, the images of a message before its text, the definitions of the functions and the rule
 * about calls as the AI is set ({@link #forRequest}, applied to every request by {@link PlacedInferenceService}). Never changes
 * the messages it is given: a message it changes is a {@link LLMChatMessage#copy() copy} in a new list.
 */
public final class PromptPlacements {

    private static final ObjectMapper JSON = new ObjectMapper();

    /** The heading of the definitions written into a user message. */
    static final String FUNCTIONS_HEADING = "Functions you can call:";

    private PromptPlacements() {
    }

    /**
     * The payload with the text placed: appended to the first system message ({@link PromptPlacement#SYSTEM}; a system message is
     * added at the start when there is none), before or after the text of the latest user message ({@link PromptPlacement#BEFORE},
     * {@link PromptPlacement#AFTER}; into the system message when there is no user message). A blank text changes nothing.
     */
    public static List<LLMChatMessage> place(List<LLMChatMessage> payload, String text, PromptPlacement where) {
        if (StringUtils.isBlank(text) || payload == null) {
            return payload;
        }
        List<LLMChatMessage> placed = new ArrayList<>(payload);
        int user = where == PromptPlacement.SYSTEM ? -1 : lastIndexOf(placed, LLMRole.USER);
        if (user >= 0) {
            LLMChatMessage message = placed.get(user).copy();
            String content = message.getContent();
            if (StringUtils.isBlank(content)) {
                message.setContent(text);
            } else {
                message.setContent(where == PromptPlacement.BEFORE ? text + "\n\n" + content : content + "\n\n" + text);
            }
            placed.set(user, message);
            return placed;
        }
        int system = firstIndexOf(placed, LLMRole.SYSTEM);
        if (system < 0) {
            placed.addFirst(LLMChatMessage.of(LLMRole.SYSTEM, text));
            return placed;
        }
        LLMChatMessage message = placed.get(system).copy();
        message.setContent(StringUtils.isBlank(message.getContent()) ? text : message.getContent() + "\n\n" + text);
        placed.set(system, message);
        return placed;
    }

    /**
     * The payload as the AI wants it sent: the images before their text ({@link AI#getImagePlacement()} {@link PromptPlacement#BEFORE}
     * of a multimodal AI), and when functions are offered and may be called, the {@linkplain #describeFunctions definitions} in the
     * latest user message ({@link AI#getFunctionsPlacement()} other than {@link PromptPlacement#SYSTEM}), then the
     * {@link AI#getFunctionRule() rule} where it is set to go.
     *
     * @param noFunctionCalls the round may not call (the last round of an agent): neither the definitions nor the rule are added
     */
    public static List<LLMChatMessage> forRequest(AI ai, List<LLMChatMessage> payload, FunctionSet functions, boolean noFunctionCalls) {
        if (ai == null || payload == null) {
            return payload;
        }
        List<LLMChatMessage> placed = payload;
        if (ai.isMultimodal() && ai.getImagePlacement() == PromptPlacement.BEFORE) {
            placed = new ArrayList<>(payload);
            for (int i = 0; i < placed.size(); i++) {
                LLMChatMessage message = placed.get(i);
                if (!message.isImagesFirst() && hasImage(message)) {
                    LLMChatMessage copy = message.copy();
                    copy.setImagesFirst(true);
                    placed.set(i, copy);
                }
            }
        }
        if (functions != null && !functions.isEmpty() && !noFunctionCalls) {
            if (ai.getFunctionsPlacement() != PromptPlacement.SYSTEM) {
                placed = place(placed, describeFunctions(functions), ai.getFunctionsPlacement());
            }
            placed = place(placed, ai.getFunctionRule(), ai.getFunctionRulePlacement());
        }
        return placed;
    }

    /** Whether {@link #forRequest} can change anything for the AI. */
    public static boolean needed(AI ai) {
        return ai != null && ((ai.isMultimodal() && ai.getImagePlacement() == PromptPlacement.BEFORE)
                || ai.getFunctionsPlacement() != PromptPlacement.SYSTEM || StringUtils.isNotBlank(ai.getFunctionRule()));
    }

    /** The definitions as text: a heading and a JSON array of the functions with their name, description and parameters. */
    public static String describeFunctions(FunctionSet functions) {
        ArrayNode list = JSON.createArrayNode();
        for (FunctionDefinition definition : functions.getDefinitions()) {
            ObjectNode function = list.addObject();
            function.put("name", definition.getName());
            if (StringUtils.isNotBlank(definition.getDescription())) {
                function.put("description", definition.getDescription());
            }
            if (definition.getParametersSchema() != null) {
                function.set("parameters", definition.getParametersSchema());
            }
        }
        try {
            return FUNCTIONS_HEADING + "\n" + JSON.writerWithDefaultPrettyPrinter().writeValueAsString(list);
        } catch (Exception e) {
            throw new IllegalStateException("The function definitions can't be written", e);
        }
    }

    private static boolean hasImage(LLMChatMessage message) {
        for (LLMAttachment attachment : message.getAttachments()) {
            LLMAttachment.Kind kind = attachment.getKind() != null ? attachment.getKind() : LLMAttachment.Kind.fromMimeType(attachment.getMimeType());
            if (kind == LLMAttachment.Kind.IMAGE) {
                return true;
            }
        }
        return false;
    }

    private static int lastIndexOf(List<LLMChatMessage> payload, LLMRole role) {
        for (int i = payload.size() - 1; i >= 0; i--) {
            if (payload.get(i).getRole() == role) {
                return i;
            }
        }
        return -1;
    }

    private static int firstIndexOf(List<LLMChatMessage> payload, LLMRole role) {
        for (int i = 0; i < payload.size(); i++) {
            if (payload.get(i).getRole() == role) {
                return i;
            }
        }
        return -1;
    }
}
