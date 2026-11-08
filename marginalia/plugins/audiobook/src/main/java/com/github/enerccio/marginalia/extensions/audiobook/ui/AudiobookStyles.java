package com.github.enerccio.marginalia.extensions.audiobook.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * CSS classes used only by this plugin. The rules are in {@code audiobook.css} inside the bundle and the application puts them into the
 * page while the extension is loaded (see {@code ExtensionHasCss}).
 */
public final class AudiobookStyles {

    public static final String SECTION = "audiobook-section";
    public static final String STUDIO = "audiobook-studio";
    public static final String PLAYER = "audiobook-player";
    public static final String PLAYER_PLAY = "audiobook-player-play";
    public static final String PLAYER_PAUSE = "audiobook-player-pause";
    public static final String PLAYER_STOP = "audiobook-player-stop";
    public static final String PLAYER_STATUS = "audiobook-player-status";
    public static final String PLAYER_SPEED = "audiobook-player-speed";

    private static final String RESOURCE = "/audiobook.css";

    private static volatile String css;
    private static volatile String script;

    private AudiobookStyles() {
    }

    /** The text of {@code audiobook.css}, read once. */
    public static String css() {
        return css == null ? css = read(RESOURCE) : css;
    }

    /** The script of the player in the viewer, a function body ({@code audiobook-player.js}), read once. */
    public static String playerScript() {
        return script == null ? script = read("/audiobook-player.js") : script;
    }

    private static String read(String resource) {
        try (InputStream in = AudiobookStyles.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Missing " + resource);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
