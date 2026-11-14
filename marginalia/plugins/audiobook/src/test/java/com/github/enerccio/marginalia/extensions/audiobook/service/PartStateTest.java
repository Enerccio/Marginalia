package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.extensions.audiobook.script.Hashes;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** What the Convert window shows of a part: not generated, labelled, generated (also with a text that changed), final. */
class PartStateTest {

    private static ChatMessage part(String text) {
        ChatMessage part = new ChatMessage();
        part.setUuid("u-" + text.length());
        part.setResponse(text);
        part.setAttributes(new JsonObject());
        return part;
    }

    private static JsonObject index(ChatMessage part) {
        JsonObject index = part.getAttributes().has(ResourceAudioStore.KEY) ? part.getAttributes().getAsJsonObject(ResourceAudioStore.KEY) : new JsonObject();
        part.getAttributes().add(ResourceAudioStore.KEY, index);
        return index;
    }

    private static void withAudio(ChatMessage part, String madeFrom) {
        JsonObject joined = new JsonObject();
        joined.addProperty("joinKey", "j");
        joined.addProperty("sourceHash", Hashes.sha256(madeFrom));
        joined.addProperty("resource", "res");
        index(part).add("part", joined);
    }

    private static void withLabels(ChatMessage part) {
        JsonObject labels = new JsonObject();
        labels.addProperty("key", "k");
        labels.add("labels", new com.google.gson.JsonArray());
        index(part).add("labels", labels);
    }

    @Test
    void aPartWithNothingIsNotGenerated() {
        AudiobookService.PartState state = AudiobookService.stateOf(3, part("Text."), false);

        assertEquals(3, state.number());
        assertEquals(AudiobookService.StoredAudio.NONE, state.audio());
        assertFalse(state.labelled());
        assertFalse(state.finalStatus());
        assertEquals("Part 3: Text.", state.title());
    }

    @Test
    void labelsAreSeenWithAndWithoutAudio() {
        ChatMessage labelled = part("Text.");
        withLabels(labelled);
        assertTrue(AudiobookService.stateOf(1, labelled, false).labelled());
        assertEquals(AudiobookService.StoredAudio.NONE, AudiobookService.stateOf(1, labelled, false).audio());

        withAudio(labelled, "Text.");
        AudiobookService.PartState both = AudiobookService.stateOf(1, labelled, true);
        assertTrue(both.labelled());
        assertEquals(AudiobookService.StoredAudio.CURRENT, both.audio());
        assertTrue(both.finalStatus());
    }

    @Test
    void audioMadeFromAnOlderTextIsOutdated() {
        ChatMessage edited = part("Text, edited.");
        withAudio(edited, "Text.");

        assertEquals(AudiobookService.StoredAudio.OUTDATED, AudiobookService.stateOf(2, edited, false).audio());
    }

    @Test
    void aLongTextIsCutInTheTitle() {
        String title = AudiobookService.stateOf(1, part("word ".repeat(30)), false).title();

        assertTrue(title.endsWith("..."), title);
        assertTrue(title.length() < 70, title);
    }
}
