package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static com.github.enerccio.marginalia.domain.collections.AgentRunStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The life of a run: what it can change to, and the groups of statuses the services ask about.
 */
class AgentRunStatusTest {

    @Test
    void terminalStatusesGoNowhere() {
        for (AgentRunStatus status : AgentRunStatus.values()) {
            assertThat(status.isTerminal()).isEqualTo(status.next().isEmpty());
        }
        assertThat(EnumSet.allOf(AgentRunStatus.class)).filteredOn(AgentRunStatus::isTerminal)
                .containsExactlyInAnyOrder(STOPPED_DISCARDED, FAILED, APPLIED);
    }

    @Test
    void theUserIsToldWhenTheAgentNeedsThemOrIsOver() {
        assertThat(IDLE.needsAttention(null)).isTrue();
        assertThat(WAITING_APPROVAL.needsAttention("a-command")).isTrue();
        assertThat(DONE.needsAttention("report")).isTrue();
        assertThat(FAILED.needsAttention("broke")).isTrue();
        // a pause is theirs, unless the run had a reason of its own (a limit, a model that can't be asked, a restart)
        assertThat(PAUSED.needsAttention(null)).isFalse();
        assertThat(PAUSED.needsAttention("  ")).isFalse();
        assertThat(PAUSED.needsAttention("The step budget is used up")).isTrue();
        for (AgentRunStatus status : EnumSet.of(RUNNING, PAUSING, STOPPING, STOPPED_KEPT, STOPPED_DISCARDED, APPLIED)) {
            assertThat(status.needsAttention("reason")).as(status.name()).isFalse();
        }
    }

    @Test
    void noStatusChangesToItself() {
        for (AgentRunStatus status : AgentRunStatus.values()) {
            assertThat(status.canChangeTo(status)).as(status.name()).isFalse();
        }
    }

    @Test
    void unfinishedIsWhatIsNotTerminal() {
        assertThat(AgentRunStatus.unfinished()).containsExactlyInAnyOrder(RUNNING, IDLE, WAITING_APPROVAL, PAUSING, PAUSED,
                STOPPING, STOPPED_KEPT, DONE);
        assertThat(AgentRunStatus.unfinished()).allMatch(AgentRunStatus::isUnfinished);
    }

    @Test
    void activeRunsHaveARunnerBusy() {
        assertThat(AgentRunStatus.active()).containsExactlyInAnyOrder(RUNNING, WAITING_APPROVAL, PAUSING, STOPPING);
        assertThat(AgentRunStatus.active()).as("a run waiting for the user is stable").doesNotContain(IDLE);
    }

    @Test
    void pausedAndKeptRunsCanBeResumedButNotFinishedOnes() {
        assertThat(PAUSED.isResumable()).isTrue();
        assertThat(STOPPED_KEPT.isResumable()).isTrue();
        assertThat(PAUSED.canChangeTo(RUNNING)).isTrue();
        assertThat(STOPPED_KEPT.canChangeTo(RUNNING)).isTrue();
        assertThat(DONE.canChangeTo(RUNNING)).isFalse();
        assertThat(APPLIED.canChangeTo(RUNNING)).isFalse();
        assertThat(FAILED.canChangeTo(RUNNING)).isFalse();
    }

    @Test
    void stableStatesPauseRightAway() {
        assertThat(RUNNING.canChangeTo(PAUSED)).as("running has to reach a stable state first").isFalse();
        assertThat(RUNNING.canChangeTo(PAUSING)).isTrue();
        assertThat(PAUSING.canChangeTo(PAUSED)).isTrue();
        assertThat(IDLE.canChangeTo(PAUSED)).isTrue();
        assertThat(WAITING_APPROVAL.canChangeTo(PAUSED)).isTrue();
    }

    @Test
    void aRunIsDiscardedOnlyAfterItStopped() {
        assertThat(RUNNING.canChangeTo(STOPPED_DISCARDED)).isFalse();
        assertThat(RUNNING.canChangeTo(STOPPING)).isTrue();
        assertThat(STOPPING.canChangeTo(STOPPED_DISCARDED)).isTrue();
        assertThat(PAUSED.canChangeTo(STOPPED_DISCARDED)).isTrue();
        assertThat(DONE.canChangeTo(STOPPED_DISCARDED)).isTrue();
    }

    @Test
    void onlyAFinishedResultCanBeApplied() {
        assertThat(EnumSet.allOf(AgentRunStatus.class)).filteredOn(status -> status.canChangeTo(APPLIED))
                .containsExactlyInAnyOrder(DONE, STOPPED_KEPT);
    }
}
