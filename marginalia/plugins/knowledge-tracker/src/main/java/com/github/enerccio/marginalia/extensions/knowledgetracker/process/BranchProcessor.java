package com.github.enerccio.marginalia.extensions.knowledgetracker.process;

import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.ProcessedPart;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.TrackedCharacter;
import com.github.enerccio.marginalia.extensions.knowledgetracker.scene.PresentCharacters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Follows the story: keeps the state in line with the active branch, and reads the parts after the cursor in order.
 * Pure logic over the state and a list of parts; the service gives it the branch and stores the result.
 * <p>
 * What it does without a model is the "has met" pass: a tracked character has met every name that is in the present
 * characters field together with it. What the characters learn is a later step that goes through the same cursor.
 */
public final class BranchProcessor {

    private BranchProcessor() {
    }

    /**
     * What a call did.
     *
     * @param rewound   how many things were taken back because the branch changed or a part was reprocessed
     * @param processed how many parts were read
     * @param pending   how many settled parts are still not read (the run was limited)
     */
    public record Report(int rewound, int processed, int pending) {
    }

    /**
     * Brings the state in line with the branch: the cursor is the last part of the contiguous run of processed parts
     * from the start point, and what came from a part that is not on the branch, or lies after the cursor, is taken
     * back. After a swipe, a switch to another branch or a deleted part this is what moves the cursor back to the last
     * part both branches share.
     *
     * @return how many things were taken back
     */
    public static int align(KnowledgeState state, List<Part> branch) {
        Map<String, Integer> index = indexOf(branch);
        Set<String> processed = new HashSet<>();
        state.getProcessed().forEach(p -> processed.add(p.getUuid()));

        int cursor = startIndex(state, index) - 1;
        while (cursor + 1 < branch.size() && processed.contains(branch.get(cursor + 1).uuid())) {
            cursor++;
        }
        final int cursorIndex = cursor;
        return rewindTo(state, branch, uuid -> !index.containsKey(uuid) || index.get(uuid) > cursorIndex,
                cursorIndex >= 0 ? branch.get(cursorIndex).uuid() : null);
    }

    /**
     * Reads the parts after the cursor, as far as they are settled, and notes who has met whom.
     *
     * @param settled how many parts from the start of the branch are settled: the others may still change (a part that is
     *                being generated, redrafted or replaced) and are left alone
     * @param max     the most parts to read in this call
     */
    public static Report process(KnowledgeState state, List<Part> branch, int settled, Collection<String> separators,
                                 long now, int max) {
        int rewound = align(state, branch);
        Map<String, Integer> index = indexOf(branch);
        int next = Math.max(cursorIndex(state, index) + 1, startIndex(state, index));
        int limit = Math.min(settled, branch.size());
        int processed = 0;
        while (next < limit && processed < max) {
            read(state, branch.get(next), separators, now);
            next++;
            processed++;
        }
        return new Report(rewound, processed, Math.max(0, limit - next));
    }

    /**
     * The settled parts after the cursor that the next run would read, at most {@code max}, in order. Brings the state in
     * line with the branch first (see {@link #align}); nothing is read.
     */
    public static List<Part> pending(KnowledgeState state, List<Part> branch, int settled, int max) {
        align(state, branch);
        Map<String, Integer> index = indexOf(branch);
        int next = Math.max(cursorIndex(state, index) + 1, startIndex(state, index));
        int limit = Math.min(settled, branch.size());
        List<Part> parts = new ArrayList<>();
        for (int i = next; i < limit && parts.size() < max; i++) {
            parts.add(branch.get(i));
        }
        return parts;
    }

    /**
     * Takes back everything that came from this part and the parts after it (and from parts that are not on the branch)
     * and puts the cursor just before it, so the next {@link #process} reads it again. Items and retractions made by hand
     * stay.
     *
     * @return how many things were taken back, or -1 when the part is not on the branch
     */
    public static int reprocessFrom(KnowledgeState state, List<Part> branch, String uuid) {
        Map<String, Integer> index = indexOf(branch);
        Integer from = index.get(uuid);
        if (from == null) {
            return -1;
        }
        int before = from - 1;
        // nothing before the start point counts as processed
        return rewindTo(state, branch, other -> !index.containsKey(other) || index.get(other) >= from,
                before >= startIndex(state, index) ? branch.get(before).uuid() : null);
    }

    /**
     * The parts that were processed and whose text is not the one that was processed (the author edited or redrafted
     * them). Nothing is reprocessed because of it, the user decides.
     */
    public static List<String> changedSinceProcessed(KnowledgeState state, List<Part> branch) {
        Map<String, Part> byUuid = new HashMap<>();
        branch.forEach(p -> byUuid.put(p.uuid(), p));
        List<String> changed = new ArrayList<>();
        for (ProcessedPart processed : state.getProcessed()) {
            Part part = byUuid.get(processed.getUuid());
            if (part != null && !TextHash.of(part.text()).equals(processed.getHash())) {
                changed.add(part.uuid());
            }
        }
        return changed;
    }

    /**
     * The position (the first part is 1) of the cursor on the branch, 0 when nothing was processed.
     */
    public static int cursorPosition(KnowledgeState state, List<Part> branch) {
        return cursorIndex(state, indexOf(branch)) + 1;
    }

    /**
     * How many parts from the start of the branch are settled: all of them when something follows the last one is
     * certain (a new generation starts, or the user asks), else all but the last; a part that is being replaced (a
     * regenerate, a swipe) and the ones after it are never settled.
     *
     * @param lastSettled whether the last part of the branch counts as settled
     * @param excludeFrom the uuid of a part that and the later parts are not settled, may be {@code null}
     */
    public static int settledCount(List<Part> branch, boolean lastSettled, String excludeFrom) {
        int count = lastSettled ? branch.size() : Math.max(0, branch.size() - 1);
        if (excludeFrom != null) {
            for (int i = 0; i < branch.size(); i++) {
                if (branch.get(i).uuid().equals(excludeFrom)) {
                    return Math.min(count, i);
                }
            }
        }
        return count;
    }

    // --- internals ---

    private static void read(KnowledgeState state, Part part, Collection<String> separators, long now) {
        List<String> names = PresentCharacters.parse(part.presentCharacters(), separators);
        Set<String> present = PresentCharacters.normalizedNames(part.presentCharacters(), separators);
        for (TrackedCharacter character : state.getCharacters()) {
            if (PresentCharacters.isPresent(present, character.getName(), character.getAliases())) {
                for (String name : names) {
                    character.addMet(name, part.uuid());
                }
            }
        }
        state.getProcessed().removeIf(p -> p.getUuid().equals(part.uuid()));
        state.getProcessed().add(new ProcessedPart(part.uuid(), TextHash.of(part.text()), now));
        state.setCursor(part.uuid());
    }

    private static int rewindTo(KnowledgeState state, List<Part> branch, Predicate<String> dropped, String newCursor) {
        int changed = state.rewind(dropped);
        int before = state.getProcessed().size();
        state.getProcessed().removeIf(p -> dropped.test(p.getUuid()));
        changed += before - state.getProcessed().size();
        state.setCursor(newCursor);
        return changed;
    }

    private static Map<String, Integer> indexOf(List<Part> branch) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < branch.size(); i++) {
            index.put(branch.get(i).uuid(), i);
        }
        return index;
    }

    /** Where processing begins: the start point when it is on the branch, else the first part. */
    private static int startIndex(KnowledgeState state, Map<String, Integer> index) {
        Integer start = state.getStartMessage() == null ? null : index.get(state.getStartMessage());
        return start == null ? 0 : start;
    }

    private static int cursorIndex(KnowledgeState state, Map<String, Integer> index) {
        Integer cursor = state.getCursor() == null ? null : index.get(state.getCursor());
        return cursor == null ? -1 : cursor;
    }
}
