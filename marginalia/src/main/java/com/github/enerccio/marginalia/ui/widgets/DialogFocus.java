package com.github.enerccio.marginalia.ui.widgets;

import com.github.enerccio.marginalia.ui.JavascriptLibrary;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.dialog.Dialog;

/**
 * Puts the cursor into the field a dialog is about, so the user can type right after it opens.
 */
public final class DialogFocus {

    private DialogFocus() {
    }

    /**
     * Focuses the component every time the dialog opens. A focus asked for right when the dialog opens is lost (the overlay is
     * not rendered yet), so it is retried until the component has it.
     */
    public static void onOpen(Dialog dialog, Focusable<?> target) {
        dialog.addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                JavascriptLibrary.focusAfterRender(target.getElement());
            }
        });
    }
}
