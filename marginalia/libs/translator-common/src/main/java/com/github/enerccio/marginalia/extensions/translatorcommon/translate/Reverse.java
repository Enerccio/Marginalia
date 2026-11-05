package com.github.enerccio.marginalia.extensions.translatorcommon.translate;

import com.github.enerccio.marginalia.domain.components.ModelText;
import com.github.enerccio.marginalia.domain.components.ModelText.Substitution;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.Span;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The reverse of the translation: the text of a part with its translated passages turned back into the English source, which is what a model
 * reads (see {@code PartTextRegistry}); "source only" in the exports and for the reader is the same function.
 * <p>
 * The passages are the ranges of the <b>stored</b> text that the plugin translated, each with its {@link Span}: the passage is the speech
 * itself, without the quotation marks around it, and so is the source, so the text around a speech (the marks, the markdown, the narration)
 * is never touched. A passage is left as it is when the author edited the translation since (its source is stale), when it has no source, when
 * its range is not in the text or when it overlaps an earlier one; the caller is told which ones.
 */
public final class Reverse {

    private Reverse() {
    }

    /**
     * A translated passage of the stored text.
     *
     * @param start first character of the passage in the stored text
     * @param end   end of the passage (exclusive)
     * @param span  what the plugin keeps for the passage
     */
    public record Located(int start, int end, Span span) {
    }

    /**
     * What the reverse made of a part.
     *
     * @param model   the text as the model reads it, with the map to the stored text
     * @param substitutions the ranges of the stored text that were replaced, for the registry of the core
     * @param edited  the passages the author edited since they were translated: left as they are, flagged
     * @param ignored the passages that could not be used (no source, not in the text, overlapping an earlier one)
     */
    public record Result(ModelText model, List<Substitution> substitutions, List<Located> edited, List<Located> ignored) {

        /**
         * The text as the model reads it.
         */
        public String text() {
            return model.text();
        }
    }

    /**
     * Turns the translated passages of the stored text back into their source.
     *
     * @param stored   the stored text of the part, null is read as empty
     * @param passages the passages in any order
     */
    public static Result toModel(String stored, List<Located> passages) {
        String text = stored == null ? "" : stored;
        List<Located> edited = new ArrayList<>();
        List<Located> ignored = new ArrayList<>();
        List<Substitution> substitutions = new ArrayList<>();
        int taken = 0;
        List<Located> ordered = new ArrayList<>(passages == null ? List.of() : passages);
        ordered.sort(Comparator.comparingInt(Located::start).thenComparingInt(Located::end));
        for (Located passage : ordered) {
            if (passage == null || passage.span() == null || passage.start() < 0 || passage.end() > text.length()
                    || passage.end() <= passage.start() || passage.span().getSource().isEmpty() || passage.start() < taken) {
                if (passage != null) {
                    ignored.add(passage);
                }
                continue;
            }
            if (passage.span().isEdited(text.substring(passage.start(), passage.end()))) {
                edited.add(passage);
                // the edited passage still holds its place: what overlaps it is not read either
                taken = passage.end();
                continue;
            }
            substitutions.add(new Substitution(passage.start(), passage.end(), passage.span().getSource()));
            taken = passage.end();
        }
        return new Result(ModelText.of(text, substitutions), List.copyOf(substitutions), edited, ignored);
    }

    /**
     * The source text only, for the places that need no map.
     */
    public static String sourceText(String stored, List<Located> passages) {
        return toModel(stored, passages).text();
    }
}
