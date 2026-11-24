package com.github.enerccio.marginalia.extensions.statetracker.extract;

import com.github.enerccio.marginalia.extensions.statetracker.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Applies the checked changes the user kept to the state. Every value it sets or ends remembers the part it was read in (for the
 * tidy: the last processed part), so that a reprocess takes it back. A change whose character, category or value is not there any more (the user changed the state while the
 * review waited) is skipped.
 */
public final class StateApplier {

    /**
     * @param applied the changes that were applied
     * @param skipped the changes that could not be applied any more
     * @param rejected the changes the user unchecked
     */
    public record Result(int applied, int skipped, int rejected) {
    }

    private StateApplier() {
    }

    public static Result apply(StateBook state, List<StateChange> changes, long now) {
        return apply(state, changes, now, false);
    }

    /**
     * @param logged whether what is applied goes into the change log of the book (nobody reviewed it), see {@link StateLog}
     */
    public static Result apply(StateBook state, List<StateChange> changes, long now, boolean logged) {
        List<StateLogEntry> entries = new ArrayList<>();
        // the story day of each processed part, for the values the changes make (the clock is folded once)
        Map<String, StoryTime> times = state.getClock().isEnabled() ? state.getClock().timeline(state) : Map.of();
        Integer today = state.today();
        int applied = 0;
        int skipped = 0;
        int rejected = 0;
        for (StateChange change : changes) {
            if (!change.isApply()) {
                rejected++;
                continue;
            }
            TrackedCharacter character = state.findCharacter(change.getCharacterId());
            Category category = state.findCategory(change.getCategoryId());
            if (character == null || category == null) {
                skipped++;
                continue;
            }
            StateItem.EndReason removed = change.isTidy() ? StateItem.EndReason.TIDIED : StateItem.EndReason.REMOVED;
            StateItem.EndReason replaced = change.isTidy() ? StateItem.EndReason.TIDIED : StateItem.EndReason.REPLACED;
            String value = change.getValue() == null ? "" : change.getValue().strip();
            StateItem item = change.getItemId() == null ? null : character.findItem(change.getItemId());
            if (change.isTidy() && (item == null || !item.isActive() || item.isLocked())) {
                // every change of the tidy is about one value: gone, ended or locked by the user meanwhile, it is left as it is
                skipped++;
                continue;
            }
            StateItem created = null;
            StateItem ended = null;
            boolean done = switch (change.getAction()) {
                case SET, ADD -> {
                    if (value.isEmpty()) {
                        yield false;
                    }
                    if (category.getKind().isSingleValued()) {
                        ended = character.active(category.getId()).stream().findFirst().orElse(null);
                    }
                    created = character.set(category, value, now, change.getPart(), replaced);
                    yield true;
                }
                case CLEAR, REMOVE -> {
                    if (item == null || !item.isActive()) {
                        yield false;
                    }
                    character.end(item, removed, now, change.getPart());
                    ended = item;
                    yield true;
                }
                case REPLACE -> {
                    if (value.isEmpty() || item == null || !item.isActive()) {
                        yield false;
                    }
                    character.end(item, replaced, now, change.getPart());
                    ended = item;
                    created = character.set(category, value, now, change.getPart(), replaced);
                    yield true;
                }
            };
            if (done && created != null && change.isTidy()) {
                created.setFromTidy(true);
            }
            if (done && created != null && today != null) {
                StoryTime at = change.getPart() == null ? null : times.get(change.getPart());
                created.setStoryDay(at == null ? today : at.day());
            }
            if (done) {
                entries.add(new StateLogEntry(now, StateLogEntry.Action.valueOf(change.getAction().name()), character, category, created,
                        ended, change.getPart(), change.getEvidence(), change.isTidy()));
                applied++;
            } else {
                skipped++;
            }
        }
        if (logged && !entries.isEmpty()) {
            StateLog.add(state, entries);
        }
        return new Result(applied, skipped, rejected);
    }
}
