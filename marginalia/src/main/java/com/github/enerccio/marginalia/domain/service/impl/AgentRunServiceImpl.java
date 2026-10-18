package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.bound.AgentRequestAttributes;
import com.github.enerccio.marginalia.bound.AgentSession;
import com.github.enerccio.marginalia.bound.AgenticSessionPoint;
import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.repository.AgentRunRepository;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.service.*;
import com.github.enerccio.marginalia.domain.service.AgentService.AgentGrant;
import com.github.enerccio.marginalia.domain.service.AgentService.ResolvedProfile;
import com.github.enerccio.marginalia.domain.traits.CommonTx;
import com.github.enerccio.marginalia.domain.traits.CommonTxReadOnly;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AgentRunServiceImpl extends ExtendableServiceImpl<AgentRun, AgentRunRepository> implements AgentRunService {

    private static final Logger log = LoggerFactory.getLogger(AgentRunServiceImpl.class);

    /**
     * The statuses a run stops working in; the run has an end from then, until it is resumed.
     */
    private static final Set<AgentRunStatus> FINISHING = Set.of(AgentRunStatus.DONE, AgentRunStatus.STOPPED_KEPT,
            AgentRunStatus.STOPPED_DISCARDED, AgentRunStatus.FAILED);

    /**
     * The statuses a run has no use for its working copy in: nothing resumes it and nothing merges it back (yet), so
     * the copy is deleted for good when the run gets there, and the next run makes a new one. A paused or kept run
     * keeps the copy, it is resumed on it.
     */
    /**
     * The statuses in which the run asks nobody anything: the commands that wait for the approval of the user are
     * refused, so the calls in the air end.
     */
    private static final Set<AgentRunStatus> REFUSING = Set.of(AgentRunStatus.PAUSED, AgentRunStatus.STOPPING,
            AgentRunStatus.STOPPED_KEPT, AgentRunStatus.STOPPED_DISCARDED, AgentRunStatus.DONE, AgentRunStatus.FAILED);

    private static final Set<AgentRunStatus> COPY_ENDING = Set.of(AgentRunStatus.DONE, AgentRunStatus.FAILED,
            AgentRunStatus.STOPPED_DISCARDED, AgentRunStatus.APPLIED);

    @Autowired
    private AgentService agentService;

    @Autowired
    @Lazy
    private ManuscriptService manuscriptService;

    @Autowired
    @Lazy
    private AgentVirtualFileSystemService fileSystemService;

    @Autowired
    @Lazy
    private AgentMessageService messageService;

    @Autowired
    @Lazy
    private AgentApprovalService approvalService;

    @Autowired
    @Lazy
    private AgentAttentionService attentionService;

    @Override
    @CommonTx
    public AgentRun start(Agent agent) throws Exception {
        return start(agent, false);
    }

    @Override
    @CommonTx
    public AgentRun start(Agent agent, boolean interactive) throws Exception {
        Agent stored = agent == null ? null : agentService.findForUser(agent.getUuid());
        if (stored == null) {
            throw new IllegalArgumentException("The agent does not exist");
        }
        checkCanWork(stored, true);

        AgentRun run = new AgentRun();
        run.setAgent(stored);
        // every run works on a copy of its own, never on the book of the user
        run.setWorkingCopy(manuscriptService.createAgenticCopy(stored.getManuscript()));
        run.setStatus(AgentRunStatus.RUNNING);
        run.setInteractive(interactive);
        run.setStartedAt(new Date());
        run.setLanguage(stored.getManuscript().getLanguage());
        AgentRun started = save(run);
        log.info("Agent run {} started: agent \"{}\" of user {}, {}", started.getUuid(), stored.getName(), currentUser.getId(),
                interactive ? "a conversation" : "autonomous");
        publishAfterCommit(started);
        return started;
    }

    /**
     * What a run needs to work, at a start and at a resume: the user may use agents, the agent is enabled and has a
     * usable main model, and the user has a free concurrent run.
     */
    private void checkCanWork(Agent stored, boolean start) throws Exception {
        User user = userService.find(currentUser.getId());
        AgentGrant grant = agentService.getGrant(user);
        if (!grant.allowed()) {
            throw new SecurityException("The user is not allowed to use agents");
        }
        if (!stored.isEnabled()) {
            throw new IllegalStateException("The agent is disabled");
        }
        if (stored.getMainProfile() == null) {
            throw new IllegalStateException("The agent has no model");
        }
        if (agentService.resolveProfile(stored.getMainProfile()) == null) {
            throw new IllegalStateException("The model of the agent is not available");
        }
        // a resumed run is the unfinished run of the agent
        if (start && getRepository().findByAgent(stored, AgentRunStatus.unfinished()) != null) {
            throw new IllegalStateException("The agent already has an unfinished run");
        }
        if (getRepository().countByStatus(user, AgentRunStatus.active()) >= grant.maxConcurrentRuns()) {
            throw new IllegalStateException("The user has all the concurrent runs they are allowed");
        }
    }

    @Override
    @CommonTx
    public AgentRun resume(AgentRun run) throws Exception {
        if (run == null || run.getId() == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        if (!run.getStatus().isResumable()) {
            throw new IllegalStateException("A run that is " + run.getStatus() + " can't be resumed");
        }
        Agent stored = agentService.findForUser(run.getAgent().getUuid());
        if (stored == null) {
            throw new IllegalArgumentException("The agent does not exist");
        }
        checkCanWork(stored, false);
        AgentRun current = find(run.getId());
        if (current != null && current.getWorkingCopy() == null) {
            // a run from before the copies, or one whose copy is gone
            getRepository().updateWorkingCopy(run.getId(), manuscriptService.createAgenticCopy(stored.getManuscript()));
        }
        return transition(run, AgentRunStatus.RUNNING, null);
    }

    @Override
    @CommonTx
    public AgentRun finish(AgentRun run) throws Exception {
        if (run == null || run.getId() == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        if (run.getStatus() != AgentRunStatus.IDLE) {
            throw new IllegalStateException("A run that is " + run.getStatus() + " can't be finished by the user");
        }
        return transition(run, AgentRunStatus.DONE, null);
    }

    @Override
    @CommonTx
    public AgentRun pause(AgentRun run) throws Exception {
        if (run == null || run.getId() == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        AgentRun current = switch (run.getStatus()) {
            case PAUSING -> run;
            case IDLE, WAITING_APPROVAL -> {
                // stable states: nothing to wait for
                yield transition(run, AgentRunStatus.PAUSED, null);
            }
            case RUNNING -> transition(run, AgentRunStatus.PAUSING, null);
            default -> throw new IllegalStateException("A run that is " + run.getStatus() + " can't be paused");
        };
        return current.getStatus() == AgentRunStatus.PAUSING && !hasWorker(current)
                ? transition(current, AgentRunStatus.PAUSED, null) : current;
    }

    @Override
    @CommonTx
    public AgentRun stop(AgentRun run, boolean keep) throws Exception {
        if (run == null || run.getId() == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        AgentRunStatus target = keep ? AgentRunStatus.STOPPED_KEPT : AgentRunStatus.STOPPED_DISCARDED;
        log.info("Agent run {}: the user asked to stop it, the result is {}", run.getUuid(), keep ? "kept" : "discarded");
        AgentRun current;
        switch (run.getStatus()) {
            case PAUSED, STOPPED_KEPT, DONE -> {
                if (run.getStatus() == target) {
                    return run;
                }
                current = transition(run, target, null);
            }
            case STOPPING -> current = run;
            case RUNNING, IDLE, WAITING_APPROVAL, PAUSING -> current = transition(run, AgentRunStatus.STOPPING, null);
            default -> throw new IllegalStateException("A run that is " + run.getStatus() + " can't be stopped");
        }
        if (current.getStatus() == AgentRunStatus.STOPPING) {
            stopTargets.put(current.getId(), target);
            // nobody to wait for: ends here
            return hasWorker(current) ? current : endStop(current);
        }
        return current;
    }

    // where a stop that waits for its worker ends, see getStopTarget
    private final Map<Long, AgentRunStatus> stopTargets = new ConcurrentHashMap<>();

    @Override
    public AgentRunStatus getStopTarget(AgentRun run) {
        return run == null || run.getId() == null ? AgentRunStatus.STOPPED_KEPT
                : stopTargets.getOrDefault(run.getId(), AgentRunStatus.STOPPED_KEPT);
    }

    @Override
    @CommonTx
    public AgentRun endStop(AgentRun run) throws Exception {
        if (run == null || run.getId() == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        AgentRunStatus target = getStopTarget(run);
        AgentRun ended = transition(run, target, null);
        stopTargets.remove(run.getId());
        return ended;
    }

    // ids of the runs a runner is busy with, kept in memory only: a restart of the server ends them all
    private final Set<Long> workers = ConcurrentHashMap.newKeySet();

    @Override
    public void workerStarted(AgentRun run) {
        if (run != null && run.getId() != null) {
            workers.add(run.getId());
        }
    }

    @Override
    public void workerFinished(AgentRun run) {
        if (run != null && run.getId() != null) {
            workers.remove(run.getId());
            stopTargets.remove(run.getId());
        }
    }

    private boolean hasWorker(AgentRun run) {
        return workers.contains(run.getId());
    }

    @Override
    @CommonTxReadOnly
    public AgentRun findLockingRun(Manuscript manuscript) throws Exception {
        return manuscript == null || manuscript.getId() == null ? null
                : getRepository().findByManuscript(manuscript, AgentRunStatus.locking());
    }

    @Override
    @CommonTxReadOnly
    public void checkNotLocked(Manuscript manuscript) throws Exception {
        AgentRun locking = findLockingRun(manuscript);
        if (locking != null) {
            throw new ManuscriptLockedException(locking.getAgent().getName());
        }
    }

    @Override
    @CommonTxReadOnly
    public List<AgentRun> findForAgent(Agent agent) throws Exception {
        Agent stored = agent == null ? null : agentService.findForUser(agent.getUuid());
        return stored == null ? List.of() : getRepository().findAllByAgent(stored);
    }

    @Override
    @CommonTxReadOnly
    public AgentRun findUnfinished(Agent agent) throws Exception {
        Agent stored = agent == null ? null : agentService.findForUser(agent.getUuid());
        return stored == null ? null : getRepository().findByAgent(stored, AgentRunStatus.unfinished());
    }

    @Override
    @CommonTx
    public AgentRun transition(AgentRun run, AgentRunStatus to, String reason) throws Exception {
        if (run == null || run.getId() == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        AgentRunStatus from = run.getStatus();
        if (!from.canChangeTo(to)) {
            throw new IllegalStateException("A run can't change from " + from + " to " + to);
        }
        // the change comes first, before anything is read: a transaction that reads and then writes fails at once
        // when another one wrote in between (SQLite), one that starts with the write waits for its turn
        if (getRepository().updateStatus(run.getId(), currentUser.getId(), from, to, reason, endOf(run, to)) == 0) {
            throw whyNotChanged(run, from);
        }
        if (REFUSING.contains(to)) {
            approvalService.rejectAll(run.getId());
        }
        AgentRun changed = reload(run.getId());
        // what the agent said (a report, a question) is the story's business and not logged, the reason of the others is
        log.info("Agent run {} of agent \"{}\": {} -> {} (round {}, {} tokens){}", changed.getUuid(), changed.getAgent().getName(),
                from, to, changed.getRounds(), changed.getTotalTokens(),
                StringUtils.isNotBlank(reason) && !(to == AgentRunStatus.IDLE || to == AgentRunStatus.DONE) ? ": " + reason : "");
        // every change, not only the ones that need the user: a stop or a pause is finished by the worker some time after the
        // button was pressed, and the table of agents on every open screen has to show where it ended
        publishAfterCommit(changed);
        return COPY_ENDING.contains(to) ? releaseCopy(changed) : changed;
    }

    @Override
    @CommonTxReadOnly
    public List<AgentRun> findNeedingAttention() throws Exception {
        return getRepository().findNeedingAttention(currentUser);
    }

    @Override
    @CommonTx
    public AgentRun acknowledge(AgentRun run) throws Exception {
        if (run == null || run.getId() == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        AgentRun stored = findForUser(run.getUuid());
        if (stored == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        if (getRepository().clearAttention(stored.getId(), currentUser.getId()) == 0) {
            return stored;
        }
        AgentRun looked = reload(stored.getId());
        publishAfterCommit(looked);
        return looked;
    }

    /**
     * The screens read the runs again when they hear of a change, so they hear of it when it is stored.
     */
    private void publishAfterCommit(AgentRun run) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    attentionService.publish(run);
                }
            });
        } else {
            attentionService.publish(run);
        }
    }

    @Override
    @CommonTx
    public AgentRun addUsage(AgentRun run, long prefillTokens, long generatedTokens) throws Exception {
        if (run == null || run.getId() == null
                || getRepository().addUsage(run.getId(), Math.max(0, prefillTokens), Math.max(0, generatedTokens)) == 0) {
            throw new IllegalArgumentException("The run does not exist");
        }
        return reload(run.getId());
    }

    @Override
    @CommonTxReadOnly
    public long getTokenBudget(AgentRun run) throws Exception {
        AgentRun stored = run == null ? null : find(run.getId());
        if (stored == null) {
            return 0;
        }
        AgentGrant grant = agentService.getGrant(stored.getOwner());
        long fromAgent = stored.getAgent().getTokenBudget() == null ? 0 : stored.getAgent().getTokenBudget();
        return smallestLimit(fromAgent, grant.runTokenBudget());
    }

    @Override
    @CommonTxReadOnly
    public long getContextCap(AgentRun run) throws Exception {
        AgentRun stored = run == null || run.getId() == null ? null : find(run.getId());
        if (stored == null) {
            return 0;
        }
        Agent agent = stored.getAgent();
        ResolvedProfile resolved = agent.getMainProfile() == null ? null : agentService.resolveProfile(agent.getMainProfile());
        return java.util.stream.Stream.of(agent.getContextCap(),
                        resolved == null ? null : resolved.profile().getContextCap(),
                        resolved == null ? null : resolved.ai().getMaxContext())
                .filter(java.util.Objects::nonNull).mapToLong(Integer::longValue).filter(cap -> cap > 0).min().orElse(0);
    }

    @Override
    @CommonTxReadOnly
    public boolean isBudgetExceeded(AgentRun run) throws Exception {
        long budget = getTokenBudget(run);
        return budget > 0 && find(run.getId()).getTotalTokens() >= budget;
    }

    @Override
    @CommonTx
    public AgentRun delete(AgentRun run, boolean hard) throws Exception {
        AgentRun stored = run == null || run.getId() == null ? null : find(run.getId());
        if (stored != null) {
            discard(stored);
        }
        log.info("Agent run {} is deleted{}", run == null ? null : run.getUuid(), hard ? " for good" : " (to the trash)");
        AgentRun deleted = super.delete(stored != null ? stored : run, hard);
        if (hard && stored != null) {
            messageService.deleteForRun(stored, true);
            fileSystemService.deleteRunFolder(stored);
        }
        return deleted;
    }

    @Override
    @CommonTx
    public void deleteForAgent(Agent agent, boolean hard) throws Exception {
        for (AgentRun run : getRepository().findAllByAgent(agent)) {
            delete(run, hard);
        }
    }

    @Override
    @CommonTxReadOnly
    public AgentRequestAttributes openSession(AgentRun run) throws Exception {
        AgentRun stored = run == null || run.getId() == null ? null : find(run.getId());
        if (stored == null || stored.getOwner() == null) {
            throw new IllegalArgumentException("The run does not exist");
        }
        AgentSession session = AgentSession.of(stored);
        User user = userService.find(userService.find(session.userUuid()));
        if (user == null) {
            throw new IllegalArgumentException("The owner of the run does not exist");
        }
        // the cage: whatever the run does, it does to its copy, never to the book of the user
        Manuscript book = stored.getWorkingCopy();
        if (book == null) {
            throw new IllegalStateException("The run has no working copy");
        }
        return new AgentRequestAttributes(session, user, new AgenticSessionPoint(book));
    }

    @Override
    @CommonTx
    public int recoverInterrupted() throws Exception {
        int recovered = 0;
        for (AgentRun run : getRepository().findAllByStatus(AgentRunStatus.active())) {
            AgentRunStatus to = run.getStatus() == AgentRunStatus.STOPPING ? AgentRunStatus.STOPPED_KEPT : AgentRunStatus.PAUSED;
            if (getRepository().updateStatus(run.getId(), null, run.getStatus(), to, "Interrupted by a restart of the server", endOf(run, to)) > 0) {
                log.info("Agent run {} was {} when the server stopped, it is {} now", run.getUuid(), run.getStatus(), to);
                recovered++;
            }
        }
        return recovered;
    }

    /**
     * A run that is not over is discarded, and its working copy is deleted with it.
     */
    private void discard(AgentRun stored) throws Exception {
        if (stored.getStatus().isUnfinished()) {
            getRepository().forceStatus(stored.getId(), AgentRunStatus.STOPPED_DISCARDED, "Deleted", new Date());
            approvalService.rejectAll(stored.getId());
        }
        releaseCopy(stored);
    }

    /**
     * Takes the working copy away from the run and deletes it for good, the next run makes a new one.
     *
     * @return the run without the copy
     */
    private AgentRun releaseCopy(AgentRun run) throws Exception {
        Manuscript copy = run.getWorkingCopy();
        if (copy == null) {
            return run;
        }
        // the run lets go of the copy first, then the copy can be deleted
        getRepository().updateWorkingCopy(run.getId(), null);
        AgentRun released = reload(run.getId());
        run.setWorkingCopy(null);
        manuscriptService.deleteAgenticCopy(copy);
        return released;
    }

    /**
     * The run as the SQL updates left it: the copy this transaction may have loaded before them is stale.
     */
    private AgentRun reload(Long id) throws Exception {
        AgentRun cached = getRepository().find(id);
        if (cached != null) {
            getRepository().evict(cached);
        }
        return getRepository().find(id);
    }

    /**
     * What went wrong when the status was not changed: no such run, not the user's, or another status by now.
     */
    private RuntimeException whyNotChanged(AgentRun run, AgentRunStatus expected) throws Exception {
        AgentRun stored = find(run.getId());
        if (stored == null || stored.isDeleted()) {
            return new IllegalArgumentException("The run does not exist");
        }
        if (stored.getOwner() == null || !Objects.equals(stored.getOwner().getId(), currentUser.getId())) {
            return new SecurityException("The run belongs to another user");
        }
        return new IllegalStateException("The run is no longer " + expected + ", it is " + stored.getStatus());
    }

    /**
     * When the run ends by the change to the status: now when it stops working, kept when it only gets applied after
     * it did, none when it works.
     */
    private static Date endOf(AgentRun run, AgentRunStatus to) {
        if (FINISHING.contains(to)) {
            return new Date();
        }
        return to == AgentRunStatus.APPLIED ? run.getFinishedAt() : null;
    }

    private static long smallestLimit(long a, long b) {
        if (a <= 0) {
            return Math.max(0, b);
        }
        return b <= 0 ? a : Math.min(a, b);
    }
}
