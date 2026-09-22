package com.github.enerccio.marginalia.domain.service.impl.generation;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CancellationToken;

public interface GenerationController {

    Manuscript getManuscript();
    void setManuscript(Manuscript manuscript);
    ChatMessage getMessage();
    void setMessage(ChatMessage message);
    CancellationToken getCancellationToken();

    void next() throws Exception;
    void emitEvent(Events event, Runnable continueAfterEventHandling) throws Exception;

}
