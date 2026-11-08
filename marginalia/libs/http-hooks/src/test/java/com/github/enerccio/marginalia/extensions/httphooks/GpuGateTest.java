package com.github.enerccio.marginalia.extensions.httphooks;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class GpuGateTest {

    private final List<String> events = new CopyOnWriteArrayList<>();
    private final GpuGate.Step before = () -> events.add("before");
    private final GpuGate.Step after = () -> events.add("after");

    @Test
    void theFirstOneLoadsAndTheLastOneFrees() throws Exception {
        GpuGate gate = new GpuGate();

        gate.acquire(before);
        gate.acquire(before);
        assertEquals(2, gate.holders());
        assertNull(gate.release(after));
        assertEquals(List.of("before"), events);
        assertNull(gate.release(after));

        assertEquals(List.of("before", "after"), events);
        assertEquals(0, gate.holders());
    }

    @Test
    void aFailedBeforeHookHoldsNothing() {
        GpuGate gate = new GpuGate();

        assertThrows(BackendException.class, () -> gate.acquire(() -> {
            throw new BackendException("no gpu");
        }));

        assertEquals(0, gate.holders());
        assertNull(gate.release(after));
        assertTrue(events.isEmpty());
    }

    @Test
    void aFailedAfterHookIsReportedAndTheGpuIsFreeAnyway() throws Exception {
        GpuGate gate = new GpuGate();
        gate.acquire(before);

        String error = gate.release(() -> {
            throw new BackendException("hook down");
        });

        assertEquals("hook down", error);
        gate.acquire(before);
        assertEquals(List.of("before", "before"), events);
    }

    @Test
    void releasingWithoutHoldingDoesNothing() {
        assertNull(new GpuGate().release(after));
        assertTrue(events.isEmpty());
    }

    @Test
    void theSameServerHasTheSameGate() {
        assertSame(GpuGate.forAddress("http://gate-test.lan:8188/prompt"), GpuGate.forAddress("HTTP://Gate-Test.LAN:8188/other"));
        assertNotSame(GpuGate.forAddress("http://gate-test.lan:8188"), GpuGate.forAddress("http://gate-test.lan:8189"));
    }

    @Test
    void oneAfterAnotherNotTogether() throws Exception {
        GpuGate gate = new GpuGate();
        AtomicInteger inside = new AtomicInteger();
        AtomicInteger most = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Future<Integer>> results = new java.util.ArrayList<>();
            for (int i = 0; i < 4; i++) {
                int n = i;
                results.add(pool.submit(() -> gate.exclusive(null, () -> {
                    most.accumulateAndGet(inside.incrementAndGet(), Math::max);
                    pause(30);
                    inside.decrementAndGet();
                    return n;
                })));
            }
            for (Future<Integer> result : results) {
                result.get(10, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }
        assertEquals(1, most.get());
    }

    @Test
    void aWaitingJobCanBeCancelled() throws Exception {
        GpuGate gate = new GpuGate();
        CountDownLatch running = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread holder = Thread.ofVirtual().start(() -> {
            try {
                gate.exclusive(null, () -> {
                    running.countDown();
                    try {
                        release.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return null;
                });
            } catch (Exception ignored) {
                // the test ends it
            }
        });
        assertTrue(running.await(5, TimeUnit.SECONDS));
        AtomicBoolean cancelled = new AtomicBoolean();
        Timer.after(300, () -> cancelled.set(true));

        var e = assertThrows(BackendException.class, () -> gate.exclusive(cancelled::get, () -> "never"));

        assertEquals("Cancelled", e.getMessage());
        release.countDown();
        holder.join(5000);
    }

    private static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /** A flag that is set after a while, without a scheduler of the test. */
    private static final class Timer {
        static void after(long millis, Runnable action) {
            Thread.ofVirtual().start(() -> {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    return;
                }
                action.run();
            });
        }
    }
}
