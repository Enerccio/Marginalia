package com.github.enerccio.marginalia.extensions.storynotes.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * CSS classes used only by this plugin. A plugin cannot add to the application's frontend bundle, so the rules are in
 * {@code storynotes.css} inside the bundle and the application puts them into the page while the extension is loaded
 * (see {@code ExtensionHasCss}, implemented by {@code StoryNotesExtension}).
 */
public final class StoryNotesStyles {

    public static final String CARD = "story-notes-card";
    public static final String CARD_BODY = "story-notes-card-body";
    public static final String CARD_ACTIONS = "story-notes-card-actions";
    public static final String TITLE = "story-notes-title";
    public static final String PREVIEW = "story-notes-preview";
    public static final String TAGS = "story-notes-tags";
    public static final String TAG = "story-notes-tag";
    public static final String FLAG = "story-notes-flag";
    public static final String EMPTY = "story-notes-empty";

    public static final String LINKS = "story-notes-links";
    public static final String LINKS_LABEL = "story-notes-links-label";
    public static final String MISSING = "story-notes-missing";
    public static final String PREVIEW_BOX = "story-notes-preview-box";

    private static final String RESOURCE = "/storynotes.css";

    private static volatile String css;

    private StoryNotesStyles() {
    }

    /**
     * The text of {@code storynotes.css}, read once.
     */
    public static String css() {
        if (css == null) {
            try (InputStream in = StoryNotesStyles.class.getResourceAsStream(RESOURCE)) {
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
