package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentApprovalService;
import com.github.enerccio.marginalia.domain.service.AgentRunService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class AgentApprovalServiceImpl implements AgentApprovalService {

    private static final Logger log = LoggerFactory.getLogger(AgentApprovalServiceImpl.class);

    private record Entry(PendingApproval approval, CompletableFuture<Boolean> decision) {
    }

    @Autowired
    @Lazy
    private AgentRunService runService;

    // by the id of the run, then the id of the approval
    private final Map<Long, Map<String, Entry>> pending = new ConcurrentHashMap<>();
    // the runs whose approvals were withdrawn by rejectAll and not decided by the user, see takeWithdrawn
    private final Set<Long> withdrawn = ConcurrentHashMap.newKeySet();

    @Override
    public CompletableFuture<Boolean> request(AgentRun run, String command, Map<String, Object> arguments) {
        if (run == null || run.getId() == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        Long runId = run.getId();
        withdrawn.remove(runId);
        PendingApproval approval = new PendingApproval(UUID.randomUUID().toString(), command,
                new LinkedHashMap<>(arguments == null ? Map.of() : arguments), new Date());
        CompletableFuture<Boolean> decision = new CompletableFuture<>();
        // the command, not its arguments (the story's business)
        log.info("Agent run {} waits for the user to approve the command {}", run.getUuid(), command);
        pending.computeIfAbsent(runId, id -> new ConcurrentHashMap<>()).put(approval.id(), new Entry(approval, decision));
        // cleans up; callbacks run in no fixed order, so what is pending is also told by the decision not being made
        decision.whenComplete((approved, error) -> pending.computeIfPresent(runId, (id, entries) -> {
            entries.remove(approval.id());
            return entries.isEmpty() ? null : entries;
        }));
        return decision;
    }

    @Override
    public boolean hasPending(Long runId) {
        Map<String, Entry> entries = runId == null ? null : pending.get(runId);
        return entries != null && entries.values().stream().anyMatch(entry -> !entry.decision().isDone());
    }

    @Override
    public List<PendingApproval> pending(AgentRun run) throws Exception {
        Long runId = owned(run);
        Map<String, Entry> entries = pending.get(runId);
        List<PendingApproval> approvals = new ArrayList<>();
        if (entries != null) {
            entries.values().stream().filter(entry -> !entry.decision().isDone()).forEach(entry -> approvals.add(entry.approval()));
        }
        approvals.sort(Comparator.comparing(PendingApproval::requestedAt));
        return approvals;
    }

    @Override
    public boolean decide(AgentRun run, String id, boolean approved) throws Exception {
        Long runId = owned(run);
        Map<String, Entry> entries = pending.get(runId);
        Entry entry = entries == null || id == null ? null : entries.get(id);
        boolean decided = entry != null && entry.decision().complete(approved);
        if (decided) {
            log.info("Agent run {}: the user {} the command {}", run.getUuid(), approved ? "approved" : "denied", entry.approval().command());
        }
        return decided;
    }

    @Override
    public int rejectAll(Long runId) {
        Map<String, Entry> entries = runId == null ? null : pending.get(runId);
        if (entries == null) {
            return 0;
        }
        int rejected = 0;
        for (Entry entry : new ArrayList<>(entries.values())) {
            if (!entry.decision().isDone()) {
                // before the decision is made: whoever waits for it asks right after
                withdrawn.add(runId);
            }
            if (entry.decision().complete(false)) {
                rejected++;
            }
        }
        return rejected;
    }

    @Override
    public boolean takeWithdrawn(Long runId) {
        return runId != null && withdrawn.remove(runId);
    }

    private Long owned(AgentRun run) throws Exception {
        AgentRun stored = run == null || run.getUuid() == null ? null : runService.findForUser(run.getUuid());
        if (stored == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        return stored.getId();
    }
}
