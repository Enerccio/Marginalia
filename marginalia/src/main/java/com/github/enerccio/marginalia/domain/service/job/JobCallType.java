package com.github.enerccio.marginalia.domain.service.job;

/**
 * What a call of a job asked of its backend.
 */
public enum JobCallType {
    /** A request to a language model. */
    LLM,
    /** Speech: text to speech, or anything else an audio backend does. */
    AUDIO,
    /** An image backend. */
    IMAGE,
    /** Any other external AI service. */
    OTHER
}
