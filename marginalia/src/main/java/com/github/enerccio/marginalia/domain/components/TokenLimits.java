package com.github.enerccio.marginalia.domain.components;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Protocol;

/**
 * Token limits of a generation. The protocol limits are optional overrides - when not set (null or not positive),
 * the limits of the AI are used. They can lower the limits of the AI but never raise them: the AI is what the
 * provider (or the administrator who shares it) allows.
 */
public final class TokenLimits {

    private TokenLimits() {
    }

    /**
     * Total context size: protocol's max tokens, otherwise AI's max context; the smaller when both are set.
     */
    public static int contextTokens(AI ai, Protocol protocol) {
        return limit(protocol == null ? null : protocol.getMaxTokens(), ai == null ? null : ai.getMaxContext());
    }

    /**
     * Maximum response size: protocol's reply tokens, otherwise AI's max completion tokens; the smaller when both
     * are set. 0 means not limited.
     */
    public static int responseTokens(AI ai, Protocol protocol) {
        return limit(protocol == null ? null : protocol.getReplyTokens(), ai == null ? null : ai.getMaxCompletionTokens());
    }

    /**
     * Tokens available for the prompt - context size minus the room reserved for the response.
     */
    public static int promptTokens(AI ai, Protocol protocol) {
        return Math.max(0, contextTokens(ai, protocol) - responseTokens(ai, protocol));
    }

    private static int limit(Integer protocolLimit, Integer aiLimit) {
        if (isSet(protocolLimit) && isSet(aiLimit)) {
            return Math.min(protocolLimit, aiLimit);
        }
        if (isSet(protocolLimit)) {
            return protocolLimit;
        }
        return isSet(aiLimit) ? aiLimit : 0;
    }

    private static boolean isSet(Integer value) {
        return value != null && value > 0;
    }
}
