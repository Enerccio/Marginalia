package com.github.enerccio.marginalia.ui.dialogs;

import com.github.enerccio.marginalia.loc.L;
import com.github.enerccio.marginalia.loc.Localization;
import com.github.enerccio.marginalia.ui.widgets.DialogFocus;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextArea;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

/**
 * Asks for a description of something (a lorebook): a few lines of text, empty clears it.
 */
public final class DescriptionDialog {

    private DescriptionDialog() {
    }

    /**
     * @param title     what the dialog says, e.g. "Edit description"
     * @param current   the description now, null for none
     * @param onConfirm called with the new text (blank when it was cleared) when the user confirms
     */
    public static void show(Localization loc, String title, String current, Consumer<String> onConfirm) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(title);
        dialog.setWidth("min(600px, 95vw)");
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        TextArea field = new TextArea(loc.getValue(L.LABEL_DESCRIPTION));
        field.setWidthFull();
        field.setMinHeight("160px");
        field.setValue(StringUtils.defaultString(current));
        dialog.add(field);
        DialogFocus.onOpen(dialog, field);

        Button cancel = new Button(loc.getValue(L.LABEL_CANCEL), event -> dialog.close());
        Button ok = new Button(loc.getValue(L.LABEL_OK), event -> {
            dialog.close();
            onConfirm.accept(field.getValue());
        });
        ok.setThemeName("primary");
        dialog.getFooter().add(cancel, ok);
        dialog.open();
    }
}
