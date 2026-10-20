package com.github.enerccio.marginalia.domain.agents;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import com.github.enerccio.marginalia.domain.model.impl.AgentMessage;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentMessageService;
import com.github.enerccio.marginalia.domain.service.agent.AgentTranscriptExport;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A run as a Markdown file: what was said, every call with its result, the report.
 */
class AgentTranscriptExportTest {

    private static AgentRun run(AgentRunStatus status, String reason) {
        AgentRun run = new AgentRun();
        run.setStatus(status);
        run.setStatusReason(reason);
        return run;
    }

    @Test
    void writesWhatWasSaidTheCallsWithTheirResultsAndTheReport() {
        String md = AgentTranscriptExport.toMarkdown("Checker", run(AgentRunStatus.DONE, "All is well."), List.of(
                LLMChatMessage.of(LLMRole.USER, "Check the dragon"),
                LLMChatMessage.ofToolCalls("Let me look", List.of(LLMToolCall.of("c1", "list-parts", "{\"limit\": 5}"))),
                LLMChatMessage.ofToolResult("c1", "1: Once upon a time"),
                LLMChatMessage.of(LLMRole.ASSISTANT, "All good")));

        assertThat(md).startsWith("# Checker\n").contains("- Status: DONE")
                .contains("## You\n\nCheck the dragon")
                .contains("## Agent\n\nLet me look")
                .contains("### Call: list-parts\n\n```\n{\"limit\": 5}\n```")
                .contains("### Result: list-parts\n\n```\n1: Once upon a time\n```")
                .contains("## Agent\n\nAll good")
                .endsWith("## DONE\n\nAll is well.\n");
        // in the order they happened
        assertThat(md.indexOf("Check the dragon")).isLessThan(md.indexOf("list-parts"));
        assertThat(md.indexOf("1: Once upon a time")).isLessThan(md.indexOf("All good"));
    }

    @Test
    void aResultWithBackticksCannotEndItsFence() {
        String md = AgentTranscriptExport.toMarkdown("Checker", run(AgentRunStatus.IDLE, null), List.of(
                LLMChatMessage.ofToolCalls(null, List.of(LLMToolCall.of("c1", "read_file", "{}"))),
                LLMChatMessage.ofToolResult("c1", "code:\n```java\nint a;\n```\nend")));

        assertThat(md).contains("````\ncode:\n```java\nint a;\n```\nend\n````");
    }

    @Test
    void anEmptyRunIsJustItsHeader() {
        String md = AgentTranscriptExport.toMarkdown("Checker", run(AgentRunStatus.STOPPED_DISCARDED, null), List.of());

        assertThat(md).contains("# Checker").contains("STOPPED_DISCARDED").doesNotContain("##");
    }

    @Test
    void theReasoningOfAnAnswerIsInTheFileBeforeIt() {
        List<LLMChatMessage> messages = List.of(LLMChatMessage.of(LLMRole.USER, "Check"), LLMChatMessage.of(LLMRole.ASSISTANT, "All good"));

        String md = AgentTranscriptExport.toMarkdown("Checker", run(AgentRunStatus.IDLE, null), messages, java.util.Arrays.asList(null, "first I thought"));

        assertThat(md).contains("### Reasoning\n\n```\nfirst I thought\n```\n\n## Agent\n\nAll good");
        assertThat(AgentTranscriptExport.toMarkdown("Checker", run(AgentRunStatus.IDLE, null), messages)).doesNotContain("Reasoning");
    }

    private static AgentMessage row(String thread, int ordinal, LLMChatMessage message, java.util.Date written) {
        AgentMessage row = new AgentMessage();
        row.setThread(thread);
        row.setOrdinal(ordinal);
        row.setChatMessage(message);
        row.setCreation(written);
        return row;
    }

    @Test
    void theStoredRunHasTheTimeOfEveryMessageAndTheConversationOfEverySubagent() {
        java.util.Date written = new java.util.GregorianCalendar(2026, java.util.Calendar.OCTOBER, 20, 16, 50, 23).getTime();
        AgentMessage task = row("sub1", 0, LLMChatMessage.of(LLMRole.USER, "check names"), written);
        task.setThreadTask("check names");
        AgentMessage answer = row("sub1", 1, LLMChatMessage.of(LLMRole.ASSISTANT, "names are fine"), written);
        AgentMessage end = row("sub1", 2, LLMChatMessage.of(LLMRole.ASSISTANT, "names are fine"), written);
        end.setThreadEnd("DONE");
        AgentMessageService.SubThread thread = new AgentMessageService.SubThread("sub1", "check names", "c1", "DONE", "names are fine", false,
                List.of(task, answer, end));
        AgentMessageService.SubThread gone = new AgentMessageService.SubThread("sub2", "rewound", "c2", null, null, true, List.of(task));

        String md = AgentTranscriptExport.export("Checker", run(AgentRunStatus.DONE, null),
                List.of(row(null, 0, LLMChatMessage.of(LLMRole.USER, "go"), written)), List.of(thread, gone));

        assertThat(md).contains("## You · 2026-10-20 16:50:23\n\ngo")
                .contains("## Sub-agent sub1\n\n- Task: check names\n- State: DONE")
                .contains("### You · 2026-10-20 16:50:23\n\ncheck names")
                .contains("### Agent · 2026-10-20 16:50:23\n\nnames are fine")
                .contains("### DONE\n\nnames are fine")
                .doesNotContain("sub2");
        assertThat(md.indexOf("## You")).isLessThan(md.indexOf("## Sub-agent sub1"));
    }
}
