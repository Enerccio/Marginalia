package com.github.enerccio.marginalia.extensions.ttsreader.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * CSS classes used only by this plugin. The rules are in {@code tts-reader.css} inside the bundle and the application
 * puts them into the page while the extension is loaded (see {@code ExtensionHasCss}). The state classes
 * ({@code tts-reader-playing}, {@code -active}, {@code -open}, {@code -reading}, {@code -unsupported}) are set by
 * {@code tts-reader.js}.
 */
public final class TtsReaderStyles {

    public static final String BAR = "tts-reader-bar";
    public static final String BUTTON_PLAY = "tts-reader-play";
    public static final String BUTTON_PAUSE = "tts-reader-pause";
    public static final String BUTTON_STOP = "tts-reader-stop";
    public static final String PANEL = "tts-reader-panel";
    public static final String FIELD = "tts-reader-field";

    private static volatile String css;
    private static volatile String script;

    private TtsReaderStyles() {
    }

    /** The text of {@code tts-reader.css}, read once. */
    public static String css() {
        return css == null ? css = read("/tts-reader.css") : css;
    }

    /** The script of the read-aloud, a function body, read once. */
    public static String script() {
        return script == null ? script = read("/tts-reader.js") : script;
    }

    private static String read(String resource) {
        try (InputStream in = TtsReaderStyles.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Missing " + resource);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
