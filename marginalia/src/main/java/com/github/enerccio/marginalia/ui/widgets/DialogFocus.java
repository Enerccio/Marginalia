package com.github.enerccio.marginalia.ui.widgets;

import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.dialog.Dialog;

/**
 * Puts the cursor into the field a dialog is about, so the user can type right after it opens.
 */
public final class DialogFocus {

    private DialogFocus() {
    }

    /**
     * Focuses the component every time the dialog opens.
     */
    public static void onOpen(Dialog dialog, Focusable<?> target) {
        dialog.addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                target.focus();
            }
        });
    }
}
