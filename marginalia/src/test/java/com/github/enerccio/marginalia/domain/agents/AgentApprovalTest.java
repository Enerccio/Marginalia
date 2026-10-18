package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.collections.DestructiveCommandMode;
import com.github.enerccio.marginalia.domain.model.impl.Agent;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentApprovalService;
import com.github.enerccio.marginalia.domain.service.AgentApprovalService.PendingApproval;
import com.github.enerccio.marginalia.domain.service.AgentMessageService;
import com.github.enerccio.marginalia.domain.service.AgentRunner;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.test.AgentTestBase;
import com.github.enerccio.marginalia.test.llm.MockLLMResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A destructive command of an agent in the {@code ASK} mode waits for the user: the run is {@code WAITING_APPROVAL}, the
 * approval is pending until the user decides, and a pause or a stop refuses it.
 */
class AgentApprovalTest extends AgentTestBase {

    private static final String CALL = "{\"search\":\"zzz\",\"replace\":\"y\"}";

    @Autowired
    private AgentRunner runner;

    @Autowired
    private AgentApprovalService approvals;

    @Autowired
    private AgentMessageService messageService;

    private Agent askingAgent() throws Exception {
        loginAgentUser();
        Agent agent = newAgent(manuscript());
        agent.setSystemPrompt("You are a test agent.");
        agent.setAllowedCommands(List.of("search-replace-parts"));
        agent.setDestructiveMode(DestructiveCommandMode.ASK);
        return agentService.save(agent);
    }

    private AgentRun startRun(Agent agent) throws Exception {
        AgentRun run = agentRunService.start(agent);
        runner.run(run);
        return run;
    }

    private AgentRun awaitStatus(AgentRun run, AgentRunStatus status, boolean idleWorker) throws Exception {
        long end = System.currentTimeMillis() + 20_000;
        while (System.currentTimeMillis() < end) {
            AgentRun current = agentRunService.find(run.getId());
            if (current.getStatus() == status && (!idleWorker || !runner.isWorking(current))) {
                return current;
            }
            Thread.sleep(25);
        }
        throw new AssertionError("The run is " + agentRunService.find(run.getId()).getStatus() + " after 20 s, wanted " + status);
    }

    private PendingApproval theOnlyPending(AgentRun run) throws Exception {
        List<PendingApproval> pending = approvals.pending(run);
        assertThat(pending).hasSize(1);
        return pending.getFirst();
    }

    private String resultOfTheCall(AgentRun run) throws Exception {
        return messageService.transcript(run).stream().filter(m -> m.getRole() == LLMRole.TOOL)
                .map(LLMChatMessage::getContent).findFirst().orElseThrow();
    }

    @Test
    void theRunWaitsForTheApprovalAndTheCommandRunsWhenItIsGiven() throws Exception {
        Agent agent = askingAgent();
        llm.enqueue(MockLLMResponse.toolCall("search-replace-parts", CALL), MockLLMResponse.text("Done."));

        AgentRun run = startRun(agent);
        awaitStatus(run, AgentRunStatus.WAITING_APPROVAL, false);

        PendingApproval pending = theOnlyPending(run);
        assertThat(pending.command()).isEqualTo("search-replace-parts");
        assertThat(pending.arguments()).containsEntry("search", "zzz").containsEntry("replace", "y");
        assertThat(approvals.hasPending(run.getId())).isTrue();
        assertThat(llm.getCompletionRequests()).as("the model waits too").hasSize(1);

        assertThat(approvals.decide(run, pending.id(), true)).isTrue();
        awaitStatus(run, AgentRunStatus.IDLE, true);

        assertThat(approvals.hasPending(run.getId())).isFalse();
        assertThat(resultOfTheCall(run)).doesNotContain("did not approve");
        assertThat(approvals.decide(run, pending.id(), true)).as("decided once").isFalse();
    }

    @Test
    void aDeniedCommandIsRefusedAndTheRunGoesOn() throws Exception {
        Agent agent = askingAgent();
        llm.enqueue(MockLLMResponse.toolCall("search-replace-parts", CALL), MockLLMResponse.text("Understood."));

        AgentRun run = startRun(agent);
        awaitStatus(run, AgentRunStatus.WAITING_APPROVAL, false);
        approvals.decide(run, theOnlyPending(run).id(), false);
        awaitStatus(run, AgentRunStatus.IDLE, true);

        assertThat(resultOfTheCall(run)).contains("did not approve");
    }

    @Test
    void aPauseRefusesTheCommandThatWaits() throws Exception {
        Agent agent = askingAgent();
        llm.enqueue(MockLLMResponse.toolCall("search-replace-parts", CALL), MockLLMResponse.text("never asked"));

        AgentRun run = startRun(agent);
        awaitStatus(run, AgentRunStatus.WAITING_APPROVAL, false);
        agentRunService.pause(agentRunService.find(run.getId()));
        awaitStatus(run, AgentRunStatus.PAUSED, true);

        assertThat(approvals.hasPending(run.getId())).isFalse();
        assertThat(resultOfTheCall(run)).contains("did not approve");
        assertThat(llm.getCompletionRequests()).as("the paused run asks nothing more").hasSize(1);
    }

    @Test
    void aStopRefusesTheCommandThatWaits() throws Exception {
        Agent agent = askingAgent();
        llm.enqueue(MockLLMResponse.toolCall("search-replace-parts", CALL), MockLLMResponse.text("never asked"));

        AgentRun run = startRun(agent);
        awaitStatus(run, AgentRunStatus.WAITING_APPROVAL, false);
        agentRunService.stop(agentRunService.find(run.getId()), false);
        awaitStatus(run, AgentRunStatus.STOPPED_DISCARDED, true);

        assertThat(approvals.hasPending(run.getId())).isFalse();
    }

    @Test
    void approvalsAreSeenAndDecidedByTheOwnerOnly() throws Exception {
        Agent agent = askingAgent();
        AgentRun run = agentRunService.start(agent);
        CompletableFuture<Boolean> decision = approvals.request(run, "search-replace-parts", Map.of("search", "a"));
        PendingApproval pending = theOnlyPending(run);

        loginAs(createUser());

        assertThatThrownBy(() -> approvals.pending(run)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> approvals.decide(run, pending.id(), true)).isInstanceOf(IllegalArgumentException.class);
        assertThat(decision).isNotDone();
        assertThat(approvals.rejectAll(run.getId())).isEqualTo(1);
        assertThat(decision.get()).isFalse();
        assertThat(approvals.hasPending(run.getId())).isFalse();
    }

    @Test
    void thePendingOnesAreOldestFirst() throws Exception {
        Agent agent = askingAgent();
        AgentRun run = agentRunService.start(agent);
        approvals.request(run, "first", Map.of());
        Thread.sleep(5);
        approvals.request(run, "second", Map.of());

        assertThat(approvals.pending(run)).extracting(PendingApproval::command).containsExactly("first", "second");
        assertThat(approvals.rejectAll(run.getId())).isEqualTo(2);
    }
}
