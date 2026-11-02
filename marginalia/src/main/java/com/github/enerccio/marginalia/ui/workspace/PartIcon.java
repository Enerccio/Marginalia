package com.github.enerccio.marginalia.ui.workspace;

import com.github.enerccio.marginalia.CssStyles;
import com.vaadin.flow.component.html.Span;

/**
 * The icons of the parts of the {@link DesktopWorkspace} (the tabs on its left side): line drawings on a 24 x 24 grid, a 2 px
 * round stroke in the color of the text ({@code currentColor}, so the icon follows the theme and turns to the primary color with
 * the selected tab) and no fill. The size and the stroke come from the {@link CssStyles#WORKSPACE_PART_ICON} rule, a drawing only
 * says the SVG elements.
 */
public enum PartIcon {

    /** A closed book with a title line. */
    BOOKS("<path d='M6.5 3H19v18H6.5A2.5 2.5 0 0 1 4 18.5v-13A2.5 2.5 0 0 1 6.5 3zM4 18.5A2.5 2.5 0 0 1 6.5 16H19M9 7.5h6'/>"),
    /** An open book, the one of the lorebook in the tabs of the story. */
    LOREBOOKS("<path d='M12 6.5C10.5 5 8 4.5 3.5 5v13c4.5-.5 7 0 8.5 1.5 1.5-1.5 4-2 8.5-1.5V5c-4.5-.5-7 0-8.5 1.5zM12 6.5v13'/>"),
    /** A person: the settings of the account. */
    SETTINGS("<circle cx='12' cy='8' r='4'/><path d='M4 21c0-4 3.6-7 8-7s8 3 8 7'/>"),
    /** Two sliders: the generation settings (temperature, top P...). */
    PROTOCOLS("<path d='M4 7h8M17 7h3M4 17h3M12 17h8'/><circle cx='14.5' cy='7' r='2.5'/><circle cx='9.5' cy='17' r='2.5'/>"),
    /** A chip with pins: the model. */
    MODELS("<rect x='7' y='7' width='10' height='10' rx='2'/>"
            + "<path d='M10 3v4M14 3v4M10 17v4M14 17v4M3 10h4M3 14h4M17 10h4M17 14h4'/>"),
    /** A robot head with an antenna. */
    AGENTS("<rect x='5' y='8' width='14' height='11' rx='3'/><path d='M12 8V5M9 13v.01M15 13v.01M9.5 16.5h5'/>"
            + "<circle cx='12' cy='3.8' r='1'/>"),
    /** A picture: the files of the user. */
    RESOURCES("<rect x='3.5' y='4.5' width='17' height='15' rx='2'/><circle cx='9' cy='10' r='1.8'/>"
            + "<path d='M3.5 17l5-4.5 4 3.5 3-2.5 5 4'/>"),
    /** A waste bin. */
    TRASH("<path d='M4 7h16M10 11v6M14 11v6M5.5 7l1 13a1 1 0 0 0 1 1h9a1 1 0 0 0 1-1l1-13M9 7V4h6v3'/>");

    private final String markup;

    PartIcon(String markup) {
        this.markup = markup;
    }

    /**
     * @return the SVG elements of the drawing on the 24 x 24 grid
     */
    public String markup() {
        return markup;
    }

    /**
     * @return the whole {@code <svg>} element of the icon
     */
    public String svg() {
        return "<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' aria-hidden='true'>" + markup + "</svg>";
    }

    /**
     * @return a new component with the icon, to put in front of the caption of the tab
     */
    public Span create() {
        Span span = new Span();
        span.addClassName(CssStyles.WORKSPACE_PART_ICON);
        span.getElement().setProperty("innerHTML", svg());
        return span;
    }

}
