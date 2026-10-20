package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.collections.DestructiveCommandMode;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentRunner;
import com.github.enerccio.marginalia.domain.service.agent.AgentDiagnostics.Report;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The diagnostics of a run that really worked: counted from what the runner stored.
 */
class AgentRunDiagnosticsTest extends AgentTestBase {

    @Autowired
    private AgentRunner runner;

    private AgentRun finishedTurn() throws Exception {
        loginAgentUser();
        Agent agent = newAgent(manuscript());
        agent.setAllowedCommands(List.of("get-book-info"));
        agent.setDestructiveMode(DestructiveCommandMode.DENY);
        agent = agentService.save(agent);
        llm.enqueue(MockLLMResponse.toolCall("get-book-info", "{}"), MockLLMResponse.text("The book is counted."));
        AgentRun run = agentRunService.start(agent);
        runner.run(run);
        long end = System.currentTimeMillis() + 20_000;
        while (System.currentTimeMillis() < end) {
            AgentRun current = agentRunService.find(run.getId());
            if (current.getStatus() == AgentRunStatus.IDLE && !runner.isWorking(current)) {
                return current;
            }
            Thread.sleep(25);
        }
        throw new AssertionError("The run did not end its turn");
    }

    @Test
    void theReportCountsWhatTheRunnerStored() throws Exception {
        AgentRun run = finishedTurn();

        Report report = runner.diagnostics(run);

        assertThat(report.agentName()).isEqualTo(run.getAgent().getName());
        assertThat(report.status()).isEqualTo(AgentRunStatus.IDLE);
        assertThat(report.overview().rounds()).isEqualTo(2);
        assertThat(report.overview().textAnswers()).isEqualTo(1);
        assertThat(report.overview().calls()).isEqualTo(1);
        assertThat(report.overview().authorMessages()).isEqualTo(1);
        assertThat(report.overview().peakContext()).isPositive();
        assertThat(report.tools()).singleElement().satisfies(tool -> {
            assertThat(tool.name()).isEqualTo("get-book-info");
            assertThat(tool.calls()).isEqualTo(1);
            assertThat(tool.errors()).isZero();
            assertThat(tool.resultChars()).isPositive();
        });
        assertThat(report.toMarkdown()).contains("get-book-info");
    }

    @Test
    void aRunThatDoesNotExistHasNoDiagnostics() throws Exception {
        AgentRun run = finishedTurn();
        AgentRun other = new AgentRun();
        other.setUuid("no-such-run");
        assertThat(run).isNotNull();

        assertThatThrownBy(() -> runner.diagnostics(other)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> runner.diagnostics(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
