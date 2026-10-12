package com.github.enerccio.marginalia.extensions.sidequery.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * CSS classes used only by this plugin. A plugin cannot add to the application's frontend bundle, so the rules are in
 * {@code sidequery.css} inside the bundle and the application puts them into the page while the extension is loaded
 * (see {@code ExtensionHasCss}, implemented by {@code SideQueryExtension}).
 */
public final class SideQueryStyles {

    public static final String BAR = "side-query-bar";
    public static final String CARD = "side-query-card";
    public static final String CARD_USER = "side-query-card-user";
    public static final String CARD_EXCLUDED = "side-query-card-excluded";

    private static final String RESOURCE = "/sidequery.css";

    private static volatile String css;

    private SideQueryStyles() {
    }

    /**
     * The text of {@code sidequery.css}, read once.
     */
    public static String css() {
        if (css == null) {
            try (InputStream in = SideQueryStyles.class.getResourceAsStream(RESOURCE)) {
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
