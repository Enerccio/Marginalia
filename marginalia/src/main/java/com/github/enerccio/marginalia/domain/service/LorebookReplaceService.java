package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Lorebook;
import com.github.enerccio.marginalia.domain.service.search.replace.TaskProgress;
import com.github.enerccio.marginalia.domain.service.search.replace.TextHit;

import java.util.List;

/**
 * Search and replace in the entries of a lorebook (their name, text and comment), in two steps like
 * {@link ManuscriptReplaceService}: {@link #plan} finds the matches and prepares the new texts without changing
 * anything, {@link #apply} does the changes in one transaction.
 */
public interface LorebookReplaceService {

    /**
     * @param caseSensitive a literal search has to match in the case of the letters (a regex is not affected)
     */
    record Options(String search, String replacement, boolean regex, boolean caseSensitive) {
    }

    /**
     * An entry with matches.
     *
     * @param id   id of the entry
     * @param name name of the entry as it is now
     * @param hits the matches in the order they are in the entry
     */
    record Item(Long id, String name, List<TextHit> hits) {
    }

    /**
     * What a replace is going to do, it is applied with {@link #apply}.
     */
    interface Plan {

        /**
         * @return entries with matches, in the order of the entries
         */
        List<Item> items();

        int hitCount();

    }

    /**
     * @param changed number of entries that were changed
     * @param hits    number of matches that were replaced
     * @param skipped number of entries that were not changed, because their text was not the one the plan was made of
     *                anymore
     */
    record Result(int changed, int hits, int skipped) {
    }

    /**
     * Finds the matches and prepares the replace, nothing is changed.
     *
     * @throws com.github.enerccio.marginalia.domain.service.search.replace.TextSearch.InvalidSearchException when the
     *                                                                                                        regular expression or its replacement is not valid
     * @throws java.util.concurrent.CancellationException                                                     when the progress asks to stop
     */
    Plan plan(Lorebook lorebook, Options options, TaskProgress progress) throws Exception;

    /**
     * Does what the plan says, in one transaction.
     */
    Result apply(Lorebook lorebook, Plan plan, TaskProgress progress) throws Exception;

}
