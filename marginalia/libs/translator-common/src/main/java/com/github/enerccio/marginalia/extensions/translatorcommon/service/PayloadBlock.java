package com.github.enerccio.marginalia.extensions.translatorcommon.service;

import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;

import java.util.ArrayList;
import java.util.List;

/**
 * Puts a block of text into the messages sent to the model, the way the Author's Note is put there.
 */
public final class PayloadBlock {

    private PayloadBlock() {
    }

    /**
     * Returns a copy of {@code payload} with {@code text} inserted {@code depth} messages before its end (0 = at the very end, 1 = right before the
     * instructions of the turn), or {@code payload} itself when there is nothing to insert. The text never goes before the leading system prompt.
     */
    public static List<LLMChatMessage> insert(List<LLMChatMessage> payload, String text, int depth, LLMRole role) {
        if (payload == null || text == null || text.isBlank()) {
            return payload;
        }
        int min = !payload.isEmpty() && payload.getFirst().getRole() == LLMRole.SYSTEM ? 1 : 0;
        int index = Math.clamp(payload.size() - Math.max(0, depth), min, payload.size());
        List<LLMChatMessage> result = new ArrayList<>(payload);
        result.add(index, LLMChatMessage.of(role, text.strip()));
        return result;
    }
}
