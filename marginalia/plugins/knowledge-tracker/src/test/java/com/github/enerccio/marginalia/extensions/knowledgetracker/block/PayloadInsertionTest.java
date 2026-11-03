package com.github.enerccio.marginalia.extensions.knowledgetracker.block;

import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Where the block lands among the messages of the prompt.
 */
class PayloadInsertionTest {

    private static List<LLMChatMessage> payload() {
        return List.of(
                LLMChatMessage.of(LLMRole.SYSTEM, "system"),
                LLMChatMessage.of(LLMRole.USER, "story"),
                LLMChatMessage.of(LLMRole.USER, "instructions"));
    }

    private static List<String> contents(List<LLMChatMessage> messages) {
        return messages.stream().map(LLMChatMessage::getContent).toList();
    }

    @Test
    void depthZeroIsTheEnd() {
        assertEquals(List.of("system", "story", "instructions", "block"),
                contents(PayloadInsertion.insert(payload(), " block ", 0, LLMRole.SYSTEM)));
    }

    @Test
    void depthOneIsRightBeforeTheInstructions() {
        List<LLMChatMessage> result = PayloadInsertion.insert(payload(), "block", 1, LLMRole.ASSISTANT);
        assertEquals(List.of("system", "story", "block", "instructions"), contents(result));
        assertEquals(LLMRole.ASSISTANT, result.get(2).getRole());
    }

    @Test
    void neverBeforeTheLeadingSystemPrompt() {
        assertEquals(List.of("system", "block", "story", "instructions"),
                contents(PayloadInsertion.insert(payload(), "block", 99, LLMRole.SYSTEM)));
    }

    @Test
    void withoutASystemPromptItMayBeFirst() {
        List<LLMChatMessage> noSystem = List.of(LLMChatMessage.of(LLMRole.USER, "story"));
        assertEquals(List.of("block", "story"), contents(PayloadInsertion.insert(noSystem, "block", 5, LLMRole.USER)));
    }

    @Test
    void nothingToInsertLeavesThePayloadAlone() {
        List<LLMChatMessage> payload = payload();
        assertSame(payload, PayloadInsertion.insert(payload, "", 1, LLMRole.SYSTEM));
        assertSame(payload, PayloadInsertion.insert(payload, "  ", 1, LLMRole.SYSTEM));
        assertSame(payload, PayloadInsertion.insert(payload, null, 1, LLMRole.SYSTEM));
        assertNull(PayloadInsertion.insert(null, "block", 1, LLMRole.SYSTEM));
    }

    @Test
    void theOriginalListIsNotChanged() {
        List<LLMChatMessage> payload = payload();
        PayloadInsertion.insert(payload, "block", 1, LLMRole.SYSTEM);
        assertEquals(3, payload.size());
    }
}
