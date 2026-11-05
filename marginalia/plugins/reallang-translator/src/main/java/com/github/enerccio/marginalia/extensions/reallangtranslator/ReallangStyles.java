package com.github.enerccio.marginalia.extensions.reallangtranslator;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * CSS classes used only by this plugin; the rules are in {@code reallangtranslator.css} inside the bundle and the application puts them into the
 * page while the extension is loaded (see {@code ExtensionHasCss}).
 */
public final class ReallangStyles {

    private static final String RESOURCE = "/reallangtranslator.css";

    private static volatile String css;

    private ReallangStyles() {
    }

    /**
     * The text of {@code reallangtranslator.css}, read once.
     */
    public static String css() {
        if (css == null) {
            try (InputStream in = ReallangStyles.class.getResourceAsStream(RESOURCE)) {
                if (in == null) {
                    throw new IllegalStateException(RESOURCE + " is missing from the bundle");
                }
                css = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return css;
    }
}
