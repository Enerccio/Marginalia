package com.github.enerccio.marginalia.domain.service;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;

/**
 * Token limits of a generation. The protocol limits are optional overrides - when not set (null or not positive),
 * the limits of the AI are used.
 */
public final class TokenLimits {

    private TokenLimits() {
    }

    /**
     * Total context size: protocol's max tokens, otherwise AI's max context.
     */
    public static int contextTokens(AI ai, Protocol protocol) {
        if (protocol != null && isSet(protocol.getMaxTokens())) {
            return protocol.getMaxTokens();
        }
        return ai != null && isSet(ai.getMaxContext()) ? ai.getMaxContext() : 0;
    }

    /**
     * Maximum response size: protocol's reply tokens, otherwise AI's max completion tokens. 0 means not limited.
     */
    public static int responseTokens(AI ai, Protocol protocol) {
        if (protocol != null && isSet(protocol.getReplyTokens())) {
            return protocol.getReplyTokens();
        }
        return ai != null && isSet(ai.getMaxCompletionTokens()) ? ai.getMaxCompletionTokens() : 0;
    }

    /**
     * Tokens available for the prompt - context size minus the room reserved for the response.
     */
    public static int promptTokens(AI ai, Protocol protocol) {
        return Math.max(0, contextTokens(ai, protocol) - responseTokens(ai, protocol));
    }

    private static boolean isSet(Integer value) {
        return value != null && value > 0;
    }
}
