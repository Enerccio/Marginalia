package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.collections.AgentRunPairKind;

/**
 * One item of a working copy and the item of the original it was copied from, as the copy reports them when it is made
 * (stored as an {@link com.github.enerccio.marginalia.domain.model.impl.AgentRunPair}).
 *
 * @param copyUuid     the uuid of the item in the copy
 * @param originalUuid the uuid of the item in the original; null for an item that has no original (made by the agent)
 * @param handle       the handle the agent knows the item by; null when it was not given one (a copy has none when it is made)
 */
public record UuidPair(AgentRunPairKind kind, String copyUuid, String originalUuid, String handle) {

    public UuidPair(AgentRunPairKind kind, String copyUuid, String originalUuid) {
        this(kind, copyUuid, originalUuid, null);
    }
}
