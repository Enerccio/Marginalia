package com.github.enerccio.marginalia.extensions.loregenerator.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

/** Asks for the name of a prompt to save. */
public class SavePromptAsDialog extends Dialog {

    /** @param onName gets the name, not empty, after the dialog closed */
    public SavePromptAsDialog(Consumer<String> onName) {
        setHeaderTitle("Save prompt as");
        TextField name = new TextField("Name");
        name.setWidthFull();
        add(name);
        Button save = new Button("Save", e -> {
            String newName = StringUtils.trimToNull(name.getValue());
            if (newName == null) {
                name.setInvalid(true);
                name.setErrorMessage("Name can't be empty");
                return;
            }
            close();
            onName.accept(newName);
        });
        save.setThemeName("primary");
        getFooter().add(new Button("Cancel", e -> close()), save);
        addOpenedChangeListener(e -> {
            if (e.isOpened()) {
                name.focus();
            }
        });
    }
}
