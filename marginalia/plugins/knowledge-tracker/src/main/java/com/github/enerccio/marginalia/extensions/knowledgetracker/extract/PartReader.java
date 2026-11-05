package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

import com.github.enerccio.marginalia.extensions.knowledgetracker.process.Part;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * What the model may read of the story while it works out what the characters learned: the parts before the ones of the
 * run (the parts of the run are in the request already, and the later ones would tell it what has not happened yet).
 * A part is addressed by its number, its position in the branch, which is what the tab shows ("Part 12") and what the
 * request says an item was learned in. Never throws: what the model asks wrongly it is told in words.
 */
public final class PartReader {

    /** How many parts the model may read in one request. */
    public static final int MAX_READS = 5;

    private final List<Part> branch;
    private final int readable;
    private final int maxChars;
    private final IntConsumer onRead;
    private final List<Integer> reads = new ArrayList<>();

    /**
     * @param branch   the parts of the active branch, part {@code n} is {@code branch.get(n - 1)}
     * @param readable how many parts from the start may be read (the ones before the run)
     * @param maxChars the longest text given back, a longer part is cut and says so
     * @param onRead   told the number of every part that was read, may be {@code null}
     */
    public PartReader(List<Part> branch, int readable, int maxChars, IntConsumer onRead) {
        this.branch = branch;
        this.readable = Math.max(0, Math.min(readable, branch.size()));
        this.maxChars = Math.max(200, maxChars);
        this.onRead = onRead;
    }

    /** The same reader (nothing read yet) that tells {@code listener} every part that is read. */
    public PartReader listening(IntConsumer listener) {
        return new PartReader(branch, readable, maxChars, listener);
    }

    /** How many parts from the start can be read. */
    public int readable() {
        return readable;
    }

    /** Whether there is anything to read. */
    public boolean isUseful() {
        return readable > 0;
    }

    /** The text of part {@code number}, or the reason it can not be read. */
    public synchronized String read(int number) {
        if (number < 1 || number > readable) {
            return readable == 0 ? "There is no earlier part to read."
                    : "Part " + number + " can not be read: you can read the parts 1 to " + readable + ".";
        }
        if (reads.contains(number)) {
            return "You have read part " + number + " already; it is above.";
        }
        if (reads.size() >= MAX_READS) {
            return "You have read " + MAX_READS + " parts, that is all you may read: decide with what you have.";
        }
        reads.add(number);
        if (onRead != null) {
            onRead.accept(number);
        }
        String text = StringUtils.defaultString(branch.get(number - 1).text()).strip();
        if (text.length() > maxChars) {
            text = text.substring(0, maxChars) + "\n[The part goes on; the rest is cut.]";
        }
        return "Part " + number + ":\n" + text;
    }

    /** The numbers of the parts that were read, in order. */
    public synchronized List<Integer> reads() {
        return List.copyOf(reads);
    }
}
