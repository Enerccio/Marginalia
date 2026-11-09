package com.github.enerccio.marginalia.extensions.reallangtranslator;

import com.github.enerccio.marginalia.ui.widgets.TabDockIcon;

/**
 * The icon of the plugin. A person speaking: a bubble with a kanji, the speech of a real language. The glyph is drawn on a 24 x 24 grid
 * for {@link TabDockIcon#glyph} (white, 2 px round stroke, the inner details with a thinner one); {@code icon.svg} in the folder of the plugin is the
 * same drawing on the colored circle of the tab, for the README.
 */
public final class ReallangIcon {

    /** The SVG elements of the glyph. */
    public static final String GLYPH = "<path d='M10 2.5h12v8h-6.5L12 13.5v-3H10z'/>"
            + "<g stroke-width='1.1'><path d='M16 3.7v1M13.6 5.6h4.8M14.3 9.3l3.4-3.7M17.7 9.3l-3.4-3.7'/></g>"
            + "<circle cx='7' cy='12.5' r='2.5'/>"
            + "<path d='M2 22c0-3.3 2.2-5.5 5-5.5s5 2.2 5 5.5'/>";

    public static final TabDockIcon TAB = TabDockIcon.glyph(GLYPH);

    private ReallangIcon() {
    }
}
