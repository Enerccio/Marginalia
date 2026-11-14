package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.extensions.audiobook.studio.StudioScript;
import com.github.enerccio.marginalia.extensions.audiobook.studio.StudioStore;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The script of a part in its attributes, under {@code audiobook} -> {@code studio}, so it travels with the book like the audio index
 * ({@link ResourceAudioStore}) and the labels ({@link PartLabelStore}). Must be used with the request context of the user.
 */
public class PartStudioStore implements StudioStore {

    private static final String STUDIO = "studio";

    private final ChatMessageService messages;
    private final Gson gson = new Gson();

    public PartStudioStore(ChatMessageService messages) {
        this.messages = messages;
    }

    @Override
    public StudioScript find(String partId) {
        try {
            ChatMessage part = messages.findForUser(partId);
            if (part == null || part.getAttributes() == null || !part.getAttributes().has(ResourceAudioStore.KEY)
                    || !part.getAttributes().get(ResourceAudioStore.KEY).isJsonObject()) {
                return null;
            }
            JsonObject index = part.getAttributes().getAsJsonObject(ResourceAudioStore.KEY);
            return index.has(STUDIO) ? gson.fromJson(index.get(STUDIO), StudioScript.class) : null;
        } catch (RuntimeException e) {
            // a script that cannot be read (changed by hand, an older form) is the same as none
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void put(String partId, StudioScript script) {
        try {
            ChatMessage part = messages.findForUser(partId);
            if (part == null) {
                throw new IllegalStateException("The part is gone");
            }
            if (part.getAttributes() == null) {
                part.setAttributes(new JsonObject());
            }
            JsonObject index = part.getAttributes().has(ResourceAudioStore.KEY) && part.getAttributes().get(ResourceAudioStore.KEY).isJsonObject()
                    ? part.getAttributes().getAsJsonObject(ResourceAudioStore.KEY) : new JsonObject();
            index.add(STUDIO, gson.toJsonTree(script));
            part.getAttributes().add(ResourceAudioStore.KEY, index);
            ResourceAudioStore.writeIndex(messages, part, STUDIO);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
