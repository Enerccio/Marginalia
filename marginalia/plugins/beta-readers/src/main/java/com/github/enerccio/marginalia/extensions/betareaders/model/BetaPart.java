package com.github.enerccio.marginalia.extensions.betareaders.model;

/**
 * A piece of a message of a persona or of the narrator; the parts of a message are interleaved (an action, then speech...).
 */
public class BetaPart {

    private PartType type = PartType.SPEECH;
    private String text = "";

    public BetaPart() {
    }

    public BetaPart(PartType type, String text) {
        this.type = type;
        this.text = text;
    }

    public PartType getType() {
        return type;
    }

    public void setType(PartType type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
