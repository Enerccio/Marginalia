package com.github.enerccio.marginalia.domain.service.search;

import org.apache.commons.lang3.StringUtils;

/**
 * Turns text typed by the user into a part of a {@code LIKE ... ESCAPE '\'} pattern: {@code *} is a wildcard
 * ({@code %}), the characters that are special in {@code LIKE} are taken literally.
 */
public final class LikePatterns {

    private LikePatterns() {
    }

    public static String fromWildcards(String text) {
        return StringUtils.replaceEach(text,
                new String[]{"\\", "_", "%", "*"},
                new String[]{"\\\\", "\\_", "\\%", "%"});
    }

    /**
     * Turns text into a part of a {@code LIKE ... ESCAPE '\'} pattern that matches exactly that text: {@code *} is a
     * plain character here, unlike in {@link #fromWildcards}.
     */
    public static String literal(String text) {
        return StringUtils.replaceEach(text,
                new String[]{"\\", "_", "%"},
                new String[]{"\\\\", "\\_", "\\%"});
    }
}
