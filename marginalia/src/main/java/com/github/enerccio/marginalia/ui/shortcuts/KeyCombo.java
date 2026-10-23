package com.github.enerccio.marginalia.ui.shortcuts;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A key combination in its canonical text form, the one the browser script records and matches and the user settings
 * store: the modifiers in the order {@code Ctrl}, {@code Alt}, {@code Shift}, {@code Meta}, then the key, joined by
 * {@code +} (for example {@code Ctrl+Enter}, {@code Ctrl+Shift+k}, {@code +}). The key is the {@code key} of the browser
 * event: a character in lower case, {@code Space}, or a name such as {@code Enter}, {@code Escape}, {@code ArrowUp},
 * {@code F5}. {@code Shift} is not written for a character that is neither a letter nor a digit ({@code +} already says
 * that it was typed with it), whatever the keyboard layout does to type it.
 * <p>
 * A key without {@code Ctrl}, {@code Alt} or {@code Meta} is a "single key": it fires only when no input has the focus,
 * otherwise it would collide with typing.
 */
public final class KeyCombo {

    private static final List<String> MODIFIERS = List.of("Ctrl", "Alt", "Shift", "Meta");
    private static final Pattern FUNCTION_KEY = Pattern.compile("F([1-9]|1[0-9]|2[0-4])");
    private static final Set<String> NAMED_KEYS = Set.of("Enter", "Escape", "Tab", "Space", "Backspace", "Delete", "Insert",
            "Home", "End", "PageUp", "PageDown", "ArrowUp", "ArrowDown", "ArrowLeft", "ArrowRight");

    /**
     * Combinations the browser keeps for itself (a page can't see or stop them): tabs and windows, Ctrl and Cmd alike.
     */
    private static final Set<String> BROWSER_KEYS = Set.of(
            "Ctrl+n", "Ctrl+t", "Ctrl+w", "Ctrl+Shift+n", "Ctrl+Shift+t", "Ctrl+Shift+w", "Ctrl+Tab", "Ctrl+Shift+Tab",
            "Ctrl+PageUp", "Ctrl+PageDown", "Alt+F4", "F11", "F12",
            "Meta+n", "Meta+t", "Meta+w", "Meta+q", "Meta+h", "Meta+m", "Meta+Shift+n", "Meta+Shift+t", "Meta+Shift+w");

    private KeyCombo() {
    }

    /**
     * The canonical form of the text, or null when it is not a key combination ({@code null} or blank, no key, a
     * modifier alone, an unknown key name).
     */
    public static String normalize(String text) {
        if (text == null) {
            return null;
        }
        if (text.equals(" ")) {
            return "Space";
        }
        if (text.isBlank()) {
            return null;
        }
        String rest = text.strip();
        boolean ctrl = false, alt = false, shift = false, meta = false;
        boolean found = true;
        while (found && rest.length() > 1) {
            found = false;
            for (String modifier : MODIFIERS) {
                if (rest.regionMatches(true, 0, modifier + "+", 0, modifier.length() + 1)) {
                    switch (modifier) {
                        case "Ctrl" -> ctrl = true;
                        case "Alt" -> alt = true;
                        case "Shift" -> shift = true;
                        default -> meta = true;
                    }
                    rest = rest.substring(modifier.length() + 1);
                    found = true;
                    break;
                }
            }
        }
        String key = normalizeKey(rest);
        if (key == null) {
            return null;
        }
        if (key.length() == 1 && !Character.isLetterOrDigit(key.charAt(0))) {
            shift = false;
        }
        StringBuilder result = new StringBuilder();
        if (ctrl) {
            result.append("Ctrl+");
        }
        if (alt) {
            result.append("Alt+");
        }
        if (shift) {
            result.append("Shift+");
        }
        if (meta) {
            result.append("Meta+");
        }
        return result.append(key).toString();
    }

    private static String normalizeKey(String key) {
        if (key.codePointCount(0, key.length()) == 1) {
            return key.equals(" ") ? "Space" : key.toLowerCase();
        }
        for (String named : NAMED_KEYS) {
            if (named.equalsIgnoreCase(key)) {
                return named;
            }
        }
        String function = key.toUpperCase();
        return FUNCTION_KEY.matcher(function).matches() ? function : null;
    }

    public static boolean isValid(String text) {
        return normalize(text) != null;
    }

    /**
     * Whether the (canonical) combination has {@code Ctrl}, {@code Alt} or {@code Meta}, so it can't be typed text and
     * fires also while an input has the focus.
     */
    public static boolean hasModifier(String combo) {
        return combo != null && (combo.contains("Ctrl+") || combo.contains("Alt+") || combo.contains("Meta+"));
    }

    /**
     * Whether the browser (or the OS) keeps the combination, so the page never gets it.
     */
    public static boolean isBrowserReserved(String combo) {
        return combo != null && BROWSER_KEYS.contains(combo);
    }

    /**
     * Whether the combination is {@code Alt} with a letter or a digit, which types a character on a Mac (and on layouts
     * with a right Alt) instead of reaching the page as a shortcut.
     */
    public static boolean isAltCharacter(String combo) {
        if (combo == null || !combo.contains("Alt+")) {
            return false;
        }
        String key = combo.substring(combo.lastIndexOf('+') + 1);
        if (combo.endsWith("++")) {
            key = "+";
        }
        return key.length() == 1 && Character.isLetterOrDigit(key.charAt(0));
    }
}
