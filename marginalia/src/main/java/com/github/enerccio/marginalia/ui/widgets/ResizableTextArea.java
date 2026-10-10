package com.github.enerccio.marginalia.ui.widgets;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.textfield.TextArea;

/**
 * Gives a {@link TextArea} two sizing modes: fixed height with an internal scrollbar (default) and growing to fit the
 * whole content. A small icon floating in the bottom right corner of the field switches between them.
 */
public final class ResizableTextArea {

    private static final String SLOT_ATTRIBUTE = "slot";

    private ResizableTextArea() {
    }

    /**
     * Makes the text area a fixed height one with the toggle icon.
     *
     * @param loc         localization for the toggle tooltip
     * @param area        text area to modify
     * @param fixedHeight css height used in the fixed mode, in expanded mode it is the minimal height
     */
    public static void install(Localization loc, TextArea area, String fixedHeight) {
        Icon expand = createToggleIcon(Solid.EXPAND_ALT, loc.getValue(L.LABEL_FIT_TEXT_AREA_TO_CONTENT));
        Icon collapse = createToggleIcon(Solid.COMPRESS_ALT, loc.getValue(L.LABEL_FIXED_TEXT_AREA_HEIGHT));
        collapse.setVisible(false);
        applyMode(area, fixedHeight, false);

        expand.addClickListener(e -> {
            applyMode(area, fixedHeight, true);
            expand.setVisible(false);
            collapse.setVisible(true);
        });
        collapse.addClickListener(e -> {
            applyMode(area, fixedHeight, false);
            collapse.setVisible(false);
            expand.setVisible(true);
        });

        // append (not setSuffixComponent) so a suffix already present, like the info icon, is kept
        area.getElement().appendChild(expand.getElement(), collapse.getElement());
    }

    private static Icon createToggleIcon(Solid solid, String tooltip) {
        Icon icon = solid.create();
        icon.setSize("var(--lumo-font-size-xs)");
        icon.getElement().setAttribute("title", tooltip);
        icon.getStyle().set("cursor", "pointer");
        icon.getStyle().set("color", "var(--lumo-secondary-text-color)");
        icon.getStyle().set("opacity", "0.7");
        // the field's inner container scrolls the text and the icon is a child of it, so it is pinned to the visible
        // bottom right corner by shifting it with the scroll position the text area publishes as a CSS variable
        // (position: sticky does not work here, it stays inside the unscrolled height of the container).
        // It is pulled over the text instead of taking space from it.
        icon.getStyle().set("align-self", "flex-end");
        icon.getStyle().set("position", "relative");
        icon.getStyle().set("top", "0");
        icon.getStyle().set("transform", "translateY(var(--_text-area-vertical-scroll-position, 0px))");
        icon.getStyle().set("margin-top", "0");
        icon.getStyle().set("margin-bottom", "var(--lumo-space-xs)");
        icon.getStyle().set("margin-left", "calc(-1 * var(--lumo-space-m))");
        icon.getStyle().set("margin-right", "var(--lumo-space-xs)");
        icon.getStyle().set("z-index", "1");
        icon.getElement().setAttribute(SLOT_ATTRIBUTE, "suffix");
        return icon;
    }

    private static void applyMode(TextArea area, String fixedHeight, boolean expanded) {
        if (expanded) {
            area.setHeight(null);
            area.setMinHeight(fixedHeight);
        } else {
            area.setMinHeight(null);
            area.setHeight(fixedHeight);
        }
    }
}
