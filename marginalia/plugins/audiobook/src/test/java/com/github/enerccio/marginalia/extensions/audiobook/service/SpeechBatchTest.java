package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpeechBatchTest {

    private final List<String> events = new ArrayList<>();

    private final SpeechBatch.Gpu gpu = cancelled -> {
        events.add("take");
        return () -> events.add("give back");
    };

    private SpeechBackend backend(boolean ready) {
        return new SpeechBackend() {
            public void prepare() throws BackendException {
                events.add("prepare");
                if (!ready) {
                    throw new BackendException("no model loaded");
                }
            }

            public byte[] synthesize(SpeechRequest request, Cancel cancel) {
                return new byte[0];
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
    void theRequestsComeBeforeAndAfterTheWorkInsideTheLock() throws Exception {
        SpeechBatch.Outcome<String> outcome = SpeechBatch.run(backend(true), gpu, () -> events.add("before"), () -> events.add("after"), null,
                b -> {
                    events.add("work");
                    return "done";
                });

        assertEquals("done", outcome.value());
        assertNull(outcome.afterError());
        assertEquals(List.of("take", "before", "prepare", "work", "after", "give back"), events);
    }

    @Test
    void theRequestsAfterRunAndTheLockIsGivenBackWhenTheWorkFails() {
        assertThrows(BackendException.class, () -> SpeechBatch.run(backend(true), gpu, () -> events.add("before"), () -> events.add("after"), null,
                b -> {
                    throw new BackendException("broken");
                }));

        assertEquals(List.of("take", "before", "prepare", "after", "give back"), events);
    }

    @Test
    void aBatchStoppedBeforeItBeganTouchesNothing() {
        assertThrows(BackendException.class, () -> SpeechBatch.run(backend(true), gpu, () -> events.add("before"), () -> events.add("after"), () -> true,
                b -> "never"));

        assertEquals(List.of(), events);
    }

    @Test
    void aServerWithoutAModelStopsTheBatchBeforeTheWork() {
        assertThrows(BackendException.class, () -> SpeechBatch.run(backend(false), gpu, () -> events.add("before"), () -> events.add("after"), null,
                b -> {
                    events.add("work");
                    return null;
                }));

        assertEquals(List.of("take", "before", "prepare", "after", "give back"), events);
    }

    @Test
    void aFailedBeforeRequestRunsNothingElseAndGivesTheLockBack() {
        assertThrows(BackendException.class, () -> SpeechBatch.run(backend(true), gpu, () -> {
            throw new BackendException("cannot start");
        }, () -> events.add("after"), null, b -> "never"));

        assertEquals(List.of("take", "give back"), events);
    }

    @Test
    void aFailedAfterRequestIsReportedAndTheWorkKept() throws Exception {
        SpeechBatch.Outcome<String> outcome = SpeechBatch.run(backend(true), gpu, null, () -> {
            throw new BackendException("stop failed");
        }, null, b -> "done");

        assertEquals("done", outcome.value());
        assertEquals("stop failed", outcome.afterError());
    }

    @Test
    void aGpuThatCannotBeTakenRunsNothing() {
        SpeechBatch.Gpu broken = cancelled -> {
            throw new BackendException("a step of the lock failed");
        };
        assertThrows(BackendException.class, () -> SpeechBatch.run(backend(true), broken, () -> events.add("before"), () -> events.add("after"), null,
                b -> "never"));
        assertEquals(List.of(), events);
    }
}
