package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationOptions;

/**
 * Lets the model write a part of the story again. Unlike a regeneration the part does not have to be the last one and
 * the model is told what the author wants changed: it gets the part itself, the story before it (the older story as
 * summaries, the way a generation sees it), as much of the story after it as fits, and the data the part was written
 * with, in the redraft prompt of the book ({@link ManuscriptService#getRedraftPrompt}).
 * <p>
 * Nothing is stored by the redraft: the text goes to the {@link Callback} and the author decides whether it replaces
 * the part (see {@link ChatMessageService#updateResponse}).
 */
public interface RedraftService {

    /**
     * Starts the redraft of the part, the answer of the model goes to the callback.
     *
     * @param instructions what the author wants changed, may be blank (the part is written again)
     * @return the token that stops it, {@code null} when it cannot start (the book has no AI or protocol)
     * @throws ContextInsufficient when the prompt does not fit the context of the AI even without the story around the part
     * @throws IllegalArgumentException when the part is not a part of the book of the user or has no text
     */
    default CancellationToken redraft(Manuscript manuscript, ChatMessage part, String instructions, Callback callback) throws Exception {
        return redraft(manuscript, part, instructions, null, GenerationOptions.defaults(), callback);
    }

    /**
     * As {@link #redraft(Manuscript, ChatMessage, String, Callback)} with another prompt and, optionally, another model.
     *
     * @param prompt the redraft prompt (a template of {@code RedraftTemplateData}) to use instead of the one of the book,
     *               blank for the one of the book
     * @param options the AI and the protocol to write with when they are set (see {@link GenerationOptions}), otherwise
     *                the ones of the book; the book is not changed by them
     */
    CancellationToken redraft(Manuscript manuscript, ChatMessage part, String instructions, String prompt, GenerationOptions options,
                              Callback callback) throws Exception;

    /**
     * What the redraft ends in; each call is made on the thread of the inference.
     */
    interface Callback {

        /**
         * Text and reasoning received so far.
         */
        void onProgress(String reasoning, String text) throws Exception;

        /**
         * The model is done, {@code text} is the redrafted part.
         */
        void onFinished(String reasoning, String text) throws Exception;

        /**
         * The author stopped the redraft, {@code text} is what arrived until then.
         */
        void onTerminated(String reasoning, String text) throws Exception;

        void onError(Throwable throwable) throws Exception;

    }

    /**
     * The redraft prompt and the part need more tokens than the AI takes.
     */
    class ContextInsufficient extends Exception {
        private final long required;
        private final long max;

        public ContextInsufficient(long required, long max) {
            super("The redraft needs " + required + " tokens, the context has " + max);
            this.required = required;
            this.max = max;
        }

        public long getRequired() {
            return required;
        }

        public long getMax() {
            return max;
        }
    }
}
