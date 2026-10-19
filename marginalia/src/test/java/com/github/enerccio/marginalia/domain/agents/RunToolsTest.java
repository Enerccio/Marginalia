package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.service.agent.RunTools.PlanItem;
import com.github.enerccio.marginalia.domain.service.agent.RunToolsImpl;
import com.github.enerccio.marginalia.domain.service.function.FunctionContinuation;
import com.github.enerccio.marginalia.domain.service.impl.AgentRunnerImpl;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The run tools note what the model asked for, the runner acts on it after the round.
 */
class RunToolsTest {

    /**
     * The answer of a tool, completed or failed.
     */
    private static final class Answer implements FunctionContinuation<String> {
        String result;
        String failure;

        @Override
        public void complete(String result) {
            this.result = result;
        }

        @Override
        public void fail(String message) {
            this.failure = message;
        }

        @Override
        public void fail(Throwable error) {
            this.failure = error.getMessage();
        }

        @Override
        public boolean isCancelled() {
            return false;
        }
    }

    private final RunToolsImpl tools = new RunToolsImpl(() -> "Rounds: 3");

    @Test
    void requestCompactionNotesTheFocusUntilTheRunnerTakesIt() {
        assertThat(tools.isCompactionRequested()).isFalse();

        Answer asked = new Answer();
        tools.requestCompaction(asked, "  the chapters ");
        assertThat(asked.result).isEqualTo(RunToolsImpl.COMPACTION_ACK);
        assertThat(tools.isCompactionRequested()).isTrue();
        assertThat(tools.getCompactionFocus()).isEqualTo("the chapters");

        tools.clearRequests();
        assertThat(tools.isCompactionRequested()).isFalse();

        tools.requestCompaction(new Answer(), null);
        assertThat(tools.isCompactionRequested()).as("a focus is not required").isTrue();
        assertThat(tools.getCompactionFocus()).isEmpty();
    }

    @Test
    void finishKeepsTheReportAndNeedsOne() {
        Answer empty = new Answer();
        tools.finish(empty, "  ");
        assertThat(empty.failure).contains("report");
        assertThat(tools.getSummary()).isNull();

        Answer done = new Answer();
        tools.finish(done, " All checked. ");
        assertThat(tools.getSummary()).isEqualTo("All checked.");
        assertThat(done.result).contains("ends");

        tools.clearRequests();
        assertThat(tools.getSummary()).isNull();
    }

    @Test
    void askUserKeepsTheQuestionWithItsOptions() {
        Answer answer = new Answer();
        tools.askUser(answer, "Which tone?", List.of("dark", " ", "light"));

        assertThat(tools.getQuestion()).isEqualTo("Which tone?\n\nSuggested answers:\n- dark\n- light");
        assertThat(answer.result).contains("waits");
    }

    @Test
    void thePlanIsCheckedAndStatusesAreNormalized() {
        Answer answer = new Answer();
        tools.updatePlan(answer, List.of(new PlanItem("Read", "In Progress"), new PlanItem("Write", "completed"),
                new PlanItem("Check", "whatever")));

        assertThat(tools.getPlan()).extracting(PlanItem::status).containsExactly("in_progress", "done", "pending");
        assertThat(answer.result).contains("1. [~] Read").contains("2. [x] Write").contains("3. [ ] Check");

        Answer none = new Answer();
        tools.updatePlan(none, List.of());
        assertThat(none.failure).contains("at least one");
        Answer blank = new Answer();
        tools.updatePlan(blank, List.of(new PlanItem(" ", "done")));
        assertThat(blank.failure).contains("text");
        Answer many = new Answer();
        List<PlanItem> items = new ArrayList<>();
        for (int i = 0; i <= RunToolsImpl.MAX_PLAN_ITEMS; i++) {
            items.add(new PlanItem("step " + i, "pending"));
        }
        tools.updatePlan(many, items);
        assertThat(many.failure).contains("or fewer");
        // a refused plan changes nothing
        assertThat(tools.getPlan()).hasSize(3);
    }

    @Test
    void theStatusShowsThePlan() {
        tools.updatePlan(new Answer(), List.of(new PlanItem("Read", "pending")));
        Answer answer = new Answer();

        tools.getRunStatus(answer);

        assertThat(answer.result).isEqualTo("Rounds: 3\n\nPlan:\n1. [ ] Read");
    }

    @Test
    void thePlanOfTheLastUpdateIsReadBackFromTheTranscript() {
        List<LLMChatMessage> transcript = List.of(
                LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of("a", "update_plan", "{\"items\":[{\"text\":\"Old\",\"status\":\"done\"}]}"))),
                LLMChatMessage.ofToolResult("a", "Plan saved."),
                LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of("b", "update_plan",
                        "{\"items\":[{\"text\":\"New\",\"status\":\"in_progress\"},{\"text\":\"Later\"}]}"))),
                LLMChatMessage.ofToolResult("b", "Plan saved."),
                LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of("c", "update_plan", "not json"))),
                LLMChatMessage.ofToolResult("c", "Error: bad arguments"));

        // the last call was refused, the one before it stands
        assertThat(AgentRunnerImpl.planOf(transcript)).containsExactly(new PlanItem("New", "in_progress"), new PlanItem("Later", "pending"));
        assertThat(AgentRunnerImpl.planOf(List.of())).isEmpty();
    }
}
