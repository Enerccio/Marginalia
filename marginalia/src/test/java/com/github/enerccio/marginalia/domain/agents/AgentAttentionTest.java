package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.domain.service.AgentAttentionService;
import com.github.enerccio.marginalia.domain.service.AgentAttentionService.Notice;
import com.github.enerccio.marginalia.domain.service.impl.AgentAttentionServiceImpl;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes;
import com.github.enerccio.marginalia.ui.components.ThreadCopyRequestAttributes.InRequestScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.AopTestUtils;
import org.springframework.web.context.request.RequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.github.enerccio.marginalia.domain.collections.AgentRunStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * An agent that needs the user (a question, a message to wait for, an approval, finished, failed, paused by itself) is
 * stored on its run, so it is there when the user comes back, and pushed to the screens that are open.
 */
class AgentAttentionTest extends AgentTestBase {

    @Autowired
    private AgentAttentionService attention;

    private User user;
    private Agent agent;
    private final List<Notice> heard = new CopyOnWriteArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        user = loginAgentUser();
        agent = agent();
        attention.subscribe(user.getId(), heard::add);
    }

    /**
     * The listeners are called on a thread of the service, after the change committed; waits for what was published.
     */
    private void settle() throws Exception {
        ((AgentAttentionServiceImpl) AopTestUtils.getUltimateTargetObject(attention)).drain();
    }

    @Test
    void aRunThatWorksNeedsNobody() throws Exception {
        AgentRun run = agentRunService.start(agent);

        assertThat(run.isAttention()).isFalse();
        assertThat(agentRunService.findNeedingAttention()).isEmpty();
        // the start is heard as a change, nobody is told
        settle();
        assertThat(heard).extracting(Notice::status, Notice::attention).containsExactly(org.assertj.core.api.Assertions.tuple(RUNNING, false));
    }

    @Test
    void everyChangeOfARunIsHeardSoTheTablesStayUpToDate() throws Exception {
        AgentRun run = agentRunService.start(agent);

        agentRunService.stop(run, false);

        // the stop of a run nobody works on goes through STOPPING and ends at once
        settle();
        assertThat(heard).extracting(Notice::status).containsExactly(RUNNING, STOPPING, STOPPED_DISCARDED);
        settle();
        assertThat(heard).extracting(Notice::attention).containsOnly(false);
    }

    @Test
    void theAgentThatWaitsForTheUserIsStoredAndPushed() throws Exception {
        AgentRun run = agentRunService.start(agent);
        settle();
        heard.clear();

        AgentRun idle = agentRunService.transition(run, IDLE, "Which chapter first?");

        assertThat(idle.isAttention()).isTrue();
        assertThat(agentRunService.findNeedingAttention()).extracting(AgentRun::getId).containsExactly(run.getId());
        settle();
        assertThat(heard).hasSize(1);
        Notice notice = heard.getFirst();
        assertThat(notice.userId()).isEqualTo(user.getId());
        assertThat(notice.runUuid()).isEqualTo(run.getUuid());
        assertThat(notice.agentUuid()).isEqualTo(agent.getUuid());
        assertThat(notice.agentName()).isEqualTo(agent.getName());
        assertThat(notice.status()).isEqualTo(IDLE);
        assertThat(notice.reason()).isEqualTo("Which chapter first?");
        assertThat(notice.attention()).isTrue();
    }

    @Test
    void approvalFinishFailureAndAPauseOfTheRunsOwnAreAllNews() throws Exception {
        updatePermission(user, PermissionType.AGENTS, permission -> permission.setAgentsMaxConcurrentRuns(10));
        for (AgentRunStatus status : List.of(WAITING_APPROVAL, DONE, FAILED)) {
            Agent another = agent();
            AgentRun run = agentRunService.start(another);
            agentRunService.transition(run, status, "why");
            assertThat(agentRunService.find(run.getId()).isAttention()).as(status.name()).isTrue();
        }
        Agent paused = agent();
        agentRunService.transition(agentRunService.start(paused), PAUSING, "Token budget used up");
        AgentRun limit = agentRunService.transition(agentRunService.findUnfinished(paused), PAUSED, "Token budget used up");

        assertThat(limit.isAttention()).isTrue();
        assertThat(agentRunService.findNeedingAttention()).hasSize(4);
    }

    @Test
    void aPauseTheUserAskedForIsNotNews() throws Exception {
        AgentRun run = agentRunService.start(agent);
        settle();
        heard.clear();

        AgentRun paused = agentRunService.pause(run);

        assertThat(paused.getStatus()).isEqualTo(PAUSED);
        assertThat(paused.isAttention()).isFalse();
        // heard as changes, not as news: the tables of agents show them, nobody is told
        settle();
        assertThat(heard).extracting(Notice::status, Notice::attention)
                .containsExactly(org.assertj.core.api.Assertions.tuple(PAUSING, false), org.assertj.core.api.Assertions.tuple(PAUSED, false));
    }

    @Test
    void goingOnClearsTheNews() throws Exception {
        AgentRun run = agentRunService.start(agent);
        agentRunService.transition(run, IDLE, null);
        assertThat(agentRunService.findNeedingAttention()).hasSize(1);

        AgentRun working = agentRunService.transition(agentRunService.find(run.getId()), RUNNING, null);

        assertThat(working.isAttention()).isFalse();
        assertThat(agentRunService.findNeedingAttention()).isEmpty();
    }

    @Test
    void lookingAtTheRunAcknowledgesItAndTheOtherScreensCountAgain() throws Exception {
        AgentRun run = agentRunService.start(agent);
        agentRunService.transition(run, DONE, "report");
        settle();
        heard.clear();

        AgentRun looked = agentRunService.acknowledge(agentRunService.find(run.getId()));

        assertThat(looked.isAttention()).isFalse();
        assertThat(looked.getStatus()).isEqualTo(DONE);
        assertThat(agentRunService.findNeedingAttention()).isEmpty();
        settle();
        assertThat(heard).extracting(Notice::attention).containsExactly(false);
        // nothing to look at twice
        settle();
        heard.clear();
        agentRunService.acknowledge(looked);
        settle();
        assertThat(heard).isEmpty();
    }

    @Test
    void theNewsOfAnotherUserIsNotMine() throws Exception {
        AgentRun run = agentRunService.start(agent);
        agentRunService.transition(run, IDLE, null);

        loginAgentUser();

        assertThat(agentRunService.findNeedingAttention()).isEmpty();
        assertThatThrownBy(() -> agentRunService.acknowledge(run)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aRunDeletedForGoodIsNoNews() throws Exception {
        AgentRun run = agentRunService.start(agent);
        agentRunService.transition(run, IDLE, null);

        agentRunService.delete(agentRunService.find(run.getId()), false);

        assertThat(agentRunService.findNeedingAttention()).isEmpty();
    }

    @Test
    void listenersHearTheirUserOnlyAndCanLeave() throws Exception {
        List<Notice> others = new ArrayList<>();
        Runnable leave = attention.subscribe(user.getId() + 1000, others::add);
        List<Notice> mine = new ArrayList<>();
        Runnable leaveMine = attention.subscribe(user.getId(), mine::add);

        AgentRun run = agentRunService.start(agent);
        agentRunService.transition(run, IDLE, null);
        settle();
        assertThat(others).isEmpty();
        settle();
        assertThat(mine).extracting(Notice::status).containsExactly(RUNNING, IDLE);

        leaveMine.run();
        leave.run();
        agentRunService.transition(agentRunService.find(run.getId()), RUNNING, null);
        agentRunService.transition(agentRunService.find(run.getId()), DONE, "done");
        settle();
        assertThat(mine).hasSize(2);
    }

    @Test
    void aListenerThatBreaksDoesNotKeepTheOthersFromHearing() throws Exception {
        attention.subscribe(user.getId(), notice -> {
            throw new IllegalStateException("a screen that is gone");
        });
        AgentRun run = agentRunService.start(agent);

        settle();
        heard.clear();
        agentRunService.transition(run, IDLE, null);

        settle();
        assertThat(heard).hasSize(1);
    }

    @Test
    void aScreenCanReadTheDatabaseWhenItHearsOfAChange() throws Exception {
        // what the Agents tab does: the notice comes from the commit of the change, the screen reads the runs at once
        RequestAttributes attributes = ThreadCopyRequestAttributes.create();
        List<Integer> counts = new CopyOnWriteArrayList<>();
        List<Throwable> failures = new CopyOnWriteArrayList<>();
        attention.subscribe(user.getId(), notice -> {
            try (InRequestScope _ = new InRequestScope(attributes)) {
                counts.add(agentRunService.findNeedingAttention().size());
            } catch (Throwable e) {
                failures.add(e);
            }
        });
        AgentRun run = agentRunService.start(agent);

        agentRunService.transition(run, IDLE, "Which chapter?");
        settle();

        assertThat(failures).isEmpty();
        assertThat(counts).containsExactly(0, 1);
    }
}
