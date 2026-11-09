package com.github.enerccio.marginalia.extensions.imagegenerator.service;

import com.github.enerccio.marginalia.extensions.httphooks.BackendException;
import com.github.enerccio.marginalia.extensions.httphooks.GpuGate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ImageSessionTest {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final List<String> events = new CopyOnWriteArrayList<>();

    @AfterEach
    void stop() {
        scheduler.shutdownNow();
    }

    private ImageBackend backend() {
        return new ImageBackend() {
            @Override
            public byte[] generate(ImageRequest request, Cancel cancel) {
                events.add("image");
                return new byte[]{1};
            }

            @Override
            public void test() {
            }
        };
    }

    private ImageSession session(int idleSeconds, java.util.function.Consumer<String> onIdle) {
        return new ImageSession(backend(), () -> events.add("before"), () -> events.add("after"), idleSeconds, scheduler, onIdle);
    }

    private static final ImageRequest REQUEST = new ImageRequest("p", "", 1, 64, 64);

    @Test
    void beforeRunsOnceAndAfterRunsOnEnd() throws Exception {
        ImageSession session = session(0, null);

        session.generate(REQUEST, () -> false);
        session.generate(REQUEST, () -> false);
        session.generate(REQUEST, () -> false);
        assertTrue(session.isLeased());
        assertNull(session.end());

        assertEquals(List.of("before", "image", "image", "image", "after"), events);
        assertFalse(session.isLeased());
    }

    @Test
    void endingTwiceRunsAfterOnce() throws Exception {
        ImageSession session = session(0, null);
        session.generate(REQUEST, () -> false);

        session.end();
        session.end();

        assertEquals(List.of("before", "image", "after"), events);
    }

    @Test
    void endingWithoutAnImageDoesNothing() {
        ImageSession session = session(0, null);

        assertNull(session.end());

        assertEquals(List.of(), events);
    }

    @Test
    void aFailingBeforeStopsTheImageAndTheNextCallTriesAgain() {
        int[] calls = {0};
        ImageSession session = new ImageSession(backend(), () -> {
            if (calls[0]++ == 0) {
                throw new BackendException("no gpu");
            }
        }, () -> events.add("after"), 0, scheduler, null);

        var e = assertThrows(BackendException.class, () -> session.generate(REQUEST, () -> false));
        assertEquals("no gpu", e.getMessage());
        assertFalse(session.isLeased());
        assertEquals(List.of(), events);

        assertDoesNotThrow(() -> session.generate(REQUEST, () -> false));
        assertEquals(List.of("image"), events);
    }

    @Test
    void aFailingAfterIsReportedNotThrown() throws Exception {
        ImageSession session = new ImageSession(backend(), () -> {
        }, () -> {
            throw new BackendException("hook down");
        }, 0, scheduler, null);
        session.generate(REQUEST, () -> false);

        assertEquals("hook down", session.end());
        assertFalse(session.isLeased());
    }

    @Test
    void theIdleTimeEndsTheSession() throws Exception {
        CountDownLatch ended = new CountDownLatch(1);
        AtomicReference<String> error = new AtomicReference<>("unset");
        ImageSession session = session(1, e -> {
            error.set(e);
            ended.countDown();
        });

        session.generate(REQUEST, () -> false);

        assertTrue(ended.await(5, TimeUnit.SECONDS));
        assertNull(error.get());
        assertEquals(List.of("before", "image", "after"), events);
        assertFalse(session.isLeased());
    }

    @Test
    void anImageRestartsTheIdleTime() throws Exception {
        CountDownLatch ended = new CountDownLatch(1);
        ImageSession session = session(2, e -> ended.countDown());

        session.generate(REQUEST, () -> false);
        Thread.sleep(1200);
        session.generate(REQUEST, () -> false);
        Thread.sleep(1200);

        // 2.4 s after the first image but only 1.2 s after the second: still held
        assertTrue(session.isLeased());
        assertTrue(ended.await(5, TimeUnit.SECONDS));
    }

    @Test
    void endingWhileAnImageIsBeingMadeWaitsForTheImage() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ImageBackend slow = new ImageBackend() {
            @Override
            public byte[] generate(ImageRequest request, Cancel cancel) {
                started.countDown();
                try {
                    release.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException ignored) {
                }
                events.add("image");
                return new byte[]{1};
            }

            @Override
            public void test() {
            }
        };
        ImageSession session = new ImageSession(slow, () -> events.add("before"), () -> events.add("after"), 0, scheduler, null);
        Thread worker = new Thread(() -> {
            try {
                session.generate(REQUEST, () -> false);
            } catch (BackendException ignored) {
            }
        });
        worker.start();
        assertTrue(started.await(5, TimeUnit.SECONDS));

        assertNull(session.end());
        assertEquals(List.of("before"), events);

        release.countDown();
        worker.join(5000);

        assertEquals(List.of("before", "image", "after"), events);
    }

    @Test
    void sessionsOnOneGpuShareTheHooks() throws Exception {
        GpuGate gate = new GpuGate();
        ImageSession first = new ImageSession(backend(), () -> events.add("before"), () -> events.add("after"), 0, scheduler, null, gate);
        ImageSession second = new ImageSession(backend(), () -> events.add("before"), () -> events.add("after"), 0, scheduler, null, gate);

        first.generate(REQUEST, () -> false);
        second.generate(REQUEST, () -> false);
        assertNull(first.end());

        assertEquals(List.of("before", "image", "image"), events);
        assertNull(second.end());
        assertEquals(List.of("before", "image", "image", "after"), events);
    }

    @Test
    void aSessionWithAnotherModeWaitsUntilTheFirstEndsAndLoadsItsOwn() throws Exception {
        GpuGate gate = new GpuGate();
        ImageSession first = new ImageSession(backend(), () -> events.add("before a"), () -> events.add("after a"), 0, scheduler, null, gate, "a");
        ImageSession second = new ImageSession(backend(), () -> events.add("before b"), () -> events.add("after b"), 0, scheduler, null, gate, "b");
        first.generate(REQUEST, () -> false);

        java.util.concurrent.CompletableFuture<byte[]> other = java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            try {
                return second.generate(REQUEST, () -> false);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        Thread.sleep(400);
        assertFalse(other.isDone());
        // the session that waits does not block asking about it or ending it
        assertFalse(second.isLeased());
        assertNull(second.end());
        assertEquals(List.of("before a", "image"), events);

        assertNull(first.end());
        other.get(5, java.util.concurrent.TimeUnit.SECONDS);

        // the end that was asked for while it waited happens when its image is done
        assertEquals(List.of("before a", "image", "after a", "before b", "image", "after b"), events);
        assertFalse(second.isLeased());
    }

    @Test
    void aSessionThatIsCancelledWhileItWaitsForTheGpuHoldsNothing() throws Exception {
        GpuGate gate = new GpuGate();
        ImageSession first = new ImageSession(backend(), () -> events.add("before a"), () -> events.add("after a"), 0, scheduler, null, gate, "a");
        ImageSession second = new ImageSession(backend(), () -> events.add("before b"), () -> events.add("after b"), 0, scheduler, null, gate, "b");
        first.generate(REQUEST, () -> false);
        java.util.concurrent.atomic.AtomicBoolean cancelled = new java.util.concurrent.atomic.AtomicBoolean();

        java.util.concurrent.CompletableFuture<Object> other = java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            try {
                return second.generate(REQUEST, cancelled::get);
            } catch (Exception e) {
                return e;
            }
        });
        Thread.sleep(300);
        cancelled.set(true);

        assertInstanceOf(com.github.enerccio.marginalia.extensions.httphooks.BackendException.class, other.get(5, java.util.concurrent.TimeUnit.SECONDS));
        assertFalse(second.isLeased());
        assertEquals(1, gate.holders());
        assertNull(first.end());
        assertEquals(List.of("before a", "image", "after a"), events);
    }

    @Test
    void imagesOfSessionsOnOneGpuAreMadeOneAtATime() throws Exception {
        GpuGate gate = new GpuGate();
        java.util.concurrent.atomic.AtomicInteger inside = new java.util.concurrent.atomic.AtomicInteger();
        java.util.concurrent.atomic.AtomicInteger most = new java.util.concurrent.atomic.AtomicInteger();
        ImageBackend slow = new ImageBackend() {
            @Override
            public byte[] generate(ImageRequest request, Cancel cancel) {
                most.accumulateAndGet(inside.incrementAndGet(), Math::max);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                inside.decrementAndGet();
                return new byte[]{1};
            }

            @Override
            public void test() {
            }
        };
        List<Thread> workers = new java.util.ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ImageSession session = new ImageSession(slow, () -> { }, () -> { }, 0, scheduler, null, gate);
            Thread worker = new Thread(() -> {
                try {
                    session.generate(REQUEST, () -> false);
                } catch (BackendException ignored) {
                }
            });
            workers.add(worker);
            worker.start();
        }
        for (Thread worker : workers) {
            worker.join(5000);
        }

        assertEquals(1, most.get());
    }
}
