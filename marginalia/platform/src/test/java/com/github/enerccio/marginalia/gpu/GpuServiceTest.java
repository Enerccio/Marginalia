package com.github.enerccio.marginalia.gpu;

import com.github.enerccio.marginalia.domain.model.impl.AI;
import com.github.enerccio.marginalia.domain.model.impl.Gpu;
import com.github.enerccio.marginalia.domain.model.impl.GpuLock;
import com.github.enerccio.marginalia.domain.model.impl.GpuStep;
import com.github.enerccio.marginalia.domain.service.AIService;
import com.github.enerccio.marginalia.domain.service.GpuService;
import com.github.enerccio.marginalia.domain.service.InferenceService.InferenceResultCallback;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.service.gpu.GpuException;
import com.github.enerccio.marginalia.domain.service.gpu.GpuLease;
import com.github.enerccio.marginalia.domain.service.gpu.GpuLockChoice;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.structured.SchemaCheck;
import com.github.enerccio.marginalia.domain.traits.LLMFunction;
import com.github.enerccio.marginalia.test.PlatformTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The GPUs, locks and steps the administrator sets up, and the requests of an AI with a lock. The steps call a local fake server.
 */
class GpuServiceTest extends PlatformTestBase {

    private static final List<LLMChatMessage> PAYLOAD = List.of(LLMChatMessage.of(LLMRole.USER, "Write."));

    @Autowired
    private GpuService gpuService;

    @Autowired
    private AIService aiService;

    @Autowired
    private JdbcTemplate jdbc;

    private HttpServer server;
    // the paths the steps called, with the Authorization header
    private final List<String> calls = new CopyOnWriteArrayList<>();

    @BeforeEach
    void aFakeServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", exchange -> {
            String auth = exchange.getRequestHeaders().getFirst("Authorization");
            calls.add(exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath() + (auth == null ? "" : " " + auth));
            exchange.getRequestBody().readAllBytes();
            int status = exchange.getRequestURI().getPath().startsWith("/fail") ? 500 : 200;
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopTheServer() {
        server.stop(0);
    }

    private String url(String path) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + path;
    }

    private GpuStep step(String name, String path) throws Exception {
        GpuStep step = new GpuStep();
        step.setName(name);
        step.setUrl(url(path));
        step.setHeaders("Authorization: Bearer secret");
        return gpuService.saveStep(step);
    }

    private GpuLock lock(String name, Gpu gpu, int max, List<GpuStep> acquire, List<GpuStep> release) throws Exception {
        GpuLock lock = new GpuLock();
        lock.setName(name);
        lock.setGpu(gpu);
        lock.setMaxConcurrent(max);
        lock.setAcquireSteps(acquire);
        lock.setReleaseSteps(release);
        return gpuService.saveLock(lock);
    }

    private Gpu gpu(String name) throws Exception {
        Gpu gpu = new Gpu();
        gpu.setName(name);
        return gpuService.saveGpu(gpu);
    }

    @Test
    void onlyAnAdministratorSetsThemUpButEveryUserSeesTheChoices() throws Exception {
        loginAdmin();
        Gpu gpu = gpu(uniqueName("gpu"));
        GpuLock lock = lock(uniqueName("llm"), gpu, 0, List.of(), List.of());

        login();
        assertThatThrownBy(() -> gpu("mine")).isInstanceOf(SecurityException.class);
        assertThatThrownBy(() -> gpuService.findSteps()).isInstanceOf(SecurityException.class);
        GpuLockChoice choice = gpuService.lockChoice(lock.getUuid());
        assertThat(choice).isNotNull();
        assertThat(choice.gpuName()).isEqualTo(gpu.getName());
        assertThat(gpuService.lockChoices()).extracting(GpuLockChoice::uuid).contains(lock.getUuid());
    }

    @Test
    void theHeadersOfAStepAreStoredEncrypted() throws Exception {
        loginAdmin();
        GpuStep step = step("unload", "/unload");
        assertThat(gpuService.findSteps()).filteredOn(s -> s.getId().equals(step.getId()))
                .extracting(GpuStep::getHeaders).containsExactly("Authorization: Bearer secret");
        String stored = jdbc.queryForObject("SELECT headers FROM gpu_steps WHERE id = ?", String.class, step.getId());
        assertThat(stored).doesNotContain("secret");
    }

    @Test
    void aJobOfAnotherLockRunsTheReleaseStepsOfTheLoadedOneThenItsAcquireSteps() throws Exception {
        loginAdmin();
        Gpu gpu = gpu(uniqueName("gpu"));
        GpuStep loadLlm = step("load llm", "/llm/load");
        GpuStep freeLlm = step("free llm", "/llm/free");
        GpuStep loadComfy = step("load comfy", "/comfy/load");
        GpuLock llm = lock("llm", gpu, 0, List.of(loadLlm), List.of(freeLlm));
        GpuLock image = lock("image", gpu, 1, List.of(freeLlm, loadComfy), List.of());

        gpuService.acquire(llm.getUuid(), null).close();
        gpuService.acquire(llm.getUuid(), null).close();
        try (GpuLease _ = gpuService.acquire(image.getUuid(), null)) {
            assertThat(gpuService.statuses()).filteredOn(s -> s.gpuUuid().equals(gpu.getUuid()))
                    .singleElement().satisfies(s -> {
                        assertThat(s.loadedLock()).isEqualTo("image");
                        assertThat(s.active()).isEqualTo(1);
                    });
        }
        assertThat(calls).containsExactly("POST /llm/load Bearer secret", "POST /llm/free Bearer secret", "POST /llm/free Bearer secret",
                "POST /comfy/load Bearer secret");
    }

    @Test
    void theAlwaysStepsAreStoredAndRunBeforeEveryJob() throws Exception {
        loginAdmin();
        Gpu gpu = gpu(uniqueName("gpu"));
        GpuStep room = step("make room", "/llama/unload");
        GpuStep load = step("load llm", "/llm/load");
        GpuLock llm = lock("llm", gpu, 0, List.of(load), List.of());
        llm.setAlwaysSteps(List.of(room));
        GpuLock saved = gpuService.saveLock(llm);
        assertThat(gpuService.findLocks()).filteredOn(l -> l.getId().equals(saved.getId())).singleElement()
                .satisfies(l -> assertThat(l.getAlwaysSteps()).extracting(GpuStep::getName).containsExactly("make room"));

        gpuService.acquire(saved.getUuid(), null).close();
        gpuService.acquire(saved.getUuid(), null).close();
        assertThat(calls).containsExactly("POST /llama/unload Bearer secret", "POST /llm/load Bearer secret", "POST /llama/unload Bearer secret");

        gpuService.deleteStep(room);
        assertThat(gpuService.findLocks()).filteredOn(l -> l.getId().equals(saved.getId())).singleElement()
                .satisfies(l -> assertThat(l.getAlwaysSteps()).isEmpty());
    }

    @Test
    void aFailingStepFailsTheJob() throws Exception {
        loginAdmin();
        Gpu gpu = gpu(uniqueName("gpu"));
        GpuLock lock = lock("broken", gpu, 0, List.of(step("fail", "/fail")), List.of());
        assertThatThrownBy(() -> gpuService.acquire(lock.getUuid(), null)).isInstanceOf(GpuException.class).hasMessageContaining("500");
        assertThat(gpuService.testStep(step("ok", "/ok"))).isNull();
        assertThat(gpuService.testStep(step("bad", "/fail"))).contains("500");
    }

    @Test
    void deletingAStepTakesItOutOfTheLocksAndDeletingAGpuDeletesItsLocks() throws Exception {
        loginAdmin();
        Gpu gpu = gpu(uniqueName("gpu"));
        GpuStep keep = step("keep", "/keep");
        GpuStep drop = step("drop", "/drop");
        GpuLock lock = lock("lock", gpu, 0, List.of(keep, drop), List.of(drop));

        gpuService.deleteStep(drop);
        GpuLock read = gpuService.findLocks().stream().filter(l -> l.getId().equals(lock.getId())).findFirst().orElseThrow();
        assertThat(read.getAcquireSteps()).extracting(GpuStep::getName).containsExactly("keep");
        assertThat(read.getReleaseSteps()).isEmpty();

        gpuService.deleteGpu(gpu);
        assertThat(gpuService.findLocks()).extracting(GpuLock::getId).doesNotContain(lock.getId());
        assertThat(gpuService.lockChoice(lock.getUuid())).isNull();
        assertThat(gpuService.acquire(lock.getUuid(), null)).as("a lock that is gone is no lock").isSameAs(GpuLease.NONE);
    }

    @Test
    void unloadRunsTheReleaseStepsOfTheLoadedLock() throws Exception {
        loginAdmin();
        Gpu gpu = gpu(uniqueName("gpu"));
        GpuLock lock = lock("llm", gpu, 0, List.of(step("load", "/load")), List.of(step("free", "/free")));
        gpuService.acquire(lock.getUuid(), null).close();
        assertThat(gpuService.unload(gpu).get(10, TimeUnit.SECONDS)).isNull();
        assertThat(calls).containsExactly("POST /load Bearer secret", "POST /free Bearer secret");
    }

    private static class Collect implements InferenceResultCallback {
        final CompletableFuture<String> done = new CompletableFuture<>();

        @Override
        public void onResult(String text, SchemaCheck check) {
            done.complete(text);
        }

        @Override
        public void onCancel() {
            done.cancel(false);
        }

        @Override
        public void onError(Throwable error) {
            done.completeExceptionally(error);
        }
    }

    @Test
    void aRequestOfAnAIWithALockHoldsTheGpuUntilItEnds() throws Exception {
        loginAdmin();
        Gpu gpu = gpu(uniqueName("gpu"));
        GpuLock llmLock = lock("llm", gpu, 0, List.of(step("load llm", "/llm/load")), List.of(step("free llm", "/llm/free")));
        GpuLock imageLock = lock("image", gpu, 0, List.of(step("load image", "/image/load")), List.of());
        AI ai = createAI();
        ai.setGpuLock(llmLock.getUuid());
        ai = aiService.save(ai);

        CountDownLatch release = new CountDownLatch(1);
        llm.enqueue(MockLLMResponse.text("first").waitFor(release));
        Collect first = new Collect();
        inferenceServices.forAI(ai).infer(PAYLOAD, first);
        llm.awaitCompletionRequests(1, Duration.ofSeconds(10));
        assertThat(calls).containsExactly("POST /llm/load Bearer secret");

        // an image job waits for the request of the model
        CompletableFuture<GpuLease> image = gpuService.acquireAsync(imageLock.getUuid(), null);
        Thread.sleep(300);
        assertThat(image).isNotDone();

        release.countDown();
        assertThat(first.done.get(10, TimeUnit.SECONDS)).isEqualTo("first");
        GpuLease imageLease = image.get(10, TimeUnit.SECONDS);
        assertThat(calls).containsExactly("POST /llm/load Bearer secret", "POST /llm/free Bearer secret", "POST /image/load Bearer secret");

        // the next request of the model waits for the image and loads the model again
        llm.enqueue(MockLLMResponse.text("second"));
        Collect second = new Collect();
        inferenceServices.forAI(ai).infer(PAYLOAD, second);
        Thread.sleep(300);
        assertThat(llm.getCompletionRequests()).as("not sent while the image holds the GPU").hasSize(1);
        imageLease.close();
        assertThat(second.done.get(10, TimeUnit.SECONDS)).isEqualTo("second");
        assertThat(calls).endsWith("POST /llm/load Bearer secret");
    }

    @Test
    void aRequestWhoseAcquireStepFailsEndsWithTheError() throws Exception {
        loginAdmin();
        Gpu gpu = gpu(uniqueName("gpu"));
        GpuLock lock = lock("broken", gpu, 0, List.of(step("fail", "/fail")), List.of());
        AI ai = createAI();
        ai.setGpuLock(lock.getUuid());
        ai = aiService.save(ai);

        Collect result = new Collect();
        inferenceServices.forAI(ai).infer(PAYLOAD, result);
        assertThatThrownBy(() -> result.done.get(10, TimeUnit.SECONDS)).hasMessageContaining("500");
        assertThat(llm.getCompletionRequests()).isEmpty();
    }
    interface Lookup {

        @LLMFunction(description = "Looks something up with the model")
        void lookup(FunctionContinuation<String> done, String name);
    }

    @Test
    void aFunctionTheModelCalledMayAskTheSameModelOnALockOfOneJob() throws Exception {
        // the GPU is given back with the connection while the function runs, or the question inside would wait for itself
        loginAdmin();
        Gpu gpu = gpu(uniqueName("gpu"));
        GpuLock lock = lock("llm", gpu, 1, List.of(step("load", "/load")), List.of());
        AI stored = createAI();
        stored.setGpuLock(lock.getUuid());
        AI ai = aiService.save(stored);
        FunctionSet functions = inferenceServices.getFunctionRegistry().bind(Lookup.class, (Lookup) (done, name) -> {
            try {
                inferenceServices.forAI(ai).infer(PAYLOAD, new InferenceResultCallback() {
                    @Override
                    public void onResult(String text, SchemaCheck check) {
                        done.complete(text);
                    }

                    @Override
                    public void onCancel() {
                        done.fail("cancelled");
                    }

                    @Override
                    public void onError(Throwable exception) {
                        done.fail(exception);
                    }
                });
            } catch (Exception e) {
                done.fail(e);
            }
        });
        llm.enqueue(MockLLMResponse.toolCall("lookup", "{\"name\":\"Anna\"}"));
        llm.enqueue(MockLLMResponse.text("Anna is 30"));
        llm.enqueue(MockLLMResponse.text("She is thirty."));
        Collect outer = new Collect();

        inferenceServices.forAI(ai).infer(PAYLOAD, null, null, functions, outer);

        assertThat(outer.done.get(10, TimeUnit.SECONDS)).isEqualTo("She is thirty.");
        assertThat(llm.getCompletionRequests()).hasSize(3);
        assertThat(calls).as("loaded once, then sticky").containsExactly("POST /load Bearer secret");
        assertThat(gpuService.statuses()).filteredOn(st -> st.gpuUuid().equals(gpu.getUuid())).singleElement()
                .satisfies(st -> assertThat(st.active()).isZero());
    }
}
