package com.github.enerccio.marginalia.extensions.conlangtranslator.model;

/**
 * One meaning of a {@link LexiconEntry}. A form may have several (a language without word classes uses one root as a noun and a verb, and
 * homophones are normal); only the same meaning listed twice is a fault.
 */
public class Sense {

    private String gloss = "";
    private String partOfSpeech = "";
    private String note = "";

    public Sense() {
    }

    public Sense(String gloss) {
        this.gloss = gloss;
    }

    public Sense(String gloss, String partOfSpeech) {
        this.gloss = gloss;
        this.partOfSpeech = partOfSpeech;
    }

    /** What the sense means, in the language of the story. */
    public String getGloss() {
        return gloss == null ? "" : gloss;
    }

    public void setGloss(String gloss) {
        this.gloss = gloss;
    }

    public String getPartOfSpeech() {
        return partOfSpeech == null ? "" : partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public String getNote() {
        return note == null ? "" : note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
