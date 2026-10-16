package com.github.enerccio.marginalia.extensions.googlegenai;

import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMAttachment;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The payload of the application as the contents of a Gemini request.
 */
class GoogleGenAiMessagesTest {

    private static LLMChatMessage message(LLMRole role, String text) {
        return LLMChatMessage.of(role, text);
    }

    private static List<Part> parts(Content content) {
        return content.parts().orElseThrow();
    }

    @Test
    void leadingSystemMessagesAreTheSystemInstruction() {
        GoogleGenAiMessages.Prepared prepared = GoogleGenAiMessages.prepare(List.of(
                message(LLMRole.SYSTEM, "You write."), message(LLMRole.SYSTEM, "Be brief."), message(LLMRole.USER, "hi")));

        assertEquals("You write.\n\nBe brief.", parts(prepared.systemInstruction()).get(0).text().orElseThrow());
        assertEquals(1, prepared.contents().size());
        assertEquals("user", prepared.contents().get(0).role().orElseThrow());
    }

    @Test
    void systemMessageInTheMiddleIsSentAsUserMessage() {
        GoogleGenAiMessages.Prepared prepared = GoogleGenAiMessages.prepare(List.of(
                message(LLMRole.USER, "one"), message(LLMRole.ASSISTANT, "two"), message(LLMRole.SYSTEM, "note"),
                message(LLMRole.USER, "three")));

        assertNull(prepared.systemInstruction());
        assertEquals(3, prepared.contents().size());
        assertEquals("model", prepared.contents().get(1).role().orElseThrow());
        // the note and the message after it are one turn
        assertEquals(List.of("note", "three"), parts(prepared.contents().get(2)).stream().map(p -> p.text().orElseThrow()).toList());
    }

    @Test
    void messagesOfTheSameRoleAreOneContent() {
        GoogleGenAiMessages.Prepared prepared = GoogleGenAiMessages.prepare(List.of(
                message(LLMRole.USER, "a"), message(LLMRole.USER, "b"), message(LLMRole.ASSISTANT, "c")));

        assertEquals(2, prepared.contents().size());
        assertEquals(2, parts(prepared.contents().get(0)).size());
    }

    @Test
    void onlyInstructionsAreSentAsTheUserMessage() {
        GoogleGenAiMessages.Prepared prepared = GoogleGenAiMessages.prepare(List.of(message(LLMRole.SYSTEM, "Just this.")));

        assertNull(prepared.systemInstruction());
        assertEquals("Just this.", parts(prepared.contents().get(0)).get(0).text().orElseThrow());
    }

    @Test
    void blankMessagesAreSkipped() {
        GoogleGenAiMessages.Prepared prepared = GoogleGenAiMessages.prepare(List.of(
                message(LLMRole.SYSTEM, " "), message(LLMRole.USER, "hi"), message(LLMRole.ASSISTANT, "")));

        assertNull(prepared.systemInstruction());
        assertEquals(1, prepared.contents().size());
    }

    @Test
    void attachmentsFollowTheText() {
        LLMChatMessage user = message(LLMRole.USER, "look").addAttachment(LLMAttachment.of("image/png", new byte[]{1, 2, 3}));

        Content content = GoogleGenAiMessages.prepare(List.of(user)).contents().get(0);

        assertEquals(2, parts(content).size());
        assertEquals("look", parts(content).get(0).text().orElseThrow());
        assertEquals("image/png", parts(content).get(1).inlineData().orElseThrow().mimeType().orElseThrow());
    }

    @Test
    void callsAndTheirResultsGoBackWithTheNamesOfTheFunctions() {
        LLMChatMessage calls = LLMChatMessage.ofToolCalls("checking", List.of(
                LLMToolCall.of("call_1", "weather", "{\"city\":\"Prague\"}"),
                LLMToolCall.of("call_2", "time", "{}")));

        GoogleGenAiMessages.Prepared prepared = GoogleGenAiMessages.prepare(List.of(
                message(LLMRole.USER, "weather?"), calls,
                LLMChatMessage.ofToolResult("call_1", "Sunny"), LLMChatMessage.ofToolResult("call_2", "Error: no clock")));

        assertEquals(3, prepared.contents().size());
        List<Part> model = parts(prepared.contents().get(1));
        assertEquals("checking", model.get(0).text().orElseThrow());
        assertEquals("Prague", model.get(1).functionCall().orElseThrow().args().orElseThrow().get("city"));
        // both results are one content
        List<Part> results = parts(prepared.contents().get(2));
        assertEquals(2, results.size());
        assertEquals("weather", results.get(0).functionResponse().orElseThrow().name().orElseThrow());
        assertEquals("Sunny", results.get(0).functionResponse().orElseThrow().response().orElseThrow().get("output"));
        assertEquals("time", results.get(1).functionResponse().orElseThrow().name().orElseThrow());
        assertEquals("Error: no clock", results.get(1).functionResponse().orElseThrow().response().orElseThrow().get("error"));
    }

    @Test
    void idsMadeUpHereAreNotSentToGoogle() {
        String generated = GoogleGenAiMessages.GENERATED_ID_PREFIX + "abc";
        LLMChatMessage calls = LLMChatMessage.ofToolCalls(null, List.of(
                LLMToolCall.of(generated, "weather", "{}"), LLMToolCall.of("from-google", "time", "{}")));

        GoogleGenAiMessages.Prepared prepared = GoogleGenAiMessages.prepare(List.of(
                message(LLMRole.USER, "go"), calls,
                LLMChatMessage.ofToolResult(generated, "a"), LLMChatMessage.ofToolResult("from-google", "b")));

        List<Part> model = parts(prepared.contents().get(1));
        assertFalse(model.get(0).functionCall().orElseThrow().id().isPresent());
        assertEquals("from-google", model.get(1).functionCall().orElseThrow().id().orElseThrow());
        List<Part> results = parts(prepared.contents().get(2));
        assertFalse(results.get(0).functionResponse().orElseThrow().id().isPresent());
        assertEquals("from-google", results.get(1).functionResponse().orElseThrow().id().orElseThrow());
    }

    @Test
    void callsGetAnIdWhenGoogleSendsNone() {
        String id = GoogleGenAiMessages.callId(com.google.genai.types.FunctionCall.builder().name("x").build());

        assertTrue(id.startsWith(GoogleGenAiMessages.GENERATED_ID_PREFIX));
        assertEquals("given", GoogleGenAiMessages.callId(com.google.genai.types.FunctionCall.builder().name("x").id("given").build()));
    }

    @Test
    void unparsableArgumentsAreAnEmptyObject() {
        LLMChatMessage calls = LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of("c", "weather", "not json")));

        Content model = GoogleGenAiMessages.prepare(List.of(message(LLMRole.USER, "go"), calls)).contents().get(1);

        assertTrue(parts(model).get(0).functionCall().orElseThrow().args().orElseThrow().isEmpty());
    }
}
