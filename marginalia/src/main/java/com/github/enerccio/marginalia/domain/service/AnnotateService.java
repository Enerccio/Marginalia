package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.*;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationOptions;

import java.util.List;

/**
 * Lets a model annotate a part of the story: it reads the part with the story around it and the lore, in the annotate prompt of the
 * book ({@link ManuscriptService#getAnnotatePrompt}), and answers with notes for the author, each about a passage it quotes. Like the
 * redraft ({@link RedraftService}) it stores nothing: the notes are proposals the author reviews, and only the ones they accept
 * become annotations ({@link #accept}).
 * <p>
 * The model never gives offsets (it can't count): it quotes, and the quote is found in the text of the part. A quote that is not
 * found is still a proposal, about the whole part, and says so.
 */
public interface AnnotateService {

    /**
     * How the notes are asked for and read.
     */
    enum Mode {
        /**
         * Structured when the AI can do it, else delimited text.
         */
        AUTO,
        /**
         * The answer is JSON that has to match a schema; one more try when it does not.
         */
        STRUCTURED,
        /**
         * The answer is text in blocks ({@code > quote} and the note), read leniently: for models that don't keep to a schema.
         */
        DELIMITED,
        /**
         * The model writes a free critique, a second model (usually a cheaper one) turns it into notes.
         */
        TWO_STAGE
    }

    /**
     * What the author asks for.
     *
     * @param instructions   what to look for ("flag continuity errors"), blank for the default task of the prompt
     * @param prompt         the annotate prompt (a template of {@code AnnotateTemplateData}) to use instead of the one of the book, blank for the
     *                       one of the book
     * @param options        the AI and the protocol to review with, the ones of the book when not set (see {@link #defaultOptions})
     * @param mode           how the notes are asked for, {@link Mode#AUTO} when null
     * @param extractAi      for {@link Mode#TWO_STAGE}: the AI that turns the critique into notes, the one that wrote it when null
     * @param extractProtocol the protocol of {@code extractAi}
     */
    record Request(String instructions, String prompt, GenerationOptions options, Mode mode, AI extractAi, Protocol extractProtocol) {

        public static Request of(String instructions) {
            return new Request(instructions, null, GenerationOptions.defaults(), Mode.AUTO, null, null);
        }
    }

    /**
     * A note the model proposes.
     *
     * @param quote the passage as the model quoted it
     * @param note  what the model says about it
     * @param type  a word for the kind of the note, null for none
     * @param start where the passage is in the text of the part; null when the quote was not found
     * @param end   the end of the passage (exclusive); null when the quote was not found
     */
    record Proposal(String quote, String note, String type, Integer start, Integer end) {

        public boolean located() {
            return start != null && end != null;
        }
    }

    /**
     * The AI and the protocol the user annotates with when they don't choose others for the run (their settings); the ones of the
     * book are used for what is not set.
     */
    GenerationOptions defaultOptions() throws Exception;

    /**
     * Starts the annotation of the part, the answer goes to the callback.
     *
     * @return the token that stops it, {@code null} when it cannot start (the book has no AI or protocol)
     * @throws ContextInsufficient when the prompt does not fit the context of the AI even without the story around the part
     * @throws IllegalArgumentException when the part is not a part of the book of the user or has no text
     */
    CancellationToken annotate(Manuscript manuscript, ChatMessage part, Request request, Callback callback) throws Exception;

    /**
     * Makes annotations of the proposals the author accepted. A proposal whose quote is not in the text any more (the part was
     * changed meanwhile) becomes a note about the whole part.
     *
     * @param label who wrote them: the name of the model
     */
    List<Annotation> accept(Manuscript manuscript, ChatMessage part, List<Proposal> proposals, String label) throws Exception;

    /**
     * What the annotation ends in; each call is made on the thread of the inference.
     */
    interface Callback {

        /**
         * Text and reasoning received so far (when the answer is a stream).
         */
        void onProgress(String reasoning, String text) throws Exception;

        /**
         * The model is done: the notes it proposed (none when it found nothing) and what it wrote.
         */
        void onFinished(List<Proposal> proposals, String raw) throws Exception;

        /**
         * The model answered, but not in a way that can be read, also not at the second try. The answer is given so that the work
         * is not lost.
         */
        void onUnreadable(String raw, String reason) throws Exception;

        /**
         * The author stopped it, {@code raw} is what arrived until then.
         */
        void onTerminated(String raw) throws Exception;

        void onError(Throwable throwable) throws Exception;

    }

    /**
     * The annotate prompt and the part need more tokens than the AI takes.
     */
    class ContextInsufficient extends Exception {
        private final long required;
        private final long max;

        public ContextInsufficient(long required, long max) {
            super("The annotation needs " + required + " tokens, the context has " + max);
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
