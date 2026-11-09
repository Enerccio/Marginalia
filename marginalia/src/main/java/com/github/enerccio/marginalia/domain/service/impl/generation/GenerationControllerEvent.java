package com.github.enerccio.marginalia.domain.service.impl.generation;

import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.PrePromptData;
import com.vaadin.flow.component.UI;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface GenerationControllerEvent {

    Manuscript getManuscript();
    void setManuscript(Manuscript manuscript);

    /**
     * AI the generation really uses, which is not always the manuscript's (see {@link GenerationOptions}): count tokens
     * and read limits with this one.
     */
    AI getAi();

    /**
     * Protocol the generation really uses, see {@link #getAi()}.
     */
    Protocol getProtocol();

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

    /**
     * Runs the action bound to the UI that started the generation ({@code ui.access} and a push afterwards), so that a listener can
     * show its own component to the user of the generation (a dialog). The listener that waits for the user holds the chain: it calls
     * {@link EventChain#next()} when the user answered, and to stop the generation it cancels first
     * ({@code getCancellationToken().cancel()}, then {@code chain.next()}; the request is not sent and the part started for it is
     * removed). It also goes on when the token is cancelled some other way ({@code getCancellationToken().onCancel(...)}), or the
     * generation waits for ever. The action and what it starts run on the thread of the UI, not in the request scope of the
     * generation: hand the work on through the chain.
     *
     * @return whether the action was scheduled; false when the generation has no UI (an agent or a command started it), then the
     * action never runs and the listener must not wait. Throws {@code UIDetachedException} when the user closed the page.
     */
    boolean accessUI(Consumer<UI> action);

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
