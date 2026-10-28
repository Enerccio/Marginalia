package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Annotation;
import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationOptions;

/**
 * Lets the model write a part of the story again. Unlike a regeneration the part does not have to be the last one and
 * the model is told what the author wants changed: it gets the part itself, the story before it (the older story as
 * summaries, the way a generation sees it), as much of the story after it as fits, and the data the part was written
 * with, in the redraft prompt of the book ({@link ManuscriptService#getRedraftPrompt}).
 * <p>
 * The redraft can be limited to a {@link Passage} of the part (the author selected it); the rest of the part is then not written again.
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
    default CancellationToken redraft(Manuscript manuscript, ChatMessage part, String instructions, String prompt, GenerationOptions options,
                                      Callback callback) throws Exception {
        return redraft(manuscript, part, instructions, prompt, options, false, callback);
    }

    /**
     * As {@link #redraft(Manuscript, ChatMessage, String, String, GenerationOptions, Callback)}, and the model is told the notes of the
     * author on the part (its open annotations, the variable {@code annotations} of the prompt) to deal with, so that redrafting to address
     * the open annotations is one step.
     *
     * @param withAnnotations whether the open annotations of the part are given to the model
     */
    default CancellationToken redraft(Manuscript manuscript, ChatMessage part, String instructions, String prompt, GenerationOptions options,
                                      boolean withAnnotations, Callback callback) throws Exception {
        return redraft(manuscript, part, instructions, prompt, options, withAnnotations, null, callback);
    }

    /**
     * As {@link #redraft(Manuscript, ChatMessage, String, String, GenerationOptions, boolean, Callback)} for one passage of the part only: the
     * model gets the whole part, with the passage marked, and writes just the text that replaces the passage ({@code onFinished} gets that
     * text, not the part). The author joins it with the rest of the part. With {@code withAnnotations} only the open annotations whose passage
     * overlaps the redrafted one are given.
     *
     * @param passage the passage of the part to write again, {@code null} for the whole part
     * @throws IllegalArgumentException when the passage is not inside the text of the part or has no text
     */
    CancellationToken redraft(Manuscript manuscript, ChatMessage part, String instructions, String prompt, GenerationOptions options,
                              boolean withAnnotations, Passage passage, Callback callback) throws Exception;

    /**
     * A passage of the text of a part, as offsets in it.
     *
     * @param start the first character of the passage
     * @param end   the character after the last one
     */
    record Passage(int start, int end) {

        /**
         * Whether the passage of the annotation overlaps this one; a note about the whole part, or one that lost its passage, does not.
         */
        public boolean overlaps(Annotation annotation) {
            return annotation.hasRange() && annotation.getStart() < end && annotation.getEnd() > start;
        }
    }

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
