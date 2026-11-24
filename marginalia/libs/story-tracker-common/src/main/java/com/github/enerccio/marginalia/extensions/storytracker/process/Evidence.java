package com.github.enerccio.marginalia.extensions.storytracker.process;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Whether a quote the model gave as evidence is in the text of a part.
 */
public final class Evidence {

    private static final Pattern ELLIPSIS = Pattern.compile("\\.{3}|…");
    private static final Pattern SPACES = Pattern.compile("\\s+");

    private Evidence() {
    }

    /**
     * Whether the quote is in the text (case, spacing and the kind of quotation marks do not matter; a quote with "..." has to have all
     * its pieces in it).
     */
    public static boolean found(String text, String evidence) {
        String haystack = norm(text);
        for (String piece : ELLIPSIS.split(evidence == null ? "" : evidence)) {
            String wanted = norm(piece);
            if (!wanted.isEmpty() && !haystack.contains(wanted)) {
                return false;
            }
        }
        return !norm(evidence == null ? "" : ELLIPSIS.matcher(evidence).replaceAll("")).isEmpty();
    }

    /** Lower case, one kind of quotes, spaces collapsed, no punctuation at the ends. */
    public static String norm(String text) {
        if (text == null) {
            return "";
        }
        String s = text.replace('“', '"').replace('”', '"').replace('‘', '\'').replace('’', '\'').toLowerCase(Locale.ROOT);
        s = SPACES.matcher(s).replaceAll(" ").strip();
        return s.replaceAll("^[\\p{P}\\s]+|[\\p{P}\\s]+$", "");
    }
}
