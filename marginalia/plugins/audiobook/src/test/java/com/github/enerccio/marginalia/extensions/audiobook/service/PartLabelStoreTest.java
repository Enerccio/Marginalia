package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.extensions.audiobook.label.Delivery;
import com.github.enerccio.marginalia.extensions.audiobook.label.PieceLabel;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PartLabelStoreTest {

    private ChatMessage part;
    private int saves;
    private ChatMessageService messages;

    @BeforeEach
    void setUp() {
        part = new ChatMessage();
        part.setUuid(UUID.randomUUID().toString());
        part.setAttributes(new JsonObject());
        messages = (ChatMessageService) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ChatMessageService.class}, (proxy, method, args) -> {
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

    private static final List<PieceLabel> LABELS = List.of(new PieceLabel(0, "Narrator", Delivery.NEUTRAL, 0.5, ""),
            new PieceLabel(1, "Tom\u00e1\u0161", Delivery.WHISPER, 0.25, "[sigh] Cold wall,"));

    @Test
    void theLabelsComeBackAsTheyWentIn() {
        PartLabelStore store = new PartLabelStore(messages);
        assertNull(store.find(part.getUuid(), "key-1"));

        store.put(part.getUuid(), "key-1", LABELS);

        assertEquals(1, saves);
        assertEquals(LABELS, new PartLabelStore(messages).find(part.getUuid(), "key-1"));
    }

    @Test
    void labelsMadeForAnotherTextOrInstructionAreNotFound() {
        PartLabelStore store = new PartLabelStore(messages);
        store.put(part.getUuid(), "key-1", LABELS);

        assertNull(store.find(part.getUuid(), "key-2"));
        store.put(part.getUuid(), "key-2", List.of(LABELS.getFirst()));
        assertEquals(1, store.find(part.getUuid(), "key-2").size(), "one set per part: the new one replaces the old");
        assertNull(store.find(part.getUuid(), "key-1"));
    }

    @Test
    void aSetThatCannotBeReadIsTheSameAsNone() {
        PartLabelStore store = new PartLabelStore(messages);
        store.put(part.getUuid(), "key-1", LABELS);
        part.getAttributes().getAsJsonObject(ResourceAudioStore.KEY).getAsJsonObject("labels").getAsJsonArray("labels").get(0).getAsJsonObject()
                .addProperty("delivery", "FURIOUS");

        assertNull(store.find(part.getUuid(), "key-1"), "the model is asked again");
    }

    @Test
    void theLabelsAndTheAudioIndexLiveSideBySide() {
        part.getAttributes().add(ResourceAudioStore.KEY, new JsonObject());
        part.getAttributes().getAsJsonObject(ResourceAudioStore.KEY).addProperty("note", "kept");

        new PartLabelStore(messages).put(part.getUuid(), "key-1", LABELS);

        JsonObject index = part.getAttributes().getAsJsonObject(ResourceAudioStore.KEY);
        assertEquals("kept", index.get("note").getAsString(), "what else is in the index stays");
        assertTrue(index.has("labels"));
    }

    @Test
    void aPartThatIsGoneIsAnError() {
        assertNull(new PartLabelStore(messages).find("gone", "key"));
        assertThrows(IllegalStateException.class, () -> new PartLabelStore(messages).put("gone", "key", LABELS));
    }
}
