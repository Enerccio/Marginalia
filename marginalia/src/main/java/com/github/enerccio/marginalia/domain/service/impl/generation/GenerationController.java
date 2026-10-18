package com.github.enerccio.marginalia.domain.service.impl.generation;

import com.github.enerccio.marginalia.domain.components.TurnInput;
import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.GenerationListener;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.PrePromptData;
import org.springframework.web.context.request.RequestAttributes;

import java.util.List;
import java.util.Map;

public interface GenerationController {

    Map<String, Object> getProperties();
    GenerationRequest getRequest();

    Manuscript getManuscript();
    void setManuscript(Manuscript manuscript);
    /**
     * AI of this generation: the one given in the {@link GenerationOptions}, otherwise the manuscript's. The steps read
     * it here and never from the manuscript, which is only loaded again from the database as the same object.
     */
    AI getAi();
    void setAi(AI ai);
    /**
     * Protocol of this generation, see {@link #getAi()}.
     */
    Protocol getProtocol();
    void setProtocol(Protocol protocol);
    ChatMessage getMessage();
    void setMessage(ChatMessage message);
    TurnInput getInput();
    CancellationToken getCancellationToken();
    GenerationListener getUIListener();
    /**
     * Request context the threads of the generation run in.
     */
    RequestAttributes getRequestAttributes();
    PrePromptData getPrePromptData();
    void setPrePromptData(PrePromptData prePromptData);
    List<LLMChatMessage> getPayload();
    void setPayload(List<LLMChatMessage> payload);
    State getState();
    void setState(State state);

    void next() throws Exception;

    void jumpTo(GenerationStepType stepType) throws Exception;

    void emitEvent(Events event, FromEventCallback continueAfterEventHandling);

    Runnable wrapCallback(FromEventCallback callback);

    interface FromEventCallback {

        void returnFromEvent() throws Exception;

    }

    enum State {
        NOT_SUCCESSFUL, PARTIAL_SUCCESS, SUCCESSFUL
    }
}
