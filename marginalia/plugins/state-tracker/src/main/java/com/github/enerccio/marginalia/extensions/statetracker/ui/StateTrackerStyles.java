package com.github.enerccio.marginalia.extensions.statetracker.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * CSS classes used only by this plugin. The rules are in {@code statetracker.css} inside the bundle and the application puts them
 * into the page while the extension is loaded (see {@code ExtensionHasCss}, implemented by {@code StateTrackerExtension}).
 */
public final class StateTrackerStyles {

    public static final String LIST = "state-tracker-list";
    public static final String CARD = "state-tracker-card";
    public static final String CARD_HEADER = "state-tracker-card-header";
    public static final String CARD_TITLE = "state-tracker-card-title";
    public static final String NAME = "state-tracker-name";
    public static final String MUTED = "state-tracker-muted";
    public static final String CATEGORY = "state-tracker-category";
    public static final String CATEGORY_HEADER = "state-tracker-category-header";
    public static final String VALUE = "state-tracker-value";
    public static final String VALUE_TEXT = "state-tracker-value-text";
    public static final String HISTORY = "state-tracker-history";
    public static final String BADGE = "state-tracker-badge";
    public static final String BADGE_WARNING = "state-tracker-badge-warning";
    public static final String SETTINGS = "state-tracker-settings";
    public static final String CATEGORIES = "state-tracker-categories";
    public static final String DIALOG_ACTIONS = "state-tracker-dialog-actions";
    public static final String PANEL = "state-tracker-panel";
    public static final String SOURCE = "state-tracker-source";
    public static final String TOOLBAR = "state-tracker-toolbar";
    public static final String DOCK = "state-tracker-dock";

    private static final String RESOURCE = "/statetracker.css";

    private static volatile String css;

    private StateTrackerStyles() {
    }

    /** The text of {@code statetracker.css}, read once. */
    public static String css() {
        if (css == null) {
            try (InputStream in = StateTrackerStyles.class.getResourceAsStream(RESOURCE)) {
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
