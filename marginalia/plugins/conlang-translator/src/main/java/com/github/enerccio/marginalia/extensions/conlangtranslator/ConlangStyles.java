package com.github.enerccio.marginalia.extensions.conlangtranslator;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * CSS classes used only by this plugin; the rules are in {@code conlangtranslator.css} inside the bundle and the application puts them into
 * the page while the extension is loaded (see {@code ExtensionHasCss}).
 */
public final class ConlangStyles {

    /** The mark over a translated speech. */
    public static final String PASSAGE = "conlang-passage";

    /** The line under a field that says what the field is for. */
    public static final String HINT = "conlang-hint";
    /** A word of the language in a list: the form as the story writes it. */
    public static final String WORD = "conlang-word";
    /** The part of an editor that grows and keeps its own scroll. */
    public static final String FILL = "conlang-fill";
    /** The label of a status in the lexicon. */
    public static final String BADGE = "conlang-badge";

    private static final String RESOURCE = "/conlangtranslator.css";

    private static volatile String css;

    private ConlangStyles() {
    }

    /**
     * The text of {@code conlangtranslator.css}, read once.
     */
    public static String css() {
        if (css == null) {
            try (InputStream in = ConlangStyles.class.getResourceAsStream(RESOURCE)) {
                if (in == null) {
                    throw new IllegalStateException(RESOURCE + " is missing from the bundle");
                }
                css = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return css;
    }
}
