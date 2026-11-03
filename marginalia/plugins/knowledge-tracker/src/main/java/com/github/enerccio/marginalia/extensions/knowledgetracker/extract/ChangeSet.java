package com.github.enerccio.marginalia.extensions.knowledgetracker.extract;

import java.util.ArrayList;
import java.util.List;

/**
 * The changes the model proposed for a run, checked against the state and the messages, and what was left out and why.
 *
 * @param changes the proposed changes, in the order of the answer
 * @param dropped what the model said that was not usable (an unknown character, an item that is not there, a change
 *                without evidence, a duplicate), one line each, for the log
 */
public record ChangeSet(List<Change> changes, List<String> dropped) {

    public ChangeSet {
        changes = new ArrayList<>(changes);
        dropped = List.copyOf(dropped);
    }

    public boolean isEmpty() {
        return changes.isEmpty();
    }

    /** The changes of one tracked character, in order. */
    public List<Change> of(String characterId) {
        return changes.stream().filter(c -> c.getCharacterId().equals(characterId)).toList();
    }
}
