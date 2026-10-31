package com.github.enerccio.marginalia.extensions.betareaders.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * CSS classes used only by this plugin. A plugin cannot add to the application's frontend bundle, so the rules are in
 * {@code betareaders.css} inside the bundle and the application puts them into the page while the extension is loaded
 * (see {@code ExtensionHasCss}, implemented by {@code BetaReadersExtension}).
 */
public final class BetaReadersStyles {

    /** The icon of a persona in a list. */
    public static final String ICON = "beta-readers-icon";
    /** The icon of a persona in its editor. */
    public static final String ICON_LARGE = "beta-readers-icon-large";
    public static final String HINT = "beta-readers-hint";
    /** The bar with the tabs of the chats. */
    public static final String BAR = "beta-readers-bar";
    /** Separates the box to write in from the conversation above it. */
    public static final String TOP = "beta-readers-top";
    /** A persona of a chat in the panel of the chat: its icon and its text. */
    public static final String PERSONA = "beta-readers-persona";
    /** The list of the personas of a chat: it scrolls, so a long list leaves room for the conversation. */
    public static final String PERSONAS = "beta-readers-personas";
    public static final String PERSONA_LEFT = "beta-readers-persona-left";
    /** A message of the user. */
    public static final String USER = "beta-readers-user";
    /** A line of the history that is not a message: a persona joined or left. */
    public static final String NOTE = "beta-readers-note";
    /** A turn of the model: the reasoning and the messages of the personas. */
    public static final String TURN = "beta-readers-turn";
    /** One message of a turn: the icon, and the name and the parts of the speaker. */
    public static final String SPOKEN = "beta-readers-spoken";
    /** The space where the icon of a speaker would be, so the narrator lines up with the personas. */
    public static final String NO_ICON = "beta-readers-no-icon";
    public static final String ACTION = "beta-readers-action";
    /** A row of small buttons (edit, delete...) at the end of a message; hidden while a reply is being written. */
    public static final String ACTIONS = "beta-readers-actions";
    /** Put on the panel of a chat while a reply is being written. */
    public static final String BUSY = "beta-readers-busy";
    /** A line with a name at the start and buttons at the end. */
    public static final String HEADLINE = "beta-readers-headline";
    public static final String TEXT = "beta-readers-text";

    private static final String RESOURCE = "/betareaders.css";

    private static volatile String css;

    private BetaReadersStyles() {
    }

    /**
     * The text of {@code betareaders.css}, read once.
     */
    public static String css() {
        if (css == null) {
            try (InputStream in = BetaReadersStyles.class.getResourceAsStream(RESOURCE)) {
                if (in == null) {
                    throw new IllegalStateException("Missing " + RESOURCE);
                }
                css = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return css;
    }
}
