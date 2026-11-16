package com.github.enerccio.marginalia.extensions.storytracker.log;

import com.github.enerccio.marginalia.extensions.storytracker.log.TrackerChangeLog.Outcome;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The shared log: it keeps the newest entries, reverts an entry once, and only when the tracker's check allows it.
 */
class TrackerChangeLogTest {

    /** An undo that says what it is told and counts what it undid. */
    private static final class FakeUndo implements TrackerChangeLog.Undo<LogEntry> {
        Outcome answer = Outcome.REVERTED;
        final List<String> undone = new ArrayList<>();

        @Override
        public Outcome check(LogEntry entry) {
            return answer;
        }

        @Override
        public void undo(LogEntry entry) {
            undone.add(entry.getText());
        }
    }

    private static LogEntry entry(String text) {
        return new LogEntry(1, "c", "Fiora", "i", text, "p1", "quote");
    }

    @Test
    void theOldestEntriesGoFirst() {
        List<LogEntry> log = new ArrayList<>();
        for (int i = 0; i < TrackerChangeLog.KEEP + 5; i++) {
            TrackerChangeLog.add(log, List.of(entry("t" + i)));
        }
        assertEquals(TrackerChangeLog.KEEP, log.size());
        assertEquals("t5", log.getFirst().getText());
    }

    @Test
    void anEntryIsRevertedOnce() {
        List<LogEntry> log = new ArrayList<>(List.of(entry("rope")));
        FakeUndo undo = new FakeUndo();
        assertTrue(TrackerChangeLog.canRevert(log.getFirst(), undo));
        assertEquals(Outcome.REVERTED, TrackerChangeLog.revert(log, log.getFirst().getId(), undo));
        assertTrue(log.getFirst().isReverted());
        assertFalse(TrackerChangeLog.canRevert(log.getFirst(), undo));
        assertEquals(Outcome.ALREADY_HANDLED, TrackerChangeLog.revert(log, log.getFirst().getId(), undo));
        assertEquals(List.of("rope"), undo.undone);
        assertEquals(Outcome.UNKNOWN, TrackerChangeLog.revert(log, "nope", undo));
    }

    @Test
    void whatTheCheckRefusesIsNotUndone() {
        List<LogEntry> log = new ArrayList<>(List.of(entry("rope")));
        FakeUndo undo = new FakeUndo();
        undo.answer = Outcome.GONE;
        assertFalse(TrackerChangeLog.canRevert(log.getFirst(), undo));
        assertEquals(Outcome.GONE, TrackerChangeLog.revert(log, log.getFirst().getId(), undo));
        assertFalse(log.getFirst().isReverted());
        assertTrue(undo.undone.isEmpty());
    }
}
