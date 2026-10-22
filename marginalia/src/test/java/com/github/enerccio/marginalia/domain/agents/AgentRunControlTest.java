package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.security.model.User;
import com.github.enerccio.marginalia.domain.security.permissions.PermissionType;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.enerccio.marginalia.domain.collections.AgentRunStatus.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * What the Start, Pause and Stop buttons of the user do to a run: the request is finished by the service when no
 * runner is busy with the run, and left to the runner when one is.
 */
class AgentRunControlTest extends AgentTestBase {

    private User user;
    private Agent agent;

    @BeforeEach
    void setUp() throws Exception {
        user = loginAgentUser();
        agent = agent();
    }

    @Test
    void pausingARunWithoutARunnerPausesItAtOnce() throws Exception {
        AgentRun run = agentRunService.start(agent);

        AgentRun paused = agentRunService.pause(run);

        assertThat(paused.getStatus()).isEqualTo(PAUSED);
        assertThat(paused.getFinishedAt()).isNull();
    }

    @Test
    void pausingARunThatIsWorkingWaitsForItsRunner() throws Exception {
        AgentRun run = agentRunService.start(agent);
        agentRunService.workerStarted(run);

        AgentRun pausing = agentRunService.pause(run);

        assertThat(pausing.getStatus()).isEqualTo(PAUSING);
        assertThat(agentRunService.pause(pausing).getStatus()).as("asking again changes nothing").isEqualTo(PAUSING);

        // the runner reaches a stable point
        AgentRun paused = agentRunService.transition(pausing, PAUSED, null);
        agentRunService.workerFinished(paused);
        assertThat(paused.getStatus()).isEqualTo(PAUSED);
    }

    @Test
    void aRunThatWaitsForTheUserIsPausedAtOnceEvenWithARunner() throws Exception {
        AgentRun run = agentRunService.start(agent);
        agentRunService.workerStarted(run);
        run = agentRunService.transition(run, IDLE, null);

        assertThat(agentRunService.pause(run).getStatus()).isEqualTo(PAUSED);
    }

    @Test
    void stoppingKeepsOrDropsTheCopy() throws Exception {
        AgentRun kept = agentRunService.stop(agentRunService.start(agent), true);
        assertThat(kept.getStatus()).isEqualTo(STOPPED_KEPT);
        assertThat(kept.getFinishedAt()).isNotNull();

        AgentRun discarded = agentRunService.stop(kept, false);
        assertThat(discarded.getStatus()).isEqualTo(STOPPED_DISCARDED);
        assertThat(agentRunService.findUnfinished(agent)).as("the agent is free again").isNull();
        assertThatThrownBy(() -> agentRunService.stop(discarded, true)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stoppingARunWithARunnerWaitsForIt() throws Exception {
        AgentRun run = agentRunService.start(agent);
        agentRunService.workerStarted(run);

        AgentRun stopping = agentRunService.stop(run, true);

        assertThat(stopping.getStatus()).isEqualTo(STOPPING);
        assertThat(agentRunService.stop(stopping, true).getStatus()).isEqualTo(STOPPING);
        AgentRun stopped = agentRunService.transition(stopping, STOPPED_KEPT, null);
        agentRunService.workerFinished(stopped);
        assertThat(stopped.getStatus()).isEqualTo(STOPPED_KEPT);
    }

    @Test
    void aPausedRunCanBeStoppedAndAStoppedRunResumed() throws Exception {
        AgentRun paused = agentRunService.pause(agentRunService.start(agent));
        AgentRun stopped = agentRunService.stop(paused, true);
        assertThat(stopped.getStatus()).isEqualTo(STOPPED_KEPT);

        AgentRun resumed = agentRunService.resume(stopped);

        assertThat(resumed.getStatus()).isEqualTo(RUNNING);
        assertThat(resumed.getFinishedAt()).isNull();
        assertThat(agentRunService.findUnfinished(agent).getId()).isEqualTo(run(resumed));
    }

    private static Long run(AgentRun run) {
        return run.getId();
    }

    @Test
    void onlyAPausedOrStoppedRunCanBeResumed() throws Exception {
        AgentRun run = agentRunService.start(agent);

        assertThatThrownBy(() -> agentRunService.resume(run)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> agentRunService.pause(agentRunService.stop(run, false)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void resumingNeedsAFreeConcurrentRun() throws Exception {
        AgentRun first = agentRunService.pause(agentRunService.start(agent));
        agentRunService.start(agent());   // takes the only concurrent run

        assertThatThrownBy(() -> agentRunService.resume(first)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("concurrent");
    }

    @Test
    void resumingNeedsThePermission() throws Exception {
        AgentRun paused = agentRunService.pause(agentRunService.start(agent));
        setPermission(user, PermissionType.AGENTS, false);

        assertThatThrownBy(() -> agentRunService.resume(paused)).isInstanceOf(SecurityException.class);
    }

    @Test
    void anotherUserCannotControlTheRun() throws Exception {
        AgentRun run = agentRunService.start(agent);
        loginAgentUser();

        assertThatThrownBy(() -> agentRunService.pause(run)).isInstanceOf(SecurityException.class);
    }
}
