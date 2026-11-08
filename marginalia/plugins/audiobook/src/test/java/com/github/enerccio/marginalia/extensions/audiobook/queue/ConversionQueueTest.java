package com.github.enerccio.marginalia.extensions.audiobook.queue;

import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.audiobook.queue.ConversionJob.State;
import com.github.enerccio.marginalia.extensions.audiobook.script.PartScript;
import com.github.enerccio.marginalia.extensions.audiobook.script.ScriptBuilder;
import com.github.enerccio.marginalia.extensions.audiobook.script.VoiceRef;
import com.github.enerccio.marginalia.extensions.audiobook.service.SpeechBackend;
import com.github.enerccio.marginalia.extensions.audiobook.service.SpeechBatch;
import com.github.enerccio.marginalia.extensions.audiobook.service.SpeechRequest;
import com.github.enerccio.marginalia.extensions.audiobook.service.SpeechRequest.VoiceMode;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

class ConversionQueueTest {

    private static final VoiceRef NARRATOR = VoiceRef.predefined("Henry.wav");

    /** What the fake server was asked to say, in order. */
    private final List<SpeechRequest> spoken = new CopyOnWriteArrayList<>();
    /** The batches the runner made: the model of each. */
    private final List<String> batches = new CopyOnWriteArrayList<>();
    private final Map<String, byte[]> segments = new HashMap<>();
    private final Map<String, AudioStore.PartAudio> parts = new HashMap<>();
    private final Map<String, byte[]> partBytes = new HashMap<>();
    private final List<String> flushed = new CopyOnWriteArrayList<>();
    private final Set<String> failing = Collections.synchronizedSet(new HashSet<>());
    private final AtomicBoolean paused = new AtomicBoolean();
    private volatile CountDownLatch holdSpeech;
    private volatile CountDownLatch speechStarted = new CountDownLatch(1);
    private volatile String beforeFails;

    private ConversionQueue queue;

    private static byte[] tone() {
        try (var in = ConversionQueueTest.class.getResourceAsStream("/tone.mp3")) {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private final AudioStore store = new AudioStore() {
        public synchronized byte[] segment(String partId, String key) {
            return segments.get(partId + "/" + key);
        }

        public synchronized void putSegment(String partId, String key, byte[] audio) {
            segments.put(partId + "/" + key, audio);
        }

        public synchronized PartAudio partAudio(String partId) {
            return parts.get(partId);
        }

        public synchronized void putPart(String partId, String joinKey, String sourceHash, byte[] audio, java.util.Collection<String> liveKeys) {
            parts.put(partId, new PartAudio(joinKey, sourceHash));
            partBytes.put(partId, audio);
            segments.keySet().removeIf(k -> k.startsWith(partId + "/") && !liveKeys.contains(k.substring(partId.length() + 1)));
        }

        public void flush(String partId) {
            flushed.add(partId);
        }
    };

    private SpeechBackend speaker(String model) {
        return new SpeechBackend() {
            public void prepare() {
            }

            public byte[] synthesize(SpeechRequest request, Cancel cancel) throws BackendException {
                speechStarted.countDown();
                CountDownLatch hold = holdSpeech;
                if (hold != null) {
                    try {
                        hold.await(10, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        throw new BackendException("interrupted", e);
                    }
                    if (cancel != null && cancel.isCancelled()) {
                        throw new BackendException("Cancelled");
                    }
                }
                if (failing.contains(request.text())) {
                    throw new BackendException("the server said no to: " + request.text());
                }
                spoken.add(request);
                return tone();
            }

            public List<String> referenceFiles() {
                return List.of();
            }

            public List<String> predefinedVoices() {
                return List.of();
            }

            public String ensureReference(byte[] audio, String extension) {
                return "m-x.wav";
            }
        };
    }

    private ConversionQueue.BatchRunner runner = new ConversionQueue.BatchRunner() {
        public <T> SpeechBatch.Outcome<T> run(BackendConfig backend, BooleanSupplier cancelled, SpeechBatch.Work<T> work) throws BackendException {
            batches.add(backend.getModel());
            if (beforeFails != null) {
                throw new BackendException(beforeFails);
            }
            T value = work.run(speaker(backend.getModel()));
            return new SpeechBatch.Outcome<>(value, null);
        }
    };

    private ConversionQueue queue(Set<String> models) {
        Voices voices = (backend, voice) -> new Voices.Resolved(voice.kind() == VoiceRef.Kind.CLIP ? VoiceMode.CLONE : VoiceMode.PREDEFINED,
                voice.kind() == VoiceRef.Kind.CLIP ? "m-" + voice.id() + ".wav" : voice.id());
        queue = new ConversionQueue(new ConversionQueue.Environment(store, voices, model -> {
            if (!models.contains(model)) {
                return null;
            }
            BackendConfig config = new BackendConfig();
            config.setModel(model);
            return config;
        }, runner, paused::get, 10));
        return queue;
    }

    @AfterEach
    void stop() {
        if (queue != null) {
            queue.close();
        }
    }

    private static PartScript script(String text) {
        return ScriptBuilder.narrated(text, NARRATOR, new BackendConfig(), 42);
    }

    private static PartScript script(String text, String model) {
        BackendConfig config = new BackendConfig();
        config.setModel(model);
        return ScriptBuilder.narrated(text, NARRATOR, config, 42);
    }

    private ConversionJob.Snapshot finished(ConversionJob job) throws InterruptedException {
        assertTrue(queue.awaitIdle(10, TimeUnit.SECONDS), "the queue is still busy: " + queue.jobs());
        return job.snapshot();
    }

    @Test
    void aPartIsSpokenPieceByPieceAndJoined() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO));
        PartScript script = script("# Title\n\nFirst paragraph.\n\nSecond paragraph.");

        ConversionJob job = queue.submit("p1", "Part 1", () -> script);
        ConversionJob.Snapshot done = finished(job);

        assertEquals(State.DONE, done.state(), done.message());
        assertEquals(3, spoken.size());
        assertEquals(List.of("Title", "First paragraph.", "Second paragraph."), spoken.stream().map(SpeechRequest::text).toList());
        assertEquals(3, done.done());
        assertEquals(3, done.total());
        assertEquals(new AudioStore.PartAudio(script.joinKey(), script.sourceHash()), parts.get("p1"));
        assertTrue(partBytes.get("p1").length > 0);
        assertEquals(List.of(BackendConfig.MODEL_TURBO), batches, "one batch for the part");
    }

    @Test
    void aPartThatIsUpToDateCostsNoBatchAtAll() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO));
        PartScript script = script("One.\n\nTwo.");
        finished(queue.submit("p1", "Part 1", () -> script));
        spoken.clear();
        batches.clear();

        ConversionJob.Snapshot again = finished(queue.submit("p1", "Part 1", () -> script));

        assertEquals(State.DONE, again.state());
        assertEquals("Up to date", again.message());
        assertTrue(spoken.isEmpty());
        assertTrue(batches.isEmpty(), "no swap of the language model for nothing");
    }

    @Test
    void aChangedParagraphIsTheOnlyOneMadeAgain() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO));
        finished(queue.submit("p1", "Part 1", () -> script("One.\n\nTwo.\n\nThree.")));
        spoken.clear();

        ConversionJob.Snapshot again = finished(queue.submit("p1", "Part 1", () -> script("One.\n\nTwo, changed.\n\nThree.")));

        assertEquals(State.DONE, again.state(), again.message());
        assertEquals(List.of("Two, changed."), spoken.stream().map(SpeechRequest::text).toList());
        assertEquals(3, again.total());
    }

    @Test
    void everyPartThatWaitsIsOneRunAndOneBatchPerModel() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO));
        holdSpeech = new CountDownLatch(1);
        ConversionJob first = queue.submit("p1", "Part 1", () -> script("Alpha."));
        assertTrue(speechStarted.await(5, TimeUnit.SECONDS));
        // while the first runs, two more come in: they wait and then share one batch
        ConversionJob second = queue.submit("p2", "Part 2", () -> script("Beta."));
        ConversionJob third = queue.submit("p3", "Part 3", () -> script("Gamma."));
        holdSpeech.countDown();
        finished(third);

        assertEquals(State.DONE, first.snapshot().state());
        assertEquals(State.DONE, second.snapshot().state());
        assertEquals(State.DONE, third.snapshot().state());
        assertEquals(2, batches.size(), "the first part alone, the other two together");
    }

    @Test
    void partsSubmittedTogetherAreOneRunEvenWhenTheWorkerIsQuick() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO));

        List<ConversionJob> jobs = queue.submitAll(List.of(
                new ConversionQueue.Task("p1", "Part 1", () -> script("Alpha.")),
                new ConversionQueue.Task("p2", "Part 2", () -> script("Beta.")),
                new ConversionQueue.Task("p3", "Part 3", () -> script("Gamma."))));
        finished(jobs.getLast());

        assertTrue(jobs.stream().allMatch(j -> j.snapshot().state() == State.DONE));
        assertEquals(1, batches.size(), "one swap for the three parts: " + batches);
    }

    @Test
    void partsWithPiecesInTwoModelsGetABatchForEachModel() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO, BackendConfig.MODEL_MULTILINGUAL));
        PartScript turbo = script("English line.");
        PartScript multilingual = script("日本語の行。", BackendConfig.MODEL_MULTILINGUAL);
        List<com.github.enerccio.marginalia.extensions.audiobook.script.ScriptEntry> both = new ArrayList<>(turbo.entries());
        both.add(multilingual.entries().getFirst());
        PartScript mixed = new PartScript(both, "mixed");

        ConversionJob.Snapshot done = finished(queue.submit("p1", "Part 1", () -> mixed));

        assertEquals(State.DONE, done.state(), done.message());
        assertEquals(List.of(BackendConfig.MODEL_TURBO, BackendConfig.MODEL_MULTILINGUAL), batches);
    }

    @Test
    void aModelWithoutABackendFailsTheJob() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO));

        ConversionJob.Snapshot done = finished(queue.submit("p1", "Part 1", () -> script("日本語。", BackendConfig.MODEL_MULTILINGUAL)));

        assertEquals(State.FAILED, done.state());
        assertTrue(done.message().contains("no backend for the model chatterbox-multilingual"), done.message());
        assertTrue(batches.isEmpty());
    }

    @Test
    void aPieceThatFailsFailsItsPartOnlyAndARetryKeepsTheRest() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO));
        failing.add("Bad.");

        ConversionJob good = queue.submit("p1", "Part 1", () -> script("Fine one.\n\nFine two."));
        ConversionJob bad = queue.submit("p2", "Part 2", () -> script("Fine start.\n\nBad.\n\nFine end."));
        finished(bad);

        assertEquals(State.DONE, good.snapshot().state());
        assertEquals(State.FAILED, bad.snapshot().state());
        assertTrue(bad.snapshot().message().contains("Bad."), bad.snapshot().message());
        assertNull(parts.get("p2"));

        failing.clear();
        spoken.clear();
        queue.retry(bad.id());
        ConversionJob.Snapshot retried = finished(bad);

        assertEquals(State.DONE, retried.state(), retried.message());
        // "Fine start." was made before the failure; the retry made what was missing
        assertFalse(spoken.stream().anyMatch(r -> r.text().equals("Fine start.")), spoken.toString());
        assertTrue(spoken.stream().anyMatch(r -> r.text().equals("Bad.")));
        assertNotNull(parts.get("p2"));
    }

    @Test
    void aFailedBatchFailsEveryJobItCarried() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO));
        beforeFails = "Request 1 of 2: cannot unload the language model";

        ConversionJob.Snapshot done = finished(queue.submit("p1", "Part 1", () -> script("Some text.")));

        assertEquals(State.FAILED, done.state());
        assertTrue(done.message().contains("cannot unload"), done.message());
    }

    @Test
    void aScriptThatCannotBeMadeOrIsEmptyFailsTheJob() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO));

        ConversionJob broken = queue.submit("p1", "Part 1", () -> {
            throw new IllegalStateException("the part is gone");
        });
        ConversionJob empty = queue.submit("p2", "Part 2", () -> script("   "));
        finished(empty);

        assertEquals(State.FAILED, broken.snapshot().state());
        assertTrue(broken.snapshot().message().contains("the part is gone"));
        assertEquals(State.FAILED, empty.snapshot().state());
        assertTrue(empty.snapshot().message().contains("no text"));
        assertTrue(batches.isEmpty());
    }

    @Test
    void aBatchWaitsWhileAGenerationRuns() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO));
        paused.set(true);

        ConversionJob job = queue.submit("p1", "Part 1", () -> script("Some text."));
        Thread.sleep(300);

        assertTrue(batches.isEmpty(), "the language model would be loaded under the speech server");
        assertEquals(State.RUNNING, job.snapshot().state());
        paused.set(false);
        assertEquals(State.DONE, finished(job).state());
        assertEquals(1, batches.size());
    }

    @Test
    void theUserCanStopARunThatIsSpeaking() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO));
        holdSpeech = new CountDownLatch(1);

        ConversionJob job = queue.submit("p1", "Part 1", () -> script("One.\n\nTwo.\n\nThree."));
        assertTrue(speechStarted.await(5, TimeUnit.SECONDS));
        queue.cancelAll();
        holdSpeech.countDown();
        ConversionJob.Snapshot done = finished(job);

        assertEquals(State.CANCELLED, done.state(), done.message());
        assertNull(parts.get("p1"));
    }

    @Test
    void aWaitingJobIsCancelledAtOnceAndRetried() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO));
        holdSpeech = new CountDownLatch(1);
        ConversionJob running = queue.submit("p1", "Part 1", () -> script("One."));
        assertTrue(speechStarted.await(5, TimeUnit.SECONDS));
        ConversionJob waiting = queue.submit("p2", "Part 2", () -> script("Two."));

        queue.cancel(waiting.id());

        assertEquals(State.CANCELLED, waiting.snapshot().state());
        holdSpeech.countDown();
        finished(running);
        assertEquals(State.DONE, running.snapshot().state());

        queue.retry(waiting.id());
        assertEquals(State.DONE, finished(waiting).state());
    }

    @Test
    void aPartWithAJobAlreadyWaitingGetsNoSecondOne() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO));
        holdSpeech = new CountDownLatch(1);

        ConversionJob first = queue.submit("p1", "Part 1", () -> script("One."));
        ConversionJob again = queue.submit("p1", "Part 1", () -> script("One."));
        holdSpeech.countDown();
        finished(first);

        assertSame(first, again);
        assertEquals(1, queue.jobs().size());
    }

    @Test
    void theClipOfAVoiceIsResolvedForTheRequest() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO));
        BackendConfig config = new BackendConfig();
        PartScript script = ScriptBuilder.narrated("Hello.", VoiceRef.clip("uuid-1"), config, 5);

        finished(queue.submit("p1", "Part 1", () -> script));

        assertEquals(VoiceMode.CLONE, spoken.getFirst().voiceMode());
        assertEquals("m-uuid-1.wav", spoken.getFirst().voice());
        assertEquals(5, spoken.getFirst().seed());
    }

    @Test
    void theStoreIsFlushedWhenAJobEndsAndForgetsPiecesOfAnOlderText() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO));
        finished(queue.submit("p1", "Part 1", () -> script("One.\n\nTwo.")));
        assertEquals(List.of("p1"), flushed);
        assertEquals(2, segments.keySet().stream().filter(k -> k.startsWith("p1/")).count());

        finished(queue.submit("p1", "Part 1", () -> script("One.\n\nTwo, changed.")));

        // the piece of the old second paragraph is gone, the first one stayed
        assertEquals(2, segments.keySet().stream().filter(k -> k.startsWith("p1/")).count());
        assertEquals(2, flushed.size());
    }

    @Test
    void aScriptOnlyJobMakesTheScriptAndSpeaksNothing() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO));

        ConversionJob job = queue.submitAll(List.of(new ConversionQueue.Task("p1", "Part 1", () -> script("One.\n\nTwo."), ConversionJob.Mode.SCRIPT_ONLY))).getFirst();
        ConversionJob.Snapshot done = finished(job);

        assertEquals(State.DONE, done.state(), done.message());
        assertEquals("2 segments", done.message());
        assertTrue(spoken.isEmpty());
        assertTrue(batches.isEmpty(), "no swap of the language model for a script");
        assertNull(parts.get("p1"));
    }

    @Test
    void aSegmentsOnlyJobSpeaksThePiecesButStoresNoPart() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO));
        PartScript one = script("Only this.");

        ConversionJob job = queue.submitAll(List.of(new ConversionQueue.Task("p1", "Hear", () -> one, ConversionJob.Mode.SEGMENTS_ONLY))).getFirst();
        ConversionJob.Snapshot done = finished(job);

        assertEquals(State.DONE, done.state(), done.message());
        assertEquals(1, spoken.size());
        assertNotNull(store.segment("p1", one.entries().getFirst().key()), "the audio of the piece is there to be heard");
        assertNull(parts.get("p1"), "no joined part");
        assertEquals(List.of("p1"), flushed);

        // hearing it again costs nothing
        spoken.clear();
        batches.clear();
        finished(queue.submitAll(List.of(new ConversionQueue.Task("p1", "Hear", () -> one, ConversionJob.Mode.SEGMENTS_ONLY))).getFirst());
        assertTrue(spoken.isEmpty());
        assertTrue(batches.isEmpty());
    }

    @Test
    void aSegmentsOnlyJobDoesNotTakeThePartForUpToDate() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO));
        PartScript whole = script("One.\n\nTwo.");
        finished(queue.submit("p1", "Part 1", () -> whole));
        spoken.clear();

        // a take of the first piece: another seed
        PartScript take = new PartScript(List.of(whole.entries().getFirst().withSeed(9)), whole.sourceHash());
        finished(queue.submitAll(List.of(new ConversionQueue.Task("p1", "Hear", () -> take, ConversionJob.Mode.SEGMENTS_ONLY))).getFirst());

        assertEquals(1, spoken.size());
        assertEquals(9, spoken.getFirst().seed());
    }

    @Test
    void theOtherTakesAreKeptWhenThePartIsJoined() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO));
        PartScript whole = script("One.");
        finished(queue.submit("p1", "Part 1", () -> whole));
        var first = whole.entries().getFirst();
        PartScript take = new PartScript(List.of(first.withSeed(9)), whole.sourceHash());
        finished(queue.submitAll(List.of(new ConversionQueue.Task("p1", "Hear", () -> take, ConversionJob.Mode.SEGMENTS_ONLY))).getFirst());

        // the part now uses the new take and asks to keep the first one
        PartScript both = new PartScript(List.of(first.withSeed(9)), whole.sourceHash(), Set.of(first.key()));
        finished(queue.submit("p1", "Part 1", () -> both));

        assertNotNull(store.segment("p1", first.key()), "the first take is still there");
        assertNotNull(store.segment("p1", first.withSeed(9).key()));
    }

    @Test
    void listenersSeeTheProgress() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO));
        List<ConversionJob.Snapshot> seen = new CopyOnWriteArrayList<>();
        queue.addListener(seen::add);

        finished(queue.submit("p1", "Part 1", () -> script("One.\n\nTwo.")));

        assertEquals(State.WAITING, seen.getFirst().state());
        assertEquals(State.DONE, seen.getLast().state());
        assertTrue(seen.stream().anyMatch(s -> s.state() == State.RUNNING && s.done() == 1 && s.total() == 2), seen.toString());
    }

    @Test
    void aBatchIsOverOnceWhenItsLastJobEnds() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO));
        failing.add("Two.");
        List<List<ConversionJob.Snapshot>> calls = new CopyOnWriteArrayList<>();

        List<ConversionJob> jobs = queue.submitAll(List.of(
                new ConversionQueue.Task("p1", "Part 1", () -> script("One.")),
                new ConversionQueue.Task("p2", "Part 2", () -> script("Two."))));
        com.github.enerccio.marginalia.extensions.audiobook.ui.BatchNotifier.whenOver(queue, jobs, calls::add);
        assertTrue(queue.awaitIdle(10, TimeUnit.SECONDS));
        Thread.sleep(50);

        assertEquals(1, calls.size(), "told once, when both are over");
        assertEquals(List.of(State.DONE, State.FAILED), calls.getFirst().stream().map(ConversionJob.Snapshot::state).toList());
    }

    @Test
    void aBatchThatIsOverAlreadyIsToldAtOnce() throws Exception {
        queue(Set.of(BackendConfig.MODEL_TURBO));
        List<ConversionJob> jobs = queue.submitAll(List.of(new ConversionQueue.Task("p1", "Part 1", () -> script("One."))));
        assertTrue(queue.awaitIdle(10, TimeUnit.SECONDS));
        List<List<ConversionJob.Snapshot>> calls = new CopyOnWriteArrayList<>();

        com.github.enerccio.marginalia.extensions.audiobook.ui.BatchNotifier.whenOver(queue, jobs, calls::add);

        assertEquals(1, calls.size());
        assertEquals(State.DONE, calls.getFirst().getFirst().state());
    }
}
