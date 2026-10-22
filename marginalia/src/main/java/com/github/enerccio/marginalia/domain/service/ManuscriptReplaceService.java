package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.search.replace.TaskProgress;
import com.github.enerccio.marginalia.domain.service.search.replace.TextHit;

import java.util.List;

/**
 * Search and replace in the text of a book: the parts of the story and, if wanted, their summaries. It works in two
 * steps, so that the user sees what is going to change: {@link #plan} finds the matches and works out everything the
 * replace needs (the new texts, the new hashes of the summaries) without changing anything, {@link #apply} then does
 * the changes in one transaction.
 * <p>
 * The hash of a summary block ({@link SummaryService.SummaryBlock}) is made of the text of the story it describes, so
 * a replace in the story would make the summaries look outdated. The summaries the replace looks into get their hash
 * recomputed, so they stay valid. The summaries it does not look into (those of the branches it does not search, or
 * all of them when it does not search summaries) can't be brought up to date, they become outdated, and the plan counts
 * them ({@link Plan#outdatedSummaries()}) so the user can be warned. A summary that was outdated before stays so.
 * <p>
 * With {@link Options#revalidateSummaries()} the hash of every summary of the book is worked out again instead, those
 * outside of the search and those that were outdated included: all the summaries are valid after the replace, so
 * nothing becomes outdated.
 */
public interface ManuscriptReplaceService {

    /**
     * @param caseSensitive a literal search has to match in the case of the letters (a regex is not affected)
     * @param summaries   look into the summaries too
     * @param allBranches look into all the branches, otherwise only into the active one
     * @param revalidateSummaries work out the hash of every summary of the book again, whether the replace touched it
     *                            or not and whether it was valid before, so all of them are valid after it
     */
    record Options(String search, String replacement, boolean regex, boolean caseSensitive, boolean summaries,
                   boolean allBranches, boolean revalidateSummaries) {
    }

    enum Kind {
        /**
         * Part of the story.
         */
        MESSAGE,
        SUMMARY
    }

    /**
     * A part of the story or a summary with matches.
     *
     * @param id   id of the part or of the summary
     * @param hits the matches in the order they are in the text
     */
    record Item(Kind kind, Long id, List<TextHit> hits) {
    }

    /**
     * What a replace is going to do, it is applied with {@link #apply}.
     */
    interface Plan {

        /**
         * @return items with matches, in the order of the story (a summary right behind its part)
         */
        List<Item> items();

        int hitCount();

        /**
         * @return number of summaries that were valid and become outdated by the replace, as it does not look into them
         */
        int outdatedSummaries();

    }

    /**
     * @param changed number of parts and summaries that were changed
     * @param hits    number of matches that were replaced
     * @param skipped number of parts and summaries that were not changed, because their text was not the one the plan
     *                was made of anymore
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
    Plan plan(Manuscript manuscript, Options options, TaskProgress progress) throws Exception;

    /**
     * Does what the plan says, in one transaction.
     */
    Result apply(Manuscript manuscript, Plan plan, TaskProgress progress) throws Exception;

}
