package com.github.enerccio.marginalia.extensions.audiobook.label;

import com.github.enerccio.marginalia.domain.service.structured.StructuredOutputException;

import java.util.*;

/**
 * Has the model label the pieces of a part. A long part goes in windows of {@link #WINDOW} pieces (the answer is as long as the part, it must
 * fit the answer limit, and a model that is asked for a hundred entries loses its place), each with the last pieces before it as read-only context
 * so that a speaker is still known when a window starts in the middle of a conversation.
 * <p>
 * A window whose answer does not match the schema (the usual cause is a cut-off JSON: the thinking of the model counts against the same limit and
 * 17 pieces took 1957 of 2048 tokens) is asked again in two halves, down to {@link #MIN_WINDOW} pieces, instead of failing the part.
 */
public final class PieceLabeler {

    /** A label is about 55 tokens of JSON when the model leaves {@code tagged_text} empty, twice that when it copies the text. */
    public static final int WINDOW = 16;
    public static final int MIN_WINDOW = 4;
    public static final int CONTEXT = 4;

    private PieceLabeler() {
    }

    /**
     * @param labels  the label of each piece the model labelled, by its id
     * @param missing how many pieces got none (the model left them out, or gave an id that is not one of the window)
     */
    public record Labelled(Map<Integer, PieceLabel> labels, int missing) {
    }

    /** @param system the instruction (see {@link LabelPrompt#system}) */
    public static Labelled label(List<Piece> pieces, String system, LabelModel model) throws Exception {
        Map<Integer, PieceLabel> labels = new LinkedHashMap<>();
        for (int from = 0; from < pieces.size(); from += WINDOW) {
            window(pieces, from, Math.min(pieces.size(), from + WINDOW), system, model, labels);
        }
        return new Labelled(labels, pieces.size() - labels.size());
    }

    private static void window(List<Piece> pieces, int from, int to, String system, LabelModel model, Map<Integer, PieceLabel> labels) throws Exception {
        List<Piece> window = pieces.subList(from, to);
        List<Piece> context = pieces.subList(Math.max(0, from - CONTEXT), from);
        LabelAnswer answer;
        LabelWatch.window(from, to, pieces.size());
        try {
            answer = model.ask(system, LabelPrompt.user(context, window));
        } catch (StructuredOutputException e) {
            if (window.size() <= MIN_WINDOW) {
                throw e;
            }
            int middle = from + window.size() / 2;
            window(pieces, from, middle, system, model, labels);
            window(pieces, middle, to, system, model, labels);
            return;
        }
        Set<Integer> ids = new HashSet<>();
        window.forEach(p -> ids.add(p.id()));
        if (answer != null && answer.labels() != null) {
            for (PieceLabel label : answer.labels()) {
                // only the pieces of this window, the first label of a piece wins
                if (ids.contains(label.id())) {
                    labels.putIfAbsent(label.id(), label);
                }
            }
        }
    }
}
