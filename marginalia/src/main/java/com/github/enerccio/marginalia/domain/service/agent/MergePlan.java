package com.github.enerccio.marginalia.domain.service.agent;

import java.util.List;
import java.util.Map;

/**
 * What a run changed in the book and its lorebooks, as a list of things the user can take over or leave: the three-way
 * comparison of the book as the run started (the base), the working copy of the agent and the book as it is now, item by
 * item (see {@link MergeDiff}).
 *
 * @param items       the changes, in the order they are applied: the settings of the book, lorebooks, entries, parts, annotations
 * @param names       the names of the lorebooks (by item key), for the fields that point to one
 * @param bookChanged whether the book changed since the run started (the plan then has conflicts to look at, a book that did
 *                    not change is taken over as it is)
 */
public record MergePlan(List<Item> items, Map<String, String> names, boolean bookChanged) {

    /** The item the agent changed was deleted from the book since the run started. */
    public static final String PROBLEM_GONE = "gone";
    /** The part the agent's new part follows was deleted from the book. */
    public static final String PROBLEM_NEEDS_PARENT = "needs-parent";
    /** The lorebook of the agent's new entry was deleted from the book. */
    public static final String PROBLEM_NEEDS_LOREBOOK = "needs-lorebook";
    /** The part of the agent's new annotation was deleted from the book. */
    public static final String PROBLEM_NEEDS_PART = "needs-part";

    public enum Kind {
        /** A setting of the book (its name, description, prompts, tags), or the data a plugin keeps in it. */
        BOOK,
        /** A part of the story. */
        MESSAGE,
        /** A lorebook. */
        LOREBOOK,
        /** An entry of a lorebook. */
        ENTRY,
        /** A note on a passage of a part. */
        ANNOTATION
    }

    public enum Change {
        /** The agent made it. */
        ADDED,
        /** The agent changed it. */
        CHANGED,
        /** The agent deleted it. */
        DELETED
    }

    /**
     * How the value of a field is read: a text the user may edit before it is taken over (or a value that is only compared),
     * a set of values (tags, subbooks) that is merged by what was added and removed on each side, one key of an item, JSON data
     * (what extensions keep in the attributes) that is merged place by place and which the user can decide in the dialog of the
     * JSON merge. The values of such a field are the compact JSON texts of {@code JsonText.compact}, empty for no data.
     */
    public enum Type {
        TEXT, SET, REF, JSON
    }

    /**
     * One thing of an item the agent changed.
     *
     * @param name     the name of the field, the key of its text in the localization is made of it
     * @param base     the value when the run started; null for an item the agent made
     * @param agent    the value in the copy of the agent
     * @param book     the value in the book now
     * @param conflict whether the book changed it too, to another value
     * @param editable whether the user can write the value that is taken over (a text)
     */
    public record Field(String name, Type type, String base, String agent, String book, boolean conflict, boolean editable) {
    }

    /**
     * One change.
     *
     * @param id           identifies the item in the plan: the kind and the uuid of the item in the book, or of the item in the copy
     *                     for one the agent made
     * @param sourceUuid   the item on the side that changed it (the copy for a review, the book for a sync); null for one it deleted
     * @param targetUuid   the item on the side that takes the change over (the book for a review, the copy for a sync); null for one
     *                     that is new
     * @param label        what the item is, for a list
     * @param dependsOn    the id of the item this one needs to be taken over first (the new part it follows, the new lorebook it
     *                     is in); null for none
     * @param fields       what changed
     * @param conflict     the book changed it too, or (a deleted item) changed it after the run started
     * @param problem      why it can't be taken over (what it needs is gone from the book); null when it can
     * @param sharedWith   for a lorebook or an entry the names of the other books that use the lorebook; changing it changes
     *                     them too
     */
    public record Item(String id, Kind kind, Change change, String sourceUuid, String targetUuid, String label, String dependsOn,
                       List<Field> fields, boolean conflict, String problem, List<String> sharedWith) {

        public boolean applicable() {
            return problem == null;
        }

        public Item withShared(List<String> shared) {
            return new Item(id, kind, change, sourceUuid, targetUuid, label, dependsOn, fields, conflict, problem, shared);
        }
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public long conflicts() {
        return items.stream().filter(Item::conflict).count();
    }
}
