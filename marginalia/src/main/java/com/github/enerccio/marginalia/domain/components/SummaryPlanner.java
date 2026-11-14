package com.github.enerccio.marginalia.domain.components;

import com.github.enerccio.marginalia.domain.model.impl.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Decides where the summaries of a long stretch of the story go (<i>Create summaries</i>): which parts get a summary, so that the
 * parts between two of them are the block that summary covers. Pure, it reads the parts it is given and nothing else.
 */
public final class SummaryPlanner {

    /**
     * Where a block ends.
     */
    public enum Mode {
        /**
         * When the parts of the block add up to the target, at the end of the part that reached it (so a block has at least the
         * target, or the rest of the story).
         */
        TOKENS,
        /**
         * Before a part that starts a chapter, but only when the block so far has the minimum of tokens: a chapter of a few short
         * parts has no summary of its own, it is the beginning of the next block.
         */
        CHAPTERS,
        /**
         * After the given number of parts.
         */
        PARTS
    }

    /**
     * @param mode        where a block ends
     * @param amount      the target tokens of {@link Mode#TOKENS}, the parts of {@link Mode#PARTS}; not used for chapters
     * @param minTokens   for {@link Mode#CHAPTERS}: the least tokens a block has before a chapter ends it
     * @param includeRest whether the parts after the last cut, a block that did not reach its end, get a summary too
     */
    public record Options(Mode mode, int amount, long minTokens, boolean includeRest) {
        public Options {
            if (mode == null) {
                throw new IllegalArgumentException("mode");
            }
            if (mode != Mode.CHAPTERS && amount < 1) {
                throw new IllegalArgumentException("amount");
            }
        }
    }

    private static final Pattern HEADING = Pattern.compile("^\\s*(#{1,6})\\s+\\S");

    private SummaryPlanner() {
    }

    /**
     * The level of the heading the text starts with ({@code #} is 1), 0 when it does not start with a heading.
     */
    static int headingLevel(String text) {
        if (text == null) {
            return 0;
        }
        Matcher matcher = HEADING.matcher(text);
        return matcher.find() ? matcher.group(1).length() : 0;
    }

    /**
     * Which of the parts start a chapter: the ones that start with a heading of the shallowest level any part starts with (a book
     * that makes its chapters {@code ##} has them so, a deeper heading that starts a part is a section of a chapter).
     *
     * @return a flag for each part, in the order of the parts
     */
    public static boolean[] chapterStarts(List<ChatMessage> parts) {
        int shallowest = Integer.MAX_VALUE;
        int[] levels = new int[parts.size()];
        for (int i = 0; i < levels.length; i++) {
            levels[i] = headingLevel(parts.get(i).getResponse());
            if (levels[i] > 0) {
                shallowest = Math.min(shallowest, levels[i]);
            }
        }
        boolean[] starts = new boolean[levels.length];
        for (int i = 0; i < levels.length; i++) {
            starts[i] = levels[i] > 0 && levels[i] == shallowest;
        }
        return starts;
    }

    /**
     * The parts that get a summary, the oldest first. A summary covers its part and the parts before it down to the part after
     * the one with the summary before it, so cutting {@code parts} gives blocks that follow each other without a gap.
     *
     * @param parts      the parts to cover, the oldest first (the story after the newest summary)
     * @param options    where the blocks end
     * @param blockLimit the most tokens a block may have (what the model can read); a block that would get over it ends before the
     *                   part that carries it over; zero is no limit. A single part over the limit is a block of its own.
     */
    public static List<ChatMessage> plan(List<ChatMessage> parts, Options options, long blockLimit) {
        List<ChatMessage> cuts = new ArrayList<>();
        boolean[] starts = options.mode() == Mode.CHAPTERS ? chapterStarts(parts) : new boolean[parts.size()];
        long sum = 0;
        int count = 0;
        for (int i = 0; i < parts.size(); i++) {
            ChatMessage part = parts.get(i);
            long tokens = part.getTokenCount() + 1;
            if (count > 0 && options.mode() == Mode.CHAPTERS && starts[i] && sum >= options.minTokens()) {
                cuts.add(parts.get(i - 1));
                sum = 0;
                count = 0;
            }
            if (count > 0 && blockLimit > 0 && sum + tokens > blockLimit) {
                cuts.add(parts.get(i - 1));
                sum = 0;
                count = 0;
            }
            sum += tokens;
            count++;
            boolean ends = switch (options.mode()) {
                case TOKENS -> sum >= options.amount();
                case PARTS -> count >= options.amount();
                case CHAPTERS -> false;
            };
            if (ends) {
                cuts.add(part);
                sum = 0;
                count = 0;
            }
        }
        // the rest: in chapters mode it must have the minimum as well, else a short last chapter would have a summary of its own
        if (count > 0 && options.includeRest() && (options.mode() != Mode.CHAPTERS || sum >= options.minTokens())) {
            cuts.add(parts.getLast());
        }
        return cuts;
    }
}
