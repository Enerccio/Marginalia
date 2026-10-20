package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.AgentMessage;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentMessageService;
import com.github.enerccio.marginalia.domain.service.agent.AgentDiagnostics;
import com.github.enerccio.marginalia.domain.service.agent.AgentDiagnostics.Report;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What a run did, counted from the rows of its transcript.
 */
class AgentDiagnosticsTest {

    private static final long START = 1_760_000_000_000L;

    // a transcript written by hand: every row gets the time a step of the given length after the row before
    private static final class Rows {
        final List<AgentMessage> rows = new ArrayList<>();
        long now = START;
        int calls;

        AgentMessage add(String thread, LLMChatMessage message, long afterMillis) {
            now += afterMillis;
            AgentMessage row = new AgentMessage();
            row.setThread(thread);
            row.setOrdinal(rows.size());
            row.setChatMessage(message);
            row.setCreation(new Date(now));
            rows.add(row);
            return row;
        }

        AgentMessage user(String text) {
            return add(null, LLMChatMessage.of(LLMRole.USER, text), 1000);
        }

        // an answer that calls the tools, then their results one by one
        void round(long contextTokens, long modelMillis, String... tools) {
            List<LLMToolCall> toolCalls = new ArrayList<>();
            for (String tool : tools) {
                toolCalls.add(LLMToolCall.of("c" + ++calls, tool, "{\"path\":\"/n" + calls + ".md\"}"));
            }
            add(null, LLMChatMessage.ofToolCalls(null, toolCalls), modelMillis).setContextTokens(contextTokens);
        }

        void result(String callId, String result, long millis) {
            add(null, LLMChatMessage.ofToolResult(callId, result), millis);
        }

        void text(long contextTokens, long modelMillis, String text) {
            add(null, LLMChatMessage.of(LLMRole.ASSISTANT, text), modelMillis).setContextTokens(contextTokens);
        }
    }

    private static AgentRun run() {
        AgentRun run = new AgentRun();
        run.setStatus(AgentRunStatus.IDLE);
        run.setStartedAt(new Date(START));
        return run;
    }

    private static Report report(Rows rows) {
        return AgentDiagnostics.analyze("Checker", run(), rows.rows, List.of());
    }

    @Test
    void theCallsAreCountedByToolWithTheirErrorsAndTheirResults() {
        Rows rows = new Rows();
        rows.user("Check the notes");
        rows.round(1000, 2000, "read_file", "list_files");
        rows.result("c1", "x".repeat(400), 100);
        rows.result("c2", "Error: the folder /nope does not exist", 50);
        rows.round(1500, 3000, "read_file");
        rows.result("c3", "y".repeat(4000) + "\n[cut: 4000 of 9000 characters; ask for less]", 700);
        rows.text(1600, 1000, "Done.");

        Report report = report(rows);

        assertThat(report.overview().rounds()).isEqualTo(3);
        assertThat(report.overview().textAnswers()).isEqualTo(1);
        assertThat(report.overview().calls()).isEqualTo(3);
        assertThat(report.overview().errors()).isEqualTo(1);
        assertThat(report.overview().authorMessages()).isEqualTo(1);
        assertThat(report.overview().avgCallsPerRound()).isCloseTo(1.0, org.assertj.core.data.Offset.offset(0.001));
        AgentDiagnostics.ToolStat read = report.tools().stream().filter(tool -> tool.name().equals("read_file")).findFirst().orElseThrow();
        assertThat(read.calls()).isEqualTo(2);
        assertThat(read.cut()).isEqualTo(1);
        assertThat(read.soloRounds()).as("only the second round called it alone").isEqualTo(1);
        assertThat(read.maxResult()).isGreaterThan(4000);
        assertThat(read.millis()).isEqualTo(800);
        assertThat(read.maxMillis()).isEqualTo(700);
        AgentDiagnostics.ToolStat list = report.tools().stream().filter(tool -> tool.name().equals("list_files")).findFirst().orElseThrow();
        assertThat(list.errors()).isEqualTo(1);
        assertThat(list.errorRate()).isEqualTo(1.0);
        assertThat(report.errorMessages()).singleElement().satisfies(error -> {
            assertThat(error.tool()).isEqualTo("list_files");
            assertThat(error.message()).isEqualTo("the folder /nope does not exist");
        });
        assertThat(report.tools().getFirst().name()).as("the most called first").isEqualTo("read_file");
    }

    @Test
    void theTimeIsTheGapBetweenTheRowsAndAPauseIsIdle() {
        Rows rows = new Rows();
        rows.user("Go");
        rows.round(1000, 4000, "read_file");
        rows.result("c1", "text", 500);
        // the run was paused for two hours
        rows.round(1100, 2 * 60 * 60 * 1000L, "read_file");
        rows.result("c2", "text", 300);
        rows.text(1200, 6000, "Done");

        AgentDiagnostics.Overview overview = report(rows).overview();

        assertThat(overview.modelMillis()).isEqualTo(4000 + 6000);
        assertThat(overview.toolMillis()).isEqualTo(800);
        assertThat(overview.idleMillis()).isEqualTo(2 * 60 * 60 * 1000L);
        assertThat(overview.wallMillis()).isEqualTo(4000 + 500 + 2 * 60 * 60 * 1000L + 300 + 6000);
        assertThat(report(rows).slowest().getFirst().round()).as("the slowest round that is not a pause").isEqualTo(3);
    }

    @Test
    void roundsOfOneToolInARowAreASeriesAndASearchBeforeItIsNoted() {
        Rows rows = new Rows();
        rows.user("Rename Harry");
        rows.round(1000, 1000, "grep_files");
        rows.result("c1", "a.md:1: Harry", 10);
        for (int i = 0; i < 4; i++) {
            rows.round(1100 + i, 1000, "replace_in_file");
            rows.result("c" + (i + 2), "replaced", 10);
        }
        rows.round(1200, 1000, "read_file", "read_file");
        rows.result("c6", "a", 10);
        rows.result("c7", "b", 10);
        rows.round(1300, 1000, "read_file");
        rows.result("c8", "c", 10);
        rows.round(1300, 1000, "read_file");
        rows.result("c9", "d", 10);
        rows.text(1400, 1000, "Done");

        Report report = report(rows);

        assertThat(report.series()).extracting(AgentDiagnostics.Series::tool).containsExactly("replace_in_file", "read_file");
        AgentDiagnostics.Series edits = report.series().getFirst();
        assertThat(edits.firstRound()).isEqualTo(2);
        assertThat(edits.rounds()).isEqualTo(4);
        assertThat(edits.singleCalls()).isEqualTo(4);
        assertThat(edits.afterSearch()).isTrue();
        AgentDiagnostics.Series reads = report.series().get(1);
        assertThat(reads.rounds()).isEqualTo(3);
        assertThat(reads.calls()).isEqualTo(4);
        assertThat(reads.singleCalls()).isEqualTo(2);
        assertThat(reads.afterSearch()).isFalse();
    }

    @Test
    void twoRoundsOfAToolAreNoSeries() {
        Rows rows = new Rows();
        rows.user("Go");
        rows.round(1000, 1000, "read_file");
        rows.result("c1", "a", 10);
        rows.round(1100, 1000, "read_file");
        rows.result("c2", "b", 10);
        rows.text(1200, 1000, "Done");

        assertThat(report(rows).series()).isEmpty();
    }

    @Test
    void theSameCallMadeAgainIsReported() {
        Rows rows = new Rows();
        rows.user("Go");
        rows.add(null, LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of("a", "get-part", "{\"position\": 3}"))), 100);
        rows.result("a", "one", 10);
        rows.add(null, LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of("b", "get-part", "{\"position\":3}"))), 100);
        rows.result("b", "one", 10);
        rows.add(null, LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of("c", "get-part", "{\"position\":4}"))), 100);
        rows.result("c", "two", 10);

        assertThat(report(rows).repeats()).singleElement().satisfies(repeat -> {
            assertThat(repeat.tool()).isEqualTo("get-part");
            assertThat(repeat.times()).isEqualTo(2);
        });
    }

    @Test
    void theBiggestResultsAndTheCallsACutOffByAPauseAreTold() {
        Rows rows = new Rows();
        rows.user("Go");
        rows.round(1000, 1000, "read_file", "get-part");
        rows.result("c1", "x".repeat(8000), 10);
        rows.result("c2", Defaults.AGENT_PAUSED_CALL, 10);

        Report report = report(rows);

        assertThat(report.biggest().getFirst().tool()).isEqualTo("read_file");
        assertThat(report.biggest().getFirst().chars()).isEqualTo(8000);
        assertThat(report.biggest().getFirst().tokens()).isEqualTo(2000);
        assertThat(report.overview().interrupted()).isEqualTo(1);
        assertThat(report.overview().errors()).as("a pause is not an error of the tool").isZero();
        assertThat(report.errorMessages()).isEmpty();
    }

    @Test
    void theNoticesOfTheRunAreCountedApartFromTheMessagesOfTheAuthor() {
        Rows rows = new Rows();
        rows.user("Go");
        rows.user(Defaults.AGENT_UNREADABLE_CALL);
        rows.user(String.format(Defaults.AGENT_TOO_MANY_CALLS, 4, 3, 1));
        rows.user(String.format(Defaults.AGENT_TOO_MANY_CALLS, 5, 2, 3));
        rows.user("Notice from the run: something else");
        rows.user("Please go on");

        AgentDiagnostics.Overview overview = report(rows).overview();

        assertThat(overview.authorMessages()).isEqualTo(2);
        assertThat(overview.notices()).containsEntry("unreadable call", 1L).containsEntry("too many calls", 2L).containsEntry("other", 1L);
    }

    @Test
    void theContextGrowsAndACompactionIsAPointOfItsOwn() {
        Rows rows = new Rows();
        rows.user("Go");
        rows.round(1000, 1000, "read_file");
        rows.result("c1", "a", 10);
        rows.round(1400, 1000, "read_file");
        rows.result("c2", "b", 10);
        rows.rows.forEach(row -> row.setCompacted(true));
        rows.add(null, LLMChatMessage.of(LLMRole.USER, "Summary of the work"), 100).setSummary(true);
        rows.round(500, 1000, "read_file");
        rows.result("c3", "c", 10);

        AgentDiagnostics.Overview overview = report(rows).overview();

        assertThat(overview.peakContext()).isEqualTo(1400);
        assertThat(overview.lastContext()).isEqualTo(500);
        assertThat(overview.growthPerRound()).as("the drop of the compaction is not a growth").isEqualTo(400);
        assertThat(overview.compactions()).isEqualTo(1);
        assertThat(report(rows).compactions()).singleElement().satisfies(compaction -> {
            assertThat(compaction.afterRound()).isEqualTo(2);
            assertThat(compaction.contextBefore()).isEqualTo(1400);
            assertThat(compaction.rows()).isEqualTo(5);
        });
        assertThat(overview.authorMessages()).as("the summary is no message of the author").isEqualTo(1);
    }

    @Test
    void aSubagentIsCountedByItsOwnConversation() {
        Rows rows = new Rows();
        rows.user("Go");
        AgentMessage task = rows.add("sub1", LLMChatMessage.of(LLMRole.USER, "check names"), 100);
        AgentMessage answer = rows.add("sub1", LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of("s1", "search-lore", "{}"))), 100);
        answer.setContextTokens(700L);
        AgentMessage result = rows.add("sub1", LLMChatMessage.ofToolResult("s1", "Error: no lorebook"), 100);
        AgentMessage end = rows.add("sub1", LLMChatMessage.of(LLMRole.ASSISTANT, "names are fine"), 100);
        end.setThreadEnd("DONE");
        AgentMessageService.SubThread thread = new AgentMessageService.SubThread("sub1", "check names", "c1", "DONE", "names are fine", false,
                List.of(task, answer, result, end));
        AgentMessageService.SubThread gone = new AgentMessageService.SubThread("sub2", "rewound", "c2", null, null, true, List.of(task));

        Report report = AgentDiagnostics.analyze("Checker", run(), List.of(rows.rows.getFirst()), List.of(thread, gone));

        assertThat(report.overview().calls()).as("the calls of the agent only").isZero();
        assertThat(report.subAgents()).singleElement().satisfies(sub -> {
            assertThat(sub.handle()).isEqualTo("sub1");
            assertThat(sub.state()).isEqualTo("DONE");
            assertThat(sub.rounds()).isEqualTo(1);
            assertThat(sub.tokens()).isEqualTo(700);
            assertThat(sub.calls()).isEqualTo(1);
            assertThat(sub.errors()).isEqualTo(1);
            assertThat(sub.tools()).containsEntry("search-lore", 1);
        });
    }

    @Test
    void aRunWithoutRowsHasAnEmptyReport() {
        Report report = AgentDiagnostics.analyze("Checker", run(), List.of(), List.of());

        assertThat(report.overview().rounds()).isZero();
        assertThat(report.tools()).isEmpty();
        assertThat(report.toMarkdown()).contains("# Diagnostics of the agent \"Checker\"").contains("No calls.").contains("No call failed.");
    }

    @Test
    void theReportIsMarkdownAndJson() {
        Rows rows = new Rows();
        rows.user("Go");
        rows.round(1000, 1000, "read_file", "list_files");
        rows.result("c1", "x".repeat(400), 100);
        rows.result("c2", "Error: nothing | here", 50);
        rows.text(1600, 1000, "Done.");

        Report report = report(rows);

        assertThat(report.toMarkdown()).contains("## Overview", "## Tools", "| read_file | 1 |", "## Errors", "nothing \\| here", "## Biggest results",
                "## Slowest rounds");
        JsonObject json = JsonParser.parseString(report.toJson()).getAsJsonObject();
        assertThat(json.getAsJsonObject("overview").get("calls").getAsLong()).isEqualTo(2);
        assertThat(json.getAsJsonArray("tools")).hasSize(2);
        assertThat(json.get("agentName").getAsString()).isEqualTo("Checker");
    }
}
