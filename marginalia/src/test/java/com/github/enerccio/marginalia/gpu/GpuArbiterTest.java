package com.github.enerccio.marginalia.gpu;

import com.github.enerccio.marginalia.domain.service.gpu.GpuArbiter;
import com.github.enerccio.marginalia.domain.service.gpu.GpuArbiter.LockDef;
import com.github.enerccio.marginalia.domain.service.gpu.GpuException;
import com.github.enerccio.marginalia.domain.service.gpu.GpuLease;
import com.github.enerccio.marginalia.domain.service.gpu.GpuStepRunner.Step;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The turns on a GPU without a database or a server: the steps are names written into a log.
 */
class GpuArbiterTest {

    private final List<String> log = new CopyOnWriteArrayList<>();
    private final Map<String, LockDef> locks = new ConcurrentHashMap<>();
    private final Set<String> failing = ConcurrentHashMap.newKeySet();
    // a step whose name is here waits for its latch
    private final Map<String, CountDownLatch> blocking = new ConcurrentHashMap<>();

    private final GpuArbiter.StepRunner runner = new GpuArbiter.StepRunner() {
        @Override
        public void runAll(List<Step> steps) throws GpuException {
            for (Step step : steps) {
                run(step);
                if (failing.contains(step.name())) {
                    throw new GpuException(step.name() + " failed");
                }
            }
        }

        @Override
        public String runAllRegardless(List<Step> steps) {
            String first = null;
            for (Step step : steps) {
                run(step);
                if (failing.contains(step.name()) && first == null) {
                    first = step.name() + " failed";
                }
            }
            return first;
        }

        private void run(Step step) {
            CountDownLatch latch = blocking.get(step.name());
            if (latch != null) {
                try {
                    latch.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            log.add(step.name());
        }
    };

    private final GpuArbiter arbiter = new GpuArbiter(uuid -> uuid == null ? null : locks.get(uuid), runner);

    private static Step step(String name) {
        return new Step(name, "http://localhost/", "POST", null, null, 1, 0);
    }

    private void lock(String uuid, String gpu, int max) {
        locks.put(uuid, new LockDef(uuid, uuid, gpu, max, List.of(step("load " + uuid)), List.of(step("free " + uuid))));
    }

    private static GpuLease get(CompletableFuture<GpuLease> future) throws Exception {
        return future.get(10, TimeUnit.SECONDS);
    }

    @Test
    void noLockOrAnUnknownLockNeedsNoGpu() throws Exception {
        assertThat(arbiter.acquireAsync(null, null).getNow(null)).isSameAs(GpuLease.NONE);
        assertThat(arbiter.acquireAsync("", null).getNow(null)).isSameAs(GpuLease.NONE);
        assertThat(arbiter.acquireAsync("gone", null).getNow(null)).isSameAs(GpuLease.NONE);
        assertThat(log).isEmpty();
    }

    @Test
    void theFirstJobLoadsTheLockAndTheNextOnesOfTheSameLockRunNoStep() throws Exception {
        lock("llm", "gpu", 0);
        GpuLease first = get(arbiter.acquireAsync("llm", null));
        first.close();
        GpuLease second = get(arbiter.acquireAsync("llm", null));
        GpuLease third = get(arbiter.acquireAsync("llm", null));
        second.close();
        third.close();
        third.close();

        assertThat(log).containsExactly("load llm");
        assertThat(arbiter.loadedLock("gpu")).as("sticky: still loaded after the jobs").isEqualTo("llm");
        assertThat(arbiter.status("gpu").active()).isZero();
    }

    private void outsideLock(String uuid, String gpu) {
        locks.put(uuid, new LockDef(uuid, uuid, gpu, 0, List.of(step("load " + uuid)), List.of(step("free " + uuid)), true));
    }

    @Test
    void onAGpuUsedOutsideTheAcquireStepsRunBeforeEveryJob() throws Exception {
        outsideLock("comfy", "gpu");
        get(arbiter.acquireAsync("comfy", null)).close();
        get(arbiter.acquireAsync("comfy", null)).close();
        GpuLease third = get(arbiter.acquireAsync("comfy", null));
        third.close();

        assertThat(log).containsExactly("load comfy", "load comfy", "load comfy");
        assertThat(arbiter.loadedLock("gpu")).isEqualTo("comfy");
        assertThat(arbiter.status("gpu").active()).isZero();
    }

    @Test
    void onAGpuUsedOutsideAFailedRecheckFailsTheJobAndTheNextOneLoadsAgain() throws Exception {
        outsideLock("comfy", "gpu");
        lock("llm", "gpu", 0);
        get(arbiter.acquireAsync("comfy", null)).close();
        failing.add("load comfy");

        assertThatThrownBy(() -> get(arbiter.acquireAsync("comfy", null))).hasCauseInstanceOf(GpuException.class);
        assertThat(arbiter.loadedLock("gpu")).as("not trusted any more").isNull();
        assertThat(arbiter.status("gpu").active()).isZero();

        failing.clear();
        get(arbiter.acquireAsync("llm", null)).close();
        assertThat(log).as("from an unknown GPU nothing is released").containsExactly("load comfy", "load comfy", "load llm");
    }

    @Test
    void anotherLockWaitsForTheJobsThenReleasesAndAcquiresInOrder() throws Exception {
        lock("llm", "gpu", 0);
        lock("image", "gpu", 1);
        GpuLease llm = get(arbiter.acquireAsync("llm", null));
        List<Integer> positions = new CopyOnWriteArrayList<>();
        CompletableFuture<GpuLease> image = arbiter.acquireAsync("image", positions::add);
        Thread.sleep(200);
        assertThat(image).as("the language model still works").isNotDone();
        assertThat(positions).containsExactly(1);

        llm.close();
        GpuLease held = get(image);
        assertThat(log).containsExactly("load llm", "free llm", "load image");
        assertThat(arbiter.loadedLock("gpu")).isEqualTo("image");
        held.close();
    }

    @Test
    void jobsOfTheLoadedLockQueueBehindAJobThatWaitsForASwitch() throws Exception {
        lock("llm", "gpu", 0);
        lock("image", "gpu", 0);
        GpuLease llm = get(arbiter.acquireAsync("llm", null));
        CompletableFuture<GpuLease> image = arbiter.acquireAsync("image", null);
        CompletableFuture<GpuLease> anotherLlm = arbiter.acquireAsync("llm", null);
        Thread.sleep(200);
        assertThat(anotherLlm).as("it would starve the image").isNotDone();

        llm.close();
        GpuLease imageLease = get(image);
        assertThat(anotherLlm).isNotDone();
        imageLease.close();
        get(anotherLlm).close();
        assertThat(log).containsExactly("load llm", "free llm", "load image", "free image", "load llm");
    }

    @Test
    void theMaximumOfALockIsKept() throws Exception {
        lock("image", "gpu", 1);
        GpuLease first = get(arbiter.acquireAsync("image", null));
        CompletableFuture<GpuLease> second = arbiter.acquireAsync("image", null);
        Thread.sleep(200);
        assertThat(second).isNotDone();
        first.close();
        get(second).close();
        assertThat(log).containsExactly("load image");
    }

    @Test
    void locksOfDifferentGpusDoNotWaitForEachOther() throws Exception {
        lock("a", "gpu1", 0);
        lock("b", "gpu2", 0);
        GpuLease a = get(arbiter.acquireAsync("a", null));
        GpuLease b = get(arbiter.acquireAsync("b", null));
        a.close();
        b.close();
        assertThat(log).containsExactlyInAnyOrder("load a", "load b");
    }

    @Test
    void aJobCancelledWhileItWaitsLeavesTheQueue() throws Exception {
        lock("llm", "gpu", 0);
        lock("image", "gpu", 0);
        GpuLease llm = get(arbiter.acquireAsync("llm", null));
        CompletableFuture<GpuLease> image = arbiter.acquireAsync("image", null);
        CompletableFuture<GpuLease> llm2 = arbiter.acquireAsync("llm", null);
        Thread.sleep(100);
        image.cancel(true);
        assertThat(get(llm2)).as("nobody waits for a switch any more").isNotNull();
        llm.close();
        assertThat(log).containsExactly("load llm");
    }

    @Test
    void theBlockingAcquireEndsWhenCancelled() throws Exception {
        lock("llm", "gpu", 0);
        lock("image", "gpu", 0);
        GpuLease llm = get(arbiter.acquireAsync("llm", null));
        AtomicBoolean cancelled = new AtomicBoolean();
        CompletableFuture<Throwable> result = CompletableFuture.supplyAsync(() -> {
            try {
                arbiter.acquire("image", cancelled::get);
                return null;
            } catch (Throwable e) {
                return e;
            }
        });
        Thread.sleep(300);
        cancelled.set(true);
        Throwable error = result.get(10, TimeUnit.SECONDS);
        assertThat(error).isInstanceOf(GpuException.class);
        assertThat(((GpuException) error).isCancelled()).isTrue();
        assertThat(arbiter.status("gpu").waiting()).isZero();
        llm.close();
    }

    @Test
    void aFailedAcquireStepFailsTheJobAndLeavesTheGpuUnknown() throws Exception {
        lock("image", "gpu", 0);
        failing.add("load image");
        assertThatThrownBy(() -> arbiter.acquire("image", null)).isInstanceOf(GpuException.class).hasMessageContaining("load image failed");
        assertThat(arbiter.loadedLock("gpu")).isNull();

        failing.clear();
        arbiter.acquire("image", null).close();
        assertThat(log).containsExactly("load image", "load image");
    }

    @Test
    void aFailedReleaseStepLoadsNothingAndKeepsTheLockThatWasLoaded() throws Exception {
        lock("llm", "gpu", 0);
        lock("image", "gpu", 0);
        arbiter.acquire("llm", null).close();
        failing.add("free llm");
        assertThatThrownBy(() -> arbiter.acquire("image", null)).isInstanceOf(GpuException.class).hasMessageContaining("free llm failed");
        assertThat(log).as("the image server is not started on a GPU that may be full").containsExactly("load llm", "free llm");
        assertThat(arbiter.loadedLock("gpu")).isEqualTo("llm");

        arbiter.acquire("llm", null).close();
        failing.clear();
        arbiter.acquire("image", null).close();
        assertThat(log).containsExactly("load llm", "free llm", "free llm", "load image");
    }

    @Test
    void anUnloadWhoseReleaseStepFailsStillLeavesTheGpuUnknown() throws Exception {
        lock("llm", "gpu", 0);
        arbiter.acquire("llm", null).close();
        failing.add("free llm");
        assertThat(arbiter.unload("gpu").get(10, TimeUnit.SECONDS)).contains("free llm failed");
        assertThat(arbiter.loadedLock("gpu")).isNull();
    }

    @Test
    void unloadWaitsForTheJobsAndRunsTheReleaseSteps() throws Exception {
        lock("llm", "gpu", 0);
        GpuLease llm = get(arbiter.acquireAsync("llm", null));
        CompletableFuture<String> unload = arbiter.unload("gpu");
        Thread.sleep(200);
        assertThat(unload).isNotDone();
        llm.close();
        assertThat(unload.get(10, TimeUnit.SECONDS)).isNull();
        assertThat(log).containsExactly("load llm", "free llm");
        assertThat(arbiter.loadedLock("gpu")).isNull();

        arbiter.acquire("llm", null).close();
        assertThat(log).as("loaded again after the unload").containsExactly("load llm", "free llm", "load llm");
    }

    @Test
    void theStepsRunOffTheCallersThreadAndJobsJoinWhileTheyRun() throws Exception {
        lock("image", "gpu", 0);
        CountDownLatch loading = new CountDownLatch(1);
        blocking.put("load image", loading);
        CompletableFuture<GpuLease> first = arbiter.acquireAsync("image", null);
        CompletableFuture<GpuLease> second = arbiter.acquireAsync("image", null);
        assertThat(first).as("acquireAsync never blocks").isNotDone();
        assertThat(arbiter.status("gpu").switching()).isTrue();
        loading.countDown();
        get(first).close();
        get(second).close();
        assertThat(log).containsExactly("load image");
    }

    @Test
    void aHeldLeaseReloadsItsLock() throws Exception {
        lock("speech", "gpu", 1);
        try (GpuLease lease = arbiter.acquire("speech", null)) {
            lease.reload();
        }
        assertThat(log).containsExactly("load speech", "free speech", "load speech");
        assertThat(arbiter.loadedLock("gpu")).isEqualTo("speech");
    }
}
