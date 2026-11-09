package com.github.enerccio.marginalia.extensions.conlangtranslator;

import com.github.enerccio.marginalia.ui.widgets.TabDockIcon;

/**
 * The icon of the plugin. An orc shouting: the speech of an invented language, a bubble of an abstract symbol. The glyph is drawn on a 24 x 24 grid
 * for {@link TabDockIcon#glyph} (white, 2 px round stroke, the inner details with a thinner one); {@code icon.svg} in the folder of the plugin is the
 * same drawing on the colored circle of the tab, for the README.
 */
public final class ConlangIcon {

    /** The SVG elements of the glyph. */
    public static final String GLYPH = "<path d='M18.1 1.5L19.1 3.7L21.7 3.8L20.9 6.1L22.9 7.6L20.7 8.9L21.1 11.3L18.5 11.0L17.1 13.1L15.4 11.3L12.9 12.1L12.8 9.7L10.3 8.8L11.9 6.9L10.7 4.8L13.2 4.2L13.7 1.9L16.0 3.0z'/>"
            + "<g stroke-width='1.1'><path d='M16.5 4.800000000000001v4.8M14.4 6.0l4.2 2.4M14.4 8.4l4.2-2.4'/></g>"
            + "<path d='M3 12h9l1.5 5-2.5 4.5H4L1.5 17z'/>"
            + "<path d='M3.2 12.2L1.2 8.6l4 1.8M11.8 12.2l1.2-2.6'/>"
            + "<path d='M4.4 14.6l2.2.9M10.6 14.6l-2.2.9'/>"
            + "<path d='M5.3 21.2v-3M9.7 21.2v-3'/>";

    public static final TabDockIcon TAB = TabDockIcon.glyph(GLYPH);

    private ConlangIcon() {
    }
}
