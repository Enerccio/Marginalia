package com.github.enerccio.marginalia.extensions.anthropic;

import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.ThinkingBlockParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.enerccio.marginalia.domain.service.InferenceException;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.github.enerccio.marginalia.extensions.anthropic.AnthropicMessages.Prepared;
import com.github.enerccio.marginalia.extensions.anthropic.AnthropicMessages.Turn;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The payload of the application as the messages of a Claude request.
 */
class AnthropicMessagesTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static LLMChatMessage message(LLMRole role, String text) {
        return LLMChatMessage.of(role, text);
    }

    private static String text(ContentBlockParam block) {
        return block.asText().text();
    }

    private static List<String> texts(Turn turn) {
        return turn.content().stream().map(AnthropicMessagesTest::text).toList();
    }

    @Test
    void leadingSystemMessagesAreTheSystemPrompt() {
        Prepared prepared = AnthropicMessages.prepare(List.of(
                message(LLMRole.SYSTEM, "You write."), message(LLMRole.SYSTEM, "Be brief."), message(LLMRole.USER, "hi")));

        assertEquals("You write.\n\nBe brief.", prepared.system());
        assertEquals(1, prepared.turns().size());
        assertFalse(prepared.turns().get(0).assistant);
    }

    @Test
    void systemMessageInTheMiddleIsSentAsUserMessage() {
        Prepared prepared = AnthropicMessages.prepare(List.of(
                message(LLMRole.USER, "one"), message(LLMRole.ASSISTANT, "two"), message(LLMRole.SYSTEM, "note"),
                message(LLMRole.USER, "three")));

        assertNull(prepared.system());
        assertEquals(3, prepared.turns().size());
        assertTrue(prepared.turns().get(1).assistant);
        // the note and the message after it are one turn
        assertEquals(List.of("note", "three"), texts(prepared.turns().get(2)));
    }

    @Test
    void messagesOfTheSameRoleAreOneTurn() {
        Prepared prepared = AnthropicMessages.prepare(List.of(
                message(LLMRole.USER, "a"), message(LLMRole.USER, "b"), message(LLMRole.ASSISTANT, "c"), message(LLMRole.USER, "d")));

        assertEquals(3, prepared.turns().size());
        assertEquals(List.of("a", "b"), texts(prepared.turns().get(0)));
    }

    @Test
    void onlyInstructionsAreSentAsTheUserMessage() {
        Prepared prepared = AnthropicMessages.prepare(List.of(message(LLMRole.SYSTEM, "Just this.")));

        assertNull(prepared.system());
        assertEquals(List.of("Just this."), texts(prepared.turns().get(0)));
    }

    @Test
    void blankMessagesAreSkipped() {
        Prepared prepared = AnthropicMessages.prepare(List.of(
                message(LLMRole.SYSTEM, " "), message(LLMRole.USER, "hi"), message(LLMRole.ASSISTANT, "")));

        assertNull(prepared.system());
        assertEquals(1, prepared.turns().size());
    }

    @Test
    void payloadThatStartsOrEndsWithTheAssistantIsRefused() {
        InferenceException atStart = assertThrows(InferenceException.class, () -> AnthropicMessages.prepare(List.of(
                message(LLMRole.SYSTEM, "rules"), message(LLMRole.ASSISTANT, "The story so far."), message(LLMRole.USER, "More."))));
        InferenceException atEnd = assertThrows(InferenceException.class, () -> AnthropicMessages.prepare(List.of(
                message(LLMRole.USER, "Begin."), message(LLMRole.ASSISTANT, "The story so far."))));

        assertEquals(InferenceException.Type.BAD_REQUEST, atStart.getType());
        assertEquals(InferenceException.Type.BAD_REQUEST, atEnd.getType());
        assertTrue(atEnd.getProviderMessage().contains("start and end with a message of the user"));
    }

    @Test
    void attachmentsFollowTheText() {
        LLMChatMessage user = message(LLMRole.USER, "look").addAttachment(LLMAttachment.of("image/png", new byte[]{1, 2, 3}));

        List<ContentBlockParam> blocks = AnthropicMessages.prepare(List.of(user)).turns().get(0).content();

        assertEquals(2, blocks.size());
        assertTrue(blocks.get(0).isText());
        assertTrue(blocks.get(1).isImage());
    }

    @Test
    void pdfAndTextFilesAreDocumentsOtherTypesAreRefused() {
        List<ContentBlockParam> blocks = AnthropicMessages.prepare(List.of(message(LLMRole.USER, "read")
                .addAttachment(LLMAttachment.of("application/pdf", new byte[]{1}))
                .addAttachment(LLMAttachment.of("text/plain", "notes".getBytes())))).turns().get(0).content();

        assertTrue(blocks.get(1).isDocument());
        assertTrue(blocks.get(2).isDocument());
        assertThrows(IllegalArgumentException.class, () -> AnthropicMessages.prepare(List.of(message(LLMRole.USER, "listen")
                .addAttachment(LLMAttachment.of("audio/mpeg", new byte[]{1})))));
    }

    @Test
    void callsAndTheirResultsAreOneTurnEachAndTheResultsComeFirst() {
        LLMChatMessage assistant = LLMChatMessage.ofToolCalls("Let me look.", List.of(
                LLMToolCall.of("toolu_1", "read_file", "{\"path\":\"/a.md\"}"),
                LLMToolCall.of("toolu_2", "list_files", "{}")));
        LLMChatMessage first = LLMChatMessage.ofToolResult("toolu_1", "contents");
        LLMChatMessage second = LLMChatMessage.ofToolResult("toolu_2", "Error: nothing there");

        Prepared prepared = AnthropicMessages.prepare(List.of(message(LLMRole.USER, "go"), assistant, first, second,
                message(LLMRole.SYSTEM, "reminder")));

        assertEquals(3, prepared.turns().size());
        List<ContentBlockParam> call = prepared.turns().get(1).content();
        assertTrue(call.get(0).isText());
        assertEquals("toolu_1", call.get(1).asToolUse().id());
        assertEquals("toolu_2", call.get(2).asToolUse().id());
        List<ContentBlockParam> results = prepared.turns().get(2).content();
        assertEquals(3, results.size());
        assertTrue(results.get(0).isToolResult());
        assertEquals("toolu_1", results.get(0).asToolResult().toolUseId());
        assertTrue(results.get(1).asToolResult().isError().orElse(false), "a failed call is an error result");
        assertTrue(results.get(2).isText(), "what the user said follows the results");
    }

    @Test
    void argumentsOfACallBecomeItsInput() throws Exception {
        var properties = AnthropicMessages.input("{\"path\":\"/a.md\",\"depth\":2,\"tags\":[\"x\"]}")._additionalProperties();

        JsonNode back = MAPPER.valueToTree(properties.entrySet().stream().collect(java.util.stream.Collectors.toMap(
                java.util.Map.Entry::getKey, entry -> entry.getValue().convert(JsonNode.class))));
        assertEquals("/a.md", back.path("path").asText());
        assertEquals(2, back.path("depth").asInt());
        assertEquals("x", back.path("tags").get(0).asText());
    }

    @Test
    void thinkingTravelsInTheSignatureOfTheFirstCall() {
        String signature = AnthropicMessages.encodeThinking(List.of(
                com.anthropic.models.messages.ContentBlock.ofThinking(com.anthropic.models.messages.ThinkingBlock.builder()
                        .thinking("Let me think.").signature("SIG").build()),
                com.anthropic.models.messages.ContentBlock.ofRedactedThinking(com.anthropic.models.messages.RedactedThinkingBlock.builder()
                        .data("opaque").build())));
        assertNotNull(signature);

        LLMChatMessage assistant = LLMChatMessage.ofToolCalls("Checking.", List.of(LLMToolCall.of("toolu_1", "read_file", "{}", signature)));
        List<ContentBlockParam> blocks = AnthropicMessages.prepare(List.of(message(LLMRole.USER, "go"), assistant,
                        LLMChatMessage.ofToolResult("toolu_1", "done"))).turns().get(1).content();

        ThinkingBlockParam thinking = blocks.get(0).asThinking();
        assertEquals("Let me think.", thinking.thinking());
        assertEquals("SIG", thinking.signature());
        assertEquals("opaque", blocks.get(1).asRedactedThinking().data());
        assertTrue(blocks.get(2).isText(), "the thinking is in front of the text");
        assertTrue(blocks.get(3).isToolUse());
    }

    @Test
    void signaturesOfOtherProvidersAreNotThinking() {
        assertTrue(AnthropicMessages.decodeThinking(null).isEmpty());
        assertTrue(AnthropicMessages.decodeThinking("c2tpcF90aG91Z2h0X3NpZ25hdHVyZV92YWxpZGF0b3I=").isEmpty());
        assertTrue(AnthropicMessages.decodeThinking("anthropic-thinking:1:not base64!").isEmpty());
        assertNull(AnthropicMessages.encodeThinking(List.of()));
    }
}
