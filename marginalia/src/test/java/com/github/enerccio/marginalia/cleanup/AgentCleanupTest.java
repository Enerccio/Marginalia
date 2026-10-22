package com.github.enerccio.marginalia.cleanup;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.CleanupService;
import com.github.enerccio.marginalia.domain.service.CleanupService.EntityKey;
import com.github.enerccio.marginalia.domain.service.TrashService;
import com.github.enerccio.marginalia.domain.service.TrashService.RestoreResult;
import com.github.enerccio.marginalia.test.AgentTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An agent depends on its manuscript, a run on its agent, and a run owns the working copy of the manuscript: they are
 * deleted, restored and purged together.
 */
class AgentCleanupTest extends AgentTestBase {

    @Autowired
    private TrashService trashService;

    @Autowired
    private CleanupService cleanupService;

    private Agent agent;
    private AgentRun run;
    private Manuscript copy;

    @BeforeEach
    void createRun() throws Exception {
        loginAgentUser();
        agent = agent();
        run = agentRunService.start(agent);
        copy = agentRunService.find(run.getId()).getWorkingCopy();
    }

    private static EntityKey key(Class<?> type, Long id) {
        return new EntityKey(type, id);
    }

    @Test
    void trashListsAgentsAndRuns() throws Exception {
        assertThat(trashService.getTypes()).contains(Agent.class, AgentRun.class);
    }

    @Test
    void restoringTheManuscriptRestoresItsAgentAndRuns() throws Exception {
        // the book of a run that works is locked: the user pauses the run first
        agentRunService.pause(agentRunService.find(run.getId()));
        manuscriptService.delete(manuscriptService.find(agent.getManuscript().getId()), false);
        assertThat(agentService.find(agent.getId()).isDeleted()).isTrue();

        RestoreResult result = trashService.restore(List.of(key(Manuscript.class, agent.getManuscript().getId())));

        assertThat(result.isBlocked()).isFalse();
        assertThat(result.getRestored()).contains(key(Agent.class, agent.getId()), key(AgentRun.class, run.getId()));
        assertThat(agentService.find(agent.getId()).isDeleted()).isFalse();
        AgentRun restored = agentRunService.find(run.getId());
        assertThat(restored.isDeleted()).isFalse();
        assertThat(restored.getStatus()).as("a run deleted while it worked stays discarded").isEqualTo(AgentRunStatus.STOPPED_DISCARDED);
        assertThat(agentRunService.start(agentService.find(agent.getId())).getId()).as("the agent is free again").isNotNull();
    }

    @Test
    void runOfADeletedAgentIsNotRestoredAlone() throws Exception {
        agentService.delete(agentService.find(agent.getId()), false);

        RestoreResult result = trashService.restore(List.of(key(AgentRun.class, run.getId())));

        assertThat(result.isBlocked()).isTrue();
        assertThat(agentRunService.find(run.getId()).isDeleted()).isTrue();
    }

    @Test
    void cleanupPurgesAgentRunsAndTheWorkingCopyTogether() throws Exception {
        // the book of a run that works is locked: the user pauses the run first
        agentRunService.pause(agentRunService.find(run.getId()));
        manuscriptService.delete(manuscriptService.find(agent.getManuscript().getId()), false);

        loginAdmin();
        cleanupService.purge();

        assertThat(manuscriptService.find(agent.getManuscript().getId())).isNull();
        assertThat(agentService.find(agent.getId())).isNull();
        assertThat(agentRunService.find(run.getId())).isNull();
        assertThat(manuscriptService.find(copy.getId())).isNull();
    }

    @Test
    void cleanupKeepsTheAgentAndTheManuscriptWhenOnlyTheRunWasDeleted() throws Exception {
        agentRunService.delete(agentRunService.find(run.getId()), false);

        loginAdmin();
        cleanupService.purge();

        assertThat(agentRunService.find(run.getId())).isNull();
        assertThat(manuscriptService.find(copy.getId())).as("the copy is deleted for good with the run").isNull();
        assertThat(agentService.find(agent.getId())).isNotNull();
        assertThat(manuscriptService.find(agent.getManuscript().getId())).isNotNull();
    }
}
