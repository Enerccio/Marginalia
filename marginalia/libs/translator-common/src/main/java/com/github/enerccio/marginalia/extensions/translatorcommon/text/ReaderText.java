package com.github.enerccio.marginalia.extensions.translatorcommon.text;

import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.ReaderMode;
import com.github.enerccio.marginalia.extensions.translatorcommon.service.PassageAccess;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.Reverse;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.Reverse.Located;
import com.github.enerccio.marginalia.utils.TextMarkup;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The markdown of a part as the reader of a published book gets it. Nothing but the text goes to the browser: with {@link ReaderMode#TRANSLATED}
 * and {@link ReaderMode#CLICK} the text is the translation (the click carries only the id of the passage, the English stays on the server), with
 * {@link ReaderMode#SOURCE} it is the English, which is the reverse the models read.
 */
public final class ReaderText {

    /** The attribute of a mark that carries the uuid of its passage. */
    public static final String MARK_ATTRIBUTE = "data-translated-passage";

    private ReaderText() {
    }

    /**
     * @param mode     what the reader sees
     * @param stored   the stored text of the part (the translation)
     * @param passages the translated passages of the part, in any order
     * @param markClass the CSS class of the mark over a translated speech (the one the plugin gave its passage kind)
     */
    public static String render(ReaderMode mode, String stored, List<AnchoredPassage> passages, String markClass) {
        return render(mode, stored, passages, passage -> markClass);
    }

    /**
     * Like {@link #render(ReaderMode, String, List, String)} for passages of several kinds (two plugins translated the part): each passage gets the
     * mark class of its own kind.
     *
     * @param markClassOf the CSS class of the mark over a passage
     */
    public static String render(ReaderMode mode, String stored, List<AnchoredPassage> passages, Function<AnchoredPassage, String> markClassOf) {
        String text = stored == null ? "" : stored;
        if (passages == null || passages.isEmpty() || mode == ReaderMode.TRANSLATED) {
            return text;
        }
        if (mode == ReaderMode.SOURCE) {
            List<Located> located = new ArrayList<>();
            for (AnchoredPassage passage : passages) {
                Located one = PassageAccess.locate(passage);
                if (one != null) {
                    located.add(one);
                }
            }
            return Reverse.toModel(text, located).text();
        }
        List<TextMarkup.Mark> marks = new ArrayList<>();
        for (AnchoredPassage passage : passages) {
            if (PassageAccess.locate(passage) != null && passage.getEnd() <= text.length() && passage.getEnd() > passage.getStart()) {
                marks.add(new TextMarkup.Mark(passage.getStart(), passage.getEnd(), markClassOf.apply(passage),
                        " " + MARK_ATTRIBUTE + "=\"" + passage.getUuid() + "\""));
            }
        }
        return TextMarkup.apply(text, marks);
    }
}
