package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

import com.github.enerccio.marginalia.extensions.knowledgetracker.model.ChangeLogEntry;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeItem;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.OldReason;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.TrackedCharacter;

import java.util.List;
import java.util.Objects;

/**
 * The log of what automatic runs applied, and taking an entry back. Pure functions over the state.
 * <p>
 * An entry is reverted by what it did: an added item is declined (kept as old knowledge, like an addition the user unchecks in
 * the review), a retracted item is made active again. It is only reverted while the item is still as the run left it; an item
 * that was edited, deleted or handled by hand since is not touched.
 */
public final class ChangeLog {

    /** The log keeps this many entries, the oldest go first. */
    public static final int KEEP = 200;

    private ChangeLog() {
    }

    public enum Outcome {
        REVERTED,
        /** The item is not as the run left it any more (the user dealt with it already). */
        ALREADY_HANDLED,
        /** The item or its character is gone. */
        GONE,
        /** There is no such entry. */
        UNKNOWN
    }

    /** Adds entries, newest last, and drops the oldest over {@link #KEEP}. */
    public static void add(KnowledgeState state, List<ChangeLogEntry> entries) {
        List<ChangeLogEntry> log = state.getChangeLog();
        log.addAll(entries);
        while (log.size() > KEEP) {
            log.removeFirst();
        }
    }

    /** Whether {@link #revert} would change something now. */
    public static boolean canRevert(KnowledgeState state, ChangeLogEntry entry) {
        return !entry.isReverted() && check(state, entry) == Outcome.REVERTED;
    }

    public static Outcome revert(KnowledgeState state, String entryId) {
        ChangeLogEntry entry = state.getChangeLog().stream().filter(e -> e.getId().equals(entryId)).findFirst().orElse(null);
        if (entry == null) {
            return Outcome.UNKNOWN;
        }
        if (entry.isReverted()) {
            return Outcome.ALREADY_HANDLED;
        }
        Outcome outcome = check(state, entry);
        if (outcome != Outcome.REVERTED) {
            return outcome;
        }
        KnowledgeItem item = state.findCharacter(entry.getCharacterId()).findItem(entry.getItemId());
        if (entry.getType() == ChangeLogEntry.Type.ADDED) {
            item.reject();
        } else {
            item.restore();
        }
        entry.setReverted(true);
        return Outcome.REVERTED;
    }

    /** Removes the log. */
    public static void clear(KnowledgeState state) {
        state.getChangeLog().clear();
    }

    private static Outcome check(KnowledgeState state, ChangeLogEntry entry) {
        TrackedCharacter character = state.findCharacter(entry.getCharacterId());
        KnowledgeItem item = character == null ? null : character.findItem(entry.getItemId());
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
}
