package com.github.enerccio.marginalia.extensions.bookdescription.service;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Works out what is sent to the model for a range of the story: the pieces (messages, or the summaries that stand in for the
 * messages they cover), and how the pieces are divided when they are more than the model takes at once. No services, so the
 * rules can be tested.
 */
public final class StoryPlanner {

    /** What a heading of a piece costs in tokens, counted into the size of every piece. */
    static final long HEADER_TOKENS = 12;

    /** A part of a story is never smaller than this, however little room is left. */
    public static final long MIN_BUDGET = 200;

    private StoryPlanner() {
    }

    /**
     * A message of the active branch.
     *
     * @param order         the place in the branch, from 1 (the number the outline of the story shows)
     * @param number        the place among the messages with text, from 1; 0 for a message without text
     * @param text          the text of the message, blank for none
     * @param tokens        the approximate number of tokens of the text
     * @param summary       when the message holds a summary that still describes the story (the head of a valid block), its text; else null
     * @param summaryTokens the approximate number of tokens of the summary
     * @param blockFirst    when {@code summary} is set, the place of the first message the summary covers
     */
    public record BranchEntry(int order, int number, String text, long tokens, String summary, long summaryTokens, int blockFirst) {

        public static BranchEntry message(int order, int number, String text, long tokens) {
            return new BranchEntry(order, number, text, tokens, null, 0, order);
        }

        public BranchEntry withSummary(String summary, long summaryTokens, int blockFirst) {
            return new BranchEntry(order, number, text, tokens, summary, summaryTokens, blockFirst);
        }

        boolean hasText() {
            return StringUtils.isNotBlank(text);
        }
    }

    /**
     * One piece of what is sent: a message, a summary or a part of a message that does not fit alone.
     *
     * @param label the heading, e.g. {@code Message 4} or {@code Messages 2-9 (summary)}
     */
    public record Piece(String label, String text, long tokens) {

        /** What the piece takes in a prompt, with its heading. */
        public long cost() {
            return tokens + HEADER_TOKENS;
        }
    }

    /**
     * The pieces for the messages from {@code fromOrder} to {@code toOrder} of the branch.
     *
     * @param branch       the whole active branch, oldest first
     * @param useSummaries a summary that covers only messages of the range stands in for them; one that reaches outside of the range
     *                     (or that no longer describes the story) is not used, its messages are
     */
    public static List<Piece> pieces(List<BranchEntry> branch, int fromOrder, int toOrder, boolean useSummaries) {
        Map<Integer, BranchEntry> blocks = new HashMap<>();
        if (useSummaries) {
            for (BranchEntry entry : branch) {
                if (entry.summary() != null && StringUtils.isNotBlank(entry.summary())) {
                    blocks.put(entry.blockFirst(), entry);
                }
            }
        }
        List<Piece> pieces = new ArrayList<>();
        int order = Math.max(1, fromOrder);
        int last = Math.min(toOrder, branch.size());
        while (order <= last) {
            BranchEntry block = blocks.get(order);
            if (block != null && block.order() <= last && block.order() >= order) {
                pieces.add(new Piece(summaryLabel(branch, order, block.order()), block.summary().strip(), Math.max(1, block.summaryTokens())));
                order = block.order() + 1;
                continue;
            }
            BranchEntry entry = branch.get(order - 1);
            if (entry.hasText()) {
                pieces.add(new Piece("Message " + entry.number(), entry.text().strip(), Math.max(1, entry.tokens())));
            }
            order++;
        }
        return pieces;
    }

    private static String summaryLabel(List<BranchEntry> branch, int first, int last) {
        int from = 0;
        int to = 0;
        for (int i = first; i <= last; i++) {
            int number = branch.get(i - 1).number();
            if (number > 0) {
                if (from == 0) {
                    from = number;
                }
                to = number;
            }
        }
        if (from == 0) {
            return "Summary";
        }
        return from == to ? "Summary of message " + from : "Summary of messages " + from + "-" + to;
    }

    /** The pieces as the prompt shows them, each under its heading. */
    public static String render(List<Piece> pieces) {
        StringBuilder text = new StringBuilder();
        for (Piece piece : pieces) {
            text.append("--- ").append(piece.label()).append(" ---\n").append(piece.text()).append("\n\n");
        }
        return text.toString().stripTrailing();
    }

    /** The tokens the pieces take in a prompt. */
    public static long cost(List<Piece> pieces) {
        return pieces.stream().mapToLong(Piece::cost).sum();
    }

    /**
     * Divides the pieces into parts of at most {@code budget} tokens (headings counted), in order. A piece larger than the budget is
     * cut into pieces that fit, at a paragraph, a line, a sentence or a word.
     *
     * @throws IllegalArgumentException when the budget is below {@link #MIN_BUDGET}
     */
    public static List<List<Piece>> chunk(List<Piece> pieces, long budget) {
        if (budget < MIN_BUDGET) {
            throw new IllegalArgumentException("The room for the story is too small: " + budget + " tokens");
        }
        List<List<Piece>> parts = new ArrayList<>();
        List<Piece> current = new ArrayList<>();
        long used = 0;
        for (Piece piece : pieces) {
            for (Piece fitting : piece.cost() > budget ? split(piece, budget) : List.of(piece)) {
                if (!current.isEmpty() && used + fitting.cost() > budget) {
                    parts.add(current);
                    current = new ArrayList<>();
                    used = 0;
                }
                current.add(fitting);
                used += fitting.cost();
            }
        }
        if (!current.isEmpty()) {
            parts.add(current);
        }
        return parts;
    }

    /** Cuts a piece into pieces of at most {@code budget} tokens (heading counted) by the proportion of its characters. */
    static List<Piece> split(Piece piece, long budget) {
        long room = Math.max(1, budget - HEADER_TOKENS);
        String text = piece.text();
        // the tokens of the text are spread evenly over its characters, with a margin for the cuts that are not even
        int maxChars = (int) Math.max(40, Math.min(Integer.MAX_VALUE, (long) text.length() * room * 9 / (Math.max(1, piece.tokens()) * 10)));
        List<String> texts = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(text.length(), start + maxChars);
            if (end < text.length()) {
                end = cutPoint(text, start, end);
            }
            String part = text.substring(start, end).strip();
            if (!part.isEmpty()) {
                texts.add(part);
            }
            start = end;
        }
        List<Piece> pieces = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            String part = texts.get(i);
            long tokens = Math.max(1, piece.tokens() * part.length() / Math.max(1, text.length()));
            pieces.add(new Piece(piece.label() + " (part " + (i + 1) + "/" + texts.size() + ")", part, tokens));
        }
        return pieces;
    }

    /** The place to cut {@code text} at, between a third of the way from {@code start} to {@code end} and {@code end}. */
    private static int cutPoint(String text, int start, int end) {
        int floor = start + (end - start) / 3;
        for (String separator : new String[] {"\n\n", "\n", ". ", "! ", "? ", " "}) {
            int at = text.lastIndexOf(separator, end - separator.length());
            if (at >= floor) {
                return at + separator.length();
            }
        }
        return end;
    }
}
