package com.github.enerccio.marginalia.extensions.audiobook.label;

import java.util.List;

/** The instruction to the model and the pieces it labels. Changing the text changes what the model answers, so {@link #VERSION} goes into the cache key of the labels. */
public final class LabelPrompt {

    /** Bump it when the instruction changes: labels made by the old one are not reused. */
    public static final String VERSION = "2";

    /** At most one sound tag per this many words of the part (the model uses them freely otherwise). */
    private static final int WORDS_PER_TAG = 80;

    private LabelPrompt() {
    }

    public static String system(List<String> characters) {
        String known = characters.isEmpty() ? "They are not known: name a speaker as the text names them."
                : "The characters known so far are: " + String.join(", ", characters) + ". Others may speak; name them as the text does.";
        return """
                You label the pieces of a story for an audiobook with several voices. The pieces are numbered; N is narration, D is direct speech. %s
                For every piece answer with:
                - "id": the number of the piece;
                - "speaker": for N always "Narrator"; for D the character who says it, resolved from the context (a name in the narration around it, a vocative, who answers whom);
                - "delivery": how it should be read (%s) and "intensity" from 0.0 to 1.0;
                - "tagged_text": for N always an empty string. For D the piece word for word, unchanged, except that you may insert a sound tag where the context clearly calls for that sound from the speaking character, and rarely (at most one tag per %d words of the whole text). Allowed tags: %s, written exactly like that. Do not change, add or remove any other word. For every piece that gets no tag use an empty string: never copy the text of a piece that has no tag.
                Pieces marked "context" were labelled already: read them to understand who speaks, do not label them.
                Answer with the JSON only, one entry per piece, in order."""
                .formatted(known, deliveries(), WORDS_PER_TAG, tags());
    }

    private static String deliveries() {
        StringBuilder all = new StringBuilder();
        for (Delivery delivery : Delivery.values()) {
            all.append(all.isEmpty() ? "" : ", ").append(delivery.name());
        }
        return all.toString();
    }

    private static String tags() {
        StringBuilder all = new StringBuilder();
        for (String tag : Tags.ALLOWED) {
            all.append(all.isEmpty() ? "" : " ").append('[').append(tag).append(']');
        }
        return all.toString();
    }

    /** The pieces of one request: some already labelled ones for the context, then those to label. */
    public static String user(List<Piece> context, List<Piece> pieces) {
        StringBuilder text = new StringBuilder();
        for (Piece piece : context) {
            text.append("(context) ").append(line(piece)).append('\n');
        }
        for (Piece piece : pieces) {
            text.append(line(piece)).append('\n');
        }
        return text.toString().strip();
    }

    private static String line(Piece piece) {
        return "[" + piece.id() + "] " + (piece.kind() == Piece.Kind.NARRATION ? "N" : "D") + ": " + piece.text();
    }
}
