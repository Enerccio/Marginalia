package com.github.enerccio.marginalia.extensions.audiobook.model;

/**
 * What the plugin keeps of a reference clip besides its audio (a resource of the user, named by its uuid): the name the user gave it, the folder it is
 * in, the clip it was made from (a cleaned version) and how, the words spoken in it, and the shifted copies made of it for unnamed speakers.
 */
public class ClipInfo {

    /** The name shown; null: the file name of the resource. */
    private String name;
    /** The folder; null: none. A version is shown under its parent, its own folder follows the parent's. */
    private String folder;
    /** The uuid of the clip this one was made from; null: an original. */
    private String parent;
    /** How it was made from the parent ("ffmpeg", the steps of a cleanup); null: uploaded. */
    private String madeWith;
    /** The words spoken in the clip (typed or recognised); null: not known. */
    private String text;
    /** For a shifted copy made for an unnamed speaker: the shift ({@code VoiceShift.spec()}); such a clip is hidden from the library and the pickers. */
    private String shift;
    /** The shifted copies made of this clip: shift → uuid of the resource; null: none. */
    private java.util.Map<String, String> shifted;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = blankToNull(name);
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = blankToNull(folder);
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = blankToNull(parent);
    }

    public String getMadeWith() {
        return madeWith;
    }

    public void setMadeWith(String madeWith) {
        this.madeWith = blankToNull(madeWith);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = blankToNull(text);
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = blankToNull(shift);
    }

    /** The shifted copies of this clip (shift → uuid), never null. */
    public java.util.Map<String, String> getShifted() {
        if (shifted == null) {
            shifted = new java.util.LinkedHashMap<>();
        }
        return shifted;
    }

    public void setShifted(java.util.Map<String, String> shifted) {
        this.shifted = shifted == null || shifted.isEmpty() ? null : new java.util.LinkedHashMap<>(shifted);
    }

    /** Whether nothing is kept (the entry can go). */
    public boolean isEmpty() {
        return name == null && folder == null && parent == null && madeWith == null && text == null && shift == null && (shifted == null || shifted.isEmpty());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
