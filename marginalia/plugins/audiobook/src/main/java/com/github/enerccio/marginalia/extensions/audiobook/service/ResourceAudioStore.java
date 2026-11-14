package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.extensions.audiobook.audio.AudioKind;
import com.github.enerccio.marginalia.extensions.audiobook.queue.AudioStore;
import com.google.gson.JsonObject;

import java.io.File;
import java.util.*;

/**
 * The audio of the parts as resources of the user. Every piece and every joined part is a {@link Resource} linked to its part; which resource
 * is which is kept in the attributes of the part, under {@value #KEY}: {@code {"segments": {"<cache key>": "<resource uuid>"}, "part": {"joinKey",
 * "sourceHash", "resource"}}}. So the audio travels with the book (a backup, an export of the book) and is found from the part alone.
 * <p>
 * Writes are kept in memory until {@link #flush}, which the queue calls when a job is over: the part is saved once, not once per piece (the
 * user may be editing it, and a save writes the whole part). What was put and not flushed is found by the reads of this object.
 * Must be used with the request context of the user (the services work for the current user).
 */
public class ResourceAudioStore implements AudioStore {

    /** The names of the resources this class makes start with it (the cast does not offer them as reference clips). */
    public static final String FILE_PREFIX = "audiobook-";

    public static final String KEY = "audiobook";

    private final ResourceService resources;
    private final ChatMessageService messages;

    private record PendingPart(String joinKey, String sourceHash, String resource, Set<String> liveKeys, Double loudness) {
    }

    private final Map<String, Map<String, String>> pendingSegments = new HashMap<>();
    private final Map<String, PendingPart> pendingParts = new HashMap<>();
    private final Map<String, Long> partIds = new HashMap<>();

    public ResourceAudioStore(ResourceService resources, ChatMessageService messages) {
        this.resources = resources;
        this.messages = messages;
    }

    @Override
    public synchronized byte[] segment(String partId, String key) {
        String uuid = Optional.ofNullable(pendingSegments.get(partId)).map(m -> m.get(key)).orElse(null);
        if (uuid == null) {
            JsonObject segments = index(partId).getAsJsonObject("segments");
            uuid = segments != null && segments.has(key) ? segments.get(key).getAsString() : null;
        }
        return uuid == null ? null : data(uuid);
    }

    @Override
    public synchronized void putSegment(String partId, String key, byte[] audio) {
        Resource resource = upload(FILE_PREFIX + key.substring(0, 12) + "." + AudioKind.of(audio).extension(), audio, partId);
        pendingSegments.computeIfAbsent(partId, p -> new HashMap<>()).put(key, resource.getUuid());
    }

    @Override
    public synchronized PartAudio partAudio(String partId) {
        PendingPart pending = pendingParts.get(partId);
        if (pending != null) {
            return new PartAudio(pending.joinKey(), pending.sourceHash(), pending.loudness());
        }
        JsonObject part = index(partId).getAsJsonObject("part");
        if (part == null || !part.has("joinKey") || !part.has("sourceHash")) {
            return null;
        }
        // the index of a book that came without its files (a backup without the resources) names audio that is not there: there is no audio
        if (part.has("resource") && !exists(part.get("resource").getAsString())) {
            return null;
        }
        return new PartAudio(part.get("joinKey").getAsString(), part.get("sourceHash").getAsString(),
                part.has("loudness") && part.get("loudness").isJsonPrimitive() ? part.get("loudness").getAsDouble() : null);
    }

    @Override
    public synchronized void putPart(String partId, String joinKey, String sourceHash, byte[] audio, Collection<String> liveKeys) {
        putPart(partId, joinKey, sourceHash, audio, liveKeys, null);
    }

    @Override
    public synchronized void putPart(String partId, String joinKey, String sourceHash, byte[] audio, Collection<String> liveKeys, Double loudness) {
        Resource resource = upload(FILE_PREFIX + "part." + AudioKind.of(audio).extension(), audio, partId);
        pendingParts.put(partId, new PendingPart(joinKey, sourceHash, resource.getUuid(), new HashSet<>(liveKeys), loudness));
    }

    @Override
    public synchronized void flush(String partId) {
        Map<String, String> segments = pendingSegments.remove(partId);
        PendingPart part = pendingParts.remove(partId);
        if (segments == null && part == null) {
            return;
        }
        try {
            ChatMessage message = messages.findForUser(partId);
            if (message == null) {
                throw new IllegalStateException("The part is gone");
            }
            JsonObject index = indexOf(message);
            JsonObject stored = index.has("segments") ? index.getAsJsonObject("segments") : new JsonObject();
            List<String> dropped = new ArrayList<>();
            if (segments != null) {
                segments.forEach(stored::addProperty);
            }
            if (part != null) {
                if (index.has("part") && index.getAsJsonObject("part").has("resource")) {
                    dropped.add(index.getAsJsonObject("part").get("resource").getAsString());
                }
                for (String key : new ArrayList<>(stored.keySet())) {
                    if (!part.liveKeys().contains(key)) {
                        dropped.add(stored.get(key).getAsString());
                        stored.remove(key);
                    }
                }
                JsonObject partIndex = new JsonObject();
                partIndex.addProperty("joinKey", part.joinKey());
                partIndex.addProperty("sourceHash", part.sourceHash());
                partIndex.addProperty("resource", part.resource());
                if (part.loudness() != null) {
                    partIndex.addProperty("loudness", part.loudness());
                }
                index.add("part", partIndex);
            }
            index.add("segments", stored);
            message.getAttributes().add(KEY, index);
            writeIndex(messages, message, "segments", "part");
            if (!dropped.isEmpty()) {
                resources.softDelete(dropped);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * Forgets all the audio of the part, the pieces and the joined audio, and deletes the files; the labels and the script of the part stay. What was
     * made since the last flush is dropped too.
     *
     * @return how many files were deleted
     */
    public synchronized int discardAudio(String partId) {
        pendingSegments.remove(partId);
        pendingParts.remove(partId);
        try {
            ChatMessage message = messages.findForUser(partId);
            if (message == null) {
                return 0;
            }
            JsonObject index = indexOf(message);
            List<String> dropped = new ArrayList<>();
            if (index.has("segments") && index.get("segments").isJsonObject()) {
                index.getAsJsonObject("segments").entrySet().forEach(e -> dropped.add(e.getValue().getAsString()));
            }
            if (index.has("part") && index.get("part").isJsonObject() && index.getAsJsonObject("part").has("resource")) {
                dropped.add(index.getAsJsonObject("part").get("resource").getAsString());
            }
            if (dropped.isEmpty() && !index.has("segments") && !index.has("part")) {
                return 0;
            }
            index.remove("segments");
            index.remove("part");
            message.getAttributes().add(KEY, index);
            writeIndex(messages, message, "segments", "part");
            if (!dropped.isEmpty()) {
                resources.softDelete(dropped);
            }
            return dropped.size();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /** The joined audio of the part as it is stored (after the last flush or put), null when there is none. */
    public synchronized byte[] partBytes(String partId) {
        PendingPart pending = pendingParts.get(partId);
        String uuid = pending != null ? pending.resource() : null;
        if (uuid == null) {
            JsonObject part = index(partId).getAsJsonObject("part");
            uuid = part != null && part.has("resource") ? part.get("resource").getAsString() : null;
        }
        return uuid == null ? null : data(uuid);
    }

    /**
     * The file of the joined audio of the part (for the exports, which read it without loading it), null when the part has none.
     */
    public synchronized File partFile(String partId) {
        PendingPart pending = pendingParts.get(partId);
        String uuid = pending != null ? pending.resource() : null;
        if (uuid == null) {
            JsonObject part = index(partId).getAsJsonObject("part");
            uuid = part != null && part.has("resource") ? part.get("resource").getAsString() : null;
        }
        if (uuid == null) {
            return null;
        }
        try {
            Resource resource = resources.findForUser(uuid);
            return resource == null ? null : resources.getResourceFile(resource);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private Resource upload(String filename, byte[] audio, String partId) {
        try {
            Resource resource = resources.upload(filename, audio, AudioKind.of(audio).mime());
            Long id = partIds.get(partId);
            if (id == null) {
                ChatMessage message = messages.findForUser(partId);
                if (message == null) {
                    throw new IllegalStateException("The part is gone");
                }
                id = message.getId();
                partIds.put(partId, id);
            }
            return resources.link(resource, ChatMessage.class, id);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private boolean exists(String uuid) {
        try {
            return resources.findForUser(uuid) != null;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private byte[] data(String uuid) {
        try {
            Resource resource = resources.findForUser(uuid);
            return resource == null ? null : resources.getResourceData(resource);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /** What is stored in the part (a copy of the part is read, nothing is kept). */
    private JsonObject index(String partId) {
        try {
            ChatMessage message = messages.findForUser(partId);
            return message == null ? new JsonObject() : indexOf(message);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * Saves the entries {@code names} of the index of {@code changed} (a part read a moment ago and changed by the caller) into the part as it
     * is stored now, in one write transaction: the rest of the part (its text, the data of other plugins, the other entries of the index, which
     * the labels and the studio script keep) is left as it is stored, not as it was read.
     */
    static ChatMessage writeIndex(ChatMessageService messages, ChatMessage changed, String... names) throws Exception {
        JsonObject index = indexOf(changed);
        return messages.update(changed, fresh -> {
            JsonObject target = indexOf(fresh);
            for (String name : names) {
                if (index.has(name)) {
                    target.add(name, index.get(name));
                } else {
                    target.remove(name);
                }
            }
            fresh.getAttributes().add(KEY, target);
        });
    }

    private static JsonObject indexOf(ChatMessage message) {
        JsonObject attributes = message.getAttributes();
        if (attributes == null) {
            attributes = new JsonObject();
            message.setAttributes(attributes);
        }
        return attributes.has(KEY) && attributes.get(KEY).isJsonObject() ? attributes.getAsJsonObject(KEY) : new JsonObject();
    }
}
