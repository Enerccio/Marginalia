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
    /** A box that scrolls its content. */
    public static final String SCROLL = "conlang-scroll";
    /** The label of a status in the lexicon. */
    public static final String BADGE = "conlang-badge";

    /** A field of a change in the review: its name, what was and what is offered, what will be written. */
    public static final String DIFF_FIELD = "conlang-diff-field";
    /** The columns (here, there) of a field of a change. */
    public static final String DIFF_COLUMNS = "conlang-diff-columns";
    /** The text of a value in the review: its line breaks stay. */
    public static final String DIFF_TEXT = "conlang-diff-text";
    /** A conflict. */
    public static final String CONFLICT = "conlang-conflict";

    /** A message of the author in the conversation with the designer. */
    public static final String CHAT_AUTHOR = "conlang-chat-author";
    /** A message of the designer. */
    public static final String CHAT_DESIGNER = "conlang-chat-designer";

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
