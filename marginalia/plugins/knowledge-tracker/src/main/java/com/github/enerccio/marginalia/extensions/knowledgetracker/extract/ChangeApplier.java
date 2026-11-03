package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

import com.github.enerccio.marginalia.extensions.knowledgetracker.model.ChangeLogEntry;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeItem;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.KnowledgeState;
import com.github.enerccio.marginalia.extensions.knowledgetracker.model.TrackedCharacter;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies the reviewed changes to the state. A change the user unchecked is not applied: a rejected addition is kept as old
 * knowledge (with its evidence and message, so the idea is not lost), a rejected retraction leaves the item as it is.
 * What is applied carries its source message, so a reprocess from that message takes it back.
 */
public final class ChangeApplier {

    private ChangeApplier() {
    }

    /**
     * @param retracted items that became old
     * @param added     items that were added (active)
     * @param rejected  additions kept as old knowledge
     */
    public record Result(int retracted, int added, int rejected) {
    }

    public static Result apply(KnowledgeState state, ChangeSet changes) {
        return apply(state, changes, false, 0);
    }

    /**
     * @param logged whether what is applied goes into the change log of the state (an automatic run, nobody reviewed it)
     * @param now    the time of the log entries
     */
    public static Result apply(KnowledgeState state, ChangeSet changes, boolean logged, long now) {
        List<ChangeLogEntry> entries = new ArrayList<>();
        int retracted = 0;
        int added = 0;
        int rejected = 0;
        for (Change change : changes.changes()) {
            TrackedCharacter character = state.findCharacter(change.getCharacterId());
            if (character == null) {
                continue;
            }
            if (change.getType() == Change.Type.RETRACT) {
                KnowledgeItem item = change.isApply() ? character.findItem(change.getItemId()) : null;
                // the tidy pass never touches an item that was locked, edited or written by hand since it asked
                if (item != null && item.isActive() && (!change.isTidy() || item.isTidyCandidate())) {
                    if (change.isTidy()) {
                        item.tidy();
                    } else {
                        item.retract(change.getMessageUuid());
                    }
                    retracted++;
                    entries.add(new ChangeLogEntry(now, ChangeLogEntry.Type.RETRACTED, character.getId(), character.getName(),
                            item.getId(), item.getText(), item.getKind(), change.isTidy() ? null : change.getMessageUuid(), change.getEvidence()));
                }
                continue;
            }
            if (change.isTidy() && !change.isApply()) {
                // a merge that was unchecked: nothing is kept, the items it would have replaced stay (or are decided one by one)
                continue;
            }
            String text = change.getText() == null ? "" : change.getText().strip();
            if (text.isEmpty()) {
                continue;
            }
            KnowledgeItem item = KnowledgeItem.learned(text, change.getKind(), change.getMessageUuid(), change.getEvidence());
            if (change.isApply()) {
                added++;
                entries.add(new ChangeLogEntry(now, ChangeLogEntry.Type.ADDED, character.getId(), character.getName(),
                        item.getId(), item.getText(), item.getKind(), change.getMessageUuid(), change.getEvidence()));
            } else {
                item.reject();
                rejected++;
            }
            character.getItems().add(item);
        }
        if (logged && !entries.isEmpty()) {
            ChangeLog.add(state, entries);
        }
        return new Result(retracted, added, rejected);
    }
}
