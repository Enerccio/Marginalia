package com.github.enerccio.marginalia.ui.widgets;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

/**
 * The icons of the tabs of a {@link TabDock}, shown in the bar of the minimized tabs: a colored circle with one or two letters,
 * drawn as SVG (a data URI, so a tab of an extension needs no resource). The color follows the id of the tab, so a tab keeps
 * its color in every session.
 */
public final class TabDockIcons {

    private TabDockIcons() {
    }

    /**
     * The letters a tab gets when its owner gave none: the initials of the first two words of the caption, or the first two
     * letters of a single word ({@code "Side Query"} is {@code SQ}, {@code "Lorebook"} is {@code Lo}).
     */
    public static String letters(String caption) {
        List<String> words = new ArrayList<>();
        for (String word : caption == null ? new String[0] : caption.replaceAll("['\u2019]", "").trim().split("[^\\p{L}\\p{N}]+")) {
            if (!word.isEmpty()) {
                words.add(word);
            }
        }
        if (words.isEmpty()) {
            return "?";
        }
        if (words.size() > 1) {
            return (first(words.get(0)) + first(words.get(1))).toUpperCase(Locale.ROOT);
        }
        String word = words.get(0);
        String head = first(word);
        String rest = word.substring(head.length());
        return head.toUpperCase(Locale.ROOT) + (rest.isEmpty() ? "" : first(rest).toLowerCase(Locale.ROOT));
    }

    private static String first(String text) {
        return text.substring(0, text.offsetByCodePoints(0, 1));
    }

    /**
     * The SVG of the icon as a data URI for the {@code src} of an image.
     *
     * @param letters one or two letters, anything longer is cut to two
     * @param key     what the color is taken from, the id of the tab
     */
    public static String dataUri(String letters, String key) {
        String text = letters == null || letters.isBlank() ? "?" : letters.strip();
        text = text.substring(0, text.offsetByCodePoints(0, Math.min(2, text.codePointCount(0, text.length()))));
        int hue = Math.floorMod(key == null ? 0 : key.hashCode(), 360);
        int size = text.codePointCount(0, text.length()) == 1 ? 17 : 13;
        String svg = "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 32 32'>"
                + "<circle cx='16' cy='16' r='15' fill='hsl(" + hue + ",55%,42%)'/>"
                + "<text x='16' y='16' dy='.35em' text-anchor='middle' font-family='sans-serif' font-weight='600' font-size='"
                + size + "' fill='white'>" + escape(text) + "</text></svg>";
        return "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

}
