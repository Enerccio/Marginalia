package com.github.enerccio.marginalia.extensions.storytracker.llm;

/**
 * What arrives from the model while it writes, to be shown at once.
 */
public interface Live {

    /**
     * @param reasoning whether the piece is reasoning (else the answer)
     * @param piece     the text, as it arrives
     */
    void chunk(boolean reasoning, String piece);

    /** A new request starts: what was shown belongs to the one before. */
    default void started() {
    }
}
