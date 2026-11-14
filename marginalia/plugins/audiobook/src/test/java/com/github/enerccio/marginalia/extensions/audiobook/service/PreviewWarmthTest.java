package com.github.enerccio.marginalia.extensions.audiobook.service;

import com.github.enerccio.marginalia.extensions.audiobook.label.Delivery;
import com.github.enerccio.marginalia.extensions.audiobook.model.BackendConfig;
import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.extensions.httphooks.GpuGate;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class PreviewWarmthTest {

    private final List<String> events = new ArrayList<>();
    private final AtomicLong now = new AtomicLong(1000);
    private final PreviewWarmth warmth = new PreviewWarmth(c -> () -> events.add("before " + c.getName()), c -> () -> events.add("after " + c.getName()),
            60_000, now::get);

    private static final SpeechBackend STUB = new SpeechBackend() {
        public void prepare() {
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

    private static BackendConfig backend(String name, String gpu) {
        BackendConfig config = new BackendConfig();
        config.setName(name);
        config.setBaseUrl("http://speech.example:8004");
        config.setGpu(gpu);
        return config;
    }

    /** A sample as the service makes it: a batch on the gate of the backend, with the requests around it. */
    private void sample(BackendConfig config) throws BackendException {
        warmth.ensure(config);
        GpuGate gate = config.gate();
        SpeechBatch.run(STUB, gate, config.gateMode(), () -> events.add("before " + config.getName()), () -> events.add("after " + config.getName()),
                null, b -> {
                    events.add("sample");
                    return null;
                });
    }

    @Test
    void whileAWindowIsOpenTheRequestsRunOnceForAllSamples() throws Exception {
        BackendConfig higgs = backend("higgs", "gpu-" + UUID.randomUUID());

        warmth.open();
        sample(higgs);
        sample(higgs);
        sample(higgs);
        warmth.close();

        assertEquals(List.of("before higgs", "sample", "sample", "sample", "after higgs"), events);
        assertFalse(warmth.holding());
    }

    @Test
    void withoutAWindowEverySampleIsABatchOfItsOwn() throws Exception {
        BackendConfig higgs = backend("higgs", "gpu-" + UUID.randomUUID());

        sample(higgs);
        sample(higgs);

        assertEquals(List.of("before higgs", "sample", "after higgs", "before higgs", "sample", "after higgs"), events);
    }

    @Test
    void anotherBackendGivesTheFirstOneBack() throws Exception {
        String gpu = "gpu-" + UUID.randomUUID();
        BackendConfig higgs = backend("higgs", gpu);
        BackendConfig turbo = backend("turbo", gpu);
        turbo.setModel(BackendConfig.MODEL_TURBO);
        higgs.setModel(BackendConfig.MODEL_HIGGS);

        warmth.open();
        sample(higgs);
        sample(turbo);
        warmth.close();

        assertEquals(List.of("before higgs", "sample", "after higgs", "before turbo", "sample", "after turbo"), events);
    }

    @Test
    void theServerIsKeptUntilTheLastWindowCloses() throws Exception {
        BackendConfig higgs = backend("higgs", "gpu-" + UUID.randomUUID());

        warmth.open();
        warmth.open();
        sample(higgs);
        warmth.close();
        assertTrue(warmth.holding());
        warmth.close();

        assertEquals(List.of("before higgs", "sample", "after higgs"), events);
    }

    @Test
    void aWindowLeftOpenGivesTheServerBackWhenNoSampleCame() throws Exception {
        BackendConfig higgs = backend("higgs", "gpu-" + UUID.randomUUID());

        warmth.open();
        sample(higgs);
        now.addAndGet(30_000);
        assertFalse(warmth.releaseIfIdle());
        now.addAndGet(31_000);
        assertTrue(warmth.releaseIfIdle());
        assertEquals(List.of("before higgs", "sample", "after higgs"), events);

        // the window is still open: the next sample starts the server again and keeps it
        sample(higgs);
        warmth.close();
        assertEquals(List.of("before higgs", "sample", "after higgs", "before higgs", "sample", "after higgs"), events);
    }

    @Test
    void theSampleSaysWhatTheModelCanVoice() {
        assertEquals(AudiobookService.SAMPLE, AudiobookService.sampleText(BackendConfig.MODEL_TURBO, null, false, null));
        assertTrue(AudiobookService.sampleText(BackendConfig.MODEL_TURBO, null, true, null).contains("[laugh]"));
        String original = AudiobookService.sampleText(BackendConfig.MODEL_ORIGINAL, null, true, null);
        assertFalse(original.contains("["), original);
        assertTrue(original.startsWith("What a long day"), original);
        String higgs = AudiobookService.sampleText(BackendConfig.MODEL_HIGGS, null, true, Delivery.SAD);
        assertTrue(higgs.startsWith("<|emotion:sadness|>"), higgs);
        assertTrue(higgs.contains("<|sfx:laughter|>"), higgs);
        assertFalse(higgs.contains("[laugh]"), higgs);
        assertFalse(AudiobookService.sampleText(BackendConfig.MODEL_HIGGS, null, false, null).contains("<|"));
        assertEquals("こんにちは、この本の声です。こんなふうに話します。", AudiobookService.sampleText(BackendConfig.MODEL_MULTILINGUAL, "ja", true, null));
    }
}
