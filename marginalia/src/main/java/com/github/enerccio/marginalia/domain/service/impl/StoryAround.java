package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.service.SummaryService.SummaryBlock;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMChatMessage;
import com.github.enerccio.marginalia.domain.service.impl.generation.dto.LLMRole;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The story around one part, for the prompts that work on a single part (the redraft, the annotation): the older story as the
 * summaries a generation sees (the story itself where there are none, or they are outdated), as much of the story before the part
 * as the room takes, and a share of the room for the story after it, which is only a hint of where it leads.
 */
final class StoryAround {

    static final String SEPARATOR = "\n\n";

    /**
     * The share of the room for the story the part after the part may take, the rest is the story before it.
     */
    private static final int FOLLOWING_SHARE = 4;

    /**
     * Parts of the budget the room of the story is rounded down to (a power of two, the room is never less than one step).
     */
    private static final int ROOM_STEPS = 16;

    private StoryAround() {
    }

    /**
     * The room for the story around the part, rounded down to a step of a sixteenth of the budget. The room is what the budget
     * leaves after the rest of the prompt (the part, its notes, the task), and as such it changes with every redraft by a few
     * tokens; and where it ends decides which part is the first of the story that is sent. A server that keeps the prompt it has
     * read can only reuse it up to the first difference, so a start of the story that moves by a part with each request makes it
     * read the whole story again. With a rounded room the story stays the same until the rest of the prompt changes by a step.
     * The prompt only gets shorter by the rounding, never longer.
     *
     * @param room   what is left of the budget
     * @param budget the tokens the whole prompt may take
     */
    static long stableRoom(long room, long budget) {
        long step = Math.max(256, Long.highestOneBit(Math.max(1, budget / ROOM_STEPS)));
        long rounded = room / step * step;
        return rounded > 0 ? rounded : room;
    }

    /** The user message in front of the first part of the story that is sent as messages. */
    static final String STORY_START = "[ The story so far. ]";

    /** The user message in front of every other part. */
    static final String STORY_MORE = "[ The story goes on. ]";

    /** What the text of the prompt says about the story that is sent as messages instead of in it. */
    static final String STORY_IN_MESSAGES = "The story before the part follows as the earlier messages of this conversation, oldest first.";

    /**
     * The tokens a part takes besides its text when it is sent as a message: the message before it from the user and the markers
     * of the roles of both.
     */
    private static final int MESSAGE_OVERHEAD = 24;

    /**
     * The prompt as the messages the model gets: the system message, then the parts of the story as the answers of the model (each
     * after a message from the user, as a generation sends them) and the last message from the user, which holds what changes from
     * one request to the next. A server that keeps what it has read can only continue from where a message of the user starts, and
     * a new part only adds messages at the end, so what it has read of the story stays valid; a story inside one big message would
     * not, and neither would a part inside the system message.
     *
     * @param story the parts of the story before the part, oldest first
     */
    static List<LLMChatMessage> messages(String system, List<String> story, String user) {
        List<LLMChatMessage> messages = new ArrayList<>();
        if (!system.isBlank()) {
            messages.add(LLMChatMessage.of(LLMRole.SYSTEM, system.stripTrailing()));
        }
        for (int i = 0; i < story.size(); i++) {
            messages.add(LLMChatMessage.of(LLMRole.USER, i == 0 ? STORY_START : STORY_MORE));
            messages.add(LLMChatMessage.of(LLMRole.ASSISTANT, story.get(i)));
        }
        // the conversation ends with a message from the user
        messages.add(LLMChatMessage.of(LLMRole.USER, user.isBlank() ? STORY_MORE : user.strip()));
        return messages;
    }

    /**
     * @param summaries the summaries of the older story, oldest first
     * @param previous  the story before the part that is not covered by the summaries, oldest first
     * @param following the story after the part
     */
    record Around(String summaries, String previous, String following, List<String> previousParts, long previousPartsTokens) {
    }

    /**
     * @param before the parts before the part, oldest first
     * @param after  the parts after it, oldest first
     * @param room   the tokens the story around the part may take
     */
    static Around collect(SummaryService summaryService, InferenceService inferenceService, List<ChatMessage> before,
                          List<ChatMessage> after, long room) throws Exception {
        return collect(summaryService, inferenceService, before, after, room, false);
    }

    /**
     * @param chain whether the story before the part is sent as messages ({@link #messages}) and not in the text: each of its parts
     *              then takes the tokens of a message besides its own
     */
    static Around collect(SummaryService summaryService, InferenceService inferenceService, List<ChatMessage> before,
                          List<ChatMessage> after, long room, boolean chain) throws Exception {
        long overhead = chain ? MESSAGE_OVERHEAD : 2;
        // the story after the part is only a hint of where it leads, so it gets a share of the room
        long followingRoom = room / FOLLOWING_SHARE;
        List<String> following = new ArrayList<>();
        long followingTokens = 0;
        for (ChatMessage next : after) {
            if (StringUtils.isBlank(next.getResponse())) {
                continue;
            }
            long tokens = tokensOf(inferenceService, next) + 2;
            if (followingTokens + tokens > followingRoom) {
                break;
            }
            following.add(next.getResponse());
            followingTokens += tokens;
        }

        // the share of the story after the part is taken from the room whenever there is some, however much of it fits: the story
        // before the part must not start elsewhere each time a part is added after it
        long previousRoom = room - (following.isEmpty() ? 0 : followingRoom);
        List<ChatMessage> newestFirst = new ArrayList<>(before.reversed());
        List<SummaryBlock> blocks = summaryService.collectBlocks(newestFirst);
        // summaries that do not describe the story anymore are not told to the model, the story is used as it is instead
        boolean summarized = !blocks.isEmpty() && blocks.stream().allMatch(SummaryBlock::isValid);

        List<String> summaries = new ArrayList<>();
        long summaryTokens = 0;
        if (summarized) {
            // the newest summaries are the ones worth keeping when they do not fit
            for (SummaryBlock block : blocks) {
                String text = StringUtils.defaultString(block.summary().getSummary());
                long tokens = inferenceService.countTokens(text) + 2;
                if (summaryTokens + tokens > previousRoom) {
                    break;
                }
                summaries.add(text);
                summaryTokens += tokens;
            }
            Collections.reverse(summaries);
        }

        List<String> previous = new ArrayList<>();
        long previousTokens = summaryTokens;
        long partsTokens = 0;
        ChatMessage stop = summarized ? blocks.getFirst().head() : null;
        for (ChatMessage earlier : newestFirst) {
            if (stop != null && Objects.equals(stop.getId(), earlier.getId())) {
                break;
            }
            if (StringUtils.isBlank(earlier.getResponse())) {
                continue;
            }
            long tokens = tokensOf(inferenceService, earlier) + overhead;
            if (previousTokens + tokens > previousRoom) {
                break;
            }
            previous.add(earlier.getResponse());
            previousTokens += tokens;
            partsTokens += tokens;
        }
        Collections.reverse(previous);

        return new Around(String.join(SEPARATOR, summaries), String.join(SEPARATOR, previous), String.join(SEPARATOR, following),
                List.copyOf(previous), partsTokens);
    }

    /**
     * The parts after the given one on the branch the book is at, oldest first; none when the part is not on it.
     */
    static List<ChatMessage> partsAfter(ChatMessageService chatMessageService, Manuscript book, ChatMessage part) throws Exception {
        ChatMessage leaf = chatMessageService.find(book.getActiveLeaf());
        if (leaf == null) {
            return List.of();
        }
        List<ChatMessage> branch = chatMessageService.getBranchFromLeaf(leaf);
        for (int i = 0; i < branch.size(); i++) {
            if (Objects.equals(branch.get(i).getId(), part.getId())) {
                return new ArrayList<>(branch.subList(i + 1, branch.size()));
            }
        }
        return List.of();
    }

    static long tokensOf(InferenceService inferenceService, ChatMessage message) throws Exception {
        return message.getTokenCount() > 0 ? message.getTokenCount() : inferenceService.countTokens(message.getResponse());
    }

}
