package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.extensions.audiobook.script.PartScript;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Which parts of a branch have audio the reader can hear: the part has a joined audio ({@link ResourceAudioStore}) that was made from the text
 * the part has now. Audio of an older text is not offered: it would read words that are not in the book any more.
 */
public final class PlayableParts {

    private PlayableParts() {
    }

    /** The audio of the part: the uuid of its resource and the hash of the text it was made from; null when the part has none. */
    public record Ref(String resource, String sourceHash) {
    }

    /** @return the joined audio the part has, whatever its text is now; null when it has none */
    public static Ref ref(ChatMessage part) {
        JsonObject attributes = part.getAttributes();
        if (attributes == null || !attributes.has(ResourceAudioStore.KEY) || !attributes.get(ResourceAudioStore.KEY).isJsonObject()) {
            return null;
        }
        JsonObject index = attributes.getAsJsonObject(ResourceAudioStore.KEY);
        if (!index.has("part") || !index.get("part").isJsonObject()) {
            return null;
        }
        JsonObject joined = index.getAsJsonObject("part");
        if (!joined.has("resource") || !joined.has("sourceHash")) {
            return null;
        }
        return new Ref(joined.get("resource").getAsString(), joined.get("sourceHash").getAsString());
    }

    /**
     * @return the id of each part of the branch that has audio made from its present text, with the uuid of the resource, in the order of the
     * branch (the id is the {@code data-message-id} of the part in the reader)
     */
    public static Map<Long, String> of(List<ChatMessage> branch) {
        Map<Long, String> playable = new LinkedHashMap<>();
        for (ChatMessage part : branch) {
            Ref ref = ref(part);
            if (ref != null && part.getId() != null && !PartScript.stale(ref.sourceHash(), part.getResponse())) {
                playable.put(part.getId(), ref.resource());
            }
        }
        return playable;
    }
}
