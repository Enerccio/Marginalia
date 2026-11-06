package com.github.enerccio.marginalia.extensions.translatorcommon.model;

/**
 * How the plugin puts the language understanding of the characters into the prompt of the story: inserted into the payload by itself (the default), or only where
 * the author's template has the macro. Kept in the settings of the book of each plugin.
 */
public class UnderstandingSettings {

    private boolean inPrompt = true;
    /** The messages from the end of the payload at which the block is inserted: 0 = at the very end, 1 = right before the instructions of the turn. */
    private int depth = 1;

    public boolean isInPrompt() {
        return inPrompt;
    }

    public void setInPrompt(boolean inPrompt) {
        this.inPrompt = inPrompt;
    }

    public int getDepth() {
        return Math.max(0, depth);
    }

    public void setDepth(int depth) {
        this.depth = Math.max(0, depth);
    }
}
