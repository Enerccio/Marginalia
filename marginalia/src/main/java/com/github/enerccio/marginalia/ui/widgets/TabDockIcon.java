package com.github.enerccio.marginalia.ui.widgets;

/**
 * What a tab of a {@link TabDock} shows in the bar of the minimized tabs: a colored circle (the color follows the id of the tab)
 * with a drawn {@linkplain #glyph(String) glyph} or with one or two {@linkplain #letters(String) letters}. A tab that is given no
 * icon gets the initials of its caption.
 * <p>
 * The tabs of the story have the icons below. An extension draws its own glyph: the SVG elements on a 24 x 24 grid, as
 * {@code <path d="..."/>}, drawn with a white 2 px round stroke and no fill (see {@link TabDockIcons#glyphUri}).
 */
public final class TabDockIcon {

    /** A list with bullets. */
    public static final TabDockIcon CHAPTERS = glyph(
            "<path d='M9 6h11M9 12h11M9 18h11'/><circle cx='4.5' cy='6' r='1' fill='white'/>"
                    + "<circle cx='4.5' cy='12' r='1' fill='white'/><circle cx='4.5' cy='18' r='1' fill='white'/>");
    /** An open book. */
    public static final TabDockIcon LOREBOOK = glyph(
            "<path d='M12 6.5C10.5 5 8 4.5 3.5 5v13c4.5-.5 7 0 8.5 1.5 1.5-1.5 4-2 8.5-1.5V5c-4.5-.5-7 0-8.5 1.5zM12 6.5v13'/>");
    /** A speech bubble with lines. */
    public static final TabDockIcon ANNOTATIONS = glyph(
            "<path d='M4 4.5h16v11H10.5L6 20v-4.5H4zM8 8.5h8M8 11.5h5'/>");

    private final String letters;
    private final String glyph;

    private TabDockIcon(String letters, String glyph) {
        this.letters = letters;
        this.glyph = glyph;
    }

    /**
     * @param letters one or two letters, the initials of the caption when null
     */
    public static TabDockIcon letters(String letters) {
        return new TabDockIcon(letters, null);
    }

    /**
     * @param markup the SVG elements of the glyph on a 24 x 24 grid, see {@link TabDockIcons#glyphUri}
     */
    public static TabDockIcon glyph(String markup) {
        return new TabDockIcon(null, markup);
    }

    /**
     * @param caption the caption of the tab, where the letters come from when the icon has none
     * @param key     the id of the tab, the color follows it
     * @return the {@code src} of an image
     */
    String dataUri(String caption, String key) {
        if (glyph != null) {
            return TabDockIcons.glyphUri(glyph, key);
        }
        return TabDockIcons.dataUri(letters != null ? letters : TabDockIcons.letters(caption), key);
    }

}
