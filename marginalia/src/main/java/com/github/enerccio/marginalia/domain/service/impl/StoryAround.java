package com.github.enerccio.marginalia.domain.service.impl;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;
import com.github.enerccio.marginalia.domain.model.impl.Manuscript;
import com.github.enerccio.marginalia.domain.service.ChatMessageService;
import com.github.enerccio.marginalia.domain.service.InferenceService;
import com.github.enerccio.marginalia.domain.service.SummaryService;
import com.github.enerccio.marginalia.domain.service.SummaryService.SummaryBlock;
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

    private StoryAround() {
    }

    /**
     * @param summaries the summaries of the older story, oldest first
     * @param previous  the story before the part that is not covered by the summaries, oldest first
     * @param following the story after the part
     */
    record Around(String summaries, String previous, String following) {
    }

    /**
     * @param before the parts before the part, oldest first
     * @param after  the parts after it, oldest first
     * @param room   the tokens the story around the part may take
     */
    static Around collect(SummaryService summaryService, InferenceService inferenceService, List<ChatMessage> before,
                          List<ChatMessage> after, long room) throws Exception {
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

        long previousRoom = room - followingTokens;
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
        ChatMessage stop = summarized ? blocks.getFirst().head() : null;
        for (ChatMessage earlier : newestFirst) {
            if (stop != null && Objects.equals(stop.getId(), earlier.getId())) {
                break;
            }
            if (StringUtils.isBlank(earlier.getResponse())) {
                continue;
            }
            long tokens = tokensOf(inferenceService, earlier) + 2;
            if (previousTokens + tokens > previousRoom) {
                break;
            }
            previous.add(earlier.getResponse());
            previousTokens += tokens;
        }
        Collections.reverse(previous);

        return new Around(String.join(SEPARATOR, summaries), String.join(SEPARATOR, previous), String.join(SEPARATOR, following));
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
