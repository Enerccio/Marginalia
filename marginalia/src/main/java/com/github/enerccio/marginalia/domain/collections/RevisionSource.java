package com.github.enerccio.marginalia.domain.collections;

/**
 * Who wrote a version of a part, see {@link com.github.enerccio.marginalia.domain.model.impl.MessageRevision}.
 */
public enum RevisionSource {

    /**
     * The first generation of the part.
     */
    GENERATE(true),
    /**
     * The part generated again, in place.
     */
    REGENERATE(true),
    /**
     * A model rewrote (a passage of) the text.
     */
    REDRAFT(true),
    /**
     * The author typed or edited it.
     */
    HUMAN(false),
    /**
     * An extension wrote it, named in the detail.
     */
    PLUGIN(false),
    /**
     * A mass action: Replace, the manuscript commands.
     */
    AUTOMATIC(false),
    /**
     * The next part was merged into the part.
     */
    MERGE(false),
    /**
     * The part was cut in two.
     */
    SPLIT(false),
    /**
     * The part was made by duplicating a book.
     */
    DUPLICATE(false),
    /**
     * An earlier version put back on top of the history, the detail says which.
     */
    RESTORE(false),
    /**
     * A version saved by hand, with a label.
     */
    SNAPSHOT(false);

    private final boolean model;

    RevisionSource(boolean model) {
        this.model = model;
    }

    /**
     * Whether a model wrote the text: only such a version keeps the reasoning that came with it.
     */
    public boolean isModel() {
        return model;
    }
}
