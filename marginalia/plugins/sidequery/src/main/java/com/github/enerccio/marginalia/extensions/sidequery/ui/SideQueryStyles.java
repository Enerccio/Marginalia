package com.github.enerccio.marginalia.extensions.sidequery.ui;

import com.vaadin.flow.component.UI;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * CSS classes used only by this plugin. A plugin cannot add to the application's frontend bundle, so the rules are in
 * {@code sidequery.css} inside the bundle and are injected into the page as a style element.
 */
public final class SideQueryStyles {

    public static final String BAR = "side-query-bar";
    public static final String CARD = "side-query-card";
    public static final String CARD_USER = "side-query-card-user";
    public static final String CARD_EXCLUDED = "side-query-card-excluded";

    private static final String RESOURCE = "/sidequery.css";
    private static final String STYLE_ID = "side-query-styles";

    private static volatile String css;

    private SideQueryStyles() {
    }

    /**
     * Adds the stylesheet to the page of the current UI, does nothing when it is already there.
     */
    public static void install() {
        UI ui = UI.getCurrent();
        if (ui == null) {
            return;
        }
        ui.getPage().executeJs("""
                if (!document.getElementById($0)) {
                  const style = document.createElement('style');
                  style.id = $0;
                  style.textContent = $1;
                  document.head.appendChild(style);
                }
                """, STYLE_ID, css());
    }

    private static String css() {
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
