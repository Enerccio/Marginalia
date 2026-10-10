package com.github.enerccio.marginalia.test;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.GenerationListener;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * {@link GenerationListener} standing in for the story editor UI: records everything and lets the test wait for the
 * end of the generation (the cleanup step always ends with {@code onComplete} or {@code onCancelled}).
 */
public class GenerationRun implements GenerationListener {

    public enum Outcome { COMPLETED, CANCELLED }

    private final CompletableFuture<Outcome> result = new CompletableFuture<>();
    private final StringBuilder response = new StringBuilder();
    private final StringBuilder reasoning = new StringBuilder();
    private final List<Throwable> errors = new CopyOnWriteArrayList<>();
    private final List<String> simpleErrors = new CopyOnWriteArrayList<>();
    private final List<String> warnings = new CopyOnWriteArrayList<>();
    private volatile ChatMessage message;

    @Override
    public void onNodeCreated(ChatMessage message) {
        this.message = message;
    }

    @Override
    public synchronized void onReasoningChunk(String chunk, ChatMessage message) {
        reasoning.append(chunk);
    }

    @Override
    public synchronized void onResponseChunk(String chunk, ChatMessage message) {
        response.append(chunk);
    }

    @Override
    public void onMetricsUpdated(ChatMessage message) {
    }

    @Override
    public void onComplete(ChatMessage message) {
        this.message = message;
        result.complete(Outcome.COMPLETED);
    }

    @Override
    public void onCancelled(ChatMessage partialMessage) {
        this.message = partialMessage;
        result.complete(Outcome.CANCELLED);
    }

    @Override
    public void onError(Throwable cause) {
        errors.add(cause);
    }

    @Override
    public void onSimpleError(String error) {
        simpleErrors.add(error);
    }

    @Override
    public void onWarning(String warning) {
        warnings.add(warning);
    }

    @Override
    public void askQuestion(String question, Runnable yes, Runnable no) {
        yes.run();
    }

    public Outcome await(Duration timeout) throws Exception {
        return result.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    public synchronized String getResponse() {
        return response.toString();
    }

    public synchronized String getReasoning() {
        return reasoning.toString();
    }

    public ChatMessage getMessage() {
        return message;
    }

    public List<Throwable> getErrors() {
        return List.copyOf(errors);
    }

    public List<String> getSimpleErrors() {
        return List.copyOf(simpleErrors);
    }

    public List<String> getWarnings() {
        return List.copyOf(warnings);
    }
}
