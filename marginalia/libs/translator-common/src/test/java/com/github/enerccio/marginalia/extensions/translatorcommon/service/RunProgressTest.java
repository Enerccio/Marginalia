package com.github.enerccio.marginalia.extensions.translatorcommon.service;

import com.github.enerccio.marginalia.domain.service.CancellationToken;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The progress of a run: bound to the thread, forwards what the model sends, and a stop cancels the request that is going and refuses the next ones.
 */
class RunProgressTest {

    private static final class Recorder implements RunProgress.Listener {
        final List<String> events = new ArrayList<>();

        @Override
        public void requestStarted() {
            events.add("start");
        }

        @Override
        public void chunk(boolean reasoning, String text) {
            events.add((reasoning ? "think:" : "say:") + text);
        }
    }

    @Test
    void isFoundOnTheThreadItWasBoundToAndOnlyThere() throws Exception {
        RunProgress progress = new RunProgress(new Recorder());
        assertNull(RunProgress.current());
        try (RunProgress.Binding _ = progress.bind()) {
            assertSame(progress, RunProgress.current());
            Thread other = new Thread(() -> assertNull(RunProgress.current()));
            other.start();
            other.join();
        }
        assertNull(RunProgress.current());
    }

    @Test
    void aBindingInsideABindingGivesBackTheOuterOne() {
        RunProgress outer = new RunProgress(new Recorder());
        RunProgress inner = new RunProgress(new Recorder());
        try (RunProgress.Binding _ = outer.bind()) {
            try (RunProgress.Binding _ = inner.bind()) {
                assertSame(inner, RunProgress.current());
            }
            assertSame(outer, RunProgress.current());
        }
        assertNull(RunProgress.current());
    }

    @Test
    void forwardsTheChunksAndSkipsEmptyOnes() {
        Recorder recorder = new Recorder();
        RunProgress progress = new RunProgress(recorder);
        progress.requestStarted();
        progress.chunk(true, "hm");
        progress.chunk(false, "");
        progress.chunk(false, "{}");
        assertEquals(List.of("start", "think:hm", "say:{}"), recorder.events);
    }

    @Test
    void aStopCancelsTheRequestThatIsGoing() {
        RunProgress progress = new RunProgress(new Recorder());
        progress.requestStarted();
        CancellationToken token = new CancellationToken();
        progress.attach(token);
        assertFalse(token.isCancelled());
        progress.stop();
        assertTrue(token.isCancelled());
        assertTrue(progress.isStopped());
    }

    @Test
    void aStopBeforeTheRequestIsAttachedCancelsItAtOnce() {
        RunProgress progress = new RunProgress(new Recorder());
        progress.requestStarted();
        progress.stop();
        CancellationToken token = new CancellationToken();
        progress.attach(token);
        assertTrue(token.isCancelled());
    }

    @Test
    void aStoppedRunStartsNoMoreRequests() {
        Recorder recorder = new Recorder();
        RunProgress progress = new RunProgress(recorder);
        progress.stop();
        assertThrows(CancellationException.class, progress::requestStarted);
        assertThrows(CancellationException.class, progress::checkNotStopped);
        assertTrue(recorder.events.isEmpty());
    }

    @Test
    void aStopCanComeMoreThanOnce() {
        RunProgress progress = new RunProgress(new Recorder());
        progress.stop();
        assertDoesNotThrow(progress::stop);
    }

    @Test
    void aStopDoesNotCancelTheNextRequestsTokenBeforeItIsAttached() {
        RunProgress progress = new RunProgress(new Recorder());
        progress.requestStarted();
        CancellationToken first = new CancellationToken();
        progress.attach(first);
        // the next request of the same run: the old token is not the one a stop reaches any more
        progress.requestStarted();
        progress.stop();
        assertFalse(first.isCancelled());
    }
}
