package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.extensions.audiobook.queue.AudioStore;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** The two services of the application are stood in for by proxies that do what the store uses of them. */
class ResourceAudioStoreTest {

    private final Map<String, byte[]> files = new LinkedHashMap<>();
    private final Map<String, Resource> resources = new HashMap<>();
    private final List<String> softDeleted = new ArrayList<>();
    private final List<String> linked = new ArrayList<>();
    private int saves;
    private ChatMessage part;
    private ResourceService resourceService;
    private ChatMessageService messageService;

    @BeforeEach
    void setUp() {
        part = new ChatMessage();
        part.setUuid(UUID.randomUUID().toString());
        part.setId(7L);
        part.setAttributes(new JsonObject());
        resourceService = (ResourceService) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ResourceService.class}, (proxy, method, args) -> {
            switch (method.getName()) {
                case "upload" -> {
                    Resource resource = new Resource();
                    resource.setUuid(UUID.randomUUID().toString());
                    resource.setOriginalName((String) args[0]);
                    resource.setMimeType((String) args[2]);
                    files.put(resource.getUuid(), (byte[]) args[1]);
                    resources.put(resource.getUuid(), resource);
                    return resource;
                }
                case "link" -> {
                    linked.add(((Resource) args[0]).getUuid() + "->" + args[1]);
                    return args[0];
                }
                case "findForUser" -> {
                    return softDeleted.contains(args[0]) ? null : resources.get(args[0]);
                }
                case "getResourceData" -> {
                    return files.get(((Resource) args[0]).getUuid());
                }
                case "softDelete" -> {
                    softDeleted.addAll((Collection<String>) args[0]);
                    return ((Collection<?>) args[0]).size();
                }
                default -> throw new UnsupportedOperationException(method.getName());
            }
        });
        messageService = (ChatMessageService) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ChatMessageService.class}, (proxy, method, args) -> {
            switch (method.getName()) {
                case "findForUser" -> {
                    return part.getUuid().equals(args[0]) ? part : null;
                }
                case "save" -> {
                    saves++;
                    return args[0];
                }
                default -> throw new UnsupportedOperationException(method.getName());
            }
        });
    }

    private ResourceAudioStore store() {
        return new ResourceAudioStore(resourceService, messageService);
    }

    private static final String KEY_A = "a".repeat(64);
    private static final String KEY_B = "b".repeat(64);

    @Test
    void aPieceIsFoundBeforeAndAfterTheFlush() {
        ResourceAudioStore store = store();
        assertNull(store.segment(part.getUuid(), KEY_A));

        store.putSegment(part.getUuid(), KEY_A, new byte[]{1, 2, 3});

        assertArrayEquals(new byte[]{1, 2, 3}, store.segment(part.getUuid(), KEY_A), "found before the part is saved");
        assertEquals(0, saves, "the part is not saved for every piece");
        store.flush(part.getUuid());
        assertEquals(1, saves);
        assertArrayEquals(new byte[]{1, 2, 3}, store().segment(part.getUuid(), KEY_A), "another store finds it from the part's attributes");
        assertTrue(part.getAttributes().getAsJsonObject(ResourceAudioStore.KEY).getAsJsonObject("segments").has(KEY_A));
        assertEquals(1, linked.size());
        assertTrue(linked.getFirst().endsWith("->" + ChatMessage.class), linked.getFirst());
    }

    @Test
    void aFlushWithNothingPendingSavesNothing() {
        store().flush(part.getUuid());
        assertEquals(0, saves);
    }

    @Test
    void thePartAudioIsKeptAndTheOldPiecesAreDropped() {
        ResourceAudioStore store = store();
        store.putSegment(part.getUuid(), KEY_A, new byte[]{1});
        store.putSegment(part.getUuid(), KEY_B, new byte[]{2});
        store.putPart(part.getUuid(), "join-1", "source-1", new byte[]{9, 9}, List.of(KEY_A, KEY_B));
        store.flush(part.getUuid());
        assertEquals(new AudioStore.PartAudio("join-1", "source-1"), store().partAudio(part.getUuid()));
        assertArrayEquals(new byte[]{9, 9}, store().partBytes(part.getUuid()));
        assertTrue(softDeleted.isEmpty());
        String firstPart = part.getAttributes().getAsJsonObject(ResourceAudioStore.KEY).getAsJsonObject("part").get("resource").getAsString();

        // the text changed: only the piece A is still in the script
        ResourceAudioStore again = store();
        again.putPart(part.getUuid(), "join-2", "source-2", new byte[]{7}, List.of(KEY_A));
        again.flush(part.getUuid());

        assertEquals(new AudioStore.PartAudio("join-2", "source-2"), store().partAudio(part.getUuid()));
        assertNotNull(store().segment(part.getUuid(), KEY_A));
        assertNull(store().segment(part.getUuid(), KEY_B), "the piece of the old text is gone");
        assertEquals(2, softDeleted.size(), "the old joined audio and the old piece");
        assertTrue(softDeleted.contains(firstPart));
        assertArrayEquals(new byte[]{7}, store().partBytes(part.getUuid()));
    }

    @Test
    void theLabelsOfThePartSurviveTheFlushOfItsAudio() {
        new PartLabelStore(messageService).put(part.getUuid(), "label-key", List.of(
                new com.github.enerccio.marginalia.extensions.audiobook.label.PieceLabel(0, "Narrator",
                        com.github.enerccio.marginalia.extensions.audiobook.label.Delivery.NEUTRAL, 0.5, "")));
        ResourceAudioStore store = store();
        store.putSegment(part.getUuid(), KEY_A, new byte[]{1});
        store.putPart(part.getUuid(), "join-1", "source-1", new byte[]{9}, List.of(KEY_A));
        store.flush(part.getUuid());

        assertNotNull(new PartLabelStore(messageService).find(part.getUuid(), "label-key"), "the audio flush kept the labels");
        assertNotNull(store().segment(part.getUuid(), KEY_A), "and the labels write kept the audio");
    }

    @Test
    void aPartWithoutAudioHasNone() {
        assertNull(store().partAudio(part.getUuid()));
        assertNull(store().partBytes(part.getUuid()));
        assertNull(store().segment("another part", KEY_A));
    }

    @Test
    void piecesOfAFailedJobStayForTheRetry() {
        ResourceAudioStore store = store();
        store.putSegment(part.getUuid(), KEY_A, new byte[]{1});
        store.flush(part.getUuid());      // the job failed: flushed without putPart

        assertNull(store().partAudio(part.getUuid()));
        assertNotNull(store().segment(part.getUuid(), KEY_A));
        assertTrue(softDeleted.isEmpty());
    }

    @Test
    void aPartThatIsGoneIsAnError() {
        ResourceAudioStore store = store();
        assertThrows(IllegalStateException.class, () -> store.putSegment("gone", KEY_A, new byte[]{1}));
    }
}
