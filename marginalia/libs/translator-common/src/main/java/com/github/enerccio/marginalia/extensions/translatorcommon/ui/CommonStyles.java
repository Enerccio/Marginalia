package com.github.enerccio.marginalia.extensions.translatorcommon.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * CSS classes of the screens the translator plugins share; the rules are in {@code translatorcommon.css} inside the library. Each plugin puts them
 * into the pages with its own CSS (see {@code ExtensionHasCss}), together with its own rules; the same rules from two plugins are harmless.
 */
public final class CommonStyles {

    /** The popover with the English source of a speech. */
    public static final String SOURCE_POPOVER = "translator-source-popover";
    /** The content of that popover. */
    public static final String SOURCE_BOX = "translator-source-box";
    /** The English text in it. */
    public static final String SOURCE_TEXT = "translator-source-text";
    /** The part of a published book that holds clickable speech: the popover is placed against it. */
    public static final String READER_PART = "translator-reader-part";
    /** The line under a field that says what the field is for. */
    public static final String HINT = "translator-hint";
    /** A conflict, a warning in the color of an error. */
    public static final String CONFLICT = "translator-conflict";

    private static final String RESOURCE = "/translatorcommon.css";

    private static volatile String css;

    private CommonStyles() {
    }

    /**
     * The text of {@code translatorcommon.css}, read once.
     */
    public static String css() {
        if (css == null) {
            try (InputStream in = CommonStyles.class.getResourceAsStream(RESOURCE)) {
                if (in == null) {
                    throw new IllegalStateException(RESOURCE + " is missing from the library");
                }
                css = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return css;
    }
}
