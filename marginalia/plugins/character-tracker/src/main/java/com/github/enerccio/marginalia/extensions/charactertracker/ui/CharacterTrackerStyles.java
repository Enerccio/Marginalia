package com.github.enerccio.marginalia.extensions.charactertracker.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * CSS classes used only by this plugin. A plugin cannot add to the application's frontend bundle, so the rules are in
 * {@code charactertracker.css} inside the bundle and the application puts them into the page while the extension is
 * loaded (see {@code ExtensionHasCss}, implemented by {@code CharacterTrackerExtension}).
 */
public final class CharacterTrackerStyles {

    public static final String CARD = "character-tracker-card";
    public static final String CARD_BODY = "character-tracker-card-body";
    public static final String CARD_ACTIONS = "character-tracker-card-actions";
    public static final String NAME = "character-tracker-name";
    public static final String TAGLINE = "character-tracker-tagline";
    public static final String DETAILS = "character-tracker-details";
    public static final String ICON = "character-tracker-icon";
    public static final String NOTE = "character-tracker-note";
    public static final String COLOR_INPUT = "character-tracker-color-input";
    public static final String DIALOG_SECTION = "character-tracker-dialog-section";
    public static final String LORE_PREVIEW = "character-tracker-lore-preview";

    public static final String MAP_HINT = "character-tracker-map-hint";
    public static final String MAP_EMPTY = "character-tracker-map-empty";

    /**
     * Custom properties with the colors of a character, set on the elements that show it.
     */
    public static final String VAR_COLOR = "--character-color";
    public static final String VAR_TEXT = "--character-text";

    private static final String RESOURCE = "/charactertracker.css";

    private static volatile String css;

    private CharacterTrackerStyles() {
    }

    /**
     * The text of {@code charactertracker.css}, read once.
     */
    public static String css() {
        if (css == null) {
            try (InputStream in = CharacterTrackerStyles.class.getResourceAsStream(RESOURCE)) {
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
