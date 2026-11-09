package com.github.enerccio.marginalia.extensions.translatorcommon.service;

import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Where a block goes in the messages sent to the model.
 */
class PayloadBlockTest {

    private static List<LLMChatMessage> payload() {
        return List.of(LLMChatMessage.of(LLMRole.SYSTEM, "system"), LLMChatMessage.of(LLMRole.USER, "story"), LLMChatMessage.of(LLMRole.USER, "turn"));
    }

    private static String texts(List<LLMChatMessage> messages) {
        return messages.stream().map(m -> m.getContent().toString()).reduce("", (a, b) -> a.isEmpty() ? b : a + "|" + b);
    }

    @Test
    void depthCountsFromTheEnd() {
        assertEquals("system|story|turn|block", texts(PayloadBlock.insert(payload(), "block", 0, LLMRole.SYSTEM)));
        assertEquals("system|story|block|turn", texts(PayloadBlock.insert(payload(), "block", 1, LLMRole.SYSTEM)));
    }

    @Test
    void neverBeforeTheSystemPromptAndNothingToInsertChangesNothing() {
        assertEquals("system|block|story|turn", texts(PayloadBlock.insert(payload(), "block", 99, LLMRole.SYSTEM)));
        List<LLMChatMessage> same = payload();
        assertSame(same, PayloadBlock.insert(same, " ", 1, LLMRole.SYSTEM));
        assertSame(same, PayloadBlock.insert(same, null, 1, LLMRole.SYSTEM));
    }
}
