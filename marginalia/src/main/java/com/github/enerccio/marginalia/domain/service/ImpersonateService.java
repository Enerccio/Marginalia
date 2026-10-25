package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.impl.generation.GenerationOptions;

/**
 * Lets the model write the instructions of the next turn in the place of the author (impersonate): the plot specification the
 * author types into the "instructions" box before the model writes the next part. The model gets the story so far (the older story
 * as summaries, the way a generation sees it), the turn details of the next turn, the examples of how the author writes
 * instructions (the instructions of the last parts), what the author wants the instructions to be about, and the plan of the
 * story the caller has (an extension, a command), in the impersonate prompt of the book ({@link ManuscriptService#getImpersonatePrompt}).
 * <p>
 * Nothing is stored by it: the text goes to the {@link Callback} and the caller decides what happens with it (the new turn form of
 * the story editor puts it into the instructions).
 */
public interface ImpersonateService {

    /**
     * Starts drafting the instructions, the answer of the model goes to the callback.
     *
     * @return the token that stops it, {@code null} when it cannot start (the book has no AI or protocol)
     * @throws ContextInsufficient when the prompt does not fit the context of the AI even without the story
     * @throws IllegalArgumentException when the book is not a book of the user
     */
    CancellationToken impersonate(Manuscript manuscript, Request request, Callback callback) throws Exception;

    /**
     * What to draft the instructions from.
     *
     * @param instructions      what the author wants the instructions to be about (a rough idea, a direction), blank for none: the model
     *                          then continues the story where it leads
     * @param outline           the plan of the story for the model (the data of the chapter the story is at, for example), blank for none
     * @param sceneSetting      the location and time of the next turn, {@code null} for the one of the last part of the branch
     * @param povCharacter      the point of view character of the next turn, {@code null} for the one of the last part
     * @param presentCharacters the characters present in the next turn, {@code null} for the ones of the last part
     * @param prompt            the impersonate prompt (a template of {@code ImpersonateTemplateData}) to use instead of the one of
     *                          the book, blank for the one of the book
     * @param options           the AI and the protocol to write with when they are set (see {@link GenerationOptions}), otherwise the
     *                          ones of the book; the book is not changed by them
     */
    record Request(String instructions, String outline, String sceneSetting, String povCharacter, String presentCharacters,
                   String prompt, GenerationOptions options) {

        public static Request of(String instructions) {
            return new Request(instructions, null, null, null, null, null, GenerationOptions.defaults());
        }

        public Request withOutline(String outline) {
            return new Request(instructions, outline, sceneSetting, povCharacter, presentCharacters, prompt, options);
        }

        public Request withTurn(String sceneSetting, String povCharacter, String presentCharacters) {
            return new Request(instructions, outline, sceneSetting, povCharacter, presentCharacters, prompt, options);
        }

        public Request withPrompt(String prompt) {
            return new Request(instructions, outline, sceneSetting, povCharacter, presentCharacters, prompt, options);
        }

        public Request withOptions(GenerationOptions options) {
            return new Request(instructions, outline, sceneSetting, povCharacter, presentCharacters, prompt, options);
        }
    }

    /**
     * What the drafting ends in; each call is made on the thread of the inference.
     */
    interface Callback {

        /**
         * Text and reasoning received so far.
         */
        void onProgress(String reasoning, String text) throws Exception;

        /**
         * The model is done, {@code text} is the instructions.
         */
        void onFinished(String reasoning, String text) throws Exception;

        /**
         * The caller stopped it, {@code text} is what arrived until then.
         */
        void onTerminated(String reasoning, String text) throws Exception;

        void onError(Throwable throwable) throws Exception;

    }

    /**
     * The impersonate prompt needs more tokens than the AI takes.
     */
    class ContextInsufficient extends Exception {
        private final long required;
        private final long max;

        public ContextInsufficient(long required, long max) {
            super("The impersonate prompt needs " + required + " tokens, the context has " + max);
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
