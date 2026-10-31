package com.github.enerccio.marginalia.domain.components;

/**
 * The counters of a {@code ManuscriptStatistic}: what is added to a day of a book ({@link #plus}), and what a sum over days,
 * books or sides comes to. Work adds up; times are not counters, they are intervals.
 *
 * @param prefillTokens   tokens sent to the models
 * @param generatedTokens tokens the models generated
 * @param requests        model calls
 * @param wordsWritten    words added to the text by hand
 * @param wordsGenerated  words added by a model
 * @param wordsRemoved    words taken away, a positive number
 * @param partsCreated    parts that got their first text
 * @param partsDeleted    parts deleted
 * @param edits           new versions of a part that already had a text
 */
public record StatisticCounts(long prefillTokens, long generatedTokens, long requests, long wordsWritten,
                              long wordsGenerated, long wordsRemoved, long partsCreated, long partsDeleted, long edits) {

    public static final StatisticCounts ZERO = new StatisticCounts(0, 0, 0, 0, 0, 0, 0, 0, 0);

    /**
     * One model call.
     */
    public static StatisticCounts tokens(long prefillTokens, long generatedTokens) {
        return new StatisticCounts(Math.max(0, prefillTokens), Math.max(0, generatedTokens), 1, 0, 0, 0, 0, 0, 0);
    }

    public static StatisticCounts wordsWritten(long words) {
        return new StatisticCounts(0, 0, 0, Math.max(0, words), 0, 0, 0, 0, 0);
    }

    public static StatisticCounts wordsGenerated(long words) {
        return new StatisticCounts(0, 0, 0, 0, Math.max(0, words), 0, 0, 0, 0);
    }

    public static StatisticCounts wordsRemoved(long words) {
        return new StatisticCounts(0, 0, 0, 0, 0, Math.max(0, words), 0, 0, 0);
    }

    public static StatisticCounts partsCreated(long parts) {
        return new StatisticCounts(0, 0, 0, 0, 0, 0, Math.max(0, parts), 0, 0);
    }

    public static StatisticCounts partsDeleted(long parts) {
        return new StatisticCounts(0, 0, 0, 0, 0, 0, 0, Math.max(0, parts), 0);
    }

    public static StatisticCounts edits(long edits) {
        return new StatisticCounts(0, 0, 0, 0, 0, 0, 0, 0, Math.max(0, edits));
    }

    public StatisticCounts plus(StatisticCounts other) {
        return new StatisticCounts(prefillTokens + other.prefillTokens, generatedTokens + other.generatedTokens,
                requests + other.requests, wordsWritten + other.wordsWritten, wordsGenerated + other.wordsGenerated,
                wordsRemoved + other.wordsRemoved, partsCreated + other.partsCreated, partsDeleted + other.partsDeleted,
                edits + other.edits);
    }

    public boolean isZero() {
        return equals(ZERO);
    }

    /**
     * All the words added, by hand and by a model.
     */
    public long wordsAdded() {
        return wordsWritten + wordsGenerated;
    }

}
