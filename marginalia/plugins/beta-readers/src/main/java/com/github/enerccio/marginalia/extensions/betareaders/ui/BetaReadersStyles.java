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
