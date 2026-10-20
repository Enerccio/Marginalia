package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.model.impl.AgentRun;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
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
        StringBuilder md = new StringBuilder();
        md.append("# ").append(StringUtils.defaultString(agentName)).append("\n\n");
        if (run.getStartedAt() != null) {
            md.append("- Started: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(run.getStartedAt())).append('\n');
        }
        md.append("- Status: ").append(run.getStatus()).append('\n');
        md.append("- Rounds: ").append(run.getRounds()).append(", tokens: ").append(run.getTotalTokens()).append("\n\n");

        Map<String, String> callNames = new HashMap<>();
        for (int i = 0; i < messages.size(); i++) {
            LLMChatMessage message = messages.get(i);
            String thought = reasoning == null || i >= reasoning.size() ? null : reasoning.get(i);
            switch (message.getRole()) {
                case USER -> md.append("## You\n\n").append(StringUtils.defaultString(message.getContent()).strip()).append("\n\n");
                case ASSISTANT -> {
                    if (StringUtils.isNotBlank(thought)) {
                        md.append("### Reasoning\n\n").append(fenced(thought)).append("\n\n");
                    }
                    if (StringUtils.isNotBlank(message.getContent())) {
                        md.append("## Agent\n\n").append(message.getContent().strip()).append("\n\n");
                    }
                    for (LLMToolCall call : message.getToolCalls()) {
                        callNames.put(call.getId(), call.getName());
                        md.append("### Call: ").append(call.getName()).append("\n\n")
                                .append(fenced(StringUtils.defaultString(call.getArguments()))).append("\n\n");
                    }
                }
                case TOOL -> md.append("### Result: ")
                        .append(callNames.getOrDefault(message.getToolCallId(), "?")).append("\n\n")
                        .append(fenced(StringUtils.defaultString(message.getContent()))).append("\n\n");
                default -> {
                    // the system message is made for every request and not part of the transcript
                }
            }
        }
        if (StringUtils.isNotBlank(run.getStatusReason())) {
            md.append("## ").append(run.getStatus()).append("\n\n").append(run.getStatusReason().strip()).append('\n');
        }
        return md.toString();
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
