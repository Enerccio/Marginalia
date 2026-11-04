package com.github.enerccio.marginalia.extensions.conlangtranslator.text;

import com.github.enerccio.marginalia.domain.model.impl.AnchoredPassage;
import com.github.enerccio.marginalia.extensions.conlangtranslator.ConlangStyles;
import com.github.enerccio.marginalia.extensions.conlangtranslator.model.ReaderMode;
import com.github.enerccio.marginalia.extensions.conlangtranslator.service.PassageAccess;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.Reverse;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.Reverse.Located;
import com.github.enerccio.marginalia.utils.TextMarkup;

import java.util.ArrayList;
import java.util.List;

/**
 * The markdown of a part as the reader of a published book gets it. Nothing but the text goes to the browser: with {@link ReaderMode#CONLANG}
 * and {@link ReaderMode#CLICK} the text is the conlang (the click carries only the id of the passage, the English stays on the server), with
 * {@link ReaderMode#SOURCE} it is the English, which is the reverse the models read.
 */
public final class ReaderText {

    /** The attribute of a mark that carries the uuid of its passage. */
    public static final String MARK_ATTRIBUTE = "data-conlang-passage";

    private ReaderText() {
    }

    /**
     * @param mode     what the reader sees
     * @param stored   the stored text of the part (the conlang)
     * @param passages the translated passages of the part, in any order
     */
    public static String render(ReaderMode mode, String stored, List<AnchoredPassage> passages) {
        String text = stored == null ? "" : stored;
        if (passages == null || passages.isEmpty() || mode == ReaderMode.CONLANG) {
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
                marks.add(new TextMarkup.Mark(passage.getStart(), passage.getEnd(), ConlangStyles.PASSAGE,
                        " " + MARK_ATTRIBUTE + "=\"" + passage.getUuid() + "\""));
            }
        }
        return TextMarkup.apply(text, marks);
    }
}
