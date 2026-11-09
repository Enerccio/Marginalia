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

    /** What the labels were made from: the words of the part and the instruction (which has the characters the model was told); a changed template asks again. */
    public static String cacheKey(String text, String system) {
        return Hashes.sha256(String.join("\u0001", LabelPrompt.VERSION, text == null ? "" : text, system));
    }

    /**
     * @param fromStore the labels were not asked of the model
     * @param missing   pieces without a label (they will be read by the narrator, neutrally)
     */
    public record Result(Map<Integer, PieceLabel> labels, boolean fromStore, int missing) {
    }

    /** @param system the instruction (see {@link LabelPrompt#system}) */
    public static Result labels(String partId, String text, List<Piece> pieces, String system, LabelModel model, LabelStore store) throws Exception {
        String key = cacheKey(text, system);
        List<PieceLabel> stored = store.find(partId, key);
        if (stored != null) {
            Map<Integer, PieceLabel> labels = new LinkedHashMap<>();
            stored.forEach(l -> labels.putIfAbsent(l.id(), l));
            return new Result(labels, true, pieces.size() - labels.size());
        }
        PieceLabeler.Labelled made = PieceLabeler.label(pieces, system, model);
        if (made.missing() == 0) {
            store.put(partId, key, List.copyOf(made.labels().values()));
        }
        return new Result(made.labels(), false, made.missing());
    }
}
