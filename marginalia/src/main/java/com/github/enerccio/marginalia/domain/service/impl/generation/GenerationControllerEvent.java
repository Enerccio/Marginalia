package com.github.enerccio.marginalia.domain.service.impl.generation;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CancellationToken;

public interface GenerationControllerEvent {

    Manuscript getManuscript();
    void setManuscript(Manuscript manuscript);
    ChatMessage getMessage();
    void setMessage(ChatMessage message);
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
