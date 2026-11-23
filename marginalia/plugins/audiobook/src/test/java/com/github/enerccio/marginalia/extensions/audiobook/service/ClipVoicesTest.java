package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.domain.model.impl.Resource;
import com.github.enerccio.marginalia.domain.service.ResourceService;
import com.github.enerccio.marginalia.extensions.audiobook.queue.Voices;
import com.github.enerccio.marginalia.extensions.audiobook.script.VoiceRef;
import com.github.enerccio.marginalia.extensions.audiobook.script.VoiceShift;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClipVoicesTest {

    private static final VoiceShift LOWER = new VoiceShift(-2, 0, 100, VoiceShift.Eq.FLAT);

    /** The resources of the user: uuid → bytes. */
    private final Map<String, byte[]> files = new HashMap<>();
    /** What the speech server was given. */
    private final List<String> uploaded = new ArrayList<>();
    private final Map<String, String> stored = new HashMap<>();
    private boolean canMake;
    private int made;

    private ResourceService resources;
    private SpeechBackend backend;

    private static byte[] bytes(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    @BeforeEach
    void setUp() {
        files.put("men", bytes("men's clip"));
        resources = (ResourceService) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ResourceService.class}, (proxy, method, args) -> {
            switch (method.getName()) {
                case "findForUser" -> {
                    if (!files.containsKey((String) args[0])) {
                        return null;
                    }
                    Resource resource = new Resource();
                    resource.setUuid((String) args[0]);
                    resource.setOriginalName(args[0] + ".wav");
                    return resource;
                }
                case "getResourceData" -> {
                    return files.get(((Resource) args[0]).getUuid());
                }
                default -> throw new UnsupportedOperationException(method.getName());
            }
        });
        backend = (SpeechBackend) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{SpeechBackend.class}, (proxy, method, args) -> {
            if (method.getName().equals("ensureReference")) {
                String content = new String((byte[]) args[0], StandardCharsets.UTF_8);
                uploaded.add(content);
                return "server-name-of " + content;
            }
            throw new UnsupportedOperationException(method.getName());
        });
    }

    private ClipVoices voices() {
        return new ClipVoices(resources, uuid -> "words of " + uuid, new ClipVoices.ShiftedClips() {
            @Override
            public String find(String baseUuid, VoiceShift shift) {
                return stored.get(baseUuid + "~" + shift.spec());
            }

            @Override
            public boolean canMake() {
                return canMake;
            }

            @Override
            public byte[] make(byte[] base, VoiceShift shift) {
                made++;
                return bytes(new String(base, StandardCharsets.UTF_8) + " shifted " + shift.spec());
            }

            @Override
            public String store(String baseUuid, VoiceShift shift, byte[] audio, String baseFileName) {
                String uuid = baseUuid + "-" + shift.spec();
                files.put(uuid, audio);
                stored.put(baseUuid + "~" + shift.spec(), uuid);
                return uuid;
            }
        });
    }

    @Test
    void aShiftedClipIsMadeOnceThenTheStoredCopyIsSent() throws Exception {
        canMake = true;
        VoiceRef guard = VoiceRef.clip("men").withShift(LOWER);

        Voices.Resolved first = voices().resolve(backend, guard);
        Voices.Resolved second = voices().resolve(backend, guard);

        assertEquals(1, made, "made once, stored, then read");
        assertEquals("server-name-of men's clip shifted p-2.f+0.t100.eF", first.name());
        assertEquals(first, second);
        assertEquals("words of men", first.text(), "the copy says the words of its base");
        assertEquals(SpeechRequest.VoiceMode.CLONE, first.mode());
    }

    @Test
    void aStoredCopyIsUsedWithoutFfmpegAndAMissingOneIsTheBaseClip() throws Exception {
        canMake = false;
        assertEquals("server-name-of men's clip", voices().resolve(backend, VoiceRef.clip("men").withShift(LOWER)).name());
        assertEquals(0, made);

        files.put("men-copy", bytes("made elsewhere"));
        stored.put("men~" + LOWER.spec(), "men-copy");
        assertEquals("server-name-of made elsewhere", voices().resolve(backend, VoiceRef.clip("men").withShift(LOWER)).name());
    }

    @Test
    void aStoredCopyThatIsGoneIsMadeAgain() throws Exception {
        canMake = true;
        stored.put("men~" + LOWER.spec(), "deleted-copy");
        assertEquals("server-name-of men's clip shifted p-2.f+0.t100.eF", voices().resolve(backend, VoiceRef.clip("men").withShift(LOWER)).name());
        assertEquals(1, made);
    }

    @Test
    void aClipAsItIsAndAVoiceOfTheServerAreUntouched() throws Exception {
        canMake = true;
        assertEquals("server-name-of men's clip", voices().resolve(backend, VoiceRef.clip("men")).name());
        assertEquals(new Voices.Resolved(SpeechRequest.VoiceMode.PREDEFINED, "Henry.wav"), voices().resolve(backend, VoiceRef.predefined("Henry.wav")));
        assertEquals(0, made);
    }
}
