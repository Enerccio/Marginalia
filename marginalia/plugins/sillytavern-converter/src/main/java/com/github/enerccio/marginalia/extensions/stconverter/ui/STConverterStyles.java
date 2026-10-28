package com.github.enerccio.marginalia.extensions.stconverter.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * CSS classes and grid part names used only by this plugin. A plugin cannot add to the application's frontend bundle, so
 * the rules are in {@code stconverter.css} inside the bundle and the application puts them into the page while the
 * extension is loaded (see {@code ExtensionHasCss}, implemented by {@code STConverterExtension}).
 */
public final class STConverterStyles {

    public static final String ROOT = "stc-root";
    public static final String BAR = "stc-bar";
    public static final String TABS = "stc-tabs";
    public static final String LISTS = "stc-lists";
    public static final String PICKER = "stc-picker";
    public static final String PAD = "stc-pad";
    public static final String ELLIPSIS = "stc-ellipsis";
    public static final String HINT = "stc-hint";
    /** Part name of the cells of the messages in the range that the wand works on. */
    public static final String PART_IN_RANGE = "stc-in-range";
    /** Part name of the cells of the rows the user has reviewed. */
    public static final String PART_REVIEWED = "stc-reviewed";
    /** Part name of the cells of the row the "next unchecked" button went to. */
    public static final String PART_CURRENT = "stc-current";
    /** Part name of the cells of a row that can be clicked. */
    public static final String PART_CLICKABLE = "stc-clickable";

    private static final String RESOURCE = "/stconverter.css";

    private static volatile String css;

    private STConverterStyles() {
    }

    /**
     * The text of {@code stconverter.css}, read once.
     */
    public static String css() {
        if (css == null) {
            try (InputStream in = STConverterStyles.class.getResourceAsStream(RESOURCE)) {
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
