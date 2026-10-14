package com.github.enerccio.marginalia.extensions.arcoutline.ui;

import com.github.enerccio.marginalia.extensions.arcoutline.model.OutlineState;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * CSS classes used only by this plugin. A plugin cannot add to the application's frontend bundle, so the rules are in
 * {@code arcoutline.css} inside the bundle and the application puts them into the page while the extension is loaded
 * (see {@code ExtensionHasCss}, implemented by {@code ArcOutlineExtension}).
 */
public final class ArcOutlineStyles {

    public static final String LIST = "arc-outline-list";
    public static final String EMPTY = "arc-outline-empty";
    public static final String CARD = "arc-outline-card";
    public static final String ARC = "arc-outline-arc";
    public static final String CHAPTER = "arc-outline-chapter";
    public static final String EXPANDED = "arc-outline-expanded";
    public static final String HEAD = "arc-outline-head";
    public static final String BODY = "arc-outline-body";
    public static final String TITLE = "arc-outline-title";
    public static final String DESCRIPTION = "arc-outline-description";
    public static final String ACTIONS = "arc-outline-actions";
    public static final String NOTE = "arc-outline-note";
    public static final String GENERATION = "arc-outline-generation";
    public static final String GENERATION_HEADING = "arc-outline-generation-heading";
    public static final String GENERATION_TEXT = "arc-outline-generation-text";
    public static final String GENERATION_JSON = "arc-outline-generation-json";
    public static final String REASONING = "arc-outline-reasoning";

    private static final String RESOURCE = "/arcoutline.css";

    private static volatile String css;

    private ArcOutlineStyles() {
    }

    /**
     * The class that colors a card by the state of its arc or chapter.
     */
    public static String of(OutlineState state) {
        return switch (state) {
            case PLANNED -> "arc-outline-planned";
            case CURRENT -> "arc-outline-current";
            case FINISHED -> "arc-outline-finished";
        };
    }

    /**
     * The text of {@code arcoutline.css}, read once.
     */
    public static String css() {
        if (css == null) {
            try (InputStream in = ArcOutlineStyles.class.getResourceAsStream(RESOURCE)) {
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
