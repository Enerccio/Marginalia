package com.github.enerccio.marginalia.extensions.betareaders.model;

import java.util.ArrayList;
import java.util.List;

/**
 * One message of a reply, as the user sees it: who says it ({@link #NARRATOR} or the name of a persona) and what.
 */
public class BetaSpoken {

    /** The speaker of the lines that describe the scene; it is no persona and has no icon. */
    public static final String NARRATOR = "Narrator";

    private String speaker = NARRATOR;
    private List<BetaPart> parts = new ArrayList<>();

    public BetaSpoken() {
    }

    public BetaSpoken(String speaker, List<BetaPart> parts) {
        this.speaker = speaker;
        this.parts = new ArrayList<>(parts);
    }

    public String getSpeaker() {
        return speaker;
    }

    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }

    public List<BetaPart> getParts() {
        return parts;
    }

    public void setParts(List<BetaPart> parts) {
        this.parts = parts;
    }

    public boolean isNarrator() {
        return NARRATOR.equalsIgnoreCase(speaker);
    }
}
