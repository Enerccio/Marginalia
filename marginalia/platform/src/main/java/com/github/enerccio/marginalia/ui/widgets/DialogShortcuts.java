package com.github.enerccio.marginalia.ui.widgets;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextArea;

/**
 * The keyboard shortcut that sends a dialog: it clicks the dialog's main button while the cursor is in one of the given
 * fields. In a single line field (name, number, password) that is <kbd>Enter</kbd>; in a text area, where <kbd>Enter</kbd>
 * starts a new line, it is <kbd>Ctrl+Enter</kbd> (<kbd>Cmd+Enter</kbd> on a Mac), which a single line field accepts too. The
 * field gives up its focus before the click, so what was typed is on the server when the button reads it. A disabled or
 * hidden button does nothing. A {@code null} field is skipped.
 * <p>
 * Use it for the button that does what the dialog is for (Save, Generate, Send), never for a destructive one. Pair it with
 * {@link DialogFocus#onOpen} so the user can type at once.
 */
public final class DialogShortcuts {

    private DialogShortcuts() {
    }

    /**
     * Makes the keys described above click the button while one of the fields has the focus.
     */
    public static void submitWith(Button button, Component... fields) {
        for (Component field : fields) {
            if (field == null) {
                continue;
            }
            if (!(field instanceof TextArea)) {
                button.addClickShortcut(Key.ENTER).listenOn(field).resetFocusOnActiveElement();
            }
            button.addClickShortcut(Key.ENTER, KeyModifier.CONTROL).listenOn(field).resetFocusOnActiveElement();
            button.addClickShortcut(Key.ENTER, KeyModifier.META).listenOn(field).resetFocusOnActiveElement();
        }
    }
}
