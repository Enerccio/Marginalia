package com.github.enerccio.marginalia.ui.widgets;

import com.flowingcode.vaadin.addons.fontawesome.FontAwesome.Solid;
import com.github.enerccio.marginalia.CssStyles;
import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.github.enerccio.marginalia.utils.UIUtils;
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

        // the field sizes its textarea from a measurement that goes stale (scrollbar appearing, late font): the end of the
        // text then cannot be scrolled to until the field is resized
        area.addAttachListener(e -> JavascriptLibrary.keepTextAreaFitted(area.getElement()));
        if (area.isAttached()) {
            JavascriptLibrary.keepTextAreaFitted(area.getElement());
        }

        // append (not setSuffixComponent) so a suffix already present, like the info icon, is kept
        area.getElement().appendChild(expand.getElement(), collapse.getElement());
    }

    private static Icon createToggleIcon(Solid solid, String tooltip) {
        Icon icon = solid.create();
        icon.setSize("var(--lumo-font-size-xs)");
        icon.getElement().setAttribute("title", tooltip);
        // the field's inner container scrolls the text and the icon is a child of it, so it is pinned to the visible
        // bottom right corner by shifting it with the scroll position the text area publishes as a CSS variable
        // (position: sticky does not work here, it stays inside the unscrolled height of the container).
        // It is pulled over the text instead of taking space from it.
        icon.addClassName(CssStyles.RESIZABLE_TEXT_AREA_ICON);
        icon.getElement().setAttribute(SLOT_ATTRIBUTE, "suffix");
        return icon;
    }

    private static void applyMode(TextArea area, String fixedHeight, boolean expanded) {
        if (UIUtils.isPhone()) {
            // the helper text of a field takes most of the height of the whole component on a narrow screen: size the field
            area.addClassName(CssStyles.RESIZABLE_TEXT_AREA_FIELD);
            area.setHeight(null);
            area.setMinHeight(null);
            area.getElement().getStyle().set("--resizable-text-area-height", expanded ? "auto" : fixedHeight);
            area.getElement().getStyle().set("--resizable-text-area-min-height", expanded ? fixedHeight : "0");
            return;
        }
        if (expanded) {
            area.setHeight(null);
            area.setMinHeight(fixedHeight);
        } else {
            area.setMinHeight(null);
            area.setHeight(fixedHeight);
        }
    }
}
