package com.github.enerccio.marginalia.extensions.conlangtranslator.diff;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * One operation of a {@link ChangeSet}: add, change, delete or move one thing of a language, with its fields before and after.
 */
public final class Change {

    /** What the change does. */
    public enum Kind {
        ADD, CHANGE, DELETE, MOVE
    }

    private final Target target;
    private final Kind kind;
    private final String id;
    private final String label;
    private final List<FieldChange> fields = new ArrayList<>();
    private final Set<String> dependsOn = new LinkedHashSet<>();
    private Conflict conflict;
    private boolean picked = true;
    /** The thing as the other language has it (an add, or a change that brings back a deleted one); a copy. */
    private Object payload;
    /** The id of the thing before it in the other language (null: first); where an add or a move puts it. */
    private String anchor;

    Change(Target target, Kind kind, String id, String label) {
        this.target = target;
        this.kind = kind;
        this.id = id;
        this.label = label;
    }

    public Target target() {
        return target;
    }

    public Kind kind() {
        return kind;
    }

    public String id() {
        return id;
    }

    /** What the user knows the thing by (the word and its meanings, the title of a section). */
    public String label() {
        return label;
    }

    /** The key of the change inside a set: the target and the id. */
    public String key() {
        return target + ":" + id + ":" + (kind == Kind.MOVE ? "move" : "");
    }

    /** The key of the thing itself, which a change of another thing may depend on. */
    public String thing() {
        return target + ":" + id;
    }

    public List<FieldChange> fields() {
        return fields;
    }

    /** The things that must be taken together with this change (the new category of a new word). */
    public Set<String> dependsOn() {
        return dependsOn;
    }

    public Conflict conflict() {
        return conflict;
    }

    void setConflict(Conflict conflict) {
        this.conflict = conflict;
        if (conflict != null) {
            picked = false;
        }
    }

    /** Whether the change, or one of its fields, is in conflict. */
    public boolean hasConflict() {
        return conflict != null || fields.stream().anyMatch(FieldChange::isConflict);
    }

    public boolean isPicked() {
        return picked;
    }

    void setPicked(boolean picked) {
        this.picked = picked;
    }

    Object payload() {
        return payload;
    }

    void setPayload(Object payload) {
        this.payload = payload;
    }

    public String anchor() {
        return anchor;
    }

    void setAnchor(String anchor) {
        this.anchor = anchor;
    }
}
