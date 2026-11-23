package com.github.enerccio.marginalia.extensions.audiobook.script;

/**
 * Which voice says a piece, as the script keeps it: a voice that comes with the speech server by its file name, or a reference clip of the
 * user by the uuid of its resource (the server name of a clip is made when a batch runs, see {@code Voices}), optionally shifted into another
 * person ({@link VoiceShift}: a speaker the cast does not name). The text form is part of the cache key of the piece, so it is stable:
 * {@code predefined:Henry.wav}, {@code clip:<uuid>}, {@code clip:<uuid>~p-2.f+1.t107.eB}.
 *
 * @param shift the change of the clip, null for the clip as it is (always null for a predefined voice: only a clip of the user can be changed here)
 */
public record VoiceRef(Kind kind, String id, VoiceShift shift) {

    /** Between the uuid of a clip and its shift in the text form (not in a uuid). */
    public static final char SHIFT_SEPARATOR = '~';

    public enum Kind {
        PREDEFINED("predefined"), CLIP("clip");

        private final String prefix;

        Kind(String prefix) {
            this.prefix = prefix;
        }
    }

    public VoiceRef {
        if (kind != Kind.CLIP || (shift != null && shift.isNone())) {
            shift = null;
        }
    }

    public VoiceRef(Kind kind, String id) {
        this(kind, id, null);
    }

    public static VoiceRef predefined(String file) {
        return new VoiceRef(Kind.PREDEFINED, file);
    }

    public static VoiceRef clip(String resourceUuid) {
        return new VoiceRef(Kind.CLIP, resourceUuid);
    }

    /** The same clip shifted (null: as it is); a predefined voice stays as it is. */
    public VoiceRef withShift(VoiceShift shift) {
        return new VoiceRef(kind, id, shift);
    }

    /** The voice without its shift: the base clip. */
    public VoiceRef base() {
        return shift == null ? this : new VoiceRef(kind, id);
    }

    /** @throws IllegalArgumentException when the text is not one made by {@link #toString()} */
    public static VoiceRef parse(String text) {
        for (Kind kind : Kind.values()) {
            if (text != null && text.startsWith(kind.prefix + ":") && text.length() > kind.prefix.length() + 1) {
                String id = text.substring(kind.prefix.length() + 1);
                int separator = kind == Kind.CLIP ? id.indexOf(SHIFT_SEPARATOR) : -1;
                if (separator < 0) {
                    return new VoiceRef(kind, id);
                }
                if (separator == 0) {
                    throw new IllegalArgumentException("Not a voice: " + text);
                }
                return new VoiceRef(kind, id.substring(0, separator), VoiceShift.parse(id.substring(separator + 1)));
            }
        }
        throw new IllegalArgumentException("Not a voice: " + text);
    }

    @Override
    public String toString() {
        return kind.prefix + ":" + id + (shift == null ? "" : SHIFT_SEPARATOR + shift.spec());
    }
}
