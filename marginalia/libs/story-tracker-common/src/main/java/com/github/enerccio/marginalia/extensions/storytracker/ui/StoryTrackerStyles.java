package com.github.enerccio.marginalia.extensions.storytracker.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * CSS classes of the library's screens (the progress panel, the review dialog), in {@code storytracker.css}. A plugin that embeds the
 * library returns {@link #css()} from its {@code getCss} together with its own rules.
 */
public final class StoryTrackerStyles {

    public static final String NAME = "story-tracker-name";
    public static final String COUNTS = "story-tracker-counts";
    public static final String CARD_HEADER = "story-tracker-card-header";
    public static final String CARD_TITLE = "story-tracker-card-title";
    public static final String PROGRESS = "story-tracker-progress";
    public static final String REASONING = "story-tracker-reasoning";
    public static final String REVIEW = "story-tracker-review";
    public static final String REVIEW_GROUP = "story-tracker-review-group";
    public static final String REVIEW_ROW = "story-tracker-review-row";
    public static final String REVIEW_BODY = "story-tracker-review-body";
    public static final String KIND = "story-tracker-kind";
    public static final String KIND_NOT = "story-tracker-kind-not";
    public static final String OLD_ITEM = "story-tracker-old-item";
    public static final String STALE = "story-tracker-stale";
    public static final String LOG_ROW = "story-tracker-log-row";
    public static final String LOG_TEXT = "story-tracker-log-text";

    private static final String RESOURCE = "/storytracker.css";

    private static volatile String css;

    private StoryTrackerStyles() {
    }

    /** The text of {@code storytracker.css}, read once. */
    public static String css() {
        if (css == null) {
            try (InputStream in = StoryTrackerStyles.class.getResourceAsStream(RESOURCE)) {
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
