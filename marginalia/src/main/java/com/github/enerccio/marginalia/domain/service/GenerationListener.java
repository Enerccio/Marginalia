package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;

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
}