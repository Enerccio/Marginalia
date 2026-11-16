package com.github.enerccio.marginalia.extensions.authorsnote.service;

import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.extensions.authorsnote.model.AuthorsNoteData;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Where the author's note goes into the prompt, and how it is read from the book.
 */
class AuthorsNoteServiceTest {

    private final AuthorsNoteService service = new AuthorsNoteService();

    private static List<LLMChatMessage> payload(LLMRole... roles) {
        LLMChatMessage[] messages = new LLMChatMessage[roles.length];
        for (int i = 0; i < roles.length; i++) {
            messages[i] = LLMChatMessage.of(roles[i], "m" + i);
        }
        return Arrays.asList(messages);
    }

    private static List<String> contents(List<LLMChatMessage> messages) {
        return messages.stream().map(LLMChatMessage::getContent).toList();
    }

    private static AuthorsNoteData note(boolean enabled, String text, int depth, LLMRole role) {
        AuthorsNoteData data = new AuthorsNoteData();
        data.setEnabled(enabled);
        data.setNote(text);
        data.setDepth(depth);
        data.setRole(role);
        return data;
    }

    @Test
    void theNoteGoesDepthMessagesBeforeTheEnd() {
        List<LLMChatMessage> payload = payload(LLMRole.SYSTEM, LLMRole.USER, LLMRole.ASSISTANT, LLMRole.USER);
        assertEquals(List.of("m0", "m1", "m2", "m3", "note"), contents(service.insertNote(payload, note(true, " note\n", 0, LLMRole.SYSTEM))));
        List<LLMChatMessage> beforeLast = service.insertNote(payload, note(true, "note", 1, LLMRole.ASSISTANT));
        assertEquals(List.of("m0", "m1", "m2", "note", "m3"), contents(beforeLast));
        assertEquals(LLMRole.ASSISTANT, beforeLast.get(3).getRole());
        assertEquals(List.of("m0", "m1", "m2", "m3", "note"), contents(service.insertNote(payload, note(true, "note", -2, LLMRole.SYSTEM))),
                "a negative depth is the end");
        assertEquals(4, payload.size(), "the payload itself is not changed");
    }

    @Test
    void theNoteNeverGoesBeforeTheSystemPrompt() {
        assertEquals(List.of("m0", "note", "m1"),
                contents(service.insertNote(payload(LLMRole.SYSTEM, LLMRole.USER), note(true, "note", 10, LLMRole.SYSTEM))));
        assertEquals(List.of("note", "m0", "m1"),
                contents(service.insertNote(payload(LLMRole.USER, LLMRole.ASSISTANT), note(true, "note", 10, LLMRole.SYSTEM))));
    }

    @Test
    void aDisabledOrEmptyNoteIsNotInserted() {
        List<LLMChatMessage> payload = payload(LLMRole.SYSTEM, LLMRole.USER);
        assertSame(payload, service.insertNote(payload, note(false, "note", 0, LLMRole.SYSTEM)));
        assertSame(payload, service.insertNote(payload, note(true, "  \n", 0, LLMRole.SYSTEM)));
        assertNull(service.insertNote(null, note(true, "note", 0, LLMRole.SYSTEM)));
    }

    @Test
    void aDisabledNoteTakesNoTokens() throws Exception {
        // no model is asked for a note that is not sent
        assertEquals(0, service.countTokens(null, note(false, "note", 1, LLMRole.SYSTEM)));
        assertEquals(0, service.countTokens(null, note(true, "", 1, LLMRole.SYSTEM)));
        assertEquals(0, service.estimateTokens(new Manuscript(), " "));
    }

    @Test
    void theNoteIsReadFromTheBookWithDefaultsForWhatIsMissing() {
        Manuscript manuscript = new Manuscript();
        JsonObject attributes = new JsonObject();
        attributes.add(AuthorsNoteData.KEY, JsonParser.parseString("{\"enabled\": true, \"note\": \"Rain all day.\", \"depth\": 3}"));
        manuscript.setAttributes(attributes);
        AuthorsNoteData data = service.getAuthorsNote(manuscript);
        assertTrue(data.isEnabled());
        assertEquals("Rain all day.", data.getNote());
        assertEquals(3, data.getDepth());
        assertEquals("", data.getPrivateNote());
        assertEquals(LLMRole.SYSTEM, data.getRole());

        AuthorsNoteData none = service.getAuthorsNote(new Manuscript());
        assertFalse(none.isEnabled());
        assertEquals(1, none.getDepth());
        assertEquals(LLMRole.SYSTEM, none.getRole());
    }

    @Test
    void aStoredNullRoleReadsAsSystem() {
        Manuscript manuscript = new Manuscript();
        JsonObject attributes = new JsonObject();
        attributes.add(AuthorsNoteData.KEY, JsonParser.parseString("{\"note\": \"x\", \"role\": null}"));
        manuscript.setAttributes(attributes);
        assertEquals(LLMRole.SYSTEM, service.getAuthorsNote(manuscript).getRole());
    }
}
