package com.github.enerccio.marginalia.domain.service;

/**
 * A change of a lorebook of a book an agent works on: the lorebooks of the book (the one it links and its subbooks) are
 * locked with the book, see {@link AgentRunService#checkNotLocked(com.github.enerccio.marginalia.domain.model.impl.Lorebook)}.
 * The screens show such a lorebook read only.
 */
public class LorebookLockedException extends ManuscriptLockedException {

    public LorebookLockedException(String agentName) {
        super("The lorebook is locked by the agent " + agentName + ", pause or stop its run to change the lorebook", agentName);
    }
}
