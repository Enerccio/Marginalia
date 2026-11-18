package com.github.enerccio.marginalia.domain.collections;

/**
 * Whether the thinking of a reasoning model is sent back to it in the next request, as the {@code reasoning_content} of the
 * assistant message it belongs to. A model that is trained for it (Gemma 4 between the calls of one turn) goes on with its own
 * thoughts; most models and servers expect none, so the default is {@link #NONE}.
 */
public enum ReasoningPassthrough {

    /** The thinking is never sent back. */
    NONE,

    /** The thinking of an answer that called tools is sent back while the tool calls of that turn go on, not in a later turn. */
    TOOL_CALLS,

    /** The thinking of every answer that has it is sent back, in a later turn too. */
    ALWAYS

}
