package com.github.enerccio.marginalia.ui.menu;

import com.github.enerccio.marginalia.CssStyles;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;

/**
 * What a menu item of the story editor shows: an optional icon, the text and, pushed to the end, the key combination of its action.
 */
final class MenuItemContent extends Div {

    private final Span text = new Span();
    private final Span key = new Span();

    MenuItemContent(Icon icon) {
        addClassName(CssStyles.MENU_ITEM_CONTENT);
        if (icon != null) {
            add(icon);
        }
        text.addClassName(CssStyles.MENU_ITEM_TEXT);
        key.addClassName(CssStyles.MENU_ITEM_KEY);
        add(text, key);
        key.setVisible(false);
    }

    void set(String label, String keyCombination) {
        text.setText(label);
        key.setText(keyCombination == null ? "" : keyCombination);
        key.setVisible(keyCombination != null);
    }
}
