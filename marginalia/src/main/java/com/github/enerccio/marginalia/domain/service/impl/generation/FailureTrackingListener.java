package com.github.enerccio.marginalia.domain.service.impl.generation;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.GenerationListener;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.GenerationFailure;

/**
 * Passes everything on to the listener of the caller and remembers the first error, so the end of the generation can
 * tell a failed one from a stopped one (the steps report errors in many places).
 */
public class FailureTrackingListener implements GenerationListener {

    private final GenerationListener delegate;
    private volatile GenerationFailure failure;

    public FailureTrackingListener(GenerationListener delegate) {
        this.delegate = delegate;
    }

    public GenerationFailure getFailure() {
        return failure;
    }

    private synchronized void remember(GenerationFailure failure) {
        if (this.failure == null) {
            this.failure = failure;
        }
    }

    @Override
    public void onNodeCreated(ChatMessage message) {
        delegate.onNodeCreated(message);
    }

    @Override
    public void onReasoningChunk(String chunk, ChatMessage message) {
        delegate.onReasoningChunk(chunk, message);
    }

    @Override
    public void onResponseChunk(String chunk, ChatMessage message) {
        delegate.onResponseChunk(chunk, message);
    }

    @Override
    public void onMetricsUpdated(ChatMessage message) {
        delegate.onMetricsUpdated(message);
    }

    @Override
    public void onComplete(ChatMessage message) {
        delegate.onComplete(message);
    }

    @Override
    public void onCancelled(ChatMessage partialMessage) {
        delegate.onCancelled(partialMessage);
    }

    @Override
    public void onError(Throwable cause) {
        remember(new GenerationFailure(cause == null ? null : cause.getMessage(), cause));
        delegate.onError(cause);
    }

    @Override
    public void onSimpleError(String error) {
        remember(new GenerationFailure(error, null));
        delegate.onSimpleError(error);
    }

    @Override
    public void onInferenceError(String description) {
        remember(new GenerationFailure(description, null));
        delegate.onInferenceError(description);
    }

    @Override
    public void onWarning(String warning) {
        delegate.onWarning(warning);
    }

    @Override
    public void askQuestion(String question, Runnable yes, Runnable no) {
        delegate.askQuestion(question, yes, no);
    }
}
