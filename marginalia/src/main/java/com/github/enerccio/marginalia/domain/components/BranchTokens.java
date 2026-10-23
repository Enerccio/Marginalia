package com.github.enerccio.marginalia.domain.components;

/**
 * The tokens the story of a branch would take in the next prompt, counted from the parts and the summaries that are
 * stored (no model is asked), to see before a generation whether the story fits the context and what the summaries
 * save.
 * <p>
 * The newest valid summary block ends the summarized part of the branch: everything up to its message is replaced by the
 * summaries, the parts after it are put into the prompt as they are.
 *
 * @param parts            parts of the branch that have a text
 * @param partTokens       tokens of the texts of all those parts, the story without summaries
 * @param summaries        valid summaries (blocks) of the branch
 * @param summaryTokens    tokens of the texts of those summaries
 * @param summarizedParts  parts the summaries stand in for
 * @param summarizedTokens tokens of those parts
 * @param staleSummaries   summaries that no longer describe the story, they are dropped when a part is generated
 */
public record BranchTokens(int parts, long partTokens, int summaries, long summaryTokens, int summarizedParts,
                           long summarizedTokens, int staleSummaries) {

    /**
     * Parts after the newest summary.
     */
    public int unsummarizedParts() {
        return parts - summarizedParts;
    }

    /**
     * Tokens of the parts after the newest summary, which go into the prompt as they are.
     */
    public long unsummarizedTokens() {
        return partTokens - summarizedTokens;
    }

    /**
     * The story as the prompt gets it: the summaries and the parts they do not cover.
     */
    public long withSummariesTokens() {
        return summaryTokens + unsummarizedTokens();
    }
}
