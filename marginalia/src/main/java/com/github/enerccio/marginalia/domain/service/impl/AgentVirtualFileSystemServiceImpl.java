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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
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
        return new WorkspaceLimits(fileBytes, totalBytes, files, imageBytes);
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
                lockOf(agent.getUuid()), StringUtils.defaultString(agent.getInstructions()));
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
