package com.github.enerccio.marginalia.extensions.conlangtranslator.model;

/**
 * Who made a word and when. A word of the author is never changed by the model; an edit of a word of the model makes it the author's.
 */
public class Origin {

    private OriginKind kind = OriginKind.AUTHOR;
    /** The part (uuid) the model invented the word in; null for the author's words. */
    private String part;
    /** The model that invented it, as the user names it. */
    private String model;
    /** When, epoch milliseconds; 0 when unknown. */
    private long time;

    public Origin() {
    }

    public static Origin author() {
        return new Origin();
    }

    public static Origin model(String part, String model, long time) {
        Origin origin = new Origin();
        origin.kind = OriginKind.MODEL;
        origin.part = part;
        origin.model = model;
        origin.time = time;
        return origin;
    }

    public OriginKind getKind() {
        return kind == null ? OriginKind.AUTHOR : kind;
    }

    public void setKind(OriginKind kind) {
        this.kind = kind;
    }

    public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
