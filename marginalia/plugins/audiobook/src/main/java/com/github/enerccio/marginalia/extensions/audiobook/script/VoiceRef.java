package com.github.enerccio.marginalia.extensions.audiobook.script;

/**
 * Which voice says a piece, as the script keeps it: a voice that comes with the speech server by its file name, or a reference clip of the
 * user by the uuid of its resource (the server name of a clip is made when a batch runs, see {@code Voices}). The text form is part of the
 * cache key of the piece, so it is stable: {@code predefined:Henry.wav}, {@code clip:<uuid>}.
 */
public record VoiceRef(Kind kind, String id) {

    public enum Kind {
        PREDEFINED("predefined"), CLIP("clip");

        private final String prefix;

        Kind(String prefix) {
            this.prefix = prefix;
        }
    }

    public static VoiceRef predefined(String file) {
        return new VoiceRef(Kind.PREDEFINED, file);
    }

    public static VoiceRef clip(String resourceUuid) {
        return new VoiceRef(Kind.CLIP, resourceUuid);
    }

    /** @throws IllegalArgumentException when the text is not one made by {@link #toString()} */
    public static VoiceRef parse(String text) {
        for (Kind kind : Kind.values()) {
            if (text != null && text.startsWith(kind.prefix + ":") && text.length() > kind.prefix.length() + 1) {
                return new VoiceRef(kind, text.substring(kind.prefix.length() + 1));
            }
        }
        throw new IllegalArgumentException("Not a voice: " + text);
    }

    @Override
    public String toString() {
        return kind.prefix + ":" + id;
    }
}
