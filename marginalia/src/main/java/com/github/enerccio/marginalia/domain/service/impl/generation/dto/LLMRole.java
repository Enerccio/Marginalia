package com.github.enerccio.marginalia.domain.service.impl.generation.dto;

public enum LLMRole {

    SYSTEM, ASSISTANT, USER,
    /** Result of a function the assistant called, see {@link LLMChatMessage#ofToolResult}. */
    TOOL

}
