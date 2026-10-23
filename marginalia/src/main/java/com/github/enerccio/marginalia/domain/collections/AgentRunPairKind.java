package com.github.enerccio.marginalia.domain.collections;

/**
 * What an {@link com.github.enerccio.marginalia.domain.model.impl.AgentRunPair} stands for: the kind of the item of the
 * working copy of an agent run, and of the item of the original it was copied from.
 */
public enum AgentRunPairKind {
    MESSAGE,
    SUMMARY,
    LOREBOOK,
    ENTRY,
    ANNOTATION,
}
