package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.collections.Language;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.ManuscriptLockedException;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.enerccio.marginalia.domain.collections.AgentRunStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Starting runs, the changes of their status, the tokens they spend and what happens to them when they are deleted or
 * the server restarts.
 */
class AgentRunServiceTest extends AgentTestBase {

    @Autowired
    private JdbcTemplate jdbc;

    private User user;
    private Agent agent;

    @BeforeEach
    void createAgent() throws Exception {
        user = loginAgentUser();
        agent = agent();
    }

    private AgentRun reload(AgentRun run) throws Exception {
        return agentRunService.find(run.getId());
    }

    @Test
    void startCreatesARunningRun() throws Exception {
        Date before = new Date(System.currentTimeMillis() - 1000);

        AgentRun run = agentRunService.start(agent);

        AgentRun loaded = reload(run);
        assertThat(loaded.getAgent().getId()).isEqualTo(agent.getId());
        assertThat(loaded.getOwner().getId()).isEqualTo(user.getId());
        assertThat(loaded.getStatus()).isEqualTo(RUNNING);
        assertThat(loaded.getStartedAt()).isAfter(before);
        assertThat(loaded.getFinishedAt()).isNull();
        assertThat(loaded.getLanguage()).isEqualTo(agent.getManuscript().getLanguage());
        assertThat(loaded.getTotalTokens()).isZero();
        assertThat(loaded.getRounds()).isZero();
        assertThat(loaded.getWorkingCopy()).as("every run works on a copy of its own").isNotNull();
        assertThat(loaded.getWorkingCopy().isAgentic()).isTrue();
        assertThat(loaded.getWorkingCopy().getId()).isNotEqualTo(agent.getManuscript().getId());
        assertThat(agentRunService.findUnfinished(agent).getId()).isEqualTo(run.getId());
        assertThat(agentRunService.findForAgent(agent)).extracting(AgentRun::getId).containsExactly(run.getId());
    }

    @Test
    void runUsesTheLanguageOfTheBook() throws Exception {
        Manuscript manuscript = manuscript();
        manuscript.setLanguage(Language.JAPANESE);
        manuscript = manuscriptService.save(manuscript);
        Agent japanese = agentService.save(newAgent(manuscript));

        assertThat(reload(agentRunService.start(japanese)).getLanguage()).isEqualTo(Language.JAPANESE);
    }

    @Test
    void startNeedsThePermission() throws Exception {
        setPermission(user, PermissionType.AGENTS, false);

        assertThatThrownBy(() -> agentRunService.start(agent)).isInstanceOf(SecurityException.class);
    }

    @Test
    void startNeedsAnEnabledAgentWithAUsableModel() throws Exception {
        Agent disabled = agentService.find(agent.getId());
        disabled.setEnabled(false);
        agentService.save(disabled);
        assertThatThrownBy(() -> agentRunService.start(agent)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("disabled");

        Agent noModel = agentService.save(newAgent(manuscript()));
        noModel.getModelProfiles().clear();
        agentService.save(noModel);
        assertThatThrownBy(() -> agentRunService.start(noModel)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no model");

        Agent badModel = newAgent(manuscript());
        badModel.getMainProfile().setAiUuid("missing");
        badModel = agentService.save(badModel);
        Agent saved = badModel;
        assertThatThrownBy(() -> agentRunService.start(saved)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void startOnlyAnAgentOfTheUser() throws Exception {
        loginAgentUser();

        assertThatThrownBy(() -> agentRunService.start(agent)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> agentRunService.start(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void agentHasOneUnfinishedRun() throws Exception {
        AgentRun first = agentRunService.start(agent);

        assertThatThrownBy(() -> agentRunService.start(agent)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unfinished");

        // every status that is not over blocks the agent, the finished result waiting for the review too
        AgentRun run = first;
        for (AgentRunStatus status : List.of(IDLE, PAUSED, RUNNING, STOPPING, STOPPED_KEPT, RUNNING, DONE)) {
            run = agentRunService.transition(run, status, null);
            assertThat(agentRunService.findUnfinished(agent)).as(status.name()).isNotNull();
            assertThatThrownBy(() -> agentRunService.start(agent)).as(status.name()).isInstanceOf(IllegalStateException.class);
        }

        agentRunService.transition(run, APPLIED, null);
        assertThat(agentRunService.findUnfinished(agent)).isNull();
        AgentRun second = agentRunService.start(agent);
        assertThat(agentRunService.findForAgent(agent)).extracting(AgentRun::getId).containsExactly(second.getId(), first.getId());
    }

    @Test
    void databaseKeepsOneUnfinishedRunPerAgentToo() throws Exception {
        agentRunService.start(agent);
        AgentRun second = new AgentRun();
        second.setAgent(agent);
        second.setStatus(PAUSED);

        assertThatThrownBy(() -> agentRunService.save(second)).isInstanceOf(Exception.class);

        AgentRun finished = new AgentRun();
        finished.setAgent(agent);
        finished.setStatus(FAILED);
        assertThat(agentRunService.save(finished).getId()).as("a finished run is not unfinished").isNotNull();
    }

    @Test
    void parallelStartsMakeOneRun() throws Exception {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        int threads = 4;
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger started = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            List<Future<?>> futures = new java.util.ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    RequestContextHolder.setRequestAttributes(attributes);
                    go.await();
                    try {
                        agentRunService.start(agent);
                        started.incrementAndGet();
                    } catch (Exception expected) {
                        // lost the race
                    }
                    return null;
                }));
            }
            go.countDown();
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            executor.shutdownNow();
        }

        assertThat(started.get()).isEqualTo(1);
        assertThat(agentRunService.findForAgent(agent)).hasSize(1);
    }

    @Test
    void concurrentRunsAreLimitedByTheGrant() throws Exception {
        Agent second = agentService.save(newAgent(manuscript()));
        agentRunService.start(agent);

        assertThatThrownBy(() -> agentRunService.start(second)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("concurrent");

        updatePermission(user, PermissionType.AGENTS, permission -> permission.setAgentsMaxConcurrentRuns(2));
        assertThat(agentRunService.start(second).getId()).isNotNull();
    }

    @Test
    void runWaitingForTheUserDoesNotCountAsConcurrent() throws Exception {
        Agent second = agentService.save(newAgent(manuscript()));
        AgentRun run = agentRunService.start(agent);
        agentRunService.transition(run, IDLE, null);

        assertThat(agentRunService.start(second).getId()).isNotNull();
    }

    @Test
    void transitionFollowsTheAllowedChanges() throws Exception {
        AgentRun run = agentRunService.start(agent);

        AgentRun paused = agentRunService.transition(agentRunService.transition(run, PAUSING, "asked"), PAUSED, "paused by the user");

        assertThat(paused.getStatus()).isEqualTo(PAUSED);
        assertThat(paused.getStatusReason()).isEqualTo("paused by the user");
        assertThat(reload(run).getStatus()).isEqualTo(PAUSED);
        assertThatThrownBy(() -> agentRunService.transition(paused, DONE, null)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PAUSED to DONE");
        assertThat(reload(run).getStatus()).isEqualTo(PAUSED);
    }

    @Test
    void transitionRefusesAStaleRun() throws Exception {
        AgentRun run = agentRunService.start(agent);
        agentRunService.transition(run, PAUSING, null);

        // the runner still thinks it runs
        assertThatThrownBy(() -> agentRunService.transition(run, IDLE, null)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no longer RUNNING");
        assertThat(reload(run).getStatus()).isEqualTo(PAUSING);
    }

    @Test
    void parallelTransitionsOfTheSameRunHaveOneWinner() throws Exception {
        AgentRun run = agentRunService.start(agent);
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        int threads = 4;
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger won = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            List<Future<?>> futures = new java.util.ArrayList<>();
            for (int i = 0; i < threads; i++) {
                AgentRunStatus to = i % 2 == 0 ? IDLE : PAUSING;
                futures.add(executor.submit(() -> {
                    RequestContextHolder.setRequestAttributes(attributes);
                    go.await();
                    try {
                        agentRunService.transition(run, to, null);
                        won.incrementAndGet();
                    } catch (IllegalStateException expected) {
                        // lost
                    }
                    return null;
                }));
            }
            go.countDown();
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            executor.shutdownNow();
        }

        assertThat(won.get()).isEqualTo(1);
    }

    @Test
    void otherUsersCannotChangeARun() throws Exception {
        AgentRun run = agentRunService.start(agent);
        loginAgentUser();

        assertThatThrownBy(() -> agentRunService.transition(run, PAUSING, null)).isInstanceOf(SecurityException.class);
        assertThat(reload(run).getStatus()).isEqualTo(RUNNING);
    }

    @Test
    void runEndsWhenItStopsWorkingAndResumedRunDoesNotHaveAnEnd() throws Exception {
        AgentRun run = agentRunService.start(agent);
        assertThat(reload(run).getFinishedAt()).isNull();

        AgentRun kept = agentRunService.transition(agentRunService.transition(run, STOPPING, null), STOPPED_KEPT, "stopped");
        Date finished = kept.getFinishedAt();
        assertThat(finished).isNotNull();

        AgentRun resumed = agentRunService.transition(kept, RUNNING, null);
        assertThat(resumed.getFinishedAt()).isNull();
        assertThat(resumed.getStartedAt()).hasSameTimeAs(run.getStartedAt());

        AgentRun done = agentRunService.transition(resumed, DONE, null);
        assertThat(done.getFinishedAt()).isNotNull();
        assertThat(agentRunService.transition(done, APPLIED, null).getFinishedAt()).as("kept when applied")
                .hasSameTimeAs(done.getFinishedAt());
    }

    @Test
    void failedRunKeepsTheReason() throws Exception {
        AgentRun run = agentRunService.start(agent);

        AgentRun failed = agentRunService.transition(run, FAILED, "The model did not answer");

        assertThat(failed.getStatusReason()).isEqualTo("The model did not answer");
        assertThat(failed.getFinishedAt()).isNotNull();
        assertThat(agentRunService.findUnfinished(agent)).isNull();
    }

    @Test
    void usageIsAddedUp() throws Exception {
        AgentRun run = agentRunService.start(agent);

        agentRunService.addUsage(run, 1000, 200);
        AgentRun after = agentRunService.addUsage(run, 1500, 300);

        assertThat(after.getPrefillTokens()).isEqualTo(2500);
        assertThat(after.getGeneratedTokens()).isEqualTo(500);
        assertThat(after.getTotalTokens()).isEqualTo(3000);
        assertThat(after.getRounds()).isEqualTo(2);
        assertThat(reload(run).getTotalTokens()).isEqualTo(3000);
        assertThatThrownBy(() -> agentRunService.addUsage(null, 1, 1)).isInstanceOf(IllegalArgumentException.class);
        assertThat(agentRunService.addUsage(run, -5, -5).getTotalTokens()).as("never subtracts").isEqualTo(3000);
    }

    @Test
    void parallelUsageLosesNothing() throws Exception {
        AgentRun run = agentRunService.start(agent);
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        int threads = 4;
        int perThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            List<Future<?>> futures = new java.util.ArrayList<>();
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    RequestContextHolder.setRequestAttributes(attributes);
                    for (int j = 0; j < perThread; j++) {
                        agentRunService.addUsage(run, 10, 1);
                    }
                    return null;
                }));
            }
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            executor.shutdownNow();
        }

        AgentRun loaded = reload(run);
        assertThat(loaded.getPrefillTokens()).isEqualTo(threads * perThread * 10L);
        assertThat(loaded.getGeneratedTokens()).isEqualTo(threads * perThread);
        assertThat(loaded.getRounds()).isEqualTo(threads * perThread);
    }

    @Test
    void savingAStaleRunDoesNotWriteBackItsStatusAndTokens() throws Exception {
        AgentRun stale = agentRunService.start(agent);
        AgentRun moved = agentRunService.transition(stale, IDLE, "waiting");
        agentRunService.addUsage(moved, 700, 70);

        stale.getUuidPairs().put("copy", "original");
        agentRunService.save(stale);

        AgentRun loaded = reload(stale);
        assertThat(loaded.getStatus()).isEqualTo(IDLE);
        assertThat(loaded.getStatusReason()).isEqualTo("waiting");
        assertThat(loaded.getTotalTokens()).isEqualTo(770);
        assertThat(loaded.getUuidPairs()).containsKey("copy");
    }

    @Test
    void budgetIsTheSmallerOfTheAgentAndTheGrant() throws Exception {
        AgentRun run = agentRunService.start(agent);
        assertThat(agentRunService.getTokenBudget(run)).as("no limits").isZero();
        assertThat(agentRunService.isBudgetExceeded(run)).isFalse();

        updatePermission(user, PermissionType.AGENTS, permission -> permission.setAgentsRunTokenBudget(5000L));
        assertThat(agentRunService.getTokenBudget(run)).as("the grant only").isEqualTo(5000);

        Agent limited = agentService.find(agent.getId());
        limited.setTokenBudget(8000L);
        agentService.save(limited);
        assertThat(agentRunService.getTokenBudget(run)).isEqualTo(5000);

        limited = agentService.find(agent.getId());
        limited.setTokenBudget(3000L);
        agentService.save(limited);
        assertThat(agentRunService.getTokenBudget(run)).isEqualTo(3000);

        agentRunService.addUsage(run, 2000, 999);
        assertThat(agentRunService.isBudgetExceeded(run)).isFalse();
        agentRunService.addUsage(run, 1, 0);
        assertThat(agentRunService.isBudgetExceeded(run)).isTrue();
    }

    @Test
    void budgetOfTheAgentApplyToAnAdministrator() throws Exception {
        loginAdmin();
        Agent admins = agent();
        admins.setTokenBudget(100L);
        agentService.save(admins);
        AgentRun run = agentRunService.start(admins);

        assertThat(agentRunService.getTokenBudget(run)).isEqualTo(100);
    }

    @Test
    void deletedRunIsDiscardedAndFreesTheAgent() throws Exception {
        AgentRun run = agentRunService.start(agent);
        agentRunService.addUsage(run, 10, 1);

        agentRunService.delete(run, false);

        AgentRun loaded = reload(run);
        assertThat(loaded.isDeleted()).isTrue();
        assertThat(loaded.getStatus()).isEqualTo(STOPPED_DISCARDED);
        assertThat(loaded.getFinishedAt()).isNotNull();
        assertThat(loaded.getTotalTokens()).isEqualTo(11);
        assertThat(agentRunService.findUnfinished(agent)).isNull();
        assertThat(agentRunService.findForAgent(agent)).isEmpty();
        assertThat(agentRunService.start(agent).getId()).isNotNull();
    }

    @Test
    void deletedRunTakesItsWorkingCopyWithIt() throws Exception {
        AgentRun run = agentRunService.start(agent);
        Long copyId = reload(run).getWorkingCopy().getId();

        agentRunService.delete(reload(run), false);

        assertThat(manuscriptService.find(copyId)).as("deleted for good, not only trashed").isNull();
        assertThat(reload(run).getWorkingCopy()).isNull();
    }

    @Test
    void deletingTheAgentDeletesItsRuns() throws Exception {
        AgentRun finished = agentRunService.transition(agentRunService.start(agent), FAILED, "failed");
        AgentRun run = agentRunService.start(agent);

        agentService.delete(agentService.find(agent.getId()), false);

        assertThat(reload(run).isDeleted()).isTrue();
        assertThat(reload(run).getStatus()).isEqualTo(STOPPED_DISCARDED);
        assertThat(reload(finished).isDeleted()).isTrue();
        assertThat(reload(finished).getStatus()).isEqualTo(FAILED);
    }

    @Test
    void deletingTheManuscriptDeletesItsAgentAndTheirRuns() throws Exception {
        AgentRun run = agentRunService.start(agent);
        Manuscript book = manuscriptService.find(agent.getManuscript().getId());

        // a book an agent works on is not deleted from under it
        assertThatThrownBy(() -> manuscriptService.delete(book, false)).isInstanceOf(ManuscriptLockedException.class);
        agentRunService.pause(run);
        manuscriptService.delete(manuscriptService.find(agent.getManuscript().getId()), false);

        assertThat(agentService.find(agent.getId()).isDeleted()).isTrue();
        assertThat(reload(run).isDeleted()).isTrue();
        assertThat(reload(run).getStatus()).isEqualTo(STOPPED_DISCARDED);
    }

    @Test
    void hardDeletedRunIsGone() throws Exception {
        AgentRun run = agentRunService.start(agent);

        agentRunService.delete(reload(run), true);

        assertThat(reload(run)).isNull();
        assertThat(agentRunService.findUnfinished(agent)).isNull();
    }

    @Test
    void recoveryPausesTheRunsARunnerWasBusyWith() throws Exception {
        updatePermission(user, PermissionType.AGENTS, permission -> permission.setAgentsMaxConcurrentRuns(10));
        AgentRun running = agentRunService.start(agent);
        Agent second = agentService.save(newAgent(manuscript()));
        AgentRun stopping = agentRunService.start(second);
        stopping = agentRunService.transition(stopping, STOPPING, null);
        Agent third = agentService.save(newAgent(manuscript()));
        AgentRun idle = agentRunService.transition(agentRunService.start(third), IDLE, null);
        Agent fourth = agentService.save(newAgent(manuscript()));
        AgentRun paused = agentRunService.transition(agentRunService.transition(agentRunService.start(fourth), IDLE, null), PAUSED, null);
        Agent fifth = agentService.save(newAgent(manuscript()));
        AgentRun done = agentRunService.transition(agentRunService.start(fifth), DONE, null);

        int recovered = agentRunService.recoverInterrupted();

        assertThat(recovered).isGreaterThanOrEqualTo(2);
        assertThat(reload(running).getStatus()).isEqualTo(PAUSED);
        assertThat(reload(running).getStatusReason()).contains("restart");
        assertThat(reload(running).getFinishedAt()).isNull();
        assertThat(reload(stopping).getStatus()).isEqualTo(STOPPED_KEPT);
        assertThat(reload(stopping).getFinishedAt()).isNotNull();
        assertThat(reload(idle).getStatus()).as("waiting for the user survives a restart").isEqualTo(IDLE);
        assertThat(reload(paused).getStatus()).isEqualTo(PAUSED);
        assertThat(reload(done).getStatus()).isEqualTo(DONE);
        assertThat(agentRunService.recoverInterrupted()).as("nothing to recover the second time").isZero();
        assertThat(agentRunService.transition(reload(running), RUNNING, null).getStatus()).as("can be resumed").isEqualTo(RUNNING);
    }

    @Test
    void recoveryWorksWithoutALoggedInUser() throws Exception {
        AgentRun running = agentRunService.start(agent);
        currentUser.setId(null);

        assertThat(agentRunService.recoverInterrupted()).isGreaterThanOrEqualTo(1);

        loginAs(user);
        assertThat(reload(running).getStatus()).isEqualTo(PAUSED);
    }

    @Test
    void runsAreCountedPerUserForTheLimit() throws Exception {
        agentRunService.start(agent);
        loginAgentUser();
        Agent mine = agent();

        assertThat(agentRunService.start(mine).getId()).as("another user has their own limit").isNotNull();
    }

    // --- the quota of working copies ---

    @Test
    void theNumberOfWorkingCopiesIsLimitedByThePermissionAndAPausedRunKeepsItsCopy() throws Exception {
        updatePermission(user, PermissionType.AGENTS, permission -> permission.setAgentsMaxCopies(1));
        Agent second = agentService.save(newAgent(manuscript()));
        AgentRun first = agentRunService.start(agent);
        first = agentRunService.pause(first);
        assertThat(first.getStatus()).isEqualTo(PAUSED);

        assertThatThrownBy(() -> agentRunService.start(second)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("working copies");

        agentRunService.stop(first, false);
        assertThat(agentRunService.start(second).getWorkingCopy()).isNotNull();
    }

    @Test
    void withoutAQuotaOfCopiesOnlyTheNumberOfAgentsLimits() throws Exception {
        Agent second = agentService.save(newAgent(manuscript()));
        agentRunService.pause(agentRunService.start(agent));

        assertThat(agentRunService.start(second).getStatus()).isEqualTo(RUNNING);
    }

    @Test
    void resumingARunThatKeepsItsCopyNeedsNoNewOne() throws Exception {
        updatePermission(user, PermissionType.AGENTS, permission -> permission.setAgentsMaxCopies(1));
        AgentRun run = agentRunService.pause(agentRunService.start(agent));

        assertThat(agentRunService.resume(run).getStatus()).isEqualTo(RUNNING);
    }
}
