package com.github.enerccio.marginalia.domain.service.impl.generation;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CancellationToken;
import com.github.enerccio.marginalia.domain.service.GenerationListener;
import com.github.enerccio.marginalia.domain.service.TurnInput;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.PrePromptData;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;

import java.util.List;
import java.util.Map;

public interface GenerationController {

    Map<String, Object> getProperties();
    GenerationRequest getRequest();

    Manuscript getManuscript();
    void setManuscript(Manuscript manuscript);
    ChatMessage getMessage();
    void setMessage(ChatMessage message);
    TurnInput getInput();
    CancellationToken getCancellationToken();
    GenerationListener getUIListener();
    ThreadCopyRequestAttributes getRequestAttributes();
    PrePromptData getPrePromptData();
    void setPrePromptData(PrePromptData prePromptData);
    List<LLMChatMessage> getPayload();
    void setPayload(List<LLMChatMessage> payload);

    void next() throws Exception;

    void jumpTo(GenerationStepType stepType) throws Exception;

    void emitEvent(Events event, FromEventCallback continueAfterEventHandling) throws Exception;

    interface FromEventCallback {

        void returnFromEvent() throws Exception;

    }
}
