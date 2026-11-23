package com.github.enerccio.marginalia.extensions.storytracker.process;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The processor on a tracker that is not the Knowledge Tracker: it only counts on {@link TrackerProgress}.
 */
class BranchProcessorTest {

    /** A tracker that keeps the uuid of every part it read, as if it took something from each. */
    static final class Notes implements TrackerProgress {
        String start;
        String cursor;
        final List<ProcessedPart> processed = new ArrayList<>();
        final List<String> notes = new ArrayList<>();

        @Override
        public String getStartMessage() {
            return start;
        }

        @Override
        public String getCursor() {
            return cursor;
        }

        @Override
        public void setCursor(String cursor) {
            this.cursor = cursor;
        }

        @Override
        public List<ProcessedPart> getProcessed() {
            return processed;
        }

        @Override
        public int rewind(Predicate<String> dropped) {
            int before = notes.size();
            notes.removeIf(dropped);
            return before - notes.size();
        }

        @Override
        public void read(Part part, Collection<String> separators) {
            notes.add(part.uuid());
        }
    }

    private static List<Part> branch(String... uuids) {
        List<Part> parts = new ArrayList<>();
        for (String uuid : uuids) {
            parts.add(new Part(uuid, "", "text of " + uuid));
        }
        return parts;
    }

    @Test
    void readsTheSettledPartsAndMovesTheCursor() {
        Notes state = new Notes();
        List<Part> branch = branch("a", "b", "c");
        BranchProcessor.Report report = BranchProcessor.process(state, branch, BranchProcessor.settledCount(branch, false, null), List.of(), 1, 10);
        assertEquals(2, report.processed());
        assertEquals(0, report.pending());
        assertEquals(List.of("a", "b"), state.notes);
        assertEquals("b", state.cursor);
        assertEquals(2, BranchProcessor.cursorPosition(state, branch));
    }

    @Test
    void anotherBranchTakesBackWhatTheDroppedPartsGave() {
        Notes state = new Notes();
        BranchProcessor.process(state, branch("a", "b", "c"), 3, List.of(), 1, 10);
        // a swipe replaced c, then b: the cursor goes back to a
        int rewound = BranchProcessor.align(state, branch("a", "x"));
        assertEquals(List.of("a"), state.notes);
        assertEquals("a", state.cursor);
        assertEquals(1, state.processed.size());
        assertTrue(rewound >= 2);
    }

    @Test
    void reprocessFromAPartReadsItAgain() {
        Notes state = new Notes();
        List<Part> branch = branch("a", "b", "c");
        BranchProcessor.process(state, branch, 3, List.of(), 1, 10);
        assertTrue(BranchProcessor.reprocessFrom(state, branch, "b") > 0);
        assertEquals("a", state.cursor);
        assertEquals(List.of(branch.get(1), branch.get(2)), BranchProcessor.pending(state, branch, 3, 10));
        assertEquals(-1, BranchProcessor.reprocessFrom(state, branch, "missing"));
    }

    @Test
    void theStartPointSkipsTheEarlierParts() {
        Notes state = new Notes();
        state.start = "b";
        BranchProcessor.process(state, branch("a", "b", "c"), 3, List.of(), 1, 10);
        assertEquals(List.of("b", "c"), state.notes);
    }

    @Test
    void aChangedTextIsReported() {
        Notes state = new Notes();
        BranchProcessor.process(state, branch("a", "b"), 2, List.of(), 1, 10);
        List<Part> edited = List.of(new Part("a", "", "text of a"), new Part("b", "", "edited"));
        assertEquals(List.of("b"), BranchProcessor.changedSinceProcessed(state, edited));
    }
}
