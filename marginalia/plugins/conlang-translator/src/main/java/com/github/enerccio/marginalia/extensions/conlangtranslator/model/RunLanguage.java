package com.github.enerccio.marginalia.extensions.conlangtranslator.model;

/**
 * A language as an agent run changed it: the run works on a copy of the book, and the library of languages is not part of the copy, so the first
 * change of a language in a run puts the language into the attributes of the copy ({@link #KEY}, an object keyed by the language's id). The copy
 * carries it everywhere the book's own data goes: the checkpoints, the restore of a checkpoint, and the review of the run, where it is a JSON field
 * the core merges three ways. When the run is applied the entry lands in the attributes of the book, and the user takes it over into the library
 * from there (a three-way comparison of {@link #getBase()}, {@link #getOurs()} and the library as it is then).
 */
public class RunLanguage {

    /** The attribute of the book (the copy of a run, then the book after the run was applied). */
    public static final String KEY = "com.github.enerccio.marginalia.extensions.conlangtranslator.runLanguages";

    /** The language as it was when the run first changed it; null for a language the run made. */
    private Language base;
    /** The language as the run has it now. */
    private Language ours;
    /** When the run first changed it. */
    private long since;

    public RunLanguage() {
    }

    public RunLanguage(Language base, Language ours, long since) {
        this.base = base;
        this.ours = ours;
        this.since = since;
    }

    public Language getBase() {
        return base;
    }

    public void setBase(Language base) {
        this.base = base;
    }

    public Language getOurs() {
        return ours;
    }

    public void setOurs(Language ours) {
        this.ours = ours;
    }

    public long getSince() {
        return since;
    }

    public void setSince(long since) {
        this.since = since;
    }
}
