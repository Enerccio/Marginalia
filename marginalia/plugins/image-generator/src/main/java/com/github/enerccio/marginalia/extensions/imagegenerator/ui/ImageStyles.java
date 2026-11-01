package com.github.enerccio.marginalia.extensions.imagegenerator.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * CSS classes used only by this plugin. The rules are in {@code image-generator.css} inside the bundle and the application puts
 * them into the page while the extension is loaded (see {@code ExtensionHasCss}).
 */
public final class ImageStyles {

    public static final String PREVIEW = "imagegen-preview";
    public static final String STATUS = "imagegen-status";
    public static final String SECTION = "imagegen-section";

    private static final String RESOURCE = "/image-generator.css";

    private static volatile String css;

    private ImageStyles() {
    }

    /** The text of {@code image-generator.css}, read once. */
    public static String css() {
        if (css == null) {
            try (InputStream in = ImageStyles.class.getResourceAsStream(RESOURCE)) {
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
