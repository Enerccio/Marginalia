package com.github.enerccio.marginalia.domain.service.agent;

import com.github.enerccio.marginalia.domain.collections.AgentRunStatus;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Everything the model of a run reads with its next request, taken apart: the sections of the system message, the
 * definition of every tool and every message of the conversation, in the order they are sent, each with its text and an
 * estimate of its tokens. Made by {@code AgentRunner#context} for the console and for a debugging session: {@link
 * #toMarkdown()} is the whole context as one document.
 *
 * @param agentName      the name of the agent
 * @param status         the status of the run
 * @param rounds         how many requests the run made
 * @param parts          the context in the order it is sent
 * @param history        what the model reported as the size of its context at each of its answers, oldest first
 * @param contextCap     the most tokens the context may have, 0 when not known
 * @param reportedTokens the size of the context at the last answer as the model reported it (the request and the answer), 0 when
 *                       there was none
 */
public record AgentContext(String agentName, AgentRunStatus status, long rounds, List<Part> parts, List<Point> history,
                           long contextCap, long reportedTokens) {

    /**
     * What a part of the context is.
     */
    public enum Kind {
        /** The prompt of the agent, the first section of the system message. */
        SYSTEM_PROMPT,
        /** The instructions of the author in the system message. */
        INSTRUCTIONS,
        /** What the runner adds to the system message: how to work, the limits, the sub-agents. */
        NOTE,
        /** The definition of one tool. */
        TOOL,
        /** The summary a compaction wrote, the first message of what the model reads after it. */
        SUMMARY,
        /** A message of the author (or of the runner in the role of the user). */
        USER,
        /** An answer of the model, its text and the calls it made. */
        ASSISTANT,
        /** The result of a call. */
        RESULT;

        public boolean isSystem() {
            return this == SYSTEM_PROMPT || this == INSTRUCTIONS || this == NOTE;
        }
    }

    /**
     * One part of the context.
     *
     * @param kind   what it is
     * @param title  what it is called: the section, the tool, "#12 assistant"
     * @param text   exactly what the model is sent as this part
     * @param tokens about how many tokens it has
     */
    public record Part(Kind kind, String title, String text, long tokens) {
    }

    /**
     * The context at one answer of the model.
     *
     * @param round  the number of the answer, from 1
     * @param tokens the size of the context the model reported, the request and its answer
     */
    public record Point(int round, long tokens) {
    }

    /**
     * Estimated tokens of the whole context: the sum of the parts.
     */
    public long estimatedTokens() {
        return parts.stream().mapToLong(Part::tokens).sum();
    }

    /**
     * Estimated tokens of the parts of this kind.
     */
    public long tokens(Kind kind) {
        return parts.stream().filter(part -> part.kind() == kind).mapToLong(Part::tokens).sum();
    }

    /**
     * The context as a Markdown document: a header, then the system message with its sections, the tools with their
     * descriptions and schemas and the conversation message by message. The text of every part is in a code fence, so what
     * the model reads can be told from the headings that name it.
     */
    public String toMarkdown() {
        StringBuilder md = new StringBuilder();
        md.append("# Context of the agent \"").append(StringUtils.defaultString(agentName)).append("\"\n\n");
        md.append("- Run: ").append(status).append(", ").append(rounds).append(" request(s) made\n");
        md.append("- Size of the context, estimated: about ").append(estimatedTokens()).append(" tokens");
        if (contextCap > 0) {
            md.append(" of ").append(contextCap);
        }
        md.append('\n');
        if (reportedTokens > 0) {
            md.append("- Reported by the model at its last answer (the request and the answer): ").append(reportedTokens).append(" tokens\n");
        }
        md.append("\nThis is what the model reads with its next request, in this order: the system message (its sections are joined by a "
                + "blank line), the definitions of the tools, and the conversation. The token counts are estimates.\n\n");

        List<Part> system = parts.stream().filter(part -> part.kind().isSystem()).toList();
        List<Part> tools = parts.stream().filter(part -> part.kind() == Kind.TOOL).toList();
        List<Part> conversation = parts.stream().filter(part -> !part.kind().isSystem() && part.kind() != Kind.TOOL).toList();

        md.append("## System message (about ").append(system.stream().mapToLong(Part::tokens).sum()).append(" tokens)\n\n");
        system.forEach(part -> append(md, part));
        md.append("## Tools (").append(tools.size()).append(", about ").append(tools.stream().mapToLong(Part::tokens).sum())
                .append(" tokens)\n\n");
        tools.forEach(part -> append(md, part));
        md.append("## Conversation (").append(conversation.size()).append(" message(s), about ")
                .append(conversation.stream().mapToLong(Part::tokens).sum()).append(" tokens)\n\n");
        conversation.forEach(part -> append(md, part));
        return md.toString();
    }

    private static void append(StringBuilder md, Part part) {
        md.append("### ").append(part.title()).append(" (about ").append(part.tokens()).append(" tokens)\n\n")
                .append(AgentTranscriptExport.fenced(StringUtils.defaultString(part.text()))).append("\n\n");
    }
}
