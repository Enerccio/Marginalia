package com.github.enerccio.marginalia.extensions.betareaders.service;

import com.github.enerccio.marginalia.extensions.betareaders.model.BetaChat;
import com.github.enerccio.marginalia.extensions.betareaders.model.BetaMessage;
import com.github.enerccio.marginalia.extensions.betareaders.model.MessageKind;
import com.github.enerccio.marginalia.extensions.betareaders.model.SentPart;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * Knows which parts of the story the personas of a chat have read, and writes what is new.
 * <p>
 * What a chat has sent is worked out from its messages (each user message stores the parts it sent), so deleting or editing a message
 * gives its parts back and nothing else has to be kept in step. A part counts as new when it was never sent, and as changed when its
 * text is not the one that was sent. A part the chat was sent that is not in the story any more (the branch was switched) is gone;
 * the message that reports it forgets it. The first message of a chat sends the context instead: the summaries and then the parts
 * after the newest of them, as far as they fit.
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
     * What stands for the story before the parts that are sent as they are: the summaries, oldest first, and the part with the newest
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
     * @param first   nothing was sent yet: the next message that includes the parts sends the context
     * @param added   parts that were never sent
     * @param changed parts whose text is not the one that was sent
     * @param gone    parts that were sent and are not in the story any more
     */
    public record Status(boolean first, int added, int changed, int gone) {

        /** Whether including the parts would send anything. */
        public boolean pending() {
            return added > 0 || changed > 0 || gone > 0;
        }

        /** The number the checkbox of the message shows. */
        public int count() {
            return added + changed;
        }
    }

    /**
     * What a message sends of the story.
     *
     * @param text     the block that goes before the text of the user; empty when there is nothing to send
     * @param sent     the parts to store with the message (all the parts it considered, also the ones the summaries stand for and the
     *                 ones that did not fit, so they are not sent later out of order)
     * @param gone     the ids of the parts to store as gone with the message
     * @param included how many parts are in the text
     * @param omitted  how many parts did not fit and are left out of the text
     * @param first    the text is the context of the first message
     */
    public record Plan(String text, List<SentPart> sent, List<String> gone, int included, int omitted, boolean first) {

        public static final Plan EMPTY = new Plan("", List.of(), List.of(), 0, 0, false);

        public boolean isEmpty() {
            return text.isEmpty();
        }
    }

    /**
     * A short fingerprint of the text of a part.
     */
    public static String hash(String text) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest((text == null ? "" : text).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                hex.append(String.format("%02x", digest[i]));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    /**
     * The parts the chat has been sent, by id, as its messages leave them: in order, a later message sending a part again replaces
     * what an earlier one sent of it, and a part reported gone is forgotten.
     */
    public static Map<String, SentPart> sentBy(BetaChat chat) {
        Map<String, SentPart> sent = new LinkedHashMap<>();
        for (BetaMessage message : chat.getMessages()) {
            if (message.getKind() != MessageKind.USER) {
                continue;
            }
            message.getGoneParts().forEach(sent::remove);
            message.getSentParts().forEach(part -> sent.put(part.getId(), part));
        }
        return sent;
    }

    /**
     * What including the parts would do now.
     *
     * @param branch    the parts of the active branch, first to last
     * @param maxNumber the last part the personas may read (the spoiler limit); 0 for no limit
     */
    public static Status status(BetaChat chat, List<StoryPart> branch, int maxNumber) {
        Map<String, SentPart> sent = sentBy(chat);
        List<StoryPart> readable = readable(branch, maxNumber);
        int added = 0;
        int changed = 0;
        for (StoryPart part : readable) {
            SentPart before = sent.get(part.id());
            if (before == null) {
                added++;
            } else if (!before.getHash().equals(hash(part.text()))) {
                changed++;
            }
        }
        return new Status(sent.isEmpty(), added, changed, gone(sent, branch).size());
    }

    /**
     * Works out what a message sends.
     *
     * @param branch    the parts of the active branch, first to last
     * @param summaries the summaries of the story, used for the first message
     * @param maxNumber the last part the personas may read; 0 for no limit
     * @param maxTokens the tokens the block may have; 0 for no limit
     * @param tokens    counts the tokens of a text
     */
    public static Plan plan(BetaChat chat, List<StoryPart> branch, Summaries summaries, int maxNumber, int maxTokens,
                            ToIntFunction<String> tokens) {
        Map<String, SentPart> sent = sentBy(chat);
        List<StoryPart> readable = readable(branch, maxNumber);
        return sent.isEmpty()
                ? firstPlan(readable, summaries == null ? Summaries.NONE : summaries, maxTokens, tokens)
                : laterPlan(readable, sent, gone(sent, branch), maxTokens, tokens);
    }

    private static Plan firstPlan(List<StoryPart> readable, Summaries summaries, int maxTokens, ToIntFunction<String> tokens) {
        int covered = -1;
        if (summaries.coveredId() != null) {
            for (int i = 0; i < readable.size(); i++) {
                if (readable.get(i).id().equals(summaries.coveredId())) {
                    covered = i;
                }
            }
        }
        // the summaries only stand for the story when the part they end with is in what the personas may read
        List<String> summaryTexts = covered >= 0 ? summaries.texts().stream().filter(text -> !text.isBlank()).toList() : List.of();
        List<StoryPart> rest = readable.subList(summaryTexts.isEmpty() ? 0 : covered + 1, readable.size());
        if (summaryTexts.isEmpty() && rest.isEmpty()) {
            return Plan.EMPTY;
        }

        StringBuilder text = new StringBuilder("[The story the author shows you]");
        int budget = maxTokens;
        if (!summaryTexts.isEmpty()) {
            String block = "Summaries of the story so far:\n" + String.join("\n\n", summaryTexts);
            text.append("\n").append(block);
            budget -= tokens.applyAsInt(block);
        }
        List<StoryPart> included = fit(rest, maxTokens > 0 ? Math.max(budget, 0) : 0, tokens);
        included.forEach(part -> text.append("\n").append(render(part, "")));
        return new Plan(text.toString(), sentParts(readable), List.of(), included.size(), rest.size() - included.size(), true);
    }

    private static Plan laterPlan(List<StoryPart> readable, Map<String, SentPart> sent, List<SentPart> gone, int maxTokens,
                                  ToIntFunction<String> tokens) {
        List<StoryPart> news = new ArrayList<>();
        Set<String> changedIds = new HashSet<>();
        for (StoryPart part : readable) {
            SentPart before = sent.get(part.id());
            if (before == null) {
                news.add(part);
            } else if (!before.getHash().equals(hash(part.text()))) {
                news.add(part);
                changedIds.add(part.id());
            }
        }
        if (news.isEmpty() && gone.isEmpty()) {
            return Plan.EMPTY;
        }

        StringBuilder text = new StringBuilder("[New in the story since you last read it]");
        if (!gone.isEmpty()) {
            List<Integer> numbers = gone.stream().map(SentPart::getNumber).filter(number -> number > 0).toList();
            String what = numbers.isEmpty() ? "some parts you read are"
                    : numbers.size() == 1 ? "the part " + numbers.getFirst() + " you read is"
                    : "the parts " + ranges(numbers) + " you read are";
            text.append("\n(The story was changed: ").append(what).append(" not there any more.");
            text.append(news.isEmpty() ? ")" : " What follows replaces them.)");
        }
        List<StoryPart> included = fit(news, maxTokens, tokens);
        included.forEach(part -> text.append("\n").append(render(part, changedIds.contains(part.id()) ? " (changed since you read it)" : "")));
        return new Plan(text.toString(), sentParts(news), gone.stream().map(SentPart::getId).toList(),
                included.size(), news.size() - included.size(), false);
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

    private static String render(StoryPart part, String note) {
        return "[Part " + part.number() + note + "]\n" + part.text().strip();
    }

    private static List<SentPart> sentParts(List<StoryPart> parts) {
        return parts.stream().map(part -> new SentPart(part.id(), hash(part.text()), part.number())).toList();
    }

    /** The parts the personas may read: not blank, and not after the limit. */
    private static List<StoryPart> readable(List<StoryPart> branch, int maxNumber) {
        return branch.stream().filter(part -> part.text() != null && !part.text().isBlank())
                .filter(part -> maxNumber <= 0 || part.number() <= maxNumber).toList();
    }

    private static List<SentPart> gone(Map<String, SentPart> sent, List<StoryPart> branch) {
        Set<String> ids = branch.stream().map(StoryPart::id).collect(Collectors.toSet());
        return sent.values().stream().filter(part -> !ids.contains(part.getId())).toList();
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
