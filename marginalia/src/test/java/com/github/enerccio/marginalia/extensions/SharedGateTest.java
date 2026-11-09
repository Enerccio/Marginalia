package com.github.enerccio.marginalia.extensions;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SharedGateTest {

    private final List<String> events = new CopyOnWriteArrayList<>();

    private SharedGate.Step before(String name) {
        return () -> events.add("before " + name);
    }

    private SharedGate.Step after(String name) {
        return () -> events.add("after " + name);
    }

    @Test
    void theSameKeyIsTheSameGateWhateverTheCase() {
        assertThat(SharedGate.forKey("Gpu-One ")).isSameAs(SharedGate.forKey("gpu-one"));
        assertThat(SharedGate.forKey("gpu-one")).isNotSameAs(SharedGate.forKey("gpu-two"));
    }

    @Test
    void holdersOfOneModeShareTheLoadAndTheLastOneFrees() throws Exception {
        SharedGate gate = new SharedGate();

        gate.acquire("speech", null, before("a"));
        gate.acquire("speech", null, before("b"));
        assertThat(gate.holders()).isEqualTo(2);
        assertThat(gate.mode()).isEqualTo("speech");
        assertThat(gate.release(after("a"))).isNull();
        assertThat(events).containsExactly("before a");
        assertThat(gate.release(after("b"))).isNull();

        assertThat(events).containsExactly("before a", "after b");
        assertThat(gate.holders()).isZero();
        assertThat(gate.mode()).isNull();
    }

    @Test
    void aFailedBeforeStepHoldsNothingAndTheErrorIsTheCallers() {
        SharedGate gate = new SharedGate();

        assertThatThrownBy(() -> gate.acquire("m", null, () -> {
            throw new IllegalStateException("no gpu");
        })).isInstanceOf(IllegalStateException.class).hasMessage("no gpu");

        assertThat(gate.holders()).isZero();
        assertThat(gate.release(after("x"))).isNull();
        assertThat(events).isEmpty();
    }

    @Test
    void aFailedAfterStepIsReportedAndTheGpuIsFreeAnyway() throws Exception {
        SharedGate gate = new SharedGate();
        gate.acquire("m", null, before("a"));

        String error = gate.release(() -> {
            throw new Exception("hook down");
        });

        assertThat(error).isEqualTo("hook down");
        gate.acquire("m", null, before("b"));
        assertThat(events).containsExactly("before a", "before b");
    }

    @Test
    void anotherModeWaitsUntilTheHoldersAreGoneThenLoadsItsOwn() throws Exception {
        SharedGate gate = new SharedGate();
        gate.acquire("speech", null, before("speech"));
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            CountDownLatch started = new CountDownLatch(1);
            Future<?> image = pool.submit(() -> {
                started.countDown();
                gate.acquire("image", null, before("image"));
                return null;
            });
            started.await();
            Thread.sleep(400);
            assertThat(image.isDone()).isFalse();
            assertThat(events).containsExactly("before speech");

            gate.release(after("speech"));
            image.get(5, TimeUnit.SECONDS);

            assertThat(events).containsExactly("before speech", "after speech", "before image");
            assertThat(gate.mode()).isEqualTo("image");
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void somebodyWhoComesAfterAWaiterOfAnotherModeDoesNotJoinTheHolders() throws Exception {
        SharedGate gate = new SharedGate();
        gate.acquire("speech", null, before("one"));
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<?> image = pool.submit(() -> {
                gate.acquire("image", null, before("image"));
                return null;
            });
            Thread.sleep(300);
            Future<?> speech = pool.submit(() -> {
                gate.acquire("speech", null, before("two"));
                return null;
            });
            Thread.sleep(400);
            // the second speech request would have joined the first before the fairness rule
            assertThat(speech.isDone()).isFalse();
            assertThat(image.isDone()).isFalse();

            gate.release(after("speech"));
            image.get(5, TimeUnit.SECONDS);
            assertThat(speech.isDone()).isFalse();
            gate.release(after("image"));
            speech.get(5, TimeUnit.SECONDS);

            assertThat(events).containsExactly("before one", "after speech", "before image", "after image", "before two");
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void aWaitCanBeCancelled() throws Exception {
        SharedGate gate = new SharedGate();
        gate.acquire("speech", null, null);
        AtomicBoolean cancelled = new AtomicBoolean();
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<?> waiter = pool.submit(() -> {
                gate.acquire("image", cancelled::get, before("image"));
                return null;
            });
            Thread.sleep(300);
            cancelled.set(true);

            assertThatThrownBy(() -> waiter.get(5, TimeUnit.SECONDS)).hasCauseInstanceOf(SharedGate.Cancelled.class);
            // the one who gave up leaves the line: the holder can go and the next one is not stuck behind a ghost
            gate.release(null);
            gate.acquire("image", null, before("later"));
            assertThat(events).containsExactly("before later");
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void workRunsOneAtATime() throws Exception {
        SharedGate gate = new SharedGate();
        java.util.concurrent.atomic.AtomicInteger inside = new java.util.concurrent.atomic.AtomicInteger();
        java.util.concurrent.atomic.AtomicInteger most = new java.util.concurrent.atomic.AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Future<Integer>> results = new java.util.ArrayList<>();
            for (int i = 0; i < 4; i++) {
                results.add(pool.submit(() -> gate.exclusive(null, () -> {
                    most.accumulateAndGet(inside.incrementAndGet(), Math::max);
                    Thread.sleep(30);
                    inside.decrementAndGet();
                    return 1;
                })));
            }
            for (Future<Integer> result : results) {
                result.get(10, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }
        assertThat(most.get()).isEqualTo(1);
    }
}
