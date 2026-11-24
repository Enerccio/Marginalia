package com.github.enerccio.marginalia.extensions.grok;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** The payload as the messages of a chat completion. */
class GrokMessagesTest {

    @Test
    void rolesAndTextArePassedOn() {
        JsonNode messages = GrokMessages.messages(List.of(
                LLMChatMessage.of(LLMRole.SYSTEM, "Rules."),
                LLMChatMessage.of(LLMRole.USER, "Begin."),
                LLMChatMessage.of(LLMRole.ASSISTANT, "Once."),
                LLMChatMessage.of(LLMRole.USER, "Go on.")));

        assertEquals(4, messages.size());
        assertEquals("system", messages.get(0).path("role").asText());
        assertEquals("Rules.", messages.get(0).path("content").asText());
        assertEquals("assistant", messages.get(2).path("role").asText());
    }

    @Test
    void messagesOfNothingAreLeftOut() {
        LLMChatMessage empty = LLMChatMessage.of(LLMRole.USER, null);

        JsonNode messages = GrokMessages.messages(List.of(empty, LLMChatMessage.of(LLMRole.SYSTEM, "  "), LLMChatMessage.of(LLMRole.USER, "x")));

        assertEquals(1, messages.size());
    }

    @Test
    void callsAndTheirResultsGoBackInOrder() {
        LLMChatMessage said = LLMChatMessage.ofToolCalls("Looking.", List.of(LLMToolCall.of("c1", "lookup", "{\"name\":\"Mira\"}")));
        LLMChatMessage result = LLMChatMessage.of(LLMRole.TOOL, "A cook.");
        result.setToolCallId("c1");

        JsonNode messages = GrokMessages.messages(List.of(LLMChatMessage.of(LLMRole.USER, "Who?"), said, result));

        JsonNode call = messages.get(1).path("tool_calls").get(0);
        assertEquals("c1", call.path("id").asText());
        assertEquals("function", call.path("type").asText());
        assertEquals("lookup", call.path("function").path("name").asText());
        assertEquals("{\"name\":\"Mira\"}", call.path("function").path("arguments").asText());
        assertEquals("tool", messages.get(2).path("role").asText());
        assertEquals("c1", messages.get(2).path("tool_call_id").asText());
    }

    @Test
    void picturesAreSentAsDataUrls() {
        LLMChatMessage user = LLMChatMessage.of(LLMRole.USER, "Look.");
        user.addAttachment(LLMAttachment.of("image/png", new byte[]{1, 2, 3}));

        JsonNode content = GrokMessages.messages(List.of(user)).get(0).path("content");

        assertEquals("text", content.get(0).path("type").asText());
        assertEquals("image_url", content.get(1).path("type").asText());
        assertTrue(content.get(1).path("image_url").path("url").asText().startsWith("data:image/png;base64,"));
    }

    @Test
    void picturesGoBeforeTheTextWhenTheMessageSaysSo() {
        LLMChatMessage user = LLMChatMessage.of(LLMRole.USER, "Look.");
        user.addAttachment(LLMAttachment.of("image/png", new byte[]{1, 2, 3}));
        user.setImagesFirst(true);

        JsonNode content = GrokMessages.messages(List.of(user)).get(0).path("content");

        assertEquals("image_url", content.get(0).path("type").asText());
        assertEquals("text", content.get(1).path("type").asText());
    }

    @Test
    void whatGrokCannotReadIsRefused() {
        LLMChatMessage user = LLMChatMessage.of(LLMRole.USER, "Read.");
        user.addAttachment(LLMAttachment.of("application/pdf", new byte[]{1}));

        assertThrows(IllegalArgumentException.class, () -> GrokMessages.messages(List.of(user)));
    }

    @Test
    void aCallThatHasNoIdGetsOne() {
        assertTrue(GrokMessages.callId(null).startsWith(GrokMessages.GENERATED_ID_PREFIX));
        assertEquals("abc", GrokMessages.callId("abc"));
    }
}
