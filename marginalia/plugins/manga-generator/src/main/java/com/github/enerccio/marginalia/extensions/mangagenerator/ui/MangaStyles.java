package com.github.enerccio.marginalia.extensions.mangagenerator.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * CSS classes used only by this plugin. A plugin cannot add to the application's frontend bundle, so the rules are in
 * {@code manga.css} inside the bundle and the application puts them into the page while the extension is loaded
 * (see {@code ExtensionHasCss}, implemented by {@code MangaGeneratorExtension}).
 */
public final class MangaStyles {

    public static final String BAR = "manga-bar";
    public static final String CARD = "manga-card";
    public static final String OUTPUT = "manga-output";
    public static final String EMPTY = "manga-empty";

    private static final String RESOURCE = "/manga.css";

    private static volatile String css;

    private MangaStyles() {
    }

    /**
     * The text of {@code manga.css}, read once.
     */
    public static String css() {
        if (css == null) {
            try (InputStream in = MangaStyles.class.getResourceAsStream(RESOURCE)) {
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
