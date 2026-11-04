package com.github.enerccio.marginalia.extensions.conlangtranslator.text;

import com.github.enerccio.marginalia.extensions.conlangtranslator.model.ExportMode;
import com.github.enerccio.marginalia.extensions.conlangtranslator.translate.Reverse.Located;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The text of a part as an export of the book gets it: the speech replaced by its English, and/or a footnote on each speech with the other
 * text. The footnotes are Markdown footnotes (<code>[^3]</code> in the text and <code>[^3]: ...</code> at the end of the part), numbered over
 * the whole export so that the labels are unique in the one document of the formats that join the parts.
 * <p>
 * A speech the author edited since it was translated has no source to be trusted: it stays as it is and has no footnote.
 */
public final class ExportText {

    /** The marks that close a quotation: the footnote mark goes behind them, not into the speech. */
    private static final String CLOSING_QUOTES = "\"”»’'」』";

    private ExportText() {
    }

    /**
     * @param text the text of the part as the exporter gets it
     * @param next the number of the first footnote of the part after this one
     */
    public record Rendered(String text, int next) {
    }

    /**
     * @param mode     what the export holds
     * @param stored   the stored text of the part (the conlang)
     * @param passages the translated passages of the part, in any order; may be null
     * @param first    the number of the first footnote of this part
     */
    public static Rendered render(ExportMode mode, String stored, List<Located> passages, int first) {
        String text = stored == null ? "" : stored;
        if (mode == ExportMode.CONLANG || passages == null || passages.isEmpty()) {
            return new Rendered(text, first);
        }
        List<Located> ordered = new ArrayList<>(passages);
        ordered.sort(Comparator.comparingInt(Located::start).thenComparingInt(Located::end));
        StringBuilder out = new StringBuilder();
        List<String> notes = new ArrayList<>();
        int number = first;
        int taken = 0;
        for (Located passage : ordered) {
            if (!usable(passage, text, taken)) {
                continue;
            }
            String conlang = text.substring(passage.start(), passage.end());
            String source = passage.span().getSource();
            out.append(text, taken, passage.start()).append(mode.showsSource() ? source : conlang);
            int end = passage.end();
            if (mode.hasNotes()) {
                // the mark goes behind the closing quotation mark
                if (end < text.length() && CLOSING_QUOTES.indexOf(text.charAt(end)) >= 0) {
                    out.append(text.charAt(end));
                    end++;
                }
                out.append("[^").append(number).append(']');
                notes.add("[^" + number + "]: " + note(passage.span().getSpeaker(), mode.showsSource() ? conlang : source));
                number++;
            }
            taken = end;
        }
        out.append(text, taken, text.length());
        for (String note : notes) {
            out.append("\n\n").append(note);
        }
        return new Rendered(out.toString(), number);
    }

    private static boolean usable(Located passage, String text, int taken) {
        return passage != null && passage.span() != null && passage.start() >= taken && passage.start() >= 0 && passage.end() <= text.length()
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
