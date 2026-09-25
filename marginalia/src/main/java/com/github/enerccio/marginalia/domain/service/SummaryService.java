package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.model.impl.Summary;
import com.github.enerccio.marginalia.domain.repository.SummaryRepository;

public interface SummaryService extends ExtendableService<Summary, SummaryRepository> {

    CancellationToken createSummary(Manuscript manuscript, ChatMessage from, AsyncCallback callback) throws Exception;

    interface AsyncCallback {

        void onSummaryProgress(String reasoning, String summary) throws Exception;
        void onSummaryFinished(Summary summary) throws Exception;
        void onSummaryTerminated() throws Exception;
        void onError(Throwable throwable) throws Exception;

    }

}
