package com.github.enerccio.marginalia.extensions.translatorcommon.text;

import com.github.enerccio.marginalia.extensions.ExportTextRegistry.Edit;
import com.github.enerccio.marginalia.extensions.translatorcommon.model.ExportMode;
import com.github.enerccio.marginalia.extensions.translatorcommon.translate.Reverse.Located;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * What an export of the book changes in the text of a part for each {@link ExportMode}: the translated speech replaced by its source, and/or a
 * footnote on each speech with the other text. Pure: the answer is a list of {@link Edit}s, ranges of the <b>stored</b> text, which the core's
 * {@code ExportTextRegistry} puts into the text (it drops overlaps and numbers the footnotes over the whole export, so that the labels are unique in
 * the one document of the formats that join the parts, and so that two plugins can work on the same book).
 * <p>
 * A speech whose text was edited since it was translated has no source to be trusted: it stays as it is and has no footnote
 * ({@link com.github.enerccio.marginalia.extensions.translatorcommon.model.Span#isEdited}). The same check is what makes a passage of another plugin,
 * whose range points at text this plugin did not write, drop out quietly.
 */
public final class ExportText {

    /** The marks that close a quotation: the footnote mark goes behind them, not into the speech. */
    private static final String CLOSING_QUOTES = "\"”»’'」』";

    private ExportText() {
    }

    /**
     * @param mode     what the export holds
     * @param stored   the stored text of the part (the translation)
     * @param passages the translated passages of the part, in any order; may be null
     * @return the edits, empty when the mode keeps the text as it is or the part has no usable passage
     */
    public static List<Edit> edits(ExportMode mode, String stored, List<Located> passages) {
        String text = stored == null ? "" : stored;
        List<Edit> edits = new ArrayList<>();
        if (mode == ExportMode.TRANSLATED || passages == null) {
            return edits;
        }
        for (Located passage : passages) {
            if (!usable(passage, text)) {
                continue;
            }
            String translated = text.substring(passage.start(), passage.end());
            String source = passage.span().getSource();
            String replacement = mode.showsSource() ? source : translated;
            if (!mode.hasNotes()) {
                edits.add(Edit.replace(passage.start(), passage.end(), replacement));
                continue;
            }
            int end = passage.end();
            // the mark goes behind the closing quotation mark
            int markAt = end < text.length() && CLOSING_QUOTES.indexOf(text.charAt(end)) >= 0 ? end + 1 : end;
            edits.add(Edit.withNote(passage.start(), end, replacement, note(passage.span().getSpeaker(), mode.showsSource() ? translated : source), markAt));
        }
        return edits;
    }

    private static boolean usable(Located passage, String text) {
        return passage != null && passage.span() != null && passage.start() >= 0 && passage.end() <= text.length()
                && passage.end() > passage.start() && !passage.span().getSource().isEmpty()
                && !passage.span().isEdited(text.substring(passage.start(), passage.end()));
    }

    /**
     * The text of a footnote on one line (a line break would end the definition), with who speaks in front when it is known.
     */
    static String note(String speaker, String text) {
        String line = text.replaceAll("\\s*[\\r\\n]+\\s*", " ").trim();
        return StringUtils.isBlank(speaker) ? line : speaker.trim() + ": " + line;
    }
}
