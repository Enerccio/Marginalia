package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.vaadin.flow.component.UI;

import java.util.function.Consumer;

public interface GenerationListener {
    /** Fired immediately after the initial ChatMessage tree node is persisted */
    void onNodeCreated(ChatMessage message);

    /** Fired when a chunk of reasoning/thinking text arrives (e.g. DeepSeek-R1, o1) */
    void onReasoningChunk(String chunk, ChatMessage message);

    /** Fired when a chunk of story response text arrives */
    void onResponseChunk(String chunk, ChatMessage message);

    /** Fired when generation metrics (TTFT, token counts, reasoning time) update */
    void onMetricsUpdated(ChatMessage message);

    /** Fired when generation completes successfully */
    void onComplete(ChatMessage message);

    /** Fired when generation is cancelled by the user */
    void onCancelled(ChatMessage partialMessage);

    /** Fired when an unhandled error occurs during generation */
    void onError(Throwable cause);

    /** For simple failures like validation etc. */
    void onSimpleError(String error);

    /**
     * The request to the inference provider failed (wrong key, no such model, a limit): the text says what to fix. A
     * simple failure by default; the UI also remembers it for the help assistant.
     */
    default void onInferenceError(String description) {
        onSimpleError(description);
    }

    /** Something the user should know, the generation goes on. */
    default void onWarning(String warning) {
    }

    /** Display dialog */
    void askQuestion(String question, Runnable yes, Runnable no);

    /**
     * Runs the action bound to the UI that started the generation (locked with {@code ui.access}, pushed to the browser after
     * it), for an extension that wants to show its own component to the user of the generation.
     *
     * @return whether the action was scheduled; false when the generation has no UI (an agent or a command started it), then
     * nobody can be shown anything and the action never runs. The action runs later, on the thread of the UI.
     * @throws com.vaadin.flow.component.UIDetachedException when the UI is gone
     */
    default boolean accessUI(Consumer<UI> action) {
        return false;
    }
}