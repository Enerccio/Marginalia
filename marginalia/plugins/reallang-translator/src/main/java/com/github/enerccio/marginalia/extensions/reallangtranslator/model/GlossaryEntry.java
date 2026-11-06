package com.github.enerccio.marginalia.extensions.reallangtranslator.model;

import java.util.UUID;

/**
 * One line of a glossary: how a name or a special word of the story is written in the language. The glossary is kept at three levels (the
 * language, a folder, the book) and merged by {@link Glossary}; an entry is the same thing at every level.
 */
public class GlossaryEntry {

    private String id = UUID.randomUUID().toString();
    /** The word as the English story writes it ("Saellen", "witch"); the key of the entry, the case does not matter. */
    private String source = "";
    private GlossaryKind kind = GlossaryKind.WRITTEN;
    /** How it is written in the language ("サエレン", "魔女"); not used for {@link GlossaryKind#KEEP}. */
    private String written = "";
    /** How it is read, for a language that has a reading (a gikun / ateji: 魔女 read ウィッチ); may be empty. */
    private String reading = "";
    /** Why, or what to watch for (the honorific the character gets, the family name order); goes to the model. */
    private String note = "";
    /**
     * Whether the entry applies. At a nearer level an entry that is off switches the same source off for that level and below: a book can do without what
     * its folder or its language says.
     */
    private boolean enabled = true;
    /**
     * Whether the author has not looked at the entry yet: a name the model had to choose is proposed as a new entry; the model uses it from then on, the
     * author accepts or changes it.
     */
    private boolean proposed;

    public GlossaryEntry() {
    }

    public GlossaryEntry(String source, GlossaryKind kind, String written, String reading, String note) {
        this.source = source;
        this.kind = kind;
        this.written = written;
        this.reading = reading;
        this.note = note;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSource() {
        return source == null ? "" : source.strip();
    }

    public void setSource(String source) {
        this.source = source;
    }

    public GlossaryKind getKind() {
        return kind == null ? GlossaryKind.WRITTEN : kind;
    }

    public void setKind(GlossaryKind kind) {
        this.kind = kind;
    }

    public String getWritten() {
        return written == null ? "" : written.strip();
    }

    public void setWritten(String written) {
        this.written = written;
    }

    public String getReading() {
        return reading == null ? "" : reading.strip();
    }

    public void setReading(String reading) {
        this.reading = reading;
    }

    public String getNote() {
        return note == null ? "" : note.strip();
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isProposed() {
        return proposed;
    }

    public void setProposed(boolean proposed) {
        this.proposed = proposed;
    }

    /** The key the levels are merged by. */
    public String key() {
        return Glossary.key(getSource());
    }

    /** What the translation should contain for the source: the written form, or the source itself for {@link GlossaryKind#KEEP}. */
    public String expected() {
        return getKind() == GlossaryKind.KEEP ? getSource() : getWritten();
    }

    /** Whether the entry can be used: it has a source and, unless it is kept, a written form. */
    public boolean isUsable() {
        return !getSource().isEmpty() && (getKind() == GlossaryKind.KEEP || !getWritten().isEmpty());
    }

    public GlossaryEntry copy() {
        GlossaryEntry copy = new GlossaryEntry(getSource(), getKind(), getWritten(), getReading(), getNote());
        copy.id = id;
        copy.enabled = enabled;
        copy.proposed = proposed;
        return copy;
    }
}
