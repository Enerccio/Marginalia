package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.Gpu;
import com.github.enerccio.marginalia.domain.model.impl.GpuLock;
import com.github.enerccio.marginalia.domain.model.impl.GpuStep;
import com.github.enerccio.marginalia.domain.repository.GpuLockRepository;
import com.github.enerccio.marginalia.domain.repository.GpuRepository;
import com.github.enerccio.marginalia.domain.repository.GpuStepRepository;
import com.github.enerccio.marginalia.domain.security.AdminGuard;
import com.github.enerccio.marginalia.domain.service.GpuService;
import com.github.enerccio.marginalia.domain.service.gpu.*;
import com.github.enerccio.marginalia.domain.service.gpu.GpuArbiter.LockDef;
import com.github.enerccio.marginalia.domain.service.gpu.GpuStepRunner.Step;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.github.enerccio.marginalia.domain.traits.NoTx;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;

public class GpuServiceImpl implements GpuService {

    private static final Logger log = LoggerFactory.getLogger(GpuServiceImpl.class);

    private static final Comparator<GpuLockChoice> CHOICE_ORDER = Comparator
            .comparing((GpuLockChoice c) -> StringUtils.defaultString(c.gpuName()), String.CASE_INSENSITIVE_ORDER)
            .thenComparing(c -> StringUtils.defaultString(c.name()), String.CASE_INSENSITIVE_ORDER);

    private GpuRepository gpuRepository;
    private GpuLockRepository lockRepository;
    private GpuStepRepository stepRepository;

    @Autowired
    private AdminGuard adminGuard;

    @Autowired
    @Qualifier("common")
    private PlatformTransactionManager transactionManager;

    /** The locks by uuid as the arbiter and the pickers need them. */
    private record Snapshot(Map<String, LockDef> definitions, Map<String, GpuLockChoice> choices) {
    }

    /** Null when the locks are to be read again. */
    private volatile Snapshot snapshot;

    private final GpuArbiter arbiter = new GpuArbiter(this::resolve, GpuArbiter.HTTP);

    // --- for everybody ---

    @Override
    @NoTx
    public List<GpuLockChoice> lockChoices() {
        List<GpuLockChoice> list = new ArrayList<>(loaded().choices().values());
        list.sort(CHOICE_ORDER);
        return list;
    }

    @Override
    @NoTx
    public GpuLockChoice lockChoice(String lockUuid) {
        if (StringUtils.isBlank(lockUuid)) {
            return null;
        }
        return loaded().choices().get(lockUuid);
    }

    @Override
    @NoTx
    public GpuLease acquire(String lockUuid, Map<String, String> variables, BooleanSupplier cancelled) throws GpuException {
        return arbiter.acquire(lockUuid, variables, cancelled);
    }

    @Override
    @NoTx
    public CompletableFuture<GpuLease> acquireAsync(String lockUuid, Map<String, String> variables, IntConsumer position) {
        return arbiter.acquireAsync(lockUuid, variables, position);
    }

    private LockDef resolve(String lockUuid) {
        if (StringUtils.isBlank(lockUuid)) {
            return null;
        }
        try {
            return loaded().definitions().get(lockUuid);
        } catch (RuntimeException e) {
            log.warn("The GPU locks could not be read, the job runs without one: {}", e.getMessage(), e);
            return null;
        }
    }

    private Snapshot loaded() {
        Snapshot current = snapshot;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (snapshot != null) {
                return snapshot;
            }
            TransactionTemplate template = new TransactionTemplate(transactionManager);
            template.setReadOnly(true);
            return template.execute(status -> {
                Map<String, LockDef> defs = new HashMap<>();
                Map<String, GpuLockChoice> found = new HashMap<>();
                try {
                    for (GpuLock lock : lockRepository.findAll()) {
                        if (lock.getGpu() == null || lock.getGpu().isDeleted()) {
                            continue;
                        }
                        defs.put(lock.getUuid(), new LockDef(lock.getUuid(), lock.getName(), lock.getGpu().getUuid(), lock.getMaxConcurrent(),
                                steps(lock.getAcquireSteps()), steps(lock.getReleaseSteps()), lock.getGpu().isUsedOutside(),
                                steps(lock.getAlwaysSteps())));
                        found.put(lock.getUuid(), new GpuLockChoice(lock.getUuid(), lock.getName(), lock.getGpu().getName()));
                    }
                } catch (Exception e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
                Snapshot read = new Snapshot(Map.copyOf(defs), Map.copyOf(found));
                snapshot = read;
                return read;
            });
        }
    }

    private static List<Step> steps(List<GpuStep> steps) {
        return steps == null ? List.of() : steps.stream().filter(s -> s != null && !s.isDeleted()).map(Step::of).toList();
    }

    /** The locks are read again on the next use, after the transaction of the change (or now, without one). */
    private void changed() {
        snapshot = null;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    snapshot = null;
                }
            });
        }
    }

    // --- for the administrator ---

    @Override
    @CommonTxReadOnly
    public List<Gpu> findGpus() throws Exception {
        adminGuard.requireAdmin();
        List<Gpu> gpus = new ArrayList<>(gpuRepository.findAll());
        gpus.sort(Comparator.comparing(Gpu::getName, String.CASE_INSENSITIVE_ORDER));
        return gpus;
    }

    @Override
    @CommonTxReadOnly
    public List<GpuLock> findLocks() throws Exception {
        adminGuard.requireAdmin();
        List<GpuLock> locks = new ArrayList<>(lockRepository.findAll());
        locks.sort(Comparator.comparing((GpuLock l) -> l.getGpu() == null ? "" : l.getGpu().getName(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(GpuLock::getName, String.CASE_INSENSITIVE_ORDER));
        return locks;
    }

    @Override
    @CommonTxReadOnly
    public List<GpuStep> findSteps() throws Exception {
        adminGuard.requireAdmin();
        List<GpuStep> steps = new ArrayList<>(stepRepository.findAll());
        steps.sort(Comparator.comparing(GpuStep::getName, String.CASE_INSENSITIVE_ORDER));
        return steps;
    }

    @Override
    @CommonTx
    public Gpu saveGpu(Gpu gpu) throws Exception {
        adminGuard.requireAdmin();
        requireName(gpu.getName());
        Gpu saved = gpuRepository.save(gpu);
        changed();
        return saved;
    }

    @Override
    @CommonTx
    public void deleteGpu(Gpu gpu) throws Exception {
        adminGuard.requireAdmin();
        Gpu stored = gpuRepository.find(gpu.getId());
        if (stored == null) {
            return;
        }
        for (GpuLock lock : lockRepository.findByGpu(stored)) {
            lockRepository.delete(lock, true);
        }
        gpuRepository.delete(stored, true);
        changed();
    }

    @Override
    @CommonTx
    public GpuLock saveLock(GpuLock lock) throws Exception {
        adminGuard.requireAdmin();
        requireName(lock.getName());
        if (lock.getGpu() == null) {
            throw new IllegalArgumentException("A GPU lock needs a GPU");
        }
        GpuLock saved = lockRepository.save(lock);
        changed();
        return saved;
    }

    @Override
    @CommonTx
    public void deleteLock(GpuLock lock) throws Exception {
        adminGuard.requireAdmin();
        GpuLock stored = lockRepository.find(lock.getId());
        if (stored != null) {
            lockRepository.delete(stored, true);
        }
        changed();
    }

    @Override
    @CommonTx
    public GpuStep saveStep(GpuStep step) throws Exception {
        adminGuard.requireAdmin();
        requireName(step.getName());
        if (StringUtils.isBlank(step.getUrl())) {
            throw new IllegalArgumentException("A GPU step needs an address");
        }
        GpuStep saved = stepRepository.save(step);
        changed();
        return saved;
    }

    @Override
    @CommonTx
    public void deleteStep(GpuStep step) throws Exception {
        adminGuard.requireAdmin();
        GpuStep stored = stepRepository.find(step.getId());
        if (stored == null) {
            return;
        }
        for (GpuLock lock : lockRepository.findUsingStep(stored)) {
            lock.getAcquireSteps().removeIf(s -> Objects.equals(s.getId(), stored.getId()));
            lock.getReleaseSteps().removeIf(s -> Objects.equals(s.getId(), stored.getId()));
            lock.getAlwaysSteps().removeIf(s -> Objects.equals(s.getId(), stored.getId()));
            lockRepository.save(lock);
        }
        stepRepository.delete(stored, true);
        changed();
    }

    @Override
    @CommonTxReadOnly
    public List<GpuStatus> statuses() throws Exception {
        adminGuard.requireAdmin();
        List<GpuStatus> statuses = new ArrayList<>();
        for (Gpu gpu : gpuRepository.findAll()) {
            statuses.add(arbiter.status(gpu.getUuid()));
        }
        return statuses;
    }

    @Override
    @NoTx
    public CompletableFuture<String> unload(Gpu gpu) throws Exception {
        adminGuard.requireAdmin();
        return arbiter.unload(gpu.getUuid());
    }

    @Override
    @NoTx
    public String testStep(GpuStep step) throws Exception {
        adminGuard.requireAdmin();
        try {
            // no job: the variables are empty
            GpuStepRunner.run(Step.of(step).with(Map.of()));
            return null;
        } catch (GpuException e) {
            return e.getMessage();
        }
    }

    private static void requireName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("A name is needed");
        }
    }

    // --- wiring ---

    public void setGpuRepository(GpuRepository gpuRepository) {
        this.gpuRepository = gpuRepository;
    }

    public void setLockRepository(GpuLockRepository lockRepository) {
        this.lockRepository = lockRepository;
    }

    public void setStepRepository(GpuStepRepository stepRepository) {
        this.stepRepository = stepRepository;
    }
}
