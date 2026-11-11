package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.extensions.audiobook.label.Delivery;
import com.github.enerccio.marginalia.extensions.audiobook.label.LabelStore;
import com.github.enerccio.marginalia.extensions.audiobook.label.PieceLabel;
import com.github.enerccio.marginalia.extensions.audiobook.script.Hashes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The labels of a part in its attributes, under {@code audiobook} -> {@code labels}: {@code {"key": ..., "labels": [...]}}. One set per part: the
 * labels of an older text are replaced. They travel with the book like the audio index ({@link ResourceAudioStore}), and the studio will show them.
 * Must be used with the request context of the user.
 */
public class PartLabelStore implements LabelStore {

    private final ChatMessageService messages;

    public PartLabelStore(ChatMessageService messages) {
        this.messages = messages;
    }

    @Override
    public List<PieceLabel> find(String partId, String key) {
        try {
            ChatMessage part = messages.findForUser(partId);
            if (part == null || part.getAttributes() == null || !part.getAttributes().has(ResourceAudioStore.KEY)
                    || !part.getAttributes().get(ResourceAudioStore.KEY).isJsonObject()) {
                return null;
            }
            JsonObject index = part.getAttributes().getAsJsonObject(ResourceAudioStore.KEY);
            if (!index.has("labels") || !index.get("labels").isJsonObject()) {
                return null;
            }
            JsonObject stored = index.getAsJsonObject("labels");
            if (!stored.has("key") || !key.equals(stored.get("key").getAsString()) || !stored.has("labels")) {
                return null;
            }
            List<PieceLabel> labels = new ArrayList<>();
            for (var element : stored.getAsJsonArray("labels")) {
                JsonObject l = element.getAsJsonObject();
                labels.add(new PieceLabel(l.get("id").getAsInt(), l.get("speaker").getAsString(), Delivery.valueOf(l.get("delivery").getAsString()),
                        l.get("intensity").getAsDouble(), l.has("taggedText") ? l.get("taggedText").getAsString() : ""));
            }
            return labels;
        } catch (RuntimeException e) {
            // a stored set that cannot be read (changed by hand, an older form) is the same as none: the model is asked again
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /** True when labels are stored with the part (whatever instruction they were made for). */
    public static boolean has(ChatMessage part) {
        if (part == null || part.getAttributes() == null || !part.getAttributes().has(ResourceAudioStore.KEY)
                || !part.getAttributes().get(ResourceAudioStore.KEY).isJsonObject()) {
            return false;
        }
        JsonObject index = part.getAttributes().getAsJsonObject(ResourceAudioStore.KEY);
        return index.has("labels") && index.get("labels").isJsonObject() && index.getAsJsonObject("labels").has("labels");
    }

    /** Forgets the labels of the part: the model is asked again the next time the part is made. The audio is left as it is. */
    public void clear(String partId) {
        try {
            ChatMessage part = messages.findForUser(partId);
            if (part == null || !has(part)) {
                return;
            }
            part.getAttributes().getAsJsonObject(ResourceAudioStore.KEY).remove("labels");
            ResourceAudioStore.writeIndex(messages, part, "labels");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public List<PieceLabel> findForText(String partId, String text) {
        try {
            ChatMessage part = messages.findForUser(partId);
            if (part == null || !has(part)) {
                return null;
            }
            JsonObject stored = part.getAttributes().getAsJsonObject(ResourceAudioStore.KEY).getAsJsonObject("labels");
            if (!stored.has("textHash") || !Hashes.sha256(text == null ? "" : text).equals(stored.get("textHash").getAsString())) {
                return null;
            }
            return find(partId, stored.get("key").getAsString());
        } catch (RuntimeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void put(String partId, String key, String text, List<PieceLabel> labels) {
        store(partId, key, text, labels);
    }

    @Override
    public void put(String partId, String key, List<PieceLabel> labels) {
        store(partId, key, null, labels);
    }

    private void store(String partId, String key, String text, List<PieceLabel> labels) {
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
            JsonArray array = new JsonArray();
            for (PieceLabel label : labels) {
                JsonObject l = new JsonObject();
                l.addProperty("id", label.id());
                l.addProperty("speaker", label.speaker());
                l.addProperty("delivery", label.delivery() == null ? Delivery.NEUTRAL.name() : label.delivery().name());
                l.addProperty("intensity", label.intensity());
                l.addProperty("taggedText", label.taggedText() == null ? "" : label.taggedText());
                array.add(l);
            }
            JsonObject stored = new JsonObject();
            stored.addProperty("key", key);
            if (text != null) {
                stored.addProperty("textHash", Hashes.sha256(text));
            }
            stored.add("labels", array);
            index.add("labels", stored);
            part.getAttributes().add(ResourceAudioStore.KEY, index);
            ResourceAudioStore.writeIndex(messages, part, "labels");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
