package com.github.enerccio.marginalia.extensions.arcoutline.service;

import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMToolCall;
import com.github.enerccio.marginalia.extensions.arcoutline.model.WizardCall;
import com.github.enerccio.marginalia.extensions.arcoutline.model.WizardMessage;
import com.github.enerccio.marginalia.extensions.arcoutline.service.ArcOutlineService.StoryMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToLongFunction;

/**
 * What of the book and the conversation fits into one request of the plot wizard: the newest parts of the story that
 * fit the budget of the author, and the newest messages of the conversation that fit what the model takes. Pure
 * functions of the numbers, so they can be tested without a model.
 */
public final class WizardContext {

    /** The longest result of a tool call that is sent again with the conversation; more is cut, the model can read the rest again. */
    static final int MAX_RESULT_CHARS = 2000;

    /** The room the model keeps for its answer is not counted here, the caller gives the room for the prompt. */
    private static final double DEFAULT_STORY_SHARE = 1.0 / 3.0;

    private WizardContext() {
    }

    /**
     * The tokens the story parts get when the author did not choose: a third of what the model takes.
     */
    public static long defaultStoryTokens(long promptCapacity) {
        return Math.max(0, Math.round(promptCapacity * DEFAULT_STORY_SHARE));
    }

    /**
     * The newest of the parts that fit the budget, oldest first. A part is never cut: the one that would go over the
     * budget and the older ones are left out.
     *
     * @param parts  the parts after the newest summary, oldest first
     * @param budget the most tokens of the parts
     */
    public static List<StoryMessage> newestThatFit(List<StoryMessage> parts, long budget) {
        List<StoryMessage> chosen = new ArrayList<>();
        long used = 0;
        for (int i = parts.size() - 1; i >= 0; i--) {
            StoryMessage part = parts.get(i);
            if (used + part.tokens() > budget) {
                break;
            }
            used += part.tokens();
            chosen.addFirst(part);
        }
        return chosen;
    }

    /**
     * The story parts as the prompt shows them, each under a heading with its place in the story (the number the story
     * outline shows, so the author and the model talk about the same part).
     */
    public static String storyText(List<StoryMessage> parts) {
        StringBuilder text = new StringBuilder();
        for (StoryMessage part : parts) {
            text.append("--- Part ").append(part.order()).append(" ---\n").append(part.text().strip()).append("\n\n");
        }
        return text.toString().stripTrailing();
    }

    /**
     * The summaries of the story as the prompt shows them, oldest first, separated by an empty line.
     */
    public static String summariesText(List<String> summaries) {
        return String.join("\n\n", summaries.stream().filter(s -> s != null && !s.isBlank()).map(String::strip).toList());
    }

    /**
     * The text of the newest summary message of the chat, null when it has none.
     */
    public static String lastSummary(List<WizardMessage> chat) {
        for (int i = chat.size() - 1; i >= 0; i--) {
            if (chat.get(i).isSummary()) {
                return chat.get(i).getText();
            }
        }
        return null;
    }

    /**
     * What the model gets of the chat: the messages after the newest summary (all of them without one). The summary
     * itself goes into the system prompt, see {@link #withSummary}.
     */
    public static List<WizardMessage> sinceSummary(List<WizardMessage> chat) {
        for (int i = chat.size() - 1; i >= 0; i--) {
            if (chat.get(i).isSummary()) {
                return new ArrayList<>(chat.subList(i + 1, chat.size()));
            }
        }
        return new ArrayList<>(chat);
    }

    /**
     * The system prompt with the summary of the earlier chat under it.
     *
     * @param summary the text of the summary, null or blank for none
     */
    public static String withSummary(String system, String summary) {
        return summary == null || summary.isBlank() ? system
                : system + "\n\n### Our discussion so far (a summary; the author and you wrote it, go on from here)\n" + summary.strip();
    }

    /**
     * The instructions for summarizing a chat with what the author asked the summary to keep or stress under them.
     *
     * @param note what the author wrote, null or blank for nothing
     */
    public static String summaryPrompt(String base, String note) {
        return note == null || note.isBlank() ? base
                : base.stripTrailing() + "\n\n### What the author asks the summary to keep or stress\n"
                + note.strip() + "\nThis comes first: put it in the summary even if it does not fit the limit above.";
    }

    /**
     * The system prompt with the instructions for the tools, and the hints of the tools the author chose (the name tools) under them.
     *
     * @param hints null or blank for none
     */
    public static String withTools(String system, String instructions, String hints) {
        StringBuilder text = new StringBuilder(system.stripTrailing()).append("\n\n").append(instructions.strip());
        if (hints != null && !hints.isBlank()) {
            text.append("\n\n").append(hints.strip());
        }
        return text.toString();
    }

    /** Everything of the message that goes to the model as text, for counting: the text and what the calls passed and got back. */
    public static String sentText(WizardMessage message) {
        StringBuilder text = new StringBuilder(message.getText() == null ? "" : message.getText());
        if (message.getCalls() != null) {
            for (WizardCall call : message.getCalls()) {
                text.append('\n').append(call.getName()).append(' ').append(call.getArguments()).append(' ').append(boundedResult(call));
            }
        }
        return text.toString();
    }

    private static String boundedResult(WizardCall call) {
        String result = call.getResult() == null ? "" : call.getResult();
        return result.length() <= MAX_RESULT_CHARS ? result : result.substring(0, MAX_RESULT_CHARS) + " ...[cut]";
    }

    /**
     * The messages of the conversation as the model gets them. The tools a model message called are sent as the calls and results they
     * were (a call, then its result), ahead of the text of the answer, so the model sees what it did and does not do it again; a text
     * that only says "tools used" is not enough, it cannot tell what was made.
     */
    public static List<LLMChatMessage> toPayload(List<WizardMessage> messages) {
        List<LLMChatMessage> payload = new ArrayList<>();
        int number = 0;
        for (WizardMessage message : messages) {
            String text = message.getText() == null ? "" : message.getText();
            if (message.isFromUser()) {
                payload.add(LLMChatMessage.of(LLMRole.USER, text));
                continue;
            }
            number++;
            List<WizardCall> calls = message.getCalls() == null ? List.of() : message.getCalls();
            if (!calls.isEmpty()) {
                List<LLMToolCall> toolCalls = new ArrayList<>();
                for (int i = 0; i < calls.size(); i++) {
                    String arguments = calls.get(i).getArguments();
                    toolCalls.add(LLMToolCall.of("call_" + number + "_" + i, calls.get(i).getName(),
                            arguments == null || arguments.isBlank() ? "{}" : arguments));
                }
                payload.add(LLMChatMessage.ofToolCalls(null, toolCalls));
                for (int i = 0; i < calls.size(); i++) {
                    payload.add(LLMChatMessage.ofToolResult("call_" + number + "_" + i, boundedResult(calls.get(i))));
                }
            }
            // an answer that is only calls ends with the results, an empty message would be refused by some providers
            if (!text.isBlank() || calls.isEmpty()) {
                payload.add(LLMChatMessage.of(LLMRole.ASSISTANT, text));
            }
        }
        return payload;
    }

    /**
     * What the model is told of the tools it called in an earlier answer: the calls are not sent again, so the text says that they
     * were made.
     */
    public static String withCalls(String text, List<com.github.enerccio.marginalia.extensions.arcoutline.model.WizardCall> calls) {
        if (calls == null || calls.isEmpty()) {
            return text;
        }
        StringBuilder note = new StringBuilder("[Tools used in this answer: ");
        for (int i = 0; i < calls.size(); i++) {
            note.append(i == 0 ? "" : ", ").append(calls.get(i).getName()).append(calls.get(i).isFailed() ? " (failed)" : "");
        }
        return text + "\n\n" + note.append(']');
    }

    /**
     * The messages of the conversation that go to the model: the newest ones whose tokens fit {@code room}, oldest first.
     * The newest message (what the author just wrote) is always kept, even when it alone is too long, a request without
     * it would answer nothing.
     *
     * @param room   what the model takes minus the system prompt
     * @param tokens the number of tokens of a text
     */
    public static List<WizardMessage> historyThatFits(List<WizardMessage> history, long room, ToLongFunction<String> tokens) {
        List<WizardMessage> chosen = new ArrayList<>();
        long used = 0;
        for (int i = history.size() - 1; i >= 0; i--) {
            WizardMessage message = history.get(i);
            long size = tokens.applyAsLong(sentText(message));
            if (!chosen.isEmpty() && used + size > room) {
                break;
            }
            used += size;
            chosen.addFirst(message);
        }
        // a conversation has to start with the author, a model that opens with its own answer confuses some providers
        while (chosen.size() > 1 && !chosen.getFirst().isFromUser()) {
            chosen.removeFirst();
        }
        return chosen;
    }
}
