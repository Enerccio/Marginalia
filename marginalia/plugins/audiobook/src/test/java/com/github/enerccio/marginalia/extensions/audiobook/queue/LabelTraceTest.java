package com.github.enerccio.marginalia.extensions.audiobook.queue;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class LabelTraceTest {

    @Test
    void whatTheModelWritesGoesToTheWindowThatWasStartedLast() {
        LabelTrace trace = new LabelTrace();
        assertTrue(trace.isEmpty());

        trace.startWindow("pieces 1-16 of 40");
        trace.append(true, "think ");
        trace.append(true, "more");
        trace.append(false, "answer");
        trace.startWindow("pieces 17-32 of 40");
        trace.append(false, "next");

        List<LabelTrace.Window> windows = trace.windows();
        assertEquals(2, windows.size());
        assertEquals(new LabelTrace.Window("pieces 1-16 of 40", "think more", "answer"), windows.get(0));
        assertEquals(new LabelTrace.Window("pieces 17-32 of 40", "", "next"), windows.get(1));
        assertFalse(trace.isEmpty());
    }

    @Test
    void textBeforeAnyWindowGetsOneOfItsOwn() {
        LabelTrace trace = new LabelTrace();
        trace.append(false, "early");
        trace.append(false, null);
        trace.append(false, "");

        assertEquals(1, trace.windows().size());
        assertEquals("early", trace.windows().getFirst().output());
    }

    @Test
    void theOldestWindowsGoWhenTheTextIsTooMuchAndTheLastIsKept() {
        LabelTrace trace = new LabelTrace();
        String chunk = "x".repeat(LabelTrace.MAX_CHARS / 4);
        for (int i = 0; i < 6; i++) {
            trace.startWindow("w" + i);
            trace.append(false, chunk);
        }

        List<LabelTrace.Window> windows = trace.windows();
        assertTrue(windows.size() <= 4, "kept " + windows.size());
        assertEquals("w5", windows.getLast().title());
        assertEquals(chunk, windows.getLast().output(), "the window being written is never cut");
    }

    @Test
    void listenersAreToldOfAWindowAtOnceAndOfChunksNotMoreOftenThanFiveTimesASecond() {
        LabelTrace trace = new LabelTrace();
        AtomicInteger told = new AtomicInteger();
        Runnable listener = told::incrementAndGet;
        trace.addListener(listener);

        trace.startWindow("w");
        int afterStart = told.get();
        for (int i = 0; i < 100; i++) {
            trace.append(false, "c");
        }

        assertEquals(1, afterStart);
        assertTrue(told.get() <= 3, "told " + told.get());
        trace.removeListener(listener);
        int before = told.get();
        trace.startWindow("again");
        assertEquals(before, told.get());
    }

    @Test
    void aListenerThatFailsDoesNotStopTheLabelling() {
        LabelTrace trace = new LabelTrace();
        trace.addListener(() -> {
            throw new IllegalStateException("the window is gone");
        });

        assertDoesNotThrow(() -> {
            trace.startWindow("w");
            trace.append(false, "text");
        });
        assertEquals("text", trace.windows().getFirst().output());
    }
}
