package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.extensions.httphooks.GpuGate;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpeechBatchTest {

    private final List<String> events = new ArrayList<>();

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
    void theRequestsComeBeforeAndAfterTheWork() throws Exception {
        SpeechBatch.Outcome<String> outcome = SpeechBatch.run(backend(true), new GpuGate(), () -> events.add("before"), () -> events.add("after"), null,
                b -> {
                    events.add("work");
                    return "done";
                });

        assertEquals(List.of("before", "prepare", "work", "after"), events);
        assertEquals("done", outcome.value());
        assertNull(outcome.afterError());
    }

    @Test
    void theAfterRequestsRunWhenTheWorkFails() {
        GpuGate gate = new GpuGate();

        assertThrows(BackendException.class, () -> SpeechBatch.run(backend(true), gate, () -> events.add("before"), () -> events.add("after"), null,
                b -> {
                    throw new BackendException("boom");
                }));

        assertEquals(List.of("before", "prepare", "after"), events);
        assertEquals(0, gate.holders());
    }

    @Test
    void aServerWithoutAModelStopsTheBatchBeforeTheWork() {
        assertThrows(BackendException.class, () -> SpeechBatch.run(backend(false), new GpuGate(), () -> events.add("before"), () -> events.add("after"), null,
                b -> {
                    events.add("work");
                    return null;
                }));

        assertEquals(List.of("before", "prepare", "after"), events);
    }

    @Test
    void aFailedBeforeRequestHoldsNothingAndRunsNothingElse() {
        GpuGate gate = new GpuGate();

        assertThrows(BackendException.class, () -> SpeechBatch.run(backend(true), gate, () -> {
            throw new BackendException("cannot unload");
        }, () -> events.add("after"), null, b -> "never"));

        assertEquals(List.of(), events);
        assertEquals(0, gate.holders());
    }

    @Test
    void aFailedAfterRequestIsReportedAndTheWorkKept() throws Exception {
        SpeechBatch.Outcome<String> outcome = SpeechBatch.run(backend(true), new GpuGate(), null, () -> {
            throw new BackendException("stop failed");
        }, null, b -> "done");

        assertEquals("done", outcome.value());
        assertEquals("stop failed", outcome.afterError());
    }

    @Test
    void batchesOnOneGpuShareTheRequestsAndTakeTurns() throws Exception {
        GpuGate gate = new GpuGate();
        java.util.concurrent.CountDownLatch firstIsWorking = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch secondHasJoined = new java.util.concurrent.CountDownLatch(1);
        Thread[] second = new Thread[1];

        SpeechBatch.Outcome<String> first = SpeechBatch.run(backend(true), gate, () -> events.add("before"), () -> events.add("after"), null, b -> {
            second[0] = new Thread(() -> {
                try {
                    firstIsWorking.await();
                    // joins the lease while the first still holds it: no second "before"
                    SpeechBatch.run(backend(true), gate, () -> events.add("before"), () -> events.add("after"), null, x -> {
                        events.add("second work");
                        return "second";
                    });
                } catch (Exception e) {
                    events.add("failed " + e.getMessage());
                }
                secondHasJoined.countDown();
            });
            second[0].start();
            firstIsWorking.countDown();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new BackendException("interrupted", e);
            }
            events.add("first work");
            return "first";
        });
        assertEquals("first", first.value());
        assertTrue(secondHasJoined.await(10, java.util.concurrent.TimeUnit.SECONDS));
        second[0].join();

        // one start for the first batch, the second batch ran after it; the server was stopped when the last one left
        assertEquals(1, events.stream().filter("before"::equals).count(), events.toString());
        assertTrue(events.indexOf("first work") < events.indexOf("second work"), events.toString());
        assertEquals("after", events.getLast());
        assertEquals(0, gate.holders());
    }
}
