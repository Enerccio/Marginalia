package com.github.enerccio.marginalia.extensions.audiobook.label;

import com.github.enerccio.marginalia.extensions.audiobook.script.Hashes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The labels of a part: from the store when the text, the characters and the instruction are the same as when they were made, else from the model
 * (which is the slow part: a minute or more, and the language model has to be loaded), and then stored. Labels that are incomplete (the model left
 * pieces out) are used but not stored, so the next run asks again.
 */
public final class LabelService {

    private LabelService() {
    }

    /** What the labels were made from: the words of the part, the characters the model was told and the instruction. */
    public static String cacheKey(String text, List<String> characters) {
        return Hashes.sha256(String.join("\u0001", LabelPrompt.VERSION, text == null ? "" : text, String.join("\u0002", characters)));
    }

    /**
     * @param fromStore the labels were not asked of the model
     * @param missing   pieces without a label (they will be read by the narrator, neutrally)
     */
    public record Result(Map<Integer, PieceLabel> labels, boolean fromStore, int missing) {
    }

    public static Result labels(String partId, String text, List<Piece> pieces, List<String> characters, LabelModel model, LabelStore store) throws Exception {
        String key = cacheKey(text, characters);
        List<PieceLabel> stored = store.find(partId, key);
        if (stored != null) {
            Map<Integer, PieceLabel> labels = new LinkedHashMap<>();
            stored.forEach(l -> labels.putIfAbsent(l.id(), l));
            return new Result(labels, true, pieces.size() - labels.size());
        }
        PieceLabeler.Labelled made = PieceLabeler.label(pieces, characters, model);
        if (made.missing() == 0) {
            store.put(partId, key, List.copyOf(made.labels().values()));
        }
        return new Result(made.labels(), false, made.missing());
    }
}
