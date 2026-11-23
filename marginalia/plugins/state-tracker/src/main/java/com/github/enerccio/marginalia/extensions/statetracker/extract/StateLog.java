package com.github.enerccio.marginalia.extensions.statetracker.extract;

import com.github.enerccio.marginalia.extensions.statetracker.model.*;
import com.github.enerccio.marginalia.extensions.storytracker.log.TrackerChangeLog;
import com.github.enerccio.marginalia.extensions.storytracker.log.TrackerChangeLog.Outcome;

import java.util.List;
import java.util.Objects;

/**
 * The log of what runs applied without asking, over the shared {@link TrackerChangeLog}. Pure functions over the state.
 * <p>
 * An entry is reverted as if the change had been unchecked in the review: the value it made is deleted, the value it ended is
 * current again. Only while both are as the run left it: a value made by it that was edited, removed or deleted since, or a value
 * ended by it that was restored, deleted or replaced by another one since, is not touched.
 */
public final class StateLog {

    private StateLog() {
    }

    /** Adds entries, newest last, and drops the oldest over {@link TrackerChangeLog#KEEP}. */
    public static void add(StateBook state, List<StateLogEntry> entries) {
        TrackerChangeLog.add(state.getChangeLog(), entries);
    }

    /** Whether {@link #revert} would change something now. */
    public static boolean canRevert(StateBook state, StateLogEntry entry) {
        return TrackerChangeLog.canRevert(entry, undo(state));
    }

    public static Outcome revert(StateBook state, String entryId) {
        return TrackerChangeLog.revert(state.getChangeLog(), entryId, undo(state));
    }

    /** Removes the log. */
    public static void clear(StateBook state) {
        state.getChangeLog().clear();
    }

    private static TrackerChangeLog.Undo<StateLogEntry> undo(StateBook state) {
        return new TrackerChangeLog.Undo<>() {
            @Override
            public Outcome check(StateLogEntry entry) {
                TrackedCharacter character = state.findCharacter(entry.getCharacterId());
                Category category = state.findCategory(entry.getCategoryId());
                if (character == null || category == null) {
                    return Outcome.GONE;
                }
                StateItem created = entry.getItemId() == null ? null : character.findItem(entry.getItemId());
                StateItem ended = entry.getEndedItemId() == null ? null : character.findItem(entry.getEndedItemId());
                if (entry.getItemId() != null && created == null || entry.getEndedItemId() != null && ended == null) {
                    return Outcome.GONE;
                }
                if (created != null && (!created.isActive() || !created.getText().equals(entry.getText()))) {
                    return Outcome.ALREADY_HANDLED;
                }
                if (ended != null) {
                    if (ended.isActive() || !Objects.equals(ended.getEndedBy(), entry.getMessageUuid())) {
                        return Outcome.ALREADY_HANDLED;
                    }
                    // a single value comes back only where nothing else took its place meanwhile
                    boolean taken = category.getKind().isSingleValued()
                            && character.active(category.getId()).stream().anyMatch(item -> item != created);
                    if (taken) {
                        return Outcome.ALREADY_HANDLED;
                    }
                }
                return Outcome.REVERTED;
            }

            @Override
            public void undo(StateLogEntry entry) {
                TrackedCharacter character = state.findCharacter(entry.getCharacterId());
                Category category = state.findCategory(entry.getCategoryId());
                if (entry.getItemId() != null) {
                    character.delete(character.findItem(entry.getItemId()));
                }
                if (entry.getEndedItemId() != null) {
                    character.restore(character.findItem(entry.getEndedItemId()), category, System.currentTimeMillis());
                }
            }
        };
    }
}
