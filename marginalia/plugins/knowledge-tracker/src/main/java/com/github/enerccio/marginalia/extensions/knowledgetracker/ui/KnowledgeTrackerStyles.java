package com.github.enerccio.marginalia.extensions.knowledgetracker.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * CSS classes used only by this plugin. A plugin cannot add to the application's frontend bundle, so the rules are in
 * {@code knowledgetracker.css} inside the bundle and the application puts them into the page while the extension is
 * loaded (see {@code ExtensionHasCss}, implemented by {@code KnowledgeTrackerExtension}).
 */
public final class KnowledgeTrackerStyles {

    public static final String CARD = "knowledge-tracker-card";
    public static final String CARD_HEADER = "knowledge-tracker-card-header";
    public static final String CARD_TITLE = "knowledge-tracker-card-title";
    public static final String NAME = "knowledge-tracker-name";
    public static final String ALIASES = "knowledge-tracker-aliases";
    public static final String COUNTS = "knowledge-tracker-counts";
    public static final String ACTIONS = "knowledge-tracker-actions";
    public static final String BODY = "knowledge-tracker-body";
    public static final String ITEM = "knowledge-tracker-item";
    public static final String ITEM_TEXT = "knowledge-tracker-item-text";
    public static final String KIND = "knowledge-tracker-kind";
    public static final String KIND_NOT = "knowledge-tracker-kind-not";
    public static final String OLD = "knowledge-tracker-old";
    public static final String OLD_ITEM = "knowledge-tracker-old-item";
    public static final String MET = "knowledge-tracker-met";
    public static final String EMPTY = "knowledge-tracker-empty";
    public static final String LIST = "knowledge-tracker-list";
    public static final String REVIEW = "knowledge-tracker-review";
    public static final String REVIEW_GROUP = "knowledge-tracker-review-group";
    public static final String REVIEW_ROW = "knowledge-tracker-review-row";
    public static final String REVIEW_BODY = "knowledge-tracker-review-body";
    public static final String REVIEW_KIND = "knowledge-tracker-review-kind";
    public static final String PROGRESS = "knowledge-tracker-progress";
    public static final String PANEL = "knowledge-tracker-panel";
    public static final String STALE = "knowledge-tracker-stale";
    public static final String REASONING = "knowledge-tracker-reasoning";
    public static final String SETTINGS = "knowledge-tracker-settings";
    public static final String SETTINGS_TITLE = "knowledge-tracker-settings-title";

    private static final String RESOURCE = "/knowledgetracker.css";

    private static volatile String css;

    private KnowledgeTrackerStyles() {
    }

    /**
     * The text of {@code knowledgetracker.css}, read once.
     */
    public static String css() {
        if (css == null) {
            try (InputStream in = KnowledgeTrackerStyles.class.getResourceAsStream(RESOURCE)) {
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
