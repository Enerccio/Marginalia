package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

import com.github.enerccio.marginalia.extensions.knowledgetracker.model.*;
import com.github.enerccio.marginalia.extensions.storytracker.log.TrackerChangeLog;
import com.github.enerccio.marginalia.extensions.storytracker.log.TrackerChangeLog.Outcome;

import java.util.List;
import java.util.Objects;

/**
 * The log of what automatic runs applied, over the shared {@link TrackerChangeLog}. Pure functions over the state.
 * <p>
 * An entry is reverted by what it did: an added item is declined (kept as old knowledge, like an addition the user unchecks in
 * the review), a retracted item is made active again. It is only reverted while the item is still as the run left it; an item
 * that was edited, deleted or handled by hand since is not touched.
 */
public final class ChangeLog {

    /** The log keeps this many entries, the oldest go first. */
    public static final int KEEP = TrackerChangeLog.KEEP;

    private ChangeLog() {
    }

    /** Adds entries, newest last, and drops the oldest over {@link #KEEP}. */
    public static void add(KnowledgeState state, List<ChangeLogEntry> entries) {
        TrackerChangeLog.add(state.getChangeLog(), entries);
    }

    /** Whether {@link #revert} would change something now. */
    public static boolean canRevert(KnowledgeState state, ChangeLogEntry entry) {
        return TrackerChangeLog.canRevert(entry, undo(state));
    }

    public static Outcome revert(KnowledgeState state, String entryId) {
        return TrackerChangeLog.revert(state.getChangeLog(), entryId, undo(state));
    }

    /** Removes the log. */
    public static void clear(KnowledgeState state) {
        state.getChangeLog().clear();
    }

    private static TrackerChangeLog.Undo<ChangeLogEntry> undo(KnowledgeState state) {
        return new TrackerChangeLog.Undo<>() {
            @Override
            public Outcome check(ChangeLogEntry entry) {
                KnowledgeItem item = item(state, entry);
                if (item == null) {
                    return Outcome.GONE;
                }
                if (entry.getType() == ChangeLogEntry.Type.ADDED) {
                    return item.isActive() ? Outcome.REVERTED : Outcome.ALREADY_HANDLED;
                }
                boolean asTheRunLeftIt = !item.isActive() && (item.getOldReason() == OldReason.TIDIED
                        || item.getOldReason() == OldReason.RETRACTED && Objects.equals(item.getRetractedAt(), entry.getMessageUuid()));
                return asTheRunLeftIt ? Outcome.REVERTED : Outcome.ALREADY_HANDLED;
            }

            @Override
            public void undo(ChangeLogEntry entry) {
                KnowledgeItem item = item(state, entry);
                if (entry.getType() == ChangeLogEntry.Type.ADDED) {
                    item.reject();
                } else {
                    item.restore();
                }
            }
        };
    }

    private static KnowledgeItem item(KnowledgeState state, ChangeLogEntry entry) {
        TrackedCharacter character = state.findCharacter(entry.getCharacterId());
        return character == null ? null : character.findItem(entry.getItemId());
    }
}
