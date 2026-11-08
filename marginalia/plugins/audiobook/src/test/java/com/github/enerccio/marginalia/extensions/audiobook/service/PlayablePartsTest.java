package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.extensions.audiobook.script.Hashes;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PlayablePartsTest {

    private static ChatMessage part(long id, String text, String resource, String sourceHash) {
        ChatMessage part = new ChatMessage();
        part.setId(id);
        part.setResponse(text);
        part.setAttributes(new JsonObject());
        if (resource != null) {
            JsonObject joined = new JsonObject();
            joined.addProperty("joinKey", "j");
            joined.addProperty("sourceHash", sourceHash);
            joined.addProperty("resource", resource);
            JsonObject index = new JsonObject();
            index.add("part", joined);
            part.getAttributes().add(ResourceAudioStore.KEY, index);
        }
        return part;
    }

    @Test
    void onlyPartsWithAudioMadeFromTheirPresentTextAreOffered() {
        ChatMessage fresh = part(1, "First.", "res-1", Hashes.sha256("First."));
        ChatMessage stale = part(2, "Second, edited.", "res-2", Hashes.sha256("Second."));
        ChatMessage none = part(3, "Third.", null, null);
        ChatMessage another = part(4, "Fourth.", "res-4", Hashes.sha256("Fourth."));

        Map<Long, String> playable = PlayableParts.of(List.of(fresh, stale, none, another));

        assertEquals(List.of(1L, 4L), List.copyOf(playable.keySet()), "in the order of the branch");
        assertEquals("res-1", playable.get(1L));
        assertEquals("res-4", playable.get(4L));
    }

    @Test
    void aPartWithoutAttributesOrWithOddOnesHasNoAudio() {
        ChatMessage plain = new ChatMessage();
        plain.setId(5L);
        plain.setResponse("Text.");
        assertNull(PlayableParts.ref(plain));

        ChatMessage odd = part(6, "Text.", null, null);
        odd.getAttributes().addProperty(ResourceAudioStore.KEY, "not an object");
        assertNull(PlayableParts.ref(odd));

        ChatMessage incomplete = part(7, "Text.", null, null);
        JsonObject index = new JsonObject();
        index.add("part", new JsonObject());
        incomplete.getAttributes().add(ResourceAudioStore.KEY, index);
        assertNull(PlayableParts.ref(incomplete));
        assertTrue(PlayableParts.of(List.of(plain, odd, incomplete)).isEmpty());
    }

    @Test
    void thePartAudioIsTheOneTheStoreWrote() {
        ChatMessage part = part(8, "Text.", "res-8", Hashes.sha256("Text."));
        PlayableParts.Ref ref = PlayableParts.ref(part);
        assertEquals("res-8", ref.resource());
        assertEquals(Hashes.sha256("Text."), ref.sourceHash());
    }
}
