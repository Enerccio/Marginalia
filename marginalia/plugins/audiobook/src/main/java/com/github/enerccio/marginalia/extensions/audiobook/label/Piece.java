package com.github.enerccio.marginalia.extensions.audiobook.label;

/**
 * A piece of the text of a part: narration or direct speech. The text is split into pieces by the code, never by the model, so what is read can't
 * be changed by it; the model only labels the pieces.
 *
 * @param id        the number of the piece in the part, from 0
 * @param text      the words of the piece: for speech what is between the quotation marks (without them), for a heading without the {@code #}
 * @param paragraph the number of the paragraph the piece is in
 * @param heading   the piece is a markdown heading
 */
public record Piece(int id, Kind kind, String text, int paragraph, boolean heading) {

    public enum Kind {
        NARRATION, SPEECH
    }
}
