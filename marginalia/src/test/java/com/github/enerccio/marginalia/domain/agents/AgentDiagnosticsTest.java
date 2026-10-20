package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.Defaults;
import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.AgentMessage;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentMessageService;
import com.github.enerccio.marginalia.domain.service.agent.AgentDiagnostics;
import com.github.enerccio.marginalia.domain.service.agent.AgentDiagnostics.Report;
import com.github.enerccio.marginalia.domain.service.agent.AgentTelemetry;
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

        // the telemetry of the answer that was added last
        void told(AgentTelemetry telemetry) {
            rows.getLast().setTelemetry(telemetry.toJson());
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
            assertThat(error.example()).as("the arguments the model wrote").isEqualTo("{\"path\":\"/n2.md\"}");
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

    private static AgentTelemetry telemetry(long prompt, long completion, long cached, long reasoning, long first, long generation,
                                            String finish, int asked, int kept) {
        return new AgentTelemetry(prompt, completion, cached, reasoning, first, generation, finish, -1, -1, asked, kept, 0);
    }

    @Test
    void whatTheRequestsReportedIsAddedUpWithTheCacheTheReasoningAndTheEndOfTheAnswers() {
        Rows rows = new Rows();
        rows.user("Go");
        rows.round(1000, 2000, "read_file");
        rows.told(telemetry(1000, 100, 0, 40, 500, 4000, "tool_calls", 3, 1));
        rows.result("c1", "a", 10);
        rows.round(1200, 2000, "read_file");
        rows.told(telemetry(1200, 300, 1000, 100, 100, 6000, "length", 1, 1));
        rows.result("c2", "b", 10);
        rows.text(1300, 1000, "Done.");
        rows.told(telemetry(1300, 50, 1200, -1, 50, 500, "stop", 0, 0));

        AgentDiagnostics.Telemetry t = report(rows).telemetry();

        assertThat(t.rounds()).isEqualTo(3);
        assertThat(t.firstTokenMillis()).isEqualTo(650);
        assertThat(t.generationMillis()).isEqualTo(10_500);
        assertThat(t.tokensPerSecond()).isEqualTo(450 * 1000.0 / 10_500);
        assertThat(t.promptTokens()).isEqualTo(3500);
        assertThat(t.cachedTokens()).isEqualTo(2200);
        assertThat(t.completionTokens()).as("only the answers that report reasoning").isEqualTo(400);
        assertThat(t.reasoningTokens()).isEqualTo(140);
        assertThat(t.finishReasons()).containsEntry("tool_calls", 1L).containsEntry("length", 1L).containsEntry("stop", 1L);
        assertThat(t.droppedCalls()).isEqualTo(2);
        assertThat(t.blocks()).singleElement().satisfies(block -> {
            assertThat(block.rounds()).isEqualTo("1-3");
            assertThat(block.cutOffs()).isEqualTo(1);
            assertThat(block.cachePercent()).isEqualTo(63);
            assertThat(block.reasoningPercent()).isEqualTo(35);
        });
        assertThat(report(rows).toMarkdown()).contains("What the requests reported").contains("From 3 answers")
                .contains("Why the answers ended: length 1, stop 1, tool_calls 1");
    }

    @Test
    void theAnswersOfARunWithoutTelemetryAreSaidToHaveNone() {
        Rows rows = new Rows();
        rows.user("Go");
        rows.round(1000, 2000, "read_file");

        Report report = report(rows);

        assertThat(report.telemetry().rounds()).isZero();
        assertThat(report.toMarkdown()).contains("have no telemetry");
    }

    @Test
    void theRequestThatWroteASummaryIsCountedApart() {
        Rows rows = new Rows();
        rows.user("Go");
        rows.round(1000, 2000, "read_file");
        rows.result("c1", "a", 10);
        rows.rows.forEach(row -> row.setCompacted(true));
        rows.add(null, LLMChatMessage.of(LLMRole.USER, "Summary of the work"), 100).setSummary(true);
        rows.told(telemetry(2000, 500, -1, -1, 3000, 9000, "stop", 0, 0));

        AgentDiagnostics.Telemetry t = report(rows).telemetry();

        assertThat(t.compactions()).isEqualTo(1);
        assertThat(t.compactionMillis()).isEqualTo(12_000);
        assertThat(t.rounds()).as("a summary is not an answer of the run").isZero();
    }

    @Test
    void theNoticesOfTheRunAreCountedApartFromTheMessagesOfTheAuthor() {
        Rows rows = new Rows();
        rows.user("Go");
        rows.user(Defaults.AGENT_UNREADABLE_CALL);
        rows.user(String.format(Defaults.AGENT_TOO_MANY_CALLS, 4, 3, 1));
        rows.user(String.format(Defaults.AGENT_TOO_MANY_CALLS, 5, 2, 3));
        rows.user(String.format(Defaults.AGENT_ONE_CALL_NOTICE, 3));
        rows.user(String.format(Defaults.AGENT_SINGLE_CALL_NOTICE, "regenerate-summary", 3, 2));
        rows.user("Notice from the run: something else");
        rows.user("Please go on");

        AgentDiagnostics.Overview overview = report(rows).overview();

        assertThat(overview.authorMessages()).isEqualTo(2);
        assertThat(overview.notices()).containsEntry("unreadable call", 1L).containsEntry("too many calls", 2L).containsEntry("other", 1L)
                .containsEntry("one call per round", 1L).containsEntry("command made alone", 1L);
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

    // a call of one tool with the arguments as written
    private static void call(Rows rows, String id, String tool, String arguments, String result) {
        rows.add(null, LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of(id, tool, arguments))), 100).setContextTokens(1000L);
        rows.result(id, result, 10);
    }

    private static String json(String path, String text) {
        JsonObject arguments = new JsonObject();
        arguments.addProperty("path", path);
        arguments.addProperty("text", text);
        return arguments.toString();
    }

    @Test
    void aLongTextWrittenOutIsToldFromAReferenceAndFromACopyOfWhatWasJustRead() {
        String chunk = "Lily laughed and slipped in to the tub, and nothing was between them but water and bubbles. ".repeat(6);
        String other = "A paragraph the agent made up itself, and nobody gave it to the agent in any result at all. ".repeat(6);
        Rows rows = new Rows();
        rows.user("Import");
        call(rows, "a", "read_file", "{\"path\":\"/chunked/0001.md\"}", chunk);
        call(rows, "b", "create-part", json("p", chunk), "created");
        call(rows, "c", "create-part", "{\"text\":\"@file:/chunked/0002.md\"}", "created");
        call(rows, "d", "create-part", "{\"text\":\"@result\"}", "created");
        call(rows, "e", "create-part", json("p", other), "created");
        call(rows, "f", "create-part", json("p", "short"), "created");

        Report report = report(rows);

        AgentDiagnostics.ToolStat parts = report.tools().stream().filter(tool -> tool.name().equals("create-part")).findFirst().orElseThrow();
        assertThat(parts.calls()).isEqualTo(5);
        assertThat(parts.referenced()).as("@file and @result").isEqualTo(2);
        assertThat(parts.longTexts()).as("the copy and the made up one").isEqualTo(2);
        assertThat(parts.copied()).as("only the one that was in the result before it").isEqualTo(1);
        assertThat(parts.copiedChars()).isEqualTo(chunk.length());
        assertThat(report.tools().stream().filter(tool -> tool.name().equals("read_file")).findFirst().orElseThrow().copied()).isZero();
        assertThat(report.bigArguments().getFirst().copied()).isIn(true, false);
        assertThat(report.bigArguments()).filteredOn(AgentDiagnostics.BigArgument::copied).singleElement()
                .satisfies(big -> assertThat(big.tool()).isEqualTo("create-part"));
    }

    @Test
    void aTextCopiedFromARoundLongAgoIsNotACopyOfWhatWasJustRead() {
        String chunk = "Severus clambered in to the tub and kept his eyes on the mermaid painting in front of him. ".repeat(6);
        Rows rows = new Rows();
        rows.user("Import");
        call(rows, "a", "read_file", "{\"path\":\"/chunked/0001.md\"}", chunk);
        for (int i = 0; i < 70; i++) {
            call(rows, "r" + i, "list_files", "{}", "nothing new " + i);
        }
        call(rows, "z", "create-part", json("p", chunk), "created");

        assertThat(report(rows).tools().stream().filter(tool -> tool.name().equals("create-part")).findFirst().orElseThrow().copied()).isZero();
    }

    @Test
    void aCallCutOffInTheMiddleCountsWhatWasWritten() {
        Rows rows = new Rows();
        rows.user("Import");
        call(rows, "a", "create-part", "{\"text\":\"" + "x".repeat(500), "Error: The arguments of create-part are not valid JSON: Unexpected end-of-input");

        AgentDiagnostics.ToolStat parts = report(rows).tools().getFirst();

        assertThat(parts.longTexts()).isEqualTo(1);
        assertThat(parts.argChars()).isGreaterThan(500);
        assertThat(parts.errors()).isEqualTo(1);
    }

    @Test
    void theRoundsAreCountedInBlocksWithTheReasoningAndTheTextTheModelWrote() {
        Rows rows = new Rows();
        rows.user("Go");
        for (int i = 1; i <= 30; i++) {
            rows.round(1000 + i, 1000, "read_file");
            rows.rows.getLast().setReasoning("t".repeat(10));
            rows.result("c" + i, "text", 10);
        }
        rows.text(1100, 1000, "Done, all of it");

        Report report = report(rows);

        assertThat(report.blocks()).hasSize(2);
        AgentDiagnostics.Block first = report.blocks().getFirst();
        assertThat(first.firstRound()).isEqualTo(1);
        assertThat(first.lastRound()).isEqualTo(AgentDiagnostics.BLOCK);
        assertThat(first.calls()).isEqualTo(AgentDiagnostics.BLOCK);
        assertThat(first.reasoningChars()).isEqualTo(10L * AgentDiagnostics.BLOCK);
        assertThat(first.modelMillis()).isEqualTo(1000L * AgentDiagnostics.BLOCK);
        AgentDiagnostics.Block second = report.blocks().get(1);
        assertThat(second.firstRound()).isEqualTo(AgentDiagnostics.BLOCK + 1);
        assertThat(second.lastRound()).isEqualTo(31);
        assertThat(second.textChars()).isEqualTo("Done, all of it".length());
        assertThat(report.overview().reasoningChars()).isEqualTo(300);
        assertThat(report.overview().textChars()).isEqualTo("Done, all of it".length());
        assertThat(report.overview().argChars()).isPositive();
        assertThat(report.toMarkdown()).contains("## What the model wrote, by block of 25 rounds", "| 1-25 |", "| 26-31 |",
                "Written by the model:", "## Longest arguments");
    }

    @Test
    void theLongestArgumentsShowTheStartOfThemWithThePathOfAFileTool() {
        Rows rows = new Rows();
        rows.user("Go");
        call(rows, "a", "write_file", json("/notes/characters.md", "n".repeat(900)), "written");
        call(rows, "b", "write_file", json("/notes/status.md", "s"), "written");

        List<AgentDiagnostics.BigArgument> big = report(rows).bigArguments();

        assertThat(big).hasSize(2);
        assertThat(big.getFirst().arguments()).contains("/notes/characters.md");
        assertThat(big.getFirst().chars()).isGreaterThan(900);
        assertThat(big.getFirst().round()).isEqualTo(1);
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
