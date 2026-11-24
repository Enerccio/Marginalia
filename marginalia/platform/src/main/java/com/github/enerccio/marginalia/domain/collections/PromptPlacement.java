package com.github.enerccio.marginalia.domain.collections;

/**
 * Where a part of the request goes (an AI setting, see {@code AI}): the images of a message, the definitions of the functions,
 * the rule that tells the model to act through calls. Chat templates differ: Gemma 4, for example, follows a rule in the latest
 * user message better than one in the system prompt, and wants images before the text.
 */
public enum PromptPlacement {

    /** In the system message (for images: the images of the system prompt in the system message itself). */
    SYSTEM,

    /** At the start of the latest user message (for images: before the text of their message). */
    BEFORE,

    /** At the end of the latest user message (for images: after the text of their message). */
    AFTER

}
