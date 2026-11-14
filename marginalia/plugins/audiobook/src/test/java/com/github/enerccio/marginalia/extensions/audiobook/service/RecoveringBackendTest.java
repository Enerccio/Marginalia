package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.extensions.httphooks.ServerGoneException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecoveringBackendTest {

    private static final SpeechRequest REQUEST = new SpeechRequest("Hello.", SpeechRequest.VoiceMode.PREDEFINED, "v.wav", null, 0.5, 0.5, 0.8, 1.0, 0,
            "mp3", 240);

    private final List<String> events = new ArrayList<>();
    /** What the server does with the next pieces: null = audio, an exception = that. */
    private final List<BackendException> script = new ArrayList<>();
    private int prepared;

    private SpeechBackend server() {
        return new SpeechBackend() {
            public void prepare() {
                prepared++;
                events.add("prepare");
            }

            public byte[] synthesize(SpeechRequest request, Cancel cancel) throws BackendException {
                BackendException next = script.isEmpty() ? null : script.remove(0);
                if (next != null) {
                    events.add("gone");
                    throw next;
                }
                events.add("piece");
                return new byte[]{1};
            }

            public List<String> referenceFiles() {
                return List.of();
            }

            public List<String> predefinedVoices() {
                return List.of();
            }

            public String ensureReference(byte[] audio, String extension) {
                return "";
            }
        };
    }

    @Test
    void theServerIsRestartedEveryNPieces() throws Exception {
        RecoveringBackend backend = new RecoveringBackend(server(), () -> events.add("restart"), 2);

        for (int i = 0; i < 5; i++) {
            backend.synthesize(REQUEST, null);
        }

        assertEquals(List.of("piece", "piece", "restart", "prepare", "piece", "piece", "restart", "prepare", "piece"), events);
    }

    @Test
    void zeroNeverRestarts() throws Exception {
        RecoveringBackend backend = new RecoveringBackend(server(), () -> events.add("restart"), 0);

        for (int i = 0; i < 5; i++) {
            backend.synthesize(REQUEST, null);
        }

        assertFalse(events.contains("restart"));
    }

    @Test
    void aPieceThatFindsTheServerGoneIsMadeAgainOnTheRestartedServer() throws Exception {
        script.add(new ServerGoneException("Cannot reach host: reset"));
        RecoveringBackend backend = new RecoveringBackend(server(), () -> events.add("restart"), 0);

        byte[] audio = backend.synthesize(REQUEST, null);

        assertArrayEquals(new byte[]{1}, audio);
        assertEquals(List.of("gone", "restart", "prepare", "piece"), events);
    }

    @Test
    void aServerThatIsGoneAgainBreaksTheBackendAndTheRestPiecesFailAtOnce() throws Exception {
        script.add(new ServerGoneException("first"));
        script.add(new ServerGoneException("second"));
        RecoveringBackend backend = new RecoveringBackend(server(), () -> events.add("restart"), 0);

        BackendException e = assertThrows(BackendException.class, () -> backend.synthesize(REQUEST, null));
        assertTrue(e.getMessage().contains("gone again"), e.getMessage());
        events.clear();

        assertThrows(BackendException.class, () -> backend.synthesize(REQUEST, null));
        assertEquals(List.of(), events, "nothing is tried on a broken backend");
    }

    @Test
    void aRestartThatFailsBreaksTheBackend() {
        script.add(new ServerGoneException("down"));
        RecoveringBackend backend = new RecoveringBackend(server(), () -> {
            throw new BackendException("tts/start answered 500");
        }, 0);

        BackendException e = assertThrows(BackendException.class, () -> backend.synthesize(REQUEST, null));
        assertTrue(e.getMessage().contains("could not be restarted"), e.getMessage());
        assertThrows(BackendException.class, () -> backend.synthesize(REQUEST, null));
        assertEquals(0, prepared);
    }

    @Test
    void anErrorOfAServerThatIsUpIsNotRetried() {
        script.add(new BackendException("Making the speech failed: HTTP 422"));
        RecoveringBackend backend = new RecoveringBackend(server(), () -> events.add("restart"), 0);

        assertThrows(BackendException.class, () -> backend.synthesize(REQUEST, null));
        assertFalse(events.contains("restart"));
    }

    @Test
    void aStopIsNotATrigger() {
        script.add(new ServerGoneException("Cannot reach host: cancelled"));
        RecoveringBackend backend = new RecoveringBackend(server(), () -> events.add("restart"), 0);

        assertThrows(ServerGoneException.class, () -> backend.synthesize(REQUEST, () -> true));
        assertFalse(events.contains("restart"));
    }
}
