package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.extensions.audiobook.queue.LabelTrace;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * What the labelling model said about a part, every run of it: one JSON resource linked to the part ({@code {"runs": [{"at", "windows": [{"title",
 * "reasoning", "output"}]}]}}), named in the attributes of the part under {@code audiobook} -> {@value #REASONING}. A file and not the attributes:
 * the reasoning is long, and the part is read and written whole. It travels with the book like the audio ({@link ResourceAudioStore}); a run is
 * added by writing a new file with all the runs and deleting the old one (files are never changed). Only {@link #clear} forgets it: new labels
 * or new audio leave it alone. Must be used with the request context of the user.
 */
public class PartReasoningStore {

    static final String REASONING = "reasoning";

    /** One labelling of the part: when it ended and what the model said, request by request. */
    public record Run(String at, List<LabelTrace.Window> windows) {
    }

    private record Stored(List<Run> runs) {
    }

    private final ResourceService resources;
    private final ChatMessageService messages;
    private final Gson gson = new Gson();

    public PartReasoningStore(ResourceService resources, ChatMessageService messages) {
        this.resources = resources;
        this.messages = messages;
    }

    /** @return the runs, oldest first; empty when nothing is stored or the file is gone (a backup without its files) or cannot be read */
    public List<Run> find(String partId) {
        try {
            ChatMessage part = messages.findForUser(partId);
            String uuid = part == null ? null : uuidOf(part);
            return uuid == null ? List.of() : read(uuid);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /** True when the part names a stored reasoning (the file is not read). */
    public static boolean has(ChatMessage part) {
        return part != null && uuidOf(part) != null;
    }

    /** Adds a run after the stored ones. */
    public void append(String partId, List<LabelTrace.Window> windows) {
        if (windows == null || windows.isEmpty()) {
            return;
        }
        try {
            ChatMessage part = messages.findForUser(partId);
            if (part == null) {
                throw new IllegalStateException("The part is gone");
            }
            String old = uuidOf(part);
            List<Run> runs = new ArrayList<>(old == null ? List.of() : read(old));
            runs.add(new Run(Instant.now().toString(), List.copyOf(windows)));
            byte[] content = gson.toJson(new Stored(runs)).getBytes(StandardCharsets.UTF_8);
            Resource resource = resources.link(resources.upload(ResourceAudioStore.FILE_PREFIX + "reasoning.json", content, "application/json"),
                    ChatMessage.class, part.getId());
            JsonObject index = indexOf(part);
            index.addProperty(REASONING, resource.getUuid());
            part.getAttributes().add(ResourceAudioStore.KEY, index);
            ResourceAudioStore.writeIndex(messages, part, REASONING);
            if (old != null) {
                resources.softDelete(List.of(old));
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /** Forgets the reasoning of the part and deletes its file. @return whether there was one */
    public boolean clear(String partId) {
        try {
            ChatMessage part = messages.findForUser(partId);
            String old = part == null ? null : uuidOf(part);
            if (old == null) {
                return false;
            }
            JsonObject index = indexOf(part);
            index.remove(REASONING);
            part.getAttributes().add(ResourceAudioStore.KEY, index);
            ResourceAudioStore.writeIndex(messages, part, REASONING);
            resources.softDelete(List.of(old));
            return true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private List<Run> read(String uuid) throws Exception {
        Resource resource = resources.findForUser(uuid);
        if (resource == null) {
            return List.of();
        }
        try {
            Stored stored = gson.fromJson(new String(resources.getResourceData(resource), StandardCharsets.UTF_8), Stored.class);
            return stored == null || stored.runs() == null ? List.of() : stored.runs();
        } catch (RuntimeException e) {
            // a file changed by hand is the same as none; the next run starts a new one
            return List.of();
        }
    }

    private static String uuidOf(ChatMessage part) {
        JsonObject index = indexOf(part);
        return index.has(REASONING) && index.get(REASONING).isJsonPrimitive() ? index.get(REASONING).getAsString() : null;
    }

    private static JsonObject indexOf(ChatMessage part) {
        JsonObject attributes = part.getAttributes();
        if (attributes == null) {
            attributes = new JsonObject();
            part.setAttributes(attributes);
        }
        return attributes.has(ResourceAudioStore.KEY) && attributes.get(ResourceAudioStore.KEY).isJsonObject()
                ? attributes.getAsJsonObject(ResourceAudioStore.KEY) : new JsonObject();
    }
}
