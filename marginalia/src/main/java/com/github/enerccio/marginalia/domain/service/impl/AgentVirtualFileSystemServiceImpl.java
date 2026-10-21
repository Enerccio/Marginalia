package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.Configuration;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.AgentService.AgentGrant;
import com.github.enerccio.marginalia.domain.service.CleanupService.CleanupContributor;
import com.github.enerccio.marginalia.domain.service.CleanupService.EntityKey;
import com.github.enerccio.marginalia.domain.service.agent.*;
import com.github.enerccio.marginalia.domain.service.function.FunctionSet;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class AgentVirtualFileSystemServiceImpl implements AgentVirtualFileSystemService, CleanupContributor, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(AgentVirtualFileSystemServiceImpl.class);

    private static final int CHUNK_SIZE = 500;

    private static final Gson GSON = new Gson();

    @Autowired
    private Configuration configuration;

    @Autowired
    @Lazy
    private AgentService agentService;

    @Autowired
    @Lazy
    private AgentRunService agentRunService;

    @Autowired
    @Lazy
    private InferenceServices inferenceServices;

    @Autowired
    @Lazy
    private CleanupService cleanupService;

    private EntityManager entityManager;
    private WorkspaceLimits limits = WorkspaceLimits.DEFAULT;

    // by agent uuid: whoever changes a workspace takes its lock, the quotas are checked against what is on the disk
    private final ConcurrentMap<String, Lock> locks = new ConcurrentHashMap<>();

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void setLimits(WorkspaceLimits limits) {
        this.limits = limits;
    }

    @Override
    public void afterPropertiesSet() {
        cleanupService.registerContributor(this);
    }

    @Override
    public WorkspaceLimits getLimits() {
        return limits;
    }

    @Override
    @CommonTxReadOnly
    public WorkspaceLimits getLimits(Agent agent) throws Exception {
        return limitsOf(storedAgent(agent));
    }

    /**
     * The default limits with the quotas of the permission of the agent's owner put over them.
     */
    private WorkspaceLimits limitsOf(Agent stored) throws Exception {
        AgentGrant grant = agentService.getGrant(stored.getOwner());
        long fileBytes = grant.maxFileBytes() > 0 ? grant.maxFileBytes() : limits.maxFileBytes();
        long totalBytes = grant.maxWorkspaceBytes() > 0 ? grant.maxWorkspaceBytes() : limits.maxTotalBytes();
        int files = grant.maxWorkspaceFiles() > 0 ? grant.maxWorkspaceFiles() : limits.maxFiles();
        // an image may be as large as the default allows, or as any file when that is more
        long imageBytes = grant.maxFileBytes() > 0 ? Math.max(fileBytes, limits.maxImageBytes()) : limits.maxImageBytes();
        long archiveBytes = grant.maxFileBytes() > 0 ? Math.max(fileBytes, limits.maxArchiveBytes()) : limits.maxArchiveBytes();
        return new WorkspaceLimits(fileBytes, totalBytes, files, imageBytes, archiveBytes);
    }

    private Path home() {
        return configuration.getFolder().toPath();
    }

    private Lock lockOf(String agentUuid) {
        return locks.computeIfAbsent(agentUuid, uuid -> new ReentrantLock());
    }

    @Override
    @CommonTxReadOnly
    public VirtualFileSystem open(AgentRun run) throws Exception {
        AgentRun stored = run == null ? null : agentRunService.findForUser(run.getUuid());
        if (stored == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        Agent agent = storedAgent(stored.getAgent());
        // the instructions are read here: a changed text applies when the run is opened again, as in the system prompt
        return VirtualFileSystem.ofRun(RunFolder.of(home(), stored.getUuid(), agent.getUuid()), limitsOf(agent),
                lockOf(agent.getUuid()), agentService.getInstructions(agent));
    }

    @Override
    @CommonTxReadOnly
    public RunFolder getFolder(AgentRun run) throws Exception {
        AgentRun stored = run == null ? null : agentRunService.findForUser(run.getUuid());
        if (stored == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        return RunFolder.of(home(), stored.getUuid(), storedAgent(stored.getAgent()).getUuid());
    }

    @Override
    @CommonTxReadOnly
    public VirtualFileSystem openWorkspace(Agent agent) throws Exception {
        Agent stored = storedAgent(agent);
        return VirtualFileSystem.ofWorkspace(RunFolder.workspaceOf(home(), stored.getUuid()), limitsOf(stored),
                lockOf(stored.getUuid()));
    }

    private Agent storedAgent(Agent agent) throws Exception {
        Agent stored = agent == null ? null : agentService.findForUser(agent.getUuid());
        if (stored == null) {
            throw new IllegalArgumentException("The agent does not exist");
        }
        return stored;
    }

    @Override
    public FunctionSet getFileTools(VirtualFileSystem fileSystem, CommandResults commandResults) {
        return inferenceServices.getFunctionRegistry().bind(AgentFileTools.class, new AgentFileToolsImpl(fileSystem, commandResults));
    }

    @Override
    @CommonTxReadOnly
    public WorkspaceUsage getUsage(Agent agent) throws Exception {
        return openWorkspace(agent).getUsage();
    }

    @Override
    @CommonTxReadOnly
    public void clearWorkspace(Agent agent) throws Exception {
        Agent stored = storedAgent(agent);
        AgentRun unfinished = agentRunService.findUnfinished(stored);
        if (unfinished != null && unfinished.getStatus().isActive()) {
            throw new IllegalStateException("The agent is working, pause or stop it first");
        }
        openWorkspace(stored).clearWorkspace();
    }

    @Override
    @CommonTxReadOnly
    public void copyWorkspace(Agent source, Agent target) throws Exception {
        Agent from = storedAgent(source);
        Agent to = storedAgent(target);
        if (from.getUuid().equals(to.getUuid())) {
            throw new IllegalArgumentException("An agent's workspace can't be copied onto itself");
        }
        if (!Files.isDirectory(RunFolder.workspaceOf(home(), from.getUuid()))) {
            // never used, nothing to copy
            return;
        }
        openWorkspace(to).copyWorkspaceFrom(openWorkspace(from));
    }

    @Override
    public void deleteWorkspace(Agent agent) {
        if (agent != null && agent.getUuid() != null) {
            deleteAfterCommit(RunFolder.workspaceOf(home(), agent.getUuid()));
            locks.remove(agent.getUuid());
        }
    }

    @Override
    @CommonTx
    public void writeBase(AgentRun run, JsonObject base) throws Exception {
        AgentRun stored = run == null ? null : agentRunService.findForUser(run.getUuid());
        if (stored == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        RunFolder folder = RunFolder.of(home(), stored.getUuid(), storedAgent(stored.getAgent()).getUuid());
        deleteTree(folder.base());
        Files.createDirectories(folder.base());
        try (Writer writer = Files.newBufferedWriter(folder.baseManuscript(), StandardCharsets.UTF_8)) {
            GSON.toJson(base, writer);
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            // the files are not brought back by a rollback, the base of a run that was not stored is not wanted
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED) {
                        deleteTree(folder.base());
                    }
                }
            });
        }
    }

    @Override
    @CommonTxReadOnly
    public JsonObject readBase(AgentRun run) throws Exception {
        AgentRun stored = run == null ? null : agentRunService.findForUser(run.getUuid());
        if (stored == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        Path file = RunFolder.runOf(home(), stored.getUuid()).resolve(RunFolder.BASE).resolve(RunFolder.BASE_MANUSCRIPT);
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, JsonObject.class);
        }
    }

    private AgentRun ownRun(AgentRun run) throws Exception {
        AgentRun stored = run == null ? null : agentRunService.findForUser(run.getUuid());
        if (stored == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        return stored;
    }

    /**
     * Does the work when the transaction commits, at once when there is none.
     */
    private static void afterCommit(Runnable work) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    work.run();
                }
            });
        } else {
            work.run();
        }
    }

    private static void writeAtomically(Path file, String content) {
        try {
            Files.createDirectories(file.getParent());
            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temporary, content, StandardCharsets.UTF_8);
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void updateBase(AgentRun run, JsonObject base) throws Exception {
        AgentRun stored = ownRun(run);
        Path file = RunFolder.runOf(home(), stored.getUuid()).resolve(RunFolder.BASE).resolve(RunFolder.BASE_MANUSCRIPT);
        String json = GSON.toJson(base);
        afterCommit(() -> writeAtomically(file, json));
    }

    @Override
    public void writeSyncReport(AgentRun run, String report) throws Exception {
        AgentRun stored = ownRun(run);
        Path folder = RunFolder.runOf(home(), stored.getUuid()).resolve(RunFolder.BASE);
        afterCommit(() -> {
            try {
                Files.deleteIfExists(folder.resolve(RunFolder.SYNC_REPORT));
                Files.deleteIfExists(folder.resolve(RunFolder.SYNC_PENDING));
                if (report != null) {
                    writeAtomically(folder.resolve(RunFolder.SYNC_REPORT), report);
                    writeAtomically(folder.resolve(RunFolder.SYNC_PENDING), "");
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Override
    public String readSyncReport(AgentRun run) throws Exception {
        AgentRun stored = ownRun(run);
        Path file = RunFolder.runOf(home(), stored.getUuid()).resolve(RunFolder.BASE).resolve(RunFolder.SYNC_REPORT);
        return Files.isRegularFile(file) ? Files.readString(file, StandardCharsets.UTF_8) : null;
    }

    @Override
    public String takePendingSync(AgentRun run) {
        if (run == null || run.getUuid() == null) {
            return null;
        }
        Path folder = RunFolder.runOf(home(), run.getUuid()).resolve(RunFolder.BASE);
        try {
            if (!Files.deleteIfExists(folder.resolve(RunFolder.SYNC_PENDING))) {
                return null;
            }
            Path file = folder.resolve(RunFolder.SYNC_REPORT);
            return Files.isRegularFile(file) ? Files.readString(file, StandardCharsets.UTF_8) : null;
        } catch (IOException e) {
            log.warn("Agent run {}: the report of the sync can't be read: {}", run.getUuid(), e.getMessage());
            return null;
        }
    }

    @Override
    public void writeCallMarker(AgentRun run, String callId) {
        writeRunFile(run, RunFolder.CALL_MARKER, callId);
    }

    @Override
    public String readCallMarker(AgentRun run) {
        return readRunFile(run, RunFolder.CALL_MARKER);
    }

    @Override
    public void writeQueue(AgentRun run, String json) {
        writeRunFile(run, RunFolder.QUEUE, json);
    }

    @Override
    public String readQueue(AgentRun run) {
        return readRunFile(run, RunFolder.QUEUE);
    }

    @Override
    public void writeCallMarker(AgentRun run, String subAgent, String callId) {
        writeRunFile(run, subCallMarker(subAgent), callId);
    }

    @Override
    public String readCallMarker(AgentRun run, String subAgent) {
        return readRunFile(run, subCallMarker(subAgent));
    }

    /**
     * The name is made of the handle, so it is a name of the run folder only when the handle is plain.
     */
    private static String subCallMarker(String subAgent) {
        if (subAgent == null || !subAgent.matches("[a-z0-9]{1,32}")) {
            throw new IllegalArgumentException("Not a handle of a sub-agent: " + subAgent);
        }
        return RunFolder.SUB_CALL_MARKER + subAgent;
    }

    @Override
    public void writeResults(AgentRun run, String json) {
        writeRunFile(run, RunFolder.RESULTS, json);
    }

    @Override
    public String readResults(AgentRun run) {
        return readRunFile(run, RunFolder.RESULTS);
    }

    @Override
    public void writeDelivery(AgentRun run, String json) {
        writeRunFile(run, RunFolder.DELIVERY, json);
    }

    @Override
    public String readDelivery(AgentRun run) {
        return readRunFile(run, RunFolder.DELIVERY);
    }

    @Override
    public void writePlan(AgentRun run, String json) {
        writeRunFile(run, RunFolder.PLAN, json);
    }

    @Override
    public String readPlan(AgentRun run) {
        return readRunFile(run, RunFolder.PLAN);
    }

    /**
     * A small file of the run, written at once (not when the transaction ends: the runner keeps state in them that it needs
     * when the server dies); null removes it. A failure is logged, the run goes on without the file.
     */
    private void writeRunFile(AgentRun run, String name, String content) {
        if (run == null || run.getUuid() == null) {
            return;
        }
        Path file = RunFolder.runOf(home(), run.getUuid()).resolve(RunFolder.BASE).resolve(name);
        try {
            if (content == null) {
                Files.deleteIfExists(file);
            } else {
                writeAtomically(file, content);
            }
        } catch (IOException | UncheckedIOException e) {
            log.warn("Agent run {}: {} can't be written: {}", run.getUuid(), name, e.getMessage());
        }
    }

    private String readRunFile(AgentRun run, String name) {
        if (run == null || run.getUuid() == null) {
            return null;
        }
        Path file = RunFolder.runOf(home(), run.getUuid()).resolve(RunFolder.BASE).resolve(name);
        try {
            return Files.isRegularFile(file) ? Files.readString(file, StandardCharsets.UTF_8) : null;
        } catch (IOException e) {
            log.warn("Agent run {}: {} can't be read: {}", run.getUuid(), name, e.getMessage());
            return null;
        }
    }

    @Override
    public void writeResumeNote(AgentRun run, String note) throws Exception {
        AgentRun stored = ownRun(run);
        Path file = RunFolder.runOf(home(), stored.getUuid()).resolve(RunFolder.BASE).resolve(RunFolder.RESUME_NOTE);
        String kept = Files.isRegularFile(file) ? Files.readString(file, StandardCharsets.UTF_8) : "";
        String text = kept.isBlank() ? note.strip() : kept.strip() + "\n\n" + note.strip();
        afterCommit(() -> writeAtomically(file, text));
    }

    @Override
    public String takeResumeNote(AgentRun run) {
        if (run == null || run.getUuid() == null) {
            return null;
        }
        Path file = RunFolder.runOf(home(), run.getUuid()).resolve(RunFolder.BASE).resolve(RunFolder.RESUME_NOTE);
        try {
            if (!Files.isRegularFile(file)) {
                return null;
            }
            String note = Files.readString(file, StandardCharsets.UTF_8);
            Files.deleteIfExists(file);
            return note.isBlank() ? null : note;
        } catch (IOException e) {
            log.warn("Agent run {}: the note of the resume can't be read: {}", run.getUuid(), e.getMessage());
            return null;
        }
    }

    @Override
    public void deleteBase(AgentRun run) {
        if (run != null && run.getUuid() != null) {
            deleteAfterCommit(RunFolder.runOf(home(), run.getUuid()).resolve(RunFolder.BASE));
        }
    }

    @Override
    public void deleteCheckpoints(AgentRun run) {
        if (run != null && run.getUuid() != null) {
            deleteAfterCommit(RunFolder.runOf(home(), run.getUuid()).resolve(RunFolder.CHECKPOINTS));
        }
    }

    @Override
    public void deleteRunFolder(AgentRun run) {
        if (run != null && run.getUuid() != null) {
            deleteAfterCommit(RunFolder.runOf(home(), run.getUuid()));
        }
    }

    // ---------------------------------------------------------------------------------------------------------------
    // purge by the cleanup
    // ---------------------------------------------------------------------------------------------------------------

    @Override
    public void beforePurge(Set<EntityKey> purged) {
        for (String uuid : uuidsOf(purged, Agent.class, "agents")) {
            deleteAfterCommit(RunFolder.workspaceOf(home(), uuid));
            locks.remove(uuid);
        }
        for (String uuid : uuidsOf(purged, AgentRun.class, "agent_runs")) {
            deleteAfterCommit(RunFolder.runOf(home(), uuid));
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> uuidsOf(Set<EntityKey> purged, Class<?> type, String table) {
        List<Long> ids = purged.stream().filter(key -> key.type() == type).map(EntityKey::id).collect(Collectors.toList());
        List<String> uuids = new java.util.ArrayList<>();
        for (int from = 0; from < ids.size(); from += CHUNK_SIZE) {
            List<Long> chunk = ids.subList(from, Math.min(ids.size(), from + CHUNK_SIZE));
            uuids.addAll(entityManager.createNativeQuery("SELECT uuid FROM " + table + " WHERE id IN (:ids)")
                    .setParameter("ids", chunk).getResultList());
        }
        return uuids;
    }

    /**
     * The files are not brought back by a rollback, so they go when the transaction that deleted their owner has
     * committed.
     */
    private void deleteAfterCommit(Path folder) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    deleteTree(folder);
                }
            });
        } else {
            deleteTree(folder);
        }
    }

    private static void deleteTree(Path folder) {
        if (!Files.exists(folder, java.nio.file.LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        try {
            Files.walkFileTree(folder, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                    if (e != null) {
                        throw e;
                    }
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.warn("The folder {} of a deleted agent or run could not be deleted", folder, e);
        }
    }

}
