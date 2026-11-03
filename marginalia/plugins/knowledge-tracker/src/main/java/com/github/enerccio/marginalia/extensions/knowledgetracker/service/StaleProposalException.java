package com.github.enerccio.marginalia.extensions.knowledgetracker.service;

/**
 * The story changed (another branch, a part deleted, the cursor moved) while the changes of a run were being reviewed, so
 * they no longer belong to the parts that are next in line.
 */
public class StaleProposalException extends Exception {

    public StaleProposalException(String message) {
        super(message);
    }
}
