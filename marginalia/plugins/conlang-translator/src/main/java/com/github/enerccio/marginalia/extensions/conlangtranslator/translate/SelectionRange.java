package com.github.enerccio.marginalia.extensions.conlangtranslator.translate;

/**
 * The range the author selected in a part, cleaned for a translation: the spaces around it and a pair of quotation marks that enclose the
 * whole selection are not a part of what is translated (the quotation marks of a speech stay in the story, as they do for a translated part).
 */
public final class SelectionRange {

    private SelectionRange() {
    }

    /**
     * @return {@code [start, end)} without the spaces and the enclosing quotation marks; empty ({@code end <= start}) when nothing is left
     */
    public static int[] trim(String text, int start, int end) {
        int from = Math.max(0, start);
        int to = Math.min(text.length(), end);
        while (from < to && Character.isWhitespace(text.charAt(from))) {
            from++;
        }
        while (to > from && Character.isWhitespace(text.charAt(to - 1))) {
            to--;
        }
        if (to - from >= 2 && isOpening(text.charAt(from)) && isClosing(text.charAt(to - 1))) {
            from++;
            to--;
            while (from < to && Character.isWhitespace(text.charAt(from))) {
                from++;
            }
            while (to > from && Character.isWhitespace(text.charAt(to - 1))) {
                to--;
            }
        }
        return new int[]{from, to};
    }

    private static boolean isOpening(char c) {
        return c == '"' || c == '“' || c == '«' || c == '„';
    }

    private static boolean isClosing(char c) {
        return c == '"' || c == '”' || c == '»' || c == '“';
    }
}
