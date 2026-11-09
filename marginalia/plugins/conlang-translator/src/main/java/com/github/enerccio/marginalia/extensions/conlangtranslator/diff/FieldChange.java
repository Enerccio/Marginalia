package com.github.enerccio.marginalia.extensions.conlangtranslator.diff;

/**
 * What one field of a {@link Change} was, becomes and is. A text field holds its text; a list field (the senses of a word, its other forms) holds its elements joined by new lines.
 * <p>
 * {@code value} is what applying writes: the other language's value when the field is not in conflict, <b>our own</b> when it is (the review
 * lets the user take the other one, or write one). {@code now} is what the field held when the change set was made; applying skips a change
 * whose field is not that any more (the language was changed after the review was opened).
 */
public final class FieldChange {

    private final String field;
    private final String before;
    private final String after;
    private final String now;
    private final boolean list;
    private final boolean conflict;
    private String value;

    FieldChange(String field, String before, String after, String now, String value, boolean list, boolean conflict) {
        this.field = field;
        this.before = before;
        this.after = after;
        this.now = now;
        this.value = value;
        this.list = list;
        this.conflict = conflict;
    }

    public String field() {
        return field;
    }

    /** The base: what the field was when both languages were one (without a base, what it is here). */
    public String before() {
        return before;
    }

    /** What the other language has. */
    public String after() {
        return after;
    }

    /** What the language has now. */
    public String now() {
        return now;
    }

    public boolean isList() {
        return list;
    }

    /** Both sides changed the field, differently. */
    public boolean isConflict() {
        return conflict;
    }

    /** What applying writes. */
    public String value() {
        return value;
    }

    /** Takes a value for the field: the other language's, or one the user wrote. Not for a list field of the set's own merge, which can only be taken or left. */
    public void setValue(String value) {
        this.value = value == null ? "" : value;
    }

    /** Whether the field takes the other language's value. */
    public boolean takesTheirs() {
        return value.equals(after);
    }

    /** Whether the field would change anything. */
    public boolean changes() {
        return !value.equals(now);
    }
}
