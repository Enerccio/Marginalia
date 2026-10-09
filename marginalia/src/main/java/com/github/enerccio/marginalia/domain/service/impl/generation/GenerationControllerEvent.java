package com.github.enerccio.marginalia.domain.service.impl.generation;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.TurnInput;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.PrePromptData;

import java.util.List;
import java.util.Map;

public interface GenerationControllerEvent {

    Manuscript getManuscript();
    void setManuscript(Manuscript manuscript);
    /**
     * Message being generated, available from {@link Events#AFTER_GENERATE_NEW_MESSAGE}.
     */
    ChatMessage getMessage();
    void setMessage(ChatMessage message);

    /**
     * Generation request (type and target node for regenerate/swipe).
     */
    GenerationRequest getRequest();

    /**
     * Turn parameters entered by user (instructions, scene, characters).
     */
    TurnInput getInput();

    /**
     * Prompt data, available from {@link Events#AFTER_STATIC_TEMPLATE_DATA}, processed user prompt from
     * {@link Events#AFTER_PREPARE_CONSTANT_DATA}.
     */
    PrePromptData getPrePromptData();
    void setPrePromptData(PrePromptData prePromptData);

    /**
     * Messages sent to the model, available from {@link Events#AFTER_PREPARE_PAYLOAD}. Changes are sent to inference.
     */
    List<LLMChatMessage> getPayload();
    void setPayload(List<LLMChatMessage> payload);

    /**
     * Shared generation state, keys and their availability are listed in {@link GenerationProperties}.
     * Listeners can also store their own values, prefixed with the extension's package to avoid clashes.
     */
    Map<String, Object> getProperties();

    @SuppressWarnings("unchecked")
    default <T> T getProperty(String key) {
        return (T) getProperties().get(key);
    }

    CancellationToken getCancellationToken();

    void terminateEvents();

    @FunctionalInterface
    interface Registration {
        void unregister();
    }

    interface EventChain {
        void next();

        void terminate();
    }

}
