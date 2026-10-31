package com.github.enerccio.marginalia.extensions.betareaders.service;

import com.github.enerccio.marginalia.extensions.betareaders.model.BetaChat;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaMessage;
import com.github.enerccio.marginalia.extensions.betareaders.model.MessageKind;
import com.github.enerccio.marginalia.extensions.betareaders.model.SentPart;

import java.util.*;
import java.util.function.ToIntFunction;

/**
 * Knows which parts of the story the personas of a chat may read, and writes the story they read.
 * <p>
 * The story is never stored in the chat. A chat only remembers which parts it was <i>given</i>: each message of the user that included
 * the latest parts lists them (their ids), so deleting such a message takes the parts back, and a chat that was only talked to has
 * been given none. This is what lets the author wait and send the story later. The text the personas read is built from the story as
 * it is now, for every request: the parts they were given, in their current words, with the summaries standing in for the oldest of
 * them. An edited part is therefore read as edited, a part that is gone from the branch is not read, and the story is in the prompt
 * once. The settings that limit what is read (the spoiler limit, the first part, the summaries) are inputs of that, so changing them
 * changes the story the personas read from the next request on.
 * <p>
 * It works on {@link StoryPart}s, so it needs no entity; {@link BetaReadersService} reads them from the active branch of the book.
 */
public final class PartsTracker {

    /** The tokens put on every part besides its text (the label). */
    private static final int PART_OVERHEAD = 8;

    private PartsTracker() {
    }

    /**
     * A part of the story as the personas see it.
     *
     * @param id     what identifies the part
     * @param number the place of the part in the active branch, from 1
     * @param text   the text of the part
     */
    public record StoryPart(String id, int number, String text) {
    }

    /**
     * What stands for the story before the parts that are shown as they are: the summaries, oldest first, and the part with the newest
     * of them; it and the parts before it are what the summaries describe.
     */
    public record Summaries(List<String> texts, String coveredId) {

        public static final Summaries NONE = new Summaries(List.of(), null);

        public Summaries {
            texts = texts == null ? List.of() : List.copyOf(texts);
        }
    }

    /**
     * How the chat stands against the story now.
     *
     * @param first nothing was given to the chat yet: including the parts gives it the story from the beginning
     * @param added parts the personas may read that they were not given
     */
    public record Status(boolean first, int added) {

        /** Whether including the parts would give the chat anything. */
        public boolean pending() {
            return added > 0;
        }

        /** The number the checkbox of the message shows. */
        public int count() {
            return added;
        }
    }

    /**
     * The story the personas read.
     *
     * @param text     the block that goes at the start of the history; empty when they were given no part
     * @param included how many parts are in the text as they are
     * @param omitted  how many parts they were given that did not fit and are left out of the text
     */
    public record Block(String text, int included, int omitted) {

        public static final Block EMPTY = new Block("", 0, 0);

        public boolean isEmpty() {
            return text.isEmpty();
        }
    }

    /**
     * The parts the chat has been given, by id, in the order of the messages that gave them. Deleting such a message takes its parts back.
     */
    public static Map<String, SentPart> sentBy(BetaChat chat) {
        Map<String, SentPart> sent = new LinkedHashMap<>();
        for (BetaMessage message : chat.getMessages()) {
            if (message.getKind() == MessageKind.USER) {
                message.getSentParts().forEach(part -> sent.put(part.getId(), part));
            }
        }
        return sent;
    }

    /**
     * The parts a message that includes the latest parts gives the chat: the ones the personas may read that they were not given.
     */
    public static List<SentPart> unreleased(BetaChat chat, List<StoryPart> branch, int minNumber, int maxNumber) {
        Map<String, SentPart> sent = sentBy(chat);
        return readable(branch, minNumber, maxNumber).stream().filter(part -> !sent.containsKey(part.id()))
                .map(part -> new SentPart(part.id(), part.number())).toList();
    }

    /**
     * What including the latest parts would do now.
     *
     * @param branch    the parts of the active branch, first to last
     * @param minNumber the first part the personas read; 0 for the first part of the story
     * @param maxNumber the last part the personas may read (the spoiler limit); 0 for no limit
     */
    public static Status status(BetaChat chat, List<StoryPart> branch, int minNumber, int maxNumber) {
        return new Status(sentBy(chat).isEmpty(), unreleased(chat, branch, minNumber, maxNumber).size());
    }

    /**
     * Writes the story the personas read: the parts they were given that are in the branch and in the range they may read, with the
     * summaries in place of the oldest when they stand for them, and as many of the newest parts as fit the budget.
     *
     * @param chat      the chat (or a view of it with the message about to be sent) that knows what it was given
     * @param branch    the parts of the active branch, first to last
     * @param summaries the summaries of the story; they are used when the personas read from the beginning and the part they end with
     *                  is among the parts they were given
     * @param minNumber the first part the personas read; 0 for the first part of the story
     * @param maxNumber the last part the personas may read; 0 for no limit
     * @param maxTokens the tokens the block may have; 0 for no limit
     * @param tokens    counts the tokens of a text
     */
    public static Block story(BetaChat chat, List<StoryPart> branch, Summaries summaries, int minNumber, int maxNumber, int maxTokens,
                              ToIntFunction<String> tokens) {
        Map<String, SentPart> sent = sentBy(chat);
        List<StoryPart> given = readable(branch, minNumber, maxNumber).stream().filter(part -> sent.containsKey(part.id())).toList();
        Summaries usable = summaries == null || minNumber > 1 ? Summaries.NONE : summaries;

        int covered = -1;
        if (usable.coveredId() != null) {
            for (int i = 0; i < given.size(); i++) {
                if (given.get(i).id().equals(usable.coveredId())) {
                    covered = i;
                }
            }
        }
        // the summaries only stand for the story when the part they end with is among the parts the personas read
        List<String> summaryTexts = covered >= 0 ? usable.texts().stream().filter(text -> !text.isBlank()).toList() : List.of();
        List<StoryPart> rest = given.subList(summaryTexts.isEmpty() ? 0 : covered + 1, given.size());
        if (summaryTexts.isEmpty() && rest.isEmpty()) {
            return Block.EMPTY;
        }

        StringBuilder text = new StringBuilder("[The story the author shows you]");
        int budget = maxTokens;
        if (!summaryTexts.isEmpty()) {
            String summaries1 = "Summaries of the story so far:\n" + String.join("\n\n", summaryTexts);
            text.append("\n").append(summaries1);
            budget -= tokens.applyAsInt(summaries1);
        }
        List<StoryPart> included = fit(rest, maxTokens > 0 ? Math.max(budget, 1) : 0, tokens);
        int omitted = rest.size() - included.size();
        if (omitted > 0) {
            text.append("\n(The ").append(omitted == 1 ? "part" : omitted + " parts").append(" before the part ")
                    .append(included.isEmpty() ? "you can see" : included.getFirst().number())
                    .append(" did not fit and ").append(omitted == 1 ? "is" : "are").append(" left out.)");
        }
        included.forEach(part -> text.append("\n").append(render(part)));
        return new Block(text.toString(), included.size(), omitted);
    }

    /**
     * The newest parts that fit the budget, in story order.
     */
    private static List<StoryPart> fit(List<StoryPart> parts, int maxTokens, ToIntFunction<String> tokens) {
        if (maxTokens <= 0) {
            return new ArrayList<>(parts);
        }
        List<StoryPart> fitting = new ArrayList<>();
        int total = 0;
        for (int i = parts.size() - 1; i >= 0; i--) {
            total += tokens.applyAsInt(parts.get(i).text()) + PART_OVERHEAD;
            if (total > maxTokens) {
                break;
            }
            fitting.addFirst(parts.get(i));
        }
        return fitting;
    }

    private static String render(StoryPart part) {
        return "[Part " + part.number() + "]\n" + part.text().strip();
    }

    /** The parts the personas may read: not blank, from the first part they read to the spoiler limit. */
    private static List<StoryPart> readable(List<StoryPart> branch, int minNumber, int maxNumber) {
        return branch.stream().filter(part -> part.text() != null && !part.text().isBlank())
                .filter(part -> minNumber <= 0 || part.number() >= minNumber)
                .filter(part -> maxNumber <= 0 || part.number() <= maxNumber).toList();
    }

    /**
     * The numbers of the parts the chat has been given, as ranges ({@code 1-5, 8}); empty when it has been given none.
     */
    public static String read(BetaChat chat) {
        return ranges(sentBy(chat).values().stream().map(SentPart::getNumber).filter(number -> number > 0).toList());
    }

    /**
     * Numbers as ranges: {@code 41-43, 50}.
     */
    public static String ranges(List<Integer> numbers) {
        List<String> ranges = new ArrayList<>();
        Integer start = null;
        Integer previous = null;
        for (int number : new TreeSet<>(numbers)) {
            if (previous != null && number == previous + 1) {
                previous = number;
                continue;
            }
            if (start != null) {
                ranges.add(range(start, previous));
            }
            start = number;
            previous = number;
        }
        if (start != null) {
            ranges.add(range(start, previous));
        }
        return String.join(", ", ranges);
    }

    private static String range(int from, int to) {
        return from == to ? String.valueOf(from) : from + "-" + to;
    }
}
