package com.github.enerccio.marginalia.domain.components;

import java.util.List;

/**
 * Which lorebook entries the generations of a book used, counted from the {@link LoreUsage} records of its parts (see
 * {@code LoreUsageService}). The parts are numbered as in the outline of the story: the first part of a branch is 1.
 *
 * @param scope          the parts that were counted
 * @param from           the first part number of the range, 1 when it was not limited
 * @param to             the last part number of the range, 0 when it was not limited
 * @param parts          the parts in the scope and the range
 * @param recordedParts  the parts among them that have a record (the others were typed by hand, made before the records
 *                       existed, or belong to a book that had no lorebook); the shares are of these
 * @param hasLorebook    the book has a lorebook now
 * @param rows           one row for every entry of the lorebooks of the book and for every other entry a part used, the
 *                       most used first
 */
public record LoreUsageReport(Scope scope, int from, int to, int parts, int recordedParts, boolean hasLorebook, List<Row> rows) {

    /**
     * The parts a report counts.
     */
    public enum Scope {
        /**
         * The parts of the branch the book is on.
         */
        ACTIVE_BRANCH,
        /**
         * Every part of the book, the other branches and the swipes too.
         */
        ALL_BRANCHES
    }

    /**
     * How often an entry was used among the parts that have a record.
     */
    public enum Status {
        /**
         * In none of them.
         */
        NEVER,
        /**
         * In some of them.
         */
        SOMETIMES,
        /**
         * In all of them.
         */
        ALWAYS
    }

    /**
     * One entry.
     *
     * @param entry       the uuid of the entry
     * @param name        its name, null when it cannot be found any more (deleted)
     * @param lorebook    the name of its lorebook, null when unknown
     * @param inLorebook  the entry is in the enabled lorebooks of the book now; a used entry that is not was deleted, moved
     *                    out or its lorebook was switched off or detached
     * @param enabled     the entry is switched on (false also for an entry that cannot be found)
     * @param used        the parts that used it
     * @param firstPart   the number of the first part that used it, 0 when none
     * @param lastPart    the number of the last part that used it, 0 when none
     * @param tokens      the tokens it cost in all those parts
     * @param maxDepth    the deepest pass of the scan depth that used it, 0 when only the prompt did (or none used it)
     * @param status      never, sometimes or always used
     */
    public record Row(String entry, String name, String lorebook, boolean inLorebook, boolean enabled, int used,
                      int firstPart, int lastPart, long tokens, int maxDepth, Status status) {

        /**
         * @return the tokens it cost in a part that used it, on average, 0 when none used it
         */
        public long averageTokens() {
            return used == 0 ? 0 : Math.round((double) tokens / used);
        }
    }
}
