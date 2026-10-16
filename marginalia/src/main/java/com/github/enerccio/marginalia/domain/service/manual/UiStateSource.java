package com.github.enerccio.marginalia.domain.service.manual;

import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;

import java.util.List;

/**
 * Where the help assistant's model reads the state of the user's screen, see {@link HelpTools#getUiState}. The user
 * interface implements it (the page the question comes from); it hands the work to the thread of the page and answers
 * through the continuation, from any thread.
 */
public interface UiStateSource {

    /**
     * @param path             logical path of the element to read, empty for the whole page
     * @param deep             whether to include what is not on the screen (tabs that are not open, hidden parts)
     * @param withInteractions whether to list what the elements offer to do, see {@link #interact}
     * @param done             answered with the state as Markdown, or failed with a message the model can act on
     *                         (unknown path)
     */
    void read(String path, boolean deep, boolean withInteractions, FunctionContinuation<String> done);

    /**
     * Applies interactions (moves through the interface, like opening a tab) to an element, one after the other, see
     * {@code LLMInteract.applyInteraction}. Only called when an administrator allowed the assistant to interact.
     *
     * @param path         logical path of the element, as {@link #read} listed it
     * @param interactions the keys of the interactions, in the order to apply them
     * @param done         answered with the state of the page after them as Markdown (with the interactions that are
     *                     offered now), or failed with a message the model can act on (no such element or interaction)
     */
    void interact(String path, List<String> interactions, FunctionContinuation<String> done);

    /**
     * What went wrong for the user lately (see {@link HelpTools#readLastErrors}), newest first: the problems the
     * session wrote down and the unexpected failures the browser tab kept.
     *
     * @param count how many to read, anything below one for the default; more than ten are cut
     * @param done  answered with the problems as Markdown
     */
    void readProblems(int count, FunctionContinuation<String> done);
}
