package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.model.impl.AgentMessage;
import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.AgentMessageService;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A run as a Markdown file the user can keep: the task of the run, every message, every command the agent called with
 * its arguments and its result, and the report it ended with. Made from the transcript as it is stored, nothing is cut.
 */
public final class AgentTranscriptExport {

    private AgentTranscriptExport() {
    }

    /**
     * @param agentName the name of the agent
     * @param run       the run, for the date, the state and the report
     * @param messages  the transcript of the run
     */
    public static String toMarkdown(String agentName, AgentRun run, List<LLMChatMessage> messages) {
        return toMarkdown(agentName, run, messages, null);
    }

    /**
     * @param reasoning the reasoning of the model for each message (null where there is none), in the order of
     *                  {@code messages}; null for none at all
     */
    public static String toMarkdown(String agentName, AgentRun run, List<LLMChatMessage> messages, List<String> reasoning) {
        StringBuilder md = header(agentName, run);
        render(md, messages, reasoning, null, "##");
        return footer(md, run);
    }

    /**
     * The run as it is stored: every message with the time it was written, and the conversation of each sub-agent after the one of
     * the agent.
     *
     * @param rows    the transcript of the run
     * @param threads the conversations of the sub-agents, the ones the agent went back over are left out
     */
    public static String export(String agentName, AgentRun run, List<AgentMessage> rows, List<AgentMessageService.SubThread> threads) {
        StringBuilder md = header(agentName, run);
        render(md, rows, "##");
        for (AgentMessageService.SubThread thread : threads) {
            if (thread.rewound()) {
                continue;
            }
            md.append("## Sub-agent ").append(thread.handle()).append("\n\n");
            md.append("- Task: ").append(StringUtils.defaultString(thread.task()).strip().replaceAll("\\s+", " ")).append('\n');
            md.append("- State: ").append(thread.isOver() ? thread.end() : "paused").append(", rounds: ").append(thread.rounds())
                    .append(", tokens: ").append(thread.tokens()).append("\n\n");
            render(md, thread.rows().stream().filter(row -> row.getThreadEnd() == null).toList(), "###");
            if (thread.isOver()) {
                md.append("### ").append(thread.end()).append("\n\n").append(StringUtils.defaultString(thread.result()).strip()).append("\n\n");
            }
        }
        return footer(md, run);
    }

    private static StringBuilder header(String agentName, AgentRun run) {
        StringBuilder md = new StringBuilder();
        md.append("# ").append(StringUtils.defaultString(agentName)).append("\n\n");
        if (run.getStartedAt() != null) {
            md.append("- Started: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(run.getStartedAt())).append('\n');
        }
        md.append("- Status: ").append(run.getStatus()).append('\n');
        md.append("- Rounds: ").append(run.getRounds()).append(", tokens: ").append(run.getTotalTokens()).append("\n\n");
        return md;
    }

    private static String footer(StringBuilder md, AgentRun run) {
        if (StringUtils.isNotBlank(run.getStatusReason())) {
            md.append("## ").append(run.getStatus()).append("\n\n").append(run.getStatusReason().strip()).append('\n');
        }
        return md.toString();
    }

    private static void render(StringBuilder md, List<AgentMessage> rows, String level) {
        render(md, rows.stream().map(AgentMessage::toChatMessage).toList(), rows.stream().map(AgentMessage::getReasoning).toList(),
                rows.stream().map(AgentMessage::getCreation).toList(), level);
    }

    /**
     * @param times when each message was written, null for no times
     * @param level the heading of a message ({@code ##}); what belongs to it is one level below
     */
    private static void render(StringBuilder md, List<LLMChatMessage> messages, List<String> reasoning, List<Date> times, String level) {
        String sub = level + "#";
        Map<String, String> callNames = new HashMap<>();
        for (int i = 0; i < messages.size(); i++) {
            LLMChatMessage message = messages.get(i);
            String thought = reasoning == null || i >= reasoning.size() ? null : reasoning.get(i);
            String time = times == null || i >= times.size() || times.get(i) == null ? ""
                    : " · " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(times.get(i));
            switch (message.getRole()) {
                case USER -> md.append(level).append(" You").append(time).append("\n\n").append(StringUtils.defaultString(message.getContent()).strip()).append("\n\n");
                case ASSISTANT -> {
                    if (StringUtils.isNotBlank(thought)) {
                        md.append(sub).append(" Reasoning").append(time).append("\n\n").append(fenced(thought)).append("\n\n");
                    }
                    if (StringUtils.isNotBlank(message.getContent())) {
                        md.append(level).append(" Agent").append(time).append("\n\n").append(message.getContent().strip()).append("\n\n");
                    }
                    for (LLMToolCall call : message.getToolCalls()) {
                        callNames.put(call.getId(), call.getName());
                        md.append(sub).append(" Call: ").append(call.getName()).append(time).append("\n\n")
                                .append(fenced(StringUtils.defaultString(call.getArguments()))).append("\n\n");
                    }
                }
                case TOOL -> md.append(sub).append(" Result: ")
                        .append(callNames.getOrDefault(message.getToolCallId(), "?")).append(time).append("\n\n")
                        .append(fenced(StringUtils.defaultString(message.getContent()))).append("\n\n");
                default -> {
                    // the system message is made for every request and not part of the transcript
                }
            }
        }
    }

    /**
     * The text in a code fence that is longer than any run of backticks in it, so the text can't end the fence.
     */
    static String fenced(String text) {
        int longest = 0;
        int run = 0;
        for (char c : text.toCharArray()) {
            run = c == '`' ? run + 1 : 0;
            longest = Math.max(longest, run);
        }
        String fence = "`".repeat(Math.max(3, longest + 1));
        return fence + "\n" + text.strip() + "\n" + fence;
    }
}
